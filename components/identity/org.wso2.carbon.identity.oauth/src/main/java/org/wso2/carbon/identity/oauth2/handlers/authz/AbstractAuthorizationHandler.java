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

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth.callback.OAuthCallbackManager;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

public abstract class AbstractAuthorizationHandler implements AuthorizationHandler {
    private OAuthCallbackManager callbackManager;
    protected OAuthIssuerImpl oauthIssuerImpl;
    protected TokenMgtDAO tokenMgtDAO;

    public AbstractAuthorizationHandler()
            throws IdentityOAuth2Exception {
        callbackManager = new OAuthCallbackManager();
        oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        tokenMgtDAO = new TokenMgtDAO();
    }

    public boolean authenticateResourceOwner(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();
        return OAuth2Util.authenticateUser(authorizationReqDTO.getUsername(),
                authorizationReqDTO.getPassword());
    }

    public boolean validateAccessDelegation(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();
        OAuthCallback authzCallback = new OAuthCallback(
                authorizationReqDTO.getUsername(),
                authorizationReqDTO.getConsumerKey(),
                OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION);
        authzCallback.setRequestedScope(authorizationReqDTO.getScopes());
        authzCallback.setResponseType(ResponseType.valueOf(authorizationReqDTO.getResponseType()));

        callbackManager.handleCallback(authzCallback);

        return authzCallback.isAuthorized();
    }


    public boolean validateScope(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();
        OAuthCallback scopeValidationCallback = new OAuthCallback(
                authorizationReqDTO.getUsername(),
                authorizationReqDTO.getConsumerKey(),
                OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION);
        scopeValidationCallback.setRequestedScope(authorizationReqDTO.getScopes());
        callbackManager.handleCallback(scopeValidationCallback);
        oauthAuthzMsgCtx.setApprovedScope(scopeValidationCallback.getApprovedScope());
        return scopeValidationCallback.isValidScope();
    }

    public abstract OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception;
}
