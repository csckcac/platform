/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.sso.saml.ui;

public class SAMLSSOProviderConstants {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ISSUER = "issuer";
    public static final String ASSRTN_CONSUMER_URL = "assertnConsumerURL";
    public static final String REQ_ID = "id";
    public static final String SUBJECT = "subject";
    public static final String RP_SESSION_ID = "relyingPartySessionId";
    public static final String ASSERTION_STR = "assertionString";

    public static final String RELAY_STATE = "RelayState";
    public static final String AUTH_REQ_SAML_ASSRTN = "SAMLRequest";
    public static final String SAML_RESP = "SAMLResponse";
    public static final String TARGET_ASSRTN_CONSUMER_URL = "targetedAssrtnConsumerURL";
    public static final String kEEP_SESSION_ALIVE = "keepSessionAlive";

    public static final String LOGOUT_RESP = "logoutResponse";

    public static final String STATUS = "status";
    public static final String STATUS_MSG = "statusMsg";

    public static final String SSO_TOKEN_ID = "ssoTokenId";
    public static final String FE_SESSION_KEY = "authSession";

    public static final String AUTH_FAILURE = "authFailure";
    public static final String AUTH_FAILURE_MSG = "authFailureMsg";

    public class AuthnModes{
        public static final String USERNAME_PASSWORD = "usernamePasswordBasedAuthn";
        public static final String OPENID = "openIDBasedAuthn";
    }
}
