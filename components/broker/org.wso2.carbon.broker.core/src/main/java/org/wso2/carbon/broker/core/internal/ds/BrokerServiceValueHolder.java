/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.broker.core.internal.ds;

import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.event.core.EventBroker;

/**
 * common place to hold some OSGI bundle references.
 */
public final class BrokerServiceValueHolder {

    private static ConfigurationContextService configurationContextService;
    private static EventBroker eventBroker;
    private static AgentServer agentServer;
    private static Agent agent;

    private BrokerServiceValueHolder(){}

    public static void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        BrokerServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return BrokerServiceValueHolder.configurationContextService;
    }

    public static void registerEventBrokerService(EventBroker eventBroker) {
        BrokerServiceValueHolder.eventBroker = eventBroker;
    }

    public static EventBroker getEventBroker() {
        return BrokerServiceValueHolder.eventBroker;
    }

    public static void registerAgentServer(AgentServer agentServer) {
        BrokerServiceValueHolder.agentServer = agentServer;
    }

    public static AgentServer getAgentServer() {
        return agentServer;
    }

    public static void registerAgent(Agent agent) {
        BrokerServiceValueHolder.agent = agent;
    }

    public static Agent getAgent() {
        return agent;
    }
}
