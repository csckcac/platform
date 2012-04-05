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

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class StratosCEPServiceTest {

    private static final Log log = LogFactory.getLog(StratosCEPServiceTest.class);

    private static final String QPID_ICF = "org.apache.qpid.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static String userName;
    private static String password;

    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String CARBON_DEFAULT_HOSTNAME;
    private static String CARBON_DEFAULT_PORT;
    private static String queueName = "testQueueQA3";


    @BeforeClass
    public void init() {
        EnvironmentBuilder builder = new EnvironmentBuilder().cep(4);
        EnvironmentVariables cepServer = builder.build().getCep();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        userName = userInfo.getUserName().replaceAll("@", "!");
        password = userInfo.getPassword();
        CARBON_DEFAULT_HOSTNAME = cepServer.getProductVariables().getHostName();
        CARBON_DEFAULT_PORT = cepServer.getProductVariables().getQpidPort();
    }

    @Test
    public void jmsQueueSenderTest() throws NamingException, JMSException {
        jmsQueueSenderTestClient();
    }


    private void jmsQueueSenderTestClient() throws NamingException, JMSException {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));

        System.out.println("getTCPConnectionURL(userName,password) = "
                           + getTCPConnectionURL(userName, password));

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

    private static String getTCPConnectionURL(String username, String password) {
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(CARBON_DEFAULT_HOSTNAME)
                .append(":").append(CARBON_DEFAULT_PORT).append("'")
                .toString();
    }
}
