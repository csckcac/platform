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
package org.wso2.carbon.eventbridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.agent.commons.Credentials;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.agent.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.commons.utils.EventDefinitionConverter;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.utils.AgentConstants;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.EventReceiver;
import org.wso2.carbon.agent.server.datastore.AbstractStreamDefinitionStore;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.EventBridgeException;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.EventDispatcher;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.utils.EventBridgeConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftReceiverConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.service.general.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.service.secure.ThriftSecureEventTransmissionServiceImpl;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.converter.ThriftEventConverter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Carbon based implementation of the agent server
 */
public class ThriftEventReceiver implements EventReceiver {
    private static final Log log = LogFactory.getLog(ThriftEventReceiver.class);
    private EventDispatcher eventDispatcher;
    private ThriftReceiverConfiguration agentServerConfiguration;
    private StreamDefinitionStore streamDefinitionStore;

    private TServer authenticationServer;
    private TServer eventReceiverServer;


    /**
     * Initialize Carbon Agent Server
     *
     * @param secureReceiverPort
     * @param receiverPort
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public ThriftEventReceiver(int secureReceiverPort, int receiverPort,
                               AuthenticationHandler authenticationHandler,
                               AbstractStreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, new ThriftEventConverter());
        this.agentServerConfiguration = new ThriftReceiverConfiguration(secureReceiverPort, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param receiverPort
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public ThriftEventReceiver(int receiverPort,
                               AuthenticationHandler authenticationHandler,
                               AbstractStreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, new ThriftEventConverter());
        this.agentServerConfiguration = new ThriftReceiverConfiguration(receiverPort + AgentConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param agentServerConfiguration
     * @param authenticationHandler
     * @param streamDefinitionStore
     */
    public ThriftEventReceiver(ThriftReceiverConfiguration agentServerConfiguration,
                               AuthenticationHandler authenticationHandler,
                               AbstractStreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, new ThriftEventConverter());
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
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName,
                                                     String streamVersion)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamName, streamVersion);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamDefinition(credentials, streamId);
    }

    @Override
    public List<EventStreamDefinition> getAllStreamDefinition(Credentials credentials)
            throws StreamDefinitionNotFoundException {
        return new ArrayList<EventStreamDefinition>(streamDefinitionStore.getAllStreamDefinitions(credentials));
    }

    @Override
    public void saveEventStreamDefinition(Credentials credentials, String eventStreamDefinition)
            throws MalformedStreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        streamDefinitionStore.saveStreamDefinition(credentials, EventDefinitionConverter.convertFromJson(eventStreamDefinition));
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException {
        return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
    }

    /**
     * To start the Agent server
     *
     * @throws org.wso2.carbon.agent.server.exception.EventBridgeException
     *          if the agent server cannot be started
     */
    public void start(String hostName)
            throws EventBridgeException {
        startSecureEventTransmission(hostName, agentServerConfiguration.getSecureEventReceiverPort(), eventDispatcher);
        startEventTransmission(agentServerConfiguration.getEventReceiverPort(), eventDispatcher);
    }


    private void startSecureEventTransmission(String hostName, int port,
                                              EventDispatcher eventDispatcher)
            throws EventBridgeException {
        try {

            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                keyStore = System.getProperty("Security.KeyStore.Location");
                if (keyStore == null) {
                    throw new EventBridgeException("Cannot start agent server, not valid Security.KeyStore.Location is null");
                }
            }
            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("Security.KeyStore.Password");
                if (keyStorePassword == null) {
                    throw new EventBridgeException("Cannot start agent server, not valid Security.KeyStore.Password is null ");
                }
            }

            startSecureEventTransmission(hostName, port, keyStore, keyStorePassword, eventDispatcher);
        } catch (TransportException e) {
            throw new EventBridgeException("Cannot start agent server on port " + port, e);
        } catch (UnknownHostException e) {
            //ignore since localhost
        }
    }


    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }


    protected void startSecureEventTransmission(String hostName, int port, String keyStore,
                                                String keyStorePassword,
                                                EventDispatcher eventDispatcher)
            throws TransportException, UnknownHostException {
        TSSLTransportFactory.TSSLTransportParameters params =
                new TSSLTransportFactory.TSSLTransportParameters();
        params.setKeyStore(keyStore, keyStorePassword);

        TServerSocket serverTransport;
        try {
            serverTransport = TSSLTransportFactory.getServerSocket(
                    port, EventBridgeConstants.CLIENT_TIMEOUT_MS, InetAddress.getByName(hostName), params);
        } catch (TTransportException e) {
            throw new TransportException("Thrift transport exception occurred ", e);
        }

        ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl> processor =
                new ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl>(
                        new ThriftSecureEventTransmissionServiceImpl(eventDispatcher));
        authenticationServer = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverTransport).processor(processor));
        Thread thread = new Thread(new ServerThread(authenticationServer));
        log.info("Thrift SSL port : " + port);
        thread.start();
    }

    protected void startEventTransmission(int port, EventDispatcher eventDispatcher)
            throws EventBridgeException {
        try {
            TServerSocket serverTransport = new TServerSocket(port);
            ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl> processor =
                    new ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl>(
                            new ThriftEventTransmissionServiceImpl(eventDispatcher));
            eventReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(eventReceiverServer));
            log.info("Thrift port : " + port);
            thread.start();
        } catch (TTransportException e) {
            throw new EventBridgeException("Cannot start Thrift server on port " + port, e);
        }
    }

    /**
     * To stop the server
     */
    public void stop() {
        authenticationServer.stop();
        eventReceiverServer.stop();
    }

    static class ServerThread implements Runnable {
        private TServer server;

        ServerThread(TServer server) {
            this.server = server;
        }

        public void run() {
            this.server.serve();
        }
    }
}

