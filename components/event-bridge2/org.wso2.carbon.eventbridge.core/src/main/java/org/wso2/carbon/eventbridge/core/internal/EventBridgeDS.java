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

package org.wso2.carbon.eventbridge.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.eventbridge.core.EventBridge;
import org.wso2.carbon.eventbridge.core.EventBridgeReceiverService;
import org.wso2.carbon.eventbridge.core.EventBridgeSubscriberService;
import org.wso2.carbon.eventbridge.core.conf.EventBridgeConfiguration;
import org.wso2.carbon.eventbridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.exception.EventBridgeConfigurationException;
import org.wso2.carbon.eventbridge.core.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.eventbridge.core.internal.utils.EventBridgeCoreBuilder;
import org.wso2.carbon.identity.authentication.AuthenticationService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="eventbridge.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 */
public class EventBridgeDS {
    private static final Log log = LogFactory.getLog(EventBridgeDS.class);
    private AuthenticationService authenticationService;
    private ServiceRegistration receiverServiceRegistration;
    private ServiceRegistration subscriberServiceRegistration;
    private EventBridge eventBridge;
    private OMElement initialConfig;
    private ServiceRegistration eventBridgeRegistration;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            EventBridgeConfiguration eventBridgeConfiguration = new EventBridgeConfiguration();
            List<String[]> eventStreamDefinitions = new ArrayList<String[]>();
            initialConfig = EventBridgeCoreBuilder.loadConfigXML();
            EventBridgeCoreBuilder.populateConfigurations(eventBridgeConfiguration, eventStreamDefinitions, initialConfig);

            if (eventBridge == null) {
                String definitionStoreName = eventBridgeConfiguration.getStreamDefinitionStoreName();
                AbstractStreamDefinitionStore streamDefinitionStore = null;
                try {
                    streamDefinitionStore = (AbstractStreamDefinitionStore) EventBridgeDS.class.getClassLoader().loadClass(definitionStoreName).newInstance();
//                    streamDefinitionStore = (AbstractStreamDefinitionStore) Class.forName(definitionStoreName).newInstance();
                } catch (Exception e) {
                    log.warn("The stream definition store :" + definitionStoreName + " is cannot be created hence using org.wso2.carbon.agent.server.definitionstore.InMemoryStreamDefinitionStore", e);
                    //by default if used InMemoryStreamDefinitionStore
                    streamDefinitionStore = new InMemoryStreamDefinitionStore();
                }


                eventBridge = new EventBridge(new CarbonAuthenticationHandler(authenticationService), streamDefinitionStore, eventBridgeConfiguration);
                eventBridge.setInitialConfig(initialConfig);

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
                receiverServiceRegistration = context.getBundleContext().
                        registerService(EventBridgeReceiverService.class.getName(), eventBridge, null);
                subscriberServiceRegistration = context.getBundleContext().
                        registerService(EventBridgeSubscriberService.class.getName(), eventBridge, null);
//                eventBridgeRegistration =
//                        context.getBundleContext().registerService(EventBridge.class.getName(), eventBridge, null);
                log.info("Successfully deployed Agent Server ");
            }
        } catch (EventBridgeConfigurationException e) {
            log.error("Agent Server Configuration is not correct hence can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(receiverServiceRegistration.getReference());
        context.getBundleContext().ungetService(subscriberServiceRegistration.getReference());
//        eventBridgeRegistration.unregister();
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

}
