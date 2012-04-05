package org.wso2.carbon.cep.admin.internal;

/**
 * This class is used to define mapping between input XML to CEP Engine Inputs
 * */
public class InputXMLMappingDTO {
    /**
     * Name of the mapping Stream
     * */
    private String stream;

    /**
     * Name of the to be converted event class
     */
    protected String mappingClass ;

    /**
     * Definition of the XPaths to be used (Prefix Namespace mapping)
     * */

    private XpathDefinitionDTO[] xpathDefinitionDTOs;

    /**
     * Properties of the mapping
     * */
    private XMLPropertyDTO[] XMLPropertyDTOs;


    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public XMLPropertyDTO[] getProperties() {
        return XMLPropertyDTOs;
    }

    public void setProperties(XMLPropertyDTO[] XMLPropertyDTOs) {
        this.XMLPropertyDTOs = XMLPropertyDTOs;
    }

    public void setXpathDefinition(XpathDefinitionDTO[] xpathDefinitionDTOs){
        this.xpathDefinitionDTOs = xpathDefinitionDTOs;
    }
     public XpathDefinitionDTO[] getXpathDefinition() {
        return xpathDefinitionDTOs;
    }

    public String getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(String mappingClass) {
        this.mappingClass = mappingClass;
    }
}
