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

package org.wso2.carbon.agent.server;

import junit.framework.TestCase;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.DifferentTypeDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedTypeDefinitionException;
import org.wso2.carbon.agent.commons.exception.TypeDefinitionException;
import org.wso2.carbon.agent.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.agent.commons.exception.WrongEventTypeException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.server.exception.AgentServerException;

import java.net.MalformedURLException;
import java.util.Random;

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
public class EventTest extends TestCase {
    static Random random = new Random();

    public void testSendingMultipleEventsOfSameType()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentTypeDefinitionAlreadyDefinedException, WrongEventTypeException,
                   InterruptedException, AgentServerException, MalformedTypeDefinitionException,
                   TypeDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7612);
        KeyStoreUtil.setTrustStoreParams();

        Thread.sleep(2000);
        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7612", "admin", "admin");
        dataPublisher.defineEventStreamDefinition("{" +
                                                  "  'streamId':'StockQuart'," +
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
        dataPublisher.publish("StockQuart", new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish("StockQuart", System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7});
        //else the user can publish event it self 
        dataPublisher.publish(new Event("StockQuart", System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);

        dataPublisher.stop();
        testServer.stop();

    }

    public void testSendingMultipleEventsOfDifferentType()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentTypeDefinitionAlreadyDefinedException, WrongEventTypeException,
                   InterruptedException, AgentServerException, MalformedTypeDefinitionException,
                   TypeDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7613);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7613", "admin", "admin");
        dataPublisher.defineEventStreamDefinition("{" +
                                                  "  'streamId':'StockQuart'," +
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
        dataPublisher.defineEventStreamDefinition("{" +
                                                  "  'streamId':'StockQuartShort'," +
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
        dataPublisher.publish("StockQuart", new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.publish("StockQuartShort", System.currentTimeMillis(), new Object[]{"127.0.0.2"}, null, new Object[]{"WSO2", 100.8, 200});
        //else the user can publish event it self
        dataPublisher.publish(new Event("StockQuart", System.currentTimeMillis(), new Object[]{"127.0.0.3"}, null, new Object[]{"WSO2", 100.8, 200, 110.4, 74.7}));
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

}
