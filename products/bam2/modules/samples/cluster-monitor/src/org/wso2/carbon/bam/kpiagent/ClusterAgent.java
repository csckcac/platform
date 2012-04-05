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
package org.wso2.carbon.bam.kpiagent;

import org.apache.log4j.BasicConfigurator;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.bam.service.Event;

public class ClusterAgent {
    public static void main(String[] args) {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setSocketTransportEnabled(true);
        eventReceiver.setUrl("https://localhost:9443/");
        eventReceiver.setUserName("admin");
        eventReceiver.setPassword("admin");
        eventReceiver.setPort(7611);

        Event dc1Event1 = new Event();
        dc1Event1.setCorrelation(createCorrelationMap());
        dc1Event1.setEvent(createEventDataMap("2", "2", "0", "6", "GridSys", "GridAS"));
        dc1Event1.setMeta(createMetaDataMap());

        Event dc1Event2 = new Event();
        dc1Event2.setCorrelation(createCorrelationMap());
        dc1Event2.setEvent(createEventDataMap("2", "0", "2", "3", "GridSys", "GridAS"));
        dc1Event2.setMeta(createMetaDataMap());

        Event dc1Event3 = new Event();
        dc1Event3.setCorrelation(createCorrelationMap());
        dc1Event3.setEvent(createEventDataMap("5", "3", "2", "2", "GridSys", "GridESB"));
        dc1Event3.setMeta(createMetaDataMap());

        Event dc2Event1 = new Event();
        dc2Event1.setCorrelation(createCorrelationMap());
        dc2Event1.setEvent(createEventDataMap("8", "4", "4", "18", "NatVest", "NatAS"));
        dc2Event1.setMeta(createMetaDataMap());

        Event dc2Event2 = new Event();
        dc2Event2.setCorrelation(createCorrelationMap());
        dc2Event2.setEvent(createEventDataMap("3", "2", "1", "1", "NatVest", "NatESB"));
        dc2Event2.setMeta(createMetaDataMap());

        Event dc2Event3 = new Event();
        dc2Event3.setCorrelation(createCorrelationMap());
        dc2Event3.setEvent(createEventDataMap("9", "8", "1", "3", "NatVest", "NatESB"));
        dc2Event3.setMeta(createMetaDataMap());

        List<Event> events = new ArrayList<Event>();
        events.add(dc1Event1);
        events.add(dc1Event2);
        events.add(dc1Event3);
        events.add(dc2Event1);
        events.add(dc2Event2);
        events.add(dc2Event3);

        BasicConfigurator.configure();
        setTrustStoreParams();

        AgentConfiguration configuration = new AgentConfiguration();
        Agent agent = new Agent(configuration);
        agent.publish(events, eventReceiver);
        agent.shutdown();
    }

    private static  void setTrustStoreParams() {
        String trustStore = "../../repository/resources/security";
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    private static Map<String, ByteBuffer> createCorrelationMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
        correlationMap.put("correlationKey1", ByteBuffer.wrap("correlationValue1".getBytes()));
        return correlationMap;
    }

    private static Map<String, ByteBuffer> createEventDataMap(String requestCount,
                                                              String responseCount,
                                                              String faultCount,
                                                              String responseTime,
                                                              String dataCenter,
                                                              String cluster) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("request_count", ByteBuffer.wrap(requestCount.getBytes()));
        eventMap.put("response_count", ByteBuffer.wrap(responseCount.getBytes()));
        eventMap.put("fault_count", ByteBuffer.wrap(faultCount.getBytes()));
        eventMap.put("response_time", ByteBuffer.wrap(responseTime.getBytes()));
        eventMap.put("dataCenter", ByteBuffer.wrap(dataCenter.getBytes()));
        eventMap.put("cluster", ByteBuffer.wrap(cluster.getBytes()));
        eventMap.put("clusterStream", ByteBuffer.wrap("1".getBytes()));
        return eventMap;
    }

    private static Map<String, ByteBuffer> createMetaDataMap() {
        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
        metaDataMap.put("metaKey1", ByteBuffer.wrap("metaValue1".getBytes()));
        return metaDataMap;
    }
}

