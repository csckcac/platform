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

import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;

public class AccessTokenIssuer {

    private AuthorizationGrantHandler authGrantHandler;

    private static Log log = LogFactory.getLog(AccessTokenIssuer.class);

    public AccessTokenIssuer(AuthorizationGrantHandler authGrantHandler) {
        this.authGrantHandler = authGrantHandler;
    }

    public OAuth2AccessTokenRespDTO issue(OAuth2AccessTokenReqDTO tokReqDTO) throws IdentityException {
        OAuth2AccessTokenRespDTO tokenRespDTO;
        boolean isValid = authGrantHandler.validate();
        OAuthAppDO oAuthAppDO = new OAuthAppDAO().getAppInformation(tokReqDTO.getClientId());

        if (!isValid) {
            tokenRespDTO = new OAuth2AccessTokenRespDTO();
            tokenRespDTO.setError(true);
            tokenRespDTO.setErrorCode(OAuthError.TokenResponse.INVALID_GRANT);
            tokenRespDTO.setErrorMsg("Provided Authorization Grant is invalid.");
            return tokenRespDTO;
        }
        tokenRespDTO = authGrantHandler.issue();
        tokenRespDTO.setCallbackURI(oAuthAppDO.getCallbackUrl());
        return tokenRespDTO;
    }

}
