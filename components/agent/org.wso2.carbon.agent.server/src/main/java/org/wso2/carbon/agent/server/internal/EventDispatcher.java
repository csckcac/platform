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


import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.EventConverter;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.authentication.session.AgentSession;
import org.wso2.carbon.agent.server.internal.queue.EventQueue;
import org.wso2.carbon.agent.server.internal.utils.EventComposite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispactches events  and their definitions subscribers
 */
public class EventDispatcher {

//    private static final Log log = LogFactory.getLog(EventDispatcher.class);

    private List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    private StreamDefinitionStore streamDefinitionStore;
    private Map<String, EventStreamTypeHolder> eventStreamTypeCache = new HashMap<String, EventStreamTypeHolder>();
    private EventQueue eventQueue;

    public EventDispatcher(StreamDefinitionStore streamDefinitionStore) {
        this.eventQueue = new EventQueue(subscribers);
        this.streamDefinitionStore = streamDefinitionStore;
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    public String defineEventStream(String streamDefinition, AgentSession agentSession)
            throws
            MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {
        EventStreamDefinition eventStreamDefinition = EventConverter.convertFromJson(streamDefinition);

        EventStreamDefinition existingEventStreamDefinition;
        try {
            existingEventStreamDefinition = streamDefinitionStore.getStreamDefinition(agentSession.getDomainName(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
            if (!existingEventStreamDefinition.equals(eventStreamDefinition)) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + eventStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(agentSession.getDomainName(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion()));
            }
            eventStreamDefinition = existingEventStreamDefinition;
        } catch (StreamDefinitionNotFoundException e) {
            streamDefinitionStore.saveStreamDefinition(agentSession.getDomainName(), eventStreamDefinition);
            updateEventStreamTypeCache(agentSession.getDomainName(), eventStreamDefinition);
        }

        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedEventStream(eventStreamDefinition, agentSession.getUsername(), agentSession.getPassword(), agentSession.getDomainName());
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


    public void publish(Object eventBundle, AgentSession agentSession)
            throws UndefinedEventTypeException {
        try {
            eventQueue.publish(new EventComposite(eventBundle, getStreamDefinitionHolder(agentSession.getDomainName()), agentSession));
        } catch (StreamDefinitionNotFoundException e) {
            throw new UndefinedEventTypeException("No event stream definition exist " + e.getErrorMessage());
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
            throws StreamDefinitionNotFoundException {
        try {
            return streamDefinitionStore.getStreamId(domainName, streamName, streamVersion);
        } catch (StreamDefinitionNotFoundException e) {
            throw new StreamDefinitionNotFoundException("No event stream definition exist " + e.getErrorMessage());
        }

    }
}
