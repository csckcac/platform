/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.tenant.roles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.tenant.roles.util.Util;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import java.util.ArrayList;
import java.util.List;

public class DefaultRolesCreatorForTenant implements TenantMgtListener {
    private static Log log = LogFactory.getLog(DefaultRolesCreatorForTenant.class);
    private static final int EXEC_ORDER = 40;
    private static List<RoleBean> roleBeanList = null;

    static {
        try {
            init();
        } catch (StratosException e) {
            log.error("Default roles creator initialization failed.", e);
        }
    }

    public static void init() throws StratosException {
        roleBeanList = new ArrayList<RoleBean>();
        try {
            AppFactoryConfiguration configuration = Util.getConfiguration();
            String[] roles = configuration.getProperties("DefaultRoles.Role");
            String adminUser = Util.getRealmService().getBootstrapRealm().
                    getRealmConfiguration().getAdminUserName();
            for (String role : roles) {
                String resourceIdString =
                        configuration.getFirstProperty("DefaultRoles.Role." + role + ".Permission");
                String[] resourceIds = resourceIdString.split(",");
                RoleBean roleBean = new RoleBean(role);
                roleBean.addUser(adminUser);
                for (String resourceId : resourceIds) {
                    Permission permission = new Permission(resourceId,
                                                           CarbonConstants.UI_PERMISSION_ACTION);
                    roleBean.addPermission(permission);
                }
                roleBeanList.add(roleBean);
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String message = "Failed to read default roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }
    }


    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        try {
            UserStoreManager userStoreManager = Util.getRealmService().
                    getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();
            for (RoleBean roleBean : roleBeanList) {
                if (!userStoreManager.isExistingRole(roleBean.getRoleName())) {
                    userStoreManager.addRole(roleBean.getRoleName(),
                                             roleBean.getUsers().toArray(new String[roleBean.getUsers().size()]),
                                             roleBean.getPermissions().toArray(new Permission[roleBean.getPermissions().size()]));
                }
            }
        } catch (UserStoreException e) {
            String message = "Failed to create default roles of tenant:" +
                             tenantInfoBean.getTenantDomain();
            log.error(message);
            throw new StratosException(message, e);
        }
    }

    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    public void onTenantRename(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    public void onTenantInitialActivation(int i) throws StratosException {
        // Do nothing
    }

    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    public int getListenerOrder() {
        return EXEC_ORDER;
    }
}