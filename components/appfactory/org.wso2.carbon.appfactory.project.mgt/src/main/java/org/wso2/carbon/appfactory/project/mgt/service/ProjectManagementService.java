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

package org.wso2.carbon.appfactory.project.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.project.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;


public class ProjectManagementService extends AbstractAdmin {
    private static Log log = LogFactory.getLog(ProjectManagementService.class);

    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";


    public String createProject(ProjectInfoBean project) {
        return project.getProjectKey();
    }


    public boolean addUserToProject(String projectKey, String userName)
            throws ProjectManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        String roles[] = {"admin"};
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(projectKey));
            realm.getUserStoreManager().addUser(userName, null, roles, null, null);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while adding user " + userName + " to project " + projectKey;
            log.error(msg, e);
            throw new ProjectManagementException(msg, e);
        }

    }

    public boolean removeUserFromProject(String projectKey, String userName)
            throws ProjectManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(projectKey));
            realm.getUserStoreManager().deleteUser(userName);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while removing user " + userName + " from project " + projectKey;
            log.error(msg, e);
            throw new ProjectManagementException(msg, e);
        }
    }


    public boolean revokeProject(String projectKey) throws ProjectManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            tenantManager.deleteTenant(tenantManager.getTenantId(projectKey));
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while revoking project " + projectKey;
            log.error(msg, e);
            throw new ProjectManagementException(msg, e);
        }
    }


    public boolean isProjectKeyAvailable(String projectKey) throws ProjectManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        int tenantID;
        try {
            tenantID = tenantManager.getTenantId(projectKey);
        } catch (UserStoreException e) {
            String msg = "Error while getting projectKey " + projectKey;
            log.error(msg, e);
            throw new ProjectManagementException(msg, e);
        }
        return tenantID < 0;
    }


    public UserInfoBean getUserInfoBean(String userName) throws ProjectManagementException {

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
            throw new ProjectManagementException(msg, e);
        }
    }
}
