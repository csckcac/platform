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

package org.wso2.carbon.identity.oauth.ui.endpoints.token;

import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2ServiceClient;
import org.wso2.carbon.identity.oauth.ui.internal.OAuthUIServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.ui.CarbonUIUtil;

/**
 * Class which will invoke the OAuth2ServiceClient to issue Access Tokens.
 */
public class OAuth2TokenClient {

    private static Log log = LogFactory.getLog(OAuth2TokenClient.class);

    private String backendServerURL;
    private ConfigurationContext configContext;

    public OAuth2TokenClient() {
        OAuthUIServiceComponentHolder serviceComponentHolder = OAuthUIServiceComponentHolder.
                getInstance();
        backendServerURL = CarbonUIUtil.getServerURL(
                serviceComponentHolder.getServerConfigurationService());
        configContext = serviceComponentHolder.
                getConfigurationContextService().getClientConfigContext();
    }

    public OAuth2AccessTokenRespDTO getAccessToken(OAuthTokenRequest oauthRequest)
                                                                    throws OAuthClientException {
        OAuth2AccessTokenReqDTO tokenReqDTO = new OAuth2AccessTokenReqDTO();
        String grantType = oauthRequest.getGrantType();

        tokenReqDTO.setGrantType(grantType);
        tokenReqDTO.setClientId(oauthRequest.getClientId());
        tokenReqDTO.setClientSecret(oauthRequest.getClientSecret());
        tokenReqDTO.setScope(oauthRequest.getScopes().toArray(new String[oauthRequest.getScopes().size()]));
        // Check the grant type and set the corresponding parameters
        if(GrantType.AUTHORIZATION_CODE.toString().equals(grantType)){
            tokenReqDTO.setAuthorizationCode(oauthRequest.getCode());
        } else if(GrantType.PASSWORD.toString().equals(grantType)){
            tokenReqDTO.setResourceOwnerUsername(oauthRequest.getUsername());
            tokenReqDTO.setResourceOwnerPassword(oauthRequest.getPassword());
        } else if (GrantType.REFRESH_TOKEN.toString().equals(grantType)){
            tokenReqDTO.setRefreshToken(oauthRequest.getRefreshToken());
        }

        try {
            OAuth2ServiceClient oauth2ServiceClient = new OAuth2ServiceClient(backendServerURL,
                                                                                configContext);
            return oauth2ServiceClient.issueAccessToken(tokenReqDTO);
        } catch (Exception e){
            String errorMsg = "Error when invoking the OAuth2Service to get an access token.";
            log.error(errorMsg, e);
            throw new OAuthClientException(errorMsg, e);
        }
    }
}
