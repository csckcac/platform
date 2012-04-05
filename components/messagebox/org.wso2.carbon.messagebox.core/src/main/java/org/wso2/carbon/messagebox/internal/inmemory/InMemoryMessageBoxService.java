package org.wso2.carbon.messagebox.internal.inmemory;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxDetails;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.MessageDetails;
import org.wso2.carbon.messagebox.queue.QueueManager;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

public class InMemoryMessageBoxService implements MessageBoxService {

    private static final Log log = LogFactory.getLog(InMemoryMessageBoxService.class);
    // all messageBoxes are stored as <composite messageBox name, MessageBox>
    // composite messageBox name = userName/messageBoxName
    private ConcurrentHashMap<String, MessageBox> messageBoxStore;


    /**
     * Constructor
     * Initialization of messageBoxStore
     */
    public InMemoryMessageBoxService() {
        messageBoxStore = new ConcurrentHashMap<String, MessageBox>();
    }

    /**
     * Create MessageBox
     *
     * @param messageBoxName           composite message box name eg.user1/queue1
     * @param defaultVisibilityTimeout visibility timeout for queue
     * @return composite message box name
     * @throws MessageBoxException Queue already exists, No permissions to create queue
     */
    public String createMessageBox(String messageBoxName, long defaultVisibilityTimeout)
            throws MessageBoxException {
        // messageBox already exists
        if (messageBoxStore.contains(messageBoxName)) {
            if (log.isWarnEnabled()) {
                log.warn(messageBoxName + " already exists");
            }
            if (messageBoxStore.get(messageBoxName).getMessageBoxDetails().
                    getDefaultVisibilityTimeout() != defaultVisibilityTimeout) {
                throw new MessageBoxException("Queue already exists",
                                              "AWS.SimpleQueueService.QueueNameExists ");
            }
        } else {
            // Create new messageBox and add to messageBox store
            MessageBox messageBox;
            String messageBoxOwner = messageBoxName.split(File.separator)[0];

            // admin can create messageBoxes for other users
            // other logged in users can not create messageBox for any other user
            if (isAdminLoggedIn() || getLoggedInUser().equals(messageBoxOwner)) {
                messageBox = new MessageBox(messageBoxOwner, messageBoxName, defaultVisibilityTimeout);
                messageBoxStore.put(messageBoxName, messageBox);
            } else {
                throw new MessageBoxException(getLoggedInUser() +
                                              " has no permission to create message box for user " +
                                              messageBoxOwner);
            }

        }
        return messageBoxName;
    }

    /**
     * Admin: Get all message boxes
     * Others: Get only the message boxes owned
     *
     * @return Array of MessageBox Details: owner, shared users, message count
     * @throws MessageBoxException
     */
    public MessageBoxDetails[] getAllMessageBoxes() throws MessageBoxException {
        ArrayList<MessageBoxDetails> arrayList = new ArrayList<MessageBoxDetails>();
        if (isAdminLoggedIn()) {
            for (String messageBoxName : messageBoxStore.keySet()) {
                // get all the messageBoxes
                MessageBox messageBox = messageBoxStore.get(messageBoxName);
                arrayList.add(messageBox.getMessageBoxDetails());
            }
        } else {
            String loggedInUser = getLoggedInUser();
            for (String messageBoxName : messageBoxStore.keySet()) {
                // get only the messageBoxes own by logged in user
                if (messageBoxName.split(File.separator)[0].equals(loggedInUser)) {
                    MessageBox messageBox = messageBoxStore.get(messageBoxName);
                    arrayList.add(messageBox.getMessageBoxDetails());
                }
            }
        }
        MessageBoxDetails[] messageBoxDetailsArray = arrayList.toArray(new MessageBoxDetails[]{});
        return messageBoxDetailsArray;
    }

    /**
     * Get All Messages in a messageBox
     * This is only for the admin user
     *
     * @param messageBoxId composite message box name eg.user1/queue1
     * @return Array of SQSMessage Details: body,id,received count
     * @throws MessageBoxException NoneExistenceQueueException, access denied
     */
    public MessageDetails[] getAllAvailableMessages(String messageBoxId)
            throws MessageBoxException {
        if (isAdminLoggedIn()) {
            MessageBox messageBox = messageBoxStore.get(messageBoxId);
            if (messageBox != null) {
                return messageBox.getAllMessages();
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to receive " +
                             "messages.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            throw new MessageBoxException("Only the Administrator can receive all messages");
        }
    }

    public MessageDetails[] getAllRetrievedMessages(String messageBoxId)
            throws MessageBoxException {
        //ToDo
        return new MessageDetails[0];
    }

    public List<org.wso2.carbon.messagebox.PermissionLabel> getAllPermissions(String messageBoxId)
            throws MessageBoxException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete MessageBox
     *
     * @param messageBoxId composite message box name eg.user1/queue1
     * @return messageBoxName
     * @throws MessageBoxException NoneExistenceQueueException, AccessDeniedException
     */
    public String deleteMessageBox(String messageBoxId) throws MessageBoxException {
        // admin can delete messageBox
        // if logged in user owns messageBox, delete it
        if (isAdminLoggedIn() || messageBoxId.split(File.separator)[0].equals(getLoggedInUser())) {
            MessageBox messageBox = messageBoxStore.remove(messageBoxId);
            if (messageBox != null) {
                return messageBoxId;
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to delete " +
                             "messageBox.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            throw getAccessDeniedException();
        }
    }

    /**
     * Send Messages to MessageBox
     *
     * @param messageBoxName composite SQSMessage box name eg.user1/queue1
     * @param sqsMessage        SQSMessage
     * @return message
     * @throws MessageBoxException NoneExistenceQueueException, AccessDeniedException
     */
    public SQSMessage putMessage(String messageBoxName, SQSMessage sqsMessage)
            throws MessageBoxException {
        if (isAccessible(messageBoxName, MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE)) {
            MessageBox messageBox = messageBoxStore.get(messageBoxName);
            if (messageBox != null) {
                String sender;
                if (getLoggedInUser() != null) {
                    sender = getLoggedInUser();
                } else {
                    sender = messageBoxName.split(File.separator)[0];
                }
                sqsMessage.setSenderId(sender);
                messageBox.putMessage(sqsMessage);
                return sqsMessage;
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxName + " doesn't exists, failed to put Message.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No permission to" +
                         " Send Message to " + messageBoxName);
            }
            throw getAccessDeniedException();
        }
    }

    /**
     * Get Messages From MessageBox
     *
     * @param messageBoxId            composite message box name eg.user1/queue1
     * @param numberOfMaximumMessages
     * @param visibilityTimeout
     * @param attributes              SQS_MESSAGE_ATTRIBUTE_SENT_TIMESTAMP, SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT, SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP,SQS_MESSAGE_ATTRIBUTE_SENDER_ID
     * @return Message list: receiptHandler, attributes
     * @throws MessageBoxException MaxNumberOfMessageException,NoneExistenceQueueException,
     *                             AccessDeniedException
     */
    public List<SQSMessage> receiveMessage(String messageBoxId, int numberOfMaximumMessages,
                                        long visibilityTimeout, Map<String, String> attributes)
            throws MessageBoxException {
        if (isAccessible(messageBoxId, MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE)) {
            MessageBox messageBox = messageBoxStore.get(messageBoxId);
            if (messageBox != null) {
                if (numberOfMaximumMessages > 0 && numberOfMaximumMessages < 11) {
                    return messageBox.receiveMessage(numberOfMaximumMessages, visibilityTimeout,
                                                     attributes);
                } else {
                    throw new MessageBoxException("The value for MaxNumberOfMessages is not valid",
                                                  "ReadCountOutOfRange ");
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to receive " +
                             "messages.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No permission to" +
                         " Send Message to " + messageBoxId);
            }
            throw getAccessDeniedException();
        }
    }

    /**
     * Delete a Message in MessageBox
     *
     * @param messageBoxId   composite message box name eg.user1/queue1
     * @param receiptHandler receiptHandler got at receiving message
     * @throws MessageBoxException
     */
    public void deleteMessage(String messageBoxId, String receiptHandler)
            throws MessageBoxException {
        if (isAccessible(messageBoxId, MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE)) {
            MessageBox messageBox = messageBoxStore.get(messageBoxId);
            if (messageBox != null) {
                if (messageBox.deleteMessage(receiptHandler) == null) {
                    throw new MessageBoxException(receiptHandler + " ,No such receiptHandler exists" +
                                                  " with " + messageBoxId);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("Message with receiptHandler " + receiptHandler + " successfully" +
                                 " deleted.");
                    }
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to delete" +
                             " message.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No permission to" +
                         " Send Message to " + messageBoxId);
            }
            throw getAccessDeniedException();
        }
    }

    /**
     * Admin: Get all queue names
     * Logged in user: Get queues owned,queues shared wish logged in user are not included
     *
     * @param qnamePrefix queue name starts with qnamePrefix
     * @return list of composite message box names
     * @throws MessageBoxException
     */
    public List<String> listQueues(String qnamePrefix) throws MessageBoxException {
        List<String> queueNameList = new ArrayList<String>();
        // admin can list all messageBoxes
        if (isAdminLoggedIn()) {
            for (String messageBoxName : messageBoxStore.keySet()) {
                if (messageBoxName.split(File.separator)[1].startsWith(qnamePrefix)) {
                    queueNameList.add(messageBoxName);
                }
            }
        } else {
            for (String messageBoxName : messageBoxStore.keySet()) {
                // list only the messageBoxes owned by logged in user
                // messageBoxes shared with logged in user is not added.
                if (messageBoxName.split(File.separator)[0].equals(getLoggedInUser()) &&
                    messageBoxName.split(File.separator)[1].startsWith(qnamePrefix)) {
                    queueNameList.add(messageBoxName);
                }
            }
        }
        return queueNameList;
    }

    /**
     * Change Visibility Timeout of a message
     *
     * @param messageBoxId      composite message box name eg.user1/queue1
     * @param receiptHandler    receipt handler of the message
     * @param visibilityTimeout
     * @throws MessageBoxException ReceiptHandlerInvalid,NoneExistenceQueueException,AccessDeniedException
     */
    public void changeVisibility(String messageBoxId, String receiptHandler,
                                 long visibilityTimeout)
            throws MessageBoxException {
        if (isAccessible(messageBoxId, MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY)) {
            MessageBox messageBox = messageBoxStore.get(messageBoxId);
            if (messageBox != null) {
                if (messageBox.changeVisibilityTimeout(receiptHandler, visibilityTimeout) == null) {
                    throw new MessageBoxException(receiptHandler + " ,No such receiptHandler exists " +
                                                  "with " + messageBox);
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("VisibilityTimeout of message with receiptHandler " + receiptHandler +
                                 " successfully changed to " + visibilityTimeout);
                    }
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to change" +
                             " visibilityTimeout.");
                }
                throw getNoneExistenceQueueException();

            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No permission to" +
                         " Change Message Visibility in " + messageBoxId);
            }
            throw getAccessDeniedException();
        }
    }

    /**
     * Get queue attributes:SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES,SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE,SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT
     * SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP,SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP,SQS_QUEUE_ATTRIBUTE_MAX_MESSAGE_SIZE,
     * SQS_QUEUE_ATTRIBUTE_MESSAGE_RETENTION_PERIOD
     *
     * @param messageBoxId composite message box name eg.user1/queue1
     * @return map of attributes<attributeName, value>
     * @throws MessageBoxException NoneExistenceQueueException,AccessDeniedException
     */
    public Map<String, String> getMessageBoxAttributes(String messageBoxId)
            throws MessageBoxException {
        if (isAccessible(messageBoxId, MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES)) {
            MessageBox messageBox = messageBoxStore.get(messageBoxId);
            if (messageBox != null) {
                return messageBox.getQueueAttributes();
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(messageBoxId + " doesn't exists, failed to get" +
                             " messagebox attributes.");
                }
                throw getNoneExistenceQueueException();
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No permission to" +
                         " Get MessageBox Attributes in " + messageBoxId);
            }
            throw getAccessDeniedException();
        }
    }

    /**
     * Set MessageBox attributes:
     *
     * @param messageBoxId composite message box name eg.user1/queue1
     * @param attributes   SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES,SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE,SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT
     *                     SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP,SQS_QUEUE_ATTRIBUTE_LAST_MODIFIED_TIMESTAMP,SQS_QUEUE_ATTRIBUTE_MAX_MESSAGE_SIZE,
     *                     SQS_QUEUE_ATTRIBUTE_MESSAGE_RETENTION_PERIOD
     * @throws MessageBoxException NoneExistenceQueueException
     */
    public void setMessageBoxAttributes(String messageBoxId, Map<String, String> attributes)
            throws MessageBoxException {
        MessageBox messageBox = messageBoxStore.get(messageBoxId);
        if (messageBox != null) {
            messageBox.setQueueAttributes(attributes);
        } else {
            if (log.isWarnEnabled()) {
                log.warn(messageBoxId + " doesn't exists, failed to set" +
                         " messagebox attributes.");
            }
            throw getNoneExistenceQueueException();
        }
    }

    /**
     * Admin: allowed to remove permission
     * Logged in user: allowed to remove permission on queues owned by him
     *
     * @param messageBoxId        composite message box name eg.user1/queue1
     * @param permissionLabelName label name under which the permissions were added
     * @throws MessageBoxException NoneExistenceQueueException, access denied
     */
    public void removePermission(String messageBoxId, String permissionLabelName)
            throws MessageBoxException {
        MessageBox messageBox = messageBoxStore.get(messageBoxId);
        if (messageBox == null) {
            throw getNoneExistenceQueueException();
        } else {
            if (isAdminLoggedIn() || messageBoxId.split(File.separator)[0].equals(getLoggedInUser())) {
                messageBox.removePermission(permissionLabelName);
            } else {
                throw new MessageBoxException(getLoggedInUser() +
                                              " has no permission to remove permission on shared message box  " +
                                              messageBoxId);
            }
        }
    }

    /**
     * Admin: allow add permissions to queues
     * Logged in user: allow only to set permissions to queues owned for operations: SQS_OPERATION_SEND_MESSAGE,
     * SQS_OPERATION_RECEIVE_MESSAGE, SQS_OPERATION_DELETE_MESSAGE ,SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY,
     * SQS_OPERATION_GET_QUEUE_ATTRIBUTES or SQS_OPERATION_ALL(*)
     *
     * @param messageBoxId        composite message box name eg.user1/queue1
     * @param operationsList      access permission to these operations
     * @param permissionLabelName required at removing permissions granted for messageBox
     * @param sharedUsersList     access permission to these users
     * @throws MessageBoxException NoneExistenceQueueException, access denied
     */
    public void addPermission(String messageBoxId, List<String> operationsList,
                              String permissionLabelName, List<String> sharedUsersList)
            throws MessageBoxException {
        MessageBox messageBox = messageBoxStore.get(messageBoxId);
        if (messageBox == null) {
            throw getNoneExistenceQueueException();
        } else {
            if (isAdminLoggedIn() || messageBoxId.split(File.separator)[0].equals(getLoggedInUser())) {
                messageBox.addPermission(permissionLabelName, sharedUsersList, operationsList);
            } else {
                throw new MessageBoxException(getLoggedInUser() +
                                              " has no permission to remove permission on shared message box  " +
                                              messageBoxId);
            }
        }
    }

    /**
     * Check if the logged in user/admin has permission to do the operation on message box
     *
     * @param messageBoxName composite message box name eg.user1/queue1
     * @param operation      SQS_OPERATION_SEND_MESSAGE,SQS_OPERATION_RECEIVE_MESSAGE, SQS_OPERATION_DELETE_MESSAGE ,SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY,
     *                       SQS_OPERATION_GET_QUEUE_ATTRIBUTES or SQS_OPERATION_ALL(*)
     * @return true if admin logged in or logged in user has access permission to queue and operation
     * @throws MessageBoxException NoneExistenceQueueException
     */
    private boolean isAccessible(String messageBoxName, String operation)
            throws MessageBoxException {
        MessageBox messageBox = messageBoxStore.get(messageBoxName);
        // messageBox doesn't exist.
        if (messageBox == null) {
            throw getNoneExistenceQueueException();
        } else {
            // admin can access messageBox
            if (isAdminLoggedIn()) {
                return true;
            } else {
                String loggedInUser = getLoggedInUser();
                // logged in user owns messageBox
                if (messageBoxName.split(File.separator)[0].equals(loggedInUser)) {
                    return true;
                } else {
                    // messageBox is shared with logged in user or not
                    return messageBox.isSharedForOperation(loggedInUser, operation);
                }
            }
        }

    }

    /**
     * @return logged in username
     * @throws MessageBoxException user name is null
     */
    private String getLoggedInUser() throws MessageBoxException {
        String loggedInUser;
        Object userName = MessageContext.getCurrentMessageContext().getProperty("username");
        if (userName != null) {
            loggedInUser = userName.toString();
            return loggedInUser;
        } else {
            throw new MessageBoxException("Failed to get logged in user");
        }

    }

    /**
     * @return Admin logged in or not
     */
    private boolean isAdminLoggedIn() {
        String username;
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        username = (String) request.getSession().getAttribute(ServerConstants.USER_LOGGED_IN);
        if (username != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return NoneExistenceQueueException
     */
    private MessageBoxException getNoneExistenceQueueException() {
        return new MessageBoxException("Queue does not exist.",
                                       "AWS.SimpleQueueService.NonExistentQueue");
    }

    /**
     * @return AccessDeniedException
     */
    private MessageBoxException getAccessDeniedException() {
        return new MessageBoxException("Access to the resource is denied.", "AccessDenied ");
    }

    public QueueManager getQueueManager() throws MessageBoxException {
        return null;
    }
}
