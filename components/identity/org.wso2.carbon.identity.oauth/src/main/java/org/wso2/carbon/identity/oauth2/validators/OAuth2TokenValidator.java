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

import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

/**
 * All the implementations that handles different token types should implement this interface.
 */
public interface OAuth2TokenValidator {
    /**
     * Validate the OAuth2 token request
     * @param validationReqDTO <code>OAuth2TokenValidationRequestDTO</code>
     *                         instance representing token request params
     * @return <code>OAuth2TokenValidationResponseDTO</code> instance representing results.
     * @throws IdentityOAuth2Exception Error when processing the token
     */
    public OAuth2TokenValidationResponseDTO validate(
            OAuth2TokenValidationRequestDTO validationReqDTO) throws IdentityOAuth2Exception;

}
