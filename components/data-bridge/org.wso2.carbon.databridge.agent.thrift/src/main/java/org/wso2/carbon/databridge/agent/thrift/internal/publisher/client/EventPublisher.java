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


package org.wso2.carbon.databridge.agent.thrift.internal.publisher.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.exception.EventPublisherException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.agent.thrift.internal.EventQueue;
import org.wso2.carbon.databridge.agent.thrift.internal.publisher.authenticator.AgentAuthenticator;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The publisher who sends all the arrived events to the Agent Server using a pool of threads
 */
public abstract class EventPublisher implements Runnable {
    private static Log log = LogFactory.getLog(EventPublisher.class);

    private EventQueue<Event> eventQueue;
    private GenericKeyedObjectPool transportPool;
    private Semaphore queueSemaphore;
    private int maxMessageBundleSize;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private AgentAuthenticator agentAuthenticator;
    private ThreadPoolExecutor threadPool;

    public EventPublisher(EventQueue<Event> eventQueue,
                          GenericKeyedObjectPool transportPool,
                          Semaphore queueSemaphore,
                          int maxMessageBundleSize,
                          DataPublisherConfiguration dataPublisherConfiguration,
                          AgentAuthenticator agentAuthenticator, ThreadPoolExecutor threadPool) {

        this.eventQueue = eventQueue;
        this.transportPool = transportPool;
        this.queueSemaphore = queueSemaphore;
        this.maxMessageBundleSize = maxMessageBundleSize;
        this.dataPublisherConfiguration = dataPublisherConfiguration;
        this.agentAuthenticator = agentAuthenticator;
        this.threadPool = threadPool;
    }

    public void run() {
        Object eventBundle = null;
        while (true) {
            Event event = eventQueue.poll();
            if (event != null) {
                queueSemaphore.release();
                eventBundle = convertToEventBundle(eventBundle, event, dataPublisherConfiguration.getSessionId());

                //Sending the event bundle when maxMessageBundleSize is reached
                if (getNumberOfEvents(eventBundle) >= maxMessageBundleSize) {
                    publishEvent(eventBundle);

                    //checking if all threads in the pool are NOT active
                    if (threadPool.getActiveCount() < threadPool.getCorePoolSize()) {
                        //starts building another event bundle with the same thread
                        eventBundle = null;
                    } else {
                        //submit the remaining task to the end of the taskQueue
                        // and exit
                        threadPool.submit(this);
                        break;
                    }
                }
            } else {
                //When the queue is empty
                if (eventBundle != null) {
                    publishEvent(eventBundle);
                }
                //Since no more events are available to dispatch, exit
                break;
            }
        }
    }

    private void publishEvent(Object eventBundle) {
        Object client = null;
        try {
            client = getClient(dataPublisherConfiguration.getPublisherKey());
            setSessionId(eventBundle, dataPublisherConfiguration.getSessionId());
            publish(client, eventBundle);
        } catch (AgentException e) {
            log.error("Cannot get a client to send events to " +
                      dataPublisherConfiguration.getPublisherKey(), e);
            transportPool.clear(dataPublisherConfiguration.getPublisherKey());

        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            setSessionId(eventBundle, reconnect(getSessionId(eventBundle)));
            republish(client, eventBundle);
        } catch (UndefinedEventTypeException e) {
            log.error("Wrongly typed event " + eventBundle + " sent to " +
                      dataPublisherConfiguration.getPublisherKey(), e);
        } catch (EventPublisherException e) {
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey(), e);
        }

        try {
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool");
            transportPool.clear(dataPublisherConfiguration.getPublisherKey());
        }
    }

    private void republish(Object client, Object eventBundle) {
        try {
            publish(client, eventBundle);
        } catch (EventPublisherException e) {
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                      " even after reconnecting ", e);
        } catch (SessionTimeoutException e) {
            log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                      + " even after reconnecting ", e);
        } catch (UndefinedEventTypeException e) {
            log.error("Wrongly typed event " + eventBundle.toString() + " sent  to " +
                      dataPublisherConfiguration.getPublisherKey(), e);
        }
    }


    protected abstract int getNumberOfEvents(Object eventBundle);

    protected abstract ThriftEventBundle convertToEventBundle(Object eventBundle, Event event,
                                                              String sessionId);


    protected abstract void setSessionId(Object eventBundle, String reconnect);

    protected abstract String getSessionId(Object eventBundle);


    abstract void publish(Object client, Object eventBundle)
            throws UndefinedEventTypeException, SessionTimeoutException, EventPublisherException;

    public String defineStream(String sessionId, String streamDefinition)
            throws AgentException, DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException, StreamDefinitionException {
        String currentSessionId = sessionId;
        String streamId = null;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = defineStream(client, currentSessionId, streamDefinition);
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            redefineStream(client, streamDefinition, currentSessionId);
        } catch (StreamDefinitionException e) {
            throw new StreamDefinitionException("Invalid type definition for stream " +
                                                streamDefinition, e);
        } catch (EventPublisherException e) {
            throw new AgentException("Cannot define type " + streamDefinition, e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Same stream id with different definition already defined before sending this event definitions to " +
                                                                       dataPublisherConfiguration.getPublisherKey(), e);
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException("Malformed event definition :" + streamDefinition + " send  to " +
                                                         dataPublisherConfiguration.getPublisherKey(), e);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
        }
        return streamId;
    }

    protected abstract String defineStream(Object client, String currentSessionId,
                                                String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException,
                   EventPublisherException, SessionTimeoutException, StreamDefinitionException;

    private void redefineStream(Object client, String streamDefinition,
                                     String currentSessionId
    )
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException,
                   MalformedStreamDefinitionException {
        try {
            defineStream(client, currentSessionId, streamDefinition);
        } catch (SessionTimeoutException ex) {
            log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                      + " even after reconnecting ", ex);
        } catch (EventPublisherException ex) {
            log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                      " even after reconnecting ", ex);
        } catch (DifferentStreamDefinitionAlreadyDefinedException ex) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Type already defined when send event definitions to" +
                                                                       dataPublisherConfiguration.getPublisherKey(), ex);
        } catch (StreamDefinitionException ex) {
            throw new StreamDefinitionException("Wrongly defined event definition after reconnection  :" + streamDefinition + " sent to " +
                                                dataPublisherConfiguration.getPublisherKey(), ex);
        } catch (MalformedStreamDefinitionException ex) {
            throw new MalformedStreamDefinitionException("Malformed event definition after reconnection  :" + streamDefinition + " sent to " +
                                                         dataPublisherConfiguration.getPublisherKey(), ex);
        }
    }


    public String findStreamId(String sessionId, String name, String version)
            throws AgentException, StreamDefinitionException, NoStreamDefinitionExistException {
        String currentSessionId = sessionId;
        String streamId = null;
        Object client = getClient(dataPublisherConfiguration.getPublisherKey());
        try {
            streamId = findStreamId(client, currentSessionId, name, version);
            transportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
        } catch (NoStreamDefinitionExistException e) {
            throw new NoStreamDefinitionExistException("No stream id found for : " + name + " " + version, e);
        } catch (SessionTimeoutException e) {
            log.info("Session timed out for " + dataPublisherConfiguration.getPublisherKey() + "," + e.getMessage());
            currentSessionId = reconnect(currentSessionId);
            try {
                streamId = findStreamId(client, currentSessionId, name, version);
            } catch (SessionTimeoutException ex) {
                log.error("Session timed out for " + dataPublisherConfiguration.getPublisherKey()
                          + " even after reconnecting ", ex);
            } catch (EventPublisherException ex) {
                log.error("Cannot send events to " + dataPublisherConfiguration.getPublisherKey() +
                          " even after reconnecting ", ex);
            } catch (NoStreamDefinitionExistException ex) {
                throw new NoStreamDefinitionExistException("No stream id found for : " + name + " " + version, ex);
            }
        } catch (EventPublisherException e) {
            throw new AgentException("Error when finding event stream definition for : " + name + " " + version, e);
        } catch (Exception e) {
            log.warn("Error occurred while returning object to connection pool", e);
        }
        return streamId;

    }

    protected abstract String findStreamId(Object client, String currentSessionId, String name,
                                                String version)
            throws NoStreamDefinitionExistException, SessionTimeoutException,
                   EventPublisherException;

    private String reconnect(String currentSessionId) {
        attemptReconnection(AgentConstants.AGENT_RECONNECTION_TIMES, currentSessionId);
        return dataPublisherConfiguration.getSessionId();
    }

    public synchronized void attemptReconnection(
            int reconnectionTime, String sessionId) {
        if (!dataPublisherConfiguration.getSessionId().equals(sessionId)) {
            return;
        }
        if (reconnectionTime > 0) {
            try {
                dataPublisherConfiguration.setSessionId(
                        agentAuthenticator.connect(dataPublisherConfiguration));
            } catch (AuthenticationException e) {
                log.error(dataPublisherConfiguration.getReceiverConfiguration().getUserName() +
                          " not authorised to access server at " +
                          dataPublisherConfiguration.getPublisherKey());
            } catch (TransportException e) {
                attemptReconnection(reconnectionTime - 1, sessionId);
            } catch (AgentException e) {
                attemptReconnection(reconnectionTime - 1, sessionId);
            }
        }
    }

    private Object getClient(String publisherKey) throws AgentException {
        try {
            return transportPool.borrowObject(publisherKey);
        } catch (Exception e) {
            throw new AgentException("Cannot borrow client for " + publisherKey, e);
        }
    }
}
