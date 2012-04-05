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
package org.wso2.carbon.agent.server;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.wso2.carbon.agent.DataPublisher;
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

public class StreamDefinitionTest extends TestCase {

    public void testSendingSameStreamDefinitions()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   DifferentTypeDefinitionAlreadyDefinedException, WrongEventTypeException,
                   InterruptedException, AgentServerException, MalformedTypeDefinitionException,
                   TypeDefinitionException {

        TestServer testServer = new TestServer();
        testServer.start(7614);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7614", "admin", "admin");
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
        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

    public void testSendingTwoDifferentStreamDefinitionsWithSameStreamId()
            throws MalformedURLException, AuthenticationException, TransportException,
                   AgentException, UndefinedEventTypeException,
                   WrongEventTypeException,
                   InterruptedException, AgentServerException, MalformedTypeDefinitionException,
                   TypeDefinitionException, DifferentTypeDefinitionAlreadyDefinedException {

        TestServer testServer = new TestServer();
        testServer.start(7615);
        KeyStoreUtil.setTrustStoreParams();
        Thread.sleep(2000);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7615", "admin", "admin");
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
        Boolean exceptionOccurred=false;
        try {
            dataPublisher.defineEventStreamDefinition("{" +
                                                      "  'streamId':'StockQuart'," +
                                                      "  'metaData':[" +
                                                      "          {'name':'ipAdd','type':'STRING'}" +
                                                      "  ]," +
                                                      "  'payloadData':[" +
                                                      "          {'name':'symbol','type':'STRING'}," +
                                                      "          {'name':'price','type':'DOUBLE'}," +
                                                      "          {'name':'volume','type':'INT'}" +
                                                      "  ]" +
                                                      "}");

        } catch (DifferentTypeDefinitionAlreadyDefinedException e) {
            exceptionOccurred=true;
        }
        Assert.assertTrue(exceptionOccurred);

        Thread.sleep(3000);
        dataPublisher.stop();
        testServer.stop();
    }

}
