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
package org.wso2.carbon.broker.core.internal.brokers.jms;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.internal.BrokerType;

import javax.jms.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JMS implementation of BrokerType
 */
public abstract class JMSBrokerType implements BrokerType {

    private static final Log log = LogFactory.getLog(JMSBrokerType.class);
    private BrokerTypeDto brokerTypeDto = null;
    private Map<String, Map<String, SubscriptionDetails>> brokerSubscriptionsMap;


    public BrokerTypeDto getBrokerTypeDto() {
        return brokerTypeDto;
    }

    /**
     * Subscribe to given topic
     *
     * @param topicName           - topic name to subscribe
     * @param brokerListener      - broker type will invoke this when it receive events
     * @param brokerConfiguration - broker configuration details
     * @throws BrokerEventProcessingException
     */
    public void subscribe(String topicName, BrokerListener brokerListener,
                          BrokerConfiguration brokerConfiguration,
                          AxisConfiguration axisConfiguration)
            throws BrokerEventProcessingException {
        // create connection
        TopicConnection topicConnection = getTopicConnection(brokerConfiguration);
        // create session, subscriber, message listener and listen on that topic
        try {
            TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            TopicSubscriber subscriber = session.createSubscriber(topic);
            MessageListener messageListener = new JMSMessageListener(brokerListener);
            subscriber.setMessageListener(messageListener);
            topicConnection.start();

            Map<String, SubscriptionDetails> topicSubscriptionsMap =
                    this.brokerSubscriptionsMap.get(brokerConfiguration.getName());
            if (topicSubscriptionsMap == null) {
                topicSubscriptionsMap = new ConcurrentHashMap<String, SubscriptionDetails>();
                this.brokerSubscriptionsMap.put(brokerConfiguration.getName(), topicSubscriptionsMap);
            }

            SubscriptionDetails subscriptionDetails =
                    new SubscriptionDetails(topicConnection, session, subscriber);
            topicSubscriptionsMap.put(topicName, subscriptionDetails);

        } catch (JMSException e) {
            String error = "Failed to subscribe to topic:" + topicName;
            log.error(error, e);
            throw new BrokerEventProcessingException(error, e);
        }
    }

    /**
     * Create Connection factory with initial context
     *
     * @param brokerConfiguration broker - configuration details to create a broker
     * @return Topic connection
     * @throws BrokerEventProcessingException - jndi look up failed
     */
    protected abstract TopicConnection getTopicConnection(BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException ;


    /**
     * Publish message to given topic
     *
     * @param topicName           - topic name to publish messages
     * @param message             - message to send
     * @param brokerConfiguration - broker configuration to be used
     * @throws BrokerEventProcessingException
     */
    public void publish(String topicName, Object message,
                        BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {

        // create topic connection
        TopicConnection topicConnection = getTopicConnection(brokerConfiguration);
        // create session, producer, message and send message to given destination(topic)
        // OMElement message text is published here.
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            Message jmsMessage = null;
            if (message instanceof OMElement){
                jmsMessage = session.createTextMessage(message.toString());
            } else if (message instanceof Map){
                MapMessage mapMessage = session.createMapMessage();
                Map sourceMessage = (Map) message;
                for (Object key : sourceMessage.keySet()){
                    mapMessage.setObject((String)key, sourceMessage.get(key));
                }
                jmsMessage = mapMessage;
            }
            producer.send(jmsMessage);
        } catch (JMSException e) {
            String error = "Failed to publish to topic:" + topicName;
            log.error(error, e);
            throw new BrokerEventProcessingException(error, e);

        } finally {
            // close used resources.
            try {
                if (session != null) {
                    session.close();
                }
                if (topicConnection != null) {
                    topicConnection.close();
                }
            } catch (JMSException e) {
                log.warn("Failed to reallocate resources.", e);
            }
        }
    }

    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration) throws BrokerEventProcessingException {
        Map<String, SubscriptionDetails> topicSubscriptionsMap =
                this.brokerSubscriptionsMap.get(brokerConfiguration.getName());
        if (topicSubscriptionsMap == null) {
            throw new BrokerEventProcessingException("There is no subscription for broker "
                    + brokerConfiguration.getName());
        }

        SubscriptionDetails subscriptionDetails = topicSubscriptionsMap.remove(topicName);
        if (subscriptionDetails == null) {
            throw new BrokerEventProcessingException("There is no subscriptions for this topic" + topicName);
        }

        try {
            subscriptionDetails.close();
        } catch (JMSException e) {
            throw new BrokerEventProcessingException("Can not unsubscribe from the broker with" +
                    "configuration " + brokerConfiguration.getName(),e);
        }

    }

    protected Map<String, Map<String, SubscriptionDetails>> getBrokerSubscriptionsMap() {
        return brokerSubscriptionsMap;
    }

    protected void setBrokerSubscriptionsMap(
            Map<String, Map<String, SubscriptionDetails>> brokerSubscriptionsMap) {
        this.brokerSubscriptionsMap = brokerSubscriptionsMap;
    }

    protected void setBrokerTypeDto(BrokerTypeDto brokerTypeDto) {
        this.brokerTypeDto = brokerTypeDto;
    }
}
