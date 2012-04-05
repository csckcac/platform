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

package org.wso2.carbon.messagebox.internal.registry;

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.messagebox.internal.utils.Utils;
import org.wso2.carbon.messagebox.queue.Queue;
import org.wso2.carbon.messagebox.queue.QueueManager;
import org.wso2.carbon.messagebox.queue.QueueRolePermission;
import org.wso2.carbon.messagebox.queue.QueueUserPermission;
import org.wso2.carbon.qpid.service.QpidService;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistryQueueManager implements QueueManager {

    public RegistryQueueManager() throws MessageBoxException {
        init();
    }

    public void init() throws MessageBoxException {

        // creates the the subscription intex
        // when creating subscriptions we going to add entries to this this resource
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();

            //create the topic storage path if it does not exists
            if (!userRegistry.resourceExists(MessageBoxConstants.MB_QUEUE_STORAGE_PATH)) {
                userRegistry.put(MessageBoxConstants.MB_QUEUE_STORAGE_PATH, userRegistry.newCollection());
            }

        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry", e);
        }
    }

    public List<Queue> getAllQueues() throws MessageBoxException {
        List<Queue> queues = new ArrayList<Queue>();
        String queueName;
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            Collection queuesResource =
                    (Collection) userRegistry.get(MessageBoxConstants.MB_QUEUE_STORAGE_PATH);
            Resource queueResource;
            Queue queue;
            String createdTime;
            String updatedTime;
            String createdFrom;
            for (String childResource : queuesResource.getChildren()) {
                queueName = childResource.substring(childResource.indexOf(MessageBoxConstants.MB_QUEUE_STORAGE_PATH)
                                                    + MessageBoxConstants.MB_QUEUE_STORAGE_PATH.length() + 1);
                queueResource = userRegistry.get(childResource);
                queue = new Queue(queueName);
                createdTime = queueResource.getProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_CREATED_TIME);
                if (createdTime != null) {
                    queue.setCreatedTime(ConverterUtil.convertToDateTime(createdTime));
                }
                updatedTime = queueResource.getProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_UPDATED_TIME);
                if (updatedTime != null) {
                    queue.setUpdatedTime(ConverterUtil.convertToDateTime(updatedTime));
                }
                createdFrom = queueResource.getProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_CREATED_FROM);
                if (createdFrom != null) {
                    queue.setCreatedFrom(createdFrom);
                }
                Object queueDepth = getAttribute(queueName, MessageBoxConstants.MB_QUEUE_ATTR_QUEUE_DEPTH);
                if (queueDepth != null) {
                    queue.setQueueDepth((Long) queueDepth);
                } else {
                    queue.setQueueDepth(0);
                }

                Object messageCount = getAttribute(queueName, MessageBoxConstants.MB_QUEUE_ATTR_MESSAGE_COUNT);
                if (messageCount != null) {
                    queue.setMessageCount((Integer) messageCount);
                } else {
                    queue.setMessageCount(0);
                }

                queues.add(queue);
            }
            return queues;
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the registry ", e);
        }

    }

    private Object getAttribute(String queueName, String attributeName) throws MessageBoxException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        QpidService qpidService = MessageBoxServiceValueHolder.getInstance().getQpidService();
        try {
            ObjectName objectName =
                    new ObjectName("org.apache.qpid:type=VirtualHost.Queue,VirtualHost=\""
                                   + qpidService.getVirtualHostName() + "\",name=\"" +
                                   Utils.getTenantBasedQueueName(queueName) + "\"");
            return mBeanServer.getAttribute(objectName, attributeName);
        } catch (MalformedObjectNameException e) {
            throw new MessageBoxException("Can not find the mbean for the queue " + queueName);
        } catch (InstanceNotFoundException e) {
            // if the queue is not found we return 0
            return null;
        } catch (ReflectionException e) {
            throw new MessageBoxException("Can not find the mbean for the queue " + queueName);
        } catch (AttributeNotFoundException e) {
            throw new MessageBoxException("Can not find the mbean for the queue " + queueName);
        } catch (MBeanException e) {
            throw new MessageBoxException("Can not find the mbean for the queue " + queueName);
        }
    }


    public void addQueue(String queueName, String createdFrom) throws MessageBoxException {
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String resourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
            String ownerName = CarbonContext.getCurrentContext().getUsername();

            //we add the queue only if it does not exits. if the topic exists then
            //we don't do any thing.
            if (!userRegistry.resourceExists(resourcePath)) {
                Collection collection = userRegistry.newCollection();
                collection.setProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_CREATED_TIME,
                                       ConverterUtil.convertToString(Calendar.getInstance()));
                collection.setProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_UPDATED_TIME,
                                       ConverterUtil.convertToString(Calendar.getInstance()));
                collection.setProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_CREATED_FROM,
                                       createdFrom);
                collection.setProperty(MessageBoxConstants.MB_PROPERYY_OWNER, ownerName);

                userRegistry.put(resourcePath, collection);

                //assign the permissions for the queue to user
                UserRealm userRealm =
                        MessageBoxServiceValueHolder.getInstance().getRealmService().getTenantUserRealm(0);
                String userName = CarbonContext.getCurrentContext().getUsername();
                // if there is no role with this role add the role and assign the role to the user
                UserStoreManager userStoreManager = Utils.getUserRelam().getUserStoreManager();
                if (!userStoreManager.isExistingRole(userName)) {
                    userStoreManager.addRole(userName, new String[]{userName}, new Permission[0]);
                }
                userRealm.getAuthorizationManager().authorizeRole(
                        userName, resourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME);
                userRealm.getAuthorizationManager().authorizeRole(
                        userName, resourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH);
                userRealm.getAuthorizationManager().authorizeRole(
                        userName, resourcePath, MessageBoxConstants.MB_PERMISSION_CHANGE_PERMISSION);

            }
        } catch (RegistryException e) {
            throw new MessageBoxException("Can not access the config registry");
        } catch (UserStoreException e) {
            throw new MessageBoxException("Can not access the user realm");
        }
    }

    public List<QueueUserPermission> getQueueUserPermissions(String queueName)
            throws MessageBoxException {

        String queueResoucePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
        List<QueueUserPermission> queueUserPermissions = new ArrayList<QueueUserPermission>();
        UserRealm userRealm = CarbonContext.getCurrentContext().getUserRealm();
        QueueUserPermission queueUserPermission;
        try {
            for (String user : userRealm.getUserStoreManager().listUsers("*", 10)) {
                queueUserPermission = new QueueUserPermission();
                queueUserPermission.setUserName(user);
                queueUserPermission.setAllowedToConsume(
                        userRealm.getAuthorizationManager().isUserAuthorized(
                                user, queueResoucePath, MessageBoxConstants.MB_PERMISSION_CONSUME));
                queueUserPermission.setAllowedToPublish(
                        userRealm.getAuthorizationManager().isUserAuthorized(
                                user, queueResoucePath, MessageBoxConstants.MB_PERMISSION_PUBLISH));
                queueUserPermissions.add(queueUserPermission);
            }
            return queueUserPermissions;
        } catch (UserStoreException e) {
            throw new MessageBoxException("Can not access the Userstore manager ", e);
        }
    }

    public List<QueueRolePermission> getQueueRolePermissions(String queueName)
            throws MessageBoxException {
        String queueResourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
        List<QueueRolePermission> queueRolePermissions = new ArrayList<QueueRolePermission>();
        UserRealm userRealm = CarbonContext.getCurrentContext().getUserRealm();
        QueueRolePermission queueRolePermission;
        try {
            for (String roleName : userRealm.getUserStoreManager().getRoleNames()) {
                queueRolePermission = new QueueRolePermission();
                queueRolePermission.setRoleName(roleName);
                queueRolePermission.setAllowedToConsume(
                        userRealm.getAuthorizationManager().isRoleAuthorized(
                                roleName, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME));
                queueRolePermission.setAllowedToPublish(
                        userRealm.getAuthorizationManager().isRoleAuthorized(
                                roleName, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH));
                queueRolePermissions.add(queueRolePermission);
            }
            return queueRolePermissions;
        } catch (UserStoreException e) {
            throw new MessageBoxException("Can not access the UserStore manager ", e);
        }
    }


    public void updateRolePermission(List<QueueRolePermission> queueRolePermissions,
                                     String queueName) throws MessageBoxException {
        String queueResourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
        UserRealm userRealm = CarbonContext.getCurrentContext().getUserRealm();
        String role;

        try {
            String loggedInUser = CarbonContext.getCurrentContext().getUsername();
            if (!userRealm.getAuthorizationManager().isUserAuthorized(
                    loggedInUser, queueResourcePath,
                    MessageBoxConstants.MB_PERMISSION_CHANGE_PERMISSION)) {
                if (!Utils.isAdmin(loggedInUser)) {
                    throw new MessageBoxException(" User " + loggedInUser + " can not change" +
                                                  " the permissions of " + queueName);
                }
            }

            for (QueueRolePermission queueRolePermission : queueRolePermissions) {
                role = queueRolePermission.getRoleName();
                if (queueRolePermission.isAllowedToConsume()) {
                    if (!userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME)) {
                        userRealm.getAuthorizationManager().denyRole(
                                role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME);
                    }
                }

                if (queueRolePermission.isAllowedToPublish()) {
                    if (!userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isRoleAuthorized(
                            role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().denyRole(
                                role, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH);
                    }
                }
            }
            setQueueUpdatedTime(queueName);
        } catch (UserStoreException e) {
            throw new MessageBoxException("Can not access the user store manager", e);
        }
    }

    public void updateUserPermission(List<QueueUserPermission> queueUserPermissions,
                                     String queueName)
            throws MessageBoxException {
        String queueResourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
        UserRealm userRealm = CarbonContext.getCurrentContext().getUserRealm();
        String user;

        try {
            String loggedInUser = CarbonContext.getCurrentContext().getUsername();
            if (!userRealm.getAuthorizationManager().isUserAuthorized(
                    loggedInUser, queueResourcePath,
                    MessageBoxConstants.MB_PERMISSION_CHANGE_PERMISSION)) {
                if (!Utils.isAdmin(loggedInUser)) {
                    throw new MessageBoxException(" User " + loggedInUser + " can not change" +
                                                  " the permissions of " + queueName);
                }
            }


            for (QueueUserPermission queueUserPermission : queueUserPermissions) {
                user = queueUserPermission.getUserName();
                // if there is no role with this role add the role and assign the role to the user
                UserStoreManager userStoreManager = Utils.getUserRelam().getUserStoreManager();
                if (!userStoreManager.isExistingRole(user)) {
                    userStoreManager.addRole(user, new String[]{user}, new Permission[0]);
                }

                if (queueUserPermission.isAllowedToConsume()) {
                    if (!userRealm.getAuthorizationManager().isUserAuthorized(
                            user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isUserAuthorized(
                            user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME)) {
                        userRealm.getAuthorizationManager().denyRole(
                                user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_CONSUME);
                    }
                }

                if (queueUserPermission.isAllowedToPublish()) {
                    if (!userRealm.getAuthorizationManager().isUserAuthorized(
                            user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().authorizeRole(
                                user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH);
                    }
                } else {
                    if (userRealm.getAuthorizationManager().isUserAuthorized(
                            user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH)) {
                        userRealm.getAuthorizationManager().denyRole(
                                user, queueResourcePath, MessageBoxConstants.MB_PERMISSION_PUBLISH);
                    }
                }
            }
            setQueueUpdatedTime(queueName);
        } catch (UserStoreException e) {
            throw new MessageBoxException("Can not access the user store manager", e);
        }
    }

    public void deleteQueue(String queueName) throws MessageBoxException {
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String resourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
            userRegistry.delete(resourcePath);
            if(Utils.queueExists(queueName)){
                Utils.deleteQueue(queueName);
            }
        } catch (RegistryException e) {
            throw new MessageBoxException("Failed to delete queue: " + queueName, e);
        }
    }

    public void setQueueUpdatedTime(String queueName) throws MessageBoxException {
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String resourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;

            if (userRegistry.resourceExists(resourcePath)) {
                Collection collection = (Collection) userRegistry.get(resourcePath);
                collection.removeProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_UPDATED_TIME);
                collection.setProperty(MessageBoxConstants.MB_QUEUE_PROPERTY_UPDATED_TIME,
                                       ConverterUtil.convertToString(Calendar.getInstance()));
                userRegistry.put(resourcePath, collection);
            }
        } catch (RegistryException e) {
            throw new MessageBoxException("Failed to set queue updated time on queue: " + queueName, e);
        }
    }

    @Override
    public boolean isQueueExists(String queueName) throws MessageBoxException {
        try {
            UserRegistry userRegistry = Utils.getUserRegistry();
            String resourcePath = MessageBoxConstants.MB_QUEUE_STORAGE_PATH + "/" + queueName;
            return userRegistry.resourceExists(resourcePath);
        } catch (RegistryException e) {
            throw new MessageBoxException("Failed to delete queue: " + queueName, e);
        }
    }
}
