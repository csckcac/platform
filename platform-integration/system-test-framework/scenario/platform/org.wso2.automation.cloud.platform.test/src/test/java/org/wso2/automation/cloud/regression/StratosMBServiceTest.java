/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.automation.cloud.regression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.automation.cloud.regression.stratosutils.ServiceLoginClient;
import org.wso2.automation.cloud.regression.stratosutils.msutils.MBThread;
import org.wso2.automation.cloud.regression.stratosutils.msutils.MessageBoxSubClient;
import org.wso2.automation.cloud.regression.stratosutils.msutils.TopicPublisher;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.PriorityQueue;
import java.util.Properties;


public class StratosMBServiceTest {

    private static final Log log = LogFactory.getLog(StratosMBServiceTest.class);

    private static final String QPID_ICF = "org.apache.qpid.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private String userName;
    private String password;

    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String CARBON_DEFAULT_HOSTNAME;
    private static String CARBON_DEFAULT_PORT;

    private String queueName = "testQueueQA2";
    private String topicName = "TestTopic";

    EnvironmentVariables mbServer;
    UserInfo userInfo;

    @BeforeClass
    public void init() {
        EnvironmentBuilder builder = new EnvironmentBuilder().mb(4);
        mbServer = builder.build().getMb();
        userInfo = UserListCsvReader.getUserInfo(4);
        userName = userInfo.getUserName().replaceAll("@", "!");
        password = userInfo.getPassword();
        CARBON_DEFAULT_HOSTNAME = mbServer.getProductVariables().getHostName();
        CARBON_DEFAULT_PORT = mbServer.getProductVariables().getQpidPort();
    }

    @Test
    public void runSuccessCase() throws NamingException, JMSException, InterruptedException {
        String mbServerHostName = CARBON_DEFAULT_HOSTNAME;
        String sessionCookie = ServiceLoginClient.loginChecker(mbServerHostName);

        jmsQueueSenderTestClient();
        MessageBoxSubClient messageBoxSubClient = new MessageBoxSubClient(mbServer.getBackEndUrl(), userInfo);

        messageBoxSubClient.getAccessKeys(sessionCookie);
        Assert.assertTrue(messageBoxSubClient.createMessageBox().contains("testMessageBox"),
                          "Message box created");
        Assert.assertTrue(messageBoxSubClient.subscribe(), "Message box created");
        Assert.assertTrue(messageBoxSubClient.publish(), "Messagebox published");
        Assert.assertTrue(messageBoxSubClient.retriveAndDeleteMessage().contains("Test publish message")
                , "Message retreaved and box is deleted");
        Assert.assertTrue(messageBoxSubClient.deleteMessageBox(), "Messagebox is deleted");
        Assert.assertTrue(messageBoxSubClient.unsubscribe(), "Messagebox unsubcribed");


        java.util.Queue<String> queue = new PriorityQueue<String>();
        int expectedResults = 2;
        Thread msThread = new MBThread(queue);
        msThread.start();


        TopicPublisher topic = new TopicPublisher(userInfo);
        topic.publishMessage();

        int receivedResults = 0;
        while (receivedResults < expectedResults) {
            if (!queue.isEmpty()) {
                String massage = queue.poll();
                if (massage.equalsIgnoreCase("TEST Message")) {
                    Assert.assertTrue(massage.equalsIgnoreCase("TEST Message"), "Message reciveed");
                    break;
                }
                receivedResults++;
            }
            if (receivedResults >= 100) {
                Assert.fail(" Response is absent");

                break;
            }
        }

        Thread.sleep(1000);

    }


    private void jmsQueueSenderTestClient() throws NamingException, JMSException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));

        log.info("TCP ConnectionURL" + getTCPConnectionURL(userName, password));
        
        InitialContext ctx = new InitialContext(properties);

        // Lookup connection factory
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
        QueueConnection queueConnection = connFactory.createQueueConnection();
        queueConnection.start();
        QueueSession queueSession =
                queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

        // Send message
        Queue queue = queueSession.createQueue(queueName + ";{create:always, node:{durable: True}}");

        // create the message to send
        TextMessage textMessage = queueSession.createTextMessage("Test Message Hello");
        javax.jms.QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setTimeToLive(100000000);

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        queueSender.send(textMessage);

        TextMessage message = (TextMessage) queueReceiver.receiveNoWait();
        System.out.println("message.getText() = " + message.getText());


        Assert.assertTrue(message.getText().equals("Test Message Hello"), "jms Queue - retrive messages failed");

        queueSender.close();
        queueSession.close();
        queueConnection.close();

    }

    public static String getTCPConnectionURL(String username, String password) {
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'")
                .toString();
    }
}
