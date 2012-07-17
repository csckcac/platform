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
package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This Class will help to build TupleMapping object from a given OMElement
 */
public class TupleInputMappingHelper {
    private static final Log log = LogFactory.getLog(XMLInputMappingHelper.class);

    public static InputMapping fromOM(OMElement tupleMappingElement)
            throws CEPConfigurationException {
        TupleInputMapping tupleInputMapping = new TupleInputMapping();

        String stream =
                tupleMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_STREAM));
        tupleInputMapping.setStream(stream);

        String className =
                tupleMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_EVENT_CLASS));
        if (className != null) {
            if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_TUPLE)) {
                tupleInputMapping.setMappingClass(Event.class);
            } else if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_MAP)) {
                tupleInputMapping.setMappingClass(Map.class);
            } else {
                try {
                    Class mappingClass = Class.forName(className);
                    tupleInputMapping.setMappingClass(mappingClass);
                } catch (ClassNotFoundException e) {
                    throw new CEPConfigurationException("No class found matching " + className, e);
                }
            }
        } else {
            tupleInputMapping.setMappingClass(Event.class);
        }

        for (Iterator iterator = tupleMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                   CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext(); ) {
            OMElement propertyElement = (OMElement) iterator.next();
            TupleProperty property = PropertyHelper.tuplePropertyFromOM(propertyElement);
            property.setInputProperty(true);
            tupleInputMapping.addProperty(property);
            if (tupleInputMapping.getMappingClass() != Map.class && tupleInputMapping.getMappingClass() != Event.class) {
                tupleInputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(tupleInputMapping.getMappingClass(), property.getName()));
            }
        }

        return tupleInputMapping;
    }

    static void addMappingToRegistry(Registry registry, TupleInputMapping tupleInputMapping,
                                     String mappingPath) throws RegistryException {
        if (tupleInputMapping.getProperties() != null) {
            List<TupleProperty> properties = tupleInputMapping.getProperties();
            for (int i = 0, propertiesSize = properties.size(); i < propertiesSize; i++) {
                TupleProperty property = properties.get(i);
                Resource propertyResource = registry.newResource();
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_NAME, property.getName());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_DATA_TYPE, property.getDataType());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_TYPE, property.getType());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_POSITION, i+"");
                registry.put(mappingPath + CEPConstants.CEP_REGISTRY_PROPERTIES + CEPConstants.CEP_REGISTRY_BS + property.getName(), propertyResource);
            }
        }
    }

    static void loadMappingsFromRegistry(Registry registry, TupleInputMapping inputMapping,
                                         Collection mappingCollection)
            throws RegistryException, CEPConfigurationException {
        for (String mappingChild : mappingCollection.getChildren()) {
            if (registry.get(mappingChild) instanceof Collection) {
                Collection mapCollection = (Collection) registry.get(mappingChild);
                if (
                        (CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES)
                                .equals(mappingChild.substring(mappingChild.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                    TupleProperty[] tupleProperties= new TupleProperty[mapCollection.getChildCount()];
                    for (String defs : mapCollection.getChildren()) {
                        Resource propertyResource = registry.get(defs);
                        TupleProperty property = new TupleProperty();
                        property.setName(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_NAME));
                        property.setDataType(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_DATA_TYPE));
                        property.setType(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_TYPE));
                        tupleProperties[Integer.parseInt(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_POSITION))]=property;
                        if (inputMapping.getMappingClass() != Map.class && inputMapping.getMappingClass() != Event.class) {
                            inputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(inputMapping.getMappingClass(), property.getName()));
                        }
                    }
                    inputMapping.setProperties(Arrays.asList(tupleProperties));
                }
            }
        }
    }
}
