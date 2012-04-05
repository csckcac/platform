/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.data.publisher.mediationstats.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.mediationstats.MDPublisherConstants;
import org.wso2.carbon.bam.data.publisher.mediationstats.MediationPublisherException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Registry persistence manager handles persisting of eventing configuration data to the registry as well as
 * loading the configuration from the registry.
 */
public class RegistryPersistenceManager {
    private static Log log = LogFactory.getLog(RegistryPersistenceManager.class);
    private static Registry registry;

    private static MediationStatConfig eventConfig = new MediationStatConfig();

    public RegistryPersistenceManager() {
        load();
    }

    public static void setRegistry(Registry registryParam) {
        registry = registryParam;
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
        String resourcePath = MDPublisherConstants.STATISTICS_REG_PATH + propertyName;
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
     * Update the properties
     *
     * @param propertyName
     * @param value
     * @throws RegistryException
     * @throws MediationPublisherException
     */
    public void updateConfigProperty(String propertyName, String value)
            throws RegistryException, MediationPublisherException {
        String resourcePath = MDPublisherConstants.STATISTICS_REG_PATH + propertyName;
        Resource resource;
        if (registry != null) {
            try {
                if (!registry.resourceExists(resourcePath)) {
                    resource = registry.newResource();
                    resource.addProperty(propertyName, value);
                    registry.put(resourcePath, resource);
                } else {
                    resource = registry.get(resourcePath);
                    resource.setProperty(propertyName, value);
                    registry.put(resourcePath, resource);
                }
            } catch (Exception e) {
                throw new MediationPublisherException("Error while accessing registry", e);
            }
        }
    }

    /**
     * Loads configuration from Registry.
     */
    private void load() {
        // First set it to defaults, but do not persist
        eventConfig.setEnableEventing(MDPublisherConstants.ENABLE_EVENTING_DEFAULT);
        eventConfig.setProxyRequestCountThreshold(MDPublisherConstants.PROXY_COUNT_THRESHOLD_DEFAULT);
        eventConfig.setEndpointRequestCountThreshold(MDPublisherConstants.ENDPOINT_COUNT_THRESHOLD_DEFAULT);
        eventConfig.setSequenceRequestCountThreshold(MDPublisherConstants.SEQUENCE_COUNT_THRESHOLD_DEFAULT);

        // then load it from registry
        try {
            String eventingStatus;

            eventingStatus = getConfigurationProperty(MDPublisherConstants.ENABLE_EVENTING);

            if (eventingStatus != null) { // Registry has eventing config
                eventConfig.setEnableEventing(eventingStatus);

                String proxyCount = MDPublisherConstants.PROXY_COUNT_THRESHOLD;
                int proxyCountThreshold = Integer.parseInt(getConfigurationProperty(proxyCount));
                eventConfig.setProxyRequestCountThreshold(proxyCountThreshold);

                String endPointCount = MDPublisherConstants.ENDPOINT_COUNT_THRESHOLD;
                int endPointCountThreshold = Integer.parseInt(getConfigurationProperty(endPointCount));
                eventConfig.setEndpointRequestCountThreshold(endPointCountThreshold);

                String sequenceCount = MDPublisherConstants.SEQUENCE_COUNT_THRESHOLD;
                int sequenceThreshold = Integer.parseInt(getConfigurationProperty(sequenceCount));
                eventConfig.setSequenceRequestCountThreshold(sequenceThreshold);
            } else {
                // Registry does not have eventing config
                update(eventConfig);
            }
        } catch (Exception e) {
            log.error("Coul not load values from registry", e);
        }

    }

    /**
     * Updates the Registry with given config data.
     *
     * @param eventConfig eventing configuration data
     */
    public void update(MediationStatConfig eventConfig) {
        try {
            updateConfigProperty(MDPublisherConstants.ENABLE_EVENTING, eventConfig.getEnableEventing());
            String proxyCountThreshold = Integer.toString(eventConfig.getProxyRequestCountThreshold());
            updateConfigProperty(MDPublisherConstants.PROXY_COUNT_THRESHOLD, proxyCountThreshold);
            String endpointCountThreshold = Integer.toString(eventConfig.getEndpointRequestCountThreshold());
            updateConfigProperty(MDPublisherConstants.ENDPOINT_COUNT_THRESHOLD, endpointCountThreshold);
            String sequenceCountThreshold = Integer.toString(eventConfig.getSequenceRequestCountThreshold());
            updateConfigProperty(MDPublisherConstants.SEQUENCE_COUNT_THRESHOLD, sequenceCountThreshold);
        }
        catch (Exception e) {
            log.error("Could not update the registry", e);
        }
        RegistryPersistenceManager.eventConfig = eventConfig;
    }

    public MediationStatConfig getEventingConfigData() {
        return eventConfig;
    }

}

