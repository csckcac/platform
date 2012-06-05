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
import org.apache.qpid.management.common.mbeans.ManagedQueue;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxDetails;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.MessageDetails;
import org.wso2.carbon.messagebox.PermissionLabel;
import org.wso2.carbon.messagebox.SQSMessage;
import org.wso2.carbon.messagebox.internal.utils.Utils;
import org.wso2.carbon.messagebox.queue.QueueManager;
import org.wso2.carbon.messagebox.queue.QueueUserPermission;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQSMessage box service is implemented using JMS queues.
 */
public class JMSMessageBoxService implements MessageBoxService {

    private static final Log log = LogFactory.getLog(JMSMessageBoxService.class);

    private static String QPID_VHOST_NAME = "carbon";

    private QueueManager queueManager;
    private RegistryMessageBoxHandler registryMessageBoxHandler;
    private MessageBoxAuthorizationHandler authorizationHandler;

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, MessageLock>> tenantBasedMessageLocks =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, MessageLock>>();

    private ConcurrentHashMap<String, MessageLock> getMessageLocks() {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        if (!tenantBasedMessageLocks.containsKey(tenantId)) {
            ConcurrentHashMap<String, MessageLock> messageLocks = new ConcurrentHashMap<String, MessageLock>();
            tenantBasedMessageLocks.put(tenantId, messageLocks);
        }
        return tenantBasedMessageLocks.get(tenantId);
    }

    public MessageBoxDetails[] getAllMessageBoxes() throws MessageBoxException {
        List<MessageBoxDetails> messageBoxDetailsList =
                this.registryMessageBoxHandler.getMessageBoxDetails();
        String username = getCurrentUser();
        if (Utils.isAdmin(username)) {
            // Admin can see all message boxes

            // set number of messages in messageBox
            for (MessageBoxDetails messageBoxDetails : messageBoxDetailsList) {
                String messageBoxId = JMSQueueHandler.getJMSQueueName(messageBoxDetails.getMessageBoxId());
                messageBoxDetails.setNumberOfMessages(getVisibleMessageCount(messageBoxId) +
                                                      getHiddenMessageCount(messageBoxId));
                messageBoxDetails.setTenantDomain(CarbonContext.getCurrentContext().getTenantDomain());
            }
            return messageBoxDetailsList.toArray(
                    new MessageBoxDetails[messageBoxDetailsList.size()]);
        } else {
            // Any other user should only see message boxes crated by that user
            List<MessageBoxDetails> userMessageBoxes = new ArrayList<MessageBoxDetails>();

            for (MessageBoxDetails messageBoxDetails : messageBoxDetailsList) {
                if (messageBoxDetails.getMessageBoxOwner().equals(username) ||
                    authorizationHandler.isUserAuthorized(username,
                                                          messageBoxDetails.getMessageBoxId(),
                                                          MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE)) {
                    String messageBoxId = JMSQueueHandler.getJMSQueueName(messageBoxDetails.getMessageBoxId());
                    messageBoxDetails.setNumberOfMessages(getVisibleMessageCount(messageBoxId) +
                                                          getHiddenMessageCount(messageBoxId));
                    messageBoxDetails.setTenantDomain(CarbonContext.getCurrentContext().getTenantDomain());
                    userMessageBoxes.add(messageBoxDetails);
                }
            }

            return userMessageBoxes.toArray(new MessageBoxDetails[userMessageBoxes.size()]);
        }
    }

    public MessageDetails[] getAllAvailableMessages(String messageBoxId)
            throws MessageBoxException {

        // Only the admin can browse messages
        String username = getCurrentUser();
        if (!(Utils.isAdmin(username) || isMessageBoxOwner(messageBoxId, username) ||
              authorizationHandler.isUserAuthorized(username,
                                                    messageBoxId,
                                                    MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE))) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        try {
            List<Message> jmsMessageList = JMSQueueHandler.browse(messageBoxId);

            MessageDetails[] messageDetailsArray = new MessageDetails[jmsMessageList.size()];
            int index = 0;
            for (Message jmsMessage : jmsMessageList) {
                messageDetailsArray[index++] = jmsMessageToMessageDetails(jmsMessage);
            }

            return messageDetailsArray;
        } catch (JMSQueueHandlerException e) {
            throw new MessageBoxException("InternalError", e);
        }
    }

