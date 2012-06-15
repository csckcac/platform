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
package org.wso2.carbon.eventbridge.receiver.thrift.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.server.conf.EventBridgeCoreConfiguration;
import org.wso2.carbon.agent.server.exception.EventBridgeConfigurationException;
import org.wso2.carbon.agent.server.internal.utils.EventBridgeCoreBuilder;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftReceiverConfiguration;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Helper class to build Agent Server Initial Configurations
 */
public final class ThriftReceiverBuilder {

    private static final Log log = LogFactory.getLog(ThriftReceiverBuilder.class);

    private ThriftReceiverBuilder() {}



    private static void populatePorts(OMElement agentServerConfig,
                                      ServerConfigurationService serverConfiguration,
                                      ThriftReceiverConfiguration thriftReceiverConfiguration) {
        int portOffSet = readPortOffset(serverConfiguration);
        OMElement secureEventReceiverPort = agentServerConfig.getFirstChildWithName(
                new QName(EventBridgeConstants.AGENT_SERVER_CONF_NAMESPACE,
                          EventBridgeConstants.SECURE_EVENT_RECEIVER_PORT));
        if (secureEventReceiverPort != null) {
            thriftReceiverConfiguration.setSecureEventReceiverPort(Integer.parseInt(secureEventReceiverPort.getText()) + portOffSet);
        }
        OMElement receiverPort = agentServerConfig.getFirstChildWithName(
                new QName(EventBridgeConstants.AGENT_SERVER_CONF_NAMESPACE,
                          EventBridgeConstants.EVENT_RECEIVER_PORT));
        if (receiverPort != null) {
            thriftReceiverConfiguration.setEventReceiverPort(Integer.parseInt(receiverPort.getText()) + portOffSet);
        }
    }


    private static int readPortOffset(ServerConfigurationService serverConfiguration) {

        String portOffset = serverConfiguration.getFirstProperty(EventBridgeConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : EventBridgeConstants.CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return EventBridgeConstants.CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    public static void populateConfigurations(ServerConfigurationService serverConfiguration,
                                              ThriftReceiverConfiguration thriftReceiverConfiguration,
                                              EventBridgeCoreConfiguration eventBridgeCoreConfiguration,
                                              List<String[]> eventStreamDefinitions)
            throws EventBridgeConfigurationException {

        OMElement agentServerConfig = EventBridgeCoreBuilder.loadConfigXML();
        if (agentServerConfig != null) {
            if (!agentServerConfig.getQName().equals(
                    new QName(EventBridgeConstants.AGENT_SERVER_CONF_NAMESPACE, EventBridgeConstants.AGENT_SERVER_CONF_ELE_ROOT))) {
                throw new EventBridgeConfigurationException("Invalid root element in agent server config");
            }
            populatePorts(agentServerConfig, serverConfiguration,thriftReceiverConfiguration);
            EventBridgeCoreBuilder.populateEventStreamDefinitions(agentServerConfig, eventStreamDefinitions);
            EventBridgeCoreBuilder.populateStreamDefinitionStore(agentServerConfig, eventBridgeCoreConfiguration);
        }
    }
}
