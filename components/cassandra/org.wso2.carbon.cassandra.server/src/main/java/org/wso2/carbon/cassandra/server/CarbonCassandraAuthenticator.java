/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.server;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.thrift.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.core.CacheEntry;
import org.wso2.carbon.caching.core.StringCacheEntry;
import org.wso2.carbon.caching.core.StringCacheKey;
import org.wso2.carbon.cassandra.server.cache.UserAccessKeyCache;
import org.wso2.carbon.identity.authentication.AuthenticationService;

import java.util.Collections;
import java.util.Map;

/**
 * Carbon's authentication based implementation for the Cassandra's <coe>IAuthenticator</code>
 * This can be used in both a MT environment and a normal Carbon plugin. For the former case, a user have to provide
 * his or her name in the form of name@domainname (e.g foo@bar.com)
 * TODO
 */
public class CarbonCassandraAuthenticator implements IAuthenticator {

    private static final Log log = LogFactory.getLog(CarbonCassandraAuthenticator.class);

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private AuthenticationService authenticationService;
    private static final UserAccessKeyCache cache = UserAccessKeyCache.getInstance();

    /**
     * @return null as a user must call login().
     */
    public AuthenticatedUser defaultUser() {
        return null; // A user must log-in to the Cassandra
    }

	public static void addToCache(String username, String accessKey) {
 		cache.addToCache(new StringCacheKey(username), new StringCacheEntry(accessKey));

	}
    /**
     * Validate the user's credentials and Call the Authentication plugin for checking permission for log-in to the
     * Cassandra.
     *
     * @param credentials a user's credentials
     * @return <code>AuthenticatedUser<code> representing a successful authentication
     * @throws AuthenticationException if the authentication is failed
     */
    public AuthenticatedUser authenticate(Map<? extends CharSequence, ? extends CharSequence> credentials) throws AuthenticationException {

        String domainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

        CharSequence user = credentials.get(USERNAME_KEY);
        if (null == user) {
            logAndAuthenticationException("Authentication request was missing the required " +
                    "key '" + USERNAME_KEY + "'");
        }
        assert user != null;

        String userName = user.toString();
        if (userName.indexOf("@") > 0) {
            domainName = userName.substring(userName.indexOf("@") + 1);
            if (domainName == null || domainName.trim().equals("")) {
                logAndAuthenticationException("Authentication request was missing the domain name of the user in" +
                        " the key " + USERNAME_KEY);
            }
        }

        CharSequence pass = credentials.get(PASSWORD_KEY);
        if (null == pass) {
            logAndAuthenticationException("Authentication request was missing the required" +
                    " key '" + PASSWORD_KEY + "'");
        }
        assert pass != null;

        String password = pass.toString();

        if (isAuthenticated(userName, password)) {
        	AuthenticatedUser authenticatedUser = new AuthenticatedUser(userName,
                    Collections.<String>emptySet(), domainName);
            return authenticatedUser;
        } else if (authenticationService.authenticate(userName, password)) {
            CarbonCassandraAuthenticator.addToCache(userName, password);
            if (log.isDebugEnabled()) {
                log.debug("Credentials for Username : " + userName + " added to cache");
            }
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(userName,
                    Collections.<String>emptySet(), domainName);
            return authenticatedUser;
        }

        return null;  //
    }

    /**private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }**/
    
	private boolean isAuthenticated(String username, String keyAccess) {

        CacheEntry cacheEntry = cache.getValueFromCache(new StringCacheKey(username));
        String value=null;
        if(cacheEntry != null){
           value = ((StringCacheEntry)cacheEntry).getStringValue();
        }else {
            log.error("The key is not present in the cache...");
            if (log.isDebugEnabled()) {
                log.debug("Credentials for Username : " + username + " retrieved from cache");
            }
        }
		if (keyAccess != null && keyAccess.equals(value)) {
			return true;
		}
		return false;
	}

    public void validateConfiguration() throws ConfigurationException {
        CassandraServerComponentManager manager = CassandraServerComponentManager.getInstance();
        authenticationService = manager.getAuthenticationService();
    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }
}
