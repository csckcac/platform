package org.wso2.carbon.messagebox.internal.utils;


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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.qpid.management.common.mbeans.ManagedBroker;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static String getTenantAwareCurrentUserName() {
        String username = CarbonContext.getCurrentContext().getUsername();
        if (CarbonContext.getCurrentContext().getTenantId() > 0) {
            return username + "@" + CarbonContext.getCurrentContext().getTenantDomain();
        }
        return username;
    }

    public static UserRegistry getUserRegistry() throws RegistryException {
        RegistryService registryService =
                MessageBoxServiceValueHolder.getInstance().getRegistryService();

        return registryService.getGovernanceSystemRegistry(CarbonContext.getCurrentContext().getTenantId());

    }

    public static org.wso2.carbon.user.api.UserRealm getUserRelam() throws UserStoreException {
        return MessageBoxServiceValueHolder.getInstance().getRealmService().
                getTenantUserRealm(CarbonContext.getCurrentContext().getTenantId());
    }

    public static String getTenantBasedQueueName(String queueName) {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        if (tenantId > 0) {
            String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();
            tenantDomain = tenantDomain.replace(".", "-");
            queueName = tenantDomain + "-" + queueName;
        }
        return queueName;
    }

    /**
     * Checks if a given user has admin privileges
     *
     * @param username Name of the user
     * @return true if the user has admin rights or false otherwise
     * @throws org.wso2.carbon.messagebox.MessageBoxException
     *          if getting roles for the user fails
     */
    public static boolean isAdmin(String username) throws MessageBoxException {
        boolean isAdmin = false;

        try {
            String[] userRoles = MessageBoxServiceValueHolder.getInstance().getRealmService().
                    getTenantUserRealm(CarbonContext.getCurrentContext().getTenantId()).
                    getUserStoreManager().getRoleListOfUser(username);
            String adminRole = MessageBoxServiceValueHolder.getInstance().getRealmService().
                    getBootstrapRealmConfiguration().getAdminUserName();
            for (String userRole : userRoles) {
                if (userRole.equals(adminRole)) {
                    isAdmin = true;
                    break;
                }
            }
        } catch (UserStoreException e) {
            throw new MessageBoxException("Failed to get list of user roles", e);
        }

        return isAdmin;
    }

    public static void deleteQueue(String queue) throws MessageBoxException {
        try {
            // Retrieve JMX objects
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(
                    "org.apache.qpid:" +
                    "type=VirtualHost.VirtualHostManager," +
                    "VirtualHost=\"" + MessageBoxConstants.QPID_VHOST_NAME + "\"");
            Set<ObjectName> set = mBeanServer.queryNames(objectName, null);

            if (set.size() > 0) {
                ManagedBroker amqBrokerManagerMBean =
                        MBeanServerInvocationHandler.newProxyInstance(mBeanServer,
                                                                      (ObjectName) set.toArray()[0],
                                                                      ManagedBroker.class, false);
                amqBrokerManagerMBean.deleteQueue(queue);
            }

        } catch (MalformedObjectNameException e) {
            throw new MessageBoxException(e);
        } catch (InstanceNotFoundException e) {
            throw new MessageBoxException(e);
        } catch (MBeanException e) {
            throw new MessageBoxException(e);
        } catch (JMException e) {
            throw new MessageBoxException(e);
        } catch (IOException e) {
            throw new MessageBoxException(e);
        }
    }

    public static boolean queueExists(String queue) throws MessageBoxException {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(
                    "org.apache.qpid:" +
                    "type=VirtualHost.Queue," +
                    "VirtualHost=\"" + MessageBoxConstants.QPID_VHOST_NAME + "\"," +
                    "name=\"" + queue + "\",*");
            Set<ObjectName> set = mBeanServer.queryNames(objectName, null);

          return (set.size()>0);

        } catch (MalformedObjectNameException e) {
            throw new MessageBoxException(e);
        } catch (JMException e) {
            throw new MessageBoxException(e);
        }
    }

}
