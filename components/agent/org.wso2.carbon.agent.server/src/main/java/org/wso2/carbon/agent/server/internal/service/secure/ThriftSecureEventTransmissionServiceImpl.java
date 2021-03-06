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

package org.wso2.carbon.agent.server.internal.service.secure;

import org.apache.thrift.TException;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.SessionTimeoutException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.server.internal.EventDispatcher;

/**
 * The client implementation for ThriftSecureEventTransmissionService
 */
public class ThriftSecureEventTransmissionServiceImpl
        implements ThriftSecureEventTransmissionService.Iface {

    private SecureEventReceiverService secureEventReceiverService;

    public ThriftSecureEventTransmissionServiceImpl(EventDispatcher eventDispatcher) {
        this.secureEventReceiverService = new SecureEventReceiverService(eventDispatcher);
    }
    public String connect(String username, String password) throws ThriftAuthenticationException {
        try {
            return SecureEventReceiverService.connect(username, password);
        } catch (AuthenticationException e) {
            throw new ThriftAuthenticationException(e.getErrorMessage());
        }
    }

    public void disconnect(String sessionId) throws TException {
        try {
            SecureEventReceiverService.disconnect(sessionId);
        } catch (Exception e) {
            throw new TException(e.getMessage());
        }
    }

    @Override
    public String defineEventStream(String sessionId, String streamDefinition)
            throws TException, ThriftSessionExpiredException,
                   ThriftDifferentStreamDefinitionAlreadyDefinedException,
                   ThriftMalformedStreamDefinitionException {
        try {
            return secureEventReceiverService.defineEventStream(sessionId, streamDefinition);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new ThriftDifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage());
        } catch (MalformedStreamDefinitionException e) {
            throw new ThriftMalformedStreamDefinitionException(e.getErrorMessage());
        } catch (SessionTimeoutException e) {
            throw new ThriftSessionExpiredException(e.getErrorMessage());
        }
    }

    @Override
    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws ThriftNoStreamDefinitionExistException, ThriftSessionExpiredException,
                   TException {
        try {
            return secureEventReceiverService.findEventStreamId(sessionId, streamName, streamVersion);
        } catch (NoStreamDefinitionExistException e) {
            throw new ThriftNoStreamDefinitionExistException(e.getErrorMessage());
        } catch (SessionTimeoutException e) {
            throw new ThriftSessionExpiredException(e.getErrorMessage());
        }
    }


    public void publish(ThriftEventBundle eventBundle)
            throws ThriftUndefinedEventTypeException, ThriftSessionExpiredException, TException {
        try {
            secureEventReceiverService.publish(eventBundle, eventBundle.getSessionId());
        } catch (UndefinedEventTypeException e) {
            throw new ThriftUndefinedEventTypeException(e.getErrorMessage());
        } catch (SessionTimeoutException e) {
            throw new ThriftSessionExpiredException(e.getErrorMessage());
        }
    }
}
