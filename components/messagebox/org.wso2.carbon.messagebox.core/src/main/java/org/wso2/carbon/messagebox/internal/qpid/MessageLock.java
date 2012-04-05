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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of MessageLock is used to keep a JMS message locked during it's visibility timeout period.
 * MessageLock instances are created when ReceiveMessage operation is called and destroyed either
 * when the visibility timeout period is elapsed or when DeleteMessage operation is called.
 * <p/>
 * IMPORTANT : An instance of MessageLock should only be used once as it uses an internal locking/unlocking
 * mechanism.
 */
public class MessageLock {
    private static final Log log = LogFactory.getLog(MessageLock.class);

    private static final long MAX_VISIBILITY_TIMEOUT = 12 * 60 * 60 * 1000; // in milli seconds

    private QueueConnection jmsConnection = null;
    private QueueSession jmsSession = null;
    private Message jmsMessage = null;
    private String receiptHandle = null;
    private ConcurrentHashMap lockStore = null;
    private Timer visibilityTimer = null;
    private long visibilityTimeout = 0; // in milli seconds
    private Date invisibleUntil = null;
    private String messageBoxID = null;
    private boolean lockReleased = false;
    private Scheduler scheduler;

    public MessageLock(QueueConnection jmsConnection,
                       QueueSession jmsSession,
                       Message jmsMessage,
                       long visibilityTimeout)
            throws MessageLockException {
        this.jmsConnection = jmsConnection;
        this.jmsSession = jmsSession;
        this.jmsMessage = jmsMessage;
        this.receiptHandle = UUID.randomUUID().toString();
        this.visibilityTimeout = visibilityTimeout;
        scheduler = new Scheduler();
        invisibleUntil = new Date(System.currentTimeMillis() + visibilityTimeout);
        // Start timer
        startVisibilityTimer();

    }

    /**
     * Set cumulative visibility timeout value
     *
     * @param extension Cumulative timeout value in seconds
     * @throws MessageLockException
     */
    public synchronized void changeMessageVisibility(long extension) throws MessageLockException {
        // Check if the accumulated timeout hits the ceiling
        if ((visibilityTimeout + extension) > MAX_VISIBILITY_TIMEOUT) {
            throw new MessageLockException("Trying to exceed " +
                                           "the maximum permitted visibility timeout");
        }

        // Increase visibility timeout
        visibilityTimeout += extension;

        // Extend timer
        extendVisibilityTimer(extension);
    }


    /**
     * Delete message from JMS queue
     *
     * @throws MessageLockException
     */
    public synchronized void deleteMessage() throws MessageLockException {
        try {
            if (!lockReleased) {
                jmsMessage.acknowledge();
                unlock();
            }
        } catch (JMSException e) {
            throw new MessageLockException("Error while deleting message : " + e.getMessage(), e);
        }
    }


    /**
     * Unlock so that the JMS message becomes visible to others
     */
    private synchronized void unlock() throws MessageLockException {
        try {
            if (!lockReleased) {
                jmsSession.close();
                QueueConnectionManager.getInstance().releaseConnection(jmsConnection);
                lockStore.remove(receiptHandle);
                lockReleased = true;
            }
        } catch (JMSException e) {
            throw new MessageLockException("Error while unlocking message : " + e.getMessage(), e);
        } catch (QueueConnectionManagerException e) {
            throw new MessageLockException("Error while unlocking message : " + e.getMessage(), e);
        }
    }

    /**
     * Starts visibility timer when locked for the very first time
     */
    private void startVisibilityTimer() {
        // Schedule timer
        visibilityTimer = new Timer();
        visibilityTimer.schedule(scheduler, visibilityTimeout);
    }

    /**
     * Extend visibility timer for subsequent calls to change visibility timeout
     *
     * @param extension Time extension in seconds
     */
    private synchronized void extendVisibilityTimer(long extension) {
        // Extend time
        if (!lockReleased) {
            invisibleUntil.setTime(invisibleUntil.getTime() + extension);

            scheduler.setTaskCanceled(true);
            visibilityTimer = new Timer();
            scheduler = new Scheduler();
            visibilityTimer.schedule(scheduler, invisibleUntil);
        }
    }

    public String getMessageBoxID() {
        return messageBoxID;
    }

    public void setMessageBoxID(String messageBoxID) {
        this.messageBoxID = messageBoxID;
    }

    public Message getJmsMessage() {
        return jmsMessage;
    }

    public void setLockStore(ConcurrentHashMap lockStore) {
        this.lockStore = lockStore;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public long getVisibilityTimeout() {
        return visibilityTimeout;
    }


    private class Scheduler extends TimerTask {
        private boolean taskCanceled = false;

        /**
         * Timer task to unlock and release the JMS message
         */
        public void run() {
            if (!taskCanceled) {
                try {
                    unlock();
                } catch (MessageLockException e) {
                    log.error("Failed to unlock the message", e);
                }
            }
        }

        public void setTaskCanceled(boolean taskCanceled) {
            this.taskCanceled = taskCanceled;
        }
    }
}
