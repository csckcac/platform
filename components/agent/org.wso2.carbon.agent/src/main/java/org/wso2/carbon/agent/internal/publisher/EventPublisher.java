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


package org.wso2.carbon.agent.internal.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.StreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.WrongEventTypeException;
import org.wso2.carbon.agent.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.exception.ThriftUndefinedEventTypeException;
import org.wso2.carbon.agent.commons.thrift.service.ThriftEventReceiverService;
import org.wso2.carbon.agent.conf.DataPublisherConfiguration;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.EventQueue;
import org.wso2.carbon.agent.internal.utils.EventConverter;

import java.util.concurrent.Semaphore;

/**
 * The publisher who send all the arrived events to the Agent Server using a thread pool
 */
public class EventPublisher implements Runnable {
    private static Log log = LogFactory.getLog(EventPublisher.class);

    private EventQueue<Event> eventQueue;
    private GenericKeyedObjectPool transportPool;
    private Semaphore queueSemaphore;
    private int maxMessageBundleSize;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private AgentAuthenticator agentAuthenticator;

    public EventPublisher(EventQueue<Event> eventQueue,
                          GenericKeyedObjectPool transportPool,
                          Semaphore queueSemaphore,
                          int maxMessageBundleSize,
                          DataPublisherConfiguration dataPublisherConfiguration,
                          AgentAuthenticator agentAuthenticator) {

        this.eventQueue = eventQueue;
        this.transportPool = transportPool;
        this.queueSemaphore = queueSemaphore;
        this.maxMessageBundleSize = maxMessageBundleSize;
        this.dataPublisherConfiguration = dataPublisherConfiguration;
        this.agentAuthenticator = agentAuthenticator;
    }

    public void run() {
        ThriftEventBundle thriftEventBundle = null;
        while (true) {
            Event event = eventQueue.poll();
            if (event != null) {
                queueSemaphore.release();
                thriftEventBundle = EventConverter.
                        toThriftEventBundle(event,
                                            thriftEventBundle,
                                            dataPublisherConfiguration.getSessionId());
                if (thriftEventBundle != null && thriftEventBundle.eventNum >= maxMessageBundleSize) {
                    sendEvent(thriftEventBundle);
                    thriftEventBundle = null;
                }
            } else {
                if (thriftEventBundle != null) {
                    sendEvent(thriftEventBundle);
                }
                break;
            }
        }
    }

