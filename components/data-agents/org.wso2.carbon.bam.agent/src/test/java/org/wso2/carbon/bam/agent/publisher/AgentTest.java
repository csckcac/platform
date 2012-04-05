package org.wso2.carbon.bam.agent.publisher;

import org.apache.log4j.BasicConfigurator;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class AgentTest {
    public static void main(String[] args) {
        Event event = new Event();

        event.setCorrelation(createCorrelationMap());
        event.setEvent(createEventDataMap());
        event.setMeta(createMetaDataMap());

        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setSocketTransportEnabled(true);
        eventReceiver.setUrl("https://localhost:9443/");
        eventReceiver.setUserName("admin");
        eventReceiver.setPassword("admin");
        eventReceiver.setPort(7611);

        ArrayList<Event> events = new ArrayList<Event>();
        events.add(event);

        BasicConfigurator.configure();
        setTrustStoreParams();

        AgentConfiguration configuration = new AgentConfiguration();
        Agent agent = new Agent(configuration);
        agent.publish(events, eventReceiver);
        agent.shutdown();
    }

    private static  void setTrustStoreParams() {
        String trustStore = "/Users/mackie/source-checkouts/carbon/products/bam2/modules/p2-profile-gen/product/target/wso2carbon-core-3.2.2/repository/resources/security";
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

    private static Map<String, ByteBuffer> createEventDataMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
        correlationMap.put("eventKey4", ByteBuffer.wrap("eventValue1".getBytes()));
        correlationMap.put("eventKey5", ByteBuffer.wrap("eventValue2".getBytes()));
        correlationMap.put("eventKey6", ByteBuffer.wrap("eventValue3".getBytes()));
        return correlationMap;
    }

    private static Map<String, ByteBuffer> createMetaDataMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
        correlationMap.put("metaKey1", ByteBuffer.wrap("metaValue1".getBytes()));
        correlationMap.put("metaKey2", ByteBuffer.wrap("metaValue2".getBytes()));
        correlationMap.put("metaKey3", ByteBuffer.wrap("metanValue3".getBytes()));
        return correlationMap;
    }
}
