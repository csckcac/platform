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
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.GroupEventListener;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerServiceComponent;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;

import java.util.HashMap;
import java.util.Map;

public class RSSMetaDataRepository implements GroupEventListener {

    private int tenantId;

    private Map<String, RSSInstance> rssInstances;

    private Map<MultiKey, Database> databases;

    private Map<MultiKey, DatabaseUser> databaseUsers;

    private Map<String, DatabasePrivilegeTemplate> privilegeTemplates;

    private RSSDAO dao;

    private Group syncGroup;

    public RSSMetaDataRepository(int tenantId) {
        this.tenantId = tenantId;
        this.dao = RSSDAOFactory.getRSSDAO();
        this.rssInstances = new HashMap<String, RSSInstance>();
        this.databases = new HashMap<MultiKey, Database>();
        this.databaseUsers = new HashMap<MultiKey, DatabaseUser>();
        this.privilegeTemplates = new HashMap<String, DatabasePrivilegeTemplate>();
    }

    public void initRepository() throws RSSManagerException {
        this.initSyncGroup();
        this.loadMetadata();
    }

    private void loadMetadata() throws RSSManagerException {
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

    public void addRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        this.getRSSInstances().put(rssInstance.getName(), rssInstance);
        this.notifyClusterOfArtifactChange(rssInstance);
    }

    public void addDatabase(Database database) throws RSSManagerException {
        this.getDatabases().put(
                new MultiKey(database.getRssInstanceName(), database.getName()), database);
        this.notifyClusterOfArtifactChange(database);
    }

    public void addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        this.getDatabaseUsers().put(
                new MultiKey(user.getRssInstanceName(), user.getUsername()), user);
        this.notifyClusterOfArtifactChange(user);
    }

    public void addPrivilegeTemplate(DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        this.getPrivilegeTemplates().put(template.getName(), template);
        this.notifyClusterOfArtifactChange(template);
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

    private void initSyncGroup() throws RSSManagerException {
        if (!RSSManagerServiceComponent.getCoodrinationService().isEnabled()) {
            return;
        }
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(this.getTenantId());
            this.syncGroup =
                    RSSManagerServiceComponent.getCoodrinationService().createGroup(
                            RSSManagerConstants.RSS_MANAGER_SYNC_GROUP_NAME);
            this.syncGroup.setGroupEventListener(this);
        } catch (CoordinationException e) {
            String msg = "Error occurred while creating RSS Manager sync group: " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    private void notifyClusterOfArtifactChange(Object o) throws RSSManagerException {
        if (this.getSyncGroup() != null) {
            try {
                this.getSyncGroup().broadcast(o.toString().getBytes());
            } catch (CoordinationException e) {
                String msg = "Error occurred while notifying the cluster of the artifact change: " +
                        e.getMessage();
                throw new RSSManagerException(msg, e);
            }
        }
    }

    private Group getSyncGroup() {
        return syncGroup;
    }

    @Override
    public void onLeaderChange(String s) {

    }

    @Override
    public void onMemberArrival(String s) {

    }

    @Override
    public void onMemberDeparture(String s) {

    }

    @Override
    public void onGroupMessage(byte[] bytes) {

    }

    @Override
    public byte[] onPeerMessage(byte[] bytes) throws CoordinationException {
        throw new CoordinationException("RSS Manager does not handle group RPC",
                CoordinationException.ExceptionCode.GENERIC_ERROR);
    }
}
