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

package org.wso2.carbon.identity.oauth2.util;

import com.google.gdata.client.authn.oauth.OAuthException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Utility methods for OAuth 2.0 implementation
 */
public class OAuth2Util {

    private static Log log = LogFactory.getLog(OAuth2Util.class);
    
    /**
     * Build a comma separated list of scopes passed as a String set by Amber.
     * @param scopes set of scopes
     * @return Comma separated list of scopes
     */
    public static String buildScopeString(String[] scopes){
        StringBuffer scopeString = new StringBuffer("");
        if (scopes != null) {
            for(String scope : scopes){
                scopeString.append(scope);
                scopeString.append(',');
            }
        }
        return scopeString.toString();
    }

    public static boolean authenticateUser(String username, String password) throws OAuthException {
        try {
            String tenantUser = MultitenantUtils.getTenantAwareUsername(username);
            String domainName = MultitenantUtils.getTenantDomain(username);
            return IdentityTenantUtil.getRealm(domainName, username).getUserStoreManager()
                    .authenticate(tenantUser, password);
        } catch (Exception e) {
            log.error("Error when authenticating the user for OAuth Authorization.", e);
            throw new OAuthException("Error when authenticating the user for OAuth Authorization.", e);
        }
    }
}
