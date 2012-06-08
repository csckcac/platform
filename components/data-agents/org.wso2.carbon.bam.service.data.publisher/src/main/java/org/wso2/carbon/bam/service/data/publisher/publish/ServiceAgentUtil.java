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


import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class ServiceAgentUtil {

    public static Event makeEventList(PublishData publishData,
                                            EventingConfigData eventingConfigData) {

        EventData event = publishData.getEventData();

        List<Object> correlationData = new ArrayList<Object>();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> eventData = new ArrayList<Object>();

        StatisticsType statisticsType = findTheStatisticType(event);

        addCommonEventData(event, eventData);

        switch (statisticsType) {

            case ACTIVITY_IN_ONLY:
                addActivityInEventData(event, eventData);
                addActivityInMetaData(event, metaData);
                addActivityInCorrelationData(event, correlationData);
                break;
            case ACTIVITY_OUT_ONLY:
                addActivityOutEventData(event, eventData);
                addActivityOutCorrelationData(event, correlationData);
                break;
            case ACTIVITY_IN_OUT:
                //In data
                addActivityInEventData(event, eventData);
                addActivityInMetaData(event, metaData);
                addActivityInCorrelationData(event, correlationData);
                //Out data -- Meta and correlation values come from In data
                addActivityOutEventData(event, eventData);
                break;
            case ACTIVITY_IN_ONLY_SERVICE_STATS:
                //In data
                addActivityInEventData(event, eventData);
                addActivityInMetaData(event, metaData);
                addActivityInCorrelationData(event, correlationData);
                //ServiceStats data -- Meta and correlation values come from In data
                addStatisticEventData(event, eventData);
                break;
            case ACTIVITY_OUT_ONLY_SERVICE_STATS:
                //Out data
                addActivityOutEventData(event, eventData);
                addActivityOutCorrelationData(event, correlationData);
                //ServiceStats data
                addStatisticEventData(event, eventData);
                break;
            case ACTIVITY_IN_OUT_SERVICE_STATS:
                //In data
                addActivityInEventData(event, eventData);
                addActivityInMetaData(event, metaData);
                addActivityInCorrelationData(event, correlationData);
                //Out data
                addActivityOutEventData(event, eventData);
                //ServiceStats data
                addStatisticEventData(event, eventData);
                break;
            case SERVICE_STATS:
                addStatisticEventData(event, eventData);
                addStatisticsMetaData(event,metaData);
                break;
        }


/*        addProperties(metaData, eventingConfigData);*/

        Event publishEvent = new Event();
        publishEvent.setCorrelationData(correlationData);
        publishEvent.setMetaData(metaData);
        publishEvent.setEventData(eventData);
        publishEvent.setStatisticsType(statisticsType);

        return publishEvent;
    }

    private static StatisticsType findTheStatisticType(EventData event) {
        StatisticsType statisticsType = null;
        if (event.getInMessageId() != null && event.getOutMessageId() == null &&
            event.getSystemStatistics() == null) {
            statisticsType = StatisticsType.ACTIVITY_IN_ONLY;
        } else if (event.getInMessageId() != null && event.getOutMessageId() != null &&
                   event.getSystemStatistics() == null) {
            statisticsType = StatisticsType.ACTIVITY_IN_OUT;
        } else if (event.getInMessageId() == null && event.getOutMessageId() != null &&
                   event.getSystemStatistics() == null) {
            statisticsType = StatisticsType.ACTIVITY_OUT_ONLY;
        } else if (event.getInMessageId() != null && event.getOutMessageId() == null &&
                   event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.ACTIVITY_IN_ONLY_SERVICE_STATS;
        } else if (event.getInMessageId() == null && event.getOutMessageId() != null &&
                   event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.ACTIVITY_OUT_ONLY_SERVICE_STATS;
        } else if (event.getInMessageId() != null && event.getOutMessageId() != null &&
                   event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.ACTIVITY_IN_OUT_SERVICE_STATS;
        } else if (event.getInMessageId() == null && event.getOutMessageId() == null &&
                   event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.SERVICE_STATS;
        }
        return statisticsType;
    }

    private static void addCommonEventData(EventData event, List<Object> eventData) {
        eventData.add(event.getServiceName());
        eventData.add(event.getOperationName());
        eventData.add(event.getTimestamp());
    }

    private static void addActivityInMetaData(EventData event, List<Object> metaData) {
        metaData.add(event.getRequestURL());
        metaData.add(event.getRemoteAddress());
        metaData.add(event.getContentType());
        metaData.add(event.getUserAgent());
        metaData.add(event.getHost());
        metaData.add(event.getReferer());
    }


    private static void addActivityInEventData(EventData event, List<Object> eventData) {
        eventData.add(event.getInMessageId());
        eventData.add(event.getInMessageBody());
    }

    private static void addActivityInCorrelationData(EventData event,
                                                     List<Object> correlationData) {
        correlationData.add(event.getActivityId());
    }

    private static void addActivityOutEventData(EventData event, List<Object> eventData) {
        eventData.add(event.getOutMessageId());
        eventData.add(event.getOutMessageBody());
    }

    private static void addActivityOutCorrelationData(EventData event,
                                                      List<Object> correlationData) {
        correlationData.add(event.getActivityId());
    }


    private static void addStatisticEventData(EventData event, List<Object> eventData) {
        SystemStatistics systemStatistics = event.getSystemStatistics();
        eventData.add(systemStatistics.getCurrentInvocationResponseTime());
        eventData.add(systemStatistics.getCurrentInvocationRequestCount());
        eventData.add(systemStatistics.getCurrentInvocationResponseCount());
        eventData.add(systemStatistics.getCurrentInvocationFaultCount());
    }

    private static void addStatisticsMetaData(EventData event, List<Object> metaData) {
        metaData.add(event.getRequestURL());
        metaData.add(event.getRemoteAddress());
        metaData.add(event.getContentType());
        metaData.add(event.getUserAgent());
        metaData.add(event.getHost());
        metaData.add(event.getReferer());
    }

/*
    private static void addProperties(Map<String, ByteBuffer> metaData,
                                      EventingConfigData eventingConfigData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && property.getKey() != "") {
                    putDataIntoMap(metaData, property.getKey(), property.getValue());
                }
            }
        }
    }*/



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
