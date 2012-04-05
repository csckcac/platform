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
package org.wso2.carbon.message.store.persistence.jms.util;


import javax.jms.*;

/**
 *
 */
public class JMSUtil {


    public static Connection createConnection(ConnectionFactory connectionFactory , String userName ,
                                       String password , boolean jmsSpec11) throws JMSException {
        Connection connection = null;

        if(jmsSpec11) {
            if(userName != null && password != null) {
                connection = connectionFactory.createConnection(userName,password);
            } else {
                connection = connectionFactory.createConnection();
            }
        } else {
            QueueConnectionFactory qConFac = (QueueConnectionFactory)connectionFactory;
            if(userName !=null && password != null) {
                connection = qConFac.createQueueConnection(userName,password);
            } else {
                connection = qConFac.createQueueConnection();
            }
        }

        return connection;
    }


    /**
     * Create a Message Producer given the destination
     * @param session JMS Session to use
     * @param destination JMS Destination to use
     * @param jmsSpec11 should we use the JMS 1.1 API?
     * @return MessageProducer instance created
     * @throws JMSException
     */
    public static MessageProducer createProducer(Session session , Destination destination ,
                                          boolean jmsSpec11) throws JMSException {
        if(jmsSpec11) {
            return session.createProducer(destination);
        } else {
            return ((QueueSession)session).createSender((Queue)destination);
        }
    }

    /**
     * Create a MessageConsumer given the destination
     * @param session JMS Session to be used
     * @param destination  JMS Destination to be used
     * @param jmsSpec11 should we use the JMS 1.1 API?
     * @return MessageConsumer instance created
     * @throws JMSException
     */
    public static MessageConsumer createConsumer(Session session , Destination destination ,
                                          boolean jmsSpec11) throws JMSException {
        if(jmsSpec11) {
            return session.createConsumer(destination);
        } else {
            return ((QueueSession) session).createReceiver((Queue) destination);
        }
    }

    /**
     * Create a JMS session given the Connection
     * @param connection  JMS Connection to be used
     * @param jmsSpec11   should we use the JMS 1.1 API?
     * @return  JMS Session created
     * @throws JMSException
     */
    public static Session createSession(Connection connection , boolean jmsSpec11) throws JMSException {
        if(jmsSpec11) {
            return connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        } else {
            return ((QueueConnection) connection).createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
        }
    }


}
