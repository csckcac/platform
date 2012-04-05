/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.brokermanager.core.internal.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
import org.wso2.carbon.brokermanager.core.internal.util.BMConstants;
import org.wso2.carbon.brokermanager.core.internal.util.RegistryHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broker configuration storing in registry
 */
public class BrokerConfigurationRegistryInvoker {
    private static final Log log = LogFactory.getLog(BrokerConfigurationRegistryInvoker.class);

    private static final String BROKER_BASE = "/Brokers";
//    private Registry registry = null;

    public BrokerConfigurationRegistryInvoker(int tenantId) throws BMConfigurationException{
        Registry registry ;
        // get registry object
        try {
             registry = RegistryHolder.getInstance().getRegistry(tenantId);
        } catch (RegistryException e) {
           log.error("Error in getting registry for the super tenant");
           throw new BMConfigurationException("Error in getting registry for the super tenant", e);
        }
        try {
            // create broker base if not exists
            if (!registry.resourceExists(BROKER_BASE)) {
                registry.put(BROKER_BASE, registry.newCollection());
            }
        } catch (RegistryException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to create new collection in registry", e);
            }
            throw new BMConfigurationException("Failed to create new collection in registry", e);
        }
    }

    /**
     * Put given broker configuration to registry
     *
     * @param brokerConfiguration - broker configuration to be stored in registry
     */
    public void saveConfigurationToRegistry(BrokerConfiguration brokerConfiguration, int tenantId) throws BMConfigurationException{
        String brokerName = brokerConfiguration.getName();
        String pathToBroker = BROKER_BASE + "/" + brokerName;
        Resource brokerResource = null;
        Registry registry = null;
        try {
            registry = RegistryHolder.getInstance().getRegistry(tenantId);
        } catch (RegistryException e) {
           log.error("Error in getting registry for the tenant :" + tenantId ,e);
            throw new BMConfigurationException("Error in getting registry for the tenant :"+tenantId ,e);
        }
        try {
            brokerResource = registry.newResource();
            Map<String, String> propertyMap = brokerConfiguration.getProperties();
            brokerResource.addProperty("name", brokerConfiguration.getName());
            brokerResource.addProperty("type", brokerConfiguration.getType());
            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                brokerResource.addProperty(entry.getKey(), entry.getValue());
            }
        } catch (RegistryException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to create new resource in registry", e);
            }
            throw new BMConfigurationException("Failed to create new resource in registry", e);
        }
        try {
            registry.put(pathToBroker, brokerResource);
        } catch (RegistryException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to saveConfigurationToRegistry new resource in registry", e);
            }
            throw new BMConfigurationException("Failed to saveConfigurationToRegistry new resource in registry", e);
        }
    }

    /**
     * Remove the broker from registry
     *
     * @param name - broker name to be removed from registry
     */
    public void removeConfigurationFromRegistry(String name, int tenantId) throws BMConfigurationException{
        String pathToBroker = BROKER_BASE + "/" + name;
        try {
            Registry registry = RegistryHolder.getInstance().getRegistry(tenantId);
            if (registry.resourceExists(pathToBroker)) {
                registry.delete(pathToBroker);
                if (log.isInfoEnabled()) {
                    log.info("Broker configuration with name " + name + " successfully removed from registry.");
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error("No such broker configuration exists to delete. requested broker for delete " + name);
                }
            }
        } catch (RegistryException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to removeConfigurationFromRegistry " + name + " from registry.");
            }
            throw new BMConfigurationException("Failed to removeConfigurationFromRegistry " + name + " from registry.", e);
        }
    }

    /**
     * Get Broker configurations from registry
     *
     * @return Map of broker configurations
     */
    public Map<String, BrokerConfiguration> getAllBrokerConfigurations(int tenantId) throws BMConfigurationException{
        Map<String, BrokerConfiguration> brokerConfigurationMap = new ConcurrentHashMap<String, BrokerConfiguration>();
        try {
            Registry registry = RegistryHolder.getInstance().getRegistry(tenantId);
            Resource brokerConfigurationsResource = registry.get(BROKER_BASE);
            if (brokerConfigurationsResource != null) {
                Object resourceContent = brokerConfigurationsResource.getContent();
                if (resourceContent instanceof String[]) {
                    String[] brokerConfigurationPaths = (String[]) resourceContent;
                    BrokerConfiguration brokerConfiguration = null;
                    for (String brokerConfigurationPath : brokerConfigurationPaths) {
                        brokerConfiguration = new BrokerConfiguration();
                        Resource brokerConfigurationResource = registry.get(brokerConfigurationPath);
                        if (brokerConfigurationResource != null) {
                            Hashtable propertiesHashTable = brokerConfigurationResource.getProperties();
                            Enumeration e = propertiesHashTable.keys();
                            while (e.hasMoreElements()) {
                                String propertyName = (String) e.nextElement();
                                ArrayList propertyValueList = (ArrayList) propertiesHashTable.get(propertyName);
                                String propertyValue = propertyValueList.get(0).toString();
                                if ("name".equals(propertyName)) {
                                    brokerConfiguration.setName(propertyValue);
                                } else if ("type".equals(propertyName)) {
                                    brokerConfiguration.setType(propertyValue);
                                } else {
                                    brokerConfiguration.addProperty(propertyName, propertyValue);
                                }

                            }
                            brokerConfigurationMap.put(brokerConfiguration.getName(), brokerConfiguration);

                        }
                    }
                }
            }
        } catch (RegistryException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource with path " + BROKER_BASE);
            }
            throw new BMConfigurationException("Failed to get resource with path " + BROKER_BASE , e);
        }
        return brokerConfigurationMap;
    }
}