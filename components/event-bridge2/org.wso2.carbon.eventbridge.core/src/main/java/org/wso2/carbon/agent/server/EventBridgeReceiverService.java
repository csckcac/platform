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
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.SessionTimeoutException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public interface EventBridgeReceiverService {


    public String defineEventStream(String sessionId, String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException, SessionTimeoutException;

    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws NoStreamDefinitionExistException, SessionTimeoutException;

    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException;


    public String login(String username, String password) throws AuthenticationException;

    public void logout(String sessionId) throws Exception;

    public OMElement getInitialConfig();
}




