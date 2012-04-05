/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rule.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.wso2.carbon.rule.server.RuleEngine;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;

import java.util.List;

/**
 * The access point to rule engine and the receiving point for messages to be
 * adapted as facts and injected into  thr rule engine.
 * The rule engine invocation is synchronous
 */
public class RuleServiceInOutMessageReceiver extends AbstractInOutMessageReceiver {

    /**
     * To enrich the Axis2 Message Context with the results from the rule execution
     */
    private OutputManager outputManager;

    private RuleServiceInvoker serviceInvoker;

    public RuleServiceInOutMessageReceiver(RuleEngine ruleEngine,
                                           InputManager inputManager,
                                           OutputManager outputManager,
                                           SessionDescription sessionDescription) {
        this.serviceInvoker = new RuleServiceInvoker(ruleEngine, inputManager, sessionDescription);
        this.outputManager = outputManager;
    }

    public void invokeBusinessLogic(MessageContext requestMessageContext,
                                    MessageContext responseMessageContext) throws AxisFault {

        SOAPFactory fac = getSOAPFactory(requestMessageContext);
        AxisOperation op = requestMessageContext.getOperationContext().getAxisOperation();
        AxisService service = requestMessageContext.getAxisService();

        // Handling the response
        OMElement bodyContent;
        AxisMessage outMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        if (outMessage.getElementQName() != null) {
            bodyContent = fac.createOMElement(outMessage.getElementQName());
        } else {
            bodyContent =
                    fac.createOMElement(outMessage.getName(),
                            fac.createOMNamespace(service.getTargetNamespace(),
                                    service.getSchemaTargetNamespacePrefix()));
        }

        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(bodyContent);
        responseMessageContext.setEnvelope(envelope);

        List results = serviceInvoker.invoke(requestMessageContext);

        if (!results.isEmpty()) {
            outputManager.processOutputs(results, responseMessageContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There is no results from the rule engine execution");
            }
        }
    }
}

