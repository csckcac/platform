/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bam.data.publisher.clientstats.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.bam.data.publisher.clientstats.ResponseTimeProcessor;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;

/**
 * Handler to compute the response time. Followed system statistics module logic.
 */
public class ResponseTimeHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(ResponseTimeHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        String service = "";
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY) != null) {
            service = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY)
                    .toString();
        } else {
            log.error("ServiceName property not found");
        }

        if (service == null) {
            return InvocationResponse.CONTINUE;
        }

        calculateResponseTimes(msgContext);
        return InvocationResponse.CONTINUE;
    }

    // ??
    public void flowComplete(MessageContext msgContext) {
        if (msgContext == null) {
            return;
        }

        AxisService axisService = msgContext.getAxisService();
        if (axisService == null
                || SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup())) {
            return;
        }
        OperationContext opContext = msgContext.getOperationContext();
        if (opContext != null) {
            AxisOperation axisOp = opContext.getAxisOperation();
            if (axisOp != null) {
                String mep = axisOp.getMessageExchangePattern();
                if (mep != null
                        && (mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) || mep
                                .equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY))) {
                    try {
                        calculateResponseTimes(msgContext);
                    } catch (AxisFault axisFault) {
                        log.error("Cannot compute reponse times", axisFault);
                    }
                }
            }
        }
    }

    private void calculateResponseTimes(MessageContext msgContext) throws AxisFault {
        String operation = "";
        String service = "";
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY) != null) {
            operation = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY)
                    .toString();
        } else {
            log.error("OperationName property not found");
        }

        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY) != null) {
            service = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY)
                    .toString();
        } else {
            log.error("ServiceName property not found");
        }

        OperationContext opctx = msgContext.getOperationContext();
        if (opctx != null) {
            MessageContext inMsgCtx = opctx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMsgCtx != null) {
                Object receivedTime = inMsgCtx
                        .getConfigurationContext()
                        .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_REQUEST_RECEIVED_TIME_PROPERTY);
                if (receivedTime != null) {
                    long responseTime = System.currentTimeMillis()
                            - Long.parseLong(receivedTime.toString());

                    // Handle global reponse time
                    Object globalReqCounterObj = inMsgCtx
                            .getConfigurationContext()
                            .getProperty(
                                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_REQUEST_COUNTER_PROPERTY);
                    int globalReqCount = 0;
                    if (globalReqCounterObj != null) {
                        if (globalReqCounterObj instanceof Counter) {
                        globalReqCount = ((Counter) globalReqCounterObj).getCount();
                        }
                    }

                    Object responseTimeProcObj = inMsgCtx
                            .getConfigurationContext()
                            .getProperty(
                                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY);
                    if (responseTimeProcObj != null) {

                        if (responseTimeProcObj instanceof ResponseTimeProcessor) {
                            ((ResponseTimeProcessor) responseTimeProcObj).addResponseTime(
                                    responseTime,
                                    globalReqCount, msgContext);
                        }
                    }

                    // handle Service response time
                    ResponseTimeProcessor proc = new ResponseTimeProcessor();
                    if (service != null) {
                        Object responseTimeProcessorObj = null;
                        Object serviceCountObj = msgContext
                                .getConfigurationContext()
                                .getProperty(
                                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_REQUEST_COUNTER_PROPERTY);
                        try {
                       responseTimeProcessorObj = msgContext
                                .getConfigurationContext()
                                .getProperty(
                                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int serviceCount = 0;
                        if (serviceCountObj != null) {
                            if (serviceCountObj instanceof Counter) {
                            serviceCount = ((Counter) serviceCountObj).getCount();
                            }
                        }
                        if (responseTimeProcessorObj != null) {
                            proc.addResponseTime(responseTime, serviceCount, msgContext);
                        } else {

                            proc.addResponseTime(responseTime, serviceCount, msgContext);
                            msgContext
                                    .getConfigurationContext()
                                    .setProperty(
                                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY,
                                            proc);
                        }
                    }

                    // Handle operation response time

                    if (operation != null) {
                        Object opeResTimeprocessorObj = msgContext
                                .getConfigurationContext()
                                .getProperty(
                                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY);
                        Object opReqCounterObj = msgContext
                                .getConfigurationContext()
                                .getProperty(
                                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_IN_OPERATION_COUNTER_PROPERTY);
                        int opReqCount = 0;
                        if (opReqCounterObj != null) {
                            if (opReqCounterObj instanceof Counter) {
                            opReqCount = ((Counter) opReqCounterObj).getCount();
                            }
                        }
                        if (opeResTimeprocessorObj != null) {
                            proc.addResponseTime(responseTime, opReqCount, msgContext);
                        } else {
                            proc.addResponseTime(responseTime, opReqCount, msgContext);
                            msgContext
                                    .getConfigurationContext()
                                    .setProperty(
                                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY,
                                            proc);

                        }
                    }

                }
            }
        }
    }
}
