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

package org.wso2.carbon.identity.oauth2.authz.handlers;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.Timestamp;
import java.util.Date;

public class TokenResponseTypeHandler extends AbstractAuthorizationHandler {

    private static Log log = LogFactory.getLog(TokenResponseTypeHandler.class);

    public TokenResponseTypeHandler() throws IdentityOAuth2Exception {
        super();
    }

    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AuthorizeRespDTO respDTO = new OAuth2AuthorizeRespDTO();
        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();
        respDTO.setCallbackURI(authorizationReqDTO.getCallbackUrl());
        String accessToken;
        try {
            accessToken = oauthIssuerImpl.accessToken();
        } catch (OAuthSystemException e) {
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }

        Timestamp timestamp = new Timestamp(new Date().getTime());

        // Default Validity Period
        long validityPeriod = OAuthServerConfiguration.getInstance()
                .getDefaultAccessTokenValidityPeriodInSeconds();

        // if a VALID validity period is set through the callback, then use it
        long callbackValidityPeriod = oauthAuthzMsgCtx.getValidityPeriod();
        if ((callbackValidityPeriod != OAuth2Constants.UNASSIGNED_VALIDITY_PERIOD)
                && callbackValidityPeriod > 0) {
            validityPeriod = callbackValidityPeriod;
        }

        // convert back to milliseconds
        validityPeriod = validityPeriod * 1000;

        tokenMgtDAO.storeAccessToken(accessToken,
                null,
                authorizationReqDTO.getConsumerKey(),
                authorizationReqDTO.getUsername(),
                timestamp,
                validityPeriod,
                OAuth2Util.buildScopeString(oauthAuthzMsgCtx.getApprovedScope()),
                OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

        if (log.isDebugEnabled()) {
            log.debug("Issued AccessToken Code to user : " +
                    authorizationReqDTO.getUsername() + ". Using the redirect url : " +
                    authorizationReqDTO.getCallbackUrl());
        }

        respDTO.setAuthorized(true);
        respDTO.setAccessToken(accessToken);
        respDTO.setValidityPeriod(validityPeriod/1000);
        return respDTO;
    }
}
