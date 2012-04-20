/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.integration;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.WSDLAwareSOAPProcessor;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

/**
 * Message receiver for the humantasks exposed as services.
 */
public class AxisHumanTaskMessageReceiver extends AbstractMessageReceiver {
    private static Log log = LogFactory.getLog(AxisHumanTaskMessageReceiver.class);

    private HumanTaskEngine humanTaskEngine;

    @Override
    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        if (log.isDebugEnabled()) {
            if (messageContext != null) {
                log.debug("Message received: " + messageContext.getEnvelope());
            } else {
                log.debug("Message Context is not available.");
            }
        }

        if (messageContext == null) {
            // TODO Handle exception
            log.error("Message context is null");
            return;
        }

        WSDLAwareSOAPProcessor soapProcessor = new WSDLAwareSOAPProcessor(messageContext);
        String taskId;
        try {
            taskId = humanTaskEngine.invoke(soapProcessor.parseRequest());
        } catch (HumanTaskException e) {
            //TODO handle exception
            log.error("Task creation failed.", e);
            return;
        } catch (Exception e) {
            //TODO handle exception
            log.error("Task creation failed.", e);
            return;
        }

        if (taskId != null) {
            if (hasResponse(messageContext.getAxisOperation())) {
                //Task Creation
                MessageContext outMessageContext = MessageContextBuilder.createOutMessageContext(messageContext);
                outMessageContext.getOperationContext().addMessageContext(outMessageContext);

                SOAPEnvelope envelope = getSOAPFactory(messageContext).getDefaultEnvelope();
                envelope.getBody().addChild(getFeedbackPayLoad(taskId));
                outMessageContext.setEnvelope(envelope);
                AxisEngine.send(outMessageContext);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("[HT] Notification request received.");
                }
                //Notification
            }
        } else {
            //TODO
            throw new UnsupportedOperationException("This operation is not currently supported in this version of WSO2 BPS.");
        }
    }

    public void setHumanTaskEngine(HumanTaskEngine humanTaskEngine) {
        this.humanTaskEngine = humanTaskEngine;
    }

    private boolean hasResponse(AxisOperation op) {
        switch (op.getAxisSpecificMEPConstant()) {
            case WSDLConstants.MEP_CONSTANT_IN_OUT:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_ONLY:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN:
                return true;
            case WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY:
                return true;
            default:
                return false;
        }
    }

    private OMElement getFeedbackPayLoad(String taskID) {
        OMFactory fbOMFactory = OMAbstractFactory.getOMFactory();
        OMElement payLoadEle = fbOMFactory.createOMElement("part", null);
        OMElement hiFeedbackEle = fbOMFactory.createOMElement(HumanTaskConstants.B4P_CORRELATION_HEADER,
                fbOMFactory.createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, null),
                payLoadEle);
        OMElement taskIDEle =
                fbOMFactory.createOMElement(HumanTaskConstants.B4P_CORRELATION_HEADER_ATTRIBUTE,
                        fbOMFactory.createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, null),
                        hiFeedbackEle);
        taskIDEle.setText(taskID);
        return payLoadEle;
    }
}
