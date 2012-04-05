package org.wso2.carbon.cep.core.mapping.property;

/**
 * This class contains properties of inputs and outputs
 * */
public class XMLProperty extends Property{


    /**
     * Xpath of the property
     * */
    private String xpath;

    /**
     * Name of the xml field
     * */
    private String xmlFieldName;

    /**
     * Type of the XML field
     * */
    private String xmlFieldType;

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getXmlFieldName() {
        return xmlFieldName;
    }

    public void setXmlFieldName(String xmlFieldName) {
        this.xmlFieldName = xmlFieldName;
    }

    public String getXmlFieldType() {
        return xmlFieldType;
    }

    public void setXmlFieldType(String xmlFieldType) {
        this.xmlFieldType = xmlFieldType;
    }


}
