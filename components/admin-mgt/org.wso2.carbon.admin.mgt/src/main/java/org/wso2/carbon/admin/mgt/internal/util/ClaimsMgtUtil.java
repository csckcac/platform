/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.admin.mgt.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.exception.AdminManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/*
* This class handles the parameters that are input during the registration
* which later are
* stored as claims.
*
* Current claims are:
* First Name - GIVEN_NAME
* Last Name - SURNAME
* Email - EMAIL
*/
public class ClaimsMgtUtil {

    private static final Log log = LogFactory.getLog(ClaimsMgtUtil.class);

    /**
     * Gets the first name of a tenant admin to address him/her in the notifications
     *
     * @param realmService RealmService
     * @param tenant       tenant
     * @param tenantId     tenant Id
     * @return first name / calling name
     * @throws AdminManagementException, if unable to retrieve the admin name
     */
    public static String getFirstName(RealmService realmService, Tenant tenant,
                                      int tenantId) throws AdminManagementException {
        String firstName = "";
        try {
            firstName = getFirstNameFromUserStoreManager(realmService, tenantId);
        } catch (Exception e) {
            String msg = "Unable to get the first name from the user store manager";
            log.info(msg, e);
            // Not exceptions, due to the existence of tenants with no full name.
        }
        if (firstName == null || firstName.trim().equals("")) {
            if (log.isDebugEnabled()) {
                log.debug("First name is not available");
            }
            try {
                firstName = getAdminUserNameFromTenantId(realmService, tenant.getId());
            } catch (AdminManagementException e) {
                String msg = "Unable to get the admin Name from the user store manager";
                log.error(msg, e);
                throw new AdminManagementException(msg, e);
            }
        }
        return firstName;
    }

    /**
     * Get the claims of the admin from the user store manager
     *
     * @param realmService RealmService
     * @param tenantId     tenantId
     * @param claim        claim name
     * @return claim value
     * @throws AdminManagementException, exception in getting the tenant admin claim
     */
    public static String getTenantAdminClaim(RealmService realmService,
                                             int tenantId, String claim)
            throws AdminManagementException {
        String userName;

        try {
            userName = getAdminUserNameFromTenantId(realmService, tenantId);
        } catch (AdminManagementException e) {
            String msg = "Couldn't find the admin user name for the tenant with tenant id: " +
                    tenantId;
            log.warn(msg);
            throw new AdminManagementException(msg, e);
        }
        return getClaimFromUserStoreManager(realmService, userName, tenantId, claim);
    }

    /**
     * Get the claims from the user store manager
     *
     * @param realmService RealmService
     * @param userName     user name
     * @param tenantId     tenantId
     * @param claim        claim name
     * @return claim value
     * @throws AdminManagementException, exception in getting the user store manager
     */
    public static String getClaimFromUserStoreManager(RealmService realmService, String userName,
                                                      int tenantId, String claim)
            throws AdminManagementException {
        UserStoreManager userStoreManager = null;
        String claimValue = "";
        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }
        } catch (UserStoreException e) {
            String msg = "Error retrieving the user store manager for the tenant";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
        try {
            if (userStoreManager != null) {
                claimValue = userStoreManager.getUserClaimValue(userName, claim,
                        UserCoreConstants.DEFAULT_PROFILE);
            }
            return claimValue;
        } catch (UserStoreException e) {
            String msg = "Unable to retrieve the claim for the given tenant";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
    }

    /**
     * Gets first name from the user store manager
     *
     * @param realmService RealmService
     * @param tenantId     tenant id
     * @return first name
     * @throws AdminManagementException, if getting the given name failed
     */
    public static String getFirstNameFromUserStoreManager(RealmService realmService,
                                                          int tenantId) throws
            AdminManagementException {
        return getTenantAdminClaim(realmService, tenantId,
                UserCoreConstants.ClaimTypeURIs.GIVEN_NAME);
    }

    /**
     * Gets email address from the user store manager
     *
     * @param realmService RealmService
     * @param userName     user name
     * @param tenantId     tenant id
     * @return email email
     * @throws AdminManagementException, if getting the claim email address failed.
     */
    public static String getEmailAddressFromUserProfile(RealmService realmService,
                                                        String userName, int tenantId)
            throws AdminManagementException {
        return getClaimFromUserStoreManager(realmService, userName, tenantId,
                UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS);
    }

    /**
     * Method to get the name of the admin user given the tenant id
     *
     * @param realmService RealmService
     * @param tenantId     tenant id
     * @return admin user name
     * @throws AdminManagementException, if unable to get the admin username from the tenant id.
     */
    public static String getAdminUserNameFromTenantId(RealmService realmService,
                                                      int tenantId) throws AdminManagementException {
        String tenantAdminName = "";
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return realmService.getBootstrapRealmConfiguration().getAdminUserName();
        }
        try {
            if (realmService.getTenantManager().getTenant(tenantId) != null) {
                tenantAdminName = realmService.getTenantManager().getTenant(tenantId).getAdminName();
            }
        } catch (UserStoreException e) {
            String msg = "Unable to retrieve the admin name for the tenant with the tenant Id: " +
                    tenantId;
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
        return tenantAdminName;
    }
}