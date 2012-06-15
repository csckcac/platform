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
package org.wso2.carbon.agent.server;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.wso2.carbon.agent.commons.Credentials;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.EventBridgeException;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.ThriftEventReceiver;

import java.util.List;

public class TestServer extends TestCase {
    Logger log = Logger.getLogger(TestServer.class);
    ThriftEventReceiver thriftEventReceiver;

    public void testServerTest() throws EventBridgeException, InterruptedException {
        TestServer testServer = new TestServer();
        testServer.start(7611);
        Thread.sleep(1000);
        testServer.stop();
    }

    public void start(int receiverPort) throws EventBridgeException {
        KeyStoreUtil.setKeyStoreParams();
        EventBridge eventBridge = new EventBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;// allays authenticate to true

            }
        }, new InMemoryStreamDefinitionStore());

        thriftEventReceiver = new ThriftEventReceiver(receiverPort, eventBridge);

        eventBridge.subscribe(new AgentCallback() {
            int totalSize = 0;

            public void definedEventStream(EventStreamDefinition eventStreamDefinition,
                                           Credentials credentials) {
                log.info("EventStreamDefinition " + eventStreamDefinition);
            }

            @Override
            public void receive(List<Event> eventList, Credentials credentials) {
                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + credentials.getUsername());
            }

        });
        thriftEventReceiver.start("localhost");
        log.info("Test Server Started");
    }

    public void stop() {
        thriftEventReceiver.stop();
        log.info("Test Server Stopped");
    }
}
