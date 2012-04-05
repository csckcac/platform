/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.axis2.receivers;

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.BPELProcessProxy;
import org.wso2.carbon.bpel.core.ode.integration.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.utils.BPELMessageContextFactory;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import static org.wso2.carbon.bpel.core.ode.integration.utils.BPELMessageContextFactory.hasResponse;

/**
 * Axis Engine will handover the incoming message to a service exposed by
 * BPEL process to this. This will handover the message to the BPELProcessProxy after doing
 * some pre processing.
 * <p/>
 * The fact that "BPELMessageReceiver extends AbstractMessageReceiver" explains most of the
 * basic things.
 */
public class BPELMessageReceiver extends AbstractInMessageReceiver {
    private static Log log = LogFactory.getLog(BPELMessageReceiver.class);
    private static Log messageTraceLog = LogFactory.getLog(BPELConstants.MESSAGE_TRACE);

    private BPELProcessProxy processProxy;

    protected final void invokeBusinessLogic(final MessageContext inMessageContext)
            throws AxisFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        if (messageTraceLog.isDebugEnabled()) {
           /* messageTraceLog.debug("Message received: Message Id: " +
                                  inMessageContext.getMessageID() +  " :: " +*/
            messageTraceLog.debug("Message received: " +
                                  inMessageContext.getAxisService().getName() + "." +
                                  inMessageContext.getAxisOperation().getName());
            if (messageTraceLog.isTraceEnabled()) {
                /*messageTraceLog.trace("Request message: Message Id: " +
                                      inMessageContext.getMessageID() + " :: " +*/
                 messageTraceLog.trace("Request message: " +
                                      inMessageContext.getEnvelope());
            }
        }

        SOAPFactory soapFactory = getSOAPFactory(inMessageContext);
        final BPELMessageContext bpelMessageContext = BPELMessageContextFactory.createBPELMessageContext(
                inMessageContext,
                processProxy,
                soapFactory);

        if (hasResponse(inMessageContext.getAxisOperation())) {
            handleInOutOperation(bpelMessageContext);
            if (messageTraceLog.isDebugEnabled()) {
                messageTraceLog.debug("Reply Sent: " +
                                      inMessageContext.getAxisService().getName() + "." +
                                      inMessageContext.getAxisOperation().getName());
                if (messageTraceLog.isTraceEnabled()) {
                    messageTraceLog.trace("Response message: " +
                                          bpelMessageContext.getOutMessageContext().getEnvelope());
                }
            }
        } else {
            handleInOnlyOperation(bpelMessageContext);
        }
    }

    public final void setProcessProxy(final BPELProcessProxy processProxy) {
        this.processProxy = processProxy;
    }

    private void handleInOutOperation(BPELMessageContext bpelMessageContext) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Received request message for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
        }

        processProxy.onAxisServiceInvoke(bpelMessageContext);

        if (log.isDebugEnabled()) {
            log.debug("Reply for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
            log.debug("\tReply message "
                    + bpelMessageContext.getOutMessageContext().getEnvelope());
        }

        AxisEngine.send(bpelMessageContext.getOutMessageContext());
    }

    private void handleInOnlyOperation(BPELMessageContext bpelMessageContext) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Received one-way message for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
        }

        processProxy.onAxisServiceInvoke(bpelMessageContext);
    }


}