    public MessageDetails[] getAllRetrievedMessages(String messageBoxId)
            throws MessageBoxException {

        // Only the admin can browse messages
        String username = getCurrentUser();
        if (!(Utils.isAdmin(username) || isMessageBoxOwner(messageBoxId, username) ||
              authorizationHandler.isUserAuthorized(username,
                                                    messageBoxId,
                                                    MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE))) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        List<MessageDetails> messageDetailsList = new ArrayList<MessageDetails>();
        MessageDetails messageDetails;
        for (MessageLock messageLock : getMessageLocks().values()) {
            if (messageLock.getMessageBoxID().equals(messageBoxId)) {
                messageDetails = jmsMessageToMessageDetails(messageLock.getJmsMessage());
                messageDetails.setReceiptHandler(messageLock.getReceiptHandle());
                messageDetailsList.add(messageDetails);
            }
        }

        return messageDetailsList.toArray(new MessageDetails[messageDetailsList.size()]);
    }

    public List<PermissionLabel> getAllPermissions(String messageBoxId) throws MessageBoxException {
        String username = getCurrentUser();
        if (!(Utils.isAdmin(username) || isMessageBoxOwner(messageBoxId, username) ||
              authorizationHandler.isUserAuthorized(username,
                                                    messageBoxId,
                                                    MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE))) {
            throw new MessageBoxException("AccessDenied");
        }
        return registryMessageBoxHandler.getAllPermissions(messageBoxId);
    }

    public String createMessageBox(String messageBoxName, long defaultVisibilityTimeout)
            throws MessageBoxException {
        String username = getCurrentUser();

        // change seconds to milliseconds
        defaultVisibilityTimeout = defaultVisibilityTimeout * 1000;

        // message box already exists and throw exception if default visibility time out is different
        // from available message box
        if (registryMessageBoxHandler.isMessageBoxExists(username, messageBoxName)) {
            MessageBoxDetails messageBoxDetails =
                    registryMessageBoxHandler.getMessageBoxDetails(
                            username + "/" + messageBoxName);

            if (messageBoxDetails.getDefaultVisibilityTimeout() != defaultVisibilityTimeout) {
                throw new MessageBoxException("message box already exists with the name, "
                                              + messageBoxName + " for user " + username);
            }
        } else {
            // message box does not exists
            registryMessageBoxHandler.createMessageBox(
                    username, messageBoxName, defaultVisibilityTimeout);
        }

        // when sending messages we going to use this name as the message queue name so
        // add message queue to the queue space
        queueManager.addQueue(messageBoxName,
                              MessageBoxConstants.MB_QUEUE_CREATED_FROM_SQS_CLIENT);

        String messageBoxId = username + "/" + messageBoxName;
        authorizationHandler.allowAllPermissionsToAdminRole(messageBoxId);
        authorizationHandler.allowAllPermissionsToUser(messageBoxId, username);
        allowQueuePermissionOnCreateQueue(username, JMSQueueHandler.getJMSQueueName(messageBoxId));
        return messageBoxId;
    }

    private void allowQueuePermissionOnCreateQueue(String username, String messageBoxId)
            throws MessageBoxException {
        List<QueueUserPermission> userPermissions = new ArrayList<QueueUserPermission>();
        QueueUserPermission queueUserPermission = new QueueUserPermission();
        queueUserPermission.setUserName(username);
        queueUserPermission.setAllowedToConsume(true);
        queueUserPermission.setAllowedToPublish(true);
        userPermissions.add(queueUserPermission);
        queueManager.updateUserPermission(userPermissions, messageBoxId);
    }

