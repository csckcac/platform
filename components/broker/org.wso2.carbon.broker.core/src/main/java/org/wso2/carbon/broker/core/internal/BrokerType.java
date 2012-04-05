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

package org.wso2.carbon.broker.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

/**
 * This is a broker type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public interface BrokerType {

    /**
     * object which describes this type. it contains the name and available properties.
     * @return - type dto
     */
    public BrokerTypeDto getBrokerTypeDto();

    /**
     * subscribe to the connection specified in the broker configuration.
     * @param topicName  - topic name to subscribe
     * @param brokerListener - broker type will invoke this when it recieve events
     * @param brokerConfiguration  - broker configuration details
     * @throws BrokerEventProcessingException  - if can not subscribe to the broker
     */
    public void subscribe(String topicName,
                          BrokerListener brokerListener,
                          BrokerConfiguration brokerConfiguration,
                          AxisConfiguration axisConfiguration) throws BrokerEventProcessingException;

    /**
     * publish a message to a given connection with the broker configuration.
     * @param topicName - topic name to publish messages
     * @param object - message to send
     * @param brokerConfiguration - broker configuration to be used
     * @throws BrokerEventProcessingException - if the message can not publish
     */
    public void publish(String topicName,
                        Object object,
                        BrokerConfiguration brokerConfiguration) throws BrokerEventProcessingException;

    /**
     * this method unsubscribes the subscription from the broker.
     * @param topicName
     * @param brokerConfiguration
     * @throws BrokerEventProcessingException
     */
    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration)
                                      throws BrokerEventProcessingException;


}
