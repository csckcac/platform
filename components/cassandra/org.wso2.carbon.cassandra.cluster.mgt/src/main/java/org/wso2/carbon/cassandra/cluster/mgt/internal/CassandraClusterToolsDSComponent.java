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
package org.wso2.carbon.cassandra.cluster.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cassandra.cluster.CassandraClusterToolsMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.CassandraClusterToolsAdminComponentManager;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.cassandra.cluster.mgt.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.cassandra.cluster.component"
 * interface="org.wso2.carbon.cassandra.cluster.CassandraClusterToolsMBeanDataAccess" cardinality="1..1"
 * policy="dynamic" bind="setCassandraClusterToolsMBeanDataAccess" unbind="unSetCassandraClusterToolsMBeanDataAccess"
 */
public class CassandraClusterToolsDSComponent {
    private static Log log = LogFactory.getLog(CassandraClusterToolsDSComponent.class);

    private CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Cluster Admin bundle is activated.");
        }
        CassandraClusterToolsAdminComponentManager.getInstance().init(cassandraClusterToolsMBeanDataAccess);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Cluster Admin bundle is deactivated.");
        }
        CassandraClusterToolsAdminComponentManager.getInstance().destroy();
    }
    protected void setCassandraClusterToolsMBeanDataAccess(CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess) {
        this.cassandraClusterToolsMBeanDataAccess= cassandraClusterToolsMBeanDataAccess;
    }

    protected void unSetCassandraClusterToolsMBeanDataAccess(CassandraClusterToolsMBeanDataAccess cassandraClusterToolsMBeanDataAccess) {
        this.cassandraClusterToolsMBeanDataAccess = null;
    }
}