    public String deleteMessageBox(String messageBoxId) throws MessageBoxException {
        //ToDo:Do we have to delete message boxes which had no activities for 30 days?
        //ToDo:Should we let users to send messages to message box for 60 seconds after deleting message box?
        //ToDo:We have to wait 60 seconds to create message box on same name after deleting?

        // Only an admin or the owner can delete a message box
        String username = getCurrentUser();
        if (!Utils.isAdmin(username) && !isMessageBoxOwner(messageBoxId, username)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        try {
            JMSQueueHandler.clearQueue(messageBoxId);
        } catch (JMSQueueHandlerException e) {
            throw new MessageBoxException("InternalError", e);
        }

        // Remove permissions attached to this message box
        List<PermissionLabel> permissionLabels =
                registryMessageBoxHandler.getAllPermissions(messageBoxId);
        for (PermissionLabel permissionLabel : permissionLabels) {
            authorizationHandler.removePermission(messageBoxId, permissionLabel);
        }
        // Remove permissions to owner
        authorizationHandler.denyAllPermissionsToUser(messageBoxId, username);
        // Remove message box from Registry
        registryMessageBoxHandler.deleteMessageBox(messageBoxId);
        queueManager.deleteQueue(messageBoxId.split("/")[1]);

        return messageBoxId;
    }

    /**
     * Check if the given message box is owned by the given user
     *
     * @param messageBoxID Message box to be checked
     * @param username     Username that is checked for ownership
     * @return true if the owner or false otherwise
     */
    private boolean isMessageBoxOwner(String messageBoxID, String username) {
        return messageBoxID.split(
                MessageBoxConstants.COMPOSITE_QUEUE_NAME_SYMBOL)[0].equals(username);
    }

    public SQSMessage putMessage(String messageBoxID, SQSMessage sqsMessage)
            throws MessageBoxException {
        // Check permissions to send messages
        if (!authorizationHandler.isAuthorized(
                messageBoxID, MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxID)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        try {
            // Push message into JMS queue
            JMSQueueHandler.pushMessage(messageBoxID, sqsMessage,
                                        sqsMessage.getAttribute().
                                                get(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID));
            queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxID));
            return sqsMessage;
        } catch (JMSQueueHandlerException e) {
            throw new MessageBoxException("InternalError", e);
        }
    }


