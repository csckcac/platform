package org.wso2.carbon.bam.MessageTracing;

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
public class MessageTracingAgent {
    public static void main(String[] args) {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.setSocketTransportEnabled(true);
        eventReceiver.setUrl("https://localhost:9443/");
        eventReceiver.setUserName("admin");
        eventReceiver.setPassword("admin");
        eventReceiver.setPort(7611);

        List<Event> events = getEvents();

        BasicConfigurator.configure();
        setTrustStoreParams();

        AgentConfiguration configuration = new AgentConfiguration();
        Agent agent = new Agent(configuration);
        agent.publish(events, eventReceiver);
        agent.shutdown();
    }

    private static void setTrustStoreParams() {
        String trustStore = "../../repository/resources/security";
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    private static Map<String, ByteBuffer> createCorrelationMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
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
        return metaDataMap;

    }

    private static List<Event> getEvents() {
        String bamActivityId = UUID.randomUUID().toString();
        List<Event> events = new ArrayList<Event>();

        getSuccessEvent(events);
        getAuthFailureEvent(events);
        getPlaceOrderFailEvent(events);

        return events;

    }


    private static List<Event> getSuccessEvent(List<Event> list) {
         String bamActivityId = UUID.randomUUID().toString();
         String cardNo = "123456";
         String symbol = "$";
         String price = "200";
         String messageId = "001";
         String product = "Television";
         String quantity = "1";

        Event authInEvent = new Event();
        authInEvent.setCorrelation(new HashMap());
        authInEvent.setEvent(createAuthenticationINEvenData(bamActivityId, cardNo, symbol, price, messageId));
        authInEvent.setMeta(new HashMap());
        list.add(authInEvent);

        Event authOUTEvent = new Event();
        authOUTEvent.setCorrelation(new HashMap());
        authOUTEvent.setEvent(createAuthenticationOUTEvenData(bamActivityId, cardNo, messageId));
        authOUTEvent.setMeta(new HashMap());
        list.add(authOUTEvent);

        Event orderINEvent = new Event();
        orderINEvent.setCorrelation(new HashMap());
        orderINEvent.setEvent(createPlaceOrderINEvenData(bamActivityId, cardNo, price, symbol, product, quantity, messageId));
        orderINEvent.setMeta(new HashMap());
        list.add(orderINEvent);

        Event orderOUTEvent = new Event();
        orderOUTEvent.setCorrelation(new HashMap());
        orderOUTEvent.setEvent(createPlaceOrderOUTEvenData(bamActivityId, cardNo, product, quantity, messageId));
        orderOUTEvent.setMeta(new HashMap());
        list.add(orderOUTEvent);

        return list;
    }


    private static List<Event> getAuthFailureEvent(List<Event> list){
      String bamActivityId = UUID.randomUUID().toString();
         String cardNo = "56789";
         String symbol = "$";
         String price = "350";
         String messageId = "003";
         String product = "HomeTheatreSystem";
         String quantity = "1";

        Event authInEvent = new Event();
        authInEvent.setCorrelation(new HashMap());
        authInEvent.setEvent(createAuthenticationINEvenData(bamActivityId, cardNo, symbol, price, messageId));
        authInEvent.setMeta(new HashMap());
        list.add(authInEvent);

        Event authFailEvent = new Event();
        authFailEvent.setCorrelation(new HashMap());
        authFailEvent.setEvent(createAuthenticationFailureEvenData(bamActivityId, cardNo, messageId));
        authFailEvent.setMeta(new HashMap());
        list.add(authFailEvent);

        return list;
    }


    private static List<Event> getPlaceOrderFailEvent(List<Event> list) {
         String bamActivityId = UUID.randomUUID().toString();
         String cardNo = "345678";
         String symbol = "$";
         String price = "100";
         String messageId = "007";
         String product = "DVDPlayer";
         String quantity = "1";

        Event authInEvent = new Event();
        authInEvent.setCorrelation(new HashMap());
        authInEvent.setEvent(createAuthenticationINEvenData(bamActivityId, cardNo, symbol, price, messageId));
        authInEvent.setMeta(new HashMap());
        list.add(authInEvent);

        Event authOUTEvent = new Event();
        authOUTEvent.setCorrelation(new HashMap());
        authOUTEvent.setEvent(createAuthenticationOUTEvenData(bamActivityId, cardNo, messageId));
        authOUTEvent.setMeta(new HashMap());
        list.add(authOUTEvent);

        Event orderINEvent = new Event();
        orderINEvent.setCorrelation(new HashMap());
        orderINEvent.setEvent(createPlaceOrderINEvenData(bamActivityId, cardNo, price, symbol, product, quantity, messageId));
        orderINEvent.setMeta(new HashMap());
        list.add(orderINEvent);

        Event orderFailureEvent = new Event();
        orderFailureEvent.setCorrelation(new HashMap());
        orderFailureEvent.setEvent(createPlaceOrderFailureEvenData(bamActivityId, cardNo, messageId, product));
        orderFailureEvent.setMeta(new HashMap());
        list.add(orderFailureEvent);

        return list;
    }

