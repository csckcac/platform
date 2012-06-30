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

package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.internal.utils.ThriftDataReceiverBuilder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @scr.component name="thriftdatareceiver.component" immediate="true"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="databridge.core"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1" policy="dynamic" bind="setDataBridgeReceiverService" unbind="unsetDatabridgeReceiverService"
 *
 */
public class ThriftDataReceiverDS {
    private static final Log log = LogFactory.getLog(ThriftDataReceiverDS.class);
    private DataBridgeReceiverService dataBridgeReceiverService;
    private ServerConfigurationService serverConfiguration;
    private ConfigurationContextService configurationContext;
    private ThriftDataReceiver dataReceiver;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            int portOffset = ThriftDataReceiverBuilder.readPortOffset(serverConfiguration);
            ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(CommonThriftConstants.DEFAULT_RECEIVER_PORT + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET + portOffset, CommonThriftConstants.DEFAULT_RECEIVER_PORT + portOffset);
            ThriftDataReceiverBuilder.populateConfigurations(portOffset, thriftDataReceiverConfiguration, dataBridgeReceiverService.getInitialConfig());

            if (dataReceiver == null) {

                dataReceiver = new ThriftDataReceiverFactory().createAgentServer(thriftDataReceiverConfiguration, dataBridgeReceiverService);
                String serverUrl = CarbonUtils.getServerURL(serverConfiguration, configurationContext.getServerConfigContext());
                String hostName = null;
                try {
                    hostName = new URL(serverUrl).getHost();
                } catch (MalformedURLException e) {
                    log.warn("The server url :" + serverUrl + " is malformed URL hence hostname is assigned as 'localhost'");
                    hostName = "localhost";
                }
                dataReceiver.start(hostName);
                log.info("Successfully deployed Agent Server ");
            }
        } catch (DataBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        dataReceiver.stop();
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

    protected void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    protected void unsetDatabridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService =null;
    }

}
