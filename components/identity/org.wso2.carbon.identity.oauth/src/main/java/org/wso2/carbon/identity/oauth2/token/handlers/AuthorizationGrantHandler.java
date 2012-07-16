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

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

/**
 * The interface needs to be implemented by all the authorization grant validators.
 */
public interface AuthorizationGrantHandler {

    public boolean authenticateClient(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception;
    /**
     * Validate the Authorization Grant
     * @return <Code>true</Code>|<Code>false</Code> if the grant_type is valid or not.
     * @throws org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception Error when validating
     * the authorization grant.
     */
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception;

    /**
     * Validate whether the claimed user is the rightful resource owner
     * @return <Code>true</Code>|<Code>false</Code> if it's the rightful resource owner
     * @throws IdentityOAuth2Exception Error when performing the callback
     */
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception;

    /**
     * Validate whether scope requested by the access token is valid
     * @return <Code>true</Code>|<Code>false</Code> if the scope is correct.
     * @throws IdentityOAuth2Exception Error when performing the callback
     */
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception;

    /**
     * Issue the Access token
     * @return <Code>OAuth2AccessTokenRespDTO</Code> representing the Access Token
     * @throws IdentityException Error when generating or persisting the access token
     */
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception;
}
