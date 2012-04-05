/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.agent.server.oneserver_multiclient;

import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.TypeDef;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentTypeDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.exception.WrongEventTypeException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.server.KeyStoreUtil;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Client of multiple client single server test
 */
public class AgentClient implements Runnable {
    static int NO_OF_EVENTS = 100000;
    static int NO_OF_CLIENT = 1;
    int stable = 0;
    Agent agent;

    public static void main(String[] args)
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentTypeDefinitionAlreadyDefinedException, WrongEventTypeException, InterruptedException {
        KeyStoreUtil.setTrustStoreParams();
        if (args.length != 0 && args[0] != null) {
            NO_OF_EVENTS = Integer.parseInt(args[0]);
        }
        if (args.length != 0 && args[1] != null) {
            NO_OF_CLIENT = Integer.parseInt(args[1]);
        }
        NO_OF_EVENTS = NO_OF_EVENTS / NO_OF_CLIENT;
        System.out.println("Event no=" + NO_OF_EVENTS);
        System.out.println("Client no=" + NO_OF_CLIENT);
        Agent agent = new Agent();
        for (int i = 0; i < NO_OF_CLIENT; i++) {
            Thread thread = new Thread(new AgentClient(1000000 / NO_OF_CLIENT, agent));
            thread.start();
        }
    }

    public AgentClient(int stable, Agent agent) {
        this.stable = stable;
        this.agent = agent;
    }

    public void run() {
//        try {
//            ReceiverConfiguration receiverConfiguration = generateReciverConf();
//            DataPublisher dataPublisher = new DataPublisher(receiverConfiguration);
//            dataPublisher.connect();
//            dataPublisher.eventStreamDefinition(createTypeDef());
//            for (int i = 0; i < NO_OF_EVENTS + stable; i++) {
//                dataPublisher.publish(generateEvent());
//            }
////            Thread.sleep(10000);
////            dataPublisher.disconnect();
////            agent.shutdown();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private Event generateEvent() {
        Event event = new Event();
        event.setStreamId("StockQuart");
        event.setTimeStamp(System.currentTimeMillis());

        event.setMetaData(createMetaData());
        event.setCorrelationData(createCorrelationData());
        event.setPayloadData(createPayloadData());
        return event;
    }


//    private ReceiverConfiguration generateReciverConf() {
//        ReceiverConfiguration receiverConfiguration = new ReceiverConfiguration();
//        receiverConfiguration.setUserName("admin");
//        receiverConfiguration.setPassword("admin");
//        receiverConfiguration.setAuthenticatorIp("localhost");
//        receiverConfiguration.setAuthenticatorPort(7611);
//        receiverConfiguration.setEventReceiverIp("localhost");
//        receiverConfiguration.setEventReceiverPort(7711);
//
//        return receiverConfiguration;
//    }

    private TypeDef createTypeDef() {
        TypeDef typeDef = new TypeDef();
        typeDef.setStreamId("StockQuart");
        List<Attribute> payloadData = new LinkedList<Attribute>();
        payloadData.add(new Attribute("symbol", AttributeType.STRING));
        payloadData.add(new Attribute("price", AttributeType.DOUBLE));
        payloadData.add(new Attribute("volume", AttributeType.INT));
        payloadData.add(new Attribute("max", AttributeType.DOUBLE));
        payloadData.add(new Attribute("min", AttributeType.DOUBLE));


        List<Attribute> metaData = new LinkedList<Attribute>();
        metaData.add(new Attribute("ipAdd", AttributeType.STRING));

        typeDef.setPayloadData(payloadData);
        typeDef.setMetaData(metaData);
        return typeDef;
    }

    private Object[] createMetaData() {
        Object[] objects = new Object[1];
        objects[0] = "127.0.0.1";
        return objects;
    }

    private Object[] createCorrelationData() {
        return null;
    }

    private Object[] createPayloadData() {
        Object[] objects = new Object[5];
        objects[0] = "IBM";
        objects[1] = 76.5;
        objects[2] = 234;
        objects[3] = 89.3;
        objects[4] = 70.5;
        return objects;
    }

}
