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
package org.wso2.carbon.bam.service.data.publisher.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.data.StatisticData;
import org.wso2.carbon.bam.service.data.publisher.publish.StatsProcessor;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


public class ServiceStatsWorker implements Runnable {

    private static Log log = LogFactory.getLog(ServiceStatsWorker.class);

    private Queue<PublishData> statisticsQueue;
    private StatsProcessor serviceStatsProcessor;

    public ServiceStatsWorker(Queue<PublishData> statisticsQueue,
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

        StatisticData statisticData = publishData.getStatisticData();

        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        addDataIntoEventData(eventData, statisticData);
        addDataIntoMetaData(metaData, statisticData);

        Event event = new Event();
        event.setCorrelation(correlationData);
        event.setMeta(metaData);
        event.setEvent(eventData);

        return event;
    }

    private void addDataIntoMetaData(Map<String, ByteBuffer> metaData,
                                     StatisticData statisticData) {

        putDataIntoMap(metaData, BAMDataPublisherConstants.REMOTE_ADDRESS,
                       statisticData.getRemoteAddress());
        putDataIntoMap(metaData, BAMDataPublisherConstants.HOST, statisticData.getHost());
        putDataIntoMap(metaData, BAMDataPublisherConstants.CONTENT_TYPE,
                       statisticData.getContentType());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REFERER, statisticData.getReferer());
        putDataIntoMap(metaData, BAMDataPublisherConstants.USER_AGENT, statisticData.getUserAgent());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REQUEST_URL, statisticData.getRequestURL());
    }

    private void addDataIntoEventData(Map<String, ByteBuffer> eventData, StatisticData statistic) {

        SystemStatistics systemStatistics = statistic.getSystemStatistics();

        putDataIntoMap(eventData, ServiceStatisticsPublisherConstants.SERVER_NAME,
                       systemStatistics.getServerName());
        putDataIntoMap(eventData, BAMDataPublisherConstants.SERVICE_NAME,
                       statistic.getServiceName());
        putDataIntoMap(eventData, BAMDataPublisherConstants.OPERATION_NAME,
                       statistic.getOperationName());
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
        putDataIntoMap(eventData, BAMDataPublisherConstants.TIMESTAMP,
                       statistic.getTimestamp().toString());

    }

    private void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }
}
