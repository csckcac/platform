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
package org.wso2.carbon.admin.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.beans.AdminMgtInfoBean;
import org.wso2.carbon.admin.mgt.beans.ConfirmationBean;
import org.wso2.carbon.admin.mgt.internal.util.PasswordUtil;
import org.wso2.carbon.admin.mgt.util.AdminMgtUtil;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;

/**
 * The service that is responsible for the password reset functionality of carbon.
 */
public class AdminManagementService {
    private static final Log log = LogFactory.getLog(AdminManagementService.class);

    /**
     *  Confirms that the link shared in the email is used only once in password reset.
     * @param secretKey - the key given in the password reset email.
     * @return ConfirmationBean, that has the data and redirect path.
     * @throws Exception, if the key provided in the link is expired, already clicked, or invalid.
     */
    public ConfirmationBean confirmUser(String secretKey) throws Exception {
        return AdminMgtUtil.confirmUser(secretKey);
    }

    /**
     * Initiating the password reset, as of the user request.
     *
     * @param adminInfoBean   AdminMgtInfobean
     * @param captchaInfoBean captcha info bean
     * @return true if successful
     * @throws Exception, if exception occurred in validating the domain.
     */
    public boolean resetPassword(    //initiatePasswordReset
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean) throws Exception {

        //processes the captchaInfoBean
        CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);

        // validate the domain
        String tenantDomain = adminInfoBean.getTenantDomain();
        if (!(tenantDomain.trim().equals(""))) {
            try {
                AdminMgtUtil.checkIsDomainValid(adminInfoBean.getTenantDomain());
            } catch (Exception e) {
                String msg = "Attempt to validate the given domain for the password reset, failed.";
                log.error(msg, e);
                // Password Reset Failed. Not passing the error details to client.
                return false;
            }
        }
        return PasswordUtil.initiateResetPassword(adminInfoBean);
    }

    /**
     * Update the password with the new password provided by the user
     *
     * @param adminInfoBean   userInfo
     * @param captchaInfoBean captcha
     * @param confirmationKey  key that confirms the password reset
     * @return true, if password is changed successfully. Final call in password reset.
     * @throws Exception, if password reset failed, due to captcha validation or the wrong attempt
     * of password reset.
     */
    public boolean updateAdminPasswordWithUserInput(
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean,
            String confirmationKey) throws Exception {
        
        String domain = adminInfoBean.getTenantDomain();                  
        String adminName = adminInfoBean.getAdmin();
        String userName = AdminMgtUtil.getUserNameWithDomain(adminName, domain);
        
        boolean isValidRequest = PasswordUtil.proceedUpdateCredentials(domain, adminName,
                confirmationKey);
        boolean isPasswordUpdated = false;
        
        if (isValidRequest) {
            //processes the captchaInfoBean
            CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);
            // change the password with the user input password
            if (log.isDebugEnabled()) {
                log.debug("Calling the password update method for the user: " + userName);
            }
            isPasswordUpdated = PasswordUtil.updateTenantPassword(adminInfoBean);
        }
        log.info("Password reset status of user " + userName + " is: " + isPasswordUpdated);
        AdminMgtUtil.cleanupResources(adminName, domain);
        return isPasswordUpdated;
    }

    /**
     * Generates a random Captcha
     *
     * @return captchaInfoBean
     * @throws Exception, if exception in cleaning old captchas or generating new captcha image.
     */
    public CaptchaInfoBean generateRandomCaptcha() throws Exception {
        // we will clean the old captchas asynchronously
        CaptchaUtil.cleanOldCaptchas();
        return CaptchaUtil.generateCaptchaImage();
    }
}