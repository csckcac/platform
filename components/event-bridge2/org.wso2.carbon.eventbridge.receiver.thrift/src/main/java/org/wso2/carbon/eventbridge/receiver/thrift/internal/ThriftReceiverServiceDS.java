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

package org.wso2.carbon.eventbridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.internal.utils.AgentConstants;
import org.wso2.carbon.agent.server.EventReceiver;
import org.wso2.carbon.agent.server.conf.EventBridgeCoreConfiguration;
import org.wso2.carbon.agent.server.datastore.AbstractStreamDefinitionStore;
import org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.EventBridgeConfigurationException;
import org.wso2.carbon.agent.server.exception.EventBridgeException;
import org.wso2.carbon.agent.server.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.eventbridge.receiver.thrift.ThriftReceiverFactory;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftReceiverConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.utils.ThriftReceiverBuilder;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="agentserverservice.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */
public class ThriftReceiverServiceDS {
    private static final Log log = LogFactory.getLog(ThriftReceiverServiceDS.class);
    private AuthenticationService authenticationService;
    private ThriftEventReceiver eventReceiver;
    private ServiceRegistration agentServerService;
    private ServerConfigurationService serverConfiguration;
    private ConfigurationContextService configurationContext;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            ThriftReceiverConfiguration thriftReceiverConfiguration = new ThriftReceiverConfiguration(AgentConstants.DEFAULT_RECEIVER_PORT + AgentConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, AgentConstants.DEFAULT_RECEIVER_PORT);
            EventBridgeCoreConfiguration eventBridgeCoreConfiguration = new EventBridgeCoreConfiguration();
            List<String[]> eventStreamDefinitions = new ArrayList<String[]>();
            ThriftReceiverBuilder.populateConfigurations(serverConfiguration, thriftReceiverConfiguration, eventBridgeCoreConfiguration, eventStreamDefinitions);

            if (eventReceiver == null) {
                String definitionStoreName = eventBridgeCoreConfiguration.getStreamDefinitionStoreName();
                AbstractStreamDefinitionStore streamDefinitionStore = null;
                try {
                    streamDefinitionStore = (AbstractStreamDefinitionStore) Class.forName(definitionStoreName).newInstance();
                } catch (Exception e) {
                    log.warn("The stream definition store :" + definitionStoreName + " is cannot be created hence using org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore", e);
                    //by default if used InMemoryStreamDefinitionStore
                    streamDefinitionStore = new InMemoryStreamDefinitionStore();
                }

                eventReceiver = new ThriftReceiverFactory().createAgentServer(
                        thriftReceiverConfiguration,
                        new CarbonAuthenticationHandler(authenticationService), streamDefinitionStore);
                String serverUrl = CarbonUtils.getServerURL(serverConfiguration, configurationContext.getServerConfigContext());
                String hostName = null;
                try {
                    hostName = new URL(serverUrl).getHost();
                } catch (MalformedURLException e) {
                    log.warn("The server url :" + serverUrl + " is malformed URL hence hostname is assigned as 'localhost'");
                    hostName = "localhost";
                }
                eventReceiver.start(hostName);
                //todo
//                for (String[] streamDefinition : eventStreamDefinitions) {
//                    try {
//                        eventReceiver.saveEventStreamDefinition(streamDefinition[0], streamDefinition[1]);
//                    } catch (MalformedStreamDefinitionException e) {
//                        log.error("Malformed Stream Definition for " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
//                        log.warn("Redefining event stream of " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    } catch (RuntimeException e) {
//                        log.error("Error in defining event stream " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    }
//                }
                agentServerService = context.getBundleContext().
                        registerService(EventReceiver.class.getName(), eventReceiver, null);
                log.info("Successfully deployed Agent Server ");
            }
        } catch (EventBridgeConfigurationException e) {
            log.error("Agent Server Configuration is not correct hence can not create and start Agent Server ", e);
        } catch (EventBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(agentServerService.getReference());
        eventReceiver.stop();
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

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = configurationContext;
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
        this.configurationContext = null;
    }


}
