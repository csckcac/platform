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
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.data.publisher.data.ActivityData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.publish.StatsProcessor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


public class ActivityWorker implements Runnable {

    private static Log log = LogFactory.getLog(ServiceStatsWorker.class);

    private Queue<PublishData> activityQueue;
    private StatsProcessor activityProcessor;

    public ActivityWorker(Queue<PublishData> activityQueue, StatsProcessor statsProcessor) {
        this.activityQueue = activityQueue;
        activityProcessor = statsProcessor;
    }

    public void run() {
        clearStatisticDataInQueue(activityQueue.size());
    }

    private void clearStatisticDataInQueue(int size) {
        if (log.isDebugEnabled()) {
            log.debug("Number of events in queue : " + size);
        }
        ArrayList<Event> eventList = new ArrayList<Event>();
        BAMServerInfo bamServerInfo = null;
        for (int i = 0; i < size; i++) {
            PublishData publishData = activityQueue.poll();
            //Sometimes other thread may get the last queue object
            if (publishData != null) {
                Event event = makeEventObject(publishData);
                eventList.add(event);
                bamServerInfo = publishData.getBamServerInfo();
            }
        }
        if (eventList.size() > 0) {
            activityProcessor.process(eventList, bamServerInfo);
        }
    }

    private Event makeEventObject(PublishData publishData) {

        ActivityData activityData = publishData.getActivityData();

        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        addEventData(eventData, activityData);
        addMetaData(metaData, activityData);
        addCorrelationData(correlationData, activityData);


        Event event = new Event();
        event.setCorrelation(correlationData);
        event.setMeta(metaData);
        event.setEvent(eventData);

        return event;
    }

    private void addCorrelationData(Map<String, ByteBuffer> correlationData,
                                    ActivityData activityData) {
        putDataIntoMap(correlationData, BAMDataPublisherConstants.MSG_ACTIVITY_ID,
                       activityData.getActivityId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.IN_MSG_ID,
                       activityData.getMessageId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.OUT_MSG_ID,
                       activityData.getMessageId());
    }

    private void addMetaData(Map<String, ByteBuffer> metaData, ActivityData activityData) {

        putDataIntoMap(metaData, BAMDataPublisherConstants.REMOTE_ADDRESS,
                       activityData.getRemoteAddress());
        putDataIntoMap(metaData, BAMDataPublisherConstants.HOST,
                       activityData.getHost());
        putDataIntoMap(metaData, BAMDataPublisherConstants.CONTENT_TYPE,
                       activityData.getContentType());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REFERER,
                       activityData.getReferer());
        putDataIntoMap(metaData, BAMDataPublisherConstants.USER_AGENT,
                       activityData.getUserAgent());
        putDataIntoMap(metaData, BAMDataPublisherConstants.REQUEST_URL,
                       activityData.getRequestURL());
    }

    private void addEventData(Map<String, ByteBuffer> eventData,
                              ActivityData activityData) {
        putDataIntoMap(eventData, BAMDataPublisherConstants.TIMESTAMP,
                       activityData.getTimestamp().toString());
/*        putDataIntoMap(eventData, BAMDataPublisherConstants.MSG_BODY,
                       activityData.getMsgBody());
        putDataIntoMap(eventData, BAMDataPublisherConstants.MSG_DIRECTION,
                       activityData.getMessageDirection());*/
        putDataIntoMap(eventData, BAMDataPublisherConstants.SERVICE_NAME,
                       activityData.getServiceName());
        putDataIntoMap(eventData, BAMDataPublisherConstants.OPERATION_NAME,
                       activityData.getOperationName());

    }

    private void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }
}
