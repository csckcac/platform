/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
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
package org.wso2.carbon.agent.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.utils.AgentConstants;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;
import org.wso2.carbon.base.ServerConfiguration;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Carbon based implementation of the agent server
 */
public abstract class AbstractAgentServer implements AgentServer {
    private static final Log log = LogFactory.getLog(AbstractAgentServer.class);
    private EventDispatcher eventDispatcher;
    private AgentServerConfiguration agentServerConfiguration;
    private StreamDefinitionStore streamDefinitionStore;

    /**
     * Initialize Carbon Agent Server
     *
     * @param secureReceiverPort
     * @param receiverPort
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public AbstractAgentServer(int secureReceiverPort, int receiverPort,
                               AuthenticationHandler authenticationHandler,
                               StreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.agentServerConfiguration = new AgentServerConfiguration(secureReceiverPort, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param receiverPort
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public AbstractAgentServer(int receiverPort,
                               AuthenticationHandler authenticationHandler,
                               StreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.agentServerConfiguration = new AgentServerConfiguration(receiverPort + AgentConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param agentServerConfiguration
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public AbstractAgentServer(AgentServerConfiguration agentServerConfiguration,
                               AuthenticationHandler authenticationHandler,
                               StreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.agentServerConfiguration = agentServerConfiguration;
    }

    /**
     * CEP/BAM can subscribe for Event Streams
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(AgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(String domainName, String streamName,
                                                     String streamVersion)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(domainName, streamName, streamVersion);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(String domainName, String streamId)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(domainName, streamId);
    }

    @Override
    public List<EventStreamDefinition> getAllStreamDefinition(String domainName)
            throws StreamDefinitionNotFoundException {
        return new ArrayList<EventStreamDefinition>(streamDefinitionStore.getAllStreamDefinitions(domainName));
    }

    @Override
    public void saveEventStreamDefinition(String domainName, String eventStreamDefinition)
            throws MalformedStreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        streamDefinitionStore.saveStreamDefinition(domainName, EventConverter.convertFromJson(eventStreamDefinition));
    }

    /**
     * To start the Agent server
     *
     * @throws org.wso2.carbon.agent.server.exception.AgentServerException
     *          if the agent server cannot be started
     */
    public void start(String hostName)
            throws AgentServerException {
        startSecureEventTransmission(hostName,agentServerConfiguration.getSecureEventReceiverPort(),eventDispatcher);
        startEventTransmission(agentServerConfiguration.getEventReceiverPort(), eventDispatcher);
    }

    protected abstract void startEventTransmission(int eventReceiverPort,
                                                   EventDispatcher eventDispatcher)
            throws AgentServerException;

    private void startSecureEventTransmission(String hostName, int port,
                                              EventDispatcher eventDispatcher) throws AgentServerException {
        try {

            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                keyStore = System.getProperty("Security.KeyStore.Location");
                if (keyStore == null) {
                    throw new AgentServerException("Cannot start agent server, not valid Security.KeyStore.Location is null");
                }
            }
            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("Security.KeyStore.Password");
                if (keyStorePassword == null) {
                    throw new AgentServerException("Cannot start agent server, not valid Security.KeyStore.Password is null ");
                }
            }

            startSecureEventTransmission(hostName,port, keyStore, keyStorePassword,eventDispatcher);
        } catch (TransportException e) {
            throw new AgentServerException("Cannot start agent server on port " + port, e);
        } catch (UnknownHostException e) {
            //ignore since localhost
        }
    }

    protected abstract void startSecureEventTransmission(String hostName, int port, String keyStore,
                                                         String keyStorePassword,
                                                         EventDispatcher eventDispatcher)
            throws AgentServerException, TransportException, UnknownHostException;


    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }


    public abstract void stop();
}

