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
import org.wso2.carbon.agent.server.conf.EventBridgeCoreConfiguration;
import org.wso2.carbon.agent.server.exception.EventBridgeConfigurationException;
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

    private EventBridgeCoreBuilder() {}

    public static OMElement loadConfigXML() throws EventBridgeConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + EventBridgeConstants.AGENT_SERVER_CONF;

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
            String errorMessage = EventBridgeConstants.AGENT_SERVER_CONF
                                  + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new EventBridgeConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + EventBridgeConstants.AGENT_SERVER_CONF
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
                new QName(EventBridgeConstants.AGENT_SERVER_CONF_NAMESPACE,
                          EventBridgeConstants.EVENT_STREAM_DEFINITIONS));

        if (eventStreamDefinitions != null) {
            for (Iterator eventStreamDefinitionIterator = eventStreamDefinitions.getChildElements();
                 eventStreamDefinitionIterator.hasNext(); ) {
                OMElement eventStreamDefinition = (OMElement) eventStreamDefinitionIterator.next();
                String domainName=eventStreamDefinition.getAttributeValue(new QName(EventBridgeConstants.DOMAIN_NAME));

                eventStreamDefinitionList.add(new String[]{domainName, eventStreamDefinition.getText()});
            }
        }
    }


    public static void populateStreamDefinitionStore(OMElement config,
                                                      EventBridgeCoreConfiguration eventBridgeCoreConfiguration){
        OMElement streamDefinitionStore = config.getFirstChildWithName(
                new QName(EventBridgeConstants.AGENT_SERVER_CONF_NAMESPACE,
                          EventBridgeConstants.STREAM_DEFINITION_STORE));
        if (streamDefinitionStore != null) {
            eventBridgeCoreConfiguration.setStreamDefinitionStoreName(streamDefinitionStore.getText());
        }

    }

}
