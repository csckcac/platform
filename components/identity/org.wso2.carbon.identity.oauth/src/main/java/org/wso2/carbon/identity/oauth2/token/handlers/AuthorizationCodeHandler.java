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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.core.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AuthzCodeDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

/**
 * Implements the AuthorizationGrantHandler for the Grant Type : authorization_code.
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
        String clientId = oAuth2AccessTokenReqDTO.getClientId();

        AuthzCodeDO authzCodeDO = null;
        // if cache is enabled, check in the cache first.
        if(cacheEnabled){
            CacheKey cacheKey = new OAuthCacheKey(OAuth2Util.buildCacheKeyStringForAuthzCode(
                    clientId, authorizationCode));
            authzCodeDO = (AuthzCodeDO) oauthCache.getValueFromCache(cacheKey);
        }

        if(log.isDebugEnabled()){
            if (authzCodeDO != null ) {
                log.debug("Authorization Code Info was available in cache for client id : "
                        + clientId);
            } else {
                log.debug("Authorization Code Info was not available in cache for client id : "
                        + clientId);
            }
        }

        // authz Code is not available in cache. check the database
        if (authzCodeDO == null) {
            authzCodeDO = tokenMgtDAO.validateAuthorizationCode(clientId,
                    authorizationCode);
        }

        //Check whether it is a valid grant
        if (authzCodeDO == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid access token request with " +
                        "Client Id : " + clientId +
                        " , Authorization Code : " + oAuth2AccessTokenReqDTO.getAuthorizationCode());
            }
            return false;
        }

        // Check whether the grant is expired
        long issuedTimeInMillis = authzCodeDO.getIssuedTime().getTime();
        long validityPeriodInMillis = authzCodeDO.getValidityPeriod();
        long timestampSkew = OAuthServerConfiguration.getInstance()
                .getDefaultTimeStampSkewInSeconds() * 1000;
        long currentTimeInMillis = System.currentTimeMillis();

        // if authorization code is expired.
        if ((currentTimeInMillis - timestampSkew) > (issuedTimeInMillis + validityPeriodInMillis)) {
            if (log.isDebugEnabled()) {
                log.debug("Authorization Code : " + authorizationCode + " is expired." +
                        " Issued Time(ms) : " + issuedTimeInMillis +
                        ", Validity Period : " + validityPeriodInMillis +
                        ", Timestamp Skew : " + timestampSkew +
                        ", Current Time : " + currentTimeInMillis);
            }

            // remove the authorization code from the database.
            tokenMgtDAO.cleanUpAuthzCode(authorizationCode);
            if(log.isDebugEnabled()){
                log.debug("Expired Authorization code : " + authorizationCode +
                        " issued for client " + clientId +
                        " was removed from the database.");
            }

            // remove the authorization code from the cache
            oauthCache.clearCacheEntry(new OAuthCacheKey(
                    OAuth2Util.buildCacheKeyStringForAuthzCode(clientId, authorizationCode)));
            if(log.isDebugEnabled()){
                log.debug("Expired Authorization code : " + authorizationCode +
                        " issued for client " + clientId +
                        " was removed from the cache.");
            }

            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Found an Authorization Code, " +
                    "Client : " + clientId +
                    " , Authorization Code : " + oAuth2AccessTokenReqDTO.getAuthorizationCode() +
                    ", authorized user : " + authzCodeDO.getAuthorizedUser() +
                    ", scope : " + OAuth2Util.buildScopeString(authzCodeDO.getScope()));
        }

        tokReqMsgCtx.setAuthorizedUser(authzCodeDO.getAuthorizedUser());
        tokReqMsgCtx.setScope(authzCodeDO.getScope());
        return true;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = super.issue(tokReqMsgCtx);
        String authorizationCode = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode();

        // Clear the cache entry
        if(cacheEnabled){
            String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
            OAuthCacheKey cacheKey = new OAuthCacheKey(OAuth2Util.buildCacheKeyStringForAuthzCode(
                    clientId, authorizationCode));
            oauthCache.clearCacheEntry(cacheKey);

            if (log.isDebugEnabled()) {
                log.debug("Cache was cleared for authorization code info for client id : " + clientId);
            }
        }
        // remove the authz code from the database
        tokenMgtDAO.cleanUpAuthzCode(authorizationCode);

        if (log.isDebugEnabled()) {
            log.debug("Authorization Code clean up completed for request from the Client, " +
                    "Client Id: " + authorizationCode);
        }

        return tokenRespDTO;
    }

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        // authorization is handled when the authorization code was issued.
        return true;
    }

}
