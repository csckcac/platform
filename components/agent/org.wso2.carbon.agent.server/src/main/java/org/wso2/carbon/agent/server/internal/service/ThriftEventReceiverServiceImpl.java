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

package org.wso2.carbon.agent.server.internal.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.service.ThriftEventReceiverService;
import org.wso2.carbon.agent.server.internal.EventDispatcher;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.authentication.session.AgentSession;

public class ThriftEventReceiverServiceImpl implements ThriftEventReceiverService.Iface {

    private EventDispatcher eventDispatcher;
    private static final Log log = LogFactory.getLog(Authenticator.class);

    public ThriftEventReceiverServiceImpl(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public String defineEventStream(String sessionId, String streamDefinition)
            throws TException, ThriftSessionExpiredException,
                   ThriftDifferentStreamDefinitionAlreadyDefinedException,
                   ThriftMalformedStreamDefinitionException {
        AgentSession agentSession = Authenticator.getInstance().getSessionTypeDef(sessionId);
        if (agentSession.getCreatedAt() == 0) {
            log.info("session " + sessionId + " expired ");
            throw new ThriftSessionExpiredException(sessionId + " expired");
        }
        return eventDispatcher.defineEventStream(streamDefinition, agentSession);
    }

    @Override
    public String findEventStreamId(String sessionId, String streamName, String streamVersion)
            throws ThriftNoStreamDefinitionExistException, ThriftSessionExpiredException,
                   TException {
        AgentSession agentSession = Authenticator.getInstance().
                getSessionTypeDef(sessionId);
        if (agentSession.getCreatedAt() == 0) {
            log.info("session " + sessionId + " expired ");
            throw new ThriftSessionExpiredException(sessionId + " expired");
        }
        return eventDispatcher.findEventStreamId(agentSession.getDomainName(), streamName, streamVersion);

    }


    public void publish(ThriftEventBundle thriftEventBundle)
            throws ThriftUndefinedEventTypeException, ThriftSessionExpiredException, TException {
        AgentSession agentSession = Authenticator.getInstance().
                getSessionTypeDef(thriftEventBundle.getSessionId());
        if (agentSession.getCreatedAt() == 0) {
            log.info("session " + thriftEventBundle.getSessionId() + " expired ");
            throw new ThriftSessionExpiredException(thriftEventBundle.sessionId + " expired");
        }
        eventDispatcher.publish(thriftEventBundle, agentSession);
    }
}
