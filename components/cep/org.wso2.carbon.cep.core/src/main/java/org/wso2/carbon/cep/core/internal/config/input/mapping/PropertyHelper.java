package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;

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
}
