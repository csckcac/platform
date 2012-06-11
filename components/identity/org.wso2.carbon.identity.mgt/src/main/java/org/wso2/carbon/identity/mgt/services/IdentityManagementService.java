/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.AccountRecoveryConfig;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.IdentityMgtException;
import org.wso2.carbon.identity.mgt.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.*;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.PasswordUtil;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;


/**
 * IdentityManagementService Service Class. This exposes identity management APIs for users and user
 * administrators.
 */
public class IdentityManagementService {
    
    private static final Log log = LogFactory.getLog(IdentityManagementService.class);

    /**
     * verify given tenant domain and given user id of the user w.r.t the underline user store.
     *
     * @param userMgtBean  bean class that contains user and tenant Information
     * @param captchaInfoBean  bean class that contains captcha information
     * @return user key; secret for sub-sequence communication. If null, user and domain not verified.
     */
    public VerificationBean verifyUser(UserMgtBean userMgtBean, CaptchaInfoBean captchaInfoBean){

        int tenantId;
        VerificationBean bean = new VerificationBean();

        AccountRecoveryConfig config = IdentityMgtServiceComponent.
                                                getRecoveryProcessor().getRecoveryConfig();

        if(config.isCaptchaVerificationInternallyManaged()){
            try {
                CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                bean.setError("Captcha validation is failed");
                bean.setVerified(false);
                return bean;
            }
        }

        try {
            Utils.processUserId(userMgtBean);
            tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        } catch (IdentityMgtException e) {
            log.error(e.getMessage(), e);
            bean.setError("Unexpected error has occurred");
            bean.setVerified(false);
            return bean;
        }

        boolean verification = Utils.verifyUserForRecovery(userMgtBean);

        if(verification){
            String userKey;
            try{
                UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                        getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                        RegistryConstants.PATH_SEPARATOR +  tenantId +
                        RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();

                // delete any existing resource for this users.
                if (registry.resourceExists(identityKeyMgtPath)) {
                    registry.delete(identityKeyMgtPath);
                }
                Utils.clearVerifiedChallenges(userMgtBean);

                userKey = UUID.randomUUID().toString();
                Resource resource = registry.newResource();
                resource.addProperty(IdentityMgtConstants.USER_KEY, userKey);
                resource.setVersionableChange(false);
                registry.put(identityKeyMgtPath, resource);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                bean.setError("Unexpected error has occurred");
                bean.setVerified(false);
                return bean;
            }

            bean.setVerified(true);
            bean.setKey(userKey);
            return bean;
        } else {
            log.error("User verification failed");
            bean.setError("User verification failed");
            bean.setVerified(false);
            return bean;
        }
    }

    /**
     * process password recovery for given user
     *
     * @param userMgtBean userMgtBean bean class that contains user and tenant Information
     * @return recovery process success or not
     * @throws IdentityMgtException if fails
     */
    public boolean processPasswordRecovery(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);

