/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *   
 *    http://www.apache.org/licenses/LICENSE-2.0
 *   
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;


public class ApplicationManagementService extends AbstractAdmin {
    private static Log log = LogFactory.getLog(ApplicationManagementService.class);

    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";


    public String createProject(ApplicationInfoBean application) {
        return application.getProjectKey();
    }


    public boolean addUserToApplication(String applicationId, String userName)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        String roles[] = {"admin"};
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(applicationId));
            realm.getUserStoreManager().addUser(userName, null, roles, null, null);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while adding user " + userName + " to application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

    }

    public boolean removeUserFromApplication(String applicationId, String userName)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(applicationId));
            realm.getUserStoreManager().deleteUser(userName);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while removing user " + userName + " from project " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }


    public boolean revokeApplication(String applicationId) throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            tenantManager.deleteTenant(tenantManager.getTenantId(applicationId));
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while revoking project " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }


    public boolean isApplicationIdAvailable(String projectKey)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        int tenantID;
        try {
            tenantID = tenantManager.getTenantId(projectKey);
        } catch (UserStoreException e) {
            String msg = "Error while getting projectKey " + projectKey;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return tenantID < 0;
    }


    public UserInfoBean getUserInfoBean(String userName) throws ApplicationManagementException {

        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(0);
            String email = realm.getUserStoreManager().getUserClaimValue(userName,
                                                                         EMAIL_CLAIM_URI, null);
            String firstName = realm.getUserStoreManager().getUserClaimValue(userName,
                                                                             FIRST_NAME_CLAIM_URI, null);
            String lastName = realm.getUserStoreManager().getUserClaimValue(userName,
                                                                            LAST_NAME_CLAIM_URI, null);
            return new UserInfoBean(userName, firstName, lastName, email);
        } catch (UserStoreException e) {
            String msg = "Error while getting info for user " + userName;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    public String[] getAllApplications() throws ApplicationManagementException {
        String apps[] = new String[0];
        List<String> list = new ArrayList<String>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                list.add(tenant.getDomain());
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting all applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        if (!list.isEmpty()) {
            apps = list.toArray(new String[list.size()]);

        }
        return apps;
    }
}
