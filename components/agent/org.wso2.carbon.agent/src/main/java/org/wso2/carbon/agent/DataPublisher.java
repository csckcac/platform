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

package org.wso2.carbon.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentTypeDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedTypeDefinitionException;
import org.wso2.carbon.agent.commons.exception.TypeDefinitionException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.exception.WrongEventTypeException;
import org.wso2.carbon.agent.conf.DataPublisherConfiguration;
import org.wso2.carbon.agent.conf.ReceiverConfiguration;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.EventQueue;
import org.wso2.carbon.agent.internal.pool.BoundedExecutor;
import org.wso2.carbon.agent.internal.publisher.EventPublisher;
import org.wso2.carbon.agent.internal.utils.AgentServerURL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

/**
 * Publisher will handle one connection, where it will connect to the server,
 * define type,send events and will get disconnect.
 */
public class DataPublisher {

    private static Log log = LogFactory.getLog(DataPublisher.class);

    private Agent agent;
    private DataPublisherConfiguration dataPublisherConfiguration;
    private EventPublisher eventPublisher;
    private EventQueue<Event> eventQueue;

    BoundedExecutor threadPool;

    public DataPublisher(String receiverUrl, String userName, String password)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        this(receiverUrl, userName, password, new Agent());
    }

    public DataPublisher(String authenticatorUrl, String receiverUrl, String userName,
                         String password)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        this(authenticatorUrl, receiverUrl, userName, password, new Agent());
    }

    public DataPublisher(String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);
        this.init(new ReceiverConfiguration(userName, password,
                                            receiverURL.getHost(), receiverURL.getPort(),
                                            receiverURL.getHost(), receiverURL.getPort() + 100),
                  agent);

    }

    public DataPublisher(String authenticatorUrl, String receiverUrl, String userName,
                         String password, Agent agent)
            throws MalformedURLException, AgentException, AuthenticationException,
                   TransportException {
        AgentServerURL authenticatorURL = new AgentServerURL(authenticatorUrl);
        AgentServerURL receiverURL = new AgentServerURL(receiverUrl);

        this.init(new ReceiverConfiguration(userName, password,
                                            receiverURL.getHost(), receiverURL.getPort(),
                                            authenticatorURL.getHost(), authenticatorURL.getPort()),
                  agent);

    }

    public void setAgent(Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        this.agent.shutdown(this);// to shutdown the old agent
        init(this.dataPublisherConfiguration.getReceiverConfiguration(), agent);
    }

    public Agent getAgent() {
        return agent;
    }

    private void init(ReceiverConfiguration receiverConfiguration, Agent agent)
            throws AgentException, AuthenticationException, TransportException {
        agent.addDataPublisher(this);
        this.agent = agent;
        this.dataPublisherConfiguration = new DataPublisherConfiguration(receiverConfiguration);
        this.eventQueue = new EventQueue<Event>();
        this.threadPool = agent.getThreadPool();
        this.eventPublisher = new EventPublisher(eventQueue, agent.getTransportPool(), agent.getQueueSemaphore(),
                                                 agent.getAgentConfiguration().getMaxMessageBundleSize(),
                                                 dataPublisherConfiguration, agent.getAgentAuthenticator());
        dataPublisherConfiguration.setSessionId(agent.getAgentAuthenticator().connect(
                dataPublisherConfiguration.getReceiverConfiguration()));
    }

    /**
     * Defining the streams that will be published by this DataPublisher
     *
     * @param eventStreamDefinition the type definition of Streams
     * @return stream name
     * @throws org.wso2.carbon.agent.exception.AgentException
     *          if client cannot publish the type definition
     * @throws org.wso2.carbon.agent.commons.exception.DifferentTypeDefinitionAlreadyDefinedException
     *          if the session has expired
     * @throws org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException
     *          if the type definition is not defined
     * @throws org.wso2.carbon.agent.commons.exception.WrongEventTypeException
     *          if the type definition is wrong
     */
    public String defineEventStreamDefinition(String eventStreamDefinition)
            throws UndefinedEventTypeException, AgentException,
                   DifferentTypeDefinitionAlreadyDefinedException,
                   WrongEventTypeException, MalformedTypeDefinitionException,
                   TypeDefinitionException {
        String streamId = null;
        String sessionId = dataPublisherConfiguration.getSessionId();
        try {
            streamId = eventPublisher.defineType(sessionId, eventStreamDefinition);
        } catch (DifferentTypeDefinitionAlreadyDefinedException e) {
            log.warn("SessionTimeout for " + dataPublisherConfiguration.getPublisherKey());
            eventPublisher.reconnect(3, sessionId);
            streamId = eventPublisher.defineType(sessionId, eventStreamDefinition);
        }
        return streamId;
    }


    /**
     * Publishing the events to the server
     *
     * @param event the event to be published
     * @throws org.wso2.carbon.agent.exception.AgentException
     *          if client cannot publish the event
     */
    public void publish(Event event) throws AgentException {
        try {
            agent.getQueueSemaphore().acquire();
            if (eventQueue.put(event)) {
                try {
                    threadPool.submitTask(eventPublisher);
                } catch (RejectedExecutionException ignoreRejection) {
                    System.out.println("Rejected ");
                }
            }
        } catch (InterruptedException e) {
            throw new AgentException("Cannot and to event queue", e);
        }
    }

    public void publish(String streamId, Object[] metaDataArray, Object[] correlationDataArray,
                        Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, System.currentTimeMillis(), metaDataArray, correlationDataArray, payloadDataArray));
    }

    public void publish(String streamId, long timeStamp, Object[] metaDataArray,
                        Object[] correlationDataArray, Object[] payloadDataArray)
            throws AgentException {
        publish(new Event(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    /**
     * Disconnecting from the server
     */
    public void stop() {
        agent.getAgentAuthenticator().disconnect(dataPublisherConfiguration.getSessionId(),
                                                 dataPublisherConfiguration.getReceiverConfiguration());
        agent.shutdown(this);
    }


}
