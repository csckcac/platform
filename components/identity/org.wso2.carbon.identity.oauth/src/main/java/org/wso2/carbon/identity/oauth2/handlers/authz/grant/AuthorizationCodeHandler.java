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

package org.wso2.carbon.identity.oauth2.handlers.authz.grant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.handlers.authz.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

/**
 * Implements the AuthorizationGrantHandler for the Authorization Code type.
 */
public class AuthorizationCodeHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(AuthorizationCodeHandler.class);

    public AuthorizationCodeHandler() throws IdentityOAuth2Exception {
        super();
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
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

        tokReqMsgCtx.setAuthorizedUser(authzData[0]);
        tokReqMsgCtx.setScope(OAuth2Util.buildScopeArray(authzData[1]));
        return true;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = super.issue(tokReqMsgCtx);
        // remove the callback code
        tokenMgtDAO.cleanUpAuthzCode(
                tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());

        if (log.isDebugEnabled()) {
            log.debug("Authorization Code clean up completed for request from the Client, " +
                    "Client Id: " + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        }

        return tokenRespDTO;
    }

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        // authorization is handled when the authorization code was issued.
        return true;
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        // scope was validated when the authorization code was issued.
        return true;
    }
}
