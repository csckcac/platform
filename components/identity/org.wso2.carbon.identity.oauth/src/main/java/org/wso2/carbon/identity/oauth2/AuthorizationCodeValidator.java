/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;

/**
 * Implements the AuthorizationGrantValidator for the Authorization Code type.
 */
public class AuthorizationCodeValidator extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(AuthorizationCodeValidator.class);

    public AuthorizationCodeValidator(OAuth2AccessTokenReqDTO reqDTO) {
        super(reqDTO);
    }

    @Override
    public boolean validate() throws IdentityException {
        String authorizationCode = oAuth2AccessTokenReqDTO.getAuthorizationCode();
        String[] authzData = tokenMgtDAO.validateAuthorizationCode(
                                                            oAuth2AccessTokenReqDTO.getClientId(),
                                                            authorizationCode);

        if (authzData[0] == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid access token request with " +
                        "Client Id : " + oAuth2AccessTokenReqDTO.getClientId() +
                        " , Authorization Code : " + oAuth2AccessTokenReqDTO.getAuthorizationCode());
            }
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Found an Authorization Code, " +
                    "Client : " + oAuth2AccessTokenReqDTO.getClientId() +
                    " , Authorization Code : " + oAuth2AccessTokenReqDTO.getAuthorizationCode() +
                    ", authorized user : " + authzData[0] +
                    ", scope : " + authzData[1]);
        }

        authorizedUser = authzData[0];
        scope = authzData[1];
        return true;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue() throws IdentityException {
        OAuth2AccessTokenRespDTO tokenRespDTO = super.issue();
        // remove the authz code
        tokenMgtDAO.cleanUpAuthzCode(oAuth2AccessTokenReqDTO.getAuthorizationCode());

        if (log.isDebugEnabled()) {
            log.debug("Authorization Code clean up completed for : " +
                    oAuth2AccessTokenReqDTO.getAuthorizationCode());
        }

        return tokenRespDTO;
    }
}
