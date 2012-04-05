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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

/**
 * Topic subscription call back handler implementation
 */
public class TestBrokerListener implements BrokerListener {
    private static final Log log = LogFactory.getLog(TestBrokerListener.class);
    private String brokerName;
    private String topic;

    public TestBrokerListener(String brokerName, String topic) {
        this.brokerName = brokerName;
        this.topic = topic;
    }

    @Override
    public void onEventDefinition(Object object) throws BrokerEventProcessingException {
        System.out.println("Definition ==> "+ object);
    }

    /**
     * Received message is logged to ensure that published messages are received.
     * @param object  - received event
     * @throws BrokerEventProcessingException
     */
    public void onEvent(Object object) throws BrokerEventProcessingException {
        log.info("brokerName ==> " + brokerName);
        log.info("topic ==> " + topic);
        log.info("omElement message ==> " + object);
    }
}
