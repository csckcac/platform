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
import org.wso2.carbon.eventbridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.eventbridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.utils.AgentConstants;
import org.wso2.carbon.agent.server.EventBridgeReceiverService;
import org.wso2.carbon.agent.server.exception.EventBridgeException;
import org.wso2.carbon.agent.server.internal.utils.EventBridgeConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftEventReceiverConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.service.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.service.ThriftSecureEventTransmissionServiceImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Carbon based implementation of the agent server
 */
public class ThriftEventReceiver {
    private static final Log log = LogFactory.getLog(ThriftEventReceiver.class);
    private EventBridgeReceiverService eventBridgeReceiverService;
    private ThriftEventReceiverConfiguration thriftEventReceiverConfiguration;
    private TServer authenticationServer;
    private TServer eventReceiverServer;

    /**
     * Initialize Carbon Agent Server
     *
     * @param secureReceiverPort
     * @param receiverPort
     * @param eventBridgeReceiverService
     */
    public ThriftEventReceiver(int secureReceiverPort, int receiverPort,
                               EventBridgeReceiverService eventBridgeReceiverService) {
        this.eventBridgeReceiverService = eventBridgeReceiverService;
        this.thriftEventReceiverConfiguration = new ThriftEventReceiverConfiguration(secureReceiverPort, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param receiverPort
     * @param eventBridgeReceiverService
     */
    public ThriftEventReceiver(int receiverPort,
                               EventBridgeReceiverService eventBridgeReceiverService) {
        this.eventBridgeReceiverService = eventBridgeReceiverService;
        this.thriftEventReceiverConfiguration = new ThriftEventReceiverConfiguration(receiverPort + AgentConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param thriftEventReceiverConfiguration
     * @param eventBridgeReceiverService
     */
    public ThriftEventReceiver(ThriftEventReceiverConfiguration thriftEventReceiverConfiguration,
                               EventBridgeReceiverService eventBridgeReceiverService) {
        this.eventBridgeReceiverService = eventBridgeReceiverService;
        this.thriftEventReceiverConfiguration = thriftEventReceiverConfiguration;
    }

    /**
     * To start the Agent server
     *
     * @throws org.wso2.carbon.agent.server.exception.EventBridgeException
     *          if the agent server cannot be started
     */
    public void start(String hostName)
            throws EventBridgeException {
        startSecureEventTransmission(hostName, thriftEventReceiverConfiguration.getSecureEventReceiverPort(), eventBridgeReceiverService);
        startEventTransmission(thriftEventReceiverConfiguration.getEventReceiverPort(), eventBridgeReceiverService);
    }


    private void startSecureEventTransmission(String hostName, int port,
                                              EventBridgeReceiverService eventBridgeReceiverService)
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

            startSecureEventTransmission(hostName, port, keyStore, keyStorePassword, eventBridgeReceiverService);
        } catch (TransportException e) {
            throw new EventBridgeException("Cannot start agent server on port " + port, e);
        } catch (UnknownHostException e) {
            //ignore since localhost
        }
    }

    protected void startSecureEventTransmission(String hostName, int port, String keyStore,
                                                String keyStorePassword,
                                                EventBridgeReceiverService eventBridgeReceiverService)
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
                        new ThriftSecureEventTransmissionServiceImpl(eventBridgeReceiverService));
        authenticationServer = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverTransport).processor(processor));
        Thread thread = new Thread(new ServerThread(authenticationServer));
        log.info("Thrift SSL port : " + port);
        thread.start();
    }

    protected void startEventTransmission(int port, EventBridgeReceiverService eventBridgeReceiverService)
            throws EventBridgeException {
        try {
            TServerSocket serverTransport = new TServerSocket(port);
            ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl> processor =
                    new ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl>(
                            new ThriftEventTransmissionServiceImpl(eventBridgeReceiverService));
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

