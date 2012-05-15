/*
 * Copyright (c) 2010 - 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.admin.mgt.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.beans.AdminMgtInfoBean;
import org.wso2.carbon.admin.mgt.constants.AdminMgtConstants;
import org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent;
import org.wso2.carbon.admin.mgt.util.AdminMgtUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * PasswordUtil - Utility class with the password related admin-management operations.
 */
public class PasswordUtil {
    private static final Log log = LogFactory.getLog(PasswordUtil.class);

    /**
     * Processing the password reset request by the user
     *
     * @param adminInfoBean tenant details
     * @return true if the reset request is processed successfully.
     * @throws Exception if reset password failed.
     */
    public static boolean resetPassword(AdminMgtInfoBean adminInfoBean) throws Exception {
        String adminName = adminInfoBean.getAdmin();
        String domainName = adminInfoBean.getTenantDomain();
        String email;
        String userName;

        TenantManager tenantManager = AdminManagementServiceComponent.getTenantManager();
        int tenantId = AdminMgtUtil.getTenantIdFromDomain(domainName);

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            userName = adminName;
        } else {
            userName = adminName + "@" + domainName;
        }
        Tenant tenant = (Tenant) tenantManager.getTenant(tenantId);

        try {
            email = getEmailAddressForUser(adminName, userName, tenantId, tenant);
        } catch (Exception e) {
            log.debug(AdminMgtConstants.NO_EMAIL_ADDRESS_SET_ERROR, e);
            return false;
        }

        if ((email == null) || (email.trim().equalsIgnoreCase(""))) {
            if (log.isDebugEnabled()) {
                log.debug(AdminMgtConstants.NO_EMAIL_ADDRESS_SET_ERROR);
            }
            return false;
        }

        // generates the confirmationKey to include in the email, and to set the resource under the adminMgtPath
        // of the tenant.
        String confirmationKey = generateConfirmationKey(tenantId);
        Map<String, String> dataToStore =
                populateDataMap(adminInfoBean, adminName, email, tenantId, tenant, confirmationKey);

