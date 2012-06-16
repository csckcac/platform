package org.wso2.carbon.eventbridge.core;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.eventbridge.core.Utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.definitionstore.StreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.core.internal.utils.EventConverter;

import java.util.Collection;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public abstract class AbstractEventReceiver implements EventBridgeReceiverService,
        StreamDefinitionStore{


    @Override
    public String defineEventStream(String sessionId, String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException, MalformedStreamDefinitionException,
            SessionTimeoutException {
        return getEventBridge().defineEventStream(sessionId, streamDefinition);
    }

    @Override
    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws NoStreamDefinitionExistException, SessionTimeoutException {
        return getEventBridge().findEventStreamId(sessionId, streamName, streamVersion);
    }

    @Override
    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        getEventBridge().publish(eventBundle, sessionId, eventConverter);
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        return getEventBridge().login(username, password);
    }

    @Override
    public void logout(String sessionId) throws Exception {
        getEventBridge().logout(sessionId);
    }

    @Override
    public OMElement getInitialConfig() {
        return getEventBridge().getInitialConfig();
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        String streamId = getEventBridge().getStreamId(credentials, streamName, streamVersion);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + credentials.getUsername()
                    + " for " + EventBridgeUtils.constructStreamKey(streamName, streamVersion));
        }
        return getEventBridge().getStreamDefinition( credentials, streamId);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        EventStreamDefinition eventStreamDefinition = getEventBridge().getStreamDefinition(credentials, streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + credentials.getUsername() + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    @Override
    public Collection<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials) {
        return getEventBridge().getAllStreamDefinitions(credentials);
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        String streamId = getEventBridge().getStreamId(credentials, streamName, streamVersion);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    @Override
    public void saveStreamDefinition(Credentials credentials, EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {
        getEventBridge().saveStreamDefinition(credentials, eventStreamDefinition);
    }

    protected abstract EventBridge getEventBridge();
}