    private static Map<String, ByteBuffer> createAuthenticationINEvenData(String bamActivityId, String cardNo, String symbol, String price, String messageId) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("authenticationInSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:anthenticate xmlns:m0=\"http://services.authenticate/xsd\">\n" +
                "      <m0:creditcardNo>" + cardNo + "</m0:creditcardNo>\n" +
                "      <m0:price>" + price + "</m0:price>\n" +
                "      <m0:symbol>" + symbol + "</m0:symbol>\n" +
                "      <m0:messageId>" + messageId + "</m0:messageId>\n" +
                " </m0:anthenticate>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("IN".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("authenticate".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("authenticateService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime() - 4).getBytes()));
        return eventMap;
    }


    private static Map<String, ByteBuffer> createAuthenticationOUTEvenData(String bamActivityId, String cardNo, String messageId) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("authenticationOUTSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:anthenticate xmlns:m0=\"http://services.authenticate/xsd\">\n" +
                "      <m0:result>success</m0:result>\n" +
                "      <m0:creditCardNo>" + cardNo + "</m0:creditCardNo>\n" +
                "      <m0:messageId>" + messageId + "</m0:messageId>\n" +
                " </m0:anthenticate>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("OUT".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("authenticate".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("authenticateService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime()-3).getBytes()));
        return eventMap;
    }

    private static Map<String, ByteBuffer> createAuthenticationFailureEvenData(String bamActivityId, String cardNo, String messageId) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("authenticationFailureSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:anthenticate xmlns:m0=\"http://services.authenticate/xsd\">\n" +
                "      <m0:result>failure</m0:result>\n" +
                "      <m0:creditCardNo>" + cardNo + "</m0:creditCardNo>\n" +
                "      <m0:reason>Not enough creadit avaliable in the account</m0:reason> \n"+
                "      <m0:messageId>" + messageId + "</m0:messageId>\n" +
                " </m0:anthenticate>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("Failure".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("authenticate".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("authenticateService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime()-3).getBytes()));
        return eventMap;
    }


    private static Map<String, ByteBuffer> createPlaceOrderINEvenData(String bamActivityId, String cardNo, String price, String symbol, String product, String quantity, String msgId) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("placeOrderINSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:placeOrder xmlns:m0=\"http://services.placeOrder/xsd\">\n" +
                "      <m0:creditcardNo>" + cardNo + "</m0:creditcardNo>\n" +
                "      <m0:price>" + price + "</m0:price>\n" +
                "      <m0:symbol>" + symbol + "</m0:symbol>\n" +
                "      <m0:product>" + product + "</m0:product>\n" +
                "      <m0:quantity>" + quantity + "</m0:quantity>\n" +
                "      <m0:messageId>" + msgId + "</m0:messageId>\n" +
                " </m0:placeOrder>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("IN".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("placeOrder".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("OrderService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime()-2).getBytes()));
        return eventMap;
    }


    private static Map<String, ByteBuffer> createPlaceOrderOUTEvenData(String bamActivityId, String cardNo, String product, String quantity, String msgId) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("placeOrderOUTSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:placeOrder xmlns:m0=\"http://services.placeOrder/xsd\">\n" +
                "      <m0:result>success</m0:result>\n" +
                "      <m0:creditCardNo>" + cardNo + "</m0:creditCardNo>\n" +
                "      <m0:product>" + product + "</m0:product>\n" +
                "      <m0:quantity>" + quantity + "</m0:quantity>\n" +
                "      <m0:recieptId>" + UUID.randomUUID().toString() + "</m0:recieptId>\n" +
                "      <m0:messageId>" + msgId + "</m0:messageId>\n" +
                " </m0:placeOrder>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("OUT".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("placeOrder".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("OrderService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime()-1).getBytes()));
        return eventMap;
    }


    private static Map<String, ByteBuffer> createPlaceOrderFailureEvenData(String bamActivityId, String cardNo, String msgId, String product) {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        eventMap.put("bam_activity_id", ByteBuffer.wrap(bamActivityId.getBytes()));
        eventMap.put("bam_current_sequence", ByteBuffer.wrap("placeOrderFailureSequence".getBytes()));
        eventMap.put("message_body", ByteBuffer.wrap(("<soapenv:Body xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <m0:placeOrder xmlns:m0=\"http://services.placeOrder/xsd\">\n" +
                "      <m0:result>failure</m0:result>\n" +
                "      <m0:creditCardNo>"+cardNo+"</m0:creditCardNo>\n" +
                "      <m0:product>"+product+"</m0:product>\n" +
                "      <m0:reason>No enough product in stock</m0:resopn>\n" +
                "      <m0:messageId>"+msgId+"</m0:messageId>\n" +
                " </m0:placeOrder>\n" +
                "</soapenv:Body>").getBytes()));
        eventMap.put("message_direction", ByteBuffer.wrap("Failure".getBytes()));
        eventMap.put("message_id", ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()));
        eventMap.put("operation_name", ByteBuffer.wrap("placeOrder".getBytes()));
        eventMap.put("service_name", ByteBuffer.wrap("OrderService".getBytes()));
        eventMap.put("soap_envelop_namespace", ByteBuffer.wrap("http://www.w3.org/2003/05/soap-envelope".getBytes()));
        eventMap.put("timestamp", ByteBuffer.wrap(String.valueOf(new Date().getTime()-1).getBytes()));
        return eventMap;
    }


}
