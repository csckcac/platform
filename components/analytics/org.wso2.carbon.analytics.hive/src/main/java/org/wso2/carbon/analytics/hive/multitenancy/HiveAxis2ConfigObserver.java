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
package org.wso2.carbon.analytics.hive.multitenancy;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabaseMetaData;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.entity.RSSInstanceMetaData;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class HiveAxis2ConfigObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(HiveAxis2ConfigObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = SuperTenantCarbonContext.getCurrentContext(
                configurationContext).getTenantId();
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            RSSManagerService rssManagerService = ServiceHolder.getRSSManagerService();
            RSSInstanceMetaData[] rssEntries = rssManagerService.getRSSInstances();

            String rssInstanceName = null;
            if (rssEntries != null) {
                for (RSSInstanceMetaData rssEntry : rssEntries) {
                    if (rssEntry.getName().equals(HiveConstants.HIVE_METASTORE_RSS_INSTANCE)) {
                        rssInstanceName = rssEntry.getName();
                    }
                }
            }

            HiveConf conf = new HiveConf();

            String url = conf.getVar(HiveConf.ConfVars.METASTORECONNECTURLKEY);
            String userName = conf.getVar(HiveConf.ConfVars.METASTORE_CONNECTION_USER_NAME);
            String password = conf.getVar(HiveConf.ConfVars.METASTOREPWD);
            if (rssInstanceName != null) {
                RSSInstance rssInstance = new RSSInstance();
                rssInstance.setName(HiveConstants.HIVE_METASTORE_RSS_INSTANCE);
                rssInstance.setAdminUsername(userName);
                rssInstance.setAdminPassword(password);
                rssInstance.setServerCategory("LOCAL");
                rssInstance.setServerURL(url);
                rssInstance.setDbmsType(url.split(":")[1]);

                rssManagerService.createRSSInstance(rssInstance);
                
            }

            boolean dbPresent = false;
            DatabaseMetaData[] databaseEntries = rssManagerService.getDatabases();
            for (DatabaseMetaData databaseEntry : databaseEntries) {
                if (databaseEntry.getName().contains(HiveConstants.HIVE_METASTORE_DB)) {
                    dbPresent = true;
                    break;
                }
            }

            if (!dbPresent) {
                Database db = new Database();
                db.setName(HiveConstants.HIVE_METASTORE_DB);
                db.setRssInstanceName(HiveConstants.HIVE_METASTORE_RSS_INSTANCE);
                db.setTenantId(tenantId);

                rssManagerService.createDatabase(db);
            }

        } catch (Exception e) {
            log.error("Error initializing tenant Hive meta store.. ", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

}
