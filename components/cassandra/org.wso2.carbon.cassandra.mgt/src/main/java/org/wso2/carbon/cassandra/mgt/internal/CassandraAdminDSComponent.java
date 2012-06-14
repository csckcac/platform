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
package org.wso2.carbon.cassandra.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.CassandraAdminComponentManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.api.ServerConfigurationService;

/**
 * @scr.component name="org.wso2.carbon.cassandra.mgt.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.cassandra.dataaccess.component"
 * interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService" cardinality="1..1"
 * policy="dynamic" bind="setDataAccessService" unbind="unSetDataAccessService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="org.wso2.carbon.base.api.ServerConfigurationService"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class CassandraAdminDSComponent {

    private static Log log = LogFactory.getLog(CassandraAdminDSComponent.class);

    private DataAccessService dataAccessService;
    private RealmService realmService;
    private static ConfigurationContextService configCtxService;
    private ServerConfigurationService serverConfigurationService;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Admin bundle is activated.");
        }
        CassandraAdminComponentManager.getInstance().init(dataAccessService, realmService,
                configCtxService,serverConfigurationService);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Admin bundle is deactivated.");
        }
        CassandraAdminComponentManager.getInstance().destroy();
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    protected void unSetDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = null;
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }
    
    public RealmService getRealmService() {
		return realmService;
	}

	protected void setConfigurationContext(ConfigurationContextService ctxService) {
    	CassandraAdminDSComponent.configCtxService = ctxService;
    }

    protected void unsetConfigurationContext(ConfigurationContextService ctxService) {
    	CassandraAdminDSComponent.configCtxService = null;
    }

	public static ConfigurationContextService getConfigCtxService() {
		return configCtxService;
	}

    public void unsetServerConfiguration(ServerConfigurationService serverConfigService){
        this.serverConfigurationService = null;
    }

    public void setServerConfiguration(ServerConfigurationService serverConfigService){
        this.serverConfigurationService = serverConfigService;
    }
}
