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

package org.wso2.carbon.messagebox.internal.qpid;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxDetails;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.PermissionLabel;
import org.wso2.carbon.messagebox.internal.utils.Utils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistryMessageBoxHandler {


    /**
     * Constructor for Registry Message Box Handler
     * get user registry and create message box base store if does not exists already
     *
     * @throws MessageBoxException if fails to get user registry
     */
    public RegistryMessageBoxHandler() throws MessageBoxException {

        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            if (!userRegistry.resourceExists(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH)) {
                userRegistry.put(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH,
                                 userRegistry.newCollection());
            }
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the user registry ", e);
        }
    }

    /**
     * Create a message box collection in registry with properties given
     *
     * @param owner             - owner of the message box
     * @param name              - message box name
     * @param visibilityTimeout - default visibility time out defined for message box
     * @throws MessageBoxException if fails to create message box in registry
     */
    public void createMessageBox(String owner, String name, long visibilityTimeout)
            throws MessageBoxException {
        // create a new collection for this message box
        // here we need to store the message boxes with the user
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            if (!userRegistry.resourceExists(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH)) {
                userRegistry.put(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH,
                                 userRegistry.newCollection());
            }
            Collection collection = userRegistry.newCollection();
            collection.setProperty(MessageBoxConstants.MB_PROPERYY_VISIBILITY_TIMEOUT,
                                   String.valueOf(visibilityTimeout));

            collection.setProperty(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP,
                                   Long.toString(System.currentTimeMillis()));
            userRegistry.put(getMessageBoxResourcePath(owner, name), collection);
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not create a new registry collection ", e);
        }
    }

    /**
     * Check if the message box is created or not in registry
     * This method is overloaded with message box id
     *
     * @param owner - owner of the message box(creator)
     * @param name  - message box name
     * @return true if message box exists or false otherwise
     * @throws MessageBoxException if fails to find the resource in registry
     */
    public boolean isMessageBoxExists(String owner, String name) throws MessageBoxException {
        try {
            return Utils.getUserRegistry().resourceExists(getMessageBoxResourcePath(owner, name));
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not find the resource ", e);
        }
    }

    public List<String> getMessageBoxURISuffixes(String queueNamePrefix)
            throws MessageBoxException {
        String loggedInUserName = UserCoreUtil.getTenantLessUsername(CarbonContext.getCurrentContext().getUsername());

        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            List<String> messageBoxURISuffixes = new ArrayList<String>();
            if (!Utils.isAdmin(loggedInUserName)) {
                addMessageBoxSufixesForUser(loggedInUserName,
                                            messageBoxURISuffixes, queueNamePrefix);
            } else {
                Collection userCollection = (Collection) userRegistry.get(
                        MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH);
                for (String userName : userCollection.getChildren()) {
                    addMessageBoxSufixesForUser(userName.substring(userName.lastIndexOf("/") + 1),
                                                messageBoxURISuffixes,
                                                queueNamePrefix);
                }
            }
            return messageBoxURISuffixes;
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry ", e);
        }
    }

    private void addMessageBoxSufixesForUser(String userName,
                                             List<String> messageBoxURISuffixes,
                                             String queueNamePrefix)
            throws RegistryException {
        UserRegistry userRegistry = Utils.getUserRegistry();
        Collection messageBoxCollection = (Collection) userRegistry.get(
                MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" + userName);
        for (String messageBoxPath : messageBoxCollection.getChildren()) {
            String messageBoxName = messageBoxPath.substring(messageBoxPath.lastIndexOf("/") + 1);
            if (messageBoxName.startsWith(queueNamePrefix)) {
                messageBoxURISuffixes.add(userName + "/" +
                                          messageBoxPath.substring(messageBoxPath.lastIndexOf("/") + 1));
            }
        }
    }

    /**
     * Get message box resource path using message box owner and message box name
     *
     * @param owner - the creator of the message box
     * @param name  - message box name(this is different from message box id(owner/name)
     * @return resource path to given message box
     */
    public String getMessageBoxResourcePath(String owner, String name) {
        return MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" + owner + "/" + name;
    }

    /**
     * Get all message boxes from registry
     * get list of message boxes and their properties, construct message box details
     *
     * @return list of message box details
     * @throws MessageBoxException if fails to access registry and get message box details
     */
    public List<MessageBoxDetails> getMessageBoxDetails() throws MessageBoxException {
        List<MessageBoxDetails> messageBoxDetailsList = new ArrayList<MessageBoxDetails>();
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            Collection userCollection = (Collection)
                    userRegistry.get(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH);

            for (String userPath : userCollection.getChildren()) {
                String userName = userPath.substring(userPath.lastIndexOf("/") + 1);
                Collection messageBoxCollection = (Collection) userRegistry.get(userPath);
                for (String messageBoxPath : messageBoxCollection.getChildren()) {
                    String messageBoxName =
                            messageBoxPath.substring(messageBoxPath.lastIndexOf("/") + 1);
                    Resource messageBoxResource = userRegistry.get(messageBoxPath);
                    long visibilityTimeOut =
                            Long.parseLong(messageBoxResource.getProperty(
                                    MessageBoxConstants.MB_PROPERYY_VISIBILITY_TIMEOUT));
                    long createdTimestamp = Long.parseLong(messageBoxResource.getProperty(
                            MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP));
                    MessageBoxDetails messageBoxDetails =
                            new MessageBoxDetails(messageBoxName, userName, visibilityTimeOut, 0);
                    messageBoxDetails.setCreatedTimeStamp(createdTimestamp);
                    messageBoxDetailsList.add(messageBoxDetails);
                }
            }
            return messageBoxDetailsList;
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the user registry ", e);
        }
    }

    /**
     * Get all message boxes from registry
     * get list of message boxes and their properties, construct message box details
     *
     * @param messageBoxId
     * @return list of message box details
     * @throws MessageBoxException if fails to access registry and get message box details
     */
    public MessageBoxDetails getMessageBoxDetails(String messageBoxId) throws MessageBoxException {
        try {
            Collection userCollection = (Collection)
                    Utils.getUserRegistry().get(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" + messageBoxId);
            String userName = messageBoxId.split("/")[0];
            long visibilityTimeOut = Long.parseLong(userCollection.getProperty(MessageBoxConstants.MB_PROPERYY_VISIBILITY_TIMEOUT));
            long createdTimestamp = Long.parseLong(userCollection.getProperty(MessageBoxConstants.SQS_QUEUE_ATTRIBUTE_CREATED_TIMESTAMP));
            MessageBoxDetails messageBoxDetails =
                    new MessageBoxDetails(messageBoxId, userName, visibilityTimeOut, 0);
            messageBoxDetails.setCreatedTimeStamp(createdTimestamp);
            return messageBoxDetails;
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the user registry ", e);
        }
    }

    /**
     * Get all message boxes from registry
     * get list of message boxes and their properties, construct message box details
     *
     * @throws MessageBoxException if fails to access registry and get message box details
     */
    public void setMessageBoxDetails(String messageBoxId, Map<String, String> attributes)
            throws MessageBoxException {
        //ToDo: max message size, message retention period, policy, these attributes are not supported yet
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            Collection userCollection = (Collection)
                    userRegistry.get(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" + messageBoxId);
            for (Map.Entry entry : attributes.entrySet()) {
                userCollection.editPropertyValue(entry.getKey().toString(),
                                                 userCollection.getProperty(entry.getKey().toString()),
                                                 entry.getValue().toString());
            }

        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the user registry ", e);
        }
    }

    /**
     * Check if the message box exists in registry
     *
     * @param messageBoxID - message box to be checked
     * @return true if message box exists or false otherwise
     * @throws MessageBoxException if fails to find the resource
     */
    public boolean isMessageBoxExists(String messageBoxID) throws MessageBoxException {
        try {
            return Utils.getUserRegistry().resourceExists(getMessageBoxResourcePath(messageBoxID));
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not find the resource ", e);
        }
    }

    /**
     * Get the resource path to store message box in registry
     * Base storage +"/"+messageBoxId
     *
     * @param messageBoxID - get resource path to this message box
     * @return resource path to given message box
     */
    public String getMessageBoxResourcePath(String messageBoxID) {
        return MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" + messageBoxID;
    }

    /**
     * Remove given message box from registry
     *
     * @param messageBoxID - message box to be deleted
     * @throws MessageBoxException if fails to access registry and delete
     */
    public void deleteMessageBox(String messageBoxID) throws MessageBoxException {
        String messageBoxResourcePath = getMessageBoxResourcePath(messageBoxID);
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            userRegistry.delete(messageBoxResourcePath);
            userRegistry.delete(MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + messageBoxID.replaceFirst("/", "."));
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry", e);
        }
    }

    /**
     * Add the given permission label in registry
     * shared users are pipe separated and stored as a property
     * operations are pipe separated and stored as a property
     *
     * @param messageBoxId    - message box to which permissions are added
     * @param permissionLabel - permission label bean with operations and shared users
     * @throws MessageBoxException if fails to add resources in registry
     */
    public void addPermission(String messageBoxId, PermissionLabel permissionLabel)
            throws MessageBoxException {
        String permissionLabelResourcePath = getMessageBoxResourcePath(messageBoxId) + "/" +
                                             permissionLabel.getLabelName();
        try {
            StringBuffer sharedUsersString = new StringBuffer();
            for (String sharedUser : permissionLabel.getSharedUsers()) {
                sharedUsersString.append(sharedUser);
                sharedUsersString.append(MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);
            }

            StringBuffer operationsString = new StringBuffer();
            for (String operation : permissionLabel.getOperations()) {
                operationsString.append(operation);
                operationsString.append(MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);
            }
            UserRegistry userRegistry = Utils.getUserRegistry();
            Resource permissionResource = userRegistry.newResource();
            permissionResource.addProperty(MessageBoxConstants.MB_REGISTRY_PROPERTY_SHARED_USERS,
                                           sharedUsersString.toString());
            permissionResource.addProperty(MessageBoxConstants.MB_REGISTRY_PROPERTY_OPERATIONS,
                                           operationsString.toString());

            userRegistry.put(permissionLabelResourcePath, permissionResource);
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry", e);
        }
    }

    /**
     * Remove permission label from registry
     *
     * @param messageBoxId    - message box for which the permission label is applied
     * @param permissionLabel - permission label to be removed
     * @throws MessageBoxException if fails to access registry and delete label
     */
    public void removePermission(String messageBoxId, String permissionLabel)
            throws MessageBoxException {
        String permissionLabelResourcePath = getMessageBoxResourcePath(messageBoxId) + "/" + permissionLabel;
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            userRegistry.delete(permissionLabelResourcePath);
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry", e);
        }
    }

    /**
     * Get list of all permissions declared for given message box
     *
     * @param messageBoxId - message box id
     * @return list of permission labels
     * @throws MessageBoxException if fails to get permission labels
     */
    public List<PermissionLabel> getAllPermissions(String messageBoxId) throws MessageBoxException {
        PermissionLabel permissionLabel;
        List<PermissionLabel> permissionList = new ArrayList<PermissionLabel>();

        String permissionLabelResourcePath = getMessageBoxResourcePath(messageBoxId);
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            Collection messageBoxCollection = (Collection) userRegistry.get(permissionLabelResourcePath);
            for (String permissionResource : messageBoxCollection.getChildren()) {
                String[] sharedUsers = userRegistry.get(permissionResource).getProperty(
                        MessageBoxConstants.MB_REGISTRY_PROPERTY_SHARED_USERS).split(
                        "\\" + MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);

                String[] operations = userRegistry.get(permissionResource).getProperty(
                        MessageBoxConstants.MB_REGISTRY_PROPERTY_OPERATIONS).split(
                        "\\" + MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);

                String permissionLabelName = permissionResource.substring(permissionResource.
                        lastIndexOf("/") + 1);

                permissionLabel = new PermissionLabel(permissionLabelName, Arrays.asList(sharedUsers),
                                                      Arrays.asList(operations));
                permissionList.add(permissionLabel);
            }
            return permissionList;
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not find resources in the registry", e);
        }

    }

    /**
     * Get given permission label from registry
     *
     * @param messageBoxId    - message box name for which permission label is applied(associated)
     * @param permissionLabel - permission label name
     * @return Permission label object
     * @throws MessageBoxException if fails to get permission label from registry
     */
    public PermissionLabel getPermission(String messageBoxId, String permissionLabel)
            throws MessageBoxException {
        String permissionLabelResourcePath = getMessageBoxResourcePath(messageBoxId) + "/" +
                                             permissionLabel;

        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String[] sharedUsers = userRegistry.get(permissionLabelResourcePath).getProperty(
                    MessageBoxConstants.MB_REGISTRY_PROPERTY_SHARED_USERS).split(
                    "\\" + MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);

            String[] operations = userRegistry.get(permissionLabelResourcePath).getProperty(
                    MessageBoxConstants.MB_REGISTRY_PROPERTY_OPERATIONS).split(
                    "\\" + MessageBoxConstants.JMS_MESSAGE_SHARED_USER_OPERATION_SEPARATOR);

            return new PermissionLabel(permissionLabel, Arrays.asList(sharedUsers),
                                       Arrays.asList(operations));
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not find the resource in " + permissionLabelResourcePath, e);
        }
    }

    /**
     * Get shared users list from access keys
     *
     * @param accessKeys- array of access keys
     * @return - list of shared user names
     * @throws MessageBoxException - if fails to get user names
     */
    public List<String> getSharedUsers(String[] accessKeys) throws MessageBoxException {
        List<String> sharedUsersList = new ArrayList<String>();
        for (String accessKey : accessKeys) {
            sharedUsersList.add(getUserName(accessKey));
        }
        return sharedUsersList;
    }

    /**
     * Get the user name from access key id
     *
     * @param accessKeyId - access key id of the returned user
     * @return - user name
     * @throws MessageBoxException - if fails to get user name from access key id
     */
    private String getUserName(String accessKeyId)
            throws MessageBoxException {
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            if (userRegistry.resourceExists(MessageBoxConstants.REGISTRY_ACCESS_KEY_INDEX_PATH)) {
                org.wso2.carbon.registry.api.Collection userCollection =
                        (org.wso2.carbon.registry.api.Collection) userRegistry.
                                get(MessageBoxConstants.REGISTRY_ACCESS_KEY_INDEX_PATH);
                if (userCollection != null) {
                    return userCollection.getProperty(accessKeyId);
                }
            }
            return null;
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new MessageBoxException("Failed to get secret id of user " + accessKeyId);
        }
    }

}
