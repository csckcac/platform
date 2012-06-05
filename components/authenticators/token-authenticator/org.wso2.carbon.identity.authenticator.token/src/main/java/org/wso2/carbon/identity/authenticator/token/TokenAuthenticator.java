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
package org.wso2.carbon.identity.authenticator.token;

import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.AuthenticationAdmin;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.identity.authenticator.token.internal.TokenAuthBEDataHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SignatureException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class TokenAuthenticator extends AbstractAdmin implements CarbonServerAuthenticator {

    private static final Log log = LogFactory.getLog(TokenAuthenticator.class);
    private static final int DEFAULT_PRIORITY_LEVEL = -5;
    private static final String AUTHENTICATOR_NAME = "TokenUIAuthenticator";

    private static Hashtable<String, HttpSession> sessionStore = new Hashtable<String, HttpSession>();

    private boolean login(String username, String password, String remoteAddress)
            throws AuthenticationException {
        return new AuthenticationAdmin().login(username, password, remoteAddress);
    }

    public String getAutheticationToken(String username, String password, String remoteAddress)
            throws AuthenticationException {
        boolean isLoggedIn = false;
        isLoggedIn = login(username, password, remoteAddress);
        if (isLoggedIn) {
            String key = UUIDGenerator.getUUID();
            try {
                RegistryService registryService = TokenAuthBEDataHolder.getInstance().getRegistryService();
                RealmService realmService = TokenAuthBEDataHolder.getInstance().getRealmService();
                String tenantDomain = MultitenantUtils.getTenantDomain(username);
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                UserRealm realm = registryService.getUserRealm(tenantId);
                int userId = realm.getUserStoreManager().getUserId(username);

                String baseString = "TenantId:=" + tenantId + "&UserId:=" + userId;
                String signature = getHMAC(key,baseString);
                baseString = baseString + "&Signature:=" + signature;

                Registry registry = registryService.getConfigSystemRegistry(tenantId);
                String path = RegistryConstants.PROFILES_PATH + username;
                Collection profile = null;
                if (!registry.resourceExists(path)) {
                    profile = registry.newCollection();
                    registry.put(path, profile);
                } else {
                    profile = (Collection) registry.get(path);
                }
                Date date = new Date();
                int ttl = 300000; // 5 seconds in ms
                key = "Key:=" + key + "&Timestamp:=" + date.getTime() + "&TTL:=" + ttl;
                profile.removeProperty(RegistryConstants.USER_TOKEN);
                profile.addProperty(RegistryConstants.USER_TOKEN, key);
                registry.put(path, profile);
                sessionStore.put(baseString, getHttpSession());
                return baseString;

            } catch (SignatureException e) {
                String msg = "Error in creating short lived authentication token for, username: "
                        + username + ".";
                log.error(msg, e);
                throw new AuthenticationException(msg, e);
            } catch (Exception e) {
                log.error("Error authenticating " + e.getMessage(), e);
                throw new AuthenticationException("Error authenticating " + e.getMessage(), e);
            }
        }
        return null;
    }

    private String getHMAC(String secretKey, String baseString) throws SignatureException {
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            return Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if(authenticatorConfig != null && authenticatorConfig.getPriority() > 0){
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public boolean isAuthenticated(MessageContext messageContext) {
        String token = getAuthenticationTokenValue(messageContext);
        if (token != null) {
            HttpSession session = sessionStore.get(token);
            HttpSession newSession = getHttpSession(messageContext);

            newSession.setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE, session
                    .getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE));
            newSession.setAttribute(ServerConstants.USER_LOGGED_IN, session
                    .getAttribute(ServerConstants.USER_LOGGED_IN));

            SuperTenantCarbonContext newContext = SuperTenantCarbonContext.getCurrentContext(newSession);
            SuperTenantCarbonContext oldContext = SuperTenantCarbonContext.getCurrentContext(session);

            newContext
                    .setRegistry(RegistryType.USER_GOVERNANCE, oldContext.getRegistry(
                            RegistryType.USER_GOVERNANCE));
            newContext.setRegistry(RegistryType.USER_CONFIGURATION,
                    oldContext.getRegistry(
                            RegistryType.USER_CONFIGURATION));
            newContext.setRegistry(RegistryType.SYSTEM_CONFIGURATION,
                    oldContext.getRegistry(
                            RegistryType.SYSTEM_CONFIGURATION));
            newContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE,
                    oldContext.getRegistry(
                            RegistryType.SYSTEM_GOVERNANCE));
            newContext.setUserRealm(oldContext.getUserRealm());
            newContext.setTenantDomain(oldContext.getTenantDomain());
            newContext.setTenantId(oldContext.getTenantId());

            String loginStatus = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
            return (loginStatus!=null);
        }
        return false;
    }

    public boolean authenticateWithRememberMe(MessageContext messageContext) {
        return false;
    }

    protected HttpSession getHttpSession(MessageContext msgCtx) {
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request = (HttpServletRequest) msgCtx
                    .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }

    private String getAuthenticationTokenValue(MessageContext messageContext) {
        Map headers = (Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if ("cookie".equals(pairs.getKey())) {
                return (String) pairs.getValue();
            }
        }
        return null;
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            return authenticatorConfig.isDisabled();
        }
        return false;
    }

    public boolean isHandle(MessageContext msgContext) {
        return true;
    }
}
