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

package org.wso2.carbon.broker.core.internal.brokers.agent;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.internal.BrokerType;
import org.wso2.carbon.broker.core.internal.ds.BrokerServiceValueHolder;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentBrokerType implements BrokerType {

    private static final Log log = LogFactory.getLog(AgentBrokerType.class);
    private Gson gson = new Gson();
    private BrokerTypeDto brokerTypeDto = null;
    private static AgentBrokerType agentBrokerType = new AgentBrokerType();

    private Map<String, Map<BrokerConfiguration, BrokerListener>> brokerListenerMap =
            new ConcurrentHashMap<String, Map<BrokerConfiguration, BrokerListener>>();
    private Map<String, EventStreamDefinition> inputTypeDefMap = new ConcurrentHashMap<String, EventStreamDefinition>();
    private Map<String, EventStreamDefinition> outputTypeDefMap = new ConcurrentHashMap<String, EventStreamDefinition>();
    private Map<BrokerConfiguration, DataPublisher> dataPublisherMap = new ConcurrentHashMap<BrokerConfiguration, DataPublisher>();
    private Agent agent;

    private AgentBrokerType() {
        this.brokerTypeDto = new BrokerTypeDto();
        this.brokerTypeDto.setName(BrokerConstants.BROKER_TYPE_AGENT);

        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.broker.core.i18n.Resources", Locale.getDefault());

        // set receiver url broker
        Property ipProperty = new Property(BrokerConstants.BROKER_CONF_AGENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_AGENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        this.brokerTypeDto.addProperty(ipProperty);

        // set authenticator url of broker
        Property authenticatorIpProperty = new Property(BrokerConstants.
                                                                BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        this.brokerTypeDto.addProperty(authenticatorIpProperty);

        // set connection user name as property
        Property userNameProperty = new Property(BrokerConstants.BROKER_CONF_AGENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_AGENT_PROP_USER_NAME));
        this.brokerTypeDto.addProperty(userNameProperty);

        // set connection password as property
        Property passwordProperty = new Property(BrokerConstants.BROKER_CONF_AGENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_AGENT_PROP_PASSWORD));
        this.brokerTypeDto.addProperty(passwordProperty);

        BrokerServiceValueHolder.getAgentServer().subscribe(new AgentBrokerCallback());
    }

    public static AgentBrokerType getInstance() {
        return agentBrokerType;
    }

    private class AgentBrokerCallback implements AgentCallback {

        @Override
        public void definedEventStream(EventStreamDefinition eventStreamDefinition, String s) {
            Map<BrokerConfiguration, BrokerListener> brokerListeners = brokerListenerMap.get(eventStreamDefinition.getStreamId());
            if (brokerListeners == null) {
                brokerListeners = new HashMap<BrokerConfiguration, BrokerListener>();
                brokerListenerMap.put(eventStreamDefinition.getStreamId(), brokerListeners);
            }
            inputTypeDefMap.put(eventStreamDefinition.getStreamId(), eventStreamDefinition);
            for (BrokerListener brokerListener : brokerListeners.values()) {
                try {
                    brokerListener.onEventDefinition(eventStreamDefinition);
                } catch (BrokerEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a brokerListener subscribed to " +
                              eventStreamDefinition.getStreamId(), e);
                }

            }
        }

        @Override
        public void receive(List<Event> events, String s) {
            //Here all events are of same stream
            Map<BrokerConfiguration, BrokerListener> brokerListeners = brokerListenerMap.get(events.get(0).getStreamId());
            for (Event event : events) {
                for (BrokerListener brokerListener : brokerListeners.values()) {
                    try {
                        brokerListener.onEvent(event);
                    } catch (BrokerEventProcessingException e) {
                        log.error("Cannot send event to a brokerListener subscribed to " +
                                  events.get(0).getStreamId(), e);
                    }

                }
            }
        }

    }

    public void subscribe(String topicName, BrokerListener brokerListener,
                          BrokerConfiguration brokerConfiguration,
                          AxisConfiguration axisConfiguration)
            throws BrokerEventProcessingException {
        if (!brokerListenerMap.containsKey(topicName)) {
            Map<BrokerConfiguration, BrokerListener> map = new HashMap<BrokerConfiguration, BrokerListener>();
            map.put(brokerConfiguration, brokerListener);
            brokerListenerMap.put(topicName, map);
        } else {
            brokerListenerMap.get(topicName).put(brokerConfiguration, brokerListener);
        }
    }

    public void publish(String topicName, Object message,
                        BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {

        DataPublisher dataPublisher = dataPublisherMap.get(brokerConfiguration);
        if (dataPublisher == null) {
            dataPublisher = createDataPublihser(brokerConfiguration);
        }

        //Building the Common Object model Event
        String streamId = ((OMElement) message).getLocalName();
        EventStreamDefinition eventStreamDefinition = outputTypeDefMap.get(streamId);
        if (eventStreamDefinition == null) {
            List<Attribute> attributes = new ArrayList<Attribute>();
            List<String> values = new ArrayList<String>();

            buildPayLoadDataAndAttributes((OMElement) message, attributes, values);

            eventStreamDefinition = new EventStreamDefinition(streamId);
            eventStreamDefinition.setPayloadData(attributes);
            outputTypeDefMap.put(streamId, eventStreamDefinition);

            String eventStreamDefinitionString = gson.toJson(eventStreamDefinition);
            try {
                dataPublisher.defineEventStream(eventStreamDefinitionString);
            } catch (Exception ex) {
                throw new BrokerEventProcessingException(
                        "Cannot define type via DataPublisher for the broker configuration:" +
                        brokerConfiguration.getName() + " on the eventStreamDefinition " + eventStreamDefinition, ex);
            }

            //Sending the first Event
            sendEvent(brokerConfiguration, dataPublisher, streamId, values.toArray());

        } else {
            //Sending Events
            Object[] data = buildPayloadData((OMElement) message, eventStreamDefinition);
            sendEvent(brokerConfiguration, dataPublisher, streamId, data);
        }

    }

    private void buildPayLoadDataAndAttributes(OMElement message, List<Attribute> attributes,
                                               List<String> values) {
        Iterator iterator = message.getChildElements();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            attributes.add(new Attribute(omElement.getLocalName(), AttributeType.STRING));
            values.add(omElement.getText());
        }
    }

    private Object[] buildPayloadData(OMElement message,
                                      EventStreamDefinition eventStreamDefinition) {
        Object[] data = new Object[eventStreamDefinition.getPayloadData().size()];
        List<Attribute> payloadAttributes = eventStreamDefinition.getPayloadData();
        for (int i = 0; i < eventStreamDefinition.getPayloadData().size(); i++) {
            data[i] = message.getChildrenWithLocalName(payloadAttributes.get(i).getName()).next();
        }
        return data;
    }

    private void sendEvent(BrokerConfiguration brokerConfiguration, DataPublisher dataPublisher,
                           String streamId, Object[] values)
            throws BrokerEventProcessingException {
        Event event = new Event();
        event.setStreamId(streamId);
        event.setCorrelationData(values);
        publishEvent(brokerConfiguration, dataPublisher, event);
    }

    private DataPublisher createDataPublihser(BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {
        if (agent == null) {
            agent = BrokerServiceValueHolder.getAgent();
        }
        DataPublisher dataPublisher = null;
        Map<String, String> properties = brokerConfiguration.getProperties();
        try {
            if (null != properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL) && properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL).length() > 0) {
                dataPublisher = new DataPublisher(properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL),
                                                  properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_RECEIVER_URL),
                                                  properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_USER_NAME),
                                                  properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_PASSWORD),
                                                  agent);
            } else {
                dataPublisher = new DataPublisher(properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_RECEIVER_URL),
                                                  properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_USER_NAME),
                                                  properties.get(BrokerConstants.BROKER_CONF_AGENT_PROP_PASSWORD),
                                                  agent);
            }
        } catch (AuthenticationException e) {
            throwBrokerEventProcessingException(brokerConfiguration, e);
        } catch (AgentException e) {
            throwBrokerEventProcessingException(brokerConfiguration, e);
        } catch (TransportException e) {
            throwBrokerEventProcessingException(brokerConfiguration, e);
        } catch (MalformedURLException e) {
            throwBrokerEventProcessingException(brokerConfiguration, e);
        }
        dataPublisherMap.put(brokerConfiguration, dataPublisher);
        return dataPublisher;
    }

    private void throwBrokerEventProcessingException(BrokerConfiguration brokerConfiguration,
                                                     Exception e)
            throws BrokerEventProcessingException {
        throw new BrokerEventProcessingException(
                "Cannot create DataPublisher for the broker configuration:" + brokerConfiguration.getName(), e);
    }

    private void publishEvent(BrokerConfiguration brokerConfiguration, DataPublisher dataPublisher,
                              Event event) throws BrokerEventProcessingException {
        try {
            dataPublisher.publish(event);
        } catch (Exception ex) {
            throw new BrokerEventProcessingException(
                    "Cannot publish data via DataPublisher for the broker configuration:" +
                    brokerConfiguration.getName() + " for the  event " + event, ex);
        }
    }

    public BrokerTypeDto getBrokerTypeDto() {
        return brokerTypeDto;
    }

    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration)
            throws BrokerEventProcessingException {
        Map<BrokerConfiguration, BrokerListener> map = brokerListenerMap.get(topicName);
        if (map != null) {
            map.remove(brokerConfiguration);
        }

    }
}
