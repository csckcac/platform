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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;

import java.sql.Timestamp;
import java.util.Date;

public abstract class AbstractAuthorizationGrantHandler implements AuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(AbstractAuthorizationGrantHandler.class);

    protected OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO;
    protected TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
    protected final OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
    protected String authorizedUser;
    protected String scope;

    public AbstractAuthorizationGrantHandler(OAuth2AccessTokenReqDTO reqDTO) {
        oAuth2AccessTokenReqDTO = reqDTO;
    }

    public abstract boolean validate() throws IdentityException;

    public OAuth2AccessTokenRespDTO issue() throws IdentityException {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        String accessToken;
        String refreshToken;

        try {
            accessToken = oauthIssuerImpl.accessToken();
            refreshToken = oauthIssuerImpl.refreshToken();
        } catch (OAuthSystemException e) {
            throw new IdentityException("Error when generating the tokens.", e);
        }

        // TODO : Fix the timestamp and validity period properly.
        Timestamp timestamp = new Timestamp(new Date().getTime());
        // Default Validity Period
        long validityPeriod = 60 * 60;

        // store the new token
        tokenMgtDAO.storeAccessToken(accessToken, refreshToken, oAuth2AccessTokenReqDTO.getClientId(),
                authorizedUser, timestamp, validityPeriod, scope,
                OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

        if (log.isDebugEnabled()) {
            log.debug("Persisted an access token with " +
                    "Client ID : " + oAuth2AccessTokenReqDTO.getClientId() +
                    "authorized user : " + authorizedUser +
                    "timestamp : " + timestamp +
                    "validity period : " + validityPeriod +
                    "scope : " + scope +
                    "Token State : " + OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);
        }

        tokenRespDTO.setAccessToken(accessToken);
        tokenRespDTO.setRefreshToken(refreshToken);
        return tokenRespDTO;
    }
}
