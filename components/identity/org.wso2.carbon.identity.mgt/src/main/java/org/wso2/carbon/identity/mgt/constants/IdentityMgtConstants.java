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
package org.wso2.carbon.identity.mgt.constants;

/**
 *  Identity management related constants
 */
public class IdentityMgtConstants {

    public static final String IDENTITY_MANAGEMENT_PATH =
            "/repository/components/org.wso2.carbon.identity.mgt";

    public static final String IDENTITY_MANAGEMENT_KEYS = IDENTITY_MANAGEMENT_PATH + "/keys";

    public static final String IDENTITY_MANAGEMENT_DATA = IDENTITY_MANAGEMENT_PATH + "/data";

    public static final String IDENTITY_MANAGEMENT_QUESTIONS = IDENTITY_MANAGEMENT_PATH + "/questionCollection";

    public static final String IDENTITY_MANAGEMENT_CHALLENGES = IDENTITY_MANAGEMENT_PATH + "/challenges";

    public static final String CONFIRMATION_KEY_NOT_MACHING = "The credential update failed. Secret key is not matching.";

//    public static final String ILLEGAL_CHARACTERS_FOR_TENANT_DOMAIN =
//            ".*[^a-zA-Z0-9\\._\\-].*";

    public static final String EMAIL_CONF_DIRECTORY = "email";

    public static final String EMAIL_ADMIN_CONF_FILE = "email-admin-config.xml";

    public static final String DEFAULT_CHALLENGE_QUESTION_URI01 =
                                                    "http://wso2.org/claims/challengeQuestion1";

    public static final String DEFAULT_CHALLENGE_ANSWER_URI01 =
                                                        "http://wso2.org/claims/challengeAnswer1";

    public static final String DEFAULT_CHALLENGE_QUESTION_URI02 =
                                                    "http://wso2.org/claims/challengeQuestion2";

    public static final String DEFAULT_CHALLENGE_ANSWER_URI02 =
                                                        "http://wso2.org/claims/challengeAnswer2";
    // TODO remove this
    public static final String[] SECRET_QUESTIONS_SET01 = new String[]{"City where you were born ?" ,
                        "Father's middle name ?", "Favorite food ?", "Favorite vacation location ?"};

    // TODO remove this
    public static final String[] SECRET_QUESTIONS_SET02 = new String[]{"Model of your first car ?",
        "Name of the hospital where you were born ?", "Name of your first pet ?", "Favorite sport ?"};
    
    public static  final String EMAIL_ADDRESS = "email";

    public static  final String FIRST_NAME = "firstName";

    public static  final String USER_NAME = "admin";

    public static  final String TENANT_DOMAIN = "tenantDomain";

//    public static  final String CONFIRMATION_KEY = "confirmationKey";

    public static  final String SECRET_KEY = "secretKey";

    public static  final String USER_KEY = "userKey";

    public static  final String VERIFIED_CHALLENGES = "verifiedChallenges";

    public static  final String TEMPORARY_PASSWORD = "temporaryPassword";

    public static  final String EMAIL_CONFIG_TYPE = "emailConfigType";

    public static  final String REDIRECT_PATH = "redirectPath";

//    public static  final String EMAIL_TYPE_PASSWORD_RESET = "sendPasswordResetLink";
//
//    public static  final String EMAIL_TYPE_ACCOUNT_CONFORM = "sendConformationLink";
//
//    public static  final String EMAIL_TYPE_TEMPORARY_PASSWORD = "sendTemporaryPassword";
//
//    public static  final String EMAIL_TYPE_ACCOUNT_ID = "sendAccountId";

    public static  final String RECOVERY_TYPE_PASSWORD_RESET = "passwordReset";

    public static  final String RECOVERY_TYPE_ACCOUNT_ID = "accountId";

    public static  final String RECOVERY_TYPE_TEMPORARY_PASSWORD = "temporaryPassword";

    public static  final String RECOVERY_TYPE_ACCOUNT_CONFORM= "accountConformation";
    
    public static  final String EMAIL_MANAGE_INTERNALLY = "emailSendingInternallyManaged";

    public static final String MAX_FAILED_ATTEMPT = "maxFailedLoginAttempt";

    public static final String CAPTCHA_VERIFICATION_INTERNALLY = "captchaVerificationInternallyManaged";

    public static final String TEMPORARY_PASSWORD_ARE_ALLOWED= "allowTemporaryPassword";

    public static final String DEFAULT_PASSWORD = "defaultPassword";

    public static final int DEFAULT_MAX_FAILED_LOGIN_ATTEMPT = 100;    
}
