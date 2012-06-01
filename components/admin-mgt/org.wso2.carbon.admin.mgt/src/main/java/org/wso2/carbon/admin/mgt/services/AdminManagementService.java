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
import org.wso2.carbon.admin.mgt.exception.AdminManagementException;
import org.wso2.carbon.admin.mgt.internal.util.PasswordUtil;
import org.wso2.carbon.admin.mgt.util.AdminMgtUtil;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.constants.CaptchaMgtConstants;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;

/**
 * The service that is responsible for the password reset functionality of carbon.
 */
public class AdminManagementService {
    private static final Log log = LogFactory.getLog(AdminManagementService.class);

    /**
     * Initiating the password reset, as of the user request.
     *
     * @param adminInfoBean   AdminMgtInfobean
     * @param captchaInfoBean captcha info bean
     * @return true if successful
     * @throws AdminManagementException, if exception occurred in validating the domain.
     */
    public boolean initiatePasswordReset(
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean) throws
            AdminManagementException {

        //processes the captchaInfoBean
        try {
            CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);
        } catch (Exception e) {
            String msg = CaptchaMgtConstants.CAPTCHA_ERROR_MSG +
                    " Failure in verifying the provided captcha for initiate password reset. ";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }

        // validate the domain
        String tenantDomain = adminInfoBean.getTenantDomain();
        if (!(tenantDomain.trim().equals(""))) {
            try {
                AdminMgtUtil.checkIsDomainValid(adminInfoBean.getTenantDomain());
            } catch (Exception e) {
                String msg = "Attempt to check the validity of the given domain for the password " +
                             "reset, failed.";
                log.error(msg, e);
                // Password Reset Failed. Not passing the error details to client.
                return false;
            }
        }
        return PasswordUtil.initiatePasswordReset(adminInfoBean);
    }

    /**
     * Update the password with the new password provided by the user
     *
     * @param adminInfoBean   userInfo
     * @param captchaInfoBean captcha
     * @param confirmationKey key that confirms the password reset
     * @return true, if password is changed successfully. Final call in password reset.
     * @throws AdminManagementException, if password reset failed, due to captcha validation
     *                                   or the wrong attempt of password reset.
     */
    public boolean updatePasswordWithUserInput(
            AdminMgtInfoBean adminInfoBean, CaptchaInfoBean captchaInfoBean,
            String confirmationKey) throws AdminManagementException {

        String domain = adminInfoBean.getTenantDomain();
        String tenantLessUserName = adminInfoBean.getTenantLessUserName();
        String userName = AdminMgtUtil.getUserNameWithDomain(tenantLessUserName, domain);

        boolean isValidRequest = PasswordUtil.proceedUpdateCredentials(domain, tenantLessUserName,
                                                                       confirmationKey);
        boolean isPasswordUpdated = false;

        if (isValidRequest) {
            //processes the captchaInfoBean
            try {
                CaptchaUtil.processCaptchaInfoBean(captchaInfoBean);
            } catch (Exception e) {
                String msg = CaptchaMgtConstants.CAPTCHA_ERROR_MSG +
                        " Failure in verifying the provided captcha for updating the password";
                log.error(msg, e);
                throw new AdminManagementException(msg, e);
            }
            // change the password with the user input password
            if (log.isDebugEnabled()) {
                log.debug("Calling the password update method for the user: " + userName);
            }
            isPasswordUpdated = PasswordUtil.updateCredentials(adminInfoBean);
        }
        log.info("Password reset status of user " + userName + " is: " + isPasswordUpdated);
        AdminMgtUtil.cleanupResources(tenantLessUserName, domain);
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