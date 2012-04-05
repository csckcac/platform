package org.wso2.carbon.messagebox.internal.inmemory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxDetails;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageDetails;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

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
public class MessageBox {
    private static final Log log = LogFactory.getLog(MessageBox.class);
    // MessageBox Id
    private String queueId;

    // Messages are stored in this Queue
    private Deque<SQSMessage> SQSMessageQueue;

    // when receiving messages, they are put to this map and when the timeout
    // is over, en-queue back to the SQSMessageQueue
    private Map<String, SQSMessage> receivedMessages;

    // this thread checks if the timeout of messages are over and en-queue to SQSMessageQueue
    private VisibilityTimeoutChecker timeoutChecker;

    // MessageBox Details are stored here
    private MessageBoxDetails messageBoxDetails;

    // Permission label names to Permissions mapping are stored
    private Map<String, PermissionLabel> permissionLabelMap;

    // SQS based queue attributes are stored
    private Map<String, String> queueAttributes;


    /**
     * @param owner
     * @param messageBoxName
     * @param defaultVisibilityTimeout timeout for entire message queue
     */
    public MessageBox(String owner, String messageBoxName, long defaultVisibilityTimeout) {
        this.queueId = messageBoxName;

        SQSMessageQueue = new LinkedBlockingDeque<SQSMessage>();
        receivedMessages = new ConcurrentHashMap<String, SQSMessage>();
        queueAttributes = new ConcurrentHashMap<String, String>();
        messageBoxDetails = new MessageBoxDetails(messageBoxName, owner, defaultVisibilityTimeout, 0);
        permissionLabelMap = new ConcurrentHashMap<String, PermissionLabel>();

        // start time out checker thread
        timeoutChecker = new VisibilityTimeoutChecker(this);
        new Thread(timeoutChecker).start();

        // set SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP in queue attributes
        String createdTime = Long.toString(System.currentTimeMillis() / 1000);
        queueAttributes.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP, createdTime);
    }

    /**
     * @return list of shared users who can access messageBox
     */
    public List<String> getSharedUsers() {
        Set<String> sharedUsersSet = new HashSet<String>();
        for (String permissionLabel : permissionLabelMap.keySet()) {
            sharedUsersSet.addAll(permissionLabelMap.get(permissionLabel).getSharedUserList());
        }
        return new ArrayList<String>(sharedUsersSet);
    }

    /**
     * Same operation set is granted for all shared users
     *
     * @param permissionLabelName name for the permissions which contains sharedUserList, operationsList
     * @param sharedUsersList     grant access to these users
     * @param operationsList      grant access to these operations
     */
    public synchronized void addPermission(String permissionLabelName, List<String> sharedUsersList,
                                           List<String> operationsList) {
        PermissionLabel permissionLabel = new PermissionLabel(sharedUsersList, operationsList);
        permissionLabelMap.put(permissionLabelName, permissionLabel);
    }

    /**
     * @param permissionLabelName name of the permissions which added permission to messageBox
     */
    public synchronized void removePermission(String permissionLabelName) {
        permissionLabelMap.remove(permissionLabelName);
    }

    /**
     * set queue attribute SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP
     */
    public synchronized void setLastModifiedTimestamp() {
        queueAttributes.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP,
                            Long.toString(System.currentTimeMillis() / 1000));
    }

    /**
     * @return MessageBox Details
     */
    public synchronized MessageBoxDetails getMessageBoxDetails() {
        // update messageBox details and return
        messageBoxDetails.setNumberOfMessages(SQSMessageQueue.size());
        messageBoxDetails.setSharedUsersList(getSharedUsers());
        return messageBoxDetails;
    }

    /**
     * Set Queue Attributes
     *
     * @param queueAttributes SQS based queue related attributes
     * @throws MessageBoxException InvalidAttributeName, InvalidAttributeValue
     */
    public synchronized void setQueueAttributes(Map<String, String> queueAttributes)
            throws MessageBoxException {
        String attributeName;
        String attributeValue;
        for (Map.Entry pairs : queueAttributes.entrySet()) {
            attributeName = (String) pairs.getKey();
            attributeValue = (String) pairs.getValue();
            if (this.queueAttributes.containsKey(attributeName)) {
                if (attributeValue != null) {
                    this.queueAttributes.put(attributeName, attributeValue);
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn(attributeName + " attribute value can not be null.");
                    }
                    throw new MessageBoxException( "InvalidAttributeValue ", "Unknown attributeValue ");
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(attributeName + " attribute is not supported.");
                }
                throw new MessageBoxException("InvalidAttributeName ", "Unknown attribute ");
            }
        }
    }

    /**
     * Get Queue Attributes
     *
     * @return Attribute Map
     */
    public synchronized Map<String, String> getQueueAttributes() {
        // update Queue attributes and return
        queueAttributes.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES,
                            Integer.toString(SQSMessageQueue.size()));
        queueAttributes.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                            Integer.toString(receivedMessages.size()));
        queueAttributes.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT,
                            Long.toString(messageBoxDetails.getDefaultVisibilityTimeout()));
        return queueAttributes;
    }


    /**
     * @param SQSMessage - SQSMessage to en-queue
     */
    public synchronized void putMessage(SQSMessage SQSMessage) {
        SQSMessage.setDefaultVisibilityTimeout(messageBoxDetails.getDefaultVisibilityTimeout());
        SQSMessage.setSentTimestamp();
        SQSMessageQueue.push(SQSMessage);
        setLastModifiedTimestamp();
    }

    /**
     * @param numberofMaximumMessages -number of messages to be received, in maximum,queue size
     * @param visibilityTimeout       - time out for locking the messages
     * @param attributes
     * @return list of messages or empty list
     */
    public synchronized List<SQSMessage> receiveMessage(int numberofMaximumMessages,
                                                     long visibilityTimeout,
                                                     Map<String, String> attributes) {
        SQSMessage SQSMessage;
        List<SQSMessage> SQSMessageList = new ArrayList<SQSMessage>();
        if (SQSMessageQueue.size() < numberofMaximumMessages) {
            numberofMaximumMessages = SQSMessageQueue.size();
        }
        for (int messageCount = 0; messageCount < numberofMaximumMessages; messageCount++) {
            SQSMessage = SQSMessageQueue.pollFirst();
            SQSMessage.setFirstReceivedTimestamp();
            SQSMessage.setReceiveCount();
            SQSMessage.setReceivedTimeStamp(System.currentTimeMillis());
            SQSMessage.setDefaultVisibilityTimeout(visibilityTimeout);
            String receiptHandler = UUID.randomUUID().toString();
            SQSMessage.setReceiptHandle(receiptHandler);
            SQSMessageList.add(SQSMessage);
            receivedMessages.put(receiptHandler, SQSMessage);
        }
        setLastModifiedTimestamp();
        return SQSMessageList;
    }

    /**
     * Get all messages
     *
     * @return Message Details
     */
    public synchronized MessageDetails[] getAllMessages() {
        SQSMessage SQSMessage;
        List<MessageDetails> messageList = new ArrayList<MessageDetails>();
        Iterator messageIterator = SQSMessageQueue.iterator();
        MessageDetails messageDetails;
        while (messageIterator.hasNext()) {
            SQSMessage = (SQSMessage) messageIterator.next();
            messageDetails = new MessageDetails();
            messageDetails.setMessageBody(SQSMessage.getBody());
            messageDetails.setMessageId(SQSMessage.getMessageId());
            if(SQSMessage.getAttribute().get(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT)!=null){
                messageDetails.setReceivedCount(SQSMessage.getAttribute().get(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT));
            }else{
                messageDetails.setReceivedCount("0");
            }
            messageList.add(messageDetails);
        }
        return messageList.toArray(new MessageDetails[messageList.size()]);
    }

    /**
     * @param receiptHandler
     * @return deleted message or null if message is not found.
     */
    public synchronized SQSMessage deleteMessage(String receiptHandler) {
        setLastModifiedTimestamp();
        return receivedMessages.remove(receiptHandler);
    }

    /**
     * @param receiptHandler
     * @param visibilityTimeout
     * @return visibility time out changed message or null if message is not found.
     */
    public synchronized SQSMessage changeVisibilityTimeout(String receiptHandler,
                                                        long visibilityTimeout) {
        SQSMessage SQSMessage = receivedMessages.get(receiptHandler);
        SQSMessage.setDefaultVisibilityTimeout(visibilityTimeout);
        setLastModifiedTimestamp();
        return receivedMessages.put(receiptHandler, SQSMessage);
    }

    /**
     * Check timeout of received messages and Add them back to SQSMessageQueue
     */
    public synchronized void checkTimeoutMessages() {
        SQSMessage SQSMessage;
        String receiptHandler;
        long currentTimeStampDifference;
        for (Map.Entry pairs : receivedMessages.entrySet()) {
            SQSMessage = (SQSMessage) pairs.getValue();
            currentTimeStampDifference = System.currentTimeMillis() - SQSMessage.getReceivedTimeStamp();
            if (currentTimeStampDifference > SQSMessage.getDefaultVisibilityTimeout()) {
                receiptHandler = (String) pairs.getKey();
                SQSMessageQueue.addFirst(SQSMessage);
                receivedMessages.remove(receiptHandler);
            }
        }
    }

    /**
     * Check if the logged in user is has permission to do the operation
     *
     * @param loggedInUser
     * @param operation
     * @return if logged in user is a shared user and has given permission to operation, return true
     */
    public boolean isSharedForOperation(String loggedInUser, String operation) {
        for (String permissionLabel : permissionLabelMap.keySet()) {
            if (permissionLabelMap.get(permissionLabel).isSharedForOperation(loggedInUser, operation)) {
                return true;
            }
        }
        return false;
    }
}