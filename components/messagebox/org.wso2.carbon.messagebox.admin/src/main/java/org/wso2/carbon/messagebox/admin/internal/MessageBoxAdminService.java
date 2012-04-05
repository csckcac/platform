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
package org.wso2.carbon.messagebox.admin.internal;

import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxDetails;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageDetails;
import org.wso2.carbon.messagebox.PermissionLabel;
import org.wso2.carbon.messagebox.admin.internal.exception.MessageBoxAdminException;
import org.wso2.carbon.messagebox.admin.internal.util.MessageBoxHolder;
import org.wso2.carbon.messagebox.internal.utils.Utils;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.util.List;
import java.util.UUID;

/**
 * Admin service for message boxes, Which provides to receive messages and list messages functionality.
 */
public class MessageBoxAdminService extends AbstractAdmin {
    /**
     * Get all message boxes
     *
     * @return array of MessageBox details
     * @throws MessageBoxAdminException if failed to list message boxes
     */
    public MessageBoxDetail[] getAllMessageBoxes(int startingIndex, int maxMessageBoxesCount)
            throws MessageBoxAdminException {
        MessageBoxHolder messageBoxService = MessageBoxHolder.getInstance();
        MessageBoxDetail[] messageBoxDetailArray;
        try {
            String[] eprs = MessageContext.getCurrentMessageContext().getConfigurationContext().getAxisConfiguration().getServiceForActivation("MessageQueue").getEPRs();
            MessageBoxDetails[] messageBoxDetailsArray =
                    messageBoxService.getMessageboxService().getAllMessageBoxes();
            int resultSetSize = maxMessageBoxesCount;
            if ((messageBoxDetailsArray.length - startingIndex) < maxMessageBoxesCount) {
                resultSetSize = (messageBoxDetailsArray.length - startingIndex);
            }
            messageBoxDetailArray = new MessageBoxDetail[resultSetSize];
            int index = 0;
            int messageBoxDetailsIndex = 0;
            for (MessageBoxDetails messageBoxDetails : messageBoxDetailsArray) {
                if (startingIndex == index || startingIndex < index) {
                    messageBoxDetailArray[messageBoxDetailsIndex] = new MessageBoxDetail();
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setMessageBoxName(messageBoxDetails.getMessageBoxName());
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setNumberOfMessages(messageBoxDetails.getNumberOfMessages());
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setOwner(messageBoxDetails.getMessageBoxOwner());
                    messageBoxDetailArray[messageBoxDetailsIndex].setVisibilityTimeout(
                            Long.toString(messageBoxDetails.getDefaultVisibilityTimeout()));
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setMessageBoxId(messageBoxDetails.getMessageBoxId());
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setTenantDomain(messageBoxDetails.getTenantDomain());
                    List<String> sharedUsersList = messageBoxDetails.getSharedUsersList();
                    messageBoxDetailArray[messageBoxDetailsIndex].
                            setSharedUsers(sharedUsersList.toArray(new String[sharedUsersList.size()]));
                    messageBoxDetailArray[messageBoxDetailsIndex].setEpr(eprs);
                    messageBoxDetailsIndex++;
                    if (messageBoxDetailsIndex == maxMessageBoxesCount) {
                        break;
                    }
                }
                index++;
            }
            return messageBoxDetailArray;
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to list all messageboxes.", e);
        }
    }

    /**
     * Get message boxes count
     *
     * @return total number of message boxes
     * @throws MessageBoxAdminException
     */
    public int getMessageBoxesCount() throws MessageBoxAdminException {
        MessageBoxHolder messageBoxService = MessageBoxHolder.getInstance();
        try {
            return messageBoxService.getMessageboxService().getAllMessageBoxes().length;
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to get total number of message boxes.", e);
        }
    }

