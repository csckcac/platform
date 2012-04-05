package org.wso2.carbon.cep.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class XMLPropertyDTO {

    /**
     * Boolean to identify the property
     * if:
     * true - InputDTO XMLPropertyDTO
     * false - OutputDTO XMLPropertyDTO
     */
    private boolean isInputProperty;
    /**
     * Name of the property
     */
    private String name;

    /**
     * Xpath of the property
     */
    private String xpath;

    /**
     * Type of the property
     */
    private String type;

    /**
     * Name of the xml field
     */
    private String xmlFieldName;

    /**
     * Type of the XML field
     */
    private String xmlFieldType;


    public boolean isInputProperty() {
        return isInputProperty;
    }

    public void setInputProperty(boolean inputProperty) {
        isInputProperty = inputProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
