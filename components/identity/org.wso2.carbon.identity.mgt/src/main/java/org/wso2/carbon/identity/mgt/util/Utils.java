package org.wso2.carbon.identity.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;
import org.wso2.carbon.identity.mgt.IdentityMgtException;
import org.wso2.carbon.identity.mgt.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.cache.LoginAttemptCache;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserEvidenceDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Properties;

/**
 *
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * Processes user id and tenant domain that contains uin the UserMgtBean
     *
     * @param userMgtBean bean class that contains user and tenant Information
     * @throws IdentityMgtException  if user id doesn't exist
     */
    public static void processUserId(UserMgtBean userMgtBean) throws IdentityMgtException {

        String domainName = null;
        String userId = userMgtBean.getUserId();

        if(userId == null || userId.trim().length() < 1){
            throw new IdentityMgtException("Can not proceed with out a user id");    
        }
        
        if(userMgtBean.getTenantDomain() == null || userMgtBean.getTenantDomain().trim().length() < 1){
            try{
                domainName = UserCoreUtil.
                        getTenantDomain(IdentityMgtServiceComponent.getRealmService(), userId);
            } catch (UserStoreException ignore) {
                // there is another option.  therefore just ignore
            }
        } else {
            domainName = userMgtBean.getTenantDomain();
        }

        userId = UserCoreUtil.getTenantLessUsername(userId);

        userMgtBean.setTenantDomain(domainName);
        userMgtBean.setUserId(userId);
    }


    /**
     * verify the user id
     *
     * @param userMgtBean  bean class that contains user and tenant Information
     * @return verification success or not
     * @throws IdentityMgtException  if fails or tenant doesn't exist
     */
    public static boolean verifyUserId(UserMgtBean userMgtBean) throws IdentityMgtException {

        boolean verification = false;
        String userKey = userMgtBean.getUserKey();

        if(userKey == null || userKey.trim().length() < 1){
            return false;
        }

        UserRegistry registry = null;
        try{
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            registry.beginTransaction();

            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();

            Resource resource;
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String actualUserKey = resource.getProperty(IdentityMgtConstants.USER_KEY);
                if ((actualUserKey != null) && (actualUserKey.equals(userKey))) {
                    verification = true;
                    registry.delete(identityKeyMgtPath);
                }
            }
        } catch (RegistryException e) {
            log.error("Error while processing userKey", e);
        } finally {
            if(registry != null){
                try{
                    if (verification) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }

        return verification;
    }

    /**
     * gets no of verified user challenges
     *
     * @param userMgtBean bean class that contains user and tenant Information
     * @return no of verified challenges
     * @throws IdentityMgtException  if fails
     */
    public static int getVerifiedChallenges(UserMgtBean userMgtBean) throws IdentityMgtException {

        int noOfChallenges = 0;

        try{
            UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_CHALLENGES +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();

            Resource resource;
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String property = resource.getProperty(IdentityMgtConstants.VERIFIED_CHALLENGES);
                if (property != null) {
                    return Integer.valueOf(property);
                }
            }
        } catch (RegistryException e) {
            log.error("Error while processing userKey", e);
        }

        return noOfChallenges;
    }

    /**
     * clear the pointer for verified user challenges
     * @param userMgtBean   bean class that contains user and tenant Information
     * @throws IdentityMgtException if fails
     */
    public static void clearVerifiedChallenges(UserMgtBean userMgtBean) throws IdentityMgtException {

        try{
            UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_CHALLENGES +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();
            if (registry.resourceExists(identityKeyMgtPath)) {
                registry.delete(identityKeyMgtPath);
            }
        } catch (RegistryException e) {
            log.error("Error while clearing meta data in challenge verification process", e);
        }
    }

    /**
     * set pointer for verified user challenges 
     * @param userMgtBean  bean class that contains user and tenant Information
     * @throws IdentityMgtException  if fails
     */
    public static void setVerifiedChallenges(UserMgtBean userMgtBean) throws IdentityMgtException {

        try{
            UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_CHALLENGES +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();
            Resource resource;
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String property = resource.getProperty(IdentityMgtConstants.VERIFIED_CHALLENGES);
                int noOfChallenges = 0;
                if(property != null){
                    noOfChallenges = Integer.valueOf(property) + 1;
                }
                resource.setProperty(IdentityMgtConstants.VERIFIED_CHALLENGES,
                                                                Integer.toString(noOfChallenges));
                registry.put(identityKeyMgtPath, resource);
            } else {
                resource = registry.newResource();
                resource.addProperty(IdentityMgtConstants.VERIFIED_CHALLENGES, "1");
                resource.setVersionableChange(false);
                registry.put(identityKeyMgtPath, resource);
            }
        } catch (RegistryException e) {
            log.error("Error while processing userKey", e);
        }

    }

    /**
     * gets the tenant id from the tenant domain
     * 
     * @param domain - tenant domain name
     * @return tenantId
     * @throws IdentityMgtException if fails or tenant doesn't exist
     */
    public static int getTenantId(String domain) throws IdentityMgtException {

        int tenantId;
        TenantManager tenantManager = IdentityMgtServiceComponent.getRealmService().getTenantManager();
        
        if (domain == null || domain.trim().length() < 1) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (log.isDebugEnabled()) {
                String msg = "Domain is not defined implicitly. So it is Super Tenant domain.";
                log.debug(msg);
            }
        } else {
            try {
                tenantId = tenantManager.getTenantId(domain);
                if (tenantId < 1) {
                    String msg = "This action can not be performed by the users in non-existing domains.";
                    log.error(msg);
                    throw new IdentityMgtException(msg);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String msg = "Error in retrieving tenant id of tenant domain: " + domain + ".";
                log.error(msg, e);
                throw new IdentityMgtException(msg, e);
            }
        }
        return tenantId;
    }

    /**
     * persist user account status
     *
     * @param userId user id
     * @param tenantId  tenant id
     * @param status whether account is lock or unlock
     * @throws IdentityMgtException whether account is lock or unlock
     */
    public static void persistAccountStatus(String userId, int tenantId, String status) throws IdentityMgtException {

        String accountStatus;

        if(UserCoreConstants.USER_LOCKED.equals(status)){
            accountStatus = UserCoreConstants.USER_LOCKED;
        } else {
            accountStatus = UserCoreConstants.USER_UNLOCKED;
        }

        try {
            ClaimsMgtUtil.setClaimInUserStoreManager(userId, tenantId,
                                UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS, accountStatus);
        } catch (IdentityMgtException e) {
            String mgs = "Error while persisting account status for user : " + userId;
            log.error(mgs, e);
            throw new IdentityMgtException(mgs, e);
        }
    }

    /**
     * persist user account status
     *
     * @param userId user id
     * @param status whether account is lock or unlock
     * @throws IdentityMgtException if fails
     */
    public static void persistAccountStatus(String userId, String status) throws IdentityMgtException {

        int tenantId = 0;
        String accountStatus;

        try {
            String domainName = UserCoreUtil.
                            getTenantDomain(IdentityMgtServiceComponent.getRealmService(), userId);
            tenantId = getTenantId(domainName);
        } catch (org.wso2.carbon.user.core.UserStoreException ignore) {
            log.warn("Tenant Domain can not be retrieved. Assume as super tenant domain");
        }

        userId = UserCoreUtil.getTenantLessUsername(userId);
        if(UserCoreConstants.USER_LOCKED.equals(status)){
            accountStatus = UserCoreConstants.USER_LOCKED;
        } else {
            accountStatus = UserCoreConstants.USER_UNLOCKED;
        }

        try {
            ClaimsMgtUtil.setClaimInUserStoreManager(userId, tenantId,
                                UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS, accountStatus);
        } catch (IdentityMgtException e) {
            String mgs = "Error while persisting account status for user : " + userId;
            log.error(mgs, e);
            throw new IdentityMgtException(mgs, e);
        }
    }

    /**
     * Confirm that confirmation key has been sent to the same user.
     *
     * @param confirmationKey confirmation key from the user
     * @return verification result as a bean
     */
    public static VerificationBean verifyConfirmationKey(String confirmationKey){

        VerificationBean verificationBean = new VerificationBean();

        boolean success = false;
        Registry registry = null;
        try {

            registry = IdentityMgtServiceComponent.getRegistryService().
                                getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);

            registry.beginTransaction();

            String secretKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA +
                    RegistryConstants.PATH_SEPARATOR + confirmationKey;
            if (registry.resourceExists(secretKeyPath)) {
                Resource resource = registry.get(secretKeyPath);
                Properties props = resource.getProperties();
                for (Object o : props.keySet()) {
                    String key = (String) o;
                    if (key.equals(IdentityMgtConstants.REDIRECT_PATH)) {
                        verificationBean.setRedirectPath(resource.getProperty(key));
                    } else if (key.equals(IdentityMgtConstants.USER_NAME)) {
                        verificationBean.setUserId(resource.getProperty(key));
                    } else if (key.equals(IdentityMgtConstants.SECRET_KEY)) {
                        verificationBean.setKey(resource.getProperty(key));
                    }
                }
                registry.delete(resource.getPath());
                success = true;
            } else {
                verificationBean.setError("Invalid confirmation key");
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            verificationBean.setError("Unexpected error has occurred");
        } finally {
            if(registry != null){
                try{
                    if (success) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }
        verificationBean.setVerified(success);
        return verificationBean;
    }

    /**
     * Verifies user id with underline user store
     *
     * @param userMgtBean  bean class that contains user and tenant Information
     * @return true/false whether user is verified or not. If user is a tenant
     *         user then always return false
     */
    public static boolean verifyUserForRecovery(UserMgtBean userMgtBean) {

        String userId = userMgtBean.getUserId();

        try {
            int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());

            UserStoreManager userStoreManager = IdentityMgtServiceComponent.getRealmService().
                                                getTenantUserRealm(tenantId).getUserStoreManager();
            TenantManager tenantManager = IdentityMgtServiceComponent.getRealmService().
                                                                                getTenantManager();

            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                return userStoreManager.isExistingUser(userId);
            } else if (tenantId > 0) {
                if(userStoreManager.isExistingUser(userId)){
                    return userId.equals(tenantManager.getTenant(tenantId).getAdminName());
                } else {
                    // tenant users are not allowed to recover their account. 
                    log.info("Tenant user from tenant domain : " + userMgtBean.getTenantDomain()
                                                            + " is trying to verify his user account");
                    return false;
                }
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
        } catch (IdentityMgtException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * verify user evidences
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return verification results as been
     * @throws IdentityMgtException if any error occurs
     */
    public static String verifyUserEvidences(UserMgtBean userMgtBean) throws IdentityMgtException {

        String domainName = userMgtBean.getTenantDomain();
        int tenantId = Utils.getTenantId(domainName);

        UserEvidenceDTO[] evidenceDTOs = userMgtBean.getUserEvidenceDTOs();

        if(evidenceDTOs == null || evidenceDTOs.length < 1){
            log.error("no evidence provided by user for verification process");
            return null;
        }

        String userName = null;
        try {
            for(UserEvidenceDTO evidenceDTO : evidenceDTOs){
                if(evidenceDTO.getClaimUri() != null && evidenceDTO.getClaimValue() != null){
                    String[] userList = ClaimsMgtUtil.getUserList(tenantId,
                                           evidenceDTO.getClaimUri(), evidenceDTO.getClaimValue());
                    if(userList != null && userList.length > 0){
                        if(userList.length == 1){
                            if(userName == null){
                                userName = userList[0];
                            } else if(!userName.equals(userList[0])) {
                                return null;
                            } else {
                                userName = userList[0];
                            }
                        } else {
                            String msg = "More than one user is associated with the given claim values";
                            log.error(msg);
                            throw new Exception(msg);
                        }
                    } else {
                        if(log.isDebugEnabled()){
                            log.debug("No associated user is found for given claim values");                            
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error while retrieving user list for given claim values", e);
        }

        return userName;
    }

    public static String[] getChallengeUris(){
        //TODO
        return new String[] {IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI01,
                                            IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI02};
    }

    public static void lockUserAccount(String userName, int tenantId){

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, userName);
        IdentityCacheEntry cacheEntry = new IdentityCacheEntry(-10);
        LoginAttemptCache.getCacheInstance().addToCache(cacheKey, cacheEntry);
    }
    
    public static void unlockUserAccount(String userName, int tenantId){

        LoginAttemptCache.getCacheInstance().clearCacheEntry(userName, tenantId);
    }
}
