/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.mediationstats.data.publisher.conf;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationPublisherException;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.TenantMediationStatConfigData;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RegistryPersistenceManager {

    private static Log log = LogFactory.getLog(RegistryPersistenceManager.class);
    private static RegistryService registryService;
    private static MediationStatConfig eventConfiguration = new MediationStatConfig();
    public static final String EMPTY_STRING = "";

    public static void setRegistryService(RegistryService registryServiceParam) {
        registryService = registryServiceParam;
    }


    /**
     * Loads configuration from Registry.
     */
    public MediationStatConfig load() {

        MediationStatConfig mediationStatConfig = new MediationStatConfig();
        // First set it to defaults, but do not persist
        mediationStatConfig.setEnableMediationStats(false);
        mediationStatConfig.setUrl(EMPTY_STRING);
        mediationStatConfig.setUserName(EMPTY_STRING);
        mediationStatConfig.setPassword(EMPTY_STRING);
        mediationStatConfig.setProperties(new Property[0]);

        // then load it from registry
        try {

            String mediationStatsEnable = getConfigurationProperty(
                    MediationDataPublisherConstants.ENABLE_MEDIATION_STATS);
            String url = getConfigurationProperty(BAMDataPublisherConstants.BAM_URL);
            String userName = getConfigurationProperty(BAMDataPublisherConstants.BAM_USER_NAME);
            String password = getConfigurationProperty(BAMDataPublisherConstants.BAM_PASSWORD);

            String streamName = getConfigurationProperty(BAMDataPublisherConstants.STREAM_NAME);
            String version = getConfigurationProperty(BAMDataPublisherConstants.VERSION);
            String description = getConfigurationProperty(BAMDataPublisherConstants.DESCRIPTION);
            String nickName = getConfigurationProperty(BAMDataPublisherConstants.NICK_NAME);

            Properties properties = getAllConfigProperties(MediationDataPublisherConstants.MEDIATION_STATISTICS_PROPERTIES_REG_PATH);

            if (mediationStatsEnable != null && url != null && userName != null && password != null) {

                int tenantId = CarbonContext.getCurrentContext().getTenantId();
                Map<Integer, MediationStatConfig> tenantEventConfigData =
                        TenantMediationStatConfigData.getTenantSpecificEventingConfigData();
                tenantEventConfigData.put(tenantId, mediationStatConfig);

                mediationStatConfig.setEnableMediationStats(Boolean.parseBoolean(mediationStatsEnable));
                mediationStatConfig.setUrl(url);
                mediationStatConfig.setUserName(userName);
                mediationStatConfig.setPassword(password);

                mediationStatConfig.setStreamName(streamName);
                mediationStatConfig.setVersion(version);
                mediationStatConfig.setDescription(description);
                mediationStatConfig.setNickName(nickName);

                if (properties != null) {
                    List<Property> propertyDTOList = new ArrayList<Property>();
                    String[] keys = properties.keySet().toArray(new String[properties.size()]);
                    for (int i = keys.length - 1; i >= 0; i--) {
                        String key = keys[i];
                        Property propertyDTO = new Property();
                        propertyDTO.setKey(key);
                        propertyDTO.setValue(((List<String>) properties.get(key)).get(0));
                        propertyDTOList.add(propertyDTO);
                    }

                    mediationStatConfig.setProperties(propertyDTOList.toArray(new Property[propertyDTOList.size()]));
                }

            } else {
                // Registry does not have eventing config
                update(mediationStatConfig);
            }
        } catch (Exception e) {
            log.error("Coul not load values from registry", e);
        }
        return mediationStatConfig;
    }

    private Properties getAllConfigProperties(String mediationStatisticsPropertiesRegPath)
            throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Properties properties = null;
        Properties filterProperties = null;

        if (registry.resourceExists(mediationStatisticsPropertiesRegPath)) {
            Resource resource = registry.get(mediationStatisticsPropertiesRegPath);
            properties = resource.getProperties();
            if (properties != null) {
                filterProperties = new Properties();
                for (Map.Entry<Object, Object> keyValuePair : properties.entrySet()) {
                    //When using mounted registry it keeps some properties starting with "registry." we don't need it.
                    if (!keyValuePair.getKey().toString().startsWith(BAMDataPublisherConstants.PREFIX_FOR_REGISTRY_HIDDEN_PROPERTIES)) {
                        filterProperties.put(keyValuePair.getKey(), keyValuePair.getValue());
                    }
                }

            }
        }
        return filterProperties;
    }

    /**
     * Updates all properties of a resource
     *
     * @param properties
     * @param registryPath
     */
    public void updateAllProperties(Properties properties, String registryPath)
            throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());

        // Always creating a new resource because properties should be replaced and overridden
        Resource resource = registry.newResource();

        resource.setProperties(properties);
        registry.put(registryPath, resource);
    }


    /**
     * Read the resource from registry
     *
     * @param propertyName
     * @return
     * @throws RegistryException
     * @throws MediationPublisherException
     */
    public String getConfigurationProperty(String propertyName)
            throws RegistryException, MediationPublisherException {
        String resourcePath = MediationDataPublisherConstants.MEDIATION_STATISTICS_REG_PATH + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        String value = null;
        if (registry != null) {
            try {
                if (registry.resourceExists(resourcePath)) {
                    Resource resource = registry.get(resourcePath);
                    value = resource.getProperty(propertyName);
                }
            } catch (Exception e) {
                throw new MediationPublisherException("Error while accessing registry", e);
            }
        }
        return value;
    }

    /**
     * Updates the Registry with given config data.
     *
     * @param eventConfig eventing configuration data
     */
    public void update(MediationStatConfig eventConfig) {
        try {

            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            Map<Integer, MediationStatConfig> tenantEventConfigData =
                    TenantMediationStatConfigData.getTenantSpecificEventingConfigData();
            tenantEventConfigData.put(tenantId, eventConfig);

            updateConfigProperty(MediationDataPublisherConstants.ENABLE_MEDIATION_STATS,
                                 eventConfig.isEnableMediationStats());
            updateConfigProperty(BAMDataPublisherConstants.BAM_URL, eventConfig.getUrl());
            updateConfigProperty(BAMDataPublisherConstants.BAM_USER_NAME,
                                 eventConfig.getUserName());
            updateConfigProperty(BAMDataPublisherConstants.BAM_PASSWORD,
                                 eventConfig.getPassword());
            updateConfigProperty(BAMDataPublisherConstants.STREAM_NAME,
                                 eventConfig.getStreamName());
            updateConfigProperty(BAMDataPublisherConstants.VERSION,
                                 eventConfig.getVersion());
            updateConfigProperty(BAMDataPublisherConstants.NICK_NAME,
                                 eventConfig.getNickName());
            updateConfigProperty(BAMDataPublisherConstants.DESCRIPTION,
                                 eventConfig.getDescription());


            Property[] propertiesDTO = eventConfig.getProperties();
            if (propertiesDTO != null) {
                Properties properties = new Properties();
                for (int i = 0; i < propertiesDTO.length; i++) {
                    Property property = propertiesDTO[i];
                    List<String> valueList = new ArrayList<String>();
                    valueList.add(property.getValue());
                    properties.put(property.getKey(), valueList);
                }
                updateAllProperties(properties, MediationDataPublisherConstants.MEDIATION_STATISTICS_PROPERTIES_REG_PATH);
            }else {
                updateAllProperties(null, MediationDataPublisherConstants.MEDIATION_STATISTICS_PROPERTIES_REG_PATH);
            }

        } catch (Exception e) {
            log.error("Could not update the registry", e);
        }
    }

    /**
     * Update the properties
     *
     * @param propertyName
     * @param value
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws MediationPublisherException
     */
    public void updateConfigProperty(String propertyName, Object value)
            throws RegistryException, MediationPublisherException {
        String resourcePath = MediationDataPublisherConstants.MEDIATION_STATISTICS_REG_PATH + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Resource resource;
        if (registry != null) {
            try {
                if (!registry.resourceExists(resourcePath)) {
                    resource = registry.newResource();
                    resource.addProperty(propertyName, value.toString());
                    registry.put(resourcePath, resource);
                } else {
                    resource = registry.get(resourcePath);
                    resource.setProperty(propertyName, value.toString());
                    registry.put(resourcePath, resource);
                }
            } catch (Exception e) {
                throw new MediationPublisherException("Error while accessing registry", e);
            }
        }
    }

    public MediationStatConfig getEventingConfigData() {
        return load();
    }


}
