/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.w3c.dom.Element;
import org.wso2.carbon.bpel.b4p.extension.BPEL4PeopleConstants;

import javax.wsdl.*;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Analyze the implementation copied from ODE source.
 */

public class SOAPHelper {

    private Binding binding;
    private SOAPFactory soapFactory;
    private boolean isRPC;

    public SOAPHelper(Binding binding, SOAPFactory soapFactory,
                      boolean isRPC) {
        this.binding = binding;
        this.soapFactory = soapFactory;
        this.isRPC = isRPC;
    }

    public static ExtensibilityElement getBindingExtension(Binding binding) {
        Collection bindings = new ArrayList();
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), HTTPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAP12Binding.class);
        if (bindings.size() == 0) {
            return null;
        } else if (bindings.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException("Multiple bindings: " + binding.getQName());
        } else {
            // retrieve the single element
            return (ExtensibilityElement) bindings.iterator().next();
        }
    }

    public void createSoapRequest(MessageContext msgCtx, Element message, Operation op)
            throws AxisFault {
        if (op == null) {
            throw new NullPointerException("Null operation");
        }
        // The message can be null if the input message has no part
        if (op.getInput().getMessage().getParts().size() > 0 && message == null) {
            throw new NullPointerException("Null message.");
        }
        if (msgCtx == null) {
            throw new NullPointerException("Null msgCtx");
        }

        BindingOperation bop = binding.getBindingOperation(op.getName(), null, null);

        if (bop == null) {
            throw new OdeFault("BindingOperation not found.");
        }

        BindingInput bi = bop.getBindingInput();
        if (bi == null) {
            throw new OdeFault("BindingInput not found.");
        }

        SOAPEnvelope soapEnv = msgCtx.getEnvelope();
        if (soapEnv == null) {
            soapEnv = soapFactory.getDefaultEnvelope();
            msgCtx.setEnvelope(soapEnv);
        }

//        createSoapHeaders(soapEnv, getSOAPHeaders(bi), op.getInput().getMessage(), message);

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = soapEnv.getBody() == null ?
                    soapFactory.createSOAPBody(soapEnv) : soapEnv.getBody();
            createSoapBody(sb, soapBody, op.getInput().getMessage(), message, op.getName());
        }

    }

    public void createSoapBody(org.apache.axiom.soap.SOAPBody sb, SOAPBody soapBody, Message msgDef,
                               Element message, String rpcWrapper) throws AxisFault {
        OMElement partHolder = isRPC ? soapFactory
                .createOMElement(new QName(soapBody.getNamespaceURI(), rpcWrapper, "odens"), sb) : sb;
        List<Part> parts = msgDef.getOrderedParts(soapBody.getParts());

        for (Part part : parts) {
            Element srcPartEl = DOMUtils.findChildByName(message, new QName(null, part.getName()));
            if (srcPartEl == null) {
                throw new AxisFault("Missing required part in ODE Message");
            }

            OMElement omPart = OMUtils.toOM(srcPartEl, soapFactory);
            if (isRPC) {
                partHolder.addChild(omPart);
            }
            else {
                for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext(); ) {
                    partHolder.addChild(i.next());
                }
            }
        }

    }

    public static String parseResponseFeedback(org.apache.axiom.soap.SOAPBody soapBody) throws FaultException {
        /*  Sample feedback response
        *   <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
            <soapenv:Body><part><axis2ns3:correlation xmlns:axis2ns3="http://wso2.org/humantask/feedback">
            <axis2ns3:taskid>10001</axis2ns3:taskid></axis2ns3:correlation></part></soapenv:Body></soapenv:Envelope>
        * */
        Iterator<OMElement> srcParts = soapBody.getChildElements();
        if (srcParts.hasNext()) {
            OMElement srcPart = srcParts.next();
            if (!srcPart.getQName().equals(new QName(null, "part"))) {
                throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                        "Unexpected element in SOAP body: " + srcPart));
            }
            OMElement hifb = srcPart.getFirstChildWithName(
                    new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                            BPEL4PeopleConstants.B4P_CORRELATION_HEADER));
            if (hifb == null) {
                throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                        "Unexpected element in SOAP body: " + srcPart));
            }
            OMElement taskIDele = hifb.getFirstChildWithName(
                    new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                            BPEL4PeopleConstants.B4P_CORRELATION_HEADER_ATTRIBUTE));
            if (taskIDele == null) {
                throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                        "Unexpected element in SOAP body: " + srcPart));
            }
            return taskIDele.getText();
//            Document doc = DOMUtils.newDocument();
//            Element destPart = doc.createElementNS(null, "part");
//            destPart.appendChild(doc.importNode(OMUtils.toDOM(srcPart), true));
//            message.setPart("part", destPart);
        }
        throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                "TaskID not found in the feedback message"));
    }

    public static SOAPBody getSOAPBody(ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, SOAPBody.class);
    }

    public static <T> T getFirstExtensibilityElement(ElementExtensible parent, Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();
    }
}