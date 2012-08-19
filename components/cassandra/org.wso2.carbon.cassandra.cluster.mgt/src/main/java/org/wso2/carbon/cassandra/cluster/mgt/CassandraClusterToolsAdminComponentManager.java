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
package org.wso2.carbon.cassandra.cluster.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cassandra.cluster.CassandraClusterToolsMBeanDataAccess;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class CassandraClusterToolsAdminComponentManager {
    private static Log log = LogFactory.getLog(CassandraClusterToolsAdminComponentManager.class);

    private static CassandraClusterToolsAdminComponentManager ourInstance = new CassandraClusterToolsAdminComponentManager();
    /* For accessing Cassandra MBeans */
    private CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess;
    private boolean initialized = false;


    public static CassandraClusterToolsAdminComponentManager getInstance() {
        return ourInstance;
    }

    private CassandraClusterToolsAdminComponentManager() {
    }

    public void init(CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess) {
        this.cassandraClusterToolsMBeanDataAccess = cassandraClusterToolsMBeanDataAccess;
        this.initialized=true;

    }

    public CassandraClusterToolsMBeanDataAccess getCassandraClusterToolsMBeanDataAccess() throws CassandraClusterToolsDataAdminException {
        assertInitialized();
        return cassandraClusterToolsMBeanDataAccess;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() throws CassandraClusterToolsDataAdminException {
        if (!initialized) {
            throw new CassandraClusterToolsDataAdminException("Cassandra Admin Component has not been initialized.... ", log);
        }
    }

    /**
     * Cleanup resources
     */
    public void destroy() {
     cassandraClusterToolsMBeanDataAccess= null;
    }
}
