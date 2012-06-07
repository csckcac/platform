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
package org.wso2.carbon.identity.mgt.beans;

import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserEvidenceDTO;
import java.util.Arrays;

/**
 * Bean that encapsulates user and tenant info
 */
public class UserMgtBean {

    /**
     * user identifier according to the user store
     */
    private String userId;

    /**
     * user's password
     */
    private String userPassword;

    /**
     * tenant domain of the user
     */
    private String tenantDomain;

    /**
     * email address of the user
     */
    private String email;

    /**
     * secret key which is assign to user
     */
    private String secretKey;

    /**
     * user key that is used to identify the user, other than the user id
     */
    private String userKey;

    /**
     * recovery type
     */
    private String recoveryType;

    /**
     * user challenges that must be answered by user
     */
    private UserChallengesDTO[] userChallenges;

    /**
     * evidences about user to identify him uniquely
     */
    private UserEvidenceDTO[] userEvidenceDTOs;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public UserChallengesDTO[] getUserChallenges() {
        return Arrays.copyOf(userChallenges, userChallenges.length);
    }

    public void setUserChallenges(UserChallengesDTO[] userChallenges) {
        this.userChallenges = Arrays.copyOf(userChallenges, userChallenges.length);
    }

    public String getRecoveryType() {
        return recoveryType;
    }

    public void setRecoveryType(String recoveryType) {
        this.recoveryType = recoveryType;
    }

    public UserEvidenceDTO[] getUserEvidenceDTOs() {
        return Arrays.copyOf(userEvidenceDTOs, userEvidenceDTOs.length);
    }

    public void setUserEvidenceDTOs(UserEvidenceDTO[] userEvidenceDTOs) {
        this.userEvidenceDTOs = Arrays.copyOf(userEvidenceDTOs, userEvidenceDTOs.length);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
