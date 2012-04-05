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
package org.wso2.carbon.broker.test;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.test.util.BrokerHolder;

/**
 * This is a tester service for testing broker services
 * Used to subscribe and publish to selected broker
 */
public class BrokerServiceTester {
    /**
     * Subscribe to given topic
     *
     * @param brokerName - unique broker name
     * @param topicName  - topic to subscribe
     * @throws BrokerEventProcessingException - if broker name does not exists
     */
    public void subscribe(BrokerConfiguration brokerConfiguration, String topicName)
            throws BrokerEventProcessingException {
        BrokerHolder.getInstance().getBrokerService().
                subscribe(brokerConfiguration, topicName, new TestBrokerListener(brokerConfiguration.getName(), topicName), null);
    }

    /**
     * Publish to given topic with message
     *
     * @param brokerName - unique broker name
     * @param topicName  - topic to publish
     * @param message    - message to publish
     * @throws BrokerEventProcessingException - if broker name does not exists
     */
    public void publish(BrokerConfiguration brokerConfiguration, String topicName, OMElement message)
            throws BrokerEventProcessingException {
        BrokerHolder.getInstance().getBrokerService().publish(brokerConfiguration, topicName, message.getFirstElement());
    }
}
