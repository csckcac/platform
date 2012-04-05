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
package org.wso2.carbon.bam.activity.mediation.data.publisher.process;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.activity.mediation.data.publisher.data.MessageActivity;
import org.wso2.carbon.bam.activity.mediation.data.publisher.publish.ActivityProcessor;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.Event;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ActivityWorker implements Runnable {

    private static final Log log = LogFactory.getLog(ActivityWorker.class);

    private Queue<MessageActivity> activityQueue;
    ActivityProcessor activityProcessor;

    public ActivityWorker(Queue<MessageActivity> activityQueue,
                          ActivityProcessor activityProcessor) {
        this.activityQueue = activityQueue;
        this.activityProcessor = activityProcessor;
    }

    @Override
    public void run() {
        clearActivityDataQueue(activityQueue.size());
    }

    private void clearActivityDataQueue(int size) {
        if (log.isDebugEnabled()) {
            log.debug("Clearing " + size + " activities from the activity queue...");
        }
        ArrayList<Event> eventList = new ArrayList<Event>();
        int tenantId = -1;
        for (int i = 0; i < size; i++) {

            MessageActivity msgActivity = activityQueue.poll();
            //Sometimes other thread may get the last queue object
            if (msgActivity != null) {
                Event event = makeEventObject(msgActivity);
                eventList.add(event);
                tenantId = msgActivity.getTenantId();
            }
        }
        if (eventList.size() > 0) {
            activityProcessor.process(eventList, tenantId);
        }

    }

    private Event makeEventObject(MessageActivity msgActivity) {
        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        addEventData(eventData, msgActivity);
        addMetaData(metaData, msgActivity);
        addCorrelationData(correlationData, msgActivity);


        Event event = new Event();
        event.setCorrelation(correlationData);
        event.setMeta(metaData);
        event.setEvent(eventData);

        return event;
    }

    private void addCorrelationData(Map<String, ByteBuffer> correlationData,
                                    MessageActivity msgActivity) {
        putDataIntoMap(correlationData, BAMDataPublisherConstants.MSG_ACTIVITY_ID,
                       msgActivity.getActivityId());
    }

    private void addMetaData(Map<String, ByteBuffer> metaData, MessageActivity msgActivity) {
        putDataIntoMap(metaData, ActivityPublisherConstants.SENDER_HOST,
                       msgActivity.getSenderHost());
    }

    private void addEventData(Map<String, ByteBuffer> eventData, MessageActivity msgActivity) {

        putDataIntoMap(eventData, BAMDataPublisherConstants.SERVICE_NAME,
                       msgActivity.getService());
        putDataIntoMap(eventData, BAMDataPublisherConstants.OPERATION_NAME,
                       msgActivity.getOperation());
        putDataIntoMap(eventData, BAMDataPublisherConstants.TIMESTAMP,
                       msgActivity.getTimestamp().toString());
        putDataIntoMap(eventData, BAMDataPublisherConstants.MSG_DIRECTION,
                       msgActivity.getDirection());
        putDataIntoMap(eventData, BAMDataPublisherConstants.MSG_ID,
                       msgActivity.getMessageId());
        putDataIntoMap(eventData, BAMDataPublisherConstants.MSG_BODY,
                       msgActivity.getPayload());
        putDataIntoMap(eventData,BAMDataPublisherConstants.SOAP_ENVELOP_NAMESPACE_URI,
                       msgActivity.getSoapEnvelopNamespaceURI());

        Map<String, String> properties = msgActivity.getProperty();

        for (Map.Entry entry : properties.entrySet()) {
            putDataIntoMap(eventData, entry.getKey().toString(), entry.getValue().toString());
        }

    }

    private void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }


}
