/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerServiceComponent;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.internal.util.RSSConfig;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;

import java.util.List;


public class RSSManagerService {

    private static final Log log = LogFactory.getLog(RSSManagerService.class);

    public void createRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        try {
            this.getRSSManager().createRSSInstance(rssInstance);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating RSS instance '" + rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public void dropRSSInstance(String rssInstanceName) throws RSSManagerException {
        try {
            this.getRSSManager().dropRSSInstance(rssInstanceName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the RSS instance '" + rssInstanceName + "'";
            handleException(msg, e);
        }
    }

    public void editRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        try {
            this.getRSSManager().editRSSInstanceConfiguration(rssInstance);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the configuration of RSS instance '" +
                    rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public RSSInstanceMetaData getRSSInstance(String rssInstanceName) throws RSSManagerException {
        RSSInstanceMetaData metadata = null;
        try {
            RSSInstance rssInstance =
                    this.getRSSManager().getRSSInstance(rssInstanceName);
            metadata = RSSManagerUtil.convertRSSInstanceToMetadata(rssInstance);
            return metadata;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of RSS instance '" +
                    rssInstanceName + "'";
            handleException(msg, e);
        }
        return metadata;
    }

    public RSSInstanceMetaData[] getRSSInstances() throws RSSManagerException {
        int tid = CarbonContext.getCurrentContext().getTenantId();
        RSSInstanceMetaData[] rssInstances = new RSSInstanceMetaData[0];
        try {
            List<RSSInstanceMetaData> tmpList =
                    this.getRSSManager().getRSSInstances(tid);
            rssInstances = tmpList.toArray(new RSSInstanceMetaData[tmpList.size()]);
        } catch (RSSManagerException e) {
            String tenantDomain = null;
            try {
                tenantDomain = RSSManagerUtil.getTenantDomainFromTenantId(tid);
            } catch (RSSManagerException e1) {
                log.error(e1);
            }
            String msg = "Error occurred in retrieving the RSS instance list of the tenant '" +
                    tenantDomain + "'";
            handleException(msg, e);
        }
        return rssInstances;
    }

    public void createDatabase(Database database) throws RSSManagerException {
        try {
            this.getRSSManager().createDatabase(database);
        } catch (RSSManagerException e) {
            String msg = "Error in creating the database '" + database.getName() + "'";
            handleException(msg, e);
        }
    }

    public void dropDatabase(String rssInstanceName, String databaseName) throws
            RSSManagerException {
        try {
            this.getRSSManager().dropDatabase(rssInstanceName, databaseName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database '" + databaseName + "'";
            handleException(msg, e);
        }
    }

    public DatabaseMetaData[] getDatabases() throws RSSManagerException {
        int tid = CarbonContext.getCurrentContext().getTenantId();
        DatabaseMetaData[] databases = new DatabaseMetaData[0];
        try {
            List<DatabaseMetaData> tmpList =
                    this.getRSSManager().getDatabases(tid);
            databases = tmpList.toArray(new DatabaseMetaData[tmpList.size()]);
        } catch (RSSManagerException e) {
            String tenantDomain = null;
            try {
                tenantDomain = RSSManagerUtil.getTenantDomainFromTenantId(tid);
            } catch (RSSManagerException e1) {
                log.error(e1);
            }
            String msg = "Error occurred while retrieving the database list of the tenant '" +
                    tenantDomain + "'";
            handleException(msg, e);
        }
        return databases;
    }

    public DatabaseMetaData getDatabase(String rssInstanceName, String databaseName) throws
            RSSManagerException {
        DatabaseMetaData medata = null;
        try {
            Database database =
                    this.getRSSManager().getDatabase(
                            rssInstanceName, databaseName);
            medata = RSSManagerUtil.convertDatabaseToMetadata(database);
            return medata;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of the database '" +
                    databaseName + "'";
            handleException(msg, e);
        }
        return medata;
    }

    public void createDatabaseUser(DatabaseUser user) throws
            RSSManagerException {
        try {
            this.getRSSManager().createDatabaseUser(user);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating the database user '" + user.getUsername() + "'";
            handleException(msg, e);
        }
    }

    public void dropDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
        try {
            this.getRSSManager().dropDatabaseUser(rssInstanceName, username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the user '" + username + "'";
            handleException(msg, e);
        }
    }

    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        this.getRSSManager().editDatabaseUserPrivileges(privileges, user, databaseName);
    }


    public DatabaseUserMetaData getDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException {
        DatabaseUserMetaData metadata = null;
        try {
            DatabaseUser user =
                    this.getRSSManager().getDatabaseUser(rssInstanceName,
                            username);
            metadata = RSSManagerUtil.convertToDatabaseUserMetadata(user);
            return metadata;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privileges of the user '" +
                    username + "'";
            handleException(msg, e);
        }
        return metadata;
    }

    public DatabaseUserMetaData[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUserMetaData[] users = new DatabaseUserMetaData[0];
        int tid = CarbonContext.getCurrentContext().getTenantId();
        try {
            List<DatabaseUserMetaData> tmpList =
                    this.getRSSManager().getDatabaseUsers(tid);
            users = tmpList.toArray(new DatabaseUserMetaData[tmpList.size()]);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database user list";
            handleException(msg, e);
        }
        return users;
    }

    public String getUserDatabasePermissions(String username, String databaseName) throws
            RSSManagerException {
        return null;
    }

    public void createDatabasePrivilegesTemplate(DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        try {
            this.getRSSManager().createDatabasePrivilegesTemplate(template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while creating the database privilege template '" +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException {
        try {
            this.getRSSManager().dropDatabasePrivilegesTemplate(templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database privilege template '" +
                    templateName + "'";
            handleException(msg, e);
        }
    }

    public void editDatabasePrivilegesTemplate(DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        try {
            this.getRSSManager().editDatabasePrivilegesTemplate(template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privilege template " +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates() throws RSSManagerException {
        List<DatabasePrivilegeTemplate> templates =
                this.getRSSManager().getDatabasePrivilegeTemplates();
        return templates.toArray(new DatabasePrivilegeTemplate[templates.size()]);
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(String templateName) throws
            RSSManagerException {
        return this.getRSSManager().getDatabasePrivilegeTemplate(
                templateName);
    }

    public void createCarbonDataSource(String databaseName, String username) throws
            RSSManagerException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        SuperTenantCarbonContext.startTenantFlow();
        SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

        Database database;
        DatabaseUser user;
        String dsName = null;
        try {
            database = dao.getDatabase(databaseName);
            user = dao.getDatabaseUser(username);

            DataSourceMetaInfo.DataSourceDefinition dsDef =
                    new DataSourceMetaInfo.DataSourceDefinition();
            dsDef.setDsXMLConfiguration(null);
            dsDef.setType(null);

            DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
            dsName = database.getName() + "_" + user.getUsername();
            metaInfo.setName(dsName);
            metaInfo.setDefinition(dsDef);

            RSSManagerServiceComponent.getDataSourceService().addDataSource(metaInfo);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while creating datasource'" +
                    username + "'";
            handleException(msg, e);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating the datasource '" + dsName + "'";
            handleException(msg, e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public int getSystemRSSInstanceCount() throws RSSManagerException {
        int count = 0;
        try {
            count = this.getRSSManager().getSystemRSSInstanceCount();
            return count;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the system RSS instance count";
            handleException(msg, e);
        }
        return count;
    }

    public void attachUserToDatabase(String rssInstanceName, String databaseName, String username,
                                     String templateName) throws RSSManagerException {
        try {
            this.getRSSManager().attachUserToDatabase(rssInstanceName, databaseName, username,
                    templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while attaching database user '" + username +
                    "' to the database '" + databaseName + "' with the database user privileges " +
                    "define in the database privilege template '" + templateName + "'";
            handleException(msg, e);
        }
    }

    public void detachUserFromDatabase(String rssInstanceName, String databaseName,
                                       String username) throws RSSManagerException {
        try {
            this.getRSSManager().detachUserFromDatabase(rssInstanceName, databaseName, username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while detaching the database user '" + username +
                    "' from the database '" + databaseName + "'";
            handleException(msg, e);
        }
    }

    public String[] getUsersAttachedToDatabase(String rssInstanceName,
                                               String databaseName) throws
            RSSManagerException {
        String[] users = new String[0];
        List<String> tmpList;
        try {
            tmpList =
                    this.getRSSManager().getUsersAttachedToDatabase(rssInstanceName, databaseName);
            users = tmpList.toArray(new String[tmpList.size()]);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users attached to " +
                    "the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }

    public String[] getAvailableUsersToAttachToDatabase(String rssInstanceName,
                                                        String databaseName) throws
            RSSManagerException {
        String[] users = new String[0];
        List<String> tmpList;
        try {
            tmpList =
                    this.getRSSManager().getAvailableUsersToAttachToDatabase(rssInstanceName,
                            databaseName);
            users = tmpList.toArray(new String[tmpList.size()]);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users available to be " +
                    "attached to the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }


    private RSSManager getRSSManager() throws RSSManagerException {
        RSSConfig config = RSSConfig.getInstance();
        if (config == null) {
            throw new RSSManagerException("RSSConfig is not properly initialized and is null");
        }
        return config.getRssManager();
    }

    public void createCarbonDataSource(UserDatabaseEntry entry) throws RSSManagerException {
        Database database = this.getRSSManager().getDatabase(entry.getRssInstanceName(),
                entry.getDatabaseName());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerServiceComponent.getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeSet getUserDatabasePermissions(
            String rssInstanceName, String databaseName, String username) throws RSSManagerException {
        DatabasePrivilegeSet privileges = null;
        try {
            privileges = this.getRSSManager().getUserDatabasePrivileges(
                    rssInstanceName, databaseName, username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the permissions granted to the user '" +
                    username + "' on database '" + databaseName + "'";
            handleException(msg, e);
        }
        return privileges;
    }

}
