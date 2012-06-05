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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.PermissionLabel;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.messagebox.internal.utils.Utils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Authorization of message boxes for given operations are handled here.
 */
public class MessageBoxAuthorizationHandler {
    private static final Log log = LogFactory.getLog(MessageBoxAuthorizationHandler.class);

    /**
     * check if the logged in user is authorized to do the operation on messageBoxId
     * if logged in user owns(created) the message box, he is authorized to do any operation
     *
     * @param messageBoxId - message box id to which operation is done
     * @param operation    - operation to check for authorization
     * @return true if logged in user authorized to do the operation
     * @throws MessageBoxException if fails to check authorizations
     */
    public boolean isAuthorized(String messageBoxId, String operation) throws MessageBoxException {
        String loggedInUser = MultitenantUtils.getTenantAwareUsername(CarbonContext.getCurrentContext().getUsername());
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;

            return authorizationManager.isUserAuthorized(loggedInUser, messageBoxPath, operation);
        } catch (UserStoreException e) {
            String error = "Failed to check is " + loggedInUser + " authorized to " + operation +
                           " to " + messageBoxId;
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    public boolean isUserAuthorized(String userName, String messageBoxId, String operation)
            throws MessageBoxException {
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;

            return authorizationManager.isUserAuthorized(userName, messageBoxPath, operation);
        } catch (UserStoreException e) {
            String error = "Failed to check is " + userName + " authorized to " + operation +
                           " to " + messageBoxId;
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    /**
     * Authorize all shared users for all operations declared in permission label
     *
     * @param messageBoxId    - message box to which permission is allowed
     * @param permissionLabel - permission label bean with shared users and operations
     * @throws MessageBoxException if failed to apply permissions
     */
    public void addPermission(String messageBoxId, PermissionLabel permissionLabel)
            throws MessageBoxException {
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;

            for (String sharedUser : permissionLabel.getSharedUsers()) {
                // if there is no role with this role add the role and assign the role to the user
                UserStoreManager userStoreManager = Utils.getUserRelam().getUserStoreManager();
                if (!userStoreManager.isExistingRole(sharedUser)) {
                    userStoreManager.addRole(sharedUser, new String[]{sharedUser}, new Permission[0]);
                }
                for (String operation : permissionLabel.getOperations()) {
                    authorizationManager.authorizeRole(sharedUser, messageBoxPath, operation);
                }
            }
        } catch (UserStoreException e) {
            String error = "Failed to add permissions to " + messageBoxId + " with permission label "
                           + permissionLabel.getLabelName();
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }


    public void allowAllPermissionsToAdminRole(String messageBoxId) throws MessageBoxException {
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;
            String adminRoleName = MessageBoxServiceValueHolder.getInstance().
                    getRealmService().getBootstrapRealmConfiguration().getAdminRoleName();
            authorizationManager.authorizeRole(adminRoleName, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE);
            authorizationManager.authorizeRole(adminRoleName, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY);
            authorizationManager.authorizeRole(adminRoleName, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES);
            authorizationManager.authorizeRole(adminRoleName, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE);
            authorizationManager.authorizeRole(adminRoleName, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE);

        } catch (UserStoreException e) {
            String error = "Failed to add permissions to admin role for message box " + messageBoxId;
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    /**
     * Allow sqs operations on message box to the given user
     *
     * @param messageBoxId - message box on permissions are allowed
     * @param user         - the user whom to the permission are allowed
     * @throws MessageBoxException - if fails to allow permissions
     */
    public void allowAllPermissionsToUser(String messageBoxId, String user)
            throws MessageBoxException {
        try {

            // if there is no role with this role add the role and assign the role to the user
            UserStoreManager userStoreManager = Utils.getUserRelam().getUserStoreManager();
            if (!userStoreManager.isExistingRole(user)){
                 userStoreManager.addRole(user, new String[]{user}, new Permission[0]);
            }
                      
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;
            authorizationManager.authorizeRole(user, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE);
            authorizationManager.authorizeRole(user, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY);
            authorizationManager.authorizeRole(user, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES);
            authorizationManager.authorizeRole(user, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE);
            authorizationManager.authorizeRole(user, messageBoxPath,
                                               MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE);

        } catch (UserStoreException e) {
            String error = "Failed to allow permissions to user " + user + " for message box " + messageBoxId;
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }




    /**
     * Deny sqs operations on message box to the given user
     *
     * @param messageBoxId - message box on permissions are denied
     * @param user         - the user whom to the permission are denied
     * @throws MessageBoxException - if fails to deny permissions
     */
    public void denyAllPermissionsToUser(String messageBoxId, String user)
            throws MessageBoxException {
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;
            authorizationManager.denyUser(user, messageBoxPath,
                                          MessageBoxConstants.SQS_OPERATION_DELETE_MESSAGE);
            authorizationManager.denyUser(user, messageBoxPath,
                                          MessageBoxConstants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY);
            authorizationManager.denyUser(user, messageBoxPath,
                                          MessageBoxConstants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES);
            authorizationManager.denyUser(user, messageBoxPath,
                                          MessageBoxConstants.SQS_OPERATION_RECEIVE_MESSAGE);
            authorizationManager.denyUser(user, messageBoxPath,
                                          MessageBoxConstants.SQS_OPERATION_SEND_MESSAGE);

        } catch (UserStoreException e) {
            String error = "Failed to deny permissions to user" + user + " for message box " + messageBoxId;
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

    /**
     * Clear all authorizations declared in permission label
     *
     * @param messageBoxId    - message box on which permissions are cleared
     * @param permissionLabel - permission label bean with shared users and operations
     * @throws MessageBoxException if fails to clear authorizations
     */
    public void removePermission(String messageBoxId, PermissionLabel permissionLabel)
            throws MessageBoxException {
        try {
            AuthorizationManager authorizationManager = Utils.getUserRelam().getAuthorizationManager();

            String messageBoxPath = MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH + "/" +
                                    messageBoxId;

            for (String sharedUser : permissionLabel.getSharedUsers()) {
                for (String operation : permissionLabel.getOperations()) {
                    authorizationManager.clearUserAuthorization(sharedUser, messageBoxPath, operation);
                }
            }
        } catch (UserStoreException e) {
            String error = "Failed to clear permissions authorized for " + messageBoxId +
                           " with permission label " + permissionLabel.getLabelName();
            log.error(error);
            throw new MessageBoxException(error, e);
        }
    }

}
