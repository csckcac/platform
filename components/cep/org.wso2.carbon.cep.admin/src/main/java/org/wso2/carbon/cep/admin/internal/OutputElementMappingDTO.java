package org.wso2.carbon.cep.admin.internal;


/**
 * This class used to Map the CEP out put to the output XML element
 */
public class OutputElementMappingDTO {
    /**
     * Document element of the element mapping
     */
    private String documentElement;

    /**
     * Namespace mapped for the document element
     */
    private String namespace;

    private XMLPropertyDTO[] XMLPropertyDTOs;



    public String getDocumentElement() {
        return documentElement;
    }

    public void setDocumentElement(String documentElement) {
        this.documentElement = documentElement;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public XMLPropertyDTO[] getProperties() {
        return XMLPropertyDTOs;
    }

    public void setProperties(XMLPropertyDTO[] XMLPropertyDTOs) {
        this.XMLPropertyDTOs = XMLPropertyDTOs;
    }
}
