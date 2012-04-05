package org.wso2.carbon.bam.kpiagent;

import org.apache.log4j.BasicConfigurator;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class KPIAgent {
    public static void main(String[] args) {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setSocketTransportEnabled(true);
        eventReceiver.setUrl("https://localhost:9443/");
        eventReceiver.setUserName("admin");
        eventReceiver.setPassword("admin");
        eventReceiver.setPort(7611);

        Event johnEvent = new Event();
        johnEvent.setCorrelation(createCorrelationMap());
        johnEvent.setEvent(createEventDataMap("Milk", "20", "3600.00", "John"));
        johnEvent.setMeta(createMetaDataMap());

        Event johnEvent1 = new Event();
        johnEvent1.setCorrelation(createCorrelationMap());
        johnEvent1.setEvent(createEventDataMap("Milk", "40", "3600.00", "John"));
        johnEvent1.setMeta(createMetaDataMap());

        Event johnEvent2 = new Event();
        johnEvent2.setCorrelation(createCorrelationMap());
        johnEvent2.setEvent(createEventDataMap("Butter", "2", "150.00", "John"));
        johnEvent2.setMeta(createMetaDataMap());

        Event tomEvent = new Event();
        tomEvent.setCorrelation(createCorrelationMap());
        tomEvent.setEvent(createEventDataMap("Milk", "2", "520.00", "Tom"));
        tomEvent.setMeta(createMetaDataMap());

        Event maryEvent = new Event();
        maryEvent.setCorrelation(createCorrelationMap());
        maryEvent.setEvent(createEventDataMap("Bread", "3", "260.00", "Mary"));
        maryEvent.setMeta(createMetaDataMap());

        Event tomEvent1 = new Event();
        tomEvent1.setCorrelation(createCorrelationMap());
        tomEvent1.setEvent(createEventDataMap("Butter", "1", "520.00", "Tom"));
        tomEvent1.setMeta(createMetaDataMap());

        Event paulEvent = new Event();
        paulEvent.setCorrelation(createCorrelationMap());
        paulEvent.setEvent(createEventDataMap("Sugar", "4", "2400.00", "Paul"));
        paulEvent.setMeta(createMetaDataMap());

        List<Event> events = new ArrayList<Event>();
        events.add(johnEvent);
        events.add(johnEvent1);
        events.add(johnEvent2);
        events.add(maryEvent);
        events.add(tomEvent);
        events.add(tomEvent1);
        events.add(paulEvent);

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
        correlationMap.put("correlationKey2", ByteBuffer.wrap("correlationValue2".getBytes()));
        correlationMap.put("correlationKey3", ByteBuffer.wrap("correlationValue3".getBytes()));
        return correlationMap;
    }

    private static Map<String, ByteBuffer> createEventDataMap(String item, String quantity, String total, String user) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("in_message_body", ByteBuffer.wrap(KPIAgent.getMessageBody(item, quantity, total, user).getBytes()));
        eventMap.put("request_count", ByteBuffer.wrap("1".getBytes()));
        eventMap.put("response_count", ByteBuffer.wrap("1".getBytes()));
        eventMap.put("response_time", ByteBuffer.wrap("2.0".getBytes()));
        return eventMap;
    }

    private static String getMessageBody(String item, String quantity, String total, String user) {
        return "   <p:placeOrder xmlns:p=\"http://order.sample\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ax23:order xmlns:ax23=\"http://order.sample\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:itemCode xmlns:xs=\"http://order.sample/xsd\">"+ item +"</xs:itemCode>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:quantity xmlns:xs=\"http://order.sample/xsd\">" + quantity + "</xs:quantity>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:total xmlns:xs=\"http://order.sample/xsd\">" + total + "</xs:total>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:userId xmlns:xs=\"http://order.sample/xsd\">" + user + "</xs:userId>\n" +
                "      </ax23:order>\n" +
                "   </p:placeOrder>";
    }

    private static Map<String, ByteBuffer> createMetaDataMap() {
        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
        metaDataMap.put("metaKey1", ByteBuffer.wrap("metaValue1".getBytes()));
        metaDataMap.put("metaKey2", ByteBuffer.wrap("metaValue2".getBytes()));
        metaDataMap.put("metaKey3", ByteBuffer.wrap("metanValue3".getBytes()));
        return metaDataMap;
    }
}
