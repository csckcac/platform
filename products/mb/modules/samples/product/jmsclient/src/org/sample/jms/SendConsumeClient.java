/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.sample.jms;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.*;
import java.util.Properties;

public class SendConsumeClient {

    public void sendMessage() {

        Properties initialContextProperties = new Properties();
        String queueName ="myQueue";
        initialContextProperties.put("java.naming.factory.initial",
                "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        String connectionString = "amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5672'";
        initialContextProperties.put("connectionfactory.qpidConnectionfactory", connectionString);
        initialContextProperties.put("queue."+queueName, queueName);

        try {
            InitialContext initialContext = new InitialContext(initialContextProperties);
            QueueConnectionFactory queueConnectionFactory
                    = (QueueConnectionFactory) initialContext.lookup("qpidConnectionfactory");

            QueueConnection queueConnection = queueConnectionFactory.createQueueConnection();
            queueConnection.start();

            QueueSession queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // first send three messages.
            TextMessage textMessage = queueSession.createTextMessage("My test message");

            // Send message
            Queue queue = (Queue)initialContext.lookup(queueName);
            javax.jms.QueueSender queueSender = queueSession.createSender(queue);
            queueSender.send(textMessage);

            // Housekeeping
            queueSender.close();
            queueSession.close();
            queueConnection.stop();
            queueConnection.close();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void consumeMessage() {

        Properties initialContextProperties = new Properties();
        String queueName = "myQueue";
        initialContextProperties.put("java.naming.factory.initial",
                "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        String connectionString = "amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5672'";
        initialContextProperties.put("connectionfactory.qpidConnectionfactory", connectionString);
        initialContextProperties.put("queue."+queueName, queueName);


        try {
            InitialContext initialContext = new InitialContext(initialContextProperties);
            QueueConnectionFactory queueConnectionFactory
                    = (QueueConnectionFactory) initialContext.lookup("qpidConnectionfactory");
            QueueConnection queueConnection = queueConnectionFactory.createQueueConnection();
            queueConnection.start();

            QueueSession queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Receive message
            Queue queue = (Queue)initialContext.lookup(queueName);
            MessageConsumer queueReceiver = queueSession.createConsumer(queue);

            TextMessage textMessage = (TextMessage) queueReceiver.receive(1000);
            System.out.println("Got message ==> " + textMessage.getText());

            queueReceiver.close();
            queueSession.close();
            queueConnection.stop();
            queueConnection.close();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        SendConsumeClient sendConsumeClient = new SendConsumeClient();
        sendConsumeClient.sendMessage();
        sendConsumeClient.consumeMessage();

    }
}
