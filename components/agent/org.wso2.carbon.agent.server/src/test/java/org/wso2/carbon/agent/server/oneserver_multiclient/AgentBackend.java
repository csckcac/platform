/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.agent.server.oneserver_multiclient;

import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.KeyStoreUtil;
import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.internal.CarbonAgentServer;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;

import java.util.List;


/**
 * Server of multiple client single server test
 */
public class AgentBackend {
    CarbonAgentServer carbonAgentServer;
    static int NO_OF_EVENTS = 100000;
    static int STABLE = 1000000;
    int offset = 0;

    public static void main(String[] args)
            throws AgentServerException, InterruptedException {

        if (0 != args.length && args[0] != null) {
            NO_OF_EVENTS = Integer.parseInt(args[0]);
        }

        System.out.println("Event no=" + NO_OF_EVENTS);

        KeyStoreUtil.setKeyStoreParams();

        AgentBackend server = new AgentBackend(0);
        server.start();
    }

    public AgentBackend(int offset) {
        this.offset = offset;
    }

    public void start() throws AgentServerException, InterruptedException {

        AgentServerConfiguration agentServerConfiguration = generateServerConf(offset);
        carbonAgentServer = new CarbonAgentServer(agentServerConfiguration,new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName, String password) {
                return true;// allays authenticate to true
            }
        },new InMemoryStreamDefinitionStore());
        carbonAgentServer.subscribe(assignAgentCallback());
        carbonAgentServer.start();

    }

    private AgentCallback assignAgentCallback() {

        return new AgentCallback() {
            long startTime = -1;
            int size = 0;
            private EventStreamDefinition eventStreamDefinition;

            public void definedEventStream(EventStreamDefinition eventStreamDefinition,
                                           String sessionId) {
                this.eventStreamDefinition = eventStreamDefinition;//not used here
            }

            public void receive(List<Event> eventList, String sessionId) {
                addCount(eventList);
                if (size <= STABLE && size > STABLE - 200) {
                    startTime = System.currentTimeMillis();
                }
                if (NO_OF_EVENTS <= (size - STABLE)) {
                    if (startTime != -1) {
                        System.out.println("Total time in ms=" + (System.currentTimeMillis() - startTime));
                    } else {
                        System.out.println("Start time not set ");
                    }
                }
            }

            private synchronized void addCount(List<Event> eventList) {
                size += eventList.size();
            }
        };
    }

    private AgentServerConfiguration generateServerConf(int offset) {
        AgentServerConfiguration agentServerConfiguration = new AgentServerConfiguration(7711 + offset,7611 + offset);
        return agentServerConfiguration;
    }

    public void stop() {
        carbonAgentServer.stop();
    }
}
