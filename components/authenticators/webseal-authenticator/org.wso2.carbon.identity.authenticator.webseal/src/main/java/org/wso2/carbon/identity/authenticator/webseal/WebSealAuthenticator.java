/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.authenticator.webseal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.authenticator.webseal.internal.WebSealAuthBEDataHolder;
import org.wso2.carbon.identity.authenticator.webseal.internal.WebSealAuthenticatorDSComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.TenantUtils;

public class WebSealAuthenticator {

    private static final Log log = LogFactory.getLog(WebSealAuthenticator.class);

    /**
     * Supports delegated identity logins. A third party system authenticates
     * the authenticatedUser - had to accept it's assertion the system needs to
     * authenticates it self to carbon. Also the only the users who belong the
     * roles having delegate-identity permission could perform this action.
     * 
     * @param username
     *            User who could delegate logins for other users.
     * @param password
     *            Password of the user who could delegate logins for other
     *            users.
     * @param authenticatedUser
     *            The user who's being authenticated by a third party system.
     * @param remoteAddress
     *            RemoteAddress
     * @return true if auth succeeded, false otherwise
     * @throws AuthenticationException
     */
    public boolean loginWithDelegation(String username, String password, String authenticatedUser,
            String remoteAddress) throws AuthenticationException {

        HttpSession httpSess = getHttpSession();
        try {
            if ((username == null) || (password == null) || (remoteAddress == null)
                    || username.trim().equals("") || password.trim().equals("")
                    || remoteAddress.trim().equals("")) {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSess, username, -1, remoteAddress,
                        "Data");
                return false;
            }

            RegistryService registryService = WebSealAuthBEDataHolder.getInstance().getRegistryService();
            RealmService realmService = WebSealAuthBEDataHolder.getInstance().getRealmService();

            String tenantDomain = UserCoreUtil.getTenantDomain(realmService, username);
            username = UserCoreUtil.getTenantLessUsername(username);

            UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                    realmService, tenantDomain);

            boolean isAuthenticated = realm.getUserStoreManager().authenticate(username, password);

            if (!isAuthenticated) {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSess, username, -1, remoteAddress,
                        "Data");
                return false;
            }

            // If we are to trust the user who delegates identity - he should be
            // in a role having
            // delegate-identity permission.
            boolean isDelegateToAuthorized = realm.getAuthorizationManager().isUserAuthorized(
                    username, "System", "delegate-identity");

            // authenticatedUser user should have the permission to login to the
            // system.
            boolean isLoginToAuthorized = realm.getAuthorizationManager().isUserAuthorized(
                    authenticatedUser, "System", "login");
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            if (isDelegateToAuthorized && isLoginToAuthorized) {
                CarbonAuthenticationUtil.onSuccessAdminLogin(httpSess, username, tenantId,
                        tenantDomain, remoteAddress);
                log.info("Identity delegation by " + username + " on behalf of "
                        + authenticatedUser + " from IP address " + remoteAddress);
                return true;
            } else {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSess, username, -1, remoteAddress,
                        "User is not authorized to login using delegation");
                return false;
            }
        } catch (Exception e) {
            String msg = "System error while Authenticating/Authorizing User with identity delegation";
            log.error(msg, e);
            return false;
        }
    }

    private HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSess = null;
        if (msgCtx != null) {
            HttpServletRequest request = (HttpServletRequest) msgCtx
                    .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSess = request.getSession();
        }
        return httpSess;
    }

}
