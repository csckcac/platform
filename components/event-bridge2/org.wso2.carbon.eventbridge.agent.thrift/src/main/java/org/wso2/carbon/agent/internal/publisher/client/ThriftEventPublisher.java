/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.agent.internal.publisher.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.SessionTimeoutException;
import org.wso2.carbon.agent.commons.exception.StreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.agent.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.conf.DataPublisherConfiguration;
import org.wso2.carbon.agent.exception.EventPublisherException;
import org.wso2.carbon.agent.internal.EventQueue;
import org.wso2.carbon.agent.internal.publisher.authenticator.AgentAuthenticator;
import org.wso2.carbon.agent.internal.utils.ThriftEventConverter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The publisher who sends all the arrived events to the Agent Server using a pool of threads
 */
public class ThriftEventPublisher extends EventPublisher {
    private static Log log = LogFactory.getLog(ThriftEventPublisher.class);

    public ThriftEventPublisher(EventQueue<Event> eventQueue, GenericKeyedObjectPool transportPool,
                                Semaphore queueSemaphore, int maxMessageBundleSize,
                                DataPublisherConfiguration dataPublisherConfiguration,
                                AgentAuthenticator agentAuthenticator,
                                ThreadPoolExecutor threadPool) {
        super(eventQueue, transportPool, queueSemaphore, maxMessageBundleSize, dataPublisherConfiguration, agentAuthenticator, threadPool);
    }

    @Override
    protected int getNumberOfEvents(Object eventBundle) {
        return ((ThriftEventBundle) eventBundle).getEventNum();
    }


    @Override
    protected ThriftEventBundle convertToEventBundle(Object eventBundle, Event event,
                                                     String sessionId) {
        return ThriftEventConverter.
                toThriftEventBundle(event, (ThriftEventBundle) eventBundle, sessionId);
    }

    @Override
    protected void setSessionId(Object eventBundle, String sessionId) {
        ((ThriftEventBundle) eventBundle).setSessionId(sessionId);
    }

    @Override
    protected String getSessionId(Object eventBundle) {
        return ((ThriftEventBundle) eventBundle).getSessionId();
    }


    @Override
    void publish(Object client, Object eventBundle)
            throws UndefinedEventTypeException, SessionTimeoutException, EventPublisherException {
        try {
            if (client instanceof ThriftSecureEventTransmissionService.Client) {
                ((ThriftSecureEventTransmissionService.Client) client).publish((ThriftEventBundle) eventBundle);
            } else {
                ((ThriftEventTransmissionService.Client) client).publish((ThriftEventBundle) eventBundle);
            }
        } catch (ThriftUndefinedEventTypeException e) {
            throw new UndefinedEventTypeException("Thrift Undefined Event Type Exception ", e);
        } catch (ThriftSessionExpiredException e) {
            throw new SessionTimeoutException("Thrift Session Expired Exception ", e);
        } catch (TException e) {
            throw new EventPublisherException("Cannot send Events", e);
        }

    }


    @Override
    protected String defineEventStream(Object client, String sessionId,
                                       String eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException, EventPublisherException,
                   SessionTimeoutException, StreamDefinitionException {
        try {
            if (client instanceof ThriftSecureEventTransmissionService.Client) {
                return ((ThriftSecureEventTransmissionService.Client) client).defineEventStream(sessionId, eventStreamDefinition);
            } else {
                return ((ThriftEventTransmissionService.Client) client).defineEventStream(sessionId, eventStreamDefinition);
            }
        } catch (ThriftDifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Thrift Different Stream Definition Already Defined ", e);
        } catch (ThriftMalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException("Malformed Stream Definition ", e);
        } catch (ThriftStreamDefinitionException e) {
            throw new StreamDefinitionException("Thrift Stream Definition Exception ", e);
        } catch (ThriftSessionExpiredException e) {
            throw new SessionTimeoutException("Session Expired ", e);
        } catch (TException e) {
            throw new EventPublisherException("TException ", e);
        }
    }

    @Override
    protected String findEventStreamId(Object client, String currentSessionId, String name,
                                       String version)
            throws NoStreamDefinitionExistException, SessionTimeoutException,
                   EventPublisherException {
        try {
            if (client instanceof ThriftSecureEventTransmissionService.Client) {
                return ((ThriftSecureEventTransmissionService.Client) client).findEventStreamId(currentSessionId, name, version);
            } else {
                return ((ThriftEventTransmissionService.Client) client).findEventStreamId(currentSessionId, name, version);
            }
        } catch (ThriftNoStreamDefinitionExistException e) {
            throw new NoStreamDefinitionExistException("Thrift No Stream Definition Exist", e);
        } catch (ThriftSessionExpiredException e) {
            throw new SessionTimeoutException("Session Expired ", e);
        } catch (TException e) {
            throw new EventPublisherException("Thrift Exception", e);
        }
    }


}
