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

package org.wso2.carbon.eventbridge.core.internal;


import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.AgentCallback;
import org.wso2.carbon.eventbridge.core.EventConverter;
import org.wso2.carbon.eventbridge.core.EventStreamTypeHolder;
import org.wso2.carbon.eventbridge.core.conf.EventBridgeConfiguration;
import org.wso2.carbon.eventbridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.core.internal.authentication.session.AgentSession;
import org.wso2.carbon.eventbridge.core.internal.queue.EventQueue;
import org.wso2.carbon.eventbridge.core.internal.utils.EventComposite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispactches events  and their definitions subscribers
 */
public class EventDispatcher {

    public static final String HACK_DOMAIN_CONSTANT = "-1234";
    private List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    private AbstractStreamDefinitionStore streamDefinitionStore;
    private Map<String, EventStreamTypeHolder> eventStreamTypeCache = new ConcurrentHashMap<String, EventStreamTypeHolder>();
    private EventQueue eventQueue;

    public EventDispatcher(AbstractStreamDefinitionStore streamDefinitionStore,
                           EventBridgeConfiguration eventBridgeConfiguration) {
        this.eventQueue = new EventQueue(subscribers, eventBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    public String defineEventStream(String streamDefinition, AgentSession agentSession)
            throws
            MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        EventStreamDefinition eventStreamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinition);

        EventStreamDefinition existingEventStreamDefinition;
        try {
            existingEventStreamDefinition = streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
            if (!existingEventStreamDefinition.equals(eventStreamDefinition)) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + eventStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), eventStreamDefinition.getName(), eventStreamDefinition.getVersion()));
            }
            eventStreamDefinition = existingEventStreamDefinition;
        } catch (StreamDefinitionNotFoundException e) {
            streamDefinitionStore.saveStreamDefinition(agentSession.getCredentials(), eventStreamDefinition);
            updateEventStreamTypeCache(agentSession.getDomainName(), eventStreamDefinition);
        }

        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedEventStream(eventStreamDefinition, agentSession.getCredentials());
        }
        return eventStreamDefinition.getStreamId();
    }

    private void updateEventStreamTypeCache(String domainName,
                                            EventStreamDefinition eventStreamDefinition) {
        EventStreamTypeHolder eventStreamTypeHolder;
        // this will occur only outside of carbon (ex: Siddhi)
//        if (domainName == null) {
//            domainName = HACK_DOMAIN_CONSTANT;
//        }

//        if (eventStreamTypeCache.containsKey(domainName)) {
//            eventStreamTypeHolder = eventStreamTypeCache.get(domainName);
//        } else {
//            eventStreamTypeHolder = new EventStreamTypeHolder(domainName);
//            eventStreamTypeCache.put(domainName, eventStreamTypeHolder);
//        }
        eventStreamTypeHolder = new EventStreamTypeHolder(domainName);
        updateEventStreamTypeHolder(eventStreamTypeHolder, eventStreamDefinition);
        eventStreamTypeCache.put(domainName, eventStreamTypeHolder);
    }


    public void publish(Object eventBundle, AgentSession agentSession,
                        EventConverter eventConverter)
            throws UndefinedEventTypeException {
        try {
            eventQueue.publish(new EventComposite(eventBundle, getStreamDefinitionHolder(agentSession.getCredentials()), agentSession,eventConverter));
        } catch (StreamDefinitionNotFoundException e) {
            throw new UndefinedEventTypeException("No event stream definition exist " + e.getErrorMessage());
        }
    }

    private EventStreamTypeHolder getStreamDefinitionHolder(Credentials credentials)
            throws StreamDefinitionNotFoundException {
        // this will occur only outside of carbon (ex: Siddhi)

//        String domainName = (credentials.getDomainName() == null) ? HACK_DOMAIN_CONSTANT : credentials.getDomainName();

        EventStreamTypeHolder eventStreamTypeHolder = eventStreamTypeCache.get(credentials.getDomainName());
        if (eventStreamTypeHolder != null) {
            return eventStreamTypeHolder;
        } else {
            eventStreamTypeHolder = new EventStreamTypeHolder(credentials.getDomainName());
            for (EventStreamDefinition eventStreamDefinition : streamDefinitionStore.getAllStreamDefinitions(credentials)) {
                updateEventStreamTypeHolder(eventStreamTypeHolder, eventStreamDefinition);
                updateEventStreamTypeCache(credentials.getDomainName(), eventStreamDefinition);
            }
        }
        return eventStreamTypeHolder;
    }

    private void updateEventStreamTypeHolder(EventStreamTypeHolder eventStreamTypeHolder,
                                             EventStreamDefinition eventStreamDefinition) {
        eventStreamTypeHolder.setMetaDataType(eventStreamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(eventStreamDefinition.getMetaData()));
        eventStreamTypeHolder.setCorrelationDataType(eventStreamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(eventStreamDefinition.getCorrelationData()));
        eventStreamTypeHolder.setPayloadDataType(eventStreamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(eventStreamDefinition.getPayloadData()));
    }

    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }

    public String findEventStreamId(Credentials credentials, String streamName,
                                    String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        try {
            return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
        } catch (StreamDefinitionNotFoundException e) {
            throw new StreamDefinitionNotFoundException("No event stream definition exist " + e.getErrorMessage());
        }

    }

    private static class EventStreamTypeCache {



    }
}
