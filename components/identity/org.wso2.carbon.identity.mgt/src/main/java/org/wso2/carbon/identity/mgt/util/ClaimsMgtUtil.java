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
package org.wso2.carbon.identity.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.IdentityMgtException;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserEvidenceDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * user claim related functions are done by this
 */
public class ClaimsMgtUtil {

    private static final Log log = LogFactory.getLog(ClaimsMgtUtil.class);

    
    /**
     * Gets the first name of user or tenant admin to address him/her in the notifications
     *
     * @param userName userName of user
     * @param tenantId tenant Id
     * @return first name / calling name
     * @throws IdentityMgtException if unable to retrieve the first name
     */
    public static String getFirstName(String userName, int tenantId) throws IdentityMgtException {

        String firstName = null;
        try {
            firstName = getClaimFromUserStoreManager(userName, tenantId,
                                                        UserCoreConstants.ClaimTypeURIs.GIVEN_NAME);
        } catch (Exception e) {
            String msg = "Unable to get the first name from the user store manager";
            log.warn(msg, e);
            // Not exceptions, due to the existence of tenants with no full name.
        }

        if(firstName == null || firstName.trim().length() < 1){
            RealmService realmService = IdentityMgtServiceComponent.getRealmService();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                return realmService.getBootstrapRealmConfiguration().getAdminUserName();
            }
            try {
                if (realmService.getTenantManager().getTenant(tenantId) != null) {
                    firstName = realmService.getTenantManager().getTenant(tenantId).getAdminName();
                }
            } catch (UserStoreException e) {
                String msg = "Unable to retrieve the first name for tenant admin with the tenant Id: " +
                        tenantId;
                log.error(msg, e);
                throw new IdentityMgtException(msg, e);
            }
        }

        return firstName;
    }


    /**
     * Get the claims from the user store manager
     *
     * @param userName user name
     * @param tenantId tenantId
     * @param claim claim name
     * @return claim value
     * @throws IdentityMgtException if fails
     */
    public static String getClaimFromUserStoreManager(String userName,int tenantId, String claim)
                                                                    throws IdentityMgtException {

        UserStoreManager userStoreManager = null;
        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        String claimValue = "";

        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for tenant id : " + tenantId;
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
        try {
            if (userStoreManager != null) {
                claimValue = userStoreManager.getUserClaimValue(userName, claim,
                                                                UserCoreConstants.DEFAULT_PROFILE);
            }
            return claimValue;
        } catch (Exception e) {
            String msg = "Unable to retrieve the claim for user : " + userName;
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
    }

    /**
     * Set claim to user store manager
     *
     * @param userName user name
     * @param tenantId  tenant id
     * @param claim  claim uri
     * @param value  claim value
     * @throws IdentityMgtException if fails
     */
    public static void setClaimInUserStoreManager(String userName,int tenantId, String claim,
                                                  String value) throws IdentityMgtException {
        UserStoreManager userStoreManager = null;
        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for the tenant";
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
        
        try {
            if (userStoreManager != null) {
                userStoreManager.setUserClaimValue(userName, claim, value,
                                                                UserCoreConstants.DEFAULT_PROFILE);
            }
        } catch (Exception e) {
            String msg =  "Unable to set the claim for user : " + userName;
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
    }

    /**
     * get user list for given claim uri and value
     *
     * @param tenantId  tenant id
     * @param claim   claim uri
     * @param value  claim value
     * @return  user list
     * @throws IdentityMgtException  if fails
     */
    public static String[] getUserList(int tenantId, String claim, String value) throws IdentityMgtException {

        UserStoreManager userStoreManager = null;
        String[] userList = null;
        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        
        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for the tenant";
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
        try {
            if (userStoreManager != null) {
                userList = ((AbstractUserStoreManager)userStoreManager).getUserList(claim, value, null);
            }
            return userList;
        } catch (Exception e) {
            String msg = "Unable to retrieve the claim for the given tenant";
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
    }

    /**
     * get email address from user store
     * 
     * @param userName  user name
     * @param tenantId  tenant id
     * @return  email address
     */
    public static String getEmailAddressForUser(String userName, int tenantId) {

        String email = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving email address from user profile.");
            }

            Tenant tenant = IdentityMgtServiceComponent.getRealmService().
                                                            getTenantManager().getTenant(tenantId);
            if(tenant != null){
                if(tenant.getAdminName().equals(userName)){
                    email = tenant.getEmail();
                }
            }

            if(email == null || email.trim().length() < 1){
                email = getClaimFromUserStoreManager(userName, tenantId,
                                                    UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS);
            }            
        } catch (Exception e) {
            String msg = "Unable to retrieve an email address associated with the given user : " + userName;
            log.warn(msg, e);   // It is common to have users with no email address defined.
        }

        return email;
    }

}