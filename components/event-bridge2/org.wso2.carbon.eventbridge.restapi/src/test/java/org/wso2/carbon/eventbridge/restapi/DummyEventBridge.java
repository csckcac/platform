package org.wso2.carbon.eventbridge.restapi;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.eventbridge.core.EventBridgeReceiverService;
import org.wso2.carbon.eventbridge.core.EventConverter;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
public class DummyEventBridge implements EventBridgeReceiverService {

    private Map<String, EventStreamDefinition> streamDefnCache = new ConcurrentHashMap<String, EventStreamDefinition>();
    private Map<String, Credentials> sessionIdCache = new ConcurrentHashMap<String, Credentials>();
    private Credentials credentials;

    public static final String ALLOWED_DUMMY_USERNAME = "dummy";

    public static final String ALLOWED_DUMMY_PASSWORD = "dummy123";


    public DummyEventBridge() {
        credentials = new Credentials(ALLOWED_DUMMY_USERNAME, ALLOWED_DUMMY_PASSWORD);
    }

    @Override
    public String defineEventStream(String sessionId, String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException, MalformedStreamDefinitionException,
            SessionTimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws NoStreamDefinitionExistException, SessionTimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EventStreamDefinition getEventStreamDefinition(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EventStreamDefinition> getAllEventStreamDefinitions(String sessionId) throws SessionTimeoutException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveEventStreamDefinition(String sessionId, EventStreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
            DifferentStreamDefinitionAlreadyDefinedException {


    }

    private boolean isLoggedIn(String sessionId) {
        return sessionIdCache.containsKey(sessionId);
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        // only allow dummy user to log in
        Credentials newCredentials = new Credentials(username, password);
        if (credentials.equals(newCredentials)) {
            String sessionId = UUID.randomUUID().toString();
            sessionIdCache.put(sessionId, credentials);
            return sessionId;
        } else {
            throw new AuthenticationException("Invalid user log in");
        }
    }

    @Override
    public void logout(String sessionId) throws Exception {
        sessionIdCache.remove(sessionId);
    }

    @Override
    public OMElement getInitialConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
