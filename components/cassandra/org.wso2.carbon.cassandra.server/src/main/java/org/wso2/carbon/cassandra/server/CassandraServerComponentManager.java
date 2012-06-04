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
/**
 * Keeps the services required by this component
 */
package org.wso2.carbon.cassandra.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.base.api.ServerConfigurationService;

/**
 * Keep and manage the services required for operation of this component
 */
public class CassandraServerComponentManager {

    private static Log log = LogFactory.getLog(CassandraServerComponentManager.class);

    private static CassandraServerComponentManager ourInstance = new CassandraServerComponentManager();

    private RealmService realmService;
    private AuthenticationService authenticationService;
    private ServerConfigurationService serverConfigurationService;
    private boolean initialized = false;

    public static CassandraServerComponentManager getInstance() {
        return ourInstance;
    }

    private CassandraServerComponentManager() {
    }

    /**
     * Initialize by giving required services
     *
     * @param realmService          realm
     * @param authenticationService authentication service
     * @param serverConfigurationService
     */
    public void init(RealmService realmService,
                     AuthenticationService authenticationService, ServerConfigurationService serverConfigurationService) {
        this.realmService = realmService;
        this.authenticationService = authenticationService;
        this.serverConfigurationService = serverConfigurationService;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new CassandraServerException("Cassandra server Component has not been initialized", log);
        }
    }

    public AuthenticationService getAuthenticationService() {
        assertInitialized();
        return authenticationService;
    }

    public UserRealm getRealmForTenant(String domainName) {
        assertInitialized();
        try {
            int tenantID = realmService.getTenantManager().getTenantId(domainName);
            return realmService.getTenantUserRealm(tenantID);
        } catch (UserStoreException e) {
            throw new CassandraServerException("Error accessing the UserRealm for super tenant : " + e, log);
        }
    }

    /**
     * Cleanup resources
     */
    public void destroy() {
        realmService = null;
        authenticationService = null;
        initialized = false;
    }
}
