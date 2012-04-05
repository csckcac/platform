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
package org.wso2.carbon.cassandra.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Keeps the runtime objects required by the operation of the cassandra admin service
 */

public class CassandraAdminComponentManager {

    private static Log log = LogFactory.getLog(CassandraAdminComponentManager.class);

    private static CassandraAdminComponentManager ourInstance = new CassandraAdminComponentManager();
    /* For accessing Cassandra clusters */
    private DataAccessService dataAccessService;
    /* For accessing cassandra(component) server configuration */
    private RealmService realmService;

    private boolean initialized = false;

    public static CassandraAdminComponentManager getInstance() {
        return ourInstance;
    }

    private CassandraAdminComponentManager() {
    }

    /**
     * Initialize with the required services
     *
     * @param dataAccessService client for accessing cassandra
     * @param realmService      Access the user realm
     */
    public void init(DataAccessService dataAccessService, RealmService realmService) {
        this.dataAccessService = dataAccessService;
        this.realmService = realmService;
        this.initialized = true;
    }

    public DataAccessService getDataAccessService() throws CassandraServerManagementException {
        assertInitialized();
        return dataAccessService;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() throws CassandraServerManagementException {
        if (!initialized) {
            throw new CassandraServerManagementException("Cassandra Admin Component has not been initialized", log);
        }
    }

    public UserRealm getRealmForCurrentTenant() throws CassandraServerManagementException {
        assertInitialized();
        try {
            return realmService.getTenantUserRealm(CarbonContext.getCurrentContext().getTenantId());
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error accessing the UserRealm for super tenant : " + e, log);
        }
    }

    /**
     * Cleanup resources
     */
    public void destroy() {
        realmService = null;
        dataAccessService = null;
        initialized = false;
    }
}
