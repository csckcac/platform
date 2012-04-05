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
package org.wso2.bam.integration.test.common.statistics;

import org.apache.axiom.om.OMElement;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.carbon.bam.data.publisher.servicestats.data.EventData;


public class ServiceEventData {

    private CumulativeStatisticsData statistics;

    private StatisticsData[] events;

    private OMElement[] eventElements;


    public CumulativeStatisticsData getStatistics() {
        return statistics;
    }

    public void setStatistics(CumulativeStatisticsData statistics) {
        this.statistics = statistics;
    }

    public StatisticsData[] getEvents() throws EventException {
        if (events == null && eventElements != null) {
            events = new StatisticsData[eventElements.length];
            for (int i = 0; i < eventElements.length; i++) {
                OMElement eventPayload = eventElements[i];
                StatisticsData data = StatisticUtils.getStatisticsData(eventPayload);
                events[i] = data;
            }
        }
        return events;
    }

    public void setEvents(StatisticsData[] events) {
        this.events = events;
    }

    public void setEventPayloads(OMElement[] events) {
        this.eventElements = events;
    }

    public OMElement[] getEventPayloads() throws EventException {
        if (eventElements == null && events != null) {
            eventElements = new OMElement[events.length];
            for (int i = 0; i < events.length; i++) {
                StatisticsData data = events[i];
                EventData eventData = convertToEventData(data);
                OMElement eventPayload = null;
                eventElements[i] = eventPayload;
            }
        }
        return eventElements;
    }

    private EventData convertToEventData(StatisticsData data) {

        EventData eventData = new EventData();
        eventData.setServiceName(data.getServiceName());
        eventData.setOperationName(data.getOperationName());
        eventData.setRequestCount(data.getRequestCount());
        eventData.setResponseCount(data.getResponseCount());
        eventData.setFaultCount(data.getFaultCount());
        eventData.setMinResponseTime(data.getMinResTime());
        eventData.setMaxResponseTime(data.getMaxResTime());
        eventData.setAvgResponseTime(data.getAvgResTime());

        return eventData;

    }

}
