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

package org.wso2.carbon.messagebox;

import org.wso2.carbon.messagebox.queue.QueueManager;

import java.util.List;
import java.util.Map;

/**
 * MessageBox OSGi service interface
 */
public interface MessageBoxService {
    /* admin functions*/

    /**
     * Get all message boxes available as an array of MessageBoxDetails which contains details such
     * as message box id, message box name, visibilityTimeout value
     *
     * @return array of MessageBoxDetails
     * @throws MessageBoxException if fails to get message boxes
     */
    public MessageBoxDetails[] getAllMessageBoxes() throws MessageBoxException;

    /**
     * Get all available messages that is, the messages which are not retrieved and locked.
     * these messages contains details such as message id, receipt handler, message body
     *
     * @param messageBoxId - message box id (owner/messageBoxName)
     * @return array of MessageDetails
     * @throws MessageBoxException if fails to get available messages
     */
    public MessageDetails[] getAllAvailableMessages(String messageBoxId)
            throws MessageBoxException;

    /**
     * Get all retrieved messages by all users in this message box. These messages are locked by
     * retrieved user
     *
     * @param messageBoxId -  message box id (owner/messageBoxName)
     * @return array of MessageDetails
     * @throws MessageBoxException if fails to get retrieved messages
     */
    public MessageDetails[] getAllRetrievedMessages(String messageBoxId)
            throws MessageBoxException;

    public List<PermissionLabel> getAllPermissions(String messageBoxId) throws MessageBoxException;

    /**
     * creates a message box and returns the id of the message box. Normally this id consists of
     * the user name as well to destinguish the message boxes in user space.
     * eg. if the message box name is myMessageBox id my be username/myMessageBox
     *
     * @param messageBoxName           - name of the message box, this is just message box name not the id
     * @param defaultVisibilityTimeout - visibility time out for the message box
     * @return message box id
     * @throws MessageBoxException if message box name exists already with this user
     */
    public String createMessageBox(String messageBoxName,
                                   long defaultVisibilityTimeout) throws MessageBoxException;

    /**
     * delete the message box. passes the message box id which is returned from the
     * create message box.
     *
     * @param messageBoxId - message box id (owner/messageBoxName)
     * @return messageBoxId
     * @throws MessageBoxException if fails to delete message box
     */
    public String deleteMessageBox(String messageBoxId) throws MessageBoxException;

    /**
     * put the SQSMessage to the SQSMessage box identified by the SQSMessage id.
     *
     * @param messageBoxId - SQSMessage box id (owner/messageBoxName)
     * @param SQSMessage      - SQSMessage to be added into SQSMessage box
     * @return message
     * @throws MessageBoxException if fails to put SQSMessage
     */
    public SQSMessage putMessage(String messageBoxId, SQSMessage SQSMessage)
            throws MessageBoxException;

    /**
     * receives a given number of messages from the message box with the id.
     * uses the given visibility time out to add messages to queue back.
     *
     * @param messageBoxId            - message box id (owner/messageBoxName)
     * @param numberOfMaximumMessages - number of messages to be retrieved,
     *                                if this value is greater than the available message count,
     *                                all messages are retrieved.
     * @param visibilityTimeout       - visibility timeout value for retrieving  messages
     * @param attributes              - map of attributes on each message retrieving, usually all
     *                                the message details are provided.
     * @return list of Messages
     * @throws MessageBoxException if fails to receive messages
     */
    public List<SQSMessage> receiveMessage(String messageBoxId,
                                        int numberOfMaximumMessages,
                                        long visibilityTimeout,
                                        Map<String, String> attributes)
            throws MessageBoxException;

    /**
     * deletes the message from the message box identified by the message box id. The receipt
     * handler is returned when receiving the message.
     *
     * @param messageBoxId   - message box id (owner/messageBoxName)
     * @param receiptHandler - this receipt handler value containing message is deleted.
     * @throws MessageBoxException if no such receipt handler exists or fails to delete message
     */
    public void deleteMessage(String messageBoxId,
                              String receiptHandler) throws MessageBoxException;

    /**
     * gives the uris for the available message boxes.
     *
     * @param qnamePrefix - message box uris with given prefix are received
     * @return list of uris
     * @throws MessageBoxException if fails to get list of uris
     */
    public List<String> listQueues(String qnamePrefix) throws MessageBoxException;

    /**
     * change the visibility timeout value of given receiptHandler contained message with given
     * new value
     *
     * @param messageBoxId      - message box id (owner/messageBoxName)
     * @param receiptHandler    - this receipt handler value containing message's visibility timeout
     *                          is changed.
     * @param visibilityTimeout - new visibility timeout value
     * @throws MessageBoxException if no such receiptHandler exists or fails to change visibility
     *                             timeout
     */
    public void changeVisibility(String messageBoxId,
                                 String receiptHandler,
                                 long visibilityTimeout) throws MessageBoxException;

    /**
     * get message box attributes such as number of messages, max message size
     *
     * @param messageBoxId - message box id (owner/messageBoxName)
     * @return map of attributes
     * @throws MessageBoxException if fails to get message box attributes
     */
    public Map<String, String> getMessageBoxAttributes(String messageBoxId)
            throws MessageBoxException;

    /**
     * set message box attributes
     *
     * @param messageBoxId - message box id (owner/messageBoxName)
     * @param attributes   - set of attributes and values for message box
     * @throws MessageBoxException if fails to set attribute values
     */
    public void setMessageBoxAttributes(String messageBoxId,
                                        Map<String, String> attributes) throws MessageBoxException;

    /**
     * remove permission for message box with given permission label
     *
     * @param messageBoxId     - message box id (owner/messageBoxName)
     * @param permissionLabel- a label to name permissions, this label is set with adding permissions
     * @throws MessageBoxException if fails to remove permission with given label
     */
    public void removePermission(String messageBoxId, String permissionLabel)
            throws MessageBoxException;

    /**
     * add permission to given message box with given set of shared users and operations
     *
     * @param messageBoxId    - message box id (owner/messageBoxName)
     * @param operationsList  - list of valid SQS operations
     * @param permissionLabel - a label to identify given permissions, this is used to remove permission
     * @param sharedUsers     - list of users allowed to do the operations on this message box
     * @throws MessageBoxException if fails to add permission
     */
    public void addPermission(String messageBoxId, List<String> operationsList,
                              String permissionLabel,
                              List<String> sharedUsers) throws MessageBoxException;

    public QueueManager getQueueManager() throws MessageBoxException;

}