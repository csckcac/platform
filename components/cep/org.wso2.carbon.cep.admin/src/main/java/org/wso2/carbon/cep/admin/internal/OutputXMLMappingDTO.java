package org.wso2.carbon.cep.admin.internal;


/**
 * This class is used give the output mapping in XML format
 */
public class OutputXMLMappingDTO {
    /**
     * Skeleton of the mapping XML
     */
    private String mappingXMLText;

    public String getMappingXMLText() {
        return mappingXMLText;
    }

    public void setMappingXMLText(String mappingXMLText) {
        this.mappingXMLText = mappingXMLText;
    }
}
