/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.andes.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.andes.core.QueueManagerException;
import org.wso2.carbon.andes.core.internal.ds.QueueManagerServiceValueHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

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
                QueueManagerServiceValueHolder.getInstance().getRegistryService();

        return registryService.getGovernanceSystemRegistry(CarbonContext.getCurrentContext().getTenantId());

    }

    public static org.wso2.carbon.user.api.UserRealm getUserRelam() throws UserStoreException {
        return QueueManagerServiceValueHolder.getInstance().getRealmService().
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
     * @throws org.wso2.carbon.andes.core.QueueManagerException
     *          if getting roles for the user fails
     */
    public static boolean isAdmin(String username) throws QueueManagerException {
        boolean isAdmin = false;

        try {
            String[] userRoles = QueueManagerServiceValueHolder.getInstance().getRealmService().
                    getTenantUserRealm(CarbonContext.getCurrentContext().getTenantId()).
                    getUserStoreManager().getRoleListOfUser(username);
            String adminRole = QueueManagerServiceValueHolder.getInstance().getRealmService().
                    getBootstrapRealmConfiguration().getAdminUserName();
            for (String userRole : userRoles) {
                if (userRole.equals(adminRole)) {
                    isAdmin = true;
                    break;
                }
            }
        } catch (UserStoreException e) {
            throw new QueueManagerException("Failed to get list of user roles", e);
        }

        return isAdmin;
    }



}
