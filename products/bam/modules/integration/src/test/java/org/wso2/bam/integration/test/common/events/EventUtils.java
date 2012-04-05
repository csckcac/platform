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
package org.wso2.bam.integration.test.common.events;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bam.integration.test.common.statistics.CumulativeStatisticsData;
import org.wso2.bam.integration.test.common.statistics.ServiceEventData;
import org.wso2.bam.integration.test.common.statistics.StatisticUtils;
import org.wso2.bam.integration.test.common.statistics.StatisticsData;
import org.wso2.carbon.bam.data.publisher.servicestats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.servicestats.data.EventData;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventUtils {

    private static final Log log = LogFactory.getLog(EventUtils.class);

    private static final int COUNT_INCREMENT_BOUND = 1000;

    private static final int REQUEST_INCREMENT = 10;
    private static final int RESPONSE_INCREMENT = 5;
    private static final int FAULT_INCREMENT = 5;

    private static final long MIN_RESPONSE_TIME_INCREMENT = 10;
    private static final long MAX_RESPONSE_TIME_INCREMENT = 10;
    private static final double AVG_RESPONSE_TIME_INCREMENT = 5;

    public static CumulativeStatisticsData populateRandomServiceStatEvents(List<OMElement> events,
                                                                           StatisticsData data,
                                                                           int count)
            throws Exception {
        if (data.getServerName() == null) {
            throw new EventException("Error creating event. Server name must be present..");
        }

        if (count <= 0) {
            throw new EventException("Error creating event. Count must be greater than zero..");
        }

        if (events == null) {
            events = new ArrayList<OMElement>(count);
        }

        PublisherUtils.setServerName(data.getServerName());

        long minResTime = 0;
        long maxResTime = 0;
        double totalAvgResTime = 0;
        int lastResCount = 0;
        int lastReqCount = 0;
        int lastFaultCount = 0;

        for (int i = 0; i < count; i++) {
            EventData eventData = getRandomEventData(lastResCount, lastReqCount, lastFaultCount);

            if (data.getServiceName() != null) {
                eventData.setServiceName(data.getServiceName());
            }

            if (data.getOperationName() != null) {
                eventData.setOperationName(data.getOperationName());
            }

            OMElement event = null;
            events.add(event);

            totalAvgResTime += eventData.getAvgResponseTime();

            if (minResTime > eventData.getMinResponseTime()) {
                minResTime = eventData.getMinResponseTime();
            }

            if (maxResTime < eventData.getMaxResponseTime()) {
                maxResTime = eventData.getMaxResponseTime();
            }

            lastResCount = eventData.getResponseCount();
            lastReqCount = eventData.getResponseCount();
            lastFaultCount = eventData.getFaultCount();
        }

        CumulativeStatisticsData cumulativeData = new CumulativeStatisticsData(count);
        cumulativeData.setMinResTime(minResTime);
        cumulativeData.setMaxResTime(maxResTime);
        cumulativeData.setTotalAvgResTime(totalAvgResTime);

        StatisticsData lastEventData = StatisticUtils.getStatisticsData(events.get(events.size() - 1));
        cumulativeData.setTotalRequestCount(lastEventData.getRequestCount());
        cumulativeData.setTotalResponseCount(lastEventData.getResponseCount());
        cumulativeData.setTotalFaultCount(lastEventData.getFaultCount());

        return cumulativeData;
    }

    public static ServiceEventData generateServiceStatEvents(String baseFile, int count)
            throws EventException, XMLStreamException {

        if (baseFile == null) {
            throw new EventException("The base event file must be provided..");
        }

        OMElement event;
        event = new StAXOMBuilder(EventUtils.class.getResourceAsStream("/" + baseFile)).
                getDocumentElement();

        StatisticsData data = StatisticUtils.getStatisticsData(event);

        if (data.getServerName() == null) {
            throw new EventException("Error creating event. Server name must be present..");
        }

        if (count <= 0) {
            throw new EventException("Error creating event. Count must be greater than zero..");
        }

        List<StatisticsData> events = new ArrayList<StatisticsData>(count);
        events.add(data);

        StatisticsData priorEvent = data;
        double totalAvgResTime = 0;
        for (int i = 0; i < (count - 1); i++) {
            StatisticsData nextEvent = getSteppedEventData(priorEvent);

            if (data.getServiceName() != null) {
                nextEvent.setServiceName(data.getServiceName());
            }

            if (data.getOperationName() != null) {
                nextEvent.setOperationName(data.getOperationName());
            }

            events.add(nextEvent);

            totalAvgResTime += nextEvent.getAvgResTime();

            priorEvent = nextEvent;

        }

        StatisticsData firstEvent = events.get(0);
        StatisticsData lastEvent = events.get(events.size() - 1);

        CumulativeStatisticsData cumulativeData = new CumulativeStatisticsData(count);
        cumulativeData.setMinResTime(firstEvent.getMinResTime());
        cumulativeData.setMaxResTime(lastEvent.getMaxResTime());
        cumulativeData.setTotalAvgResTime(totalAvgResTime);
        cumulativeData.setTotalRequestCount(lastEvent.getRequestCount());
        cumulativeData.setTotalResponseCount(lastEvent.getResponseCount());
        cumulativeData.setTotalFaultCount(lastEvent.getFaultCount());

        ServiceEventData eventData = new ServiceEventData();
        eventData.setEvents(events.toArray(new StatisticsData[]{}));
        eventData.setStatistics(cumulativeData);

        return eventData;
    }

    private static EventData getRandomEventData(int lastResCount, int lastReqCount,
                                                int lastFaultCount) {
        EventData data = new EventData();
        Random rand = new Random();

        int value = rand.nextInt(COUNT_INCREMENT_BOUND);
        int currentCount = lastReqCount + value;
        data.setRequestCount(currentCount);

        value = rand.nextInt(COUNT_INCREMENT_BOUND);
        currentCount = lastResCount + value;
        data.setResponseCount(currentCount);

        value = rand.nextInt(COUNT_INCREMENT_BOUND);
        currentCount = lastFaultCount + value;
        data.setFaultCount(currentCount);

        long randNumberOne = rand.nextLong();
        long randNumberTwo = rand.nextLong();

        if (randNumberOne > randNumberTwo) {
            data.setMaxResponseTime(randNumberOne);
            data.setMinResponseTime(randNumberTwo);
        } else {
            data.setMaxResponseTime(randNumberTwo);
            data.setMaxResponseTime(randNumberOne);
        }

        data.setAvgResponseTime(rand.nextDouble());

        return data;
    }

    private static StatisticsData getSteppedEventData(StatisticsData lastEvent) {
        StatisticsData nextEvent = new StatisticsData();
        nextEvent.setServerName(lastEvent.getServerName());
        nextEvent.setServiceName(lastEvent.getServiceName());
        nextEvent.setOperationName(lastEvent.getOperationName());

        nextEvent.setRequestCount(lastEvent.getRequestCount() + REQUEST_INCREMENT);
        nextEvent.setResponseCount(lastEvent.getResponseCount() + RESPONSE_INCREMENT);
        nextEvent.setFaultCount(lastEvent.getFaultCount() + FAULT_INCREMENT);
        nextEvent.setMaxResTime(lastEvent.getMaxResTime() + MAX_RESPONSE_TIME_INCREMENT);
        nextEvent.setMinResTime(lastEvent.getMinResTime() + MIN_RESPONSE_TIME_INCREMENT);
        nextEvent.setAvgResTime(lastEvent.getAvgResTime() + AVG_RESPONSE_TIME_INCREMENT);

        return nextEvent;
    }

    private static EventData covertToEventData(StatisticsData data) {

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
