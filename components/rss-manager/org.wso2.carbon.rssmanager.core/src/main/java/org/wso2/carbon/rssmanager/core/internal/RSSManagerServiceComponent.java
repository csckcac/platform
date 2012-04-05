/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.rssmanager.core.multitenancy.RSSManagerAxis2ConfigObserver;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class activates the admin.console bundle
 *
 * @scr.component name="admin.console.url.service" immediate="true"
 * @scr.reference name="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * interface="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * cardinality="1..1" policy="dynamic" bind="setCarbonDataSourceService"
 * unbind="unsetCarbonDataSourceService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1" policy="dynamic"
 * bind="setRealmService" unbind="unsetRealmService"
 */
public class RSSManagerServiceComponent {

    private static Log log = LogFactory.getLog(RSSManagerServiceComponent.class);
    
    private static DataSourceInformationRepositoryService dataSourceService;

    private static RealmService realmService;

    /**
     * Activates the bundle.
     *
     * @param componentContext ComponentContext
     */
    protected void activate(ComponentContext componentContext) {
        RSSManagerBundleActivator RSSManagerBundleActivator;
        BundleContext bundleContext = componentContext.getBundleContext();

        try {
            RSSManagerBundleActivator = new RSSManagerBundleActivator();
            RSSManagerBundleActivator.start(bundleContext);

            /* Loading tenant specific data */
            bundleContext.registerService(AbstractAxis2ConfigurationContextObserver.class.getName(),
                    new RSSManagerAxis2ConfigObserver(), null);

        } catch (Throwable e) {
            String msg = "Failed To Register Admin Console Bundle As An OSGi Service";
            log.error(msg, e);
        }
    }

    /**
     * Deactivates the bundle. The content of this method is intentionally left blank as the
     * underlying OSGi layer handles the corresponding task.
     *
     * @param componentContext ComponentContext
     */
    protected void deactivate(ComponentContext componentContext) {

    }

    /**
     * Sets CarbonDataSourceService service.
     * @param dataSourceService Carbon data source service.
     */
    protected void setCarbonDataSourceService(
            DataSourceInformationRepositoryService dataSourceService) {
        RSSManagerServiceComponent.dataSourceService = dataSourceService;
    }

    /**
     * Unsets CarbonDataSourceService service.
     * @param dataSourceService Carbon data source service.
     */
    protected void unsetCarbonDataSourceService(
            DataSourceInformationRepositoryService dataSourceService) {
        RSSManagerServiceComponent.dataSourceService = null;
    }

    /**
     * Returns CarbonDataSourceService service.
     * @return Carbon data source service.
     */
    public static DataSourceInformationRepositoryService getCarbonDataSourceService() {
        return dataSourceService;
    }

    /**
     * Exposes the current realm service
     * @return realmService
     */
    public static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Sets Realm Service
     * @param realmService associated realm service
     */
    protected void setRealmService(RealmService realmService) {
        RSSManagerServiceComponent.realmService = realmService;
    }

    /**
     * Unsets Realm Service
     * @param realmService associated realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    /**
     * Retrieves the associated TenantManager
     * @return TenantManager
     */
    public static TenantManager getTenantManager() {
        return realmService.getTenantManager();
    }

    /**
     * Retrieves the Bootstrap Realm Configuration
     * @return RealmConfiguration
     */
    public static RealmConfiguration getBootstrapRealmConfiguration() {
        return realmService.getBootstrapRealmConfiguration();
    }

}
