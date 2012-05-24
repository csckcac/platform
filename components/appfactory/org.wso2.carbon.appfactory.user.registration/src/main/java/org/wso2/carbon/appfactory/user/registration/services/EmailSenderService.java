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
package org.wso2.carbon.appfactory.user.registration.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.user.registration.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.email.sender.api.EmailSender;
import org.wso2.carbon.email.sender.api.EmailSenderConfiguration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Email sending service for both sending generic emails and activation email
 */
public class EmailSenderService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EmailSenderService.class);
    public static final String CONFIRMATION_EMAIL_CONFIG = "confirmation-email-config.xml";

    public boolean sendActivationEmail(String userName, String firstName, String activationKey,
                                       String email)
            throws UserRegistrationException {
        EmailVerifcationSubscriber emailVerifier = Util.getEmailVerificationService();
        Map<String, String> dataToStore = new HashMap<String, String>();
        dataToStore.put("first-name",
                        firstName);
        dataToStore.put("email", email);
        dataToStore.put("admin", userName);
        dataToStore.put("confirmationKey", activationKey);

        try {
            emailVerifier.requestUserVerification(dataToStore,
                                                  loadEmailVerificationConfiguration(
                                                          EmailSenderService.CONFIRMATION_EMAIL_CONFIG));
        } catch (Exception e) {
            String msg = "Activation email sending is failed for  " + userName;
            log.error(msg, e);
            throw new UserRegistrationException(msg, e);
        }
        return true;

    }

    public boolean sendMail(String userName, String firstName, String email, String applicationName,
                            String applicationKey, String config) throws UserRegistrationException {
        EmailSender sender = new EmailSender(loadEmailSenderConfiguration(config));
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("userName", userName);
        userParams.put("firstName", firstName);
        userParams.put("applicationName", applicationName);
        userParams.put("applicationKey", applicationKey);
        try {
            sender.sendEmail(email, userParams);
        } catch (Exception e) {
            String msg = "Email sending is failed for  " + email;
            log.error(msg, e);
            throw new UserRegistrationException(msg, e);

        }
        return true;

    }

    private EmailVerifierConfig loadEmailVerificationConfiguration(String configFile) {
        String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "email" +
                                File.separator + configFile;
        return org.wso2.carbon.email.verification.util.Util.loadeMailVerificationConfig(configFilePath);
    }

    private EmailSenderConfiguration loadEmailSenderConfiguration(String configFile) {
        String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "email" +
                                File.separator + configFile;
        return EmailSenderConfiguration.loadEmailSenderConfiguration(configFilePath);
    }
}
