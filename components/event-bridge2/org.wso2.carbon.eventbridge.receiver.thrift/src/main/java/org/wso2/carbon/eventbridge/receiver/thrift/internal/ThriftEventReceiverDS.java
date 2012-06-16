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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.eventbridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.eventbridge.core.EventBridgeReceiverService;
import org.wso2.carbon.eventbridge.core.exception.EventBridgeException;
import org.wso2.carbon.eventbridge.receiver.thrift.ThriftEventReceiverFactory;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftEventReceiverConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.utils.ThriftEventReceiverBuilder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @scr.component name="thrifteventreceiver.component" immediate="true"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */
public class ThriftEventReceiverDS {
    private static final Log log = LogFactory.getLog(ThriftEventReceiverDS.class);
    private EventBridgeReceiverService eventBridgeReceiverService;
    private ServerConfigurationService serverConfiguration;
    private ConfigurationContextService configurationContext;
    private ThriftEventReceiver eventReceiver;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            int portOffset = ThriftEventReceiverBuilder.readPortOffset(serverConfiguration);
            ThriftEventReceiverConfiguration thriftEventReceiverConfiguration = new ThriftEventReceiverConfiguration(CommonThriftConstants.DEFAULT_RECEIVER_PORT + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET + portOffset, CommonThriftConstants.DEFAULT_RECEIVER_PORT + portOffset);
            ThriftEventReceiverBuilder.populateConfigurations(portOffset, thriftEventReceiverConfiguration, eventBridgeReceiverService.getInitialConfig());

            if (eventReceiver == null) {

                eventReceiver = new ThriftEventReceiverFactory().createAgentServer(thriftEventReceiverConfiguration, eventBridgeReceiverService);
                String serverUrl = CarbonUtils.getServerURL(serverConfiguration, configurationContext.getServerConfigContext());
                String hostName = null;
                try {
                    hostName = new URL(serverUrl).getHost();
                } catch (MalformedURLException e) {
                    log.warn("The server url :" + serverUrl + " is malformed URL hence hostname is assigned as 'localhost'");
                    hostName = "localhost";
                }
                eventReceiver.start(hostName);
                log.info("Successfully deployed Agent Server ");
            }
        } catch (EventBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        eventReceiver.stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
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
