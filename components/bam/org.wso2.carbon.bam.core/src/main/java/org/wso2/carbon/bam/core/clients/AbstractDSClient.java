/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.core.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.wso2.carbon.dataservices.core.DBInOnlyMessageReceiver;
import org.wso2.carbon.dataservices.core.DBInOutMessageReceiver;
import org.wso2.carbon.utils.ConfigurationContextService;

public abstract class AbstractDSClient<T extends Stub> extends AbstractAdminClient<T> {
    protected ConfigurationContextService ccService;
    protected enum MEP {
        INONLY, INOUT
    } ;

    protected OMElement invokeDataServiceOperation(String opName, OMElement soapBody, MEP mep) throws AxisFault, XMLStreamException {
        MessageReceiver msgReceiver;
        if (mep == MEP.INOUT) {
            msgReceiver = new DBInOutMessageReceiver();
        } else {
            msgReceiver = new DBInOnlyMessageReceiver();
        }

        MessageContext msgCtx = buildMessageContext(opName);
        SOAPFactory soapFactory = new SOAP12Factory();
        SOAPEnvelope env = soapFactory.getDefaultEnvelope();
        env.getBody().addChild(soapBody);
        msgCtx.setEnvelope(env);

        OMElement bodyEle = null;

        if (mep == MEP.INOUT) {
            MessageContext retMsgCtx = new MessageContext();
            ((DBInOutMessageReceiver) msgReceiver).invokeBusinessLogic(msgCtx, retMsgCtx);
            bodyEle = retMsgCtx.getEnvelope().getBody();
        } else {   // MEP.INONLY
            ((DBInOnlyMessageReceiver) msgReceiver).invokeBusinessLogic(msgCtx);
        }
        return bodyEle;
    }

    protected abstract AxisService getAxisService() throws AxisFault;

    protected static AxisOperation getAxisOperation(AxisService svc, String opName) {
        return svc.getOperationByAction(opName);
    }

    protected MessageContext buildMessageContext(String opName) throws AxisFault {
        MessageContext msgCtx = new MessageContext();
        AxisService svc = getAxisService();
        msgCtx.setAxisService(svc);
        msgCtx.setAxisOperation(getAxisOperation(svc, opName));
        return msgCtx;
    }

    protected static java.util.Map getOMElementNamespaces(OMElement element) {
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = element.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

}
