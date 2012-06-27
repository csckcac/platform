/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.cep.core.internal.config.output;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.cep.core.internal.config.input.mapping.PropertyHelper;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.output.mapping.ElementOutputMapping;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

/**
 * This class will help to build the Element Xml Output Object from a given OMELement
 */
public class ElementOutputMappingHelper {

    private static final Log log = LogFactory.getLog(ElementOutputMappingHelper.class);


    public static ElementOutputMapping fromOM(OMElement elementMappingElement) {
        
        String documentElement =
                elementMappingElement.getAttributeValue(new QName(CEPConstants.CEP_CONT_ATTR_DOC_ELEMENT));
        String namespace = elementMappingElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_NAMESPACE));

        ElementOutputMapping elementOutputMapping = new ElementOutputMapping();
        elementOutputMapping.setDocumentElement(documentElement);
        elementOutputMapping.setNamespace(namespace);

        for (Iterator iterator = elementMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext();) {
            OMElement propertyElement = (OMElement) iterator.next();
            XMLProperty property = PropertyHelper.xmlPropertyFromOM(propertyElement);
            property.setInputProperty(false);
            elementOutputMapping.addProperty(property);
        }

        return elementOutputMapping;
    }

    public static void addElementMappingToRegistry(Registry registry,
                                                   ElementOutputMapping elementOutputMapping,
                                                   String queryPath) throws CEPConfigurationException {
        try {
            String elementMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_OUTPUT +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_ELEMENT_MAPPING;
            registry.put(queryPath + elementMappingPathString, registry.newCollection());
            Resource elementMappingResource = registry.newResource();
            elementMappingResource.addProperty(CEPConstants.CEP_REGISTRY_DOC_ELEMENT, elementOutputMapping.getDocumentElement());
            elementMappingResource.addProperty(CEPConstants.CEP_REGISTRY_NS, elementOutputMapping.getNamespace());
            registry.put(queryPath + elementMappingPathString +
                    CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_DETAILS, elementMappingResource);

            registry.put(queryPath + elementMappingPathString +
                    CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES, registry.newCollection());
            for (XMLProperty property : elementOutputMapping.getProperties()) {
                Resource elementMappingProperties = registry.newResource();
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_NAME, property.getName());
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_XML_FIELD_NAME, property.getXmlFieldName());
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_XML_FIELD_TYPE, property.getXmlFieldType());
                registry.put(queryPath + elementMappingPathString +
                        CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES +
                        CEPConstants.CEP_REGISTRY_BS + property.getName(), elementMappingProperties);
            }
        } catch (Exception e) {
            String errorMessage = "Can not add element mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyElementMappingInRegistry(Registry registry,
                                                      ElementOutputMapping elementOutputMapping,
                                                      String queryPath) throws CEPConfigurationException {
        try {
            String elementMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_OUTPUT +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_ELEMENT_MAPPING;
            registry.put(queryPath + elementMappingPathString, registry.newCollection());
            Resource elementMappingResource = registry.newResource();
            elementMappingResource.addProperty(CEPConstants.CEP_REGISTRY_DOC_ELEMENT, elementOutputMapping.getDocumentElement());
            elementMappingResource.addProperty(CEPConstants.CEP_REGISTRY_NS, elementOutputMapping.getNamespace());
            registry.put(queryPath + elementMappingPathString +
                    CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_DETAILS, elementMappingResource);

            registry.put(queryPath + elementMappingPathString +
                    CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES, registry.newCollection());
            for (XMLProperty property : elementOutputMapping.getProperties()) {
                Resource elementMappingProperties = registry.newResource();
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_NAME, property.getName());
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_XML_FIELD_NAME, property.getXmlFieldName());
                elementMappingProperties.addProperty(CEPConstants.CEP_REGISTRY_XML_FIELD_TYPE, property.getXmlFieldType());
                registry.put(queryPath + elementMappingPathString +
                        CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES +
                        CEPConstants.CEP_REGISTRY_BS + property.getName(), elementMappingProperties);
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not modify element mapping in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }

    public static ElementOutputMapping loadElementMappingFromRegistry(Registry registry,
                                                                String mappingName) throws CEPConfigurationException {
        ElementOutputMapping elementOutputMapping = new ElementOutputMapping();
        try {
            if (registry.get(mappingName) instanceof Collection) {
                Collection propertyCollection = (Collection) registry.get(mappingName);
                for (String propertyName : propertyCollection.getChildren()) {
                    Resource propertyResource = registry.get(propertyName);
                    Hashtable propertiesHashtable = propertyResource.getProperties();
                    Enumeration e = propertiesHashtable.keys();
                    XMLProperty property = new XMLProperty();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        ArrayList values = (ArrayList) propertiesHashtable.get(key);
                        if (CEPConstants.CEP_REGISTRY_NAME.equals(key)) {
                            property.setName(values.get(0).toString());
                        } else if (CEPConstants.CEP_REGISTRY_XML_FIELD_NAME.equals(key)) {
                            property.setXmlFieldName(values.get(0).toString());
                        } else if (CEPConstants.CEP_REGISTRY_XML_FIELD_TYPE.equals(key)) {
                            property.setXmlFieldType(values.get(0).toString());
                        }
                    }
                    elementOutputMapping.addProperty(property);
                }
            } else {
                Resource outputdetailsResource = registry.get(mappingName);
                Hashtable propertiesHashtable = outputdetailsResource.getProperties();
                Enumeration e = propertiesHashtable.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    ArrayList values = (ArrayList) propertiesHashtable.get(key);
                    if (CEPConstants.CEP_REGISTRY_DOC_ELEMENT.equals(key)) {
                        elementOutputMapping.setDocumentElement(values.get(0).toString());
                    } else if (CEPConstants.CEP_REGISTRY_NS.equals(key)) {
                        elementOutputMapping.setNamespace(values.get(0).toString());
                    }
                }
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not load element mapping from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return elementOutputMapping;
    }


    


	public static OMElement elementOutputMappingToOM(
			ElementOutputMapping elementOutputMapping) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement queryElementOutputMapping = factory
				.createOMElement(new QName(CEPConstants.CEP_CONF_NAMESPACE,
						CEPConstants.CEP_CONF_ELE_EMAPPING,
						CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String documentElement = elementOutputMapping.getDocumentElement();
		String elementNameSpace = elementOutputMapping.getNamespace();
		queryElementOutputMapping.addAttribute(
				CEPConstants.CEP_REGISTRY_DOC_ELEMENT, documentElement, null);
		queryElementOutputMapping.addAttribute(CEPConstants.CEP_REGISTRY_NS,
				elementNameSpace, null);
		List<XMLProperty> xmlPropertyList = elementOutputMapping
				.getProperties();
		for (XMLProperty xmlProperty : xmlPropertyList) {
			OMElement propertyChild = PropertyHelper
					.xmlPropertyToOM(xmlProperty);
			queryElementOutputMapping.addChild(propertyChild);
		}
		return queryElementOutputMapping;
	}


}