    /**
     * Get all available messages to receive (messages which are not received at the moment)
     *
     * @param messageBoxId message box id in which messages need to be taken
     * @return array of Message Details
     * @throws MessageBoxAdminException if fails to get messages
     */
    public MessageDetails[] getAllAvailableMessages(String messageBoxId)
            throws MessageBoxAdminException {
        MessageBoxHolder messageBoxService = MessageBoxHolder.getInstance();
        try {
            return messageBoxService.getMessageboxService().getAllAvailableMessages(messageBoxId);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to receive messages from messagebox: " +
                                               messageBoxId, e);
        }

    }

    /**
     * Get all retrieved messages (messages which are not available yet to receive)
     *
     * @param messageBoxId message box id in which messages need to be taken
     * @return array of Message Details
     * @throws MessageBoxAdminException if fails to get messages
     */
    public MessageDetails[] getAllRetrievedMessages(String messageBoxId)
            throws MessageBoxAdminException {
        MessageBoxHolder messageBoxService = MessageBoxHolder.getInstance();
        try {
            return messageBoxService.getMessageboxService().getAllRetrievedMessages(messageBoxId);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to receive messages from messagebox: " +
                                               messageBoxId, e);
        }

    }

    /**
     * Get all permissions on given message box
     *
     * @param messageBoxId - message box id to be get permissions
     * @return PermissionLabel array containing permissions declared for this message box
     * @throws MessageBoxAdminException - if fails to get permissions
     */
    public PermissionLabel[] getAllPermissions(String messageBoxId)
            throws MessageBoxAdminException {
        MessageBoxHolder messageBoxService = MessageBoxHolder.getInstance();
        try {
            List<PermissionLabel> permissionLabels =
                    messageBoxService.getMessageboxService().getAllPermissions(messageBoxId);
            return permissionLabels.toArray(new PermissionLabel[permissionLabels.size()]);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to receive messages from messagebox: " +
                                               messageBoxId, e);
        }
    }

    /**
     * Get SQS keys for a given user
     *
     * @param userName - generate or get keys for this user
     * @return - SQSKeys object
     * @throws MessageBoxAdminException- if fails to get SQSKeys
     */
    public SQSKeys getSQSKeys(String userName) throws MessageBoxAdminException {
        Registry registry =
                CarbonContext.getCurrentContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        String loggedInUser = CarbonContext.getCurrentContext().getUsername();

        String accessKeyId = null;
        String secretAccessKeyId = null;
        Collection userCollection;
        try {
            if (registry.resourceExists(RegistryConstants.PROFILES_PATH + userName)) {
                userCollection = (Collection) registry.get(RegistryConstants.PROFILES_PATH + userName);
                accessKeyId = userCollection.getProperty(MessageBoxConstants.ACCESS_KEY_ID);
                secretAccessKeyId = userCollection.getProperty(MessageBoxConstants.SECRET_ACCESS_KEY_ID);
            }

            if (accessKeyId == null || secretAccessKeyId == null ||
                (!registry.resourceExists(RegistryConstants.PROFILES_PATH + userName))) {

                // generate keys
                accessKeyId = UUID.randomUUID().toString();
                secretAccessKeyId = UUID.fromString(accessKeyId).toString().
                        concat(UUID.randomUUID().toString()).replaceAll("-", "").substring(24);
                accessKeyId = accessKeyId.replaceAll("-", "").substring(12);

                // store keys in registry
                userCollection = registry.newCollection();
                registry.put(RegistryConstants.PROFILES_PATH + userName, userCollection);
                userCollection.addProperty(MessageBoxConstants.ACCESS_KEY_ID, accessKeyId);
                userCollection.addProperty(MessageBoxConstants.SECRET_ACCESS_KEY_ID, secretAccessKeyId);
                registry.put(RegistryConstants.PROFILES_PATH + userName, userCollection);


                // store user/access key in registry
                String accessKeyIndexPath = MessageBoxConstants.REGISTRY_ACCESS_KEY_INDEX_PATH;
                if (!registry.resourceExists(accessKeyIndexPath)) {
                    userCollection = registry.newCollection();
                    registry.put(accessKeyIndexPath, userCollection);
                }
                userCollection = (Collection) registry.get(accessKeyIndexPath);
                userCollection.addProperty(accessKeyId, userName);

                registry.put(accessKeyIndexPath, userCollection);
            }

            // we only allow user and admin to see the secret keys
            try {
                if (!(loggedInUser.equals(userName) || Utils.isAdmin(loggedInUser))) {
                    secretAccessKeyId = null;
                }
            } catch (MessageBoxException e) {
                throw new MessageBoxAdminException(
                        "Failed to check if the logged in user has admin privileges.");
            }

            return new SQSKeys(accessKeyId, secretAccessKeyId);
        } catch (RegistryException e) {
            throw new MessageBoxAdminException("Failed to get access keys of user " + userName, e);
        }

    }
}