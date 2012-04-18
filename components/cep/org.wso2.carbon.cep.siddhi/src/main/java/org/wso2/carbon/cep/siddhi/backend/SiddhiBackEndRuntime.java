/*
 * Copyright 2004,2012 The Apache Software Foundation.
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


package org.wso2.carbon.cep.siddhi.backend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.listener.CEPEventListener;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.property.Property;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.siddhi.api.eventstream.EventStream;
import org.wso2.siddhi.api.eventstream.InputEventStream;
import org.wso2.siddhi.api.exception.SiddhiPraserException;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.EventImpl;
import org.wso2.siddhi.core.eventstream.StreamReference;
import org.wso2.siddhi.core.exception.SiddhiException;
import org.wso2.siddhi.core.node.InputHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Siddhi based CEP back end runtime.
 */

public class SiddhiBackEndRuntime implements CEPBackEndRuntime {

    private static final Log log = LogFactory.getLog(SiddhiBackEndRuntime.class);
    private String bucketName;
    private SiddhiManager siddhiManager;
    private Map<String, InputHandler> siddhiInputHandlerMap;
    private Map<String, StreamReference> queryReferenceMap;
    private int tenantId;

    static ConcurrentHashMap<String, Class> javaTypeToClass;

    static {
        javaTypeToClass = new ConcurrentHashMap<String, Class>();
        javaTypeToClass.put(Integer.class.getName(), Integer.class);
        javaTypeToClass.put(String.class.getName(), String.class);
        javaTypeToClass.put(Double.class.getName(), Double.class);
        javaTypeToClass.put(Long.class.getName(), Long.class);
        javaTypeToClass.put(Float.class.getName(), Float.class);
    }

    public SiddhiBackEndRuntime(String bucketName, SiddhiManager siddhiManager,
                                Map<String, InputHandler> siddhiInputHandlerMap, int tenantId) {
        this.bucketName = bucketName;
        this.siddhiManager = siddhiManager;
        this.siddhiInputHandlerMap = siddhiInputHandlerMap;
        this.tenantId = tenantId;
        this.queryReferenceMap = new HashMap<String, StreamReference>();

    }

    /**
     * get the cep input handler and insert the event.
     *
     * @param event        - object representing the event data
     * @param inputMapping - Mapping to the topic which publish data
     *                     details of the input stream to add events.
     */
    public void insertEvent(Object event, InputMapping inputMapping) {

        String streamName = inputMapping.getStream();
        InputHandler inputHandler = siddhiInputHandlerMap.get(streamName);
        Event siddhiEvent = new EventImpl(streamName,
                                          ((org.wso2.carbon.agent.commons.Event) event).getPayloadData(),
                                          ((org.wso2.carbon.agent.commons.Event) event).getTimeStamp());
        inputHandler.sendEvent(siddhiEvent);
    }

    /**
     * adds the query to the siddhi backend. if the expression is inline it takes the query
     * from it. otherwise read from the registry.
     *
     * @param expression       - cep rule source
     * @param queryName        - Name of the Query To be added
     * @param cepEventListener - wso2 cep engine pass this object to receive the events
     *                         back from the cep engine.
     * @throws org.wso2.carbon.cep.core.exception.CEPConfigurationException
     *
     */
    public void addQuery(String queryName, Expression expression,
                         final CEPEventListener cepEventListener)
            throws CEPConfigurationException {
        String siddhiQuery = null;
        try {
            if (expression.getType().equals("inline")) {
                siddhiQuery = expression.getText();
            } else {
                siddhiQuery = readSourceTextFromRegistry(expression.getText().trim());
            }
            StreamReference streamReference = siddhiManager.addQuery(siddhiQuery);
            queryReferenceMap.put(queryName, streamReference);
            if (cepEventListener != null) {
                cepEventListener.defineStream(createStreamTypeDef(siddhiManager.getEventStream(streamReference.getStreamId())));
                siddhiManager.addCallback(new SiddhiEventListner(siddhiManager.getEventStream(streamReference.getStreamId()),
                                                                 cepEventListener));
            }
            siddhiManager.update();
        } catch (RegistryException e) {
            log.error("Error in reading query from registry");
            throw new CEPConfigurationException("Problem with reading query from registry " + e);
        } catch (SiddhiPraserException e) {
            throw new CEPConfigurationException("Query :" + siddhiQuery + " , is invalid", e);
        } catch (SiddhiException e) {
            throw new CEPConfigurationException("Exception when adding query :" + siddhiQuery, e);
        }


    }