    public List<SQSMessage> receiveMessage(String messageBoxId, int maximumNumberOfMessages,
                                           long visibilityTimeout, Map<String, String> attributes)
            throws MessageBoxException {
        // Check permission to receive messages
        if (!authorizationHandler.isAuthorized(
                messageBoxId, MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }
        if (visibilityTimeout == 0) {
            visibilityTimeout = registryMessageBoxHandler.getMessageBoxDetails(messageBoxId).
                    getDefaultVisibilityTimeout();
        }

        try {
            List<SQSMessage> sqsMessageList = new ArrayList<SQSMessage>();
            // Fetch messages
            while (0 < maximumNumberOfMessages) {
                // Create and store lock
                MessageLock messageLock = JMSQueueHandler.popMessage(messageBoxId, visibilityTimeout);
                if (messageLock == null) {
                    break;
                }
                messageLock.setLockStore(getMessageLocks());
                getMessageLocks().put(messageLock.getReceiptHandle(), messageLock);

                // Create and store SQS message
                SQSMessage sqsMessage = jmsMessageToSQSMessage(messageLock.getJmsMessage());
                sqsMessage.setReceiptHandle(messageLock.getReceiptHandle());

                // set visibility timeout of message as default visibility timeout of messageBox
                if (sqsMessage.getDefaultVisibilityTimeout() == 0) {
                    MessageBoxDetails messageBoxDetails =
                            registryMessageBoxHandler.getMessageBoxDetails(messageBoxId);
                    sqsMessage.setDefaultVisibilityTimeout(
                            messageBoxDetails.getDefaultVisibilityTimeout());
                }
                sqsMessage.setDefaultVisibilityTimeout(visibilityTimeout);

                sqsMessageList.add(sqsMessage);

                maximumNumberOfMessages--;
            }
            queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
            return sqsMessageList;
        } catch (JMSQueueHandlerException e) {
            throw new MessageBoxException("InternalError", e);
        }
    }


    public void deleteMessage(String messageBoxId, String receiptHandler)
            throws MessageBoxException {
        // Check permission to receive messages
        if (!authorizationHandler.isAuthorized(
                messageBoxId, MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        // Delete message
        try {
            MessageLock messageLock = getMessageLocks().get(receiptHandler);
            if (messageLock != null) {
                messageLock.deleteMessage();
                queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
            }
        } catch (MessageLockException e) {
            throw new MessageBoxException("InternalError", e);
        }
    }

    public List<String> listQueues(String qnamePrefix) throws MessageBoxException {
        return registryMessageBoxHandler.getMessageBoxURISuffixes(qnamePrefix);
    }

    public void changeVisibility(String messageBoxId, String receiptHandler,
                                 long extension) throws MessageBoxException {
        // Check permission to change message visibility
        if (!authorizationHandler.isAuthorized(
                messageBoxId, MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        // Accumulate visibility timeout
        try {
            MessageLock messageLock = getMessageLocks().get(receiptHandler);
            if (null != messageLock) {
                messageLock.changeMessageVisibility(extension);
                JMSQueueHandler.setVisibilityTimeoutStringProperty(messageLock.getVisibilityTimeout(),
                                                                   messageLock.getJmsMessage());
                queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
            }
        } catch (MessageLockException e) {
            throw new MessageBoxException("ExceedVisibilityTimeout", e);
        } catch (JMSQueueHandlerException e) {
            throw new MessageBoxException("ExceedVisibilityTimeout", e);
        }
    }

    public Map<String, String> getMessageBoxAttributes(String messageBoxId)
            throws MessageBoxException {
        // Check permissions to get message box attributes
        if (!authorizationHandler.isAuthorized(
                messageBoxId, MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        Map<String, String> attributeMap = new HashMap<String, String>();

        MessageBoxDetails messageBoxDetails =
                registryMessageBoxHandler.getMessageBoxDetails(messageBoxId);

        attributeMap.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP,
                         Long.toString(messageBoxDetails.getCreatedTimeStamp()));
        attributeMap.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_VISIBILITY_TIMEOUT,
                         Long.toString(messageBoxDetails.getDefaultVisibilityTimeout()));
        attributeMap.put(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES,
                         Integer.toString(getVisibleMessageCount(messageBoxId)));
        attributeMap.put(
                MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                Integer.toString(getHiddenMessageCount(messageBoxId)));

        return attributeMap;
    }

    public void setMessageBoxAttributes(String messageBoxId, Map<String, String> attributes)
            throws MessageBoxException {
        // Only an admin or the owner can set message box attributes
        String username = getCurrentUser();
        if (!Utils.isAdmin(username) && !isMessageBoxOwner(messageBoxId, username)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        registryMessageBoxHandler.setMessageBoxDetails(messageBoxId, attributes);
        queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
    }

    public void removePermission(String messageBoxId, String permissionLabelName)
            throws MessageBoxException {
        // Check permission to remove permissions on message box
        String username = getCurrentUser();
        if (!Utils.isAdmin(username) && !isMessageBoxOwner(messageBoxId, username)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }

        // Get permissions before removing from registry
        PermissionLabel removedPermissionLabel = registryMessageBoxHandler.getPermission(
                messageBoxId, permissionLabelName);
        // Remove from registry allowed permission label
        registryMessageBoxHandler.removePermission(messageBoxId, permissionLabelName);

        // Get all the permissions associated with this message box
        List<PermissionLabel> allPermissions =
                registryMessageBoxHandler.getAllPermissions(messageBoxId);

        /**
         * There may be permissions set with other permission labels, duplicate permissions
         * Before un-authorizing allowed permissions, search those permissions and add them
         * to another list of permission labels
         */
        List<PermissionLabel> otherAllowedPermissions = new ArrayList<PermissionLabel>();

        // Check for all removed users
        for (String removedSharedUser : removedPermissionLabel.getSharedUsers()) {
            // for all permissions on this message box
            for (PermissionLabel permissionLabel : allPermissions) {
                // just check if shared users contains removed user, if not no need of processing further
                if (permissionLabel.getSharedUsers().contains(removedSharedUser)) {
                    for (String removedOperation : removedPermissionLabel.getOperations()) {
                        if (permissionLabel.getOperations().contains(removedOperation)) {
                            // if removed operation is allowed in another label,
                            //  that should not be unauthorized
                            List<String> duplicatedSharedUsers = new ArrayList<String>();
                            duplicatedSharedUsers.add(removedSharedUser);
                            List<String> duplicatedOperations = new ArrayList<String>();
                            duplicatedOperations.add(removedOperation);
                            PermissionLabel duplicatedPermissions =
                                    new PermissionLabel("DuplicatedPermissions",
                                                        duplicatedSharedUsers, duplicatedOperations);

                            otherAllowedPermissions.add(duplicatedPermissions);
                        }
                    }
                }
            }
        }

        // remove all permissions with given permission label related users and operations
        authorizationHandler.removePermission(messageBoxId, removedPermissionLabel);


        //ToDo: all permissions are removed. but using queue manager, permissions may have set.
        // ToDo: those permissions too removed. this is wrong
        // remove consume/publish permissions
        List<String> removePermissionOperations = removedPermissionLabel.getOperations();
        for (String sharedUser : removedPermissionLabel.getSharedUsers()) {
            QueueUserPermission queueUserPermission = new QueueUserPermission();

            setPublishPermission(removePermissionOperations, queueUserPermission, false);
            setConsumePermission(removePermissionOperations, queueUserPermission, false);
            queueUserPermission.setUserName(sharedUser);

            List<QueueUserPermission> userPermissions = new ArrayList<QueueUserPermission>();
            userPermissions.add(queueUserPermission);

            queueManager.updateUserPermission(userPermissions,
                                              JMSQueueHandler.getJMSQueueName(messageBoxId));
        }

        // re-authorize removed permissions since those were authorized in a different permission label
        for (PermissionLabel permissionLabel : otherAllowedPermissions) {
            authorizationHandler.addPermission(messageBoxId, permissionLabel);
            for (String sharedUser : permissionLabel.getSharedUsers()) {
                QueueUserPermission queueUserPermission = new QueueUserPermission();

                setPublishPermission(removePermissionOperations, queueUserPermission, true);
                setConsumePermission(removePermissionOperations, queueUserPermission, true);
                queueUserPermission.setUserName(sharedUser);

                List<QueueUserPermission> userPermissions = new ArrayList<QueueUserPermission>();
                userPermissions.add(queueUserPermission);

                queueManager.updateUserPermission(userPermissions,
                                                  JMSQueueHandler.getJMSQueueName(messageBoxId));
            }
        }
        queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
    }

    public void addPermission(String messageBoxId, List<String> operationsList,
                              String permissionLabelName, List<String> sharedUsers)
            throws MessageBoxException {
        // Only an admin or the owner can add permissions on message box
        String username = getCurrentUser();
        if (!Utils.isAdmin(username) && !isMessageBoxOwner(messageBoxId, username)) {
            throw new MessageBoxException("AccessDenied");
        }

        if (!registryMessageBoxHandler.isMessageBoxExists(messageBoxId)) {
            throw new MessageBoxException("AWS.SimpleQueueService.NonExistentQueue");
        }
        sharedUsers = registryMessageBoxHandler.getSharedUsers(sharedUsers.toArray(
                new String[sharedUsers.size()]));
        PermissionLabel permissionLabel =
                new PermissionLabel(permissionLabelName, sharedUsers, operationsList);
        // Add permission on the Registry
        registryMessageBoxHandler.addPermission(messageBoxId, permissionLabel);

        // Authorize in the Authorization Manager
        authorizationHandler.addPermission(messageBoxId, permissionLabel);

        // allow consume/publish permissions
        List<String> operationList = permissionLabel.getOperations();
        for (String sharedUser : sharedUsers) {
            QueueUserPermission queueUserPermission = new QueueUserPermission();
            setPublishPermission(operationList, queueUserPermission, true);
            setConsumePermission(operationList, queueUserPermission, true);
            queueUserPermission.setUserName(sharedUser);
            List<QueueUserPermission> userPermissions = new ArrayList<QueueUserPermission>();
            userPermissions.add(queueUserPermission);

            queueManager.updateUserPermission(userPermissions,
                                              JMSQueueHandler.getJMSQueueName(messageBoxId));
        }
        queueManager.setQueueUpdatedTime(JMSQueueHandler.getJMSQueueName(messageBoxId));
    }

    private void setConsumePermission(List<String> operationList,
                                      QueueUserPermission queueUserPermission,
                                      boolean allowConsume) {
        if (operationList.contains(MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY) ||
            operationList.contains(MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES) ||
            operationList.contains(MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE) ||
            operationList.contains(MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE) ||
            operationList.contains(MessageBoxConstants.SQS_OPERATION_ALL)) {

            queueUserPermission.setAllowedToConsume(allowConsume);
        }
    }

    private void setPublishPermission(List<String> operationList,
                                      QueueUserPermission queueUserPermission,
                                      boolean allowPublish) {
        if (operationList.contains(MessageBoxConstants.SQS_OPERATION_ALL) ||
            operationList.contains(MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE)) {
            queueUserPermission.setAllowedToPublish(allowPublish);
        }
    }

    /**
     * Get currently logged in username
     *
     * @return Current username
     */
    private String getCurrentUser() {
        return MultitenantUtils.getTenantAwareUsername(CarbonContext.getCurrentContext().getUsername());
    }


    /**
     * Get messages which are invisible
     *
     * @param messageBoxID Name of the message box
     * @return Approximate hidden message count on the given message box
     */
    private int getHiddenMessageCount(String messageBoxID) {
        int count = 0;

        for (MessageLock messageLock : getMessageLocks().values()) {
            if (messageBoxID.equals(messageLock.getMessageBoxID())) {
                count++;
            }
        }

        return count;
    }

    /**
     * Get number of messages that are visible to consumers
     *
     * @param messageBoxID Interested message box name
     * @return Approximate visible message count
     * @throws org.wso2.carbon.messagebox.MessageBoxException
     *          if fails to get message count
     */
    private int getVisibleMessageCount(String messageBoxID) throws MessageBoxException {
        try {
            int count = 0;

            // Retrieve JMX objects
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(
                    "org.apache.qpid:" +
                    "type=VirtualHost.Queue," +
                    "VirtualHost=\"" + QPID_VHOST_NAME + "\"," +
                    "name=\"" + Utils.getTenantBasedQueueName(messageBoxID) + "\",*");
            Set<ObjectName> set = mBeanServer.queryNames(objectName, null);

            if (set.size() > 0) {
                ManagedQueue managedQueue =
                        MBeanServerInvocationHandler.newProxyInstance(mBeanServer,
                                                                      (ObjectName) set.toArray()[0],
                                                                      ManagedQueue.class, false);
                count = managedQueue.getMessageCount();
            }

            return count;
        } catch (MalformedObjectNameException e) {
            throw new MessageBoxException(e);
        } catch (IOException e) {
            throw new MessageBoxException(e);
        }
    }

    /**
     * Convert JMS message to SQSMessage
     *
     * @param jmsMessage Message to be converted
     * @return Corresponding SQSMessage instance
     * @throws MessageBoxException
     */
    public SQSMessage jmsMessageToSQSMessage(Message jmsMessage) throws MessageBoxException {
        SQSMessage sqsMessage = new SQSMessage();
        TextMessage textMessage = (TextMessage) jmsMessage;
        try {
            sqsMessage.setDefaultVisibilityTimeout(
                    Long.parseLong(textMessage.getStringProperty(
                            MessageBoxConstants.JMS_MESSAGE_PROPERTY_VISIBILITY_TIME_OUT)));
            sqsMessage.setMd5ofMessageBody(
                    textMessage.getStringProperty(
                            MessageBoxConstants.JMS_MESSAGE_PROPERTY_MD5_OF_MESSAGE));
            sqsMessage.setBody(textMessage.getText());
            sqsMessage.setMessageId(
                    textMessage.getStringProperty(
                            MessageBoxConstants.JMS_MESSAGE_PROPERTY_MESSAGE_ID));
            sqsMessage.getAttribute().put(
                    MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID,
                    textMessage.getStringProperty(
                            MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID));
            sqsMessage.getAttribute().put(
                    MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENT_TIMESTAMP,
                    textMessage.getStringProperty(
                            MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENT_TIMESTAMP));
            sqsMessage.getAttribute().put(
                    MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP,
                    textMessage.getStringProperty(
                            MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP));
            sqsMessage.getAttribute().put(
                    MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT,
                    textMessage.getStringProperty(
                            MessageBoxConstants.JMS_MESSAGE_PROPERTY_RECEIVED_COUNT));
            return sqsMessage;
        } catch (JMSException e) {
            String error = "Failed to convert JMS Message to SQSMessage";
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    /**
     * Convert JMS message to MessageDetails
     *
     * @param jmsMessage message to be converted
     * @return Corresponding MessageDetails instance
     * @throws MessageBoxException
     */
    public MessageDetails jmsMessageToMessageDetails(Message jmsMessage)
            throws MessageBoxException {
        try {
            MessageDetails messageDetails = new MessageDetails();
            TextMessage textMessage = (TextMessage) jmsMessage;
            String receivedCount;

            messageDetails.setMessageBody(textMessage.getText());
            messageDetails.setMessageId(textMessage.getStringProperty(
                    MessageBoxConstants.JMS_MESSAGE_PROPERTY_MESSAGE_ID));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(textMessage.getStringProperty(
                    MessageBoxConstants.JMS_MESSAGE_PROPERTY_VISIBILITY_TIME_OUT)));
            messageDetails.setDefaultVisibilityTimeout(calendar.getTime().toString());

            receivedCount = textMessage.getStringProperty(
                    MessageBoxConstants.JMS_MESSAGE_PROPERTY_RECEIVED_COUNT);
            messageDetails.setReceivedCount((null != receivedCount) ? receivedCount : "0");

            calendar.setTimeInMillis(Long.parseLong(textMessage.getStringProperty(
                    MessageBoxConstants.JMS_MESSAGE_PROPERTY_SENT_TIMESTAMP)));
            messageDetails.setSentTimestamp(calendar.getTime().toString());

            return messageDetails;
        } catch (JMSException e) {
            String error = "Failed to convert JMS Message to MessageDetails";
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    public QueueManager getQueueManager() throws MessageBoxException {
        return this.queueManager;
    }

    public void setQueueManager(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void setRegistryMessageBoxHandler(RegistryMessageBoxHandler registryMessageBoxHandler) {
        this.registryMessageBoxHandler = registryMessageBoxHandler;
    }

    public void setAuthorizationHandler(MessageBoxAuthorizationHandler authorizationHandler) {
        this.authorizationHandler = authorizationHandler;
    }


}
