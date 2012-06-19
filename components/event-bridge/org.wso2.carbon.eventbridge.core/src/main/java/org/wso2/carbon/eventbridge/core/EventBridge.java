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

package org.wso2.carbon.eventbridge.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.conf.EventBridgeConfiguration;
import org.wso2.carbon.eventbridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.definitionstore.StreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.core.internal.EventDispatcher;
import org.wso2.carbon.eventbridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.eventbridge.core.internal.authentication.Authenticator;
import org.wso2.carbon.eventbridge.core.internal.authentication.session.AgentSession;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public class EventBridge implements EventBridgeSubscriberService, EventBridgeReceiverService {

    private static final Log log = LogFactory.getLog(EventBridge.class);
    private StreamDefinitionStore streamDefinitionStore;
    private EventDispatcher eventDispatcher;
    private Authenticator authenticator;
    private OMElement initialConfig;

    public EventBridge(AuthenticationHandler authenticationHandler,
                       AbstractStreamDefinitionStore streamDefinitionStore,
                       EventBridgeConfiguration eventBridgeConfiguration) {
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, eventBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticator = new Authenticator(authenticationHandler, eventBridgeConfiguration);
    }

    public EventBridge(AuthenticationHandler authenticationHandler,
                       AbstractStreamDefinitionStore streamDefinitionStore) {
        EventBridgeConfiguration eventBridgeConfiguration =new EventBridgeConfiguration();
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, eventBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticator = new Authenticator(authenticationHandler, eventBridgeConfiguration);
    }

    public String defineEventStream(String sessionId, String streamDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            return eventDispatcher.defineEventStream(streamDefinition, agentSession);
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        }
    }

    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws NoStreamDefinitionExistException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            return eventDispatcher.findEventStreamId(agentSession.getCredentials(), streamName,
                    streamVersion);
        } catch (StreamDefinitionNotFoundException e) {
            throw new NoStreamDefinitionExistException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new NoStreamDefinitionExistException(e.getErrorMessage(), e);
        }

    }


    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        eventDispatcher.publish(eventBundle, agentSession, eventConverter);
    }

    public String login(String username, String password) throws AuthenticationException {
        log.info(username + " connected");
        return authenticator.authenticate(username, password);
    }


    public void logout(String sessionId) throws Exception {
        log.info(sessionId + " disconnected");
        authenticator.logout(sessionId);
    }

    public OMElement getInitialConfig() {
        return initialConfig;
    }

    public void setInitialConfig(OMElement initialConfig) {
        this.initialConfig = initialConfig;
    }

    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(AgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    @Override
    public EventStreamDefinition getEventStreamDefinition(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        return getStreamDefinition(agentSession.getCredentials(), streamName, streamVersion);
    }

    @Override
    public List<EventStreamDefinition> getAllEventStreamDefinitions(String sessionId) throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        return getAllStreamDefinitions(agentSession.getCredentials());
    }

    @Override
    public void saveEventStreamDefinition(String sessionId, EventStreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
            DifferentStreamDefinitionAlreadyDefinedException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        saveStreamDefinition(agentSession.getCredentials(), streamDefinition);
    }


    // Stream store operations
    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName,
                                                     String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamName, streamVersion);

    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamId);

    }

    @Override
    public List<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials) {
        return new ArrayList<EventStreamDefinition>(streamDefinitionStore.getAllStreamDefinitions(credentials));
    }


    public void saveStreamDefinition(Credentials credentials, String eventStreamDefinition)
            throws MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        saveStreamDefinition(credentials, EventDefinitionConverterUtils.convertFromJson(eventStreamDefinition));
    }



    @Override
    public void saveStreamDefinition(Credentials credentials, EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        streamDefinitionStore.saveStreamDefinition(credentials, eventStreamDefinition);
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
    }


    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }


}




