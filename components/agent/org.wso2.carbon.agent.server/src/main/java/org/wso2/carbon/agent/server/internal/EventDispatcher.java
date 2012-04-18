/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.agent.server.internal;


import com.google.gson.Gson;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.authentication.session.AgentSession;
import org.wso2.carbon.agent.server.internal.queue.EventQueue;
import org.wso2.carbon.agent.server.internal.utils.EventComposite;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDispatcher {


//    private static final Log log = LogFactory.getLog(EventDispatcher.class);

    private List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    private StreamDefinitionStore streamDefinitionStore;
    private Map<String, EventStreamTypeHolder> eventStreamTypeCache = new HashMap<String, EventStreamTypeHolder>();
    private EventQueue eventQueue;
    private Gson gson = new Gson();

    public EventDispatcher(StreamDefinitionStore streamDefinitionStore) {
        this.eventQueue = new EventQueue(subscribers);
        this.streamDefinitionStore = streamDefinitionStore;
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    public String defineEventStream(String streamDefinition, AgentSession agentSession)
            throws ThriftDifferentStreamDefinitionAlreadyDefinedException,
                   ThriftMalformedStreamDefinitionException {
        EventStreamDefinition eventStreamDefinition = convertFromJson(streamDefinition);

        EventStreamDefinition existingEventStreamDefinition = null;
        try {
            existingEventStreamDefinition = streamDefinitionStore.getStreamDefinition(agentSession.getDomainName(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
            if (!existingEventStreamDefinition.equals(eventStreamDefinition)) {
                throw new ThriftDifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + eventStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(agentSession.getDomainName(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion()));
            }
            eventStreamDefinition = existingEventStreamDefinition;
        } catch (StreamDefinitionNotFoundException e) {
            streamDefinitionStore.saveStreamDefinition(agentSession.getDomainName(), eventStreamDefinition);
            updateEventStreamTypeCache(agentSession.getDomainName(), eventStreamDefinition);
        }

        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedEventStream(eventStreamDefinition, agentSession.getSessionId());
        }
        return eventStreamDefinition.getStreamId();
    }

    private void updateEventStreamTypeCache(String domainName,
                                            EventStreamDefinition eventStreamDefinition) {
        EventStreamTypeHolder eventStreamTypeHolder;
        if (eventStreamTypeCache.containsKey(domainName)) {
            eventStreamTypeHolder = eventStreamTypeCache.get(domainName);
        } else {
            eventStreamTypeHolder = new EventStreamTypeHolder(domainName);
            eventStreamTypeCache.put(domainName, eventStreamTypeHolder);
        }
        updateEventStreamTypeHolder(eventStreamTypeHolder, eventStreamDefinition);
    }

    public EventStreamDefinition convertFromJson(String streamDefinition)
            throws ThriftMalformedStreamDefinitionException {
        EventStreamDefinition eventStreamDefinition = gson.fromJson(streamDefinition.
                replaceAll("(?i)int", "INT").replaceAll("(?i)long", "LONG").
                replaceAll("(?i)float", "FLOAT").replaceAll("(?i)double", "DOUBLE").
                replaceAll("(?i)bool", "BOOL").replaceAll("(?i)string", "STRING"), EventStreamDefinition.class);
        eventStreamDefinition.generateSteamId();
        if (eventStreamDefinition.getName() == null) {
            throw new ThriftMalformedStreamDefinitionException("Stream Name is null");
        }
        String versionPattern = "^\\d+\\.\\d+\\.\\d+$";
        if (!eventStreamDefinition.getVersion().matches(versionPattern)) {
            throw new ThriftMalformedStreamDefinitionException("version " + eventStreamDefinition.getVersion() + " does not adhere to the format x.x.x ");
        }
        return eventStreamDefinition;
    }

    public void publish(ThriftEventBundle thriftEventBundle, AgentSession agentSession)
            throws ThriftUndefinedEventTypeException {
        try {
            eventQueue.publish(new EventComposite(thriftEventBundle, getStreamDefinitionHolder(agentSession.getDomainName())));
        } catch (StreamDefinitionNotFoundException e) {
            throw new ThriftUndefinedEventTypeException("No event stream definition exist " + e.toString());
        }
    }

    private EventStreamTypeHolder getStreamDefinitionHolder(String domainName)
            throws StreamDefinitionNotFoundException {
        EventStreamTypeHolder eventStreamTypeHolder = eventStreamTypeCache.get(domainName);
        if (eventStreamTypeHolder != null) {
            return eventStreamTypeHolder;
        } else {
            eventStreamTypeHolder = new EventStreamTypeHolder(domainName);
            for (EventStreamDefinition eventStreamDefinition : streamDefinitionStore.getAllStreamDefinitions(domainName)) {
                updateEventStreamTypeHolder(eventStreamTypeHolder, eventStreamDefinition);
            }
        }
        return eventStreamTypeHolder;
    }

    private void updateEventStreamTypeHolder(EventStreamTypeHolder eventStreamTypeHolder,
                                             EventStreamDefinition eventStreamDefinition) {
        eventStreamTypeHolder.setMetaDataType(eventStreamDefinition.getStreamId(), EventConverter.generateAttributeTypeArray(eventStreamDefinition.getMetaData()));
        eventStreamTypeHolder.setCorrelationDataType(eventStreamDefinition.getStreamId(), EventConverter.generateAttributeTypeArray(eventStreamDefinition.getCorrelationData()));
        eventStreamTypeHolder.setPayloadDataType(eventStreamDefinition.getStreamId(), EventConverter.generateAttributeTypeArray(eventStreamDefinition.getPayloadData()));
    }

    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }

    public String findEventStreamId(String domainName, String streamName, String streamVersion)
            throws ThriftNoStreamDefinitionExistException {
        try {
            return streamDefinitionStore.getStreamId(domainName, streamName, streamVersion);
        } catch (StreamDefinitionNotFoundException e) {
            throw new ThriftNoStreamDefinitionExistException("No event stream definition exist " + e.toString());
        }

    }
}
