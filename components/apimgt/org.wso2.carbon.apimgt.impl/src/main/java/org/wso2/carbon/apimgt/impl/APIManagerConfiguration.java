/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global API Manager configuration. This is generally populated from the repository/conf/amConfig.xml
 * file at system startup. Once successfully populated, this class does not allow more parameters
 * to be added to the configuration.
 */
public class APIManagerConfiguration {
    
    private Map<String,List<String>> configuration = new ConcurrentHashMap<String, List<String>>();

    private boolean initialized;
    
    public void load(String filePath) throws APIManagementException {
        if (initialized) {
            return;
        }
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
        } catch (IOException e) {
            throw new APIManagementException("I/O error while reading the API manager " +
                    "configuration: " + filePath, e);
        } catch (XMLStreamException e) {
            throw new APIManagementException("Error while parsing the API manager " +
                    "configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String getFirstProperty(String key) {
        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    private void readChildElements(OMElement serverConfig,
                                   Stack<String> nameStack) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements
                .hasNext();) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                String value = element.getText();
                addToConfiguration(key, value);
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private void addToConfiguration(String key, String value) {
        List<String> list = configuration.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configuration.put(key, list);
        } else {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

}
