/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.activity.mediation.data.publisher.conf;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.TenantActivityConfigData;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.Map;

public class RegistryPersistenceManager {

    private static final Log log = LogFactory.getLog(RegistryPersistenceManager.class);


    private static RegistryService registryService;
    private static final String EMPTY_STRING = "";

    public static void setRegistryService(RegistryService registryServiceParam) {
        registryService = registryServiceParam;
    }

    public void updateConfigurationProperty(String propertyName, Object value)
            throws RegistryException {
        String resourcePath = ActivityPublisherConstants.ACTIVITY_REG_PATH + "/" + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        Resource resource;
        if (registry != null) {
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
                resource.addProperty(propertyName, value.toString());
                registry.put(resourcePath, resource);
            } else {
                resource = registry.get(resourcePath);
                resource.setProperty(propertyName, value.toString());
                registry.put(resourcePath, resource);
            }
        }
    }


    /**
     * Loads configuration from Registry.
     */
    public ActivityConfigData load() {

        ActivityConfigData activityConfigData = new ActivityConfigData();
        // First set it to defaults, but do not persist
        activityConfigData.setMessageDumpingEnable(false);
        activityConfigData.setUrl(EMPTY_STRING);
        activityConfigData.setUserName(EMPTY_STRING);
        activityConfigData.setPassword(EMPTY_STRING);
        activityConfigData.setHttpTransportEnable(false);
        activityConfigData.setSocketTransportEnable(true);
        activityConfigData.setPort(7611);

        // then load it from registry
        try {
            String activityEnable = getConfigurationProperty(
                    ActivityPublisherConstants.ENABLE_ACTIVITY);
            String bamUrl = getConfigurationProperty(BAMDataPublisherConstants.BAM_URL);
            String bamPassword = getConfigurationProperty(BAMDataPublisherConstants.BAM_PASSWORD);
            String bamUserName = getConfigurationProperty(BAMDataPublisherConstants.BAM_USER_NAME);
            String httpTransportEnable = getConfigurationProperty(BAMDataPublisherConstants.ENABLE_HTTP_TRANSPORT);
            String socketTransportEnable = getConfigurationProperty(BAMDataPublisherConstants.ENABLE_SOCKET_TRANSPORT);
            String port = getConfigurationProperty(BAMDataPublisherConstants.BAM_SOCKET_PORT);

            if (activityEnable != null && bamUrl != null && bamPassword != null && bamUserName != null) {

                activityConfigData.setMessageDumpingEnable(Boolean.parseBoolean(activityEnable));
                activityConfigData.setUrl(bamUrl);
                activityConfigData.setUserName(bamUserName);
                activityConfigData.setPassword(bamPassword);
                activityConfigData.setHttpTransportEnable(Boolean.parseBoolean(httpTransportEnable));
                activityConfigData.setSocketTransportEnable(Boolean.parseBoolean(socketTransportEnable));
                if (port != null) {
                    activityConfigData.setPort(Integer.parseInt(port));
                }

                int tenantId = CarbonContext.getCurrentContext().getTenantId();
                Map<Integer, ActivityConfigData> tenantEventConfigData =
                        TenantActivityConfigData.getTenantSpecificEventingConfigData();
                tenantEventConfigData.put(tenantId, activityConfigData);
            } else {
                update(activityConfigData);
            }

        } catch (Exception e) {
            log.error("Could not load properties from registry", e);
        }
        return activityConfigData;
    }

    /**
     * Read the resource from registry
     *
     * @param propertyName
     * @return
     * @throws RegistryException
     * @throws
     */
    public String getConfigurationProperty(String propertyName)
            throws RegistryException {
        String resourcePath = ActivityPublisherConstants.ACTIVITY_REG_PATH + "/" + propertyName;
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        String value = null;
        if (registry != null) {
            try {
                if (registry.resourceExists(resourcePath)) {
                    Resource resource = registry.get(resourcePath);
                    value = resource.getProperty(propertyName);
                }
            } catch (Exception e) {
                throw new RegistryException("Error while accessing registry", e);
            }
        }
        return value;
    }


    /**
     * Updates the Registry with given config data.
     *
     * @param eventingConfigData eventing configuration data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          thrown when updating the registry properties fails.
     */
    public void update(ActivityConfigData eventingConfigData) throws RegistryException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<Integer, ActivityConfigData> tenantEventConfigData =
                TenantActivityConfigData.getTenantSpecificEventingConfigData();
        tenantEventConfigData.put(tenantId, eventingConfigData);

        updateConfigurationProperty(ActivityPublisherConstants.ENABLE_ACTIVITY, eventingConfigData.
                isMessageDumpingEnable());
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_URL, eventingConfigData.
                getUrl());
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_USER_NAME, eventingConfigData.
                getUserName());
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_PASSWORD, eventingConfigData.
                getPassword());
        updateConfigurationProperty(BAMDataPublisherConstants.ENABLE_HTTP_TRANSPORT,
                                    eventingConfigData.isHttpTransportEnable());
        updateConfigurationProperty(BAMDataPublisherConstants.ENABLE_SOCKET_TRANSPORT,
                                    eventingConfigData.isSocketTransportEnable());
        updateConfigurationProperty(BAMDataPublisherConstants.BAM_SOCKET_PORT,
                                    eventingConfigData.getPort());

    }


    public ActivityConfigData getEventingConfigData() {
        return load();
    }


}
