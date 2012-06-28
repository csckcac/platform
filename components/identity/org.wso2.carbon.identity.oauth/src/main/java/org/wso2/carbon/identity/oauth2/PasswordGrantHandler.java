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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Handles the Password Grant Type of the OAuth 2.0 specification. Resource owner sends his
 * credentials in the token request which is validated against the corresponding user store.
 */
public class PasswordGrantHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(PasswordGrantHandler.class);

    public PasswordGrantHandler(OAuth2AccessTokenReqDTO reqDTO) {
        super(reqDTO);
    }

    @Override
    public boolean validate() throws IdentityException {
        String username = oAuth2AccessTokenReqDTO.getResourceOwnerUsername();
        int tenantId = IdentityUtil.getTenantIdOFUser(username);

        // tenantId < 0, means an invalid tenant.
        if(tenantId < 0){
            if (log.isDebugEnabled()) {
                log.debug("Token request with Password Grant Type for an invalid tenant : " +
                        MultitenantUtils.getTenantDomain(username));
            }
            return false;
        }

        RealmService realmService = OAuthComponentServiceHolder.getRealmService();
        boolean authStatus;
        try {
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(MultitenantUtils.getTenantAwareUsername(username),
                    oAuth2AccessTokenReqDTO.getResourceOwnerPassword());

            if(log.isDebugEnabled()){
                log.debug("Token request with Password Grant Type received. " +
                        "Username : " + username +
                        "Scope : " + oAuth2AccessTokenReqDTO.getScope() +
                        ", Authentication State : " + authStatus);
            }

        } catch (UserStoreException e) {
            throw new IdentityException("Error when authenticating the user credentials.", e);
        }

        authorizedUser = oAuth2AccessTokenReqDTO.getResourceOwnerUsername();
        //scope = oAuth2AccessTokenReqDTO.getScope();
        return authStatus;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue() throws IdentityException {
        return super.issue();
    }
}
