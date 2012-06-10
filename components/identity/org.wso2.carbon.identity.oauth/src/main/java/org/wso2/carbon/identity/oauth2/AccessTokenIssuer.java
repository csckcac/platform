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

package org.wso2.carbon.identity.oauth2;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;

import java.sql.Timestamp;
import java.util.Date;

public class AccessTokenIssuer {

    private OAuthIssuer oauthIssuerImpl;
    private static Log log = LogFactory.getLog(AccessTokenIssuer.class);

    public AccessTokenIssuer() {
        oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
    }

    public OAuth2AccessTokenRespDTO issue(OAuth2AccessTokenReqDTO tokReqDTO) throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();

        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
        if(GrantType.AUTHORIZATION_CODE.toString().equals(tokReqDTO.getGrantType())){
            try {
                String authorizationCode = tokReqDTO.getAuthorizationCode();
                String[] authzCodeData = tokenMgtDAO.validateAuthorizationCode(tokReqDTO.getClientId(),
                        authorizationCode);
                if(authzCodeData[0] == null){
                    if(log.isDebugEnabled()){
                        log.debug("Invalid access token request with Client Id : " + tokReqDTO.getClientId()
                                + " , Authorization Code : " + tokReqDTO.getAuthorizationCode());
                    }
                    OAuthAppDO oAuthAppDO = new OAuthAppDAO().getAppInformation(tokReqDTO.getClientId());
                    tokenRespDTO.setError(true);
                    tokenRespDTO.setErrorCode(OAuthError.TokenResponse.INVALID_GRANT);
                    tokenRespDTO.setErrorMsg("Provided Authorization Grant is invalid.");
                    tokenRespDTO.setCallbackURI(oAuthAppDO.getCallbackUrl());
                    return tokenRespDTO;
                }
                // TODO : Check the Scope

                String accessToken = oauthIssuerImpl.accessToken();
                String refreshToken = oauthIssuerImpl.refreshToken();

                // TODO : Fix the timestamp and validity period properly.
                Timestamp timestamp = new Timestamp(new Date().getTime());
                // Default Validity Period
                long validityPeriod = 60 * 60;

                // store the new token
                tokenMgtDAO.storeAccessToken(accessToken, refreshToken, tokReqDTO.getClientId(),
                        authzCodeData[1], timestamp, validityPeriod, authzCodeData[2],
                        OAuth2Constants.TokenStates.TOKEN_STATE_ACTIVE);

                // remove the authz code
                tokenMgtDAO.cleanUpAuthzCode(authorizationCode);

                tokenRespDTO.setAccessToken(accessToken);
                tokenRespDTO.setRefreshToken(refreshToken);
                tokenRespDTO.setCallbackURI(authzCodeData[0]);

                return tokenRespDTO;

            } catch (Exception e) {
                throw new IdentityOAuth2Exception("Error when issuing the access token. ", e);
            }
        }
        return null;
    }
}
