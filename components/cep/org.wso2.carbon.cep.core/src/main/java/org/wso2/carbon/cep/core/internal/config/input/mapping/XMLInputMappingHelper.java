package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.XpathDefinition;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This Class will help to build XMLMapping object from a given OMElement
 */
public class XMLInputMappingHelper {

    private static final Log log = LogFactory.getLog(XMLInputMappingHelper.class);


    public static InputMapping fromOM(OMElement xmlMappingElement)
            throws CEPConfigurationException {

        XMLInputMapping xmlInputMapping = new XMLInputMapping();

        String stream =
                xmlMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_STREAM));
        xmlInputMapping.setStream(stream);

        String className =
                xmlMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_EVENT_CLASS));

        if (className != null) {
            if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_TUPLE)) {
                xmlInputMapping.setMappingClass(Event.class);
            } else if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_MAP)) {
                xmlInputMapping.setMappingClass(Map.class);
            } else {
                try {
                    Class mappingClass = Class.forName(className);
                    xmlInputMapping.setMappingClass(mappingClass);
                } catch (ClassNotFoundException e) {
                    throw new CEPConfigurationException("No class found matching " + className, e);
                }
            }
        } else {
            xmlInputMapping.setMappingClass(Map.class);
        }



        for (Iterator iterator = xmlMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                 CEPConstants.CEP_CONF_ELE_XPATH_DEFINITON)); iterator.hasNext(); ) {
            OMElement xpathDefinitionElement = (OMElement) iterator.next();
            String prefix = xpathDefinitionElement.getAttributeValue(
                    new QName(CEPConstants.CEP_CONF_ATTR_PREFIX));
            String namespace = xpathDefinitionElement.getAttributeValue(
                    new QName(CEPConstants.CEP_CONF_ATTR_NAMESPACE));
            XpathDefinition xpathDefinition = new XpathDefinition();
            xpathDefinition.setPrefix(prefix);
            xpathDefinition.setNamespace(namespace);
            xmlInputMapping.addXpathDefinition(xpathDefinition);
        }

        for (Iterator iterator = xmlMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                 CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext(); ) {
            OMElement propertyElement = (OMElement) iterator.next();
            XMLProperty property = PropertyHelper.xmlPropertyFromOM(propertyElement);
            property.setInputProperty(true);
            xmlInputMapping.addProperty(property);
            if (xmlInputMapping.getMappingClass() != Map.class && xmlInputMapping.getMappingClass() != Event.class) {
                xmlInputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(xmlInputMapping.getMappingClass(), property.getName()));
            }
        }

        return xmlInputMapping;
    }


    static void addMappingToRegistry(Registry registry, XMLInputMapping xmlInputMapping,
                                     String mappingPath) throws RegistryException {

        List<XpathDefinition> xpathDefinitionList = xmlInputMapping.getXpathNamespacePrefixes();
        if (xpathDefinitionList != null && xpathDefinitionList.size() > 0) {
            for (XpathDefinition xpathDefinition : xpathDefinitionList) {
                String key = xpathDefinition.getPrefix();
                String value = xpathDefinition.getNamespace();
                Resource xpathDef = registry.newResource();
                xpathDef.setProperty(CEPConstants.CEP_REGISTRY_KEY, key);
                xpathDef.setProperty(CEPConstants.CEP_REGISTRY_VALUE, value);
                registry.put(mappingPath + CEPConstants.CEP_REGISTRY_XPATH_DEFS + CEPConstants.CEP_REGISTRY_BS + key, xpathDef);
            }
        }
        if (xmlInputMapping.getProperties() != null) {
            for (XMLProperty property : xmlInputMapping.getProperties()) {
                Resource propertyResource = registry.newResource();
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_NAME, property.getName());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_TYPE, property.getType());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_XPATH, property.getXpath());
                registry.put(mappingPath + CEPConstants.CEP_REGISTRY_PROPERTIES + CEPConstants.CEP_REGISTRY_BS + property.getName(), propertyResource);
            }
        }
    }

    static void loadMappingsFromRegistry(Registry registry, XMLInputMapping inputMapping,
                                         Collection mappingCollection)
            throws RegistryException, CEPConfigurationException {
        for (String mappingChild : mappingCollection.getChildren()) {
            if (registry.get(mappingChild) instanceof Collection) {
                Collection mapCollection = (Collection) registry.get(mappingChild);
                if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_XPATH_DEFS)
                        .equals(mappingChild.substring(mappingChild.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                    for (String defs : mapCollection.getChildren()) {
                        Resource xpathDefResource = registry.get(defs);
                        XpathDefinition xpathDefinition = new XpathDefinition();
                        xpathDefinition.setPrefix(xpathDefResource.getProperty(CEPConstants.CEP_REGISTRY_KEY));
                        xpathDefinition.setNamespace(xpathDefResource.getProperty(CEPConstants.CEP_REGISTRY_VALUE));
                        inputMapping.addXpathDefinition(xpathDefinition);
                    }
                } else if (
                        (CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES)
                                .equals(mappingChild.substring(mappingChild.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                    for (String defs : mapCollection.getChildren()) {
                        Resource xpathDefResource = registry.get(defs);
                        XMLProperty property = new XMLProperty();
                        property.setName(xpathDefResource.getProperty(CEPConstants.CEP_REGISTRY_NAME));
                        property.setType(xpathDefResource.getProperty(CEPConstants.CEP_REGISTRY_TYPE));
                        property.setXpath(xpathDefResource.getProperty(CEPConstants.CEP_REGISTRY_XPATH));
                        inputMapping.addProperty(property);
                        if (inputMapping.getMappingClass() != Map.class && inputMapping.getMappingClass() != Event.class) {
                            inputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(inputMapping.getMappingClass(), property.getName()));
                        }
                    }
                }
            }
        }
    }


    


	public static OMElement xmlInputMappingToOM(XMLInputMapping xmlInputMapping) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement omXMLInputMapping = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_XML_MAPPING,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String inputStream = xmlInputMapping.getStream();
		List<XpathDefinition> xpathDefinitionList = xmlInputMapping
				.getXpathDefinitionList();
		List<XMLProperty> xmlPropertyList = xmlInputMapping.getProperties();
		for (XpathDefinition xpathDefinition : xpathDefinitionList) {
			OMElement xpathChild = factory.createOMElement(new QName(
					CEPConstants.CEP_CONF_NAMESPACE,
					CEPConstants.CEP_CONF_ELE_XPATH_DEFINITON,
					CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
			String inputXpathPrefix = xpathDefinition.getPrefix();
			String inputXpathNameSpace = xpathDefinition.getNamespace();
			xpathChild.addAttribute(CEPConstants.CEP_CONF_ATTR_PREFIX,
					inputXpathPrefix, null);
			xpathChild.addAttribute(CEPConstants.CEP_CONF_ATTR_NAMESPACE,
					inputXpathNameSpace, null);
			omXMLInputMapping.addChild(xpathChild);
		}
		for (XMLProperty xmlProperty : xmlPropertyList) {
			OMElement propertychild = PropertyHelper
					.xmlPropertyToOM(xmlProperty);
			omXMLInputMapping.addChild(propertychild);
		}
		omXMLInputMapping.addAttribute(CEPConstants.CEP_CONF_ATTR_STREAM,
				inputStream, null);
		return omXMLInputMapping;
	}





}
