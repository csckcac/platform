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
package org.wso2.carbon.bam.service.data.publisher.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.data.publisher.data.ActivityData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.publish.StatsProcessor;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class QueueWorker implements Runnable {

    private static Log log = LogFactory.getLog(QueueWorker.class);

    private Queue<PublishData> statisticsQueue;
    private StatsProcessor serviceStatsProcessor;

    public QueueWorker(Queue<PublishData> statisticsQueue,
                       StatsProcessor serviceStatsProcessor) {
        this.statisticsQueue = statisticsQueue;
        this.serviceStatsProcessor = serviceStatsProcessor;
    }

    public void run() {
        clearStatisticDataInQueue(statisticsQueue.size());
    }

    private void clearStatisticDataInQueue(int size) {
        if (log.isDebugEnabled()) {
            log.debug("Number of events in queue : " + size);
        }
        ArrayList<Event> eventList = new ArrayList<Event>();
        BAMServerInfo bamServerInfo = null;
        for (int i = 0; i < size; i++) {
            PublishData publishData = statisticsQueue.poll();
            //Sometimes other thread may get the last queue object
            if (publishData != null) {
                Event event = makeEventObject(publishData);
                eventList.add(event);
                bamServerInfo = publishData.getBamServerInfo();
            }
        }
        if (eventList.size() > 0) {
            serviceStatsProcessor.process(eventList, bamServerInfo);
        }
    }

    private Event makeEventObject(PublishData publishData) {

        EventData event = publishData.getEventData();

        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        addEventData(eventData, event);
        addMetaData(metaData, event);
        addCorrelationData(correlationData, event);


        Event publishEvent = new Event();
        publishEvent.setCorrelation(correlationData);
        publishEvent.setMeta(metaData);
        publishEvent.setEvent(eventData);

        return publishEvent;
    }

    private void addEventData(Map<String, ByteBuffer> eventData,
                              EventData eventInfo) {

        SystemStatistics systemStatistics = eventInfo.getSystemStatistics();
        if (systemStatistics != null) {
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.SERVER_NAME,
                           systemStatistics.getServerName());
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.TOTAL_SYSTEM_AVG_RESPONSE_TIME,
                           Double.toString(systemStatistics.getAvgResponseTime()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.TOTAL_SYSTEM_MIN_RESPONSE_TIME,
                           Long.toString(systemStatistics.getMinResponseTime()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.TOTAL_SYSTEM_MAX_RESPONSE_TIME,
                           Long.toString(systemStatistics.getMaxResponseTime()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.TOTAL_SYSTEM_REQUEST_COUNT,
                           Integer.toString(systemStatistics.getTotalRequestCount()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.TOTAL_SYSTEM_RESPONSE_COUNT,
                           Integer.toString(systemStatistics.getTotalResponseCount()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.RESPONSE_TIME,
                           Long.toString(systemStatistics.getCurrentInvocationResponseTime()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.REQUEST_COUNT,
                           Integer.toString(systemStatistics.getCurrentInvocationRequestCount()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.RESPONSE_COUNT,
                           Integer.toString(systemStatistics.getCurrentInvocationResponseCount()));
            putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.FAULT_COUNT,
                           Integer.toString(systemStatistics.getCurrentInvocationFaultCount()));
        }

        putDataIntoMap(eventData, BAMDataPublisherConstants.SERVICE_NAME,
                       eventInfo.getServiceName());
        putDataIntoMap(eventData, BAMDataPublisherConstants.OPERATION_NAME,
                       eventInfo.getOperationName());
        putDataIntoMap(eventData, BAMDataPublisherConstants.TIMESTAMP,
                       eventInfo.getTimestamp().toString());

        putDataIntoMap(eventData, BAMDataPublisherConstants.IN_MSG_BODY,
                       eventInfo.getInMessageBody());
        putDataIntoMap(eventData, BAMDataPublisherConstants.OUT_MSG_BODY,
                       eventInfo.getOutMessageBody());

    }

    private void addMetaData(Map<String, ByteBuffer> metaData, EventData eventData) {

        putDataIntoMap(metaData, BAMDataPublisherConstants.REMOTE_ADDRESS,
                       eventData.getRemoteAddress());
        putDataIntoMap(metaData, BAMDataPublisherConstants.HOST,
                       eventData.getHost());
        putDataIntoMap(metaData, BAMDataPublisherConstants.CONTENT_TYPE,
                       eventData.getContentType());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REFERER,
                       eventData.getReferer());
        putDataIntoMap(metaData, BAMDataPublisherConstants.USER_AGENT,
                       eventData.getUserAgent());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REQUEST_URL,
                       eventData.getRequestURL());
    }

    private void addCorrelationData(Map<String, ByteBuffer> correlationData,
                                    EventData eventData) {
        putDataIntoMap(correlationData, BAMDataPublisherConstants.MSG_ACTIVITY_ID,
                       eventData.getActivityId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.IN_MSG_ID,
                       eventData.getInMessageId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.OUT_MSG_ID,
                       eventData.getOutMessageId());
    }

    private void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }

}
