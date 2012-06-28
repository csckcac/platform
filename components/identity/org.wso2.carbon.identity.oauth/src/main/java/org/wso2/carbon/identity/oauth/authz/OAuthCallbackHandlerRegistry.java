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

package org.wso2.carbon.identity.oauth.authz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import javax.security.auth.callback.Callback;
import java.util.*;

/**
 * This is used to maintain the list of OAuthCallback Handlers registered in the system. It handles
 * <Code>OAuthAuthorizationCallbackHandler</Code>, etc which are called during the OAuth token
 * issuance process.
 */
public class OAuthCallbackHandlerRegistry {

    private static Log log = LogFactory.getLog(OAuthCallbackHandlerRegistry.class);

    private static OAuthCallbackHandlerRegistry instance = new OAuthCallbackHandlerRegistry();

    private transient boolean initAuthzHandlers = false;

    private Set<OAuthAuthorizationCallbackHandlerMetaData> callbackHandlerMetaData =
            new HashSet<OAuthAuthorizationCallbackHandlerMetaData>();

    private OAuthAuthorizationCallbackHandler[] authzCallbackHandlers;

    /**
     * Comparator for OAuthAuthorizationCallbackHandler based on their priority.
     */
    private class OAuthAuthzCbHandlerComparator
            implements Comparator<OAuthAuthorizationCallbackHandler> {

        public int compare(OAuthAuthorizationCallbackHandler o1,
                           OAuthAuthorizationCallbackHandler o2) {
            return o1.getPriority() - o2.getPriority();
        }
    }

    private OAuthCallbackHandlerRegistry() {

    }

    public static OAuthCallbackHandlerRegistry getInstance() {
        return instance;
    }

    public void addOAuthAuthorizationCallbackHandlerMetadata(
            OAuthAuthorizationCallbackHandlerMetaData metaData) {
        callbackHandlerMetaData.add(metaData);
    }

    /**
     * Initialize the OAuthAuthorizationCallbackHandlers. This is a one-time operation.
     *
     * @throws IdentityOAuth2Exception Error when instantiating the
     *                                 OAuthAuthorizationCallbackHandler instances
     */
    public void initAuthzCallbackHandlers() throws IdentityOAuth2Exception {
        if (!initAuthzHandlers) {
            synchronized (this) {
                if (!initAuthzHandlers) {
                    log.debug("initializing the OAuth Authorization Callback Handlers.");
                    List<OAuthAuthorizationCallbackHandler> oauthAuthzHandlers =
                            new ArrayList<OAuthAuthorizationCallbackHandler>();

                    // create an object of each OAuthAuthorizationCallbackHandler registered.
                    for (OAuthAuthorizationCallbackHandlerMetaData metaData : callbackHandlerMetaData) {

                        String className = metaData.getClassName();
                        Class clazz;
                        OAuthAuthorizationCallbackHandler callbackHandler;

                        try {
                            clazz = Thread.currentThread().getContextClassLoader().loadClass(
                                    metaData.getClassName());
                            callbackHandler = (OAuthAuthorizationCallbackHandler) clazz.newInstance();
                            callbackHandler.setPriority(metaData.getPriority());
                            callbackHandler.setProperties(metaData.getProperties());
                            oauthAuthzHandlers.add(callbackHandler);

                            if (log.isDebugEnabled()) {
                                log.debug("Instantiated an OAuth Authorization Callback Handler." +
                                        " Class : " + clazz.getName());
                            }

                        } catch (ClassNotFoundException e) {
                            String errorMsg = "Error when loading the OAuthAuthorizationCallbackHandler : "
                                    + className;
                            log.error(errorMsg, e);
                            throw new IdentityOAuth2Exception(errorMsg, e);
                        } catch (InstantiationException e) {
                            String errorMsg = "Error when instantiating the OAuthAuthorizationCallbackHandler : "
                                    + className;
                            log.error(errorMsg, e);
                            throw new IdentityOAuth2Exception(errorMsg, e);
                        } catch (IllegalAccessException e) {
                            String errorMsg = "Error when instantiating the OAuthAuthorizationCallbackHandler : "
                                    + className;
                            log.error(errorMsg, e);
                            throw new IdentityOAuth2Exception(errorMsg, e);
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Finished initializing OAuth Authorization Callback Handlers. " +
                                "No. of Authz Handlers registered : " + oauthAuthzHandlers.size());
                    }

                    authzCallbackHandlers = oauthAuthzHandlers.toArray(
                            new OAuthAuthorizationCallbackHandler[oauthAuthzHandlers.size()]);
                    // sort the OAuth Authorization Handlers based on priorities.
                    Arrays.sort(authzCallbackHandlers, new OAuthAuthzCbHandlerComparator());
                    initAuthzHandlers = true;
                }
            }
        }
    }

    /**
     * Get the appropriate <Code>OAuthAuthorizationCallbackHandler</Code> for the given callback
     *
     * @param authzCallback <Code>OAuthAuthorizationCallback</Code> object
     * @return <Code>OAuthAuthorizationCallbackHandler</Code> instance which can handle the
     *         given callback, return <Code>null</Code> if there is no OAuthAuthorizationCallbackHandler which
     *         can handle the given callback
     * @throws IdentityOAuth2Exception Error while evaluating the canHandle method
     */
    public OAuthAuthorizationCallbackHandler getOAuthAuthzHandler(
            OAuthAuthorizationCallback authzCallback) throws IdentityOAuth2Exception {

        for (OAuthAuthorizationCallbackHandler oauthAuthzCbHandler : authzCallbackHandlers) {
            if (oauthAuthzCbHandler.canHandle(new Callback[]{authzCallback})) {
                if (log.isDebugEnabled()) {
                    log.debug("OAuthAuthorizationCallbackHandler was found for the callback." +
                            " Class Name : " + oauthAuthzCbHandler.getClass().getName() +
                            " Resource Owner : " + authzCallback.getResourceOwner() +
                            " Client Id : " + authzCallback.getClient() +
                            " Scope : " + OAuth2Util.buildScopeString(authzCallback.getScope()));
                }
                return oauthAuthzCbHandler;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No OAuthAuthorizationCallbackHandlers were found for the callback." +
                    " Resource Owner : " + authzCallback.getResourceOwner() +
                    " Client Id : " + authzCallback.getClient() +
                    " Scope : " + OAuth2Util.buildScopeString(authzCallback.getScope()));
        }
        return null;
    }
}