        return verifyPasswordResetRequest(userName, dataToStore);
    }

    private static boolean verifyPasswordResetRequest(String userName,
                                                      Map<String, String> dataToStore) {
        try {
            AdminMgtUtil.requestUserVerification(dataToStore);
            if (log.isDebugEnabled()) {
                log.debug("Credentials Configurations mail has been sent for: " + userName);
            }
            return true;
        } catch (Exception e) {
            String msg = "Error in verifying the user for the configuration of the admin " +
                    "management for " + userName + ".";
            log.error(msg);
        }
        return false;
    }

    private static String generateConfirmationKey(int tenantId) throws RegistryException {
        // generating the confirmation key as a random UUID.
        String confirmationKey = UUIDGenerator.generateUUID();
        // resources are stored in the superTenant registry space, since no user is initially associated with the
        // password reset invocation, as no user logged in.
        UserRegistry superTenantGovernanceSystemRegistry =
                AdminManagementServiceComponent.getGovernanceSystemRegistry(
                        MultitenantConstants.SUPER_TENANT_ID);

        Resource resource;
        // adminManagementPath is associated with the tenantId, by appending it.
        String adminManagementPath = AdminMgtConstants.ADMIN_MANAGEMENT_FLAG_PATH +
                RegistryConstants.PATH_SEPARATOR + tenantId;

        if (superTenantGovernanceSystemRegistry.resourceExists(adminManagementPath)) {
            resource = superTenantGovernanceSystemRegistry.get(adminManagementPath);
        } else {
            resource = superTenantGovernanceSystemRegistry.newResource();
        }
        // confirmationKey is set as the content of the new resource.
        resource.setContent(confirmationKey);
        // resource is put into the superTenant Registry, with the adminMgtPath associated to the tenant.
        superTenantGovernanceSystemRegistry.put(adminManagementPath, resource);
        return confirmationKey;
    }

    private static Map<String, String> populateDataMap(AdminMgtInfoBean adminInfoBean,
                                                       String adminName, String email, int tenantId,
                                                       Tenant tenant,
                                                       String confirmationKey) throws Exception {
        Map<String, String> dataToStore = new HashMap<String, String>();
        dataToStore.put("email", email);
        dataToStore.put("first-name", ClaimsMgtUtil.getFirstName(
                AdminManagementServiceComponent.getRealmService(), tenant, tenantId));
        dataToStore.put("admin", adminName);
        dataToStore.put("tenantDomain", adminInfoBean.getTenantDomain());
        dataToStore.put("confirmationKey", confirmationKey);
        return dataToStore;
    }

    private static String getEmailAddressForUser(String adminName, String userName,
                                                 int tenantId, Tenant tenant) throws Exception {
        String email = "";
        try {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (log.isDebugEnabled()) {
                // Admin Name is included in the email, in case if the user has forgotten that.
                log.debug("Getting email address for the super tenant user password reset");
            }
            email = ClaimsMgtUtil.getEmailAddressFromUserProfile(
                    AdminManagementServiceComponent.getRealmService(), userName, tenant, tenantId);
        } else if (tenantId > 0) {
            String adminNameFromUserStore = ClaimsMgtUtil.getAdminUserNameFromTenantId(
                    AdminManagementServiceComponent.getRealmService(), tenantId);

            email = getEmailAddressForTenants(
                    userName, adminName, tenantId, tenant, adminNameFromUserStore);
        }
        } catch (Exception e) {
            String msg = "Unable to retrieve an email address associated with the given user.";
            log.info(msg, e);   // It is common to have users with no email address defined.
            throw new Exception (msg, e);
        }
        return email;
    }

    private static String getEmailAddressForTenants(String userName, String adminName, int tenantId,
                                                    Tenant tenant,
                                                    String adminNameFromUserStore)
            throws UserStoreException {
        String email = "";
        if (adminNameFromUserStore.equalsIgnoreCase(adminName)) {
            if (log.isDebugEnabled()) {
                log.debug("The User is a tenant admin");
            }
            email = tenant.getEmail();
        } else if (!adminNameFromUserStore.equalsIgnoreCase(adminName)) {
            if (log.isDebugEnabled()) {
                log.debug("A tenant user password reset");
            }
            email = ClaimsMgtUtil.getEmailAddressFromUserProfile(
                    AdminManagementServiceComponent.getRealmService(), userName, tenant, tenantId);
        }
        return email;
    }

    /**
     * Update Password with the user input
     * @param adminInfoBean, Admin Info Bean object
     * @param userStoreManager, UserStoreManager
     * @return true - if password was successfully reset
     * @throws Exception, if password reset failed.
     */
    public static boolean updatePassword(AdminMgtInfoBean adminInfoBean,
                                         UserStoreManager userStoreManager) throws Exception {
        String adminName, tenantDomain, password, userName;
        try {
            adminName = adminInfoBean.getAdmin();
            tenantDomain = adminInfoBean.getTenantDomain();
            password = adminInfoBean.getAdminPassword();
            userName = AdminMgtUtil.getUserNameWithDomain(adminName, tenantDomain);
        } catch (Exception e) {
            String msg = "Unable to find the required information for the password reset, " +
                    "from the adminInfoBean object";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        try {
            userStoreManager.updateCredentialByAdmin(adminName, password);
            String msg = "Password reset for the user: " + userName;
            log.info(msg);
            return true;
        } catch (UserStoreException e) {
            String msg = "Error in changing the password for user: " + userName;
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Updates the tenant admin password, with the tenant provided password or
     * the autogenerated password, in case the tenant forgot the initial password
     *
     * @param adminInfoBean tenant domain details
     * @return true if successfully reset
     * @throws Exception if failed due to userStore or registry exceptions.
     */
    public static boolean updateTenantPassword(AdminMgtInfoBean adminInfoBean) throws Exception {
        String tenantDomain = adminInfoBean.getTenantDomain();
        int tenantId = AdminMgtUtil.getTenantIdFromDomain(tenantDomain);
        UserStoreManager userStoreManager;

        // filling the non-set admin and admin password first
        UserRegistry configSystemRegistry =
                AdminManagementServiceComponent.getConfigSystemRegistry(tenantId);

        boolean updatePassword = false;
        if (adminInfoBean.getAdminPassword() != null
                && !adminInfoBean.getAdminPassword().equals("")) {
            updatePassword = true;
        }

        UserRealm userRealm = configSystemRegistry.getUserRealm();
        try {
            userStoreManager = userRealm.getUserStoreManager();
        } catch (UserStoreException e) {
            String msg = "Error in getting the user store manager for the user.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        if (!userStoreManager.isReadOnly() && updatePassword) {
            return updatePassword(adminInfoBean, userStoreManager);
        }
        return false;
    }

}
