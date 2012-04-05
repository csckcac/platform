package org.wso2.carbon.messagebox.internal.builder;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.messagebox.MessageBoxConfigurationException;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.MessageBoxServiceFactory;
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

public class MessageBoxServiceBuilder {
    public static MessageBoxService createMessageBoxService() throws
                                                              MessageBoxConfigurationException {
        OMElement mbConfig = loadConfigXML();
        if (!mbConfig.getQName().equals(
                new QName(MessageBoxConstants.MB_CONF_NAMESPACE, MessageBoxConstants.MB_CONF_ELE_ROOT))) {
            throw new MessageBoxConfigurationException("Invalid root element in message box config");
        }

        OMElement messageBoxService =
                mbConfig.getFirstChildWithName(new QName(MessageBoxConstants.MB_CONF_NAMESPACE,
                                                         MessageBoxConstants.MB_CONF_ELE_SERVICE));
        String className =
                messageBoxService.getAttributeValue(new QName(null, MessageBoxConstants.MB_CONF_ATTR_CLASS));

        try {
            Class messageBoxServiceImplClass = Class.forName(className);
            MessageBoxServiceFactory messageBoxServiceFactory =
                    (MessageBoxServiceFactory) messageBoxServiceImplClass.newInstance();
            return messageBoxServiceFactory.getMessageBoxService(messageBoxService);
        } catch (ClassNotFoundException e) {
            throw new MessageBoxConfigurationException("Can not load the class " + className, e);
        } catch (IllegalAccessException e) {
            throw new MessageBoxConfigurationException("Can not access the class " + className, e);
        } catch (InstantiationException e) {
            throw new MessageBoxConfigurationException("Can not instantiate the class " + className, e);
        }
    }

    /**
     * Helper method to load the message box config
     *
     * @return OMElement representation of the message box config
     */
    private static OMElement loadConfigXML() throws MessageBoxConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + MessageBoxConstants.MESSAGE_BOX_CONF;
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
            throw new MessageBoxConfigurationException(MessageBoxConstants.MESSAGE_BOX_CONF
                                                       + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new MessageBoxConfigurationException("Invalid XML for " + MessageBoxConstants.MESSAGE_BOX_CONF
                                                       + " located in the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
                throw new MessageBoxConfigurationException("Can not close the input stream");
            }
        }
    }
}
