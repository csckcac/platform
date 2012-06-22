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

package org.wso2.carbon.brokermanager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
import org.wso2.carbon.brokermanager.core.BrokerManagerService;
import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
import org.wso2.carbon.brokermanager.core.internal.registry.BrokerConfigurationRegistryInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the broker manager.
 */
public class CarbonBrokerManagerService implements BrokerManagerService {
    private static final Log log = LogFactory.getLog(CarbonBrokerManagerService.class);
    /**
     * broker configuration map to keep the broker configuration details
     */

    private Map<Integer, Map<String, BrokerConfiguration>> tenantSpecificBrokerConfigurationMap;


    /**
     * Broker configuration registry access through brokerConfigurationRegistry
     */
    private BrokerConfigurationRegistryInvoker brokerConfigurationRegistryInvoker;

    public CarbonBrokerManagerService() {
        tenantSpecificBrokerConfigurationMap = new ConcurrentHashMap<Integer, Map<String, BrokerConfiguration>>();
    }

    public void loadConfigurations(int tenantId) throws BMConfigurationException{
        brokerConfigurationRegistryInvoker = new BrokerConfigurationRegistryInvoker(tenantId);
        if (brokerConfigurationRegistryInvoker.getAllBrokerConfigurations(tenantId).size()>0) {
            tenantSpecificBrokerConfigurationMap.put(tenantId
                    , brokerConfigurationRegistryInvoker.getAllBrokerConfigurations(tenantId));
        }

    }

    public void addBrokerConfiguration(BrokerConfiguration brokerConfiguration,
                                       int tenantId) throws BMConfigurationException{
        Map<String, BrokerConfiguration> brokerConfigurationMap = tenantSpecificBrokerConfigurationMap.get(tenantId);
        if (brokerConfigurationMap == null) {
            brokerConfigurationMap = new ConcurrentHashMap<String, BrokerConfiguration>();
            brokerConfigurationMap.put(brokerConfiguration.getName(), brokerConfiguration);
            tenantSpecificBrokerConfigurationMap.put(tenantId, brokerConfigurationMap);
        } else {
            brokerConfigurationMap.put(brokerConfiguration.getName(), brokerConfiguration);
        }
        brokerConfigurationRegistryInvoker.saveConfigurationToRegistry(brokerConfiguration, tenantId);
    }


    public void addBrokerConfigurationForSuperTenant(
            BrokerConfiguration brokerConfiguration) throws BMConfigurationException{
       Map<String, BrokerConfiguration> brokerConfigurationMap
               = tenantSpecificBrokerConfigurationMap.get(MultitenantConstants.SUPER_TENANT_ID);
        if (brokerConfigurationMap == null) {
            brokerConfigurationMap = new ConcurrentHashMap<String, BrokerConfiguration>();
            brokerConfigurationMap.put(brokerConfiguration.getName(), brokerConfiguration);
            tenantSpecificBrokerConfigurationMap.put(MultitenantConstants.SUPER_TENANT_ID, brokerConfigurationMap);
        } else {
            brokerConfigurationMap.put(brokerConfiguration.getName(), brokerConfiguration);
        }
        brokerConfigurationRegistryInvoker.saveConfigurationToRegistry(brokerConfiguration,
                                                                       MultitenantConstants.SUPER_TENANT_ID);
    }

    public void removeBrokerConfiguration(String name,
                                          int tenantId) throws BMConfigurationException{
        tenantSpecificBrokerConfigurationMap.get(tenantId).remove(name);
        brokerConfigurationRegistryInvoker.removeConfigurationFromRegistry(name,tenantId);
    }

    public List<BrokerConfiguration> getAllBrokerConfigurations(int tenantId) {
        List<BrokerConfiguration> brokerConfigurations = new ArrayList<BrokerConfiguration>();
        if (tenantSpecificBrokerConfigurationMap.get(tenantId)!= null) {
            for (BrokerConfiguration brokerConfiguration : tenantSpecificBrokerConfigurationMap.get(
                    tenantId).values()) {
                brokerConfigurations.add(brokerConfiguration);
            }
        }
        return brokerConfigurations;
    }

    public List<String> getAllBrokerConfigurationNames(int tenantId) {
        List<String> brokerProxyNames = new ArrayList<String>();
        if (tenantSpecificBrokerConfigurationMap.get(tenantId)!= null) {
            for (BrokerConfiguration brokerConfiguration : tenantSpecificBrokerConfigurationMap.get(
                    tenantId).values()) {
                brokerProxyNames.add(brokerConfiguration.getName());
            }
        }
        return brokerProxyNames;
    }

    public BrokerConfiguration getBrokerConfiguration(String name, int tenantId) {
        if(tenantSpecificBrokerConfigurationMap.get(tenantId) == null){
            try {
                loadConfigurations(tenantId);
            } catch (BMConfigurationException e) {
                log.error("Error in loading tenant specific broker configurations" , e);
            }
        }
        return tenantSpecificBrokerConfigurationMap.get(tenantId).get(name);
    }

}
