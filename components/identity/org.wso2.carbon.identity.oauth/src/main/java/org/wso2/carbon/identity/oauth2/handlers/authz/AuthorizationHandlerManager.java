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

package org.wso2.carbon.identity.oauth2.handlers.authz;

import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

public class AuthorizationHandlerManager {

    private AuthorizationHandler authzHandler;
    private OAuth2AuthorizeReqDTO authzReqDTO;
    private static Log log = LogFactory.getLog(AuthorizationHandlerManager.class);

    public AuthorizationHandlerManager(OAuth2AuthorizeReqDTO authzReqDTO)
            throws IdentityOAuth2Exception {
        if (ResponseType.CODE.toString().equals(authzReqDTO.getResponseType())) {
            authzHandler = new CodeResponseTypeHandler(authzReqDTO);
        } else if (ResponseType.TOKEN.toString().equals(authzReqDTO.getResponseType())) {
            authzHandler = new TokenResponseTypeHandler(authzReqDTO);
        }
    }

    public OAuth2AuthorizeRespDTO handleAuthorization() throws IdentityOAuth2Exception {
        OAuth2AuthorizeRespDTO authorizeRespDTO = new OAuth2AuthorizeRespDTO();

        boolean authStatus = authzHandler.authenticateResourceOwner();
        if (!authStatus) {
            log.warn("User Authentication failed for user : " + authzReqDTO.getUsername());
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.ACCESS_DENIED,
                    "Authentication Failure, Invalid Credentials!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            return authorizeRespDTO;
        }

        boolean accessDelegationAuthzStatus = authzHandler.validateAccessDelegation();
        if(!accessDelegationAuthzStatus){
            log.warn("User : " +  authzReqDTO.getUsername() +
                    " doesn't have necessary rights to grant access to the resource(s) " +
                    OAuth2Util.buildScopeString(authzReqDTO.getScopes()));
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "Authorization Failure!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            return authorizeRespDTO;
        }

        boolean scopeValidationStatus = authzHandler.validateScope();
        if(!scopeValidationStatus){
            log.warn("Scope validation failed for user : " + authzReqDTO.getUsername() +
                    ", for the scope : " +
                    OAuth2Util.buildScopeString(authzReqDTO.getScopes()));
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.INVALID_SCOPE,
                    "Invalid Scope!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            return authorizeRespDTO;
        }

        authorizeRespDTO = authzHandler.issue();
        return authorizeRespDTO;
    }

    private void handleErrorRequest(OAuth2AuthorizeRespDTO respDTO, String errorCode,
                                    String errorMsg) {
        respDTO.setAuthorized(false);
        respDTO.setErrorCode(errorCode);
        respDTO.setErrorMsg(errorMsg);
    }
}
