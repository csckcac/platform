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
package org.wso2.carbon.bam.receiver.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.service.AuthenticationException;
import org.wso2.carbon.identity.authentication.AuthenticationService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThriftAuthenticator {

    private static final Log log = LogFactory.getLog(ThriftAuthenticator.class);

    private static ThriftAuthenticator instance = new ThriftAuthenticator();

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private AuthenticationService authenticationService;
    private Map<String, ThriftSession> authenticatedSessions =
            new ConcurrentHashMap<String, ThriftSession>();

    private ThriftAuthenticator() {
        
    }

    public static ThriftAuthenticator getInstance() {
        return instance;
    }

    public void init(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

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

        boolean isSuccessful = authenticationService.authenticate(userName, password);
        if (isSuccessful) {
            String sessionId = UUID.randomUUID().toString();

            ThriftSession session = new ThriftSession();
            session.setSessionId(sessionId);
            session.setUserName(userName);
            session.setPassword(password);
            session.setCreatedAt(System.currentTimeMillis());

            authenticatedSessions.put(sessionId, session);

            return sessionId;
        }

        return null;

    }

    public boolean isAuthenticated(String sessionId) {

        if (sessionId == null) {
            return false;
        }

        if (authenticatedSessions.containsKey(sessionId)) {
            return true;
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

    private class SessionInvalidator implements Runnable {

        public void run() {
                
        }
    }

}
