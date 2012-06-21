/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.eventbridge.test.thrift;

import junit.framework.TestCase;
import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;
import org.wso2.carbon.eventbridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.exception.AuthenticationException;
import org.wso2.carbon.eventbridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.eventbridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.exception.TransportException;
import org.wso2.carbon.eventbridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.eventbridge.core.exception.EventBridgeException;

import java.net.MalformedURLException;

public class EventTest extends TestCase {


    public void testSendingEvent()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7625);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7625", "admin", "admin");
        String id1 = dataPublisher.defineEventStream("{" +
                                                     "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                     "  'version':'2.3.0'," +
                                                     "  'nickName': 'Stock Quote Information'," +
                                                     "  'description': 'Some Desc'," +
                                                     "  'tags':['foo', 'bar']," +
                                                     "  'metaData':[" +
                                                     "          {'name':'ipAdd','type':'STRING'}" +
                                                     "  ]," +
                                                     "  'correlationData':[" +
                                                     "          {'name':'correlationId','type':'STRING'}" +
                                                     "  ]," +
                                                     "  'payloadData':[" +
                                                     "          {'name':'symbol','type':'STRING'}," +
                                                     "          {'name':'price','type':'DOUBLE'}," +
                                                     "          {'name':'volume','type':'INT'}," +
                                                     "          {'name':'max','type':'DOUBLE'}," +
                                                     "          {'name':'min','type':'Double'}" +
                                                     "  ]" +
                                                     "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingEventSendingNull()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7626);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7626", "admin", "admin");
        String id1 = dataPublisher.defineEventStream("{" +
                                                     "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                     "  'version':'2.3.0'," +
                                                     "  'nickName': 'Stock Quote Information'," +
                                                     "  'description': 'Some Desc'," +
                                                     "  'tags':['foo', 'bar']," +
                                                     "  'metaData':[" +
                                                     "          {'name':'ipAdd','type':'STRING'}" +
                                                     "  ]," +
                                                     "  'correlationData':[" +
                                                     "          {'name':'correlationId','type':'STRING'}" +
                                                     "  ]," +
                                                     "  'payloadData':[" +
                                                     "          {'name':'symbol','type':'STRING'}," +
                                                     "          {'name':'price','type':'DOUBLE'}," +
                                                     "          {'name':'volume','type':'INT'}," +
                                                     "          {'name':'max','type':'DOUBLE'}," +
                                                     "          {'name':'min','type':'Double'}" +
                                                     "  ]" +
                                                     "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"null"}, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingMultipleEventsOfSameType()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7612);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7612", "admin", "admin");
        String streamId = dataPublisher.defineEventStream("{" +
                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                          "  'version':'1.3.0'," +
                                                          "  'nickName': 'Stock Quote Information'," +
                                                          "  'description': 'Some Desc'," +
                                                          "  'metaData':[" +
                                                          "          {'name':'ipAdd','type':'STRING'}" +
                                                          "  ]," +
                                                          "  'payloadData':[" +
                                                          "          {'name':'symbol','type':'STRING'}," +
                                                          "          {'name':'price','type':'DOUBLE'}," +
                                                          "          {'name':'volume','type':'INT'}," +
                                                          "          {'name':'max','type':'DOUBLE'}," +
                                                          "          {'name':'min','type':'Double'}" +
                                                          "  ]" +
                                                          "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self 
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingMultipleEventsOfDifferentType()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7613);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7713", "tcp://localhost:7613", "admin", "admin");
        String streamId = dataPublisher.defineEventStream("{" +
                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                          "  'version':'2.3.0'," +
                                                          "  'nickName': 'Stock Quote Information'," +
                                                          "  'description': 'Some Desc'," +
                                                          "  'metaData':[" +
                                                          "          {'name':'ipAdd','type':'STRING'}" +
                                                          "  ]," +
                                                          "  'payloadData':[" +
                                                          "          {'name':'symbol','type':'STRING'}," +
                                                          "          {'name':'price','type':'DOUBLE'}," +
                                                          "          {'name':'volume','type':'INT'}," +
                                                          "          {'name':'max','type':'DOUBLE'}," +
                                                          "          {'name':'min','type':'Double'}" +
                                                          "  ]" +
                                                          "}");
        String shortStreamId = dataPublisher.defineEventStream("{" +
                                                               "  'name':'org.wso2.esb.MediatorStatisticsShort'," +
                                                               "  'version':'2.0.0'," +
                                                               "  'nickName': 'Short Stock Quote Information'," +
                                                               "  'description': 'Some Desc'," +
                                                               "  'metaData':[" +
                                                               "          {'name':'ipAdd','type':'STRING'}" +
                                                               "  ]," +
                                                               "  'payloadData':[" +
                                                               "          {'name':'symbol','type':'STRING'}," +
                                                               "          {'name':'price','type':'DOUBLE'}," +
                                                               "          {'name':'volume','type':'INT'}" +
                                                               "  ]" +
                                                               "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(shortStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

//    public void testSendingWrongEvents()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, EventBridgeException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7618);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7618", "admin", "admin");
//        String streamId = dataPublisher.definedEventStream("{" +
//                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
//                                                          "  'version':'1.3.0'," +
//                                                          "  'nickName': 'Stock Quote Information'," +
//                                                          "  'description': 'Some Desc'," +
//                                                          "  'metaData':[" +
//                                                          "          {'name':'ipAdd','type':'STRING'}" +
//                                                          "  ]," +
//                                                          "  'payloadData':[" +
//                                                          "          {'name':'symbol','type':'STRING'}," +
//                                                          "          {'name':'price','type':'DOUBLE'}," +
//                                                          "          {'name':'volume','type':'INT'}," +
//                                                          "          {'name':'max','type':'DOUBLE'}," +
//                                                          "          {'name':'min','type':'Double'}" +
//                                                          "  ]" +
//                                                          "}");
//        //In this case correlation data is null
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6});
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }

    public void testRequestingEventStreamId()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException, NoStreamDefinitionExistException {

        TestServer testServer = new TestServer();
        testServer.start(7619);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7619", "admin", "admin");
        dataPublisher.defineEventStream("{" +
                                        "  'name':'org.wso2.esb.MediatorStatistics'," +
                                        "  'version':'1.3.0'," +
                                        "  'nickName': 'Stock Quote Information'," +
                                        "  'description': 'Some Desc'," +
                                        "  'metaData':[" +
                                        "          {'name':'ipAdd','type':'STRING'}" +
                                        "  ]," +
                                        "  'payloadData':[" +
                                        "          {'name':'symbol','type':'STRING'}," +
                                        "          {'name':'price','type':'DOUBLE'}," +
                                        "          {'name':'volume','type':'INT'}," +
                                        "          {'name':'max','type':'DOUBLE'}," +
                                        "          {'name':'min','type':'Double'}" +
                                        "  ]" +
                                        "}");
        String receivedStreamId = dataPublisher.findEventStream("org.wso2.esb.MediatorStatistics", "1.3.0");
        //In this case correlation data is null
        dataPublisher.publish(receivedStreamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 20.6});
        dataPublisher.publish(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
//        else the user can publish event it self
        dataPublisher.publish(new Event(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingSecureEventsOfSameType()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7620);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7720", "admin", "admin");
        Thread.sleep(2000);
        String streamId = dataPublisher.defineEventStream("{" +
                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                          "  'version':'1.3.0'," +
                                                          "  'nickName': 'Stock Quote Information'," +
                                                          "  'description': 'Some Desc'," +
                                                          "  'metaData':[" +
                                                          "          {'name':'ipAdd','type':'STRING'}" +
                                                          "  ]," +
                                                          "  'payloadData':[" +
                                                          "          {'name':'symbol','type':'STRING'}," +
                                                          "          {'name':'price','type':'DOUBLE'}," +
                                                          "          {'name':'volume','type':'INT'}," +
                                                          "          {'name':'max','type':'DOUBLE'}," +
                                                          "          {'name':'min','type':'Double'}" +
                                                          "  ]" +
                                                          "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingSecureEventsByDefiningAllUrls()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7621);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7721", "ssl://localhost:7721", "admin", "admin");
        String streamId = dataPublisher.defineEventStream("{" +
                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                          "  'version':'1.3.0'," +
                                                          "  'nickName': 'Stock Quote Information'," +
                                                          "  'description': 'Some Desc'," +
                                                          "  'metaData':[" +
                                                          "          {'name':'ipAdd','type':'STRING'}" +
                                                          "  ]," +
                                                          "  'payloadData':[" +
                                                          "          {'name':'symbol','type':'STRING'}," +
                                                          "          {'name':'price','type':'DOUBLE'}," +
                                                          "          {'name':'volume','type':'INT'}," +
                                                          "          {'name':'max','type':'DOUBLE'}," +
                                                          "          {'name':'min','type':'Double'}" +
                                                          "  ]" +
                                                          "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self
        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSessionTimeOut()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   InterruptedException, EventBridgeException,
                   MalformedStreamDefinitionException,
                   StreamDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7631);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7631", "admin", "admin");
        String streamId = dataPublisher.defineEventStream("{" +
                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
                                                          "  'version':'1.3.0'," +
                                                          "  'nickName': 'Stock Quote Information'," +
                                                          "  'description': 'Some Desc'," +
                                                          "  'metaData':[" +
                                                          "          {'name':'ipAdd','type':'STRING'}" +
                                                          "  ]," +
                                                          "  'payloadData':[" +
                                                          "          {'name':'symbol','type':'STRING'}," +
                                                          "          {'name':'price','type':'DOUBLE'}," +
                                                          "          {'name':'volume','type':'INT'}," +
                                                          "          {'name':'max','type':'DOUBLE'}," +
                                                          "          {'name':'min','type':'Double'}" +
                                                          "  ]" +
                                                          "}");
        //In this case correlation data is null
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        Thread.sleep(1000);
        testServer.stop();
        Thread.sleep(10000);
        testServer.start(7631);
        Thread.sleep(3000);
        //Stream is again defined here because we are using inmMemoryDataStore at the server and it wont persist data
        // when using casandra the defineEventStream() is not needed
        dataPublisher.defineEventStream("{" +
                                        "  'name':'org.wso2.esb.MediatorStatistics'," +
                                        "  'version':'1.3.0'," +
                                        "  'nickName': 'Stock Quote Information'," +
                                        "  'description': 'Some Desc'," +
                                        "  'metaData':[" +
                                        "          {'name':'ipAdd','type':'STRING'}" +
                                        "  ]," +
                                        "  'payloadData':[" +
                                        "          {'name':'symbol','type':'STRING'}," +
                                        "          {'name':'price','type':'DOUBLE'}," +
                                        "          {'name':'volume','type':'INT'}," +
                                        "          {'name':'max','type':'DOUBLE'}," +
                                        "          {'name':'min','type':'Double'}" +
                                        "  ]" +
                                        "}");   Thread.sleep(1000);
        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();

    }

}
