/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.internal.StatisticsServiceComponent;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class StatisticsHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(StatisticsHandler.class);

    @Override
    public Handler.InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        SystemStatisticsUtil systemStatisticsUtil;
        SystemStatistics systemStatistics;
        ServiceStatistics serviceStatistics;
        OperationStatistics operationStatistics;

        try {
            AxisConfiguration axisConfiguration = msgContext.getConfigurationContext().getAxisConfiguration();
            int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            Map<Integer, EventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
            EventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);

            //Check service stats enable -- if true -- go
            if (eventingConfigData != null && eventingConfigData.isServiceStatsEnable()) {

                systemStatisticsUtil = StatisticsServiceComponent.getSystemStatisticsUtil();
                systemStatistics = systemStatisticsUtil.getSystemStatistics(axisConfiguration);
                AxisOperation axisOperation = msgContext.getAxisOperation();
                if (axisOperation != null) {
                    operationStatistics = systemStatisticsUtil.getOperationStatistics(
                            axisOperation);
                    if (operationStatistics.getTotalRequestCount() == 0) {
                        return Handler.InvocationResponse.CONTINUE;
                    }
                }//In very rare cases msgContext.getAxisOperation() becomes null, then we can't get statistics.
                else {
                    return Handler.InvocationResponse.CONTINUE;
                }

/*                MessageContext currentMessageContext = MessageContext.getCurrentMessageContext();
                if (currentMessageContext != null) {
                    timestamp = new Timestamp(Long.parseLong(currentMessageContext.getProperty(
                            StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                    // Check if already done and skip
                    Object requestProperty = currentMessageContext.getProperty(
                            HTTPConstants.MC_HTTP_SERVLETREQUEST);
                    extractInfoFromHttpHeaders(eventData, requestProperty);
                } else {
                    Date date = new Date();
                    timestamp = new Timestamp(date.getTime());
                }*/

                MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

                //If already set in the activity handlers get it or create new publish data
                PublishData publishData = (PublishData) msgContext.getProperty(
                        BAMDataPublisherConstants.PUBLISH_DATA);

                EventData eventData;
                if (publishData != null) {
                    eventData = publishData.getEventData();
                } else {
                    publishData = new PublishData();
                    eventData = new EventData();
                }


                // Skip resetting same info if already set by activity in/out handlers
                if (!eventingConfigData.isMsgDumpingEnable()) {

                    Timestamp timestamp = null;
                    if (inMessageContext != null) {
                        timestamp = new Timestamp(Long.parseLong(inMessageContext.getProperty(
                                StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                        Object requestProperty = inMessageContext.getProperty(
                                HTTPConstants.MC_HTTP_SERVLETREQUEST);
                        ServiceAgentUtil.extractInfoFromHttpHeaders(eventData, requestProperty);
                    } else {
                        Date date = new Date();
                        timestamp = new Timestamp(date.getTime());
                    }

                    eventData.setTimestamp(timestamp);
                    eventData.setOperationName(axisOperation.getName().getLocalPart());
                    eventData.setServiceName(msgContext.getAxisService().getName());

                }

                serviceStatistics = systemStatisticsUtil.getServiceStatistics(msgContext.getAxisService());

                eventData.setSystemStatistics(systemStatistics);
                eventData.setServiceStatistics(serviceStatistics);
                eventData.setOperationStatistics(operationStatistics);
/*                eventData.setOperationName(axisOperation.getName().getLocalPart());
                eventData.setServiceName(msgContext.getAxisService().getName());
                eventData.setTimestamp(timestamp);*/

                publishData.setEventData(eventData);

                // Skip if bam server info already set at activity handlers
                if (!eventingConfigData.isMsgDumpingEnable()) {
                    BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                    publishData.setBamServerInfo(bamServerInfo);
                }

                Event event = ServiceAgentUtil.makeEventList(publishData, eventingConfigData);
            }
        } catch (Throwable ignore) {
            log.error("Error at SystemStatisticsHandler. " +
                      "But continuing message processing for message id: " +
                      msgContext.getMessageID(), ignore);
        }

        return Handler.InvocationResponse.CONTINUE;
    }


}
