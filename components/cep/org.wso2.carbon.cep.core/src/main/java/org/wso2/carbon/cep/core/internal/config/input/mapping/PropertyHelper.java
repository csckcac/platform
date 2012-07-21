package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.property.MapProperty;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;

import javax.xml.namespace.QName;

/**
 * This class will help to build Property object from a given OMElement
 */
public class PropertyHelper {
    public static XMLProperty xmlPropertyFromOM(OMElement propertyElement) {
        XMLProperty property = new XMLProperty();
        String name = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_NAME));
        String xpath = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_XPATH));
        String type = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_TYPE));
        String xmlFieldName = propertyElement.getAttributeValue(new QName(CEPConstants.CE_CONF_ELE_XML_FIELD_NAME));
        String xmlFieldType = propertyElement.getAttributeValue(new QName(CEPConstants.CE_CONF_ELE_XML_FIELD_TYPE));

        property.setName(name);
        property.setXpath(xpath);
        property.setType(type);
        property.setXmlFieldName(xmlFieldName);
        property.setXmlFieldType(xmlFieldType);

        return property;
    }
    public static TupleProperty tuplePropertyFromOM(OMElement propertyElement) {
        TupleProperty property = new TupleProperty();
        String dataType = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE));
        String name = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_NAME));
        String type = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_TYPE));

        property.setName(name);
        property.setDataType(dataType);
        property.setType(type);

        return property;
    }

    public static MapProperty mapPropertyFromOM(OMElement propertyElement) {
        MapProperty property = new MapProperty();
        String name = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_NAME));
        String type = propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_TYPE));

        property.setName(name);
        property.setType(type);

        return property;
    }



   

	public static OMElement xmlPropertyToOM(XMLProperty xmlProperty) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement propertyChild = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_PROPERTY,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String propertyName = xmlProperty.getName();
		String propertyXPath = xmlProperty.getXpath();
		String propertyType = xmlProperty.getType();
		String propertyXmlField = xmlProperty.getXmlFieldName();
		String propertyXmlFielType = xmlProperty.getXmlFieldType();
		propertyChild.addAttribute(CEPConstants.CEP_CONF_ATTR_NAME,
				propertyName, null);
		if (propertyXPath != null) {
			propertyChild.addAttribute(CEPConstants.CEP_CONF_ELE_XPATH,
					propertyXPath, null);
		}
		if (propertyType != null) {
			propertyChild.addAttribute(CEPConstants.CEP_CONT_ATTR_TYPE,
					propertyType, null);
		}
		if (propertyXmlField != null) {
			propertyChild.addAttribute(CEPConstants.CEP_REGISTRY_XML_FIELD_NAME,
					propertyXmlField, null);
		}
		if (propertyXmlFielType != null) {
			propertyChild.addAttribute(CEPConstants.CEP_REGISTRY_XML_FIELD_TYPE,
					propertyXmlFielType, null);
		}
		return propertyChild;
	}





}
