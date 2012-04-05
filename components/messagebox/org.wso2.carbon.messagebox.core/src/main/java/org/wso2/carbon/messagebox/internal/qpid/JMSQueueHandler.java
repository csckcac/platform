/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.messagebox.internal.qpid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.internal.utils.Utils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations on JMS queues are implemented here.
 */
public class JMSQueueHandler {
    private static final Log log = LogFactory.getLog(JMSQueueHandler.class);

    /**
     * Send a message to the given queue
     *
     * @param queueName  Name of the queue queue the message is sent to
     * @param sqsMessage Message instance
     * @throws JMSQueueHandlerException
     */
    public static void pushMessage(String queueName, SQSMessage sqsMessage, String userName)
            throws JMSQueueHandlerException {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        QueueSender queueSender = null;
        try {
            // Grab connection and create new session
            queueConnection = QueueConnectionManager.getInstance().getConnection(Utils.getTenantAwareCurrentUserName());
            queueConnection.start();
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Send message
            Queue queue = getDurableQueue(queueName, queueSession);

            // create the message to send
            TextMessage textMessage = queueSession.createTextMessage(sqsMessage.getBody());

            if (sqsMessage.getMd5ofMessageBody() != null) {
                textMessage.setStringProperty(
                        MessageBoxConstants.JMS_MESSAGE_PROPERTY_MD5_OF_MESSAGE,
                        sqsMessage.getMd5ofMessageBody());
            }
            textMessage.setStringProperty(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID,
                                          sqsMessage.getAttribute().get(
                                                  MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID));
            textMessage.setStringProperty(MessageBoxConstants.JMS_MESSAGE_PROPERTY_VISIBILITY_TIME_OUT,
                                          Long.toString(sqsMessage.getDefaultVisibilityTimeout()));
            textMessage.setStringProperty(MessageBoxConstants.JMS_MESSAGE_PROPERTY_SENT_TIMESTAMP,
                                          String.valueOf(System.currentTimeMillis()));
            textMessage.setStringProperty(MessageBoxConstants.JMS_MESSAGE_PROPERTY_MESSAGE_ID,
                                          sqsMessage.getMessageId());
            String receivedCount = sqsMessage.getAttribute().get(
                    MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT);
            textMessage.setStringProperty(MessageBoxConstants.JMS_MESSAGE_PROPERTY_RECEIVED_COUNT,
                                          (receivedCount != null) ? receivedCount : "0");

            queueSender = queueSession.createSender(queue);
            queueSender.send(textMessage);


        } catch (QueueConnectionManagerException e) {
            throw new JMSQueueHandlerException(e);
        } catch (JMSException e) {
            throw new JMSQueueHandlerException(e);
        } finally {
            if (queueSender != null) {
                try {
                    queueSender.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue sender.", e);
                }
            }
            if (queueSession != null) {
                try {
                    queueSession.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue session.", e);
                }
            }
            if (queueConnection != null) {
                try {
                    QueueConnectionManager.getInstance().releaseConnection(queueConnection);
                } catch (QueueConnectionManagerException e) {
                    log.error("Failed to close queue connection.", e);
                }
            }
        }
    }

    public static String getJMSQueueName(String queueName) {
        return queueName.split("/")[1];
    }

    /**
     * Pop out the message from given queue
     *
     * @param messageBoxID      Name of the message which the massage is removed from
     * @param visibilityTimeout
     * @return MessageLock instance for the popped out message or null if no message returned
     * @throws JMSQueueHandlerException
     */
    public static MessageLock popMessage(String messageBoxID, long visibilityTimeout)
            throws JMSQueueHandlerException {
        try {
            // Grab connection and create new session
            QueueConnection queueConnection = QueueConnectionManager.getInstance().getConnection(
                    Utils.getTenantAwareCurrentUserName());
            queueConnection.start();

            QueueSession queueSession =
                    queueConnection.createQueueSession(false, QueueSession.CLIENT_ACKNOWLEDGE);

            // Receive message
            Queue queue = getDurableQueue(messageBoxID, queueSession);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive(
                    MessageBoxConstants.MESSAGE_RECEIVE_WAIT_TIME);

            MessageLock lock = null;
            if (message != null) {
                setVisibilityTimeoutStringProperty(visibilityTimeout, message);
                // Crate lock
                lock = new MessageLock(queueConnection,
                                       queueSession,
                                       message,
                                       visibilityTimeout);
                lock.setMessageBoxID(messageBoxID);

                queueReceiver.close();
            } else {
                queueReceiver.close();
                queueSession.close();
                QueueConnectionManager.getInstance().releaseConnection(queueConnection);
            }
            return lock;
        } catch (QueueConnectionManagerException e) {
            throw new JMSQueueHandlerException(e);
        } catch (JMSException e) {
            throw new JMSQueueHandlerException(e);
        } catch (MessageLockException e) {
            throw new JMSQueueHandlerException(e);
        }
    }

