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
package org.wso2.carbon.agent.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.conf.AgentConfiguration;
import org.wso2.carbon.agent.exception.AgentConfigurationException;
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
 * Helper class to build Agent's configurations
 */
public final class AgentBuilder {

    private static final Log log = LogFactory.getLog(AgentBuilder.class);

    private AgentBuilder() {

    }

    /**
     * Helper method to load the agent config
     *
     * @return the Agent configuration
     * @throws AgentConfigurationException
     */
    public static AgentConfiguration loadAgentConfiguration()
            throws AgentConfigurationException {

        OMElement agentConfig = loadConfigXML();
        if (agentConfig != null) {
            if (!agentConfig.getQName().equals(
                    new QName(AgentConstants.AGENT_CONF_NAMESPACE, AgentConstants.AGENT_CONF_ELE_ROOT))) {
                throw new AgentConfigurationException("Invalid root element in agent server config");
            }
            return buildAgentConfiguration(agentConfig);
        }
        throw new AgentConfigurationException("Invalid agent config");

    }

    /**
     * Helper method to load the agent config
     *
     * @return OMElement representation of the agent config
     */
    private static OMElement loadConfigXML() throws AgentConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + AgentConstants.AGENT_CONF;

        // if the agent config file not exists then simply return null.
        File agentConfigFile = new File(path);
        if (!agentConfigFile.exists()) {
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
            String errorMessage = AgentConstants.AGENT_CONF
                                  + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new AgentConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + AgentConstants.AGENT_CONF
                                  + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new AgentConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
                throw new AgentConfigurationException(errorMessage, e);
            }
        }
    }

    private static AgentConfiguration buildAgentConfiguration(
            OMElement agentServerConfig) {

        AgentConfiguration agentConfiguration = new AgentConfiguration();

        buildReceiverConfiguration(agentServerConfig, agentConfiguration);
        buildKeyStoreConfiguration(agentServerConfig, agentConfiguration);

        return agentConfiguration;
    }

    private static void buildReceiverConfiguration(OMElement agentServerConfig,
                                                   AgentConfiguration agentConfiguration) {
        OMElement maxTransportPoolSize = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.MAX_TRANSPORT_POOL_SIZE));
        if (maxTransportPoolSize != null) {
            agentConfiguration.setMaxTransportPoolSize(Integer.parseInt(maxTransportPoolSize.getText()));
        }

        OMElement maxIdleConnections = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.MAX_IDLE_CONNECTIONS));
        if (maxIdleConnections != null) {
            agentConfiguration.setMaxIdleConnections(Integer.parseInt(maxIdleConnections.getText()));
        }

        OMElement maxMessageBundleSize = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.MAX_MESSAGE_BUNDLE_SIZE));
        if (maxMessageBundleSize != null) {
            agentConfiguration.setMaxMessageBundleSize(Integer.parseInt(maxMessageBundleSize.getText()));
        }

        OMElement minIdleTimeInPool = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.MIN_IDLE_TIME_IN_POOL));
        if (minIdleTimeInPool != null) {
            agentConfiguration.setMinIdleTimeInPool(Integer.parseInt(minIdleTimeInPool.getText()));
        }


        OMElement bufferedEventsSize = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.BUFFERED_EVENTS_SIZE));
        if (bufferedEventsSize != null) {
            agentConfiguration.setBufferedEventsSize(Integer.parseInt(bufferedEventsSize.getText()));
        }

        OMElement poolSize = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.POOL_SIZE));
        if (poolSize != null) {
            agentConfiguration.setPoolSize(Integer.parseInt(poolSize.getText()));
        }
        OMElement evictionTimePeriod = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.EVICTION_TIME_PERIOD));
        if (evictionTimePeriod != null) {
            agentConfiguration.setEvictionTimePeriod(Integer.parseInt(evictionTimePeriod.getText()));
        }
        OMElement secureEvictionTimePeriod = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.SECURE_EVICTION_TIME_PERIOD));
        if (secureEvictionTimePeriod != null) {
            agentConfiguration.setSecureEvictionTimePeriod(Integer.parseInt(secureEvictionTimePeriod.getText()));
        }
        OMElement secureMaxIdleConnections = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.SECURE_MAX_IDLE_CONNECTIONS));
        if (secureMaxIdleConnections != null) {
            agentConfiguration.setEvictionTimePeriod(Integer.parseInt(secureMaxIdleConnections.getText()));
        }
        OMElement secureMaxTransportPoolSize = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.SECURE_MAX_TRANSPORT_POOL_SIZE));
        if (secureMaxTransportPoolSize != null) {
            agentConfiguration.setEvictionTimePeriod(Integer.parseInt(secureMaxTransportPoolSize.getText()));
        }
        OMElement secureMinIdleTimeInPool = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.SECURE_MIN_IDLE_TIME_IN_POOL));
        if (secureMinIdleTimeInPool != null) {
            agentConfiguration.setEvictionTimePeriod(Integer.parseInt(secureMinIdleTimeInPool.getText()));
        }
    }

    private static void buildKeyStoreConfiguration(OMElement agentServerConfig,
                                                   AgentConfiguration agentConfiguration) {
        OMElement trustStore = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.THRUST_STORE));
        if (trustStore != null) {
            agentConfiguration.setTrustStore(trustStore.getText());
        }
        OMElement trustStorePassword = agentServerConfig.getFirstChildWithName(
                new QName(AgentConstants.AGENT_CONF_NAMESPACE,
                          AgentConstants.THRUST_STORE_PASSWORD));
        if (trustStorePassword != null) {
            agentConfiguration.setTrustStorePassword(trustStorePassword.getText());
        }
    }

}
