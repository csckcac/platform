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
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import java.util.Hashtable;
import java.util.Map;

/**
 * Handles the token validation by invoking the proper validation handler by looking at the token
 * type.
 */
public class TokenValidationHandler {

    private Log log = LogFactory.getLog(TokenValidationHandler.class);

    private Map<String, OAuth2TokenValidator> tokenValidators =
            new Hashtable<String, OAuth2TokenValidator>();

    private static TokenValidationHandler instance = new TokenValidationHandler();

    private TokenValidationHandler() {
        tokenValidators.put(BearerTokenValidator.TOKEN_TYPE, new BearerTokenValidator());
    }

    public static TokenValidationHandler getInstance() {
        return instance;
    }

    public OAuth2TokenValidationResponseDTO validate(OAuth2TokenValidationRequestDTO requestDTO)
            throws IdentityOAuth2Exception {

        OAuth2TokenValidator tokenValidator = tokenValidators.get(requestDTO.getTokenType());

        // There is no token validator for the provided token type.
        if (tokenValidator == null) {
            log.warn("Unsupported token type.");
            OAuth2TokenValidationResponseDTO tokenValidationRespDTO =
                    new OAuth2TokenValidationResponseDTO();
            tokenValidationRespDTO.setValid(false);
            tokenValidationRespDTO.setErrorMsg("Unsupported token type.");
            return tokenValidationRespDTO;
        }

        return tokenValidator.validate(requestDTO);
    }
}
