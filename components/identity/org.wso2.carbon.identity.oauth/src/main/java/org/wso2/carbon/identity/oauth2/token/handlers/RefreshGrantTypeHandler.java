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

package org.wso2.carbon.identity.oauth2.token.handlers;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Grant Type handler for Grant Type refresh_token which is used to get a new access token.
 */
public class RefreshGrantTypeHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(RefreshGrantTypeHandler.class);
    private static final String PREV_ACCESS_TOKEN = "previousAccessToken";

    public RefreshGrantTypeHandler() throws IdentityOAuth2Exception {
        super();
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenReqDTO tokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();

        RefreshTokenValidationDataDO validationDataDO = tokenMgtDAO.validateRefreshToken(
                tokenReqDTO.getClientId(),
                tokenReqDTO.getRefreshToken());

        if (validationDataDO.getAccessToken() == null) {
            log.debug("Invalid Refresh Token provided for Client with " +
                    "Client Id : " + tokenReqDTO.getClientId() +
                    ", Refresh Token : " + tokenReqDTO.getRefreshToken());
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Refresh token validation successful for " +
                    "Client id : " + tokenReqDTO.getClientId() +
                    ", Authorized User : " + validationDataDO.getAuthorizedUser() +
                    ", Token Scope : " + OAuth2Util.buildScopeString(validationDataDO.getScope()));
        }

        tokReqMsgCtx.setAuthorizedUser(validationDataDO.getAuthorizedUser());
        tokReqMsgCtx.setScope(validationDataDO.getScope());
        tokReqMsgCtx.addProperty(PREV_ACCESS_TOKEN, validationDataDO.getAccessToken());
        return true;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        OAuth2AccessTokenReqDTO oauth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();

        String accessToken;

        try {
            accessToken = oauthIssuerImpl.accessToken();
        } catch (OAuthSystemException e) {
            throw new IdentityOAuth2Exception("Error when generating the tokens.", e);
        }

        Timestamp timestamp = new Timestamp(new Date().getTime());

        // Default Validity Period (in seconds)
        long validityPeriod = OAuthServerConfiguration.getInstance()
                .getDefaultAccessTokenValidityPeriodInSeconds();

        // if a VALID validity period is set through the callback, then use it
        long callbackValidityPeriod = tokReqMsgCtx.getValidityPeriod();
        if ((callbackValidityPeriod != OAuth2Constants.UNASSIGNED_VALIDITY_PERIOD)
                && callbackValidityPeriod > 0) {
            validityPeriod = callbackValidityPeriod;
        }

        validityPeriod = validityPeriod * 1000;

        String scopeString = OAuth2Util.buildScopeString(tokReqMsgCtx.getScope());
        String refreshToken = oauth2AccessTokenReqDTO.getRefreshToken();

        // store the new access token
        tokenMgtDAO.storeAccessToken(accessToken, refreshToken,
                oauth2AccessTokenReqDTO.getClientId(), tokReqMsgCtx.getAuthorizedUser(),
                timestamp, validityPeriod, scopeString,
                OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

        // Remove the previous access token
        tokenMgtDAO.cleanUpAccessToken(tokReqMsgCtx.getProperty(PREV_ACCESS_TOKEN));

        if (log.isDebugEnabled()) {
            log.debug("Persisted an access token for the refresh token, " +
                    "Client ID : " + oauth2AccessTokenReqDTO.getClientId() +
                    "authorized user : " + tokReqMsgCtx.getAuthorizedUser() +
                    "timestamp : " + timestamp +
                    "validity period : " + validityPeriod +
                    "scope : " + OAuth2Util.buildScopeString(tokReqMsgCtx.getScope()) +
                    "Token State : " + OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);
        }

        tokenRespDTO.setAccessToken(accessToken);
        tokenRespDTO.setRefreshToken(refreshToken);
        tokenRespDTO.setExpiresIn(validityPeriod/1000);
        return tokenRespDTO;
    }
}
