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

import java.util.Map;

/**
 * encapsulates recovery config data
 */
public class AccountRecoveryConfig {

    private int noOfUserChallenges;

    private boolean emailSendingInternallyManaged;

    private boolean captchaVerificationInternallyManaged;

    private String temporaryPassword;

    private Map<String, String>  redirectionUrlMap;


    public int getNoOfUserChallenges() {
        return noOfUserChallenges;
    }

    public void setNoOfUserChallenges(int noOfUserChallenges) {
        this.noOfUserChallenges = noOfUserChallenges;
    }

    public Map<String, String> getRedirectionUrlMap() {
        return redirectionUrlMap;
    }

    public void setRedirectionUrlMap(Map<String, String> redirectionUrlMap) {
        this.redirectionUrlMap = redirectionUrlMap;
    }

    public boolean isEmailSendingInternallyManaged() {
        return emailSendingInternallyManaged;
    }

    public void setEmailSendingInternallyManaged(boolean emailSendingInternallyManaged) {
        this.emailSendingInternallyManaged = emailSendingInternallyManaged;
    }

    public boolean isCaptchaVerificationInternallyManaged() {
        return captchaVerificationInternallyManaged;
    }

    public void setCaptchaVerificationInternallyManaged(boolean captchaVerificationInternallyManaged) {
        this.captchaVerificationInternallyManaged = captchaVerificationInternallyManaged;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}
