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
import org.wso2.carbon.agent.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.agent.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.agent.server.internal.service.general.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.agent.server.internal.service.secure.ThriftSecureEventTransmissionServiceImpl;
import org.wso2.carbon.agent.server.internal.utils.AgentServerConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Carbon based implementation of the agent server
 */
public class ThriftAgentServer extends AbstractAgentServer {
    private static Log log = LogFactory.getLog(ThriftAgentServer.class);
    private TServer authenticationServer;
    private TServer eventReceiverServer;

    public ThriftAgentServer(int secureReceiverPort, int receiverPort,
                             AuthenticationHandler authenticationHandler,
                             StreamDefinitionStore streamDefinitionStore) {
        super(secureReceiverPort, receiverPort, authenticationHandler, streamDefinitionStore);
    }

    public ThriftAgentServer(int receiverPort, AuthenticationHandler authenticationHandler,
                             StreamDefinitionStore streamDefinitionStore) {
        super(receiverPort, authenticationHandler, streamDefinitionStore);
    }

    public ThriftAgentServer(AgentServerConfiguration agentServerConfiguration,
                             AuthenticationHandler authenticationHandler,
                             StreamDefinitionStore streamDefinitionStore) {
        super(agentServerConfiguration, authenticationHandler, streamDefinitionStore);
    }

    @Override
    protected void startSecureEventTransmission(int port, String keyStore, String keyStorePassword,
                                                EventDispatcher eventDispatcher)
            throws TransportException, UnknownHostException {
        TSSLTransportFactory.TSSLTransportParameters params =
                new TSSLTransportFactory.TSSLTransportParameters();
        params.setKeyStore(keyStore, keyStorePassword);

        TServerSocket serverTransport = null;
        try {
            serverTransport = TSSLTransportFactory.getServerSocket(
                    port, AgentServerConstants.CLIENT_TIMEOUT_MS, InetAddress.getByName("localhost"), params);
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
            throws AgentServerException {
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

