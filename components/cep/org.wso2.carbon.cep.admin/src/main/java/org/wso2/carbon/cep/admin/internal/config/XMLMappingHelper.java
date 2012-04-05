package org.wso2.carbon.cep.admin.internal.config;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.admin.internal.OutputXMLMappingDTO;

public class XMLMappingHelper {
    private static final Log log = LogFactory.getLog(XMLMappingHelper.class);

    public static OutputXMLMappingDTO fromOM(OMElement xmlMappingOmElement) {
        OutputXMLMappingDTO outputXmlMappingDTO = new OutputXMLMappingDTO();
        outputXmlMappingDTO.setMappingXMLText(xmlMappingOmElement.toString());
        return outputXmlMappingDTO;
    }
}
