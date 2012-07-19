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
import org.apache.tomcat.jdbc.pool.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSMetaDataRepository;
import org.wso2.carbon.rssmanager.core.internal.util.RSSConfig;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This class activates the RSS manager core bundle
 *
 * @scr.component name="rss.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="datasources.service"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 */
public class RSSManagerServiceComponent {

    private static Log log = LogFactory.getLog(RSSManagerServiceComponent.class);

    private static DataSourceService dataSourceService;

    private static RealmService realmService;


    /**
     * Activates the RSS Manager Core bundle.
     *
     * @param componentContext ComponentContext
     */
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();

        SuperTenantCarbonContext.startTenantFlow();
        CarbonContextHolder.getCurrentCarbonContextHolder().setTenantId(
                MultitenantConstants.SUPER_TENANT_ID);
        try {
            /* Initializes the RSS configuration */
            RSSConfig.init();
            /* Initializes the system RSS instances */
            this.initSystemRSSInstances();
            /* Initializes the RSS Manager repositories of all existing tenants */
            RSSConfig.getInstance().getRssManager().initAllTenants();

            /* Loading tenant specific data */
            bundleContext.registerService(AbstractAxis2ConfigurationContextObserver.class.getName(),
                    new RSSManagerAxis2ConfigContextObserver(), null);
            /* Registers RSSManager service */
//            bundleContext.registerService(RSSManagerService.class.getName(),
//                    new RSSManagerService(), null);

        } catch (Throwable e) {
            String msg = "Error occurred while initializing RSS Manager core bundle";
            log.error(msg, e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    /**
     * Deactivates the bundle. The content of this method is intentionally left blank as the
     * underlying OSGi layer handles the corresponding task.
     *
     * @param componentContext ComponentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        try {
            ((DataSource) (RSSConfig.getInstance().getDataSource())).close();
        } catch (RSSManagerException e) {
            log.error("Error occurred while closing the database connection pool used by the " +
                    "RSSConfig", e);
        }
    }

//    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
//        return RSSManagerServiceComponent.secretCallbackHandlerService;
//    }
//
//    protected void setSecretCallbackHandlerService(
//            SecretCallbackHandlerService secretCallbackHandlerService) {
//        if (log.isDebugEnabled()) {
//            log.debug("SecretCallbackHandlerService acquired");
//        }
//        RSSManagerServiceComponent.secretCallbackHandlerService = secretCallbackHandlerService;
//    }
//
//    protected void unsetSecretCallbackHandlerService(
//            SecretCallbackHandlerService secretCallbackHandlerService) {
//        RSSManagerServiceComponent.secretCallbackHandlerService = null;
//    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Data Sources Service");
        }
        RSSManagerServiceComponent.dataSourceService = dataSourceService;
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Data Sources Service");
        }
        RSSManagerServiceComponent.dataSourceService = null;
    }

    /**
     * Provides access to DataSource service
     *
     * @return DataSourceService instance
     */
    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    /**
     * Exposes the current realm service
     *
     * @return realmService
     */
    private static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Sets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void setRealmService(RealmService realmService) {
        RSSManagerServiceComponent.realmService = realmService;
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    /**
     * Retrieves the associated TenantManager
     *
     * @return TenantManager
     */
    public static TenantManager getTenantManager() {
        return getRealmService().getTenantManager();
    }

    /**
     * Initialises the RSS DAO database by reading from the "wso2-rss-config.xml".
     *
     * @throws org.wso2.carbon.rssmanager.core.RSSManagerException
     *          rssDaoException
     */
    private void initSystemRSSInstances() throws RSSManagerException {
        int tid = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        try {
            RSSConfig config = RSSConfig.getInstance();
            RSSMetaDataRepository repository =
                    config.getRssManager().getTenantMetadataRepository(tid);
            if (repository == null) {
                repository = new RSSMetaDataRepository(tid);
            }

            /* adds the rss instances listed in the configuration file,
             * if any of them are already existing in the database, they will be skipped */
            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
            for (RSSInstance tmpInst : config.getSystemRSSInstances()) {
                rssInstances.put(tmpInst.getName(), tmpInst);
            }
            RSSDAO rssDAO = RSSDAOFactory.getRSSDAO();
            for (RSSInstance tmpInst : rssDAO.getAllSystemRSSInstances()) {
                rssInstances.remove(tmpInst.getName());
            }
            for (RSSInstance inst : rssInstances.values()) {
                rssDAO.createRSSInstance(inst);
                repository.addRSSInstance(inst);
            }
            config.getRssManager().setMetaDataRepository(tid, repository);
        } catch (RSSManagerException e) {
            log.error("Error occurred while initializing system RSS instances", e);
            throw e;
        }
    }

}