    private EventStreamDefinition createStreamTypeDef(EventStream eventStream) {
        EventStreamDefinition typeDef = new EventStreamDefinition(eventStream.getStreamId());
        List<Attribute> attributeList = new ArrayList<Attribute>();
        String[] names = eventStream.getNames();
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            String name = names[i];
            Class attributeClass = eventStream.getNthAttributeType(i);
            if (attributeClass == String.class) {
                attributeList.add(new Attribute(name, AttributeType.STRING));
            } else if (attributeClass == Integer.class) {
                attributeList.add(new Attribute(name, AttributeType.INT));
            } else if (attributeClass == Long.class) {
                attributeList.add(new Attribute(name, AttributeType.LONG));
            } else if (attributeClass == Boolean.class) {
                attributeList.add(new Attribute(name, AttributeType.BOOL));
            } else if (attributeClass == Long.class) {
                attributeList.add(new Attribute(name, AttributeType.LONG));
            } else if (attributeClass == Float.class) {
                attributeList.add(new Attribute(name, AttributeType.FLOAT));
            } else if (attributeClass == Double.class) {
                attributeList.add(new Attribute(name, AttributeType.DOUBLE));
            }
        }
        typeDef.setPayloadData(attributeList);
        return typeDef;
    }

    public void removeQuery(String queryName) throws CEPConfigurationException {
        StreamReference streamReference = queryReferenceMap.remove(queryName);
        if (streamReference != null) {
            siddhiManager.removeStream(streamReference);
        }
    }

    public void removeAllQueries() throws CEPConfigurationException {
        for (StreamReference streamReference : queryReferenceMap.values()) {
            siddhiManager.removeStream(streamReference);
        }
    }

    @Override
    public void addInput(Input input) throws CEPConfigurationException {
        InputMapping mapping = input.getInputMapping();
        List properties;
        if (mapping instanceof TupleInputMapping) {
            TupleInputMapping tupleInputMapping = (TupleInputMapping) mapping;
            properties = tupleInputMapping.getProperties();
        } else { //Xml mapping
            XMLInputMapping xmlInputMapping = (XMLInputMapping) mapping;
            properties = xmlInputMapping.getProperties();
        }
        String[] attributeNames = new String[properties.size()];
        Class[] attributeTypes = new Class[properties.size()];

        for (int i = 0, propertiesSize = properties.size(); i < propertiesSize; i++) {
            Property property = (Property) properties.get(i);
            attributeNames[i] = property.getName();
            attributeTypes[i] = javaTypeToClass.get(property.getType());
        }
        try {
            siddhiInputHandlerMap.put(mapping.getStream(),
                                      siddhiManager.addInputEventStream(
                                              new InputEventStream(mapping.getStream(),
                                                                   attributeNames,
                                                                   attributeTypes)));
        } catch (SiddhiException e) {
            throw new CEPConfigurationException("Invalid input stream configuration for " +
                                                mapping.getStream(), e);
        }
        try {
            siddhiManager.update();
        } catch (SiddhiException e) {
            throw new CEPConfigurationException("Cannot add input topic " + input.getTopic() + " to  Siddhi Backend", e);
        }
    }

    @Override
    public void removeInput(Input input) throws CEPConfigurationException {
        String stream = input.getInputMapping().getStream();
        InputHandler inputHandler = siddhiInputHandlerMap.get(stream);
        if (inputHandler != null) {
            siddhiManager.removeStream(new StreamReference(inputHandler.getStreamId(), inputHandler.getNodeId()));
        }
        try {
            siddhiManager.update();
        } catch (SiddhiException e) {
            throw new CEPConfigurationException("Cannot remove input topic " + input.getTopic() + " from Siddhi Backend", e);
        }
    }

    private String readSourceTextFromRegistry(String key) throws RegistryException {
        Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
        Resource resource = registry.get(key);
        String content = new String((byte[]) resource.getContent());
        return content;

    }


}
