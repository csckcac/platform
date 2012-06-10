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
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.internal.AbstractAgentServer;
import org.wso2.carbon.agent.server.internal.ThriftAgentServer;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;

import java.util.List;

public class TestServer extends TestCase {
    Logger log=Logger.getLogger(TestServer.class);
    AbstractAgentServer agentServer;

    public void testServerTest() throws AgentServerException, InterruptedException {
        TestServer testServer = new TestServer();
        testServer.start(7611);
        Thread.sleep(1000);
        testServer.stop();
    }

    public void start(int receiverPort) throws AgentServerException {
        KeyStoreUtil.setKeyStoreParams();

        agentServer = new ThriftAgentServer(receiverPort, new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName, String password) {
                return true;// allays authenticate to true
            }
        },new InMemoryStreamDefinitionStore());

        agentServer.subscribe(new AgentCallback() {
            int totalSize = 0;

            public void definedEventStream(EventStreamDefinition eventStreamDefinition,
                                           String userName, String password, String domainName) {
                log.info("EventStreamDefinition " + userName);
            }

            @Override
            public void receive(List<Event> eventList, String userName, String password,
                                String domainName) {
                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " + userName);
            }

        });
        agentServer.start("localhost");
        log.info("Test Server Started");
    }

    public void stop() {
        agentServer.stop();
        log.info("Test Server Stopped");
    }
}
