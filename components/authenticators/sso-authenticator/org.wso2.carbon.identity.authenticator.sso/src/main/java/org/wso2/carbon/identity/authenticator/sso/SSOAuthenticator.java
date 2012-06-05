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
package org.wso2.carbon.identity.authenticator.sso;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.authenticator.sso.internal.SSOAuthBEDataHolder;
import org.wso2.carbon.identity.authenticator.sso.internal.SSOAuthenticatorDSComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SSOAuthenticator {

    private static final Log log = LogFactory.getLog(SSOAuthenticator.class);

    public boolean login(String username, String password, String remoteAddress)
            throws AuthenticationException {
        HttpSession httpSess = getHttpSession();
        try {
            if ((username == null) || (password == null) || (remoteAddress == null)
                    || username.trim().equals("") || password.trim().equals("")
                    || remoteAddress.trim().equals("")) {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSess, username, -1, remoteAddress,
                        "Data");
                return false;
            }

            RegistryService registryService = SSOAuthBEDataHolder.getInstance().getRegistryService();
            RealmService realmService = SSOAuthBEDataHolder.getInstance().getRealmService();

            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            username = MultitenantUtils.getTenantAwareUsername(username);

            UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                    realmService, tenantDomain);

            boolean isAuthenticated = false;
            AxisConfiguration axisConfig =
                    CarbonConfigurationContextFactory.getConfigurationContext().getAxisConfiguration();

            if (axisConfig.getParameter(SSOConstants.SSO_SERVICE_EPR) != null) {
                String epr = (String) axisConfig.getParameter(SSOConstants.SSO_SERVICE_EPR)
                        .getValue();
                // TODO: the tenantId login should be modified..
                isAuthenticated = new SSOConsumer(epr).isAuthenticated(username, password);
            }

            boolean isAuthorized = realm.getAuthorizationManager().isUserAuthorized(username,
                    "/permission/admin/login", CarbonConstants.UI_PERMISSION_ACTION);
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            if (isAuthenticated && isAuthorized) {
                CarbonAuthenticationUtil.onSuccessAdminLogin(httpSess, username, tenantId,
                        tenantDomain, remoteAddress);
                return true;
            } else {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSess, username, tenantId,
                        remoteAddress, "Invalid credential");
                return false;
            }
        } catch (Exception e) {
            String msg = "System error while Authenticating/Authorizing User : " + e.getMessage();
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
