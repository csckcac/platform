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
package org.wso2.carbon.eventbridge.core.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.core.conf.EventBridgeConfiguration;
import org.wso2.carbon.eventbridge.core.exception.EventBridgeConfigurationException;
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
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to build Agent Server Initial Configurations
 */
public final class EventBridgeCoreBuilder {

    private static final Log log = LogFactory.getLog(EventBridgeCoreBuilder.class);

    private EventBridgeCoreBuilder() {
    }

    public static OMElement loadConfigXML() throws EventBridgeConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + EventBridgeConstants.EVENT_BRIDGE_CONFIG_XML;

        // if the agent server config file not exists then simply return null.
        File eventBridgeConfigFile = new File(path);
        if (!eventBridgeConfigFile.exists()) {
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
            String errorMessage = EventBridgeConstants.EVENT_BRIDGE_CONFIG_XML
                                  + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new EventBridgeConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + EventBridgeConstants.EVENT_BRIDGE_CONFIG_XML
                                  + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new EventBridgeConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }

    public static void populateEventStreamDefinitions(OMElement config,
                                                      List<String[]> eventStreamDefinitionList) {
        OMElement eventStreamDefinitions = config.getFirstChildWithName(
                new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE,
                          EventBridgeConstants.EVENT_STREAM_DEFINITIONS_ELEMENT));

        if (eventStreamDefinitions != null) {
            for (Iterator eventStreamDefinitionIterator = eventStreamDefinitions.getChildElements();
                 eventStreamDefinitionIterator.hasNext(); ) {
                OMElement eventStreamDefinition = (OMElement) eventStreamDefinitionIterator.next();
                String domainName = eventStreamDefinition.getAttributeValue(new QName(EventBridgeConstants.DOMAIN_NAME_ATTRIBUTE));

                eventStreamDefinitionList.add(new String[]{domainName, eventStreamDefinition.getText()});
            }
        }
    }


    public static void populateStreamDefinitionStore(OMElement config,
                                                     EventBridgeConfiguration eventBridgeConfiguration) {
        OMElement streamDefinitionStore = config.getFirstChildWithName(
                new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE,
                          EventBridgeConstants.STREAM_DEFINITION_STORE_ELEMENT));
        if (streamDefinitionStore != null) {
            eventBridgeConfiguration.setStreamDefinitionStoreName(streamDefinitionStore.getText());
        }

    }

    public static void populateRuntimeParameters(OMElement config,
                                                 EventBridgeConfiguration eventBridgeConfiguration) {
        OMElement workerThreads = config.getFirstChildWithName(
                new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE,
                          EventBridgeConstants.WORKER_THREADS_ELEMENT));
        if (workerThreads != null) {
            try {
                eventBridgeConfiguration.setWorkerThreads(Integer.parseInt(workerThreads.getText()));
            } catch (NumberFormatException ignored) {

            }
        }
        OMElement eventBufferCapacity = config.getFirstChildWithName(
                new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE,
                          EventBridgeConstants.EVENT_BUFFER_CAPACITY_ELEMENT));
        if (eventBufferCapacity != null) {
            try {
                eventBridgeConfiguration.setEventBufferCapacity(Integer.parseInt(eventBufferCapacity.getText()));
            } catch (NumberFormatException ignored) {

            }
        }
        OMElement clientTimeout = config.getFirstChildWithName(
                new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE,
                          EventBridgeConstants.CLIENT_TIMEOUT_ELEMENT));
        if (clientTimeout != null) {
            try {
                eventBridgeConfiguration.setClientTimeOut(Integer.parseInt(clientTimeout.getText()));
            } catch (NumberFormatException ignored) {

            }
        }

    }

    public static void populateConfigurations(EventBridgeConfiguration eventBridgeConfiguration,
                                              List<String[]> eventStreamDefinitions,
                                              OMElement bridgeConfig)
            throws EventBridgeConfigurationException {

        if (bridgeConfig != null) {
            if (!bridgeConfig.getQName().equals(
                    new QName(EventBridgeConstants.EVENT_BRIDGE_NAMESPACE, EventBridgeConstants.EVENT_BRIDGE_ROOT_ELEMENT))) {
                throw new EventBridgeConfigurationException("Invalid root element in agent server config");
            }
            EventBridgeCoreBuilder.populateEventStreamDefinitions(bridgeConfig, eventStreamDefinitions);
            EventBridgeCoreBuilder.populateStreamDefinitionStore(bridgeConfig, eventBridgeConfiguration);
        }
    }
}
