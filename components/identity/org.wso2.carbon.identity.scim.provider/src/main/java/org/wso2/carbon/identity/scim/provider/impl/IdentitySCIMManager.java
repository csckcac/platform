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
package org.wso2.carbon.identity.scim.provider.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.charon.core.encoder.Decoder;
import org.wso2.charon.core.encoder.Encoder;
import org.wso2.charon.core.encoder.json.JSONDecoder;
import org.wso2.charon.core.encoder.json.JSONEncoder;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.exceptions.FormatNotSupportedException;
import org.wso2.charon.core.exceptions.UnauthorizedException;
import org.wso2.charon.core.extensions.AuthenticationHandler;
import org.wso2.charon.core.extensions.AuthenticationInfo;
import org.wso2.charon.core.extensions.CharonManager;
import org.wso2.charon.core.extensions.TenantDTO;
import org.wso2.charon.core.extensions.TenantManager;
import org.wso2.charon.core.extensions.UserManager;
import org.wso2.charon.core.protocol.ResponseCodeConstants;
import org.wso2.charon.core.protocol.endpoints.AbstractResourceEndpoint;
import org.wso2.charon.core.schema.SCIMConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdentitySCIMManager implements CharonManager {
    /*private static AuthenticationHandler authenticationHandler;
    private static AuthenticationInfo authenticationInfo;*/
    //private TenantManager tenantManager;
    private static Log log = LogFactory.getLog(IdentitySCIMManager.class);

    private static volatile IdentitySCIMManager identitySCIMManager;

    private static Map<String, Encoder> encoderMap = new HashMap<String, Encoder>();
    private static Map<String, Decoder> decoderMap = new HashMap<String, Decoder>();
    private static Map<String, Map> authenticators = new HashMap<String, Map>();
    private static Map<String, String> endpointURLs = new HashMap<String, String>();

    private static Map<Integer, UserManager> userManagers = new ConcurrentHashMap<Integer, UserManager>();
    private static final String INSTANCE = "instance";

    //TODO:should be moved to charon-config
    private static final String USERS_URL = "http://localhost:9764/wso2/scim/Users";
    private static final String GROUPS_URL = "http://localhost:9764/wso2/scim/Groups";

    /**
     * Perform initialization at the deployment of the webapp.
     */
    private void init() throws CharonException {
        //TODO:read config and init stuff, if nothing in config, make sure to initialize default stuff.

        //if no encoder/decoders provided by the configuration, register defaults.
        encoderMap.put(SCIMConstants.JSON, new JSONEncoder());
        decoderMap.put(SCIMConstants.JSON, new JSONDecoder());

        //create basic auth - authenticator property
        Map<String, Object> basicAuthAuthenticator = new HashMap<String, Object>();
        basicAuthAuthenticator.put(INSTANCE, new BasicAuthHandler());
        basicAuthAuthenticator.put(SCIMConstants.AUTH_PROPERTY_PRIMARY, true);
        //add basic auth authenticator properties to authenticators list.
        authenticators.put(SCIMConstants.AUTH_TYPE_BASIC, basicAuthAuthenticator);

        //register encoder,decoders in AbstractResourceEndpoint, since they are called with in the API
        registerCoders();

        //Define endpoint urls to be used in Location Header
        endpointURLs.put(SCIMConstants.USER_ENDPOINT, USERS_URL);
        endpointURLs.put(SCIMConstants.GROUP_ENDPOINT, GROUPS_URL);

        //register endpoint URLs in AbstractResourceEndpoint since they are called with in the API
        registerEndpointURLs();

        //register a default user manager
        //UserManager userManager = new InMemroyUserManager(0, "wso2.com");
        //userManagers.put(0, userManager);
    }

    /**
     * Should return the static instance of CharonManager implementation.
     * Read the config and initialize extensions as specified in the config.
     *
     * @return
     */
    public static IdentitySCIMManager getInstance() throws CharonException {
        if (identitySCIMManager == null) {
            synchronized (IdentitySCIMManager.class) {
                if (identitySCIMManager == null) {
                    identitySCIMManager = new IdentitySCIMManager();
                    return identitySCIMManager;
                } else {
                    return identitySCIMManager;
                }
            }
        } else {
            return identitySCIMManager;
        }
    }

    public IdentitySCIMManager() throws CharonException {
        init();
    }

    public Encoder getEncoder(String format) throws FormatNotSupportedException {
        if (!encoderMap.containsKey(format)) {
            //Error is logged by the caller.
            throw new FormatNotSupportedException(ResponseCodeConstants.CODE_FORMAT_NOT_SUPPORTED,
                                                  ResponseCodeConstants.DESC_FORMAT_NOT_SUPPORTED);
        }
        return encoderMap.get(format);
    }

    public Decoder getDecoder(String format) throws FormatNotSupportedException {
        if (!decoderMap.containsKey(format)) {
            //Error is logged by the caller.
            throw new FormatNotSupportedException(ResponseCodeConstants.CODE_FORMAT_NOT_SUPPORTED,
                                                  ResponseCodeConstants.DESC_FORMAT_NOT_SUPPORTED);
        }
        return decoderMap.get(format);

    }

    public AuthenticationHandler getAuthenticationHandler(String authMechanism)
            throws CharonException {
        if (authenticators.size() != 0) {
            Map authenticatorProperties = authenticators.get(authMechanism);
            if (authenticatorProperties != null && authenticatorProperties.size() != 0) {
                return (AuthenticationHandler) authenticatorProperties.get(INSTANCE);
            }
        }
        String error = "Requested authentication mechanism is not supported.";
        throw new CharonException(error);
    }

    public UserManager getUserManager(String userName) throws CharonException {
        SCIMUserManager scimUserManager = null;
        //TODO:identify tenant id by username
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        try {
            //get super tenant context and get realm service which is an osgi service
            RealmService realmService = (RealmService)
                    SuperTenantCarbonContext.getCurrentContext().getOSGiService(RealmService.class);
            if (realmService != null) {
                int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                //UserRealm userRealm = realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID);
                UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                if (userRealm != null) {
                    scimUserManager = new SCIMUserManager(userRealm.getUserStoreManager());
                }
            } else {
                String error = "Can not obtain carbon realm service..";
                log.error(error);
                throw new CharonException(error);
            }
            //get user store manager
        } catch (UserStoreException e) {
            e.printStackTrace();
        }
        return scimUserManager;
    }

    public TenantManager getTenantManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AuthenticationInfo registerTenant(TenantDTO tenantDTO) throws CharonException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAuthenticationSupported(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AuthenticationInfo handleAuthentication(Map<String, String> authHeaderMap)
            throws UnauthorizedException {
        AuthenticationInfo authInfo = null;
        try {
            String authType = identifyAuthType(authHeaderMap);
            Map authPropertyMap = authenticators.get(authType);
            if (authHeaderMap != null) {
                AuthenticationHandler authHandler = (AuthenticationHandler) authPropertyMap.get(INSTANCE);
                if (authHandler != null) {
                    authHandler.setCharonManager(this);
                    authHandler.isAuthenticated(authHeaderMap);
                    return authHandler.getAuthenticationInfo();
                }
            }
        } catch (CharonException e) {
            throw new UnauthorizedException();
        }
        throw new UnauthorizedException();
    }

    /**
     * Register encoders and decoders in AbstractResourceEndpoint.
     */
    private void registerCoders() throws CharonException {
        if (!encoderMap.isEmpty()) {
            for (Map.Entry<String, Encoder> encoderEntry : encoderMap.entrySet()) {
                AbstractResourceEndpoint.registerEncoder(encoderEntry.getKey(), encoderEntry.getValue());
            }
        }
        if (!encoderMap.isEmpty()) {
            for (Map.Entry<String, Decoder> decoderEntry : decoderMap.entrySet()) {
                AbstractResourceEndpoint.registerDecoder(decoderEntry.getKey(), decoderEntry.getValue());
            }
        }

    }

    private void registerEndpointURLs() {
        if (endpointURLs != null && endpointURLs.size() != 0) {
            AbstractResourceEndpoint.registerResourceEndpointURLs(endpointURLs);
        }
    }

    /**
     * Identify the authentication mechanism, given the http headers sent in the SCIM API access request.
     *
     * @param authHeaders
     * @return
     * @throws CharonException
     */
    public String identifyAuthType(Map<String, String> authHeaders)
            throws CharonException, UnauthorizedException {
        String authorizationHeader = authHeaders.get(SCIMConstants.AUTHORIZATION_HEADER);
        String authenticationType = null;
        if (authorizationHeader != null) {
            authenticationType = authorizationHeader.split(" ")[0];
        } else {
            String error = "No Authorization header found";
            log.error(error);
            throw new UnauthorizedException();
        }
        if (SCIMConstants.AUTH_TYPE_BASIC.equals(authenticationType)) {
            return SCIMConstants.AUTH_TYPE_BASIC;
        } else if (SCIMConstants.AUTH_TYPE_OAUTH.equals(authenticationType)) {
            return SCIMConstants.AUTH_TYPE_OAUTH;
        } else if (authHeaders.get(SCIMConstants.AUTHENTICATION_TYPE_HEADER) != null) {
            return authHeaders.get(SCIMConstants.AUTHENTICATION_TYPE_HEADER);
        } else {
            String error = "Provided authentication headers do not contain supported authentication headers.";
            throw new CharonException(error);
        }
    }
}