    public static void setVisibilityTimeoutStringProperty(long visibilityTimeout, Message message)
            throws JMSQueueHandlerException {

        Enumeration enumeration;
        Map<String, String> propertyMap = new HashMap<String, String>();
        try {
            enumeration = message.getPropertyNames();
            while (enumeration.hasMoreElements()) {
                String propertyName = enumeration.nextElement().toString();
                propertyMap.put(propertyName, message.getStringProperty(propertyName));
            }
            message.clearProperties();
            for (Map.Entry entry : propertyMap.entrySet()) {
                message.setStringProperty(entry.getKey().toString(), entry.getValue().toString());

            }
            message.setStringProperty(MessageBoxConstants.JMS_MESSAGE_PROPERTY_VISIBILITY_TIME_OUT,
                                      String.valueOf(visibilityTimeout + System.currentTimeMillis()));
        } catch (JMSException e) {
            throw new JMSQueueHandlerException(e);
        }
    }

    /**
     * Browse all messages in given queue
     *
     * @param queueName Name of the queue to be browsed
     * @return List of JMS messages
     * @throws JMSQueueHandlerException If queue browsing fails
     */
    public static List<Message> browse(String queueName) throws JMSQueueHandlerException {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        QueueBrowser queueBrowser = null;
        try {
            // Grab connection and create new session
            queueConnection = QueueConnectionManager.getInstance().getConnection(
                    Utils.getTenantAwareCurrentUserName());
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Create browser
            Queue queue = getDurableQueue(queueName, queueSession);
            queueBrowser = queueSession.createBrowser(queue);
            queueConnection.start();

            // Browse queue
            Enumeration messageEnumeration = queueBrowser.getEnumeration();
            List<Message> messageList = new ArrayList<Message>();
            while (messageEnumeration.hasMoreElements()) {
                messageList.add((Message) messageEnumeration.nextElement());
            }

            return messageList;
        } catch (QueueConnectionManagerException e) {
            throw new JMSQueueHandlerException(e);
        } catch (JMSException e) {
            throw new JMSQueueHandlerException(e);
        } finally {
            if (queueBrowser != null) {
                try {
                    queueBrowser.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue browser.", e);
                }
            }
            if (queueSession != null) {
                try {
                    queueSession.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue session.", e);
                }
            }
            if (queueConnection != null) {
                try {
                    QueueConnectionManager.getInstance().releaseConnection(queueConnection);
                } catch (QueueConnectionManagerException e) {
                    log.error("Failed to close queue connection.", e);
                }
            }
        }
    }

    /**
     * Clear all messages in the queue
     *
     * @param queueName Name of the queue to be cleared
     * @throws JMSQueueHandlerException
     */
    public static void clearQueue(String queueName) throws JMSQueueHandlerException {
        QueueConnection queueConnection = null;
        QueueSession queueSession = null;
        QueueReceiver queueReceiver = null;
        try {
            // Grab connection and create new session
            queueConnection = QueueConnectionManager.getInstance().getConnection(
                    Utils.getTenantAwareCurrentUserName());
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            // Create receiver
            Queue queue = getDurableQueue(queueName, queueSession);
            queueReceiver = queueSession.createReceiver(queue);
            queueConnection.start();

            // Pop all messages out
            while (queueReceiver.receive(
                    MessageBoxConstants.MESSAGE_RECEIVE_WAIT_TIME) != null) {
                // No action on messages
            }

        } catch (QueueConnectionManagerException e) {
            throw new JMSQueueHandlerException(e);
        } catch (JMSException e) {
            throw new JMSQueueHandlerException(e);
        } finally {
            if (queueReceiver != null) {
                try {
                    queueReceiver.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue receiver", e);
                }
            }
            if (queueSession != null) {
                try {
                    queueSession.close();
                } catch (JMSException e) {
                    log.error("Failed to close queue session", e);
                }
            }
            if (queueConnection != null) {
                try {
                    QueueConnectionManager.getInstance().releaseConnection(queueConnection);
                } catch (QueueConnectionManagerException e) {
                    log.error("Failed to close queue connection", e);
                }
            }
        }

    }

    /**
     * Create durable queues
     *
     * @param queueName    - queue name
     * @param queueSession - queue session by which queue is created
     * @return created Queue, jms type queue
     * @throws JMSException - if fails to create queue
     */
    private static Queue getDurableQueue(String queueName, QueueSession queueSession)
            throws JMSException {
        queueName = Utils.getTenantBasedQueueName(getJMSQueueName(queueName));
        return queueSession.createQueue(queueName +
                                        ";{create:always, node:{durable: True}}");
    }
}
