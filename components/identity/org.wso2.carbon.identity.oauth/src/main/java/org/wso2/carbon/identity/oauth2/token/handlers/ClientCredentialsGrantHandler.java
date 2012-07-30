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
import org.wso2.carbon.caching.core.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Grant Handler for Grant Type : client_credentials
 */
public class ClientCredentialsGrantHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(ClientCredentialsGrantHandler.class);

    public ClientCredentialsGrantHandler() throws IdentityOAuth2Exception {
        super();
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();

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

        AccessTokenDO accessTokenDO = new AccessTokenDO(tokReqMsgCtx.getAuthorizedUser(),
                tokReqMsgCtx.getScope(), timestamp, validityPeriod);
        accessTokenDO.setTokenState(OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

        // get the secured version of the access token
        String preprocessedAccessToken = tokenPersistencePreprocessor.getPreprocessedToken(accessToken);

        // store the new token
        tokenMgtDAO.storeAccessToken(preprocessedAccessToken, oAuth2AccessTokenReqDTO.getClientId(),
                accessTokenDO);

        // add the access token info to the cache, if it's enabled.
        if(cacheEnabled){
            CacheKey cacheKey = new OAuthCacheKey(preprocessedAccessToken);
            oauthCache.addToCache(cacheKey, accessTokenDO);

            if(log.isDebugEnabled()){
                log.debug("Access Token info was added to the cache for the client id : " +
                        oAuth2AccessTokenReqDTO.getClientId());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Persisted an access token with " +
                    "Client ID : " + oAuth2AccessTokenReqDTO.getClientId() +
                    "authorized user : " + tokReqMsgCtx.getAuthorizedUser() +
                    "timestamp : " + timestamp +
                    "validity period : " + validityPeriod +
                    "scope : " + OAuth2Util.buildScopeString(tokReqMsgCtx.getScope()) +
                    "Token State : " + OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);
        }

        tokenRespDTO.setAccessToken(accessToken);
        tokenRespDTO.setExpiresIn(validityPeriod/1000);
        return tokenRespDTO;
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        // By this time, we have already validated client credentials.
        tokReqMsgCtx.setScope(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getScope());
        return true;
    }
}
