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

package org.wso2.carbon.databridge.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.session.AgentSession;
import org.wso2.carbon.databridge.core.internal.queue.EventQueue;
import org.wso2.carbon.databridge.core.internal.utils.EventComposite;

import java.util.ArrayList;
import java.util.Collection;
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
    private Map<String, StreamTypeHolder> streamTypeCache = new ConcurrentHashMap<String, StreamTypeHolder>();
    private EventQueue eventQueue;

    private static final Log log = LogFactory.getLog(EventDispatcher.class);


    public EventDispatcher(AbstractStreamDefinitionStore streamDefinitionStore,
                           DataBridgeConfiguration dataBridgeConfiguration) {
        this.eventQueue = new EventQueue(subscribers, dataBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    public String defineStream(String streamDefinition, AgentSession agentSession)
            throws
            MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        synchronized (EventDispatcher.class) {
            StreamDefinition newStreamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinition);

            StreamDefinition existingStreamDefinition;
            try {
                existingStreamDefinition = streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), newStreamDefinition.getName(), newStreamDefinition.getVersion());
                if (!existingStreamDefinition.equals(newStreamDefinition)) {
                    throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + newStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), newStreamDefinition.getName(), newStreamDefinition.getVersion()));
                }
                newStreamDefinition = existingStreamDefinition;
            } catch (StreamDefinitionNotFoundException e) {
                streamDefinitionStore.saveStreamDefinition(agentSession.getCredentials(), newStreamDefinition);
                updateStreamTypeCache(agentSession.getDomainName(), newStreamDefinition);
            }

            for (AgentCallback agentCallback : subscribers) {
                agentCallback.definedStream(newStreamDefinition, agentSession.getCredentials());
            }
            return newStreamDefinition.getStreamId();
        }
    }

    private void updateStreamTypeCache(String domainName,
                                            StreamDefinition streamDefinition) {
        StreamTypeHolder streamTypeHolder;
        // this will occur only outside of carbon (ex: Siddhi)
//        if (domainName == null) {
//            domainName = HACK_DOMAIN_CONSTANT;
//        }

        synchronized (EventDispatcher.class) {
            if (streamTypeCache.containsKey(domainName)) {
                streamTypeHolder = streamTypeCache.get(domainName);
            } else {
                streamTypeHolder = new StreamTypeHolder(domainName);
            }

            updateStreamTypeHolder(streamTypeHolder, streamDefinition);
            if (log.isTraceEnabled()) {
                String logMsg = "Event Stream Type getting updated : ";
                logMsg += "Event stream holder for domain name : " + domainName + " : \n ";
                logMsg += "Correlation Data Type Map : " + streamTypeHolder.getCorrelationDataTypeMap() + "\n";
                logMsg += "Payload Data Type Map : " + streamTypeHolder.getPayloadDataTypeMap() + "\n";
                logMsg += "Meta Data Type Map : " + streamTypeHolder.getMetaDataTypeMap() + "\n";
                log.trace(logMsg);
            }


            streamTypeCache.put(domainName, streamTypeHolder);
        }
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

    private StreamTypeHolder getStreamDefinitionHolder(Credentials credentials)
            throws StreamDefinitionNotFoundException {
        // this will occur only outside of carbon (ex: Siddhi)

//        String domainName = (credentials.getDomainName() == null) ? HACK_DOMAIN_CONSTANT : credentials.getDomainName();

        StreamTypeHolder streamTypeHolder = streamTypeCache.get(credentials.getDomainName());

        if (log.isTraceEnabled()) {
            log.trace("Retrieving Event Stream Type Cache : " + streamTypeCache);
        }

        if (streamTypeHolder != null) {
            if (log.isTraceEnabled()) {
                String logMsg = "Event stream holder for domain name : " + credentials.getDomainName() + " : \n ";
                logMsg += "Correlation Data Type Map : " + streamTypeHolder.getCorrelationDataTypeMap() + "\n";
                logMsg += "Payload Data Type Map : " + streamTypeHolder.getPayloadDataTypeMap() + "\n";
                logMsg += "Meta Data Type Map : " + streamTypeHolder.getMetaDataTypeMap() + "\n";
                log.trace(logMsg);
            }
            return streamTypeHolder;
        } else {
            synchronized (EventDispatcher.class) {
                streamTypeHolder = new StreamTypeHolder(credentials.getDomainName());
                Collection<StreamDefinition> allStreamDefinitions =
                        streamDefinitionStore.getAllStreamDefinitions(credentials);
                for (StreamDefinition streamDefinition : allStreamDefinitions) {
                    updateStreamTypeHolder(streamTypeHolder, streamDefinition);
                    updateStreamTypeCache(credentials.getDomainName(), streamDefinition);
                }
            }
        }

        if (log.isTraceEnabled()) {
        String logMsg = "Event stream holder for domain name : " + credentials.getDomainName() + " : \n ";
            logMsg += "Correlation Data Type Map : " + streamTypeHolder.getCorrelationDataTypeMap() + "\n";
            logMsg += "Payload Data Type Map : " + streamTypeHolder.getPayloadDataTypeMap() + "\n";
            logMsg += "Meta Data Type Map : " + streamTypeHolder.getMetaDataTypeMap() + "\n";
            log.trace(logMsg);
        }
        return streamTypeHolder;
    }

    private void updateStreamTypeHolder(StreamTypeHolder streamTypeHolder,
                                             StreamDefinition streamDefinition) {
        streamTypeHolder.setMetaDataType(streamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(streamDefinition.getMetaData()));
        streamTypeHolder.setCorrelationDataType(streamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(streamDefinition.getCorrelationData()));
        streamTypeHolder.setPayloadDataType(streamDefinition.getStreamId(), EventDefinitionConverterUtils
                .generateAttributeTypeArray(streamDefinition.getPayloadData()));
    }

    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }

    public String findStreamId(Credentials credentials, String streamName,
                                    String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        try {
            return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
        } catch (StreamDefinitionNotFoundException e) {
            throw new StreamDefinitionNotFoundException("No event stream definition exist " + e.getErrorMessage());
        }

    }

    private static class StreamTypeCache {



    }
}
