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
package org.wso2.carbon.bam.service.data.publisher.publish;


import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.conf.Property;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.queue.ActivityQueue;
import org.wso2.carbon.bam.service.data.publisher.queue.EventQueue;
import org.wso2.carbon.bam.service.data.publisher.queue.ServiceStatisticsQueue;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAgentUtil {

    private static ServiceStatisticsQueue serviceStatsQueue;
    private static ActivityQueue activityInStatsQueue;

    private static EventQueue eventQueue;

    private static PublisherConfiguration publisherConfiguration;

    @Deprecated
    public static void publishServiceStats(PublishData publishData) {
        serviceStatsQueue.enqueue(publishData);
    }

    @Deprecated
    public static void publishActivityStats(PublishData publishData) {
        activityInStatsQueue.enqueue(publishData);
    }

    public static void setServiceStatisticQueue(ServiceStatisticsQueue serviceStatisticsQueue) {
        serviceStatsQueue = serviceStatisticsQueue;
    }

    public static void setActivityInQueue(ActivityQueue activityInQueue) {
        activityInStatsQueue = activityInQueue;
    }

    public static void setPublisherConfiguration(PublisherConfiguration configuration) {
        publisherConfiguration = configuration;
    }

    public static PublisherConfiguration getPublisherConfiguration() {
        return publisherConfiguration;
    }

    public static void setEventQueue(EventQueue queue) {
        eventQueue = queue;
    }

    public static void publishEvent(PublishData data) {
        eventQueue.enqueue(data);
    }

    public static EventReceiver constructEventReceiver(BAMServerInfo bamServerInfo) {
        EventReceiver receiver = new EventReceiver();
        receiver.setHttpTransportEnabled(bamServerInfo.isHttpTransportEnable());
        receiver.setSocketTransportEnabled(bamServerInfo.isSocketTransportEnable());
        receiver.setPassword(bamServerInfo.getBamPassword());
        receiver.setPort(bamServerInfo.getPort());
        receiver.setUserName(bamServerInfo.getBamUserName());
        receiver.setUrl(bamServerInfo.getBamServerURL());
        return receiver;
    }

    public static List<Event> makeEventList(PublishData publishData, EventingConfigData eventingConfigData) {

        EventData event = publishData.getEventData();

        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        addEventData(eventData, event);
        addMetaData(metaData, event);
        addCorrelationData(correlationData, event);

        addProperties(metaData, eventingConfigData);

        Event publishEvent = new Event();
        publishEvent.setCorrelation(correlationData);
        publishEvent.setMeta(metaData);
        publishEvent.setEvent(eventData);

        ArrayList<Event> events = new ArrayList<Event>();
        events.add(publishEvent);
        return events;
    }

    private static void addProperties(Map<String, ByteBuffer> metaData, EventingConfigData eventingConfigData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && property.getKey() != "") {
                    putDataIntoMap(metaData, property.getKey(), property.getValue());
                }
            }
        }
    }

    private static void addEventData(Map<String, ByteBuffer> eventData,
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

    private static void addMetaData(Map<String, ByteBuffer> metaData, EventData eventData) {

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

    private static void addCorrelationData(Map<String, ByteBuffer> correlationData,
                                           EventData eventData) {
        putDataIntoMap(correlationData, BAMDataPublisherConstants.MSG_ACTIVITY_ID,
                eventData.getActivityId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.IN_MSG_ID,
                eventData.getInMessageId());
        putDataIntoMap(correlationData, BAMDataPublisherConstants.OUT_MSG_ID,
                eventData.getOutMessageId());
    }

    private static void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }

    public static void extractInfoFromHttpHeaders(EventData eventData, Object requestProperty) {

        if (requestProperty instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) requestProperty;
            eventData.setRequestURL(httpServletRequest.getRequestURL().toString());
            eventData.setRemoteAddress(httpServletRequest.getRemoteAddr());
            eventData.setContentType(httpServletRequest.getContentType());
            eventData.setUserAgent(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_USER_AGENT));
            eventData.setHost(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_HOST));
            eventData.setReferer(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_REFERER));
        }

    }

    public static BAMServerInfo addBAMServerInfo(EventingConfigData eventingConfigData) {
        BAMServerInfo bamServerInfo = new BAMServerInfo();
        bamServerInfo.setBamServerURL(eventingConfigData.getUrl());
        bamServerInfo.setBamUserName(eventingConfigData.getUserName());
        bamServerInfo.setBamPassword(eventingConfigData.getPassword());
        bamServerInfo.setHttpTransportEnable(eventingConfigData.isHttpTransportEnable());
        bamServerInfo.setSocketTransportEnable(eventingConfigData.isSocketTransportEnable());
        bamServerInfo.setPort(eventingConfigData.getPort());
        return bamServerInfo;
    }
}
