package org.wso2.carbon.bam.faultdetection;

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
public class FaultAgent {
        public static void main(String[] args) {
            EventReceiver eventReceiver = new EventReceiver();
            eventReceiver.setSocketTransportEnabled(true);
            eventReceiver.setUrl("https://localhost:9443/");
            eventReceiver.setUserName("admin");
            eventReceiver.setPassword("admin");
            eventReceiver.setPort(7611);

            Event faultEvent = new Event();
            faultEvent.setCorrelation(createCorrelationMap());
            faultEvent.setEvent(createEventDataMap());
            faultEvent.setMeta(createMetaDataMap());

            List<Event> events = new ArrayList<Event>();
            events.add(faultEvent);

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

        private static Map<String, ByteBuffer> createEventDataMap() {
            Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
            eventMap.put("bam_activity_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
            eventMap.put("bam_current_sequence", ByteBuffer.wrap("echoProxyOutSequence".getBytes()));
            eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
                    "<soapenv:Fault xmlns:axis2ns1=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Code>" +
                    "<soapenv:Value>axis2ns1:Sender</soapenv:Value></soapenv:Code><soapenv:Reason>" +
                    "<soapenv:Text xml:lang=\"en-US\">Invalid value \"?\" for element in</soapenv:Text>" +
                    "</soapenv:Reason><soapenv:Detail /></soapenv:Fault></soapenv:Body>").getBytes()));
            eventMap.put("message_direction", ByteBuffer.wrap("OUT".getBytes()));
            eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
            eventMap.put("operation_name", ByteBuffer.wrap("echoInt".getBytes()));
            eventMap.put("service_name", ByteBuffer.wrap("EchoProxy".getBytes()));
            eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
            eventMap.put("operation_name", ByteBuffer.wrap("OUT".getBytes()));
            eventMap.put("timestamp", ByteBuffer.wrap(new Date().toString().getBytes()));

            return eventMap;
        }


        private static Map<String, ByteBuffer> createMetaDataMap() {
            Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
            metaDataMap.put("metaKey1", ByteBuffer.wrap("metaValue1".getBytes()));
            metaDataMap.put("metaKey2", ByteBuffer.wrap("metaValue2".getBytes()));
            metaDataMap.put("metaKey3", ByteBuffer.wrap("metanValue3".getBytes()));
            return metaDataMap;
        }

}
