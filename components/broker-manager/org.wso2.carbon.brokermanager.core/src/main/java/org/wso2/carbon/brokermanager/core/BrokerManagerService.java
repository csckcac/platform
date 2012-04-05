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

package org.wso2.carbon.brokermanager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;

import java.util.List;

public interface BrokerManagerService {

    /**
     * use to add a new Broker Configuration instance to the system. A Broker Configuration instance represents the
     * details of a pirticular broker connection details.
     * @param brokerConfiguration - broker configuration to be added
     */
    public void addBrokerConfiguration(BrokerConfiguration brokerConfiguration,
                                       int tenantId) throws BMConfigurationException;

    /**
     * removes the broker configuration instance from the system.
     * @param name - broker configuration to be removed
     */
    public void removeBrokerConfiguration(String name,
                                         int tenantId) throws BMConfigurationException;

    /**
     * getting all the broker proxy instance deatils. this is used to dispaly all the
     * broker configuration instances.
     * @return - list of available broker configuration
     */
    public List<BrokerConfiguration> getAllBrokerConfigurations(int tenantId);

    /**
     * this method returns all the broker configuration names to be used by other componets
     * @return - all broker configurations
     */
    public List<String> getAllBrokerConfigurationNames(int tenantId);

    /**
     * retuns the broker configuration for the given name
     * @param name  - broker configuration name
     * @return - broker configuration
     */
    public BrokerConfiguration getBrokerConfiguration(String name ,int tenantId);
}
