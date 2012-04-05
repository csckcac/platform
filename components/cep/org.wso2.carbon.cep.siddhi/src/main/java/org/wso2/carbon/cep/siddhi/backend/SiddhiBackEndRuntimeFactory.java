/*
 * Copyright 2004,2012 The Apache Software Foundation.
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

package org.wso2.carbon.cep.siddhi.backend;

import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntimeFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.property.Property;
import org.wso2.siddhi.api.eventstream.InputEventStream;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiException;
import org.wso2.siddhi.core.node.InputHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiddhiBackEndRuntimeFactory implements CEPBackEndRuntimeFactory {


    public CEPBackEndRuntime createCEPBackEndRuntime(String bucketName,
                                                     List<InputMapping> mappings,
                                                     int tenantId)
            throws CEPConfigurationException {

        SiddhiManager siddhiManager = new SiddhiManager();

        Map<String, InputHandler> siddhiInputHandlerMap = new HashMap<String, InputHandler>();

        for (InputMapping mapping : mappings) {
            List properties;
            if (mapping instanceof TupleInputMapping) {
                TupleInputMapping tupleInputMapping = (TupleInputMapping) mapping;
                properties = tupleInputMapping.getProperties();
            } else { //Xml mapping
                XMLInputMapping xmlInputMapping = (XMLInputMapping) mapping;
                properties = xmlInputMapping.getProperties();
            }
            String[] attributeNames = new String[properties.size()];
            Class[] attributeTypes = new Class[properties.size()];

            for (int i = 0, propertiesSize = properties.size(); i < propertiesSize; i++) {
                Property property = (Property) properties.get(i);
                attributeNames[i] = property.getName();
                attributeTypes[i] = SiddhiBackEndRuntime.javaTypeToClass.get(property.getType());
            }


            try {
                siddhiInputHandlerMap.put(mapping.getStream(),
                                          siddhiManager.addInputEventStream(
                                                  new InputEventStream(mapping.getStream(),
                                                                       attributeNames,
                                                                       attributeTypes)));
            } catch (SiddhiException e) {
                throw new CEPConfigurationException("Invalid input stream configuration for " +
                                                    mapping.getStream(), e);
            }
        }

        try {
            siddhiManager.init();
        } catch (SiddhiException e) {
            throw new CEPConfigurationException("Cannot init Siddhi Backend", e);
        }
        return new SiddhiBackEndRuntime(bucketName, siddhiManager, siddhiInputHandlerMap, tenantId);
    }
}
