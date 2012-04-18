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
package org.wso2.carbon.agent.server.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.internal.utils.AgentConstants;
import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.exception.AgentServerConfigurationException;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Helper class to build Agent Server
 */
public final class AgentServerBuilder {

    private static final Log log = LogFactory.getLog(AgentServerBuilder.class);

    private AgentServerBuilder() {

    }

    /**
     * Helper method to load the agent server config
     *
     * @param serverConfiguration
     * @return Agent Server Configuration
     * @throws org.wso2.carbon.agent.server.exception.AgentServerConfigurationException
     *
     */
    public static AgentServerConfiguration loadAgentServerConfiguration(
            ServerConfigurationService serverConfiguration)
            throws AgentServerConfigurationException {

        OMElement agentServerConfig = loadConfigXML();
        if (agentServerConfig != null) {
            if (!agentServerConfig.getQName().equals(
                    new QName(AgentServerConstants.AGENT_SERVER_CONF_NAMESPACE, AgentServerConstants.AGENT_SERVER_CONF_ELE_ROOT))) {
                throw new AgentServerConfigurationException("Invalid root element in agent server config");
            }
            return buildAgentServerConfiguration(agentServerConfig, serverConfiguration);
        }
        throw new AgentServerConfigurationException("Invalid agent server config");

    }

    private static OMElement loadConfigXML() throws AgentServerConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + AgentServerConstants.AGENT_SERVER_CONF;

        // if the agent server config file not exists then simply return null.
        File agentServerConfigFile = new File(path);
        if (!agentServerConfigFile.exists()) {
            return null;
        }

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = AgentServerConstants.AGENT_SERVER_CONF
                                  + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new AgentServerConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + AgentServerConstants.AGENT_SERVER_CONF
                                  + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new AgentServerConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
                throw new AgentServerConfigurationException(errorMessage, e);
            }
        }
    }

    private static AgentServerConfiguration buildAgentServerConfiguration(
            OMElement agentServerConfig, ServerConfigurationService serverConfiguration) {
        int portOffSet = readPortOffset(serverConfiguration);
        AgentServerConfiguration agentServerConfiguration = new AgentServerConfiguration(AgentConstants.DEFAULT_RECEIVER_PORT+AgentConstants.AUTHENTICATOR_PORT_OFFSET, AgentConstants.DEFAULT_RECEIVER_PORT);
        OMElement authenticatorPort = agentServerConfig.getFirstChildWithName(
                new QName(AgentServerConstants.AGENT_SERVER_CONF_NAMESPACE,
                          AgentServerConstants.AUTHENTICATOR_PORT));
        if (authenticatorPort != null) {
            agentServerConfiguration.setAuthenticatorPort(Integer.parseInt(authenticatorPort.getText()) + portOffSet);
        }
        OMElement receiverPort = agentServerConfig.getFirstChildWithName(
                new QName(AgentServerConstants.AGENT_SERVER_CONF_NAMESPACE,
                          AgentServerConstants.EVENT_RECEIVER_PORT));
        if (receiverPort != null) {
            agentServerConfiguration.setEventReceiverPort(Integer.parseInt(receiverPort.getText()) + portOffSet);
        }
        return agentServerConfiguration;
    }

    private static int readPortOffset(ServerConfigurationService serverConfiguration) {

        String portOffset = serverConfiguration.getFirstProperty(AgentServerConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : AgentServerConstants.CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return AgentServerConstants.CARBON_DEFAULT_PORT_OFFSET;
        }
    }

}
