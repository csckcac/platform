/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.data.publisher.servicestats;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.servicestats.data.EventData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.OperationStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.ServiceStatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.data.StatisticData;
import org.wso2.carbon.bam.data.publisher.servicestats.internal.StatisticsServiceComponent;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventPublisher implements ServiceStatsProcessor {

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void process(StatisticData[] statisticData) {
//        EventBroker broker = StatisticsServiceComponent.getEventBroker();
//        Message message = new Message();

        OMElement eventPayLoad = null;
        for (StatisticData statistic : statisticData) {
            eventPayLoad = constructEventPayLoad(statistic);
//            message.setMessage(eventPayLoad);
            try {
                SuperTenantCarbonContext.startTenantFlow();
                int tenantId = SuperTenantCarbonContext.getCurrentContext(StatisticsServiceComponent.getConfigurationContext()).getTenantId();
                SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
                SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

                ServiceHolder.getLWEventBroker().publish(ServiceStatisticsPublisherConstants.SERVICE_STATS_TOPIC, eventPayLoad);
//                broker.publishRobust(message, ServiceStatisticsPublisherConstants.BAM_REG_PATH);

            } catch (Exception e) {
                log.error("Can not publish the message ", e);
            } finally {
                SuperTenantCarbonContext.endTenantFlow();
            }
//            try {
//                if (broker != null) {
//
//                    broker.publishRobust(message, ServiceStatisticsPublisherConstants.BAM_REG_PATH);
//                    if (log.isDebugEnabled()) {
//                        log.debug("Event is published" + message.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("EventPublisher - Unable to publish event", e);
//            }
        }

    }

    private OMElement constructEventPayLoad(StatisticData statData) {
        SystemStatistics systemStatistics = statData.getSystemStatistics();
        Collection<OperationStatisticData> operationStatisticsList = statData.getOperationStatisticsList();
        Collection<ServiceStatisticData> serviceStatisticsList = statData.getServiceStatisticsList();
        Timestamp timestamp = statData.getTimestamp();

        List<EventData> opData = new ArrayList<EventData>();
        List<EventData> serviceData = new ArrayList<EventData>();

        for (OperationStatisticData operationStats : operationStatisticsList) {
            if (operationStats.isUpdateFlag()) {
                opData.add(PublisherUtils.getOperationEventData(operationStats, timestamp));
            }
        }
        for (ServiceStatisticData serviceStats : serviceStatisticsList) {
            if (serviceStats.isUpdateFlag()) {
                serviceData.add(PublisherUtils.getServiceEventData(serviceStats, timestamp));
            }
        }
        MessageContext msgContext = statData.getMsgCtxOfStatData();
        EventData systemData = PublisherUtils.getSystemEventData(statData, systemStatistics);
        OMElement payLoad = PublisherUtils.getEventPayload(msgContext, systemData, serviceData, opData);

        return payLoad;
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Terminating BAM EventPublisher");
        }
    }
}
