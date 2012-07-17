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
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiddhiBackEndRuntimeFactory implements CEPBackEndRuntimeFactory {


    public CEPBackEndRuntime createCEPBackEndRuntime(String bucketName,
                                                     List<InputMapping> mappings,
                                                     int tenantId)
            throws CEPConfigurationException {

        SiddhiConfiguration siddhiConfig=new SiddhiConfiguration();
        siddhiConfig.setSingleThreading(false);
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfig);

        Map<String, InputHandler> siddhiInputHandlerMap = new HashMap<String, InputHandler>();

        for (InputMapping mapping : mappings) {

            StreamDefinition streamDefinition = new StreamDefinition();
            streamDefinition.name(mapping.getStream());

            List properties;
            if (mapping instanceof TupleInputMapping) {
                TupleInputMapping tupleInputMapping = (TupleInputMapping) mapping;
                properties = tupleInputMapping.getProperties();
            } else { //Xml mapping
                XMLInputMapping xmlInputMapping = (XMLInputMapping) mapping;
                properties = xmlInputMapping.getProperties();
            }
//            String[] attributeNames = new String[properties.size()];
//            Class[] attributeTypes = new Class[properties.size()];

            for (Object property1 : properties) {
                Property property = (Property) property1;
                streamDefinition.attribute(property.getName(), SiddhiBackEndRuntime.javaToSiddhiType.get(property.getType()));
            }


//            try {
            siddhiInputHandlerMap.put(mapping.getStream(), siddhiManager.defineStream(streamDefinition));
//            } catch (SiddhiException e) {
//                throw new CEPConfigurationException("Invalid input stream configuration for " +
//                                                    mapping.getStream(), e);
//            }
        }

//        try {
//            siddhiManager.init();
//        } catch (SiddhiException e) {
//            throw new CEPConfigurationException("Cannot init Siddhi Backend", e);
//        }
        return new SiddhiBackEndRuntime(bucketName, siddhiManager, siddhiInputHandlerMap, tenantId);
    }
}
