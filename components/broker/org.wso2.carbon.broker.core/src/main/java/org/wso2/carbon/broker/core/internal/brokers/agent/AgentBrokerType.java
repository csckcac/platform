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
import org.wso2.carbon.agent.commons.TypeDef;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.internal.BrokerType;
import org.wso2.carbon.broker.core.internal.ds.BrokerServiceValueHolder;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class AgentBrokerType implements BrokerType {

    private static final Log log = LogFactory.getLog(AgentBrokerType.class);
    private Gson gson= new Gson();
    private BrokerTypeDto brokerTypeDto = null;
    private static AgentBrokerType agentBrokerType = new AgentBrokerType();

    private Map<String, Map<BrokerConfiguration, BrokerListener>> brokerListenerMap =
            new ConcurrentHashMap<String, Map<BrokerConfiguration, BrokerListener>>();
    private Map<String, TypeDef> inputTypeDefMap = new ConcurrentHashMap<String, TypeDef>();
    private Map<String, TypeDef> outputTypeDefMap = new ConcurrentHashMap<String, TypeDef>();
    private Map<BrokerConfiguration, DataPublisher> dataPublisherMap =
            new ConcurrentHashMap<BrokerConfiguration, DataPublisher>();
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

        BrokerServiceValueHolder.getAgentServer().subscribe(assignAgentCallback());
    }

    public static AgentBrokerType getInstance() {
        return agentBrokerType;
    }

    private AgentCallback assignAgentCallback() {
        return new AgentCallback() {
            @Override
            public void definedType(TypeDef typeDef, String s) {
                Map<BrokerConfiguration, BrokerListener> brokerListeners = brokerListenerMap.get(typeDef.getStreamId());
                if (brokerListeners == null) {
                    brokerListeners = new HashMap<BrokerConfiguration, BrokerListener>();
                    brokerListenerMap.put(typeDef.getStreamId(), brokerListeners);
                }
                inputTypeDefMap.put(typeDef.getStreamId(), typeDef);
                for (BrokerListener brokerListener : brokerListeners.values()) {
                    try {
                        brokerListener.onEventDefinition(typeDef);
                    } catch (BrokerEventProcessingException e) {
                        log.error("Cannot send Stream Definition to a brokerListener subscribed to " +
                                  typeDef.getStreamId(), e);
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
        };
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
            if (agent == null) {
                agent = BrokerServiceValueHolder.getAgent();
            }
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
            } catch (Exception e) {
                throw new BrokerEventProcessingException(
                        "Cannot create DataPublisher for the broker configuration:" + brokerConfiguration.getName(), e);
            }


        }
        dataPublisherMap.put(brokerConfiguration, dataPublisher);

        //Building the Common Object model Event
        String streamId = ((OMElement) message).getLocalName();
        TypeDef typeDef = outputTypeDefMap.get(streamId);
        if (typeDef == null) {
            List<Attribute> attributes = new ArrayList<Attribute>();
            List<String> values = new ArrayList<String>();
            Iterator iterator = ((OMElement) message).getChildElements();
            while (iterator.hasNext()) {
                OMElement omElement = (OMElement) iterator.next();
                attributes.add(new Attribute(omElement.getLocalName(), AttributeType.STRING));
                values.add(omElement.getText());
            }

            typeDef = new TypeDef();
            typeDef.setStreamId(streamId);
            typeDef.setPayloadData(attributes);
            outputTypeDefMap.put(streamId, typeDef);
            try {
                dataPublisher.defineEventStreamDefinition(gson.toJson(typeDef));
            } catch (Exception ex) {
                throw new BrokerEventProcessingException(
                        "Cannot define type via DataPublisher for the broker configuration:" +
                        brokerConfiguration.getName() + " on the typeDef " + typeDef, ex);
            }

            //Sending the first Event
            Event event = new Event();
            event.setStreamId(streamId);
            event.setCorrelationData(values.toArray());
            publishEvent(brokerConfiguration, dataPublisher, event);

        } else {
            //Sending Events
            Object[] data = new Object[typeDef.getPayloadData().size()];
            List<Attribute> payloadAttributes = typeDef.getPayloadData();
            for (int i = 0; i < typeDef.getPayloadData().size(); i++) {
                data[i] = ((OMElement) message).getChildrenWithLocalName(payloadAttributes.get(i).getName()).next();
            }
            Event event = new Event();
            event.setStreamId(streamId);
            event.setPayloadData(data);
            publishEvent(brokerConfiguration, dataPublisher, event);
        }

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
