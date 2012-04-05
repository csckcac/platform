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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.wso2.carbon.bpel.b4p.extension.BPEL4PeopleConstants;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * TODO: Analyze the implementation copied from ODE source.
 */

public class SOAPHelper {
    private static Log log = LogFactory.getLog(SOAPHelper.class);

    private Binding binding;
    private String serviceName;
    private String portName;
    private SOAPFactory soapFactory;
    private boolean isRPC;
    private Definition wsdlDefintion;
    private Operation responseOperation;

    public SOAPHelper(Definition wsdlDef, Binding binding, String serviceName, String portName, SOAPFactory soapFactory,
                      boolean isRPC, Operation responseOperation) {
        this.binding = binding;
        this.serviceName = serviceName;
        this.portName = portName;
        this.soapFactory = soapFactory;
        this.isRPC = isRPC;
        this.wsdlDefintion = wsdlDef;
        this.responseOperation = responseOperation;
    }

    public SOAPHelper(Definition wsdlDef, Binding binding, String serviceName, String portName, SOAPFactory soapFactory,
                      boolean isRPC) {
        this.binding = binding;
        this.serviceName = serviceName;
        this.portName = portName;
        this.soapFactory = soapFactory;
        this.isRPC = isRPC;
        this.wsdlDefintion = wsdlDef;
        this.responseOperation = null;
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
            ExtensibilityElement result = (ExtensibilityElement) bindings.iterator().next();
            return result;
        }
    }

    public void createSoapResponse(MessageContext msgCtx, org.apache.ode.bpel.iapi.Message message, Operation op) throws AxisFault {
        if (op == null)
            throw new NullPointerException("Null operation");
        if (message == null)
            throw new NullPointerException("Null message.");
        if (msgCtx == null)
            throw new NullPointerException("Null msgCtx");

        BindingOperation bop = binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new AxisFault("BindingOperation not found.");

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new AxisFault("BindingOutput not found");

        SOAPEnvelope soapEnv = msgCtx.getEnvelope();
        if (soapEnv == null) {
            soapEnv = soapFactory.getDefaultEnvelope();
            msgCtx.setEnvelope(soapEnv);
        }

        if (message.getHeaderParts().size() > 0 || getSOAPHeaders(bo).size() > 0)
            createSoapHeaders(soapEnv, getSOAPHeaders(bo), op.getOutput().getMessage(), message);

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = soapEnv.getBody() == null ? soapFactory.createSOAPBody(soapEnv) : soapEnv.getBody();
            createSoapBody(sb, soapBody, op.getOutput().getMessage(), message.getMessage(), op.getName() + "Response");
        }
    }

    public void createSoapRequest(MessageContext msgCtx, Element message, Operation op) throws AxisFault{
        if (op == null)
            throw new NullPointerException("Null operation");
        // The message can be null if the input message has no part
        if (op.getInput().getMessage().getParts().size() > 0 && message == null)
            throw new NullPointerException("Null message.");
        if (msgCtx == null)
            throw new NullPointerException("Null msgCtx");

        BindingOperation bop = binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new OdeFault("BindingOperation not found.");

        BindingInput bi = bop.getBindingInput();
        if (bi == null)
            throw new OdeFault("BindingInput not found.");

        SOAPEnvelope soapEnv = msgCtx.getEnvelope();
        if (soapEnv == null) {
            soapEnv = soapFactory.getDefaultEnvelope();
            msgCtx.setEnvelope(soapEnv);
        }

//        createSoapHeaders(soapEnv, getSOAPHeaders(bi), op.getInput().getMessage(), message);

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = soapEnv.getBody() == null ? soapFactory.createSOAPBody(soapEnv) : soapEnv.getBody();
            createSoapBody(sb, soapBody, op.getInput().getMessage(), message, op.getName());
        }

    }

    @SuppressWarnings("unchecked")

    public void createSoapBody(org.apache.axiom.soap.SOAPBody sb, SOAPBody soapBody, Message msgDef,
                               Element message, String rpcWrapper) throws AxisFault {
        OMElement partHolder = isRPC ? soapFactory
                .createOMElement(new QName(soapBody.getNamespaceURI(), rpcWrapper, "odens"), sb) : sb;
        List<Part> parts = msgDef.getOrderedParts(soapBody.getParts());

        for (Part part : parts) {
            Element srcPartEl = DOMUtils.findChildByName(message, new QName(null, part.getName()));
            if (srcPartEl == null)
                throw new AxisFault("Missing required part in ODE Message");

            OMElement omPart = OMUtils.toOM(srcPartEl, soapFactory);
            if (isRPC) partHolder.addChild(omPart);
            else for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();) partHolder.addChild(i.next());
        }

    }

    public void createSoapHeaders(SOAPEnvelope soapEnv, List<SOAPHeader> headerDefs, Message msgdef, org.apache.ode.bpel.iapi.Message message) throws AxisFault {
        for (SOAPHeader sh : headerDefs) handleSoapHeaderDef(soapEnv, sh, msgdef, message);

        org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();
        if (soaphdr == null) soaphdr = soapFactory.createSOAPHeader(soapEnv);

        for (Node headerNode : message.getHeaderParts().values())
            if (headerNode.getNodeType() == Node.ELEMENT_NODE) {
                if (soaphdr.getFirstChildWithName(new QName(headerNode.getNamespaceURI(), headerNode.getLocalName())) == null) {
                    OMElement omHeaderNode = OMUtils.toOM((Element) headerNode, soapFactory);
                    SOAPHeaderBlock hb = soaphdr.addHeaderBlock(omHeaderNode.getLocalName(), omHeaderNode.getNamespace());

                    // add child elements
                    OMNode omNode = null;
                    for (Iterator iter = omHeaderNode.getChildren(); iter.hasNext();) {
                        omNode = (OMNode) iter.next();
                        hb.addChild(omNode);
                    }

                    OMAttribute omatribute = null;
                    // add attributes
                    for (Iterator iter = omHeaderNode.getAllAttributes(); iter.hasNext();) {
                        omatribute = (OMAttribute) iter.next();
                        hb.addAttribute(omatribute);
                    }
                }
            } else {
                throw new AxisFault("SOAP Header Must be an Element");
            }
    }

    @SuppressWarnings("unchecked")
    private void handleSoapHeaderDef(SOAPEnvelope soapEnv, SOAPHeader headerdef, Message msgdef, org.apache.ode.bpel.iapi.Message message) throws AxisFault {
        Map<String, Node> headers = message.getHeaderParts();
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgdef.getQName());

        if (headerdef.getPart() == null) return;

        if (payloadMessageHeader && msgdef.getPart(headerdef.getPart()) == null)
            throw new AxisFault("SOAP Header refer unknown part");

        Element srcPartEl = null;
        if (headers.size() > 0 && payloadMessageHeader) {
            try {
                srcPartEl = (Element) headers.get(headerdef.getPart());
            } catch (ClassCastException e) {
                throw new AxisFault("SOAP Header Must be an Element");
            }
        }

        // We don't complain about missing header data unless they are part of the message payload. This is
        // because AXIS may be providing these headers.
        if (srcPartEl == null && payloadMessageHeader) {
            if (message.getPart(headerdef.getPart()) != null) {
                srcPartEl = (Element) message.getPart(headerdef.getPart());
            } else {
                throw new AxisFault("Missing required part in ODE Message");
            }
        }

        if (srcPartEl == null) return;

        org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();
        if (soaphdr == null) {
            soaphdr = soapFactory.createSOAPHeader(soapEnv);
        }

        OMElement omPart = OMUtils.toOM(srcPartEl, soapFactory);
        for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();)
            soaphdr.addChild(i.next());
    }

    public void parseSoapResponse(org.apache.ode.bpel.iapi.Message odeMessage,
                                  SOAPEnvelope envelope, Operation op) throws AxisFault {
        // TODO: Refactor this.
        if (op == null)
            op = responseOperation;
        BindingOperation bop = binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new OdeFault("Binding Operation not found.");

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new OdeFault("Binding Output not found.");

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null)
             extractSoapBodyParts(odeMessage, envelope.getBody(), soapBody, op.getInput().getMessage(),
                     op.getName() + "Response");

