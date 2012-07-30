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

package org.wso2.carbon.identity.oauth2.authz;

import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.handlers.AuthorizationHandler;
import org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.authz.handlers.TokenResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class AuthorizationHandlerManager {

    private static Log log = LogFactory.getLog(AuthorizationHandlerManager.class);

    private static AuthorizationHandlerManager instance;

    private Map<String, AuthorizationHandler> authzHandlers = new Hashtable<String, AuthorizationHandler>();
    private List<String> supportedRespTypes;

    public static AuthorizationHandlerManager getInstance() throws IdentityOAuth2Exception {

        CarbonUtils.checkSecurity();

        if(instance == null){
            synchronized (AuthorizationHandlerManager.class){
                if(instance == null){
                    instance = new AuthorizationHandlerManager();
                }
            }
        }
        return instance;
    }

    private AuthorizationHandlerManager()
            throws IdentityOAuth2Exception {
        supportedRespTypes = OAuthServerConfiguration.getInstance().getSupportedResponseTypes();
        authzHandlers.put(ResponseType.CODE.toString(), new CodeResponseTypeHandler());
        authzHandlers.put(ResponseType.TOKEN.toString(), new TokenResponseTypeHandler());
    }

    public OAuth2AuthorizeRespDTO handleAuthorization(OAuth2AuthorizeReqDTO authzReqDTO)
            throws IdentityOAuth2Exception {

        String responseType = authzReqDTO.getResponseType();
        OAuth2AuthorizeRespDTO authorizeRespDTO = new OAuth2AuthorizeRespDTO();

        if(!supportedRespTypes.contains(responseType)) {
            log.warn("Unsupported Response Type : " + responseType  +
                    " provided  for user : " + authzReqDTO.getUsername());
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.UNSUPPORTED_RESP_TYPE,
                    "Unsupported Response Type!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            return authorizeRespDTO;
        }

        AuthorizationHandler authzHandler = authzHandlers.get(responseType);
        OAuthAuthzReqMessageContext authzReqMsgCtx = new OAuthAuthzReqMessageContext(authzReqDTO);

        boolean authStatus = authzHandler.authenticateResourceOwner(authzReqMsgCtx);
        if (!authStatus) {
            log.warn("User Authentication failed for user : " + authzReqDTO.getUsername());
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "Authentication Failure, Invalid Credentials!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            return authorizeRespDTO;
        }

        boolean accessDelegationAuthzStatus = authzHandler.validateAccessDelegation(authzReqMsgCtx);
        if(!accessDelegationAuthzStatus){
            log.warn("User : " + authzReqDTO.getUsername() +
                    " doesn't have necessary rights to grant access to the resource(s) " +
                    OAuth2Util.buildScopeString(authzReqDTO.getScopes()));
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "Authorization Failure!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            authorizeRespDTO.setAuthenticated(true);
            return authorizeRespDTO;
        }

        boolean scopeValidationStatus = authzHandler.validateScope(authzReqMsgCtx);
        if(!scopeValidationStatus){
            log.warn("Scope validation failed for user : " + authzReqDTO.getUsername() +
                    ", for the scope : " +
                    OAuth2Util.buildScopeString(authzReqDTO.getScopes()));
            handleErrorRequest(authorizeRespDTO, OAuth2ErrorCodes.INVALID_SCOPE,
                    "Invalid Scope!");
            authorizeRespDTO.setCallbackURI(authzReqDTO.getCallbackUrl());
            authorizeRespDTO.setAuthenticated(true);
            return authorizeRespDTO;
        }

        authorizeRespDTO = authzHandler.issue(authzReqMsgCtx);
        return authorizeRespDTO;
    }

    private void handleErrorRequest(OAuth2AuthorizeRespDTO respDTO, String errorCode,
                                    String errorMsg) {
        respDTO.setAuthorized(false);
        respDTO.setErrorCode(errorCode);
        respDTO.setErrorMsg(errorMsg);
    }
}
