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

package org.wso2.carbon.identity.oauth2.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.BearerTokenValidationDO;

/**
 * Token validator that supports "bearer" token type.
 */
public class BearerTokenValidator implements OAuth2TokenValidator {

    private static Log log = LogFactory.getLog(BearerTokenValidator.class);

    private TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();

    public static final String TOKEN_TYPE = "bearer";

    public OAuth2TokenValidationResponseDTO validate(
            OAuth2TokenValidationRequestDTO validationReqDTO)
            throws IdentityOAuth2Exception {

        OAuth2TokenValidationResponseDTO tokenRespDTO = new OAuth2TokenValidationResponseDTO();

        String accessToken = validationReqDTO.getAccessToken();
        String clientId = validationReqDTO.getClientId();

        // incomplete token validation request
        if (clientId == null || accessToken == null) {
            log.warn("Client Id or Access Token is not present in the validation request.");
            tokenRespDTO.setValid(false);
            tokenRespDTO.setErrorMsg("Client Id or Access Token is not present " +
                    "in the validation request.");
            return tokenRespDTO;
        }

        BearerTokenValidationDO tokValidationDO = tokenMgtDAO.validateBearerToken(clientId,
                accessToken);

        // if the access token or client id is not valid
        if (tokValidationDO == null) {
            log.warn("Invalid Access Token or Client Id. " +
                    "Access Token : " + accessToken + ", Client id : " + clientId);
            tokenRespDTO.setValid(false);
            tokenRespDTO.setErrorMsg("Invalid Access Token or Client Id.");
            return tokenRespDTO;
        }

        // Check whether the grant is expired
        long issuedTimeInMillis = tokValidationDO.getIssuedTime().getTime();
        long validityPeriodInMillis = tokValidationDO.getValidityPeriod();
        long timestampSkew = OAuthServerConfiguration.getInstance()
                .getDefaultTimeStampSkewInSeconds() * 1000;
        long currentTimeInMillis = System.currentTimeMillis();

        if ((currentTimeInMillis - timestampSkew) > (issuedTimeInMillis + validityPeriodInMillis)) {
            log.warn("Access Token is expired. Client Id : " + clientId);
            if (log.isDebugEnabled()) {
                log.debug("Access Token : " + accessToken + " is expired." +
                        " Issued Time(ms) : " + issuedTimeInMillis +
                        ", Validity Period : " + validityPeriodInMillis +
                        ", Timestamp Skew : " + timestampSkew +
                        ", Current Time : " + currentTimeInMillis);
            }
            tokenRespDTO.setValid(false);
            tokenRespDTO.setErrorMsg("Access Token is expired");
            return tokenRespDTO;
        }

        tokenRespDTO.setValid(true);
        tokenRespDTO.setAuthorizedUser(tokValidationDO.getAuthzUser());
        tokenRespDTO.setExpiryTime(validityPeriodInMillis/1000);
        tokenRespDTO.setScope(tokValidationDO.getScope());
        return tokenRespDTO;
    }

}
