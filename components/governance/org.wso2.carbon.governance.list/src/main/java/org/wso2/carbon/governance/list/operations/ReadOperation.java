/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.list.operations;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class ReadOperation extends AbstractOperation{
    private Log log = LogFactory.getLog(ReadOperation.class);
    private String content;

    public ReadOperation(QName name, Registry systemRegistry, String mediatype, String namespace) {
        super(name, systemRegistry, mediatype, namespace);
    }

    @Override
    public void setPayload(OMElement bodyContent, String namespace) throws XMLStreamException {
        bodyContent.addChild(AXIOMUtil.stringToOM("<return>" + content + "</return>"));
    }

    @Override
    public String getRequestParameterSchemaFragment() {
        return "<xs:element minOccurs=\"0\" name=\"artifactId\" nillable=\"true\" type=\"xs:string\"/>";
    }

    @Override
    public String getResponseType() {
        return "string";
    }

    public MessageContext process(MessageContext requestMessageContext) throws AxisFault {
        String artifactId;
        AXIOMXPath expression;
        try {
            String operation = requestMessageContext.getOperationContext().getAxisOperation().getName().getLocalPart();
            expression = new AXIOMXPath("//ns:" + operation + "/ns:artifactId");
            expression.addNamespace("ns", namespace);
            artifactId = ((OMElement) expression.selectNodes(requestMessageContext.getEnvelope().getBody()).get(0)).getText();
        } catch (JaxenException e) {
            String msg = "Error occured while reading the content of the SOAP message";
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(systemRegistry, rxtKey);
            String path = artifactManager.getGenericArtifact(artifactId).getPath();
            content = new String((byte [])systemRegistry.get(path).getContent());
        } catch (RegistryException e) {
            String msg = "Error occured while deleting the resource at " + artifactId;
            log.error(msg);
            throw new AxisFault(msg, e);
        } catch (NullPointerException e) {
            String msg = "Artifact not found for the artifact id " + artifactId;
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        return getAbstractResponseMessageContext(requestMessageContext);
    }
}
