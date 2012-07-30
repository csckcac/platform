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

package org.wso2.carbon.identity.oauth2.token;

import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.handlers.*;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class AccessTokenIssuer {

    private Map<String, AuthorizationGrantHandler> authzGrantHandlers =
            new Hashtable<String, AuthorizationGrantHandler>();

    private List<String> supportedGrantTypes;

    private static AccessTokenIssuer instance;

    private static Log log = LogFactory.getLog(AccessTokenIssuer.class);

    public static AccessTokenIssuer getInstance() throws IdentityOAuth2Exception {

        CarbonUtils.checkSecurity();

        if(instance == null){
            synchronized (AccessTokenIssuer.class){
                if(instance == null){
                    instance = new AccessTokenIssuer();
                }
            }
        }
        return instance;
    }

    private AccessTokenIssuer() throws IdentityOAuth2Exception {

        supportedGrantTypes = OAuthServerConfiguration.getInstance().getSupportedGrantTypes();

        authzGrantHandlers.put(GrantType.AUTHORIZATION_CODE.toString(),
                new AuthorizationCodeHandler());
        authzGrantHandlers.put(GrantType.PASSWORD.toString(),
                new PasswordGrantHandler());
        authzGrantHandlers.put(GrantType.CLIENT_CREDENTIALS.toString(),
                new ClientCredentialsGrantHandler());
        authzGrantHandlers.put(GrantType.REFRESH_TOKEN.toString(),
                new RefreshGrantTypeHandler());
    }

    public OAuth2AccessTokenRespDTO issue(OAuth2AccessTokenReqDTO tokenReqDTO)
            throws IdentityException {

        String grantType = tokenReqDTO.getGrantType();
        OAuth2AccessTokenRespDTO tokenRespDTO;

        if(!supportedGrantTypes.contains(grantType)){
            log.warn("Unsupported Grant Type : " + grantType +
                    " for client id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE,
                    "Unsupported Grant Type!", tokenReqDTO);
            return tokenRespDTO;
        }

        AuthorizationGrantHandler authzGrantHandler = authzGrantHandlers.get(
                grantType);
        OAuthAppDO oAuthAppDO = new OAuthAppDAO().getAppInformation(tokenReqDTO.getClientId());
        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenReqDTO);

        boolean isAuthenticated = authzGrantHandler.authenticateClient(tokReqMsgCtx);
        if (!isAuthenticated) {
            log.warn("Client Authentication Failed for client id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_CLIENT,
                    "Client credentials are invalid.", tokenReqDTO);
            return tokenRespDTO;
        }

        boolean isValidGrant = authzGrantHandler.validateGrant(tokReqMsgCtx);
        if (!isValidGrant) {
            log.warn("Invalid Grant provided by the client, id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_GRANT,
                    "Provided Authorization Grant is invalid.", tokenReqDTO);
            return tokenRespDTO;
        }

        boolean isAuthorized = authzGrantHandler.authorizeAccessDelegation(tokReqMsgCtx);
        if (!isAuthorized) {
            log.warn("Resource owner is not authorized to grant access, client id : "
                    + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT,
                    "Unauthorized Client!", tokenReqDTO);
            return tokenRespDTO;
        }

        boolean isValidScope = authzGrantHandler.validateScope(tokReqMsgCtx);
        if (!isValidScope) {
            log.warn("Invalid Scope provided. client id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_SCOPE, "Invalid Scope!", tokenReqDTO);
            return tokenRespDTO;
        }

        tokenRespDTO = authzGrantHandler.issue(tokReqMsgCtx);
        tokenRespDTO.setCallbackURI(oAuthAppDO.getCallbackUrl());
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO handleError(String errorCode,
                                                 String errorMsg,
                                                 OAuth2AccessTokenReqDTO tokenReqDTO) {
        if (log.isDebugEnabled()) {
            log.debug("OAuth Error Code : " + errorCode + ", client id : " + tokenReqDTO.getClientId()
                    + ", Grant Type : " + tokenReqDTO.getGrantType()
                    + ", Scope : " + OAuth2Util.buildScopeString(tokenReqDTO.getScope()));
        }
        OAuth2AccessTokenRespDTO tokenRespDTO;
        tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setError(true);
        tokenRespDTO.setErrorCode(errorCode);
        tokenRespDTO.setErrorMsg(errorMsg);
        return tokenRespDTO;
    }

}
