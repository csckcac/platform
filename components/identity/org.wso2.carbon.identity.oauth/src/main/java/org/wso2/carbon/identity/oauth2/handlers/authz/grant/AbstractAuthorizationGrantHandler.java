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

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth.callback.OAuthCallbackManager;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.handlers.authz.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.Timestamp;
import java.util.Date;

public abstract class AbstractAuthorizationGrantHandler implements AuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(AbstractAuthorizationGrantHandler.class);

    protected TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
    protected final OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
    protected OAuthCallbackManager callbackManager;

    protected AbstractAuthorizationGrantHandler() throws IdentityOAuth2Exception {
        callbackManager = new OAuthCallbackManager();
    }

    public boolean authenticateClient(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
        try {
            return OAuth2Util.authenticateClient(
                    oAuth2AccessTokenReqDTO.getClientId(),
                    oAuth2AccessTokenReqDTO.getClientSecret());
        } catch (IdentityOAuthAdminException e) {
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
    }

    public abstract boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception;

    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();

        String accessToken;
        String refreshToken;

        try {
            accessToken = oauthIssuerImpl.accessToken();
            refreshToken = oauthIssuerImpl.refreshToken();
        } catch (OAuthSystemException e) {
            throw new IdentityOAuth2Exception("Error when generating the tokens.", e);
        }

        // TODO : Fix the timestamp and validity period properly.
        Timestamp timestamp = new Timestamp(new Date().getTime());
        // Default Validity Period
        long validityPeriod = 60 * 60;

        String scopeString = OAuth2Util.buildScopeString(tokReqMsgCtx.getScope());
        // store the new token
        tokenMgtDAO.storeAccessToken(accessToken, refreshToken, oAuth2AccessTokenReqDTO.getClientId(),
                tokReqMsgCtx.getAuthorizedUser(), timestamp, validityPeriod, scopeString,
                OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

        if (log.isDebugEnabled()) {
            log.debug("Persisted an access token with " +
                    "Client ID : " + oAuth2AccessTokenReqDTO.getClientId() +
                    "authorized user : " + tokReqMsgCtx.getAuthorizedUser() +
                    "timestamp : " + timestamp +
                    "validity period : " + validityPeriod +
                    "scope : " + tokReqMsgCtx.getScope() +
                    "Token State : " + OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);
        }

        tokenRespDTO.setAccessToken(accessToken);
        tokenRespDTO.setRefreshToken(refreshToken);
        return tokenRespDTO;
    }

    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuthCallback authzCallback = new OAuthCallback(
                tokReqMsgCtx.getAuthorizedUser(),
                tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId(),
                OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION);
        authzCallback.setRequestedScope(tokReqMsgCtx.getScope());
        authzCallback.setGrantType(GrantType.valueOf(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType()));

        callbackManager.handleCallback(authzCallback);
        return authzCallback.isAuthorized();
    }

    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuthCallback scopeValidationCallback = new OAuthCallback(
                tokReqMsgCtx.getAuthorizedUser(),
                tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId(),
                OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION);
        scopeValidationCallback.setRequestedScope(tokReqMsgCtx.getScope());
        callbackManager.handleCallback(scopeValidationCallback);
        tokReqMsgCtx.setScope(scopeValidationCallback.getApprovedScope());
        return scopeValidationCallback.isValidScope();
    }
}
