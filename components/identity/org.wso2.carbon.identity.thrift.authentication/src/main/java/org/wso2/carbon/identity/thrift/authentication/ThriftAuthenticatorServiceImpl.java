/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.thrift.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.ThriftSession;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a utility class that performs authentication related functionalities
 * by talking to back end authentication service.
 */
public class ThriftAuthenticatorServiceImpl extends AbstractAdmin
        implements ThriftAuthenticatorService {

    private static final Log log = LogFactory.getLog(ThriftAuthenticatorServiceImpl.class);

    private static ThriftAuthenticatorServiceImpl instance = new ThriftAuthenticatorServiceImpl();
    //session timeout in milli seconds
    private static long thriftSessionTimeOut = 60000;
    private AuthenticationService authenticationService;
    private RealmService realmService;

    private Map<String, ThriftSession> authenticatedSessions =
            new ConcurrentHashMap<String, ThriftSession>();

    private ThriftAuthenticatorServiceImpl() {
        //start session invalidator thread
        //Runnable sessionInvalidator = new SessionInvalidator();
        //exec.submit(sessionInvalidator);
    }

    public static ThriftAuthenticatorServiceImpl getInstance() {
        return instance;
    }

    /*initialize the org.wso2.carbon.identity.authentication.AuthenticationService which is wrapped
    by this ThriftAuthenticationService.*/

    public void init(AuthenticationService authenticationService, RealmService realmService) {
        this.authenticationService = authenticationService;
        this.realmService = realmService;
    }
    //TODO: get remote address as an input to be used for logging purposes in OnSuccessAdminLogin method. 

    public String authenticate(String userName, String password) throws AuthenticationException {

        if (userName == null) {
            logAndAuthenticationException("Authentication request was missing the user name ");
        }

        if (userName.indexOf("@") > 0) {
            String domainName = userName.substring(userName.indexOf("@") + 1);
            if (domainName == null || domainName.trim().equals("")) {
                logAndAuthenticationException("Authentication request was missing the domain name of" +
                                              " the user");
            }
        }

        if (password == null) {
            logAndAuthenticationException("Authentication request was missing the required password");
        }
        //check whether the credentials are authenticated.
        boolean isSuccessful = authenticationService.authenticate(userName, password);

        if (isSuccessful) {
            //check if an already valid authenticated session exists for the given user name and password.
            for (Map.Entry<String, ThriftSession> thriftSessionEntry : authenticatedSessions.entrySet()) {

                if ((userName.equals(thriftSessionEntry.getValue().getUserName()))
                    && password.equals(thriftSessionEntry.getValue().getPassword())) {
                    //get relevant session id
                    String existingId = thriftSessionEntry.getKey();
                    //check whether session is valid
                    if (isSessionValid(existingId)) {
                        //update last access time
                        authenticatedSessions.get(existingId).setLastAccess(System.currentTimeMillis());
                        return thriftSessionEntry.getKey();
                    } else {
                        //session expired, remove the session from map
                        authenticatedSessions.remove(existingId);
                    }
                }
            }
            //if not, create a new session
            String sessionId = null;
            ThriftSession session = null;
            try {
                sessionId = UUID.randomUUID().toString();
                //populate thrift session
                session = new ThriftSession();
                session.setSessionId(sessionId);
                session.setUserName(userName);
                session.setPassword(password);
                session.setCreatedAt(System.currentTimeMillis());
                session.setLastAccess(System.currentTimeMillis());
                /*call onSuccessLogin in CarbonAuthenticationUtil to initialize registry for this
                thrift session*/
                if (realmService != null) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(userName);
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    CarbonAuthenticationUtil.onSuccessAdminLogin(session, userName, tenantId,
                                                                 tenantDomain, "");
                    /*call handleAuthenticationComplete to facilitate AuthenticationObservers*/
                    authenticatedSessions.put(sessionId, session);
                } else {
                    String errorMsg = "Realm service not properly set..";
                    log.error(errorMsg);
                    throw new AuthenticationException(errorMsg);
                }

            } catch (Exception e) {
                String errorMsg = "Error occured while authenticating the user: " + userName;
                log.error(errorMsg, e);
                throw new AuthenticationException(errorMsg);
            }
            return sessionId;
        } else {
            //TODO:call onFailedLogin: just for logging purposes
        }

        return null;

    }

    public boolean isAuthenticated(String sessionId) {

        if (sessionId == null) {
            return false;
        }

        if (authenticatedSessions.containsKey(sessionId)) {
            if (isSessionValid(sessionId)) {
                //update the last access time.
                authenticatedSessions.get(sessionId).setLastAccess(System.currentTimeMillis());
                return true;
            } else {
                //invalidate session
                authenticatedSessions.remove(sessionId);
                return false;
            }
        }
        return false;
    }

    public ThriftSession getSessionInfo(String sessionId) {
        return authenticatedSessions.get(sessionId);
    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }

    /**
     * Perform session invalidation to avoid replay attacks.
     */
    /*public class SessionInvalidator implements Runnable {

        public void run() {
            while (true) {
                try {
                    for (ThriftSession thriftSession : authenticatedSessions.values()) {
                        long currentTime = System.currentTimeMillis();
                        long createdTime = thriftSession.getCreatedAt();
                        if ((currentTime - createdTime) > 50000) {
                            authenticatedSessions.remove(thriftSession.getSessionId());
                        }
                    }
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
    private boolean isSessionValid(String sessionId) {
        if (authenticatedSessions.containsKey(sessionId)) {
            ThriftSession authSession = authenticatedSessions.get(sessionId);
            //check whether the session is expired.
            return ((System.currentTimeMillis() - (authSession.getLastAccess()) < thriftSessionTimeOut));
        }
        return false;
    }
}
