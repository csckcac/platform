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
package org.wso2.carbon.identity.sso.saml;

public class SAMLSSOConstants {

    public static final String NAME_ID_POLICY_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    public static final String SUBJECT_CONFIRM_BEARER = "urn:oasis:names:tc:SAML:2.0:cm:bearer";

    public class StatusCodes {
        public static final String SUCCESS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success";    
        public static final String REQUESTOR_ERROR = "urn:oasis:names:tc:SAML:2.0:status:Requester";
        public static final String IDENTITY_PROVIDER_ERROR = "urn:oasis:names:tc:SAML:2.0:status:Responder";
        public static final String VERSION_MISMATCH = "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch";
        public static final String AUTHN_FAILURE = "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed";
    }

    public class SingleLogoutCodes{
        public static final String LOGOUT_USER = "urn:oasis:names:tc:SAML:2.0:logout:user";
        public static final String LOGOUT_ADMIN = "urn:oasis:names:tc:SAML:2.0:logout:admin";
    }

    public class AuthnModes{
        public static final String USERNAME_PASSWORD = "usernamePasswordBasedAuthn";
        public static final String OPENID = "openIDBasedAuthn";
    }
}

