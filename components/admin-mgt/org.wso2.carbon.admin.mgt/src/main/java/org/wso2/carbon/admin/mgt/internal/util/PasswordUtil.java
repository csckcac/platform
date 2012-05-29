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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.admin.mgt.beans.AdminMgtInfoBean;
import org.wso2.carbon.admin.mgt.constants.AdminMgtConstants;
import org.wso2.carbon.admin.mgt.exception.AdminManagementException;
import org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent;
import org.wso2.carbon.admin.mgt.util.AdminMgtUtil;
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
    private static Log audit = CarbonConstants.AUDIT_LOG;

    /**
     * Processing the password reset request by the user
     *
     * @param adminInfoBean tenant details
     * @return true if the reset request is processed successfully.
     * @throws Exception if reset password failed.
     */
    public static boolean initiatePasswordReset(AdminMgtInfoBean adminInfoBean) throws Exception {
        String tenantLessUserName = adminInfoBean.getTenantLessUserName();
        String domainName = adminInfoBean.getTenantDomain();
        String email;
        String userName;

        TenantManager tenantManager = AdminManagementServiceComponent.getTenantManager();
        int tenantId = AdminMgtUtil.getTenantIdFromDomain(domainName);

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            userName = tenantLessUserName;
        } else {
            userName = tenantLessUserName + "@" + domainName;
        }
        Tenant tenant = (Tenant) tenantManager.getTenant(tenantId);

        try {
            email = getEmailAddressForUser(tenantLessUserName, userName, tenantId, tenant);
        } catch (AdminManagementException e) {
            log.error(AdminMgtConstants.NO_EMAIL_ADDRESS_SET_ERROR, e);
            return false;
        }

        if ((email == null) || (email.trim().equalsIgnoreCase(""))) {
            if (log.isDebugEnabled()) {
                log.debug(AdminMgtConstants.NO_EMAIL_ADDRESS_SET_ERROR);
            }
            return false;
        }

        // generates the confirmationKey to include in the email, and to set the resource under the
        // adminMgtPath of the tenant.
        String confirmationKey = generateConfirmationKey(tenantLessUserName, domainName);
        Map<String, String> dataToStore =
                populateDataMap(adminInfoBean, tenantLessUserName, email, tenantId, confirmationKey);

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

    private static String generateConfirmationKey(
            String adminName, String domain) throws RegistryException, AdminManagementException {
        // generating the confirmation key as a random UUID.
        String confirmationKey = UUIDGenerator.generateUUID();
        // resources are stored in the superTenant registry space, since no user is initially
        // associated with the password reset invocation, as no user logged in.
        UserRegistry superTenantGovernanceSystemRegistry =
                AdminManagementServiceComponent.getGovernanceSystemRegistry(
                        MultitenantConstants.SUPER_TENANT_ID);

        Resource resource;
        // adminManagementPath is associated with the tenantId, by appending it.
        String adminManagementPath = AdminMgtUtil.getAdminManagementPath(adminName, domain);

        if (superTenantGovernanceSystemRegistry.resourceExists(adminManagementPath)) {
            resource = superTenantGovernanceSystemRegistry.get(adminManagementPath);
        } else {
            resource = superTenantGovernanceSystemRegistry.newResource();
        }
        // confirmationKey is set as the content of the new resource.
        resource.setContent(confirmationKey);
        // resource is put into the superTenant Registry, with the adminMgtPath
        // associated to the tenant.
        superTenantGovernanceSystemRegistry.put(adminManagementPath, resource);
        return confirmationKey;
    }

    private static Map<String, String> populateDataMap(AdminMgtInfoBean adminInfoBean,
                                                       String adminName, String email, int tenantId,
                                                       String confirmationKey) throws
            AdminManagementException {
        Map<String, String> dataToStore = new HashMap<String, String>();
        dataToStore.put("email", email);
        dataToStore.put("first-name", ClaimsMgtUtil.getFirstName(
                AdminManagementServiceComponent.getRealmService(), tenantId));
        dataToStore.put("admin", adminName);
        dataToStore.put("tenantDomain", adminInfoBean.getTenantDomain());
        dataToStore.put("confirmationKey", confirmationKey);
        return dataToStore;
    }

    private static String getEmailAddressForUser(String tenantLessUserName, String userName,
                                                 int tenantId,
                                                 Tenant tenant) throws AdminManagementException {
        String email = "";
        try {
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                if (log.isDebugEnabled()) {
                    log.debug("Getting email address for the super tenant user password reset");
                }
                email = ClaimsMgtUtil.getEmailAddressFromUserProfile(
                        AdminManagementServiceComponent.getRealmService(), userName, tenantId);
                audit.info("Password reset link for the user " + userName + " of the super tenant" +
                        " to be sent to the email address " + email);
            } else if (tenantId > 0) {
                String adminNameFromUserStore = ClaimsMgtUtil.getAdminUserNameFromTenantId(
                        AdminManagementServiceComponent.getRealmService(), tenantId);
                email = getEmailAddressForTenants(
                        userName, tenantLessUserName, tenantId, tenant, adminNameFromUserStore);
            }
        } catch (AdminManagementException e) {
            String msg = "Unable to retrieve an email address associated with the given user.";
            log.info(msg, e);   // It is common to have users with no email address defined.
            throw new AdminManagementException(msg, e);
        }
        return email;
    }

    private static String getEmailAddressForTenants(String userName, String adminName, int tenantId,
                                                    Tenant tenant,
                                                    String adminNameFromUserStore)
            throws AdminManagementException {
        String email = "";
        if (adminNameFromUserStore.equalsIgnoreCase(adminName)) {
            if (log.isDebugEnabled()) {
                log.debug("Password reset for a tenant admin");
            }
            email = tenant.getEmail();
            audit.info("Password reset link for the tenant admin " + userName + " of tenant id: "
                    + tenantId + " to be sent to the email address " + email);
        } else if (!adminNameFromUserStore.equalsIgnoreCase(adminName)) {
            if (log.isDebugEnabled()) {
                log.debug("Password reset for a non-admin tenant user");
            }
            email = ClaimsMgtUtil.getEmailAddressFromUserProfile(
                    AdminManagementServiceComponent.getRealmService(), userName, tenantId);
            audit.info("Password reset link for a user " + userName + " of the tenant of " +
                    "tenant id: " + tenantId + " to be sent to the email address " + email);
        }
        return email;
    }

    /**
     * Update Password with the user input
     *
     * @param adminInfoBean,    Admin Info Bean object
     * @param userStoreManager, UserStoreManager
     * @return true - if password was successfully reset
     * @throws AdminManagementException, if password reset failed.
     */
    private static boolean updatePassword(AdminMgtInfoBean adminInfoBean,
                                          UserStoreManager userStoreManager) throws
            AdminManagementException {
        String tenantLessUserName, tenantDomain, password, userName;
        tenantLessUserName = adminInfoBean.getTenantLessUserName();
        tenantDomain = adminInfoBean.getTenantDomain();
        password = adminInfoBean.getPassword();
        userName = AdminMgtUtil.getUserNameWithDomain(tenantLessUserName, tenantDomain);
        try {
            userStoreManager.updateCredentialByAdmin(tenantLessUserName, password);
            String msg = "Password reset for the user: " + userName;
            log.info(msg);
            audit.info("Password for the user " + userName + " is successfully reset");
            return true;
        } catch (UserStoreException e) {
            String msg = "Error in changing the password for user: " + userName;
            audit.error("Error in changing the password for the user: " + userName);
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
    }

    /**
     * Updates the password, with the user provided password
     *
     * @param adminInfoBean tenant domain details
     * @return true if successfully reset
     * @throws AdminManagementException, update password failed due to AdminManagementException
     * @throws UserStoreException,       exception in getting the user store.
     * @throws RegistryException,        exception in getting the config system registry.
     */
    public static boolean updateCredentials(AdminMgtInfoBean adminInfoBean)
            throws AdminManagementException, UserStoreException, RegistryException {
        String tenantDomain = adminInfoBean.getTenantDomain();
        int tenantId = AdminMgtUtil.getTenantIdFromDomain(tenantDomain);
        UserStoreManager userStoreManager;

        // filling the non-set admin and admin password first
        UserRegistry configSystemRegistry =
                AdminManagementServiceComponent.getConfigSystemRegistry(tenantId);

        boolean updatePassword = false;
        if (adminInfoBean.getPassword() != null
                && !adminInfoBean.getPassword().equals("")) {
            updatePassword = true;
        }

        UserRealm userRealm = configSystemRegistry.getUserRealm();
        try {
            userStoreManager = userRealm.getUserStoreManager();
        } catch (UserStoreException e) {
            String msg = "Error in getting the user store manager for the user.";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }

        if (!userStoreManager.isReadOnly() && updatePassword) {
            return updatePassword(adminInfoBean, userStoreManager);
        }
        return false;
    }

    /**
     * To proceed updating credentials
     *
     * @param domain          domain name to update the credentials
     * @param adminName       adminName
     * @param confirmationKey confirmation key to verify the request.
     * @return True, if successful in verifying and hence updating the credentials.
     * @throws RegistryException,        confirmation key doesn't exist in the registry.
     * @throws AdminManagementException, getting the admin Management path failed.
     */
    public static boolean proceedUpdateCredentials(String domain, String adminName,
                                                   String confirmationKey)
            throws RegistryException, AdminManagementException {

        String adminManagementPath = AdminMgtUtil.getAdminManagementPath(adminName, domain);

        UserRegistry superTenantSystemRegistry = AdminManagementServiceComponent.
                getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
        Resource resource;
        if (superTenantSystemRegistry.resourceExists(adminManagementPath)) {
            resource = superTenantSystemRegistry.get(adminManagementPath);
            String actualConfirmationKey = null;
            Object content = resource.getContent();
            if (content instanceof String) {
                actualConfirmationKey = (String) content;
            } else if (content instanceof byte[]) {
                actualConfirmationKey = new String((byte[]) content);
            }

            if ((actualConfirmationKey != null) &&
                    (actualConfirmationKey.equals(confirmationKey))) {
                if (log.isDebugEnabled()) {
                    log.debug("Password resetting for the user of the domain: " + domain);
                }
                return true;
            } else if (actualConfirmationKey == null ||
                    !actualConfirmationKey.equals(confirmationKey)) {
                String msg = AdminMgtConstants.CONFIRMATION_KEY_NOT_MACHING;
                log.error(msg);
                return false; // validation fails; do not proceed
            }
        } else {
            log.warn("The confirmationKey doesn't exist in service.");
        }
        return false;
    }
}