    private void sendEvent(ThriftEventBundle thriftEventBundle) {
        ThriftEventReceiverService.Client client = null;
        try {
            client = getThriftClient(dataPublisherConfiguration.getPublisherKey());
            thriftEventBundle.setSessionId(dataPublisherConfiguration.getSessionId());
            client.publish(thriftEventBundle);
        } catch (AgentException e) {
            log.error("Cannot get a client to send events to " +
                      dataPublisherConfiguration.getPublisherKey(), e);
            transportPool.clear(dataPublisherConfiguration.getPublisherKey());
        } catch (ThriftSessionExpiredException e) {
            reconnect(3, thriftEventBundle.getSessionId());
            try {
                thriftEventBundle.setSessionId(dataPublisherConfiguration.getSessionId());
                client.publish(thriftEventBundle);
            } catch (ThriftUndefinedEventTypeException ex) {
                log.error("Wrongly typed event " + thriftEventBundle.toString() + " send events to " +
                          dataPublisherConfiguration.getPublisherKey(), ex);
            } catch (ThriftSessionExpiredException ex) {
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                          + " even after reconnecting ", ex);
            } catch (TException ex) {
                log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                          " even after reconnecting ", ex);
            }
        } catch (TException e) {
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey(), e);
        } catch (ThriftUndefinedEventTypeException e) {
            log.error("Wrongly typed event " + thriftEventBundle.toString() + " send events to " +
                      dataPublisherConfiguration.getPublisherKey(), e);

        }

        try {
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool");
            transportPool.clear(dataPublisherConfiguration.getPublisherKey());
        }
    }

    public String defineEventStream(String sessionId, String eventStreamDefinition)
            throws AgentException, DifferentStreamDefinitionAlreadyDefinedException,
                   WrongEventTypeException, MalformedStreamDefinitionException,
                   StreamDefinitionException {
        String streamId = null;
        ThriftEventReceiverService.Client client = getThriftClient(
                dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = client.defineEventStream(sessionId, eventStreamDefinition);
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (ThriftStreamDefinitionException e) {
            throw new WrongEventTypeException("Invalid type definition for stream " +
                                              eventStreamDefinition, e);
        } catch (TException e) {
            throw new AgentException("Cannot define type " + eventStreamDefinition, e);
        } catch (ThriftSessionExpiredException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey(), e);
            reconnect(3, sessionId);
            try {
                sessionId = dataPublisherConfiguration.getSessionId();
                client.defineEventStream(sessionId, eventStreamDefinition);
            } catch (ThriftSessionExpiredException ex) {
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                          + " even after reconnecting ", ex);
            } catch (TException ex) {
                log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                          " even after reconnecting ", ex);
            } catch (ThriftDifferentStreamDefinitionAlreadyDefinedException e1) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Type already defined when send event definitions to" +
                                                                           dataPublisherConfiguration.getPublisherKey(), e);
            } catch (ThriftStreamDefinitionException e1) {
                throw new StreamDefinitionException("Wrongly event definition :" + eventStreamDefinition + " send event definitions to " +
                                                    dataPublisherConfiguration.getPublisherKey(), e);
            } catch (ThriftMalformedStreamDefinitionException e1) {
                throw new MalformedStreamDefinitionException("Wrongly event definition :" + eventStreamDefinition + " send event definitions to " +
                                                             dataPublisherConfiguration.getPublisherKey(), e);
            }
        } catch (ThriftDifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Same stream id with different definition already defined before sending this event definitions to " +
                                                                       dataPublisherConfiguration.getPublisherKey(), e);
        } catch (ThriftMalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException("Wrongly event definition :" + eventStreamDefinition + " send event definitions to " +
                                                         dataPublisherConfiguration.getPublisherKey(), e);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
        }
        return streamId;
    }


    public String findEventStreamId(String sessionId, String name, String version)
            throws AgentException, StreamDefinitionException, NoStreamDefinitionExistException {
        String streamId = null;
        ThriftEventReceiverService.Client client = getThriftClient(
                dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = client.findEventStreamId(sessionId, name, version);
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (ThriftNoStreamDefinitionExistException e) {
            throw new NoStreamDefinitionExistException("No stream id found for : " + name + " " + version, e);
        } catch (TException e) {
            throw new AgentException("Error when finding event stream definition for : " + name + " " + version, e);
        } catch (ThriftSessionExpiredException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey(), e);
            reconnect(3, sessionId);
            try {
                sessionId = dataPublisherConfiguration.getSessionId();
                streamId = client.findEventStreamId(sessionId, name, version);
            } catch (ThriftSessionExpiredException ex) {
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                          + " even after reconnecting ", ex);
            } catch (TException ex) {
                log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                          " even after reconnecting ", ex);
            } catch (ThriftNoStreamDefinitionExistException e1) {
                throw new NoStreamDefinitionExistException("No stream id found for : " + name + " " + version, e);
            }
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
        }
        return streamId;

    }

    public synchronized void reconnect(
            int connectionTime, String sessionId) {
        if (!dataPublisherConfiguration.getSessionId().equals(sessionId)) {
            return;
        }
        if (connectionTime > 0) {
            connectionTime--;
            try {
                dataPublisherConfiguration.setSessionId(
                        agentAuthenticator.connect(
                                dataPublisherConfiguration.getReceiverConfiguration()));
            } catch (AuthenticationException e) {
                log.error(dataPublisherConfiguration.getReceiverConfiguration().getUserName() +
                          " not authorised to access server at " +
                          dataPublisherConfiguration.getPublisherKey());
            } catch (TransportException e) {
                reconnect(connectionTime, sessionId);
            } catch (AgentException e) {
                reconnect(connectionTime, sessionId);
            }
        }
    }

    private ThriftEventReceiverService.Client getThriftClient(String publisherKey)
            throws AgentException {
        try {
            return (ThriftEventReceiverService.Client) transportPool.borrowObject(publisherKey);
        } catch (Exception e) {
            throw new AgentException("Cannot borrow client for " + publisherKey, e);
        }
    }
}
