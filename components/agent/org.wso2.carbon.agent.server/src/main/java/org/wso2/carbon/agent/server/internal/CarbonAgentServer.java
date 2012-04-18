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
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.thrift.authentication.service.ThriftAuthenticatorService;
import org.wso2.carbon.agent.commons.thrift.service.ThriftEventReceiverService;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.authentication.Authenticator;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.agent.server.internal.service.ThriftAuthenticatorServiceImpl;
import org.wso2.carbon.agent.server.internal.service.ThriftEventReceiverServiceImpl;
import org.wso2.carbon.agent.server.internal.utils.AgentServerConstants;
import org.wso2.carbon.base.ServerConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class CarbonAgentServer implements AgentServer {
    private static Log log = LogFactory.getLog(CarbonAgentServer.class);
    private TServer authenticationServer;
    private TServer eventReceiverServer;
    private EventDispatcher eventDispatcher;
    private AgentServerConfiguration agentServerConfiguration;
    private StreamDefinitionStore streamDefinitionStore;

    /**
     * Initialize Carbon Agent Server
     *
     * @param authenticatorPort
     * @param receiverPort
     * @param authenticationHandler
     */
    public CarbonAgentServer(int authenticatorPort, int receiverPort,
                             AuthenticationHandler authenticationHandler,
                             StreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.agentServerConfiguration = new AgentServerConfiguration(authenticatorPort, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param receiverPort
     * @param authenticationHandler
     */
    public CarbonAgentServer(int receiverPort,
                             AuthenticationHandler authenticationHandler,
                             StreamDefinitionStore streamDefinitionStore) {
        this.streamDefinitionStore = streamDefinitionStore;
        Authenticator.getInstance().init(authenticationHandler);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore);
        this.agentServerConfiguration = new AgentServerConfiguration(receiverPort + AgentServerConstants.AUTHENTICATOR_PORT_OFFSET, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param agentServerConfiguration
     * @param authenticationHandler
     */
    public CarbonAgentServer(AgentServerConfiguration agentServerConfiguration,
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
        return streamDefinitionStore.getStreamDefinition(domainName);
    }

    /**
     * To start the Agent server
     *
     * @throws AgentServerException if the agent server cannot be started
     */
    public void start()
            throws AgentServerException {
        startAgentAuthenticator(agentServerConfiguration.getAuthenticatorPort());
        startEventReceiver(agentServerConfiguration.getEventReceiverPort());
    }

    private void startAgentAuthenticator(int port) throws AgentServerException {
        try {

            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                keyStore = System.getProperty("Security.KeyStore.Location");
                if (keyStore == null) {
                    throw new AgentServerException("Cannot start Thrift server, not valid Security.KeyStore.Location is null");
                }
            }
            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("Security.KeyStore.Password");
                if (keyStorePassword == null) {
                    throw new AgentServerException("Cannot start Thrift server, not valid Security.KeyStore.Password is null ");
                }
            }

            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setKeyStore(keyStore, keyStorePassword);

            TServerSocket serverTransport = TSSLTransportFactory.getServerSocket(
                    port, AgentServerConstants.THRIFT_CLIENT_TIMEOUT, InetAddress.getByName("localhost"), params);

            ThriftAuthenticatorService.Processor<ThriftAuthenticatorServiceImpl> processor =
                    new ThriftAuthenticatorService.Processor<ThriftAuthenticatorServiceImpl>(new ThriftAuthenticatorServiceImpl());
            authenticationServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(authenticationServer));
            log.info("Thrift Authenticator port : " + port);
            thread.start();
        } catch (TTransportException e) {
            throw new AgentServerException("Cannot start Thrift server on port " + port, e);
        } catch (UnknownHostException e) {
            //ignore since localhost
        }
    }

    private void startEventReceiver(int port)
            throws AgentServerException {
        try {
            TServerSocket serverTransport = new TServerSocket(port);
            ThriftEventReceiverService.Processor<ThriftEventReceiverServiceImpl> processor =
                    new ThriftEventReceiverService.Processor<ThriftEventReceiverServiceImpl>(
                            new ThriftEventReceiverServiceImpl(eventDispatcher));
            eventReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(eventReceiverServer));
            log.info("Thrift Server port : " + port);
            thread.start();
        } catch (TTransportException e) {
            throw new AgentServerException("Cannot start Thrift server on port " + port, e);
        }
    }

    /**
     * To stop the server
     */
    public void stop() {
        authenticationServer.stop();
        eventReceiverServer.stop();
    }

    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
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

