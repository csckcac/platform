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

package org.wso2.carbon.broker.core;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

import java.util.List;

/**
 * OSGI interface for the Broker Service
 */
public interface BrokerService {

    /**
     * this method returns all the available broker types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * broker objects.
     *
     * @return list of available types
     */
    public List<BrokerTypeDto> getBrokerTypes();

    /**
     * get all the names of the broker types
     * @return - list of broker names
     */
    public List<String> getBrokerTypeNames();

    /**
     * Get broker property list for getting details(isSecured, isRequired) of each properties
     * @param brokerType - type of the broker
     * @return Property list
     */
    public List<Property> getBrokerProperties(String brokerType);

    /**
     * subscribe to a pirticulare broker configuration. When the Broker receives the
     * message it send that to the user through the listener interface.
     *
     * @param brokerConfiguration     - Configuration details of the broker
     * @param topicName      - topic to subscribe
     * @param brokerListener - listener interface to notify
     * @throws org.wso2.carbon.broker.core.exception.BrokerEventProcessingException
     *          - if problem happen when subscribing
     */
    public void subscribe(BrokerConfiguration brokerConfiguration ,
                          String topicName,
                          BrokerListener brokerListener,
                          AxisConfiguration axisConfiguration) throws BrokerEventProcessingException;


    /**
     * publishes the message using the givne broker proxy to the given topic.
     *
     * @param brokerConfiguration - Configuration Details of the broker
     * @param topicName  - topic to publish
     * @param object    - message to send
     * @throws org.wso2.carbon.broker.core.exception.BrokerEventProcessingException
     *          - if problem happen when publishing
     */
    public void publish(BrokerConfiguration brokerConfiguration,
                        String topicName,
                        Object object) throws BrokerEventProcessingException;

    /**
     * un subscribes from the broker.
     * @param topicName - topic name to which previously subscribed
     * @param brokerConfiguration  - broker configuration to be used
     * @param axisConfiguration - acis configuration
     * @throws BrokerEventProcessingException
     */
    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration)
                                      throws BrokerEventProcessingException;

}
