/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.business.messaging.paypal.mediator.ui.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

public class ConfigHandler {

    private static ConfigHandler configHandler;

    private InputStream configsInputStream;
    public static final String BUSINESS_ADAPTER_CONFIG_INPUTS_PATH = "./repository/conf/business-adapter-config/inputs.xml";

    private OMElement rootElem;

    public static ConfigHandler getInstance() {
        if (null == configHandler) {
            configHandler = new ConfigHandler();
        }
        return configHandler;
    }

    public OMElement parse(String operationName) throws
                                                 XMLStreamException, JaxenException, IOException {

        if (rootElem == null) {
            rootElem = new StAXOMBuilder(getInputResourceStream()).getDocumentElement();
        }
        AXIOMXPath xPath = new AXIOMXPath("//service/operation[@name='"
                                          + operationName + "']");
        return (OMElement) xPath.selectSingleNode(rootElem);
    }

    public void setInputResourceStream(String filePath) throws FileNotFoundException {
        if (configsInputStream == null) {
            this.configsInputStream = new FileInputStream(filePath);
        }
    }

    public void setInputResourceStream(InputStream stream) {
        if (configsInputStream == null) {
            this.configsInputStream = stream;
        }
    }

    public InputStream getInputResourceStream() {
        return configsInputStream;
    }

    public OMElement getRootElement(){
        return rootElem;
    }


}
