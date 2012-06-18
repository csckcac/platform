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
package org.wso2.carbon.bam.service.data.publisher.conf;


import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.CommonConstants;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.*;

public class RegistryPersistenceManager {

    private static RegistryService registryService;
    private static EventingConfigData eventingConfigData = new EventingConfigData();
    public static final String EMPTY_STRING = "";

    public static void setRegistryService(RegistryService registryServiceParam) {
        registryService = registryServiceParam;
    }


    /**
     * Updates configuration property to a new value.
     *
     * @param propertyName Name of the property to be updated.
     * @param value        New value of the property
     * @throws RegistryException
     */
    public void updateConfigurationProperty(String propertyName, String value)
            throws RegistryException {
        String resourcePath = ServiceStatisticsPublisherConstants.SERVICE_STATISTICS_REG_PATH + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Resource resource;
        if (!registry.resourceExists(resourcePath)) {
            resource = registry.newResource();
            resource.addProperty(propertyName, value);
            registry.put(resourcePath, resource);
        } else {
            resource = registry.get(resourcePath);
            resource.setProperty(propertyName, value);
            registry.put(resourcePath, resource);
        }
    }

    /**
     * Loads configuration from Registry.
     */
    public EventingConfigData load() {
        EventingConfigData eventingConfigData = new EventingConfigData();
        // First set it to defaults, but do not persist
        eventingConfigData.setServiceStatsEnable(false);
        eventingConfigData.setMsgDumpingEnable(false);
        eventingConfigData.setUrl(EMPTY_STRING);
        eventingConfigData.setPassword(EMPTY_STRING);
        eventingConfigData.setUserName(EMPTY_STRING);
        eventingConfigData.setProperties(new Property[0]);

        // then load it from registry
        try {
            String serviceStatsStatus = getConfigurationProperty(
                    ServiceStatisticsPublisherConstants.SERVICE_STATISTICS_REG_PATH,
                    ServiceStatisticsPublisherConstants.ENABLE_SERVICE_STATS_EVENTING);

            String activityStatus = getConfigurationProperty(
                    ActivityPublisherConstants.ACTIVITY_REG_PATH,
                    ActivityPublisherConstants.ENABLE_ACTIVITY);

            String bamUrl = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                     BAMDataPublisherConstants.BAM_URL);
            String bamUserName = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                          BAMDataPublisherConstants.BAM_USER_NAME);
            String bamPassword = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                          BAMDataPublisherConstants.BAM_PASSWORD);

            String streamName = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                         BAMDataPublisherConstants.STREAM_NAME);
            String version = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                      BAMDataPublisherConstants.VERSION);
            String description = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                          BAMDataPublisherConstants.DESCRIPTION);
            String nickName = getConfigurationProperty(CommonConstants.SERVICE_COMMON_REG_PATH,
                                                       BAMDataPublisherConstants.NICK_NAME);

            Properties properties = getAllConfigProperties(CommonConstants.SERVICE_PROPERTIES_REG_PATH);

            if (serviceStatsStatus != null && activityStatus != null && bamUrl != null && bamUserName != null &&
                bamPassword != null) {

                eventingConfigData.setServiceStatsEnable(Boolean.parseBoolean(serviceStatsStatus));
                eventingConfigData.setMsgDumpingEnable(Boolean.parseBoolean(activityStatus));
                eventingConfigData.setUrl(bamUrl);
                eventingConfigData.setUserName(bamUserName);
                eventingConfigData.setPassword(bamPassword);
                eventingConfigData.setStreamName(streamName);
                eventingConfigData.setVersion(version);
                eventingConfigData.setDescription(description);
                eventingConfigData.setNickName(nickName);

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

                    eventingConfigData.setProperties(propertyDTOList.toArray(new Property[propertyDTOList.size()]));
                }

                int tenantId = CarbonContext.getCurrentContext().getTenantId();
                Map<Integer, EventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
                tenantEventConfigData.put(tenantId, eventingConfigData);

            } else { // Registry does not have eventing config. Set to defaults.
                update(eventingConfigData);
            }
        } catch (Exception ignored) {
            // If something went wrong, then we have the default, or whatever loaded so far
        }
        return eventingConfigData;
    }


    /**
     * Updates configuration property to a new value.
     *
     * @param propertyName Name of the property to be updated.
     * @param value        New value of the property
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    public void updateConfigurationProperty(String propertyName, Object value, String registryPath)
            throws RegistryException {
        String resourcePath = registryPath + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Resource resource;
        if (!registry.resourceExists(resourcePath)) {
            resource = registry.newResource();
            resource.addProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        } else {
            resource = registry.get(resourcePath);
            resource.setProperty(propertyName, String.valueOf(value));
            registry.put(resourcePath, resource);
        }
    }

    /**
     * Updates all properties of a resource
     * @param properties
     * @param registryPath
     */
    public void updateAllProperties(Properties properties, String registryPath) throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());

        // Always creating a new resource because properties should be replaced and overridden
        Resource resource = registry.newResource();

        resource.setProperties(properties);
        registry.put(registryPath, resource);
    }


    /**
     * Updates the Registry with given config data.
     *
     * @param eventingConfigData eventing configuration data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          thrown when updating the registry properties fails.
     */
    public void update(EventingConfigData eventingConfigData) throws RegistryException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<Integer, EventingConfigData> tenantEventConfigData = TenantEventConfigData.getTenantSpecificEventingConfigData();
        tenantEventConfigData.put(tenantId, eventingConfigData);

        updateConfigurationProperty(ServiceStatisticsPublisherConstants.ENABLE_SERVICE_STATS_EVENTING,
                                    eventingConfigData.isServiceStatsEnable(),
                                    ServiceStatisticsPublisherConstants.SERVICE_STATISTICS_REG_PATH);
        updateConfigurationProperty(ActivityPublisherConstants.ENABLE_ACTIVITY,
                                    eventingConfigData.isMsgDumpingEnable(),
                                    ActivityPublisherConstants.ACTIVITY_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_URL, eventingConfigData.getUrl(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_USER_NAME, eventingConfigData.getUserName(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_PASSWORD, eventingConfigData.getPassword(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);

        updateConfigurationProperty(BAMDataPublisherConstants.STREAM_NAME, eventingConfigData.getStreamName(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.VERSION, eventingConfigData.getVersion(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.NICK_NAME, eventingConfigData.getNickName(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);
        updateConfigurationProperty(BAMDataPublisherConstants.DESCRIPTION, eventingConfigData.getDescription(),
                                    CommonConstants.SERVICE_COMMON_REG_PATH);

        Property[] propertiesDTO = eventingConfigData.getProperties();
        if (propertiesDTO != null) {
            Properties properties = new Properties();
            for (int i = 0; i < propertiesDTO.length; i++) {
                Property property = propertiesDTO[i];
                List<String> valueList = new ArrayList<String>();
                valueList.add(property.getValue());
                properties.put(property.getKey(), valueList);
            }
            updateAllProperties(properties, CommonConstants.SERVICE_PROPERTIES_REG_PATH);
        }else {
            updateAllProperties(null, CommonConstants.SERVICE_PROPERTIES_REG_PATH);
        }

    }


    /**
     * Fetches the value of the property with propertyName from registry. Returns null if no property
     * exists with the given name.
     *
     * @param propertyName Name of the property to be fetched.
     * @return Property value
     * @throws RegistryException
     */
    public String getConfigurationProperty(String registryPath, String propertyName)
            throws RegistryException {
        String resourcePath = registryPath + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        String value = null;
        if (registry.resourceExists(resourcePath)) {
            Resource resource = registry.get(resourcePath);
            value = resource.getProperty(propertyName);
        }
        return value;
    }

    /**
     * Fetches all properties for any registry resource
     * @param registryPath
     * @return properties
     * @throws RegistryException
     */
    public Properties getAllConfigProperties(String registryPath) throws RegistryException {
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Properties properties = null;
        Properties filterProperties= null;
//        Properties reverseProperties = null;
        if (registry.resourceExists(registryPath)) {
            Resource resource = registry.get(registryPath);
            properties = resource.getProperties();
            if (properties != null) {
                filterProperties = new Properties();
                for (Map.Entry<Object, Object> keyValuePair : properties.entrySet()) {
                    //When using mounted registry it keeps some properties starting with "registry." we don't need it.
                    if(!keyValuePair.getKey().toString().startsWith(BAMDataPublisherConstants.PREFIX_FOR_REGISTRY_HIDDEN_PROPERTIES)){
                        filterProperties.put(keyValuePair.getKey(),keyValuePair.getValue());
                    }
                }

            }
        }
        return filterProperties;
    }


    public EventingConfigData getEventingConfigData() {
        return load();
    }


}
