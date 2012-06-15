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

package org.wso2.carbon.agent.server;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Credentials;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.SessionTimeoutException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.utils.EventDefinitionConverter;
import org.wso2.carbon.agent.server.datastore.AbstractStreamDefinitionStore;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.EventDispatcher;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.authentication.session.AgentSession;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

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
    private OMElement initialConfig;

    public EventBridge(AuthenticationHandler authenticationHandler,
                       AbstractStreamDefinitionStore streamDefinitionStore) {
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
    }

    public String defineEventStream(String sessionId, String streamDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = Authenticator.getInstance().getSession(sessionId);
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
        }
    }

    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws NoStreamDefinitionExistException, SessionTimeoutException {
        AgentSession agentSession = Authenticator.getInstance().
                getSession(sessionId);
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
        }

    }


    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        AgentSession agentSession = Authenticator.getInstance().getSession(sessionId);
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
        try {
            return Authenticator.getInstance().authenticate(username, password);
        } catch (AuthenticationException e) {
            throw new AuthenticationException(username + " is not authorised to access the server " + e.getErrorMessage());
        }
    }

    public void logout(String sessionId) throws Exception {
        log.info(sessionId + " disconnected");
        Authenticator.getInstance().logout(sessionId);
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
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName,
                                                     String streamVersion)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamName, streamVersion);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamId);
    }

    @Override
    public List<EventStreamDefinition> getAllStreamDefinition(Credentials credentials)
            throws StreamDefinitionNotFoundException {
        return new ArrayList<EventStreamDefinition>(streamDefinitionStore.getAllStreamDefinitions(credentials));
    }

    @Override
    public void saveEventStreamDefinition(Credentials credentials, String eventStreamDefinition)
            throws MalformedStreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        streamDefinitionStore.saveStreamDefinition(credentials, EventDefinitionConverter.convertFromJson(eventStreamDefinition));
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
    }


    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }


}