        if(!Utils.verifyUserId(userMgtBean)){
            log.warn("Invalid user is trying to recover the password  : " + userMgtBean.getUserId());
            return false;
        }

        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_PASSWORD_RESET);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        RecoveryDataDTO dto = IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
        return dto.isEmailSent();
    }

    /**
     * process account confirmation for given user
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return confirmation process success or not
     * @throws IdentityMgtException if fails
     */
    public boolean processAccountConfirmation(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        RecoveryDataDTO dto = IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
        return dto.isEmailSent();
    }

    /**
     * process temporary password for given user
     *
     * @param userMgtBean userMgtBean bean class that contains user and tenant Information
     * @return processing temporary password success or not
     * @throws IdentityMgtException if fails
     */
    public boolean processTemporaryPassword(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);

        if(!Utils.verifyUserId(userMgtBean)){
            log.warn("Invalid user is trying to recover the password");
            return false;
        }
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        RecoveryDataDTO dto = IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);

        return dto.isEmailSent();
    }


    /**
     *  process account recovery for given user
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return  processing account recovery success or not
     * @throws  IdentityMgtException if fails
     */
    public boolean processAccountRecovery(UserMgtBean userMgtBean) throws IdentityMgtException {

        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_ID);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        String userName = Utils.verifyUserEvidences(userMgtBean);
        if(userName != null){
            userMgtBean.setUserId(userName);
            RecoveryDataDTO dto = IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
            return dto.isEmailSent();
        }

        return false;
    }

    /**
     * verifies user account w.r.t confirmation key 
     * 
     * @param confirmationKey key
     * @return verified result as a bean
     */
    public static VerificationBean confirmUserAccount(String confirmationKey) {
        return Utils.verifyConfirmationKey(confirmationKey);
    }


    /**
     * proceed updating credentials of user
     *
     * @param userMgtBean userMgtBean bean class that contains user and tenant Information
     * @param captchaInfoBean  bean class that contains captcha information
     * @return True, if successful in verifying and hence updating the credentials.
     */
    public boolean updateCredential(UserMgtBean userMgtBean, CaptchaInfoBean captchaInfoBean) {

        boolean success = false;
        UserRegistry registry = null;
        Resource resource;
        
        AccountRecoveryConfig config = IdentityMgtServiceComponent.
                                                getRecoveryProcessor().getRecoveryConfig();
        try{
            if(config.isCaptchaVerificationInternallyManaged()){
                CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);
            }

            Utils.processUserId(userMgtBean);

            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);            
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String actualSecretKey = null;
                if(resource != null){
                    actualSecretKey = resource.getProperty(IdentityMgtConstants.SECRET_KEY);
                }
                if ((actualSecretKey != null) && (actualSecretKey.equals(userMgtBean.getSecretKey()))) {
                    success = PasswordUtil.updatePassword(userMgtBean);
                    registry.delete(resource.getPath());
                        if(userMgtBean.getTenantDomain() == null){
                            log.info("Credential is updated for user : " + userMgtBean.getUserId());
                        } else {
                            log.info("Credential is updated for tenant domain : " +
                                                                    userMgtBean.getTenantDomain());
                        }
                } else {
                    log.warn("Invalid user tried to update credential");
                }
            } else {
                log.warn("Invalid user tried to update credential");
            }

        } catch (Exception e) {
            log.error("Error while updating credential" , e);
        } finally{
            if(registry != null){
                try {
                    if(success){
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (Exception e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }
        return success;
    }


    /**
     * unlock user's account
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return  True, if successful in verifying and hence unlocking the account
     */
    public boolean unlockUserAccount(UserMgtBean userMgtBean){

        UserRegistry registry = null;
        boolean success = false;

        try{
            Resource resource;

            Utils.processUserId(userMgtBean);
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                    RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                    RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String actualSecretKey = null;
                if(resource != null){
                    actualSecretKey = resource.getProperty(IdentityMgtConstants.SECRET_KEY);
                }
                if ((actualSecretKey != null) && (actualSecretKey.equals(userMgtBean.getSecretKey()))) {
                    Utils.persistAccountStatus(userMgtBean.getUserId(),
                            Utils.getTenantId(userMgtBean.getTenantDomain()),
                            UserCoreConstants.USER_UNLOCKED);
                    registry.delete(resource.getPath());
                        if(userMgtBean.getTenantDomain() == null){
                            log.info("Account is unlocked for : " + userMgtBean.getUserId());
                        } else {
                            log.info("Account is unlocked for tenant domain : " +
                                                                    userMgtBean.getTenantDomain());
                        }
                } else {
                    log.warn("Invalid user tried to unlock account");
                }
            } else {
                log.warn("Invalid user tried to unlock account");
            }
        } catch (Exception e) {
            log.error("Error while unlocking account" , e);
        }finally {
            if(registry != null){
                try {
                    if(success){
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (Exception e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }
        return success;
    }

    /**
     * generates a random Captcha
     *
     * @return captchaInfoBean
     * @throws IdentityMgtException, if fails
     */
    public CaptchaInfoBean generateRandomCaptcha() throws IdentityMgtException {

        try {
            CaptchaUtil.cleanOldCaptchas();
            return CaptchaUtil.generateCaptchaImage();
        } catch (Exception e) {
            log.error("Error while generating captcha", e);
            throw new IdentityMgtException("Error while generating captcha", e);
        }

    }

    /**
     * get challenges of user
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return array of challenges  if null, return empty array
     * @throws IdentityMgtException  if fails
     */
    public UserChallengesDTO[] getChallengeQuestionsOfUser(UserMgtBean userMgtBean)
                                                                    throws IdentityMgtException {

        Utils.processUserId(userMgtBean);

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();

        return processor.getChallengeQuestionsOfUser(userMgtBean.getUserId(),
                                                Utils.getTenantId(userMgtBean.getTenantDomain()));
    }

    /**
     * get primary challenges of user
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return array of primary user challenges, if null, return empty array 
     * @throws IdentityMgtException if fails
     */
    public UserChallengesDTO[] getPrimaryQuestionsOfUser(UserMgtBean userMgtBean)
                                                                    throws IdentityMgtException {


        Utils.processUserId(userMgtBean);

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();

        return processor.getPrimaryChallengeQuestionsOfUser(userMgtBean.getUserId(),
                                                Utils.getTenantId(userMgtBean.getTenantDomain()));
    }


    /**
     * verify challenge questions
     *
     * @param userMgtBean    userMgtBean bean class that contains user and tenant Information
     * @return verification results as been
     * @throws IdentityMgtException if any error occurs
     */
    public VerificationBean verifyChallengeQuestion(UserMgtBean userMgtBean) throws IdentityMgtException {

        VerificationBean bean = new VerificationBean();
        bean.setVerified(false);

        UserChallengesDTO[] challengesDTOs = userMgtBean.getUserChallenges();

        if(challengesDTOs == null || challengesDTOs.length < 1){
            log.error("no challenges provided by user for verifications");
            bean.setError("no challenges provided by user for verifications");
            return bean;
        }

        Utils.processUserId(userMgtBean);
        
        if(!Utils.verifyUserId(userMgtBean)){
            log.warn("Invalid user is trying to verify user challenges");
            bean.setError("Invalid user is trying to verify user challenges");
            return bean;
        }

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());

        boolean verification = processor.verifyChallengeQuestion(userMgtBean.getUserId(), tenantId,
                                                                                    challengesDTOs);
        if(verification){
            int noOfVerifiedChallenges = Utils.getVerifiedChallenges(userMgtBean);
            int noOfChallenges = processor.getNoOfChallengeQuestions(userMgtBean.getUserId(), tenantId);
            String propertyId;
            if(noOfVerifiedChallenges == noOfChallenges - 1){
                propertyId = IdentityMgtConstants.SECRET_KEY;
            } else {
                propertyId = IdentityMgtConstants.USER_KEY;
            }

            try{
                UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                        getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                        RegistryConstants.PATH_SEPARATOR + Utils.getTenantId(userMgtBean.getTenantDomain()) +
                        RegistryConstants.PATH_SEPARATOR + userMgtBean.getUserId();

                if (registry.resourceExists(identityKeyMgtPath)) {
                    registry.delete(identityKeyMgtPath);
                }

                String key = UUID.randomUUID().toString();
                Resource resource = registry.newResource();
                resource.addProperty(propertyId, key);
                resource.setVersionableChange(false);
                registry.put(identityKeyMgtPath, resource);
                if(IdentityMgtConstants.USER_KEY.equals(propertyId)){
                    Utils.setVerifiedChallenges(userMgtBean);
                } else {
                    Utils.clearVerifiedChallenges(userMgtBean); 
                }
                bean.setVerified(true);
                bean.setKey(key);
            } catch (RegistryException e) {
                log.error("Unexpected error has occurred", e);
                bean.setError("Unexpected error has occurred");
                return bean;
            }

        }

        return bean;
    }

    /**
     * verify challenge questions    TODO
     *
     * @param userMgtBean    userMgtBean bean class that contains user and tenant Information
     * @return verification results as been
     * @throws IdentityMgtException if any error occurs
     */
    public boolean verifyPrimaryChallengeQuestion(UserMgtBean userMgtBean) throws IdentityMgtException {


        Utils.processUserId(userMgtBean);

        UserChallengesDTO[] challengesDTOs = userMgtBean.getUserChallenges();

        if(challengesDTOs == null || challengesDTOs.length < 1){
            log.error("no challenges provided by user for verifications");
            return false;
        }

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();

        return processor.verifyPrimaryChallengeQuestion(userMgtBean.getUserId(),
                Utils.getTenantId(userMgtBean.getTenantDomain()), challengesDTOs);
    }


    /**
     * get all promoted user challenges
     *
     * @return array of user challenges
     * @throws IdentityMgtException  if fails
     */
    public UserChallengesSetDTO[] getAllPromotedUserChallenge() throws IdentityMgtException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();
        List<UserChallengesSetDTO> challengeQuestionSetDTOs = new ArrayList<UserChallengesSetDTO>();
        List<ChallengeQuestionDTO> questionDTOs =  processor.getAllChallengeQuestions();
        Map<String, List<UserChallengesDTO>> listMap = new HashMap<String, List<UserChallengesDTO>>();
        for(ChallengeQuestionDTO dto : questionDTOs){

            List<UserChallengesDTO>  dtoList = listMap.get(dto.getQuestionSetId());
            if(dtoList == null){
                dtoList = new ArrayList<UserChallengesDTO>();
            }

            UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
            userChallengesDTO.setSetId(dto.getQuestionSetId());
            userChallengesDTO.setQuestion(dto.getQuestion());
            userChallengesDTO.setOrder(dto.getOrder());

            dtoList.add(userChallengesDTO);
            listMap.put(dto.getQuestionSetId(), dtoList);
        }

        for(Map.Entry<String, List<UserChallengesDTO>> listEntry : listMap.entrySet()){
            UserChallengesSetDTO dto = new UserChallengesSetDTO();
            dto.setId(listEntry.getKey());
            List<UserChallengesDTO>  dtoList  = listEntry.getValue();
            dto.setChallengesDTOs(dtoList.toArray(new UserChallengesDTO[dtoList.size()]));
            challengeQuestionSetDTOs.add(dto);
        }

        return challengeQuestionSetDTOs.toArray(new UserChallengesSetDTO[challengeQuestionSetDTOs.size()]);
    }

    /**
     * returns the recovery data for password recovery request.
     *
     * @param userMgtBean bean class that contains user and tenant Information
     * @return  recovery data
     * @throws IdentityMgtException if fails
     */
    public RecoveryDataDTO getPasswordRecoveryData(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_PASSWORD_RESET);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        return IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
    }

    /**
     * returns the recovery data for account confirmation request.
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return recovery data
     * @throws IdentityMgtException if fails
     */
    public RecoveryDataDTO getAccountConfirmationData(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        return IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
    }


    /**
     * returns the recovery data when temporary password is requested.
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return recovery data
     * @throws IdentityMgtException if fails
     */
    public RecoveryDataDTO getTemporaryPasswordData(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        return IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
    }

    /**
     * returns the recovery data for account recovery request.
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @return recovery data
     * @throws IdentityMgtException if fails
     */
    public RecoveryDataDTO getAccountRecoveryData(UserMgtBean userMgtBean) throws IdentityMgtException {

        Utils.processUserId(userMgtBean);
        userMgtBean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_ID);
        int tenantId = Utils.getTenantId(userMgtBean.getTenantDomain());
        String userName = Utils.verifyUserEvidences(userMgtBean);
        if(userName != null){
            userMgtBean.setUserId(userName);
            return IdentityMgtServiceComponent.getRecoveryProcessor().
                                                processRecoveryUsingEmail(userMgtBean, tenantId);
        } else {
            RecoveryDataDTO emailDataDTO = new RecoveryDataDTO();
            emailDataDTO.setEmailSent(false);
            return emailDataDTO;
        }
    }

    /**
     * get all challenge questions
     *
     * @return array of questions
     * @throws IdentityMgtException if fails
     */
    public ChallengeQuestionDTO[] getAllChallengeQuestions() throws IdentityMgtException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();
        List<ChallengeQuestionDTO> questionDTOs = processor.getAllChallengeQuestions();
        return questionDTOs.toArray(new ChallengeQuestionDTO[questionDTOs.size()]);

    }

    /**
     * set all challenge questions
     *
     * @param challengeQuestionDTOs  array of questions
     * @throws IdentityMgtException if fails
     */
    public void setChallengeQuestions(ChallengeQuestionDTO[] challengeQuestionDTOs)
                                                                    throws IdentityMgtException {

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                        getRecoveryProcessor().getQuestionProcessor();
        processor.setChallengeQuestions(challengeQuestionDTOs);
    }

    /**
     * set challenges of user
     *
     * @param userMgtBean  userMgtBean bean class that contains user and tenant Information
     * @throws IdentityMgtException  if fails
     */
    public void setChallengeQuestionsOfUser(UserMgtBean userMgtBean) throws IdentityMgtException {


        Utils.processUserId(userMgtBean);

        UserChallengesDTO[] challengesDTOs = userMgtBean.getUserChallenges();

        if(challengesDTOs == null || challengesDTOs.length < 1){
            log.error("no challenges provided by user");
            throw new IdentityMgtException("no challenges provided by user");
        }

        ChallengeQuestionProcessor processor = IdentityMgtServiceComponent.
                                                    getRecoveryProcessor().getQuestionProcessor();

        processor.setChallengesOfUser(userMgtBean.getUserId(), Utils.
                                    getTenantId(userMgtBean.getTenantDomain()), challengesDTOs);
    }
}