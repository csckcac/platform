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
package org.wso2.carbon.rssmanager.core.internal.manager;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.Database;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.RSSInstance;

import java.util.HashMap;
import java.util.Map;

public class RSSMetaDataRepository {

    private int tenantId;

    private Map<String, RSSInstance> rssInstances;

    private Map<MultiKey, Database> databases;

    private Map<MultiKey, DatabaseUser> databaseUsers;

    private Map<String, DatabasePrivilegeTemplate> privilegeTemplates;

    private RSSDAO dao;

    public RSSMetaDataRepository(int tenantId) {
        this.tenantId = tenantId;
        this.dao = RSSDAOFactory.getRSSDAO();
        this.rssInstances = new HashMap<String, RSSInstance>();
        this.databases = new HashMap<MultiKey, Database>();
        this.databaseUsers = new HashMap<MultiKey, DatabaseUser>();
        this.privilegeTemplates = new HashMap<String, DatabasePrivilegeTemplate>();
    }

    public void initRepository() throws RSSManagerException {
        for (RSSInstance rssIns : this.getDAO().getAllRSSInstances(this.getTenantId())) {
            this.addRSSInstance(rssIns);
        }
        for (Database database : this.getDAO().getAllDatabases(this.getTenantId())) {
            this.addDatabase(database);
        }
        for (DatabaseUser user : this.getDAO().getAllDatabaseUsers(this.getTenantId())) {
            this.addDatabaseUser(user);
        }
        for (DatabasePrivilegeTemplate template : this.getDAO().getAllDatabasePrivilegesTemplates(
                this.getTenantId())) {
            this.addPrivilegeTemplate(template);
        }
    }

    public int getTenantId() {
        return tenantId;
    }

    private RSSDAO getDAO() {
        return dao;
    }

    public Map<String, RSSInstance> getRSSInstances() {
        return rssInstances;
    }

    public Map<MultiKey, Database> getDatabases() {
        return databases;
    }

    public Map<MultiKey, DatabaseUser> getDatabaseUsers() {
        return databaseUsers;
    }

    public Map<String, DatabasePrivilegeTemplate> getPrivilegeTemplates() {
        return privilegeTemplates;
    }

    public void addRSSInstance(RSSInstance rssInstance) {
        this.getRSSInstances().put(rssInstance.getName(), rssInstance);
    }

    public void addDatabase(Database database) {
        this.getDatabases().put(
                new MultiKey(database.getRssInstanceName(), database.getName()), database);
    }

    public void addDatabaseUser(DatabaseUser user) {
        this.getDatabaseUsers().put(
                new MultiKey(user.getRssInstanceName(), user.getUsername()), user);
    }

    public void addPrivilegeTemplate(DatabasePrivilegeTemplate template) {
        this.getPrivilegeTemplates().put(template.getName(), template);
    }

    public RSSInstance getRSSInstance(String rssInstanceName) {
        return this.getRSSInstances().get(rssInstanceName);
    }

    public Database getDatabase(String rssInstanceName, String databaseName) {
        return this.getDatabases().get(new MultiKey(rssInstanceName, databaseName));
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName, String username) {
        return this.getDatabaseUsers().get(new MultiKey(rssInstanceName, username));
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(String templateName) {
        return this.getPrivilegeTemplates().get(templateName);
    }

}
