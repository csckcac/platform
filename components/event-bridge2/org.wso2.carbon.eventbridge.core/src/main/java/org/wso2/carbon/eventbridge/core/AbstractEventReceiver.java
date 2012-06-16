package org.wso2.carbon.eventbridge.core;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;

import java.util.List;

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
public abstract class AbstractEventReceiver implements EventBridgeReceiverService {


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
    public EventStreamDefinition getEventStreamDefinition(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return getEventBridge().getEventStreamDefinition(sessionId, streamName, streamVersion);
    }

    @Override
    public List<EventStreamDefinition> getAllEventStreamDefinitions(String sessionId) throws SessionTimeoutException {
        return getEventBridge().getAllEventStreamDefinitions(sessionId);
    }

    @Override
    public void saveEventStreamDefinition(String sessionId, EventStreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
            DifferentStreamDefinitionAlreadyDefinedException {
        getEventBridge().saveEventStreamDefinition(sessionId, streamDefinition);
    }

    @Override
    public OMElement getInitialConfig() {
        return getEventBridge().getInitialConfig();
    }

    protected abstract EventBridge getEventBridge();
}
