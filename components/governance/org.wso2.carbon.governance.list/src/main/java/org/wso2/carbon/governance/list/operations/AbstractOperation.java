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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public abstract class AbstractOperation extends InOutAxisOperation implements MessageProcessor {
    private Log log = LogFactory.getLog(AbstractOperation.class);
    protected String rxtKey;
    protected Registry systemRegistry;
    protected String name;
    protected String mediatype;
    protected String namespace;
    protected MessageContext messageContext;

    protected AbstractOperation(QName name, Registry systemRegistry, String mediatype, String namespace) {
        super(name);
        this.systemRegistry = systemRegistry;
        this.name = name.getLocalPart();
        this.mediatype = mediatype;
        this.namespace = namespace;
    }

    public AbstractOperation init(String rxtKey, RXTMessageReceiver receiver) {
        this.rxtKey = rxtKey;
        receiver.setMessageProcessor(name, this);
        setMessageReceiver(receiver);
        String namespace = "http://services." + name + ".governance.carbon.wso2.org";
        AxisMessage in = getMessage("In");
        in.setName(name + "Request");
        in.setElementQName(new QName(namespace, name));
        AxisMessage out = getMessage("Out");
        out.setName(name + "Response");
        out.setElementQName(new QName(namespace, name));
        AxisMessage fault = new AxisMessage();
        fault.setName(name + "ServiceGovernanceException");
        fault.setElementQName(new QName(namespace, name + "ServiceGovernanceException"));
        setFaultMessages(fault);
        return this;
    }

    public XmlSchema[] getSchemas(XmlSchemaCollection collection) {
        String str = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ax2234=\"http://exception.api.governance.carbon.wso2.org/xsd\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" targetNamespace=\"" + namespace + "\">\n" +
                "            <xs:import namespace=\"http://exception.api.governance.carbon.wso2.org/xsd\" />\n" +
                "            <xs:element name=\"" + name + "ServiceGovernanceException\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"GovernanceException\" nillable=\"true\" type=\"ax2234:GovernanceException\" />\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"" + name + "\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        " + getRequestParameterSchemaFragment() + "\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"" + name + "Response\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" type=\"xs:" + getResponseType() + "\" />\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "        </xs:schema>";
        return Arrays.asList(collection.read(new StreamSource(new ByteArrayInputStream(str.getBytes())), null)).toArray(
                new XmlSchema[1]);
    }

    public MessageContext getAbstractResponseMessageContext(MessageContext requestMessageContext) throws AxisFault {
        MessageContext outMessageCtx = MessageContextBuilder.createOutMessageContext(requestMessageContext);

        SOAPFactory factory = getSOAPFactory(requestMessageContext);
        AxisOperation operation = requestMessageContext.getOperationContext().getAxisOperation();
        AxisService service = requestMessageContext.getAxisService();

        OMElement bodyContent;

        AxisMessage outMessage = operation.getMessage("Out");

        bodyContent = factory.createOMElement(outMessage.getName(),
                factory.createOMNamespace(service.getTargetNamespace(),
                        service.getSchemaTargetNamespacePrefix()));
        try {
            setPayload(bodyContent, service.getSchemaTargetNamespace());
        } catch (XMLStreamException e) {
            String msg = "Error in adding the payload to the response message";
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        SOAPEnvelope soapEnvelope = factory.getDefaultEnvelope();
        soapEnvelope.getBody().addChild(bodyContent);
        outMessageCtx.setEnvelope(soapEnvelope);
        return outMessageCtx;
    }

    public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }

    public abstract void setPayload(OMElement bodyContent, String namespace) throws XMLStreamException;

    public abstract String getRequestParameterSchemaFragment();

    public abstract String getResponseType();
}
