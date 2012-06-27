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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.mapping.output.mapping.XMLOutputMapping;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import javax.xml.namespace.QName;
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
 * This class will help to build the Output Mapping from a given OMELement
 */
public class XMLOutputMappingHelper {

    private static final Log log = LogFactory.getLog(XMLOutputMappingHelper.class);


    public static XMLOutputMapping fromOM(OMElement xmlMappingElement) {
        XMLOutputMapping xmlOutMapping = new XMLOutputMapping();
        String xmlMappingText = xmlMappingElement.toString();
        int index1 = xmlMappingText.indexOf(">");
        int index2 = xmlMappingText.lastIndexOf("<");
        xmlMappingText = xmlMappingText.substring(index1 + 1, index2);
        xmlOutMapping.setMappingXMLText(xmlMappingText);
        return xmlOutMapping;
    }

    public static void addXMLMappingToRegistry(Registry registry, String queryPath, XMLOutputMapping xmlOutMapping) throws CEPConfigurationException {
        try {
            Resource xmlMappingResource = registry.newResource();
            xmlMappingResource.setContent(xmlOutMapping.getMappingXMLText());
            registry.put(queryPath +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_OUTPUT +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_XML_MAPPING
                    , xmlMappingResource);
        } catch (RegistryException e) {
            String errorMessage = "Can not add xml mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyXMLMappingInRegistry(Registry registry, String queryPath, XMLOutputMapping xmlOutMapping) throws CEPConfigurationException {
        try {
            Resource xmlMappingResource = registry.newResource();
            xmlMappingResource.setContent(xmlOutMapping.getMappingXMLText());
            registry.put(queryPath +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_OUTPUT +
                    CEPConstants.CEP_REGISTRY_BS +
                    CEPConstants.CEP_REGISTRY_XML_MAPPING
                    , xmlMappingResource);
        } catch (RegistryException e) {
            String errorMessage = "Can not modify xml mapping in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static XMLOutputMapping loadXMLMappingFromRegistry(Registry registry, String mappingName) throws CEPConfigurationException {
        XMLOutputMapping xmlOutputMapping = null;
        try {
            xmlOutputMapping = new XMLOutputMapping();

            Resource outputdetailsResource = registry.get(mappingName);
            String content = new String((byte[]) outputdetailsResource.getContent());
            xmlOutputMapping.setMappingXMLText(content);
        } catch (RegistryException e) {
            String errorMessage = "Can not load xml mapping from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

        return xmlOutputMapping;
    }

    

	public static OMElement xmlOutputMappingToOM(
			XMLOutputMapping xmlOutputMapping) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement queryXMLOutputMapping = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_XML_MAPPING,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String mappingXMLText = xmlOutputMapping.getMappingXMLText();
		factory.createOMText(queryXMLOutputMapping, mappingXMLText,
				XMLStreamReader.CDATA);
		return queryXMLOutputMapping;
}





}
