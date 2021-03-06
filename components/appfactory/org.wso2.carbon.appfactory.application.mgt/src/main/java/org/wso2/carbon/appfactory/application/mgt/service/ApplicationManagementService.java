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
import org.wso2.carbon.CarbonConstants;
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


    public String createApplication(ApplicationInfoBean application) {
        return application.getApplicationKey();
    }


    public boolean addUserToApplication(String applicationId, String userName, String[] roles)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(applicationId));
            realm.getUserStoreManager().updateRoleListOfUser(userName, null, roles);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while adding user " + userName + " to application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

    }                               

    public boolean updateRolesOfUserForApplication (String applicationId, String userName, String[] rolesToDelete, String[] newRoles)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(applicationId));
            realm.getUserStoreManager().updateRoleListOfUser(userName, rolesToDelete, newRoles);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error while updating roles for user: " + userName + " of application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    public String[] getUsersOfApplication(String applicationId ) throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        ArrayList<String> userList = new ArrayList<String>();
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(applicationId));
            String[] roles = realm.getUserStoreManager().getRoleNames();
            if (roles.length > 0) {
                for (String roleName : roles) {
                    if (!Util.getRealmService().getBootstrapRealmConfiguration().getEveryOneRoleName().equals(roleName)) {
                        String[] usersOfRole = realm.getUserStoreManager().getUserListOfRole(roleName);
                        if (usersOfRole != null && usersOfRole.length > 0) {
                            for (String userName : usersOfRole) {
                                if (!userList.contains(userName) &&
                                    !Util.getRealmService().getBootstrapRealmConfiguration().getAdminUserName().equals(userName)
                                    && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                                    userList.add(userName);
                                }
                            }
                        }
                    }

                }
            }
            return userList.toArray(new String[userList.size()]);
        } catch (UserStoreException e) {
            String msg = "Error while getting users of application " + applicationId;
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
            String msg = "Error while removing user " + userName + " from application " + applicationId;
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
            String msg = "Error while revoking application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }


    public boolean isApplicationIdAvailable(String applicationKey)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        int tenantID;
        try {
            tenantID = tenantManager.getTenantId(applicationKey);
        } catch (UserStoreException e) {
            String msg = "Error while getting applicationKey " + applicationKey;
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

    public UserInfoBean[] getUserInfo(String applicationId) throws ApplicationManagementException {
        String[] users = getUsersOfApplication(applicationId);
        ArrayList<UserInfoBean> userInfoList = new ArrayList<UserInfoBean>();
        if (users != null && users.length >0 ) {
            for(int i = 0 ; i < users.length; i++ ) {
                try {
                    userInfoList.add(getUserInfoBean(users[i]));
                } catch (ApplicationManagementException e) {
                    String msg = "Error while getting info for user " + users[i]+ "\n Continue getting other users information";
                    log.error(msg, e);
                }
            }
        }
        return userInfoList.toArray(new UserInfoBean[userInfoList.size()]);
    }                   

    public String[] getAllApplications(String userName) throws ApplicationManagementException {
        String apps[] = new String[0];
        List<String> list = new ArrayList<String>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                UserRealm realm = Util.getRealmService().getTenantUserRealm(tenant.getId());
                // every user in everyone role
                if (realm != null && realm.getUserStoreManager().getRoleListOfUser(userName).length > 1) {
                    list.add(tenant.getDomain());
                }
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

    public String[] getRolesOfUserPerApplication(String appId, String userName)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        org.wso2.carbon.user.api.UserStoreManager userStoreManager;
        userStoreManager = null;
        String roles[];
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(appId));
            userStoreManager = realm.getUserStoreManager();
            roles = userStoreManager.getRoleListOfUser(userName);
        } catch (UserStoreException e) {
            String msg = "Error while getting role of the user " + userName;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return roles;
    }
}
