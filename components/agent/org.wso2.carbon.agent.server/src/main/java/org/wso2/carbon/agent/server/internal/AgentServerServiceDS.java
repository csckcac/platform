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

package org.wso2.carbon.agent.server.internal;

import org.wso2.carbon.agent.server.AgentServerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.server.AgentServer;
import org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.AgentServerConfigurationException;
import org.wso2.carbon.agent.server.exception.AgentServerException;
import org.wso2.carbon.agent.server.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.agent.server.internal.utils.AgentServerBuilder;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.identity.authentication.AuthenticationService;

/**
 * @scr.component name="agentserverservice.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class AgentServerServiceDS {
    private static final Log log = LogFactory.getLog(AgentServerServiceDS.class);
    private AuthenticationService authenticationService;
    private CarbonAgentServer carbonAgentServer;
    private ServiceRegistration agentServerService;
    private ServerConfigurationService serverConfiguration;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            carbonAgentServer = new AgentServerFactory().createAgentServer(
                    AgentServerBuilder.loadAgentServerConfiguration(serverConfiguration),
                    new CarbonAuthenticationHandler(authenticationService),new InMemoryStreamDefinitionStore());
            carbonAgentServer.start();
            agentServerService = context.getBundleContext().
                    registerService(AgentServer.class.getName(), carbonAgentServer, null);
            log.info("Successfully deployed Agent Server ");
        } catch (AgentServerConfigurationException e) {
            log.error("Agent Server Configuration is not correct hence can not create and start Agent Server ", e);
        } catch (AgentServerException e) {
            log.error("Can not create and start Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(agentServerService.getReference());
        carbonAgentServer.stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        this.serverConfiguration = null;
    }


}
