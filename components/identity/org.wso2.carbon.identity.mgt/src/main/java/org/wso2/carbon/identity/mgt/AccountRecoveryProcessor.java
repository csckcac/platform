/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.RecoveryDataDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.ClaimsMgtUtil;
import org.wso2.carbon.identity.mgt.util.PasswordUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * recover processor 
 */
public class AccountRecoveryProcessor {

    private static final Log log = LogFactory.getLog(ChallengeQuestionProcessor.class);

    private EmailProcessor emailProcessor;

    private ChallengeQuestionProcessor questionProcessor;

    private AccountRecoveryConfig recoveryConfig;

    public AccountRecoveryProcessor() {
        
        emailProcessor = new EmailProcessor();
        questionProcessor = new ChallengeQuestionProcessor();
        recoveryConfig = new AccountRecoveryConfig();
        String emailSendingInternallyManaged = IdentityMgtServiceComponent.getRealmService().
            getBootstrapRealmConfiguration().getUserStoreProperty(IdentityMgtConstants.EMAIL_MANAGE_INTERNALLY);
        if("true".equals(emailSendingInternallyManaged)){
            recoveryConfig.setEmailSendingInternallyManaged(true);
            emailProcessor.setEmailSendingInternallyManaged(true);
        }
        String captchaVerificationInternallyManaged = IdentityMgtServiceComponent.getRealmService().
            getBootstrapRealmConfiguration().getUserStoreProperty(IdentityMgtConstants.CAPTCHA_VERIFICATION_INTERNALLY);
        if("true".equals(captchaVerificationInternallyManaged)){
            recoveryConfig.setCaptchaVerificationInternallyManaged(true);
        }
    }


    /**
     * Processing recovery
     *
     * @param userMgtBean  bean class that contains user and tenant Information
     * @param tenantId  tenant id
     * @return true if the reset request is processed successfully.
     * @throws IdentityMgtException  if fails
     */
    public RecoveryDataDTO processRecoveryUsingEmail(UserMgtBean userMgtBean, int tenantId)
                                                                        throws IdentityMgtException {

        String userId = userMgtBean.getUserId();
        String domainName = userMgtBean.getTenantDomain();
        String email;
        String secretKey;

        if(tenantId == 0){
            tenantId = Utils.getTenantId(domainName);
        }

        RecoveryDataDTO emailDataDTO = new RecoveryDataDTO();

        email = ClaimsMgtUtil.getEmailAddressForUser(userId, tenantId);

        if ((email == null) || (email.trim().length() < 0)) {
            emailDataDTO.setEmailSent(false);
            return emailDataDTO;
        }

        userMgtBean.setEmail(email);

        if(userMgtBean.getRecoveryType() != null){
            String recoveryType = userMgtBean.getRecoveryType().trim();

            if(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM.equals(recoveryType) ||
                    IdentityMgtConstants.RECOVERY_TYPE_PASSWORD_RESET.equals(recoveryType)){

                try {
                    secretKey = generateSecretKey(userId, tenantId);
                } catch (RegistryException e) {
                    log.error("Error while generating secret key", e);
                    emailDataDTO.setEmailSent(false);
                    return emailDataDTO;                    
                }
                userMgtBean.setSecretKey(secretKey);

            } else if(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD.equals(recoveryType)){

                String temporaryPassword = userMgtBean.getUserPassword();
                if(temporaryPassword == null || temporaryPassword.trim().length() < 1){
                    temporaryPassword = PasswordUtil.generateTemporaryPassword();
                    userMgtBean.setUserPassword(temporaryPassword);
                }
                PasswordUtil.updatePassword(userMgtBean);
            }
        }

        Map<String, String> dataToStore = populateDataMap(userMgtBean);
        return emailProcessor.processEmail(dataToStore);
    }

    /**
     * generates secret key and store it in the registry
     *
     * @param userName  user name
     * @param tenantId  tenant id
     * @return  secret key as String
     * @throws RegistryException if fails
     */
    private String generateSecretKey(String userName, int tenantId) throws RegistryException {
        
        String secretKey = UUIDGenerator.generateUUID();

        UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);

        Resource resource;
        String identityMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                RegistryConstants.PATH_SEPARATOR + tenantId + RegistryConstants.PATH_SEPARATOR  + userName;

        if (registry.resourceExists(identityMgtPath)) {
            registry.delete(identityMgtPath);
        }
        resource = registry.newResource();
        resource.setProperty(IdentityMgtConstants.SECRET_KEY, secretKey);
        registry.put(identityMgtPath, resource);
        return secretKey;
    }

    /**
     * populates data to writes to the registry
     *
     * @param userMgtBean  bean class that contains user and tenant Information
     * @return Map
     */
    private  Map<String, String> populateDataMap(UserMgtBean userMgtBean) {

        Map<String, String> dataToStore = new HashMap<String, String>();

        dataToStore.put(IdentityMgtConstants.EMAIL_ADDRESS, userMgtBean.getEmail());
        try {
            dataToStore.put(IdentityMgtConstants.FIRST_NAME, ClaimsMgtUtil.
                    getFirstName(userMgtBean.getUserId(), Utils.getTenantId(userMgtBean.getTenantDomain())));
        } catch (IdentityMgtException e) {
            log.warn("FirstName of the user can not be retrieved.", e);
            dataToStore.put(IdentityMgtConstants.FIRST_NAME, userMgtBean.getUserId());
        }
        dataToStore.put(IdentityMgtConstants.USER_NAME, userMgtBean.getUserId());
        dataToStore.put(IdentityMgtConstants.TENANT_DOMAIN, userMgtBean.getTenantDomain());
        dataToStore.put(IdentityMgtConstants.SECRET_KEY, userMgtBean.getSecretKey());
        dataToStore.put(IdentityMgtConstants.TEMPORARY_PASSWORD, userMgtBean.getUserPassword());
        dataToStore.put(IdentityMgtConstants.EMAIL_CONFIG_TYPE, userMgtBean.getRecoveryType());
        return dataToStore;
    }

    public EmailProcessor getEmailProcessor() {
        return emailProcessor;
    }

    public ChallengeQuestionProcessor getQuestionProcessor() {
        return questionProcessor;
    }

    public AccountRecoveryConfig getRecoveryConfig() {
        return recoveryConfig;
    }
}
