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

package org.wso2.carbon.eventbridge.core;

import junit.framework.TestCase;

public class EventTest extends TestCase {
//
//    public void testSendingMultipleEventsOfSameType()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, AgentServerException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7612);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7612", "admin", "admin");
//        String streamId = dataPublisher.defineEventStream("{" +
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
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
//        //else the user can publish event it self
//        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }
//
//    public void testSendingMultipleEventsOfDifferentType()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, AgentServerException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7613);
//        KeyStoreUtil.setTrustStoreParams();
//        Thread.sleep(2000);
//
//        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7713","tcp://localhost:7613", "admin", "admin");
//        String streamId = dataPublisher.defineEventStream("{" +
//                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
//                                                          "  'version':'2.3.0'," +
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
//        String shortStreamId = dataPublisher.defineEventStream("{" +
//                                                               "  'name':'org.wso2.esb.MediatorStatisticsShort'," +
//                                                               "  'version':'2.0.0'," +
//                                                               "  'nickName': 'Short Stock Quote Information'," +
//                                                               "  'description': 'Some Desc'," +
//                                                               "  'metaData':[" +
//                                                               "          {'name':'ipAdd','type':'STRING'}" +
//                                                               "  ]," +
//                                                               "  'payloadData':[" +
//                                                               "          {'name':'symbol','type':'STRING'}," +
//                                                               "          {'name':'price','type':'DOUBLE'}," +
//                                                               "          {'name':'volume','type':'INT'}" +
//                                                               "  ]" +
//                                                               "}");
//        //In this case correlation data is null
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(shortStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200});
//        //else the user can publish event it self
//        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
//        Thread.sleep(3000);
//        dataPublisher.stop();
//        testServer.stop();
//    }
//
////    public void testSendingWrongEvents()
////            throws MalformedURLException, AuthenticationException, TransportException,
////                   AgentException, UndefinedEventTypeException,
////                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
////                   InterruptedException, AgentServerException,
////                   MalformedStreamDefinitionException,
////                   StreamDefinitionException {
////
////        TestServer testServer = new TestServer();
////        testServer.start(7618);
////        KeyStoreUtil.setTrustStoreParams();
////
////        Thread.sleep(2000);
////        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
////        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7618", "admin", "admin");
////        String streamId = dataPublisher.definedEventStream("{" +
////                                                          "  'name':'org.wso2.esb.MediatorStatistics'," +
////                                                          "  'version':'1.3.0'," +
////                                                          "  'nickName': 'Stock Quote Information'," +
////                                                          "  'description': 'Some Desc'," +
////                                                          "  'metaData':[" +
////                                                          "          {'name':'ipAdd','type':'STRING'}" +
////                                                          "  ]," +
////                                                          "  'payloadData':[" +
////                                                          "          {'name':'symbol','type':'STRING'}," +
////                                                          "          {'name':'price','type':'DOUBLE'}," +
////                                                          "          {'name':'volume','type':'INT'}," +
////                                                          "          {'name':'max','type':'DOUBLE'}," +
////                                                          "          {'name':'min','type':'Double'}" +
////                                                          "  ]" +
////                                                          "}");
////        //In this case correlation data is null
////        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6});
////        Thread.sleep(3000);
////
////        dataPublisher.stop();
////        testServer.stop();
////
////    }
//
//    public void testRequestingEventStreamId()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, AgentServerException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException, NoStreamDefinitionExistException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7619);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7619", "admin", "admin");
//        dataPublisher.defineEventStream("{" +
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
//        String receivedStreamId = dataPublisher.findEventStream("org.wso2.esb.MediatorStatistics", "1.3.0");
//        //In this case correlation data is null
//        dataPublisher.publish(receivedStreamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6,20.6});
//        dataPublisher.publish(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
////        else the user can publish event it self
//        dataPublisher.publish(new Event(receivedStreamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }
//
//    public void testSendingSecureEventsOfSameType()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, AgentServerException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7620);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7720", "admin", "admin");
//        Thread.sleep(2000);
//        String streamId = dataPublisher.defineEventStream("{" +
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
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
//        //else the user can publish event it self
//        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }
//    public void testSendingSecureEventsByDefiningAllUrls()
//            throws MalformedURLException, AuthenticationException, TransportException,
//                   AgentException, UndefinedEventTypeException,
//                   DifferentStreamDefinitionAlreadyDefinedException, WrongEventTypeException,
//                   InterruptedException, AgentServerException,
//                   MalformedStreamDefinitionException,
//                   StreamDefinitionException {
//
//        TestServer testServer = new TestServer();
//        testServer.start(7621);
//        KeyStoreUtil.setTrustStoreParams();
//
//        Thread.sleep(2000);
//        //according to the convention the authentication port will be 7612+100= 7711 and its host will be the same
//        DataPublisher dataPublisher = new DataPublisher("ssl://localhost:7721","ssl://localhost:7721", "admin", "admin");
//        String streamId = dataPublisher.defineEventStream("{" +
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
//        dataPublisher.publish(streamId, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
//        dataPublisher.publish(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
//        //else the user can publish event it self
//        dataPublisher.publish(new Event(streamId, System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
//        Thread.sleep(3000);
//
//        dataPublisher.stop();
//        testServer.stop();
//
//    }

}
