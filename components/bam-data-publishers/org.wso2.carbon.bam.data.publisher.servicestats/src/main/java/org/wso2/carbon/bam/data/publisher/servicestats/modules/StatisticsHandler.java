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
package org.wso2.carbon.bam.data.publisher.servicestats.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.servicestats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.servicestats.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.data.publisher.servicestats.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.OperationStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.ServiceStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.StatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.internal.StatisticsServiceComponent;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class StatisticsHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(StatisticsHandler.class);

    @Override
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        try {

            SystemStatisticsUtil systemStatisticsUtil = StatisticsServiceComponent.getSystemStatisticsUtil();
            EventingConfigData eventingConfigData = StatisticsServiceComponent.getServiceStatisticsPublisherAdmin()
                    .getEventingConfigData();

            if (eventingConfigData != null && eventingConfigData.eventingEnabled()
                && eventingConfigData.getSystemRequestCountThreshold() > 0) {

                AxisConfiguration axisConfiguration = msgContext.getConfigurationContext().getAxisConfiguration();
                SystemStatistics systemStatistics = systemStatisticsUtil.getSystemStatistics(axisConfiguration);

                if (msgContext.getAxisOperation() != null) {
                    OperationStatistics operationStatistics = systemStatisticsUtil.getOperationStatistics(
                            msgContext.getAxisOperation());
                    if (operationStatistics.getTotalRequestCount() == 0) {
                        return InvocationResponse.CONTINUE;
                    }
                }//In very rare cases msgContext.getAxisOperation() becomes null, then we can't get statistics.
                 else {
                    return InvocationResponse.CONTINUE;
                }

                Collection<ServiceStatisticData> latestServiceStats = updateAndGetLatestServiceStatistics(
                        systemStatisticsUtil,
                        msgContext);
                Collection<OperationStatisticData> latestOperationStats = updateAndGetLatestOperationStatistics(
                        systemStatisticsUtil,
                        msgContext);

                Parameter lastCountParameter = axisConfiguration.getParameter(ServiceStatisticsPublisherConstants.STATISTICS_LAST_COUNT);
                int lastCount = 0;
                if(lastCountParameter!=null){
                    lastCount =(Integer)lastCountParameter.getValue();
                }
                // Current hit count for system
                int currentCount = systemStatistics.getTotalRequestCount();


                if (currentCount - lastCount > eventingConfigData.getSystemRequestCountThreshold()) {

                    lastCountParameter = new Parameter();
                    lastCountParameter.setName(ServiceStatisticsPublisherConstants.STATISTICS_LAST_COUNT);
                    lastCountParameter.setValue(currentCount);
                    axisConfiguration.addParameter(lastCountParameter);

                    getStatisticAndPublish(msgContext, systemStatistics, latestServiceStats, latestOperationStats);
                }

            }

        } catch (Throwable ignore) {
            log.error("Error at SystemStatisticsHandler. " +
                      "But continuing message processing for message id: " +
                      msgContext.getMessageID(), ignore);
        }

        return InvocationResponse.CONTINUE;
    }

    private void getStatisticAndPublish(MessageContext msgContext,
                                        SystemStatistics systemStatistics,
                                        Collection<ServiceStatisticData> latestServiceStats,
                                        Collection<OperationStatisticData> latestOperationStats) {
        StatisticData statisticData = new StatisticData();
        statisticData.setMsgCtxOfStatData(msgContext);
        statisticData.setSystemStatistics(systemStatistics);
        statisticData.setOperationStatisticsList(latestOperationStats);
        statisticData.setServiceStatisticsList(latestServiceStats);
        java.util.Date currentDate = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
        statisticData.setTimestamp(timestamp);

        try {
            PublisherUtils.publishEvent(statisticData);
        } catch (Exception e) {
            log.error("StatisticsHandler - Unable to send event for message" + " message id: " +
                      msgContext.getMessageID(), e);
        }
    }


    private Collection<ServiceStatisticData> updateAndGetLatestServiceStatistics(
            SystemStatisticsUtil systemStatisticsUtil, MessageContext msgContext) throws AxisFault {
        AxisConfiguration axisConfig = msgContext.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfig).getTenantId();

        String serviceName = msgContext.getAxisService().getName();

        ServiceStatistics serviceStatistics = systemStatisticsUtil.getServiceStatistics(msgContext.getAxisService());

        ServiceStatisticData serviceStatisticData = new ServiceStatisticData();
        serviceStatisticData.setServiceStatistics(serviceStatistics);
        serviceStatisticData.setServiceName(serviceName);
        serviceStatisticData.setTenantId(tenantID);
        //This flag is used to identify which services has updated from last triggered event.
        serviceStatisticData.setUpdateFlag(true);


        Collection<ServiceStatisticData> newCollection = new ArrayList<ServiceStatisticData>();
        Map<String, ServiceStatisticData> serviceStats = new HashMap<String, ServiceStatisticData>();

        Parameter serviceStatsParameter = axisConfig.getParameter(ServiceStatisticsPublisherConstants.STATISTICS_SERVICE_NAME);

        if (serviceStatsParameter == null) {
            synchronized (this) {
                if (serviceStatsParameter == null) {
                    serviceStats.put(serviceName, serviceStatisticData);
                    serviceStatsParameter = new Parameter();
                    serviceStatsParameter.setName(ServiceStatisticsPublisherConstants.STATISTICS_SERVICE_NAME);
                    serviceStatsParameter.setValue(serviceStats);
                    axisConfig.addParameter(serviceStatsParameter);
                } else {
                    serviceStats = (Map<String, ServiceStatisticData>) serviceStatsParameter.getValue();
                    serviceStats.put(serviceName, serviceStatisticData);
                }
            }
        } else {
            serviceStats = (Map<String, ServiceStatisticData>) serviceStatsParameter.getValue();
            serviceStats.put(serviceName, serviceStatisticData);
        }

        //Keep a copy otherwise data will change later
        newCollection.addAll(serviceStats.values());
        return newCollection;
    }

    private Collection<OperationStatisticData> updateAndGetLatestOperationStatistics(
            SystemStatisticsUtil systemStatisticsUtil, MessageContext msgContext) throws AxisFault {

        AxisConfiguration axisConfig = msgContext.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfig).getTenantId();

        String operationName = msgContext.getAxisService().getName() + "-"
                               + msgContext.getAxisOperation().getName().getLocalPart();
        OperationStatistics operationStatistics = systemStatisticsUtil.getOperationStatistics(msgContext.getAxisOperation());

        OperationStatisticData operationStatisticData = new OperationStatisticData();
        operationStatisticData.setOperationStatistics(operationStatistics);
        operationStatisticData.setTenantId(tenantID);
        operationStatisticData.setServiceName(msgContext.getAxisService().getName());
        operationStatisticData.setOperationName(msgContext.getAxisOperation().getName().getLocalPart());
        operationStatisticData.setUpdateFlag(true);

        Collection<OperationStatisticData> newCollection = new ArrayList<OperationStatisticData>();
        Map<String, OperationStatisticData> operationStats = new HashMap<String, OperationStatisticData>();

        Parameter operationStatsParameter = axisConfig.getParameter(ServiceStatisticsPublisherConstants.STATISTICS_OPERATION_NAME);

        if (operationStatsParameter == null) {
            synchronized (this) {
                if (operationStatsParameter == null) {
                    operationStats.put(operationName,operationStatisticData);
                    operationStatsParameter = new Parameter();
                    operationStatsParameter.setName(ServiceStatisticsPublisherConstants.STATISTICS_OPERATION_NAME);
                    operationStatsParameter.setValue(operationStats);
                    axisConfig.addParameter(operationStatsParameter);
                } else {
                    operationStats = (Map<String, OperationStatisticData>) operationStatsParameter.getValue();
                    operationStats.put(operationName,operationStatisticData);
                }
            }
        } else {
            operationStats = (Map<String, OperationStatisticData>) operationStatsParameter.getValue();
            operationStats.put(operationName, operationStatisticData);
        }

        //Keep a copy otherwise data will change later
        newCollection.addAll(operationStats.values());
        return newCollection;
    }
}
