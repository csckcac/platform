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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.broker.core.BrokerService;
import org.wso2.carbon.broker.core.exception.BrokerConfigException;
import org.wso2.carbon.broker.core.internal.builder.BrokerServiceBuilder;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="brokerservice.component" immediate="true"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="agentserverservice.service"
 * interface="org.wso2.carbon.agent.server.AgentServer" cardinality="1..1"
 * policy="dynamic" bind="setAgentServer" unbind="unSetAgentServer"
 * @scr.reference name="agentservice.service"
 * interface="org.wso2.carbon.agent.Agent" cardinality="1..1"
 * policy="dynamic" bind="setAgent" unbind="unSetAgent"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setEventBrokerService" unbind="unSetEventBrokerService"
 */
public class BrokerServiceDS {

    private static final Log log = LogFactory.getLog(BrokerServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            BrokerService brokerService = BrokerServiceBuilder.createBrokerService();
            context.getBundleContext().registerService(BrokerService.class.getName(),
                                                       brokerService, null);
            log.info("Successfully deployed the broker service");
        } catch (BrokerConfigException e) {
            log.error("Can not create the broker service ", e);
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        BrokerServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

    }

    protected void setEventBrokerService(EventBroker eventBroker) {
        BrokerServiceValueHolder.registerEventBrokerService(eventBroker);
    }

    protected void unSetEventBrokerService(EventBroker eventBroker) {

    }

    protected void setAgentServer(AgentServer agentServer) {
        BrokerServiceValueHolder.registerAgentServer(agentServer);
    }

    protected void unSetAgentServer(AgentServer agentServer) {

    }

    protected void setAgent(Agent agent) {
        BrokerServiceValueHolder.registerAgent(agent);
    }

    protected void unSetAgent(Agent agent) {

    }

}