//        if (envelope.getHeader() != null)
//            extractSoapHeaderParts(odeMessage, envelope.getHeader(), getSOAPHeaders(bo), op.getOutput().getMessage());
    }

    @SuppressWarnings("unchecked")
    public void extractSoapBodyParts(org.apache.ode.bpel.iapi.Message message, org.apache.axiom.soap.SOAPBody soapBody,
                                     SOAPBody bodyDef, Message msg,String rpcWrapper) throws AxisFault {

        List<Part> bodyParts = msg.getOrderedParts(bodyDef.getParts());

        if (isRPC) {
            QName rpcWrapQName = new QName(bodyDef.getNamespaceURI(), rpcWrapper);
            OMElement partWrapper = soapBody.getFirstChildWithName(rpcWrapQName);
            if (partWrapper == null)
                throw new OdeFault("Message body doesn't contain expected part wrapper.");
            // In RPC the body element is the operation name, wrapping parts. Order doesn't really matter as far as
            // we're concerned. All we need to do is copy the soap:body children, since doc-lit rpc looks the same
            // in ode and soap.
            for (Part pdef : bodyParts) {
                OMElement srcPart = partWrapper.getFirstChildWithName(new QName(null, pdef.getName()));
                if (srcPart == null)
                    throw new OdeFault("SOAP body doesn't contain required part.");
                message.setPart(srcPart.getLocalName(), OMUtils.toDOM(srcPart));
            }

        } else {
            // In doc-literal style, we expect the elements in the body to correspond (in order) to the
            // parts defined in the binding. All the parts should be element-typed, otherwise it is a mess.
            Iterator<OMElement> srcParts = soapBody.getChildElements();
            for (Part partDef : bodyParts) {
                if (!srcParts.hasNext())
                    throw new OdeFault("SOAP Mesaage body doesn't contain required part.");

                OMElement srcPart = srcParts.next();
                if (partDef.getElementName() == null)
                    throw new OdeFault("Binding defines non element doc list parts.");
                if (!srcPart.getQName().equals(partDef.getElementName()))
                    throw new OdeFault("Unexpected element in SOAP body");
                Document doc = DOMUtils.newDocument();
                Element destPart = doc.createElementNS(null, partDef.getName());
                destPart.appendChild(doc.importNode(OMUtils.toDOM(srcPart), true));
                message.setPart(partDef.getName(), destPart);
            }
        }
    }

    public static String parseResponseFeedback (org.apache.axiom.soap.SOAPBody soapBody) throws FaultException {
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

    public void extractSoapHeaderParts(org.apache.ode.bpel.iapi.Message message,
                                       org.apache.axiom.soap.SOAPHeader soapHeader,
                                       List<SOAPHeader> headerDefs, Message msg) throws AxisFault {
        // Checking that the definitions we have are at least there
        for (SOAPHeader headerDef : headerDefs)
            handleSoapHeaderPartDef(message, soapHeader, headerDef, msg);

        // Extracting whatever header elements we find in the message, binding and abstract parts
        // aren't reliable enough given what people do out there.
        Iterator headersIter = soapHeader.getChildElements();
        while (headersIter.hasNext()) {
            OMElement header = (OMElement) headersIter.next();
            String partName = findHeaderPartName(headerDefs, header.getQName());
            message.setHeaderPart(partName, OMUtils.toDOM(header));
        }
    }

    private void handleSoapHeaderPartDef(org.apache.ode.bpel.iapi.Message odeMessage, org.apache.axiom.soap.SOAPHeader header, SOAPHeader headerdef,
            Message msgType) throws AxisFault {
        // Is this header part of the "payload" messsage?
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgType.getQName());
        boolean requiredHeader = payloadMessageHeader || (headerdef.getRequired() != null && headerdef.getRequired());

        if (requiredHeader && header == null)
            throw new OdeFault("SOAP Header missing required element.");

        if (header == null)
            return;

        Message hdrMsg =wsdlDefintion.getMessage(headerdef.getMessage());
        if (hdrMsg == null)
            return;
        Part p = hdrMsg.getPart(headerdef.getPart());
        if (p == null || p.getElementName() == null)
            return;

        OMElement headerEl = header.getFirstChildWithName(p.getElementName());
        if (requiredHeader && headerEl == null)
            throw new OdeFault("SOAP Header missing required element.");

        if (headerEl == null) return;

        odeMessage.setHeaderPart(p.getName(), OMUtils.toDOM(headerEl));
    }

    private String findHeaderPartName(List<SOAPHeader> headerDefs, QName elmtName) {
        for (SOAPHeader headerDef : headerDefs) {
            Message hdrMsg = wsdlDefintion.getMessage(headerDef.getMessage());
            for (Object o : hdrMsg.getParts().values()) {
                Part p = (Part) o;
                if (p.getElementName().equals(elmtName)) return p.getName();
            }
        }
        return elmtName.getLocalPart();
    }

    public Fault parseSoapFault(Element odeMsgEl, SOAPEnvelope envelope, Operation operation) throws AxisFault {
        SOAPFault flt = envelope.getBody().getFault();
        SOAPFaultDetail detail = flt.getDetail();
        Fault fdef = inferFault(operation, flt);
        if (fdef == null)
            return null;

        Part pdef = (Part)fdef.getMessage().getParts().values().iterator().next();
        Element partel = odeMsgEl.getOwnerDocument().createElementNS(null,pdef.getName());
        odeMsgEl.appendChild(partel);

        if (detail.getFirstChildWithName(pdef.getElementName()) != null) {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(
                    OMUtils.toDOM(detail.getFirstChildWithName(pdef.getElementName())), true));
        } else {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(OMUtils.toDOM(detail),true));
        }

        return fdef;
    }

    private Fault inferFault(Operation operation, SOAPFault flt) {
        if (flt.getDetail() == null) {
            return null;
        }

        if(flt.getDetail().getFirstElement() == null){
            return null;
        }

        // The detail is a dummy <detail> node containing the interesting fault element
        QName elName = flt.getDetail().getFirstElement().getQName();
        return WsdlUtils.inferFault(operation, elName);
    }


    public SOAPFault createSoapFault(Element message, QName faultName, Operation op) throws AxisFault {
        OMElement detail = buildSoapDetail(message, faultName, op);

        SOAPFault fault = soapFactory.createSOAPFault();
        SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(Namespaces.SOAP_ENV_NS, "Server"));
        SOAPFaultReason reason = soapFactory.createSOAPFaultReason(fault);
        reason.setText(faultName);
        SOAPFaultDetail soapDetail = soapFactory.createSOAPFaultDetail(fault);
        if (detail != null)
            soapDetail.addDetailEntry(detail);
        return fault;
    }

    private OMElement buildSoapDetail(Element message, QName faultName, Operation op) throws AxisFault {
        if (faultName.getNamespaceURI() == null)
            return toFaultDetail(message);
        Fault f = op.getFault(faultName.getLocalPart());
        if (f == null)
            return toFaultDetail(message);

        // For faults, there will be exactly one part.
        Part p = (Part) f.getMessage().getParts().values().iterator().next();
        if (p == null)
            return toFaultDetail(message);
        Element partEl = DOMUtils.findChildByName(message, new QName(null, p.getName()));
        if (partEl == null)
            return toFaultDetail(message);
        Element detail = DOMUtils.findChildByName(partEl, p.getElementName());
        if (detail == null)
            return toFaultDetail(message);

        return OMUtils.toOM(detail, soapFactory);
    }

    private OMElement toFaultDetail(Element message) {
        if (message == null) return null;
        Element firstPart = DOMUtils.getFirstChildElement(message);
        if (firstPart == null) return null;
        Element detail = DOMUtils.getFirstChildElement(firstPart);
        if (detail == null) return OMUtils.toOM(firstPart, soapFactory);
        return OMUtils.toOM(detail, soapFactory);
    }

    public static SOAPBody getSOAPBody(ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, SOAPBody.class);
    }

    public static <T> T getFirstExtensibilityElement(ElementExtensible parent, Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();

    }

    @SuppressWarnings("unchecked")
    public static List<SOAPHeader> getSOAPHeaders(ElementExtensible eee) {
        return CollectionsX.filter(new ArrayList<SOAPHeader>(), (Collection<Object>) eee.getExtensibilityElements(),
                SOAPHeader.class);
    }

}