package org.wso2.carbon.governance.list.operations;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.List;

public class GetAllArtifactIDsOperation extends AbstractOperation{
    private Log log = LogFactory.getLog(GetAllArtifactIDsOperation.class);
    private List<String> artifactIDs;

    public GetAllArtifactIDsOperation(QName name, Registry governanceSystemRegistry, String mediatype, String namespace) {
        super(name, governanceSystemRegistry, mediatype, namespace);
    }

    @Override
    public void setPayload(OMElement bodyContent, String namespace) throws XMLStreamException {
        bodyContent.addChild(AXIOMUtil.stringToOM("<return>" + artifactIDs + "</return>"));
    }

    @Override
    public String getRequestName() {
        return "";
    }

    @Override
    public String getRequestType() {
        return "";
    }

    @Override
    public String getResponseType() {
        return "string";
    }

    public MessageContext process(MessageContext requestMessageContext) throws AxisFault {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(governanceSystemRegistry, rxtKey);
            artifactIDs = Arrays.asList(artifactManager.getAllGenericArtifactIds());
        } catch (RegistryException e) {
            String msg = "Error occured while retrieving artifacts";
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        return getAbstractResponseMessageContext(requestMessageContext);
    }
}
