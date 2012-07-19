package org.wso2.carbon.rssmanager.core.internal.manager;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.internal.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.*;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RSSManager {

    private Map<Integer, RSSMetaDataRepository> metadataRepositoryMap;

    private RSSDAO dao = RSSDAOFactory.getRSSDAO();

    public RSSManager() {
        this.metadataRepositoryMap = new ConcurrentHashMap<Integer, RSSMetaDataRepository>();
    }

    public abstract void createDatabase(Database database) throws RSSManagerException;

    public abstract void dropDatabase(String rssInstanceName, String databaseName) throws
            RSSManagerException;

    public abstract void createDatabaseUser(DatabaseUser databaseUser) throws RSSManagerException;

    public abstract void dropDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException;

    public abstract void editDatabaseUserPrivileges(DatabasePermissions permissions,
                                                    DatabaseUser databaseUser,
                                                    String databaseName) throws RSSManagerException;

    public abstract void attachUserToDatabase(String rssInstanceName, String databaseName,
                                              String username, String templateName) throws 
            RSSManagerException;

    public abstract void detachUserFromDatabase(String rssInstanceName, String databaseName,
                                              String username) throws RSSManagerException;

    public void initAllTenants() throws RSSManagerException {
        for (int tid : RSSManagerUtil.getAllTenants()) {
            this.initTenant(tid);
        }
    }

    public void initTenant(int tid) throws RSSManagerException {
        RSSMetaDataRepository repository = this.getMetadataRepositoryMap().get(tid);
        if (repository != null) {
            repository.initRepository();
        } else {
            repository = new RSSMetaDataRepository(tid);
            repository.initRepository();
            this.setMetaDataRepository(tid, repository);
        }
    }

    public void createRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        int tid = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        rssInstance.setTenantId(tid);
        this.getDAO().createRSSInstance(rssInstance);
        this.getTenantMetadataRepository(tid).addRSSInstance(rssInstance);
    }

    public void dropRSSInstance(String rssInstanceName) throws RSSManagerException {
        RSSInstance rssInstance = this.getRSSInstance(rssInstanceName);
        DataSource dataSource = (DataSource) rssInstance.getDataSource();
        if (dataSource != null) {
            dataSource.close();
        }
        this.getDAO().dropRSSInstance(rssInstanceName);
        //TODO : Drop dependent databases etc.
        this.getTenantMetadataRepository(this.getCurrentTenantId()).getRSSInstances().
                remove(rssInstanceName);
    }

    public void editRSSInstanceConfiguration(RSSInstance rssInstance) throws RSSManagerException {
        int tid = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        rssInstance.setTenantId(tid);
        this.getDAO().updateRSSInstance(rssInstance);
        this.getTenantMetadataRepository(tid).addRSSInstance(rssInstance);
    }

    public List<RSSInstanceMetaData> getRSSInstances(int tid) throws RSSManagerException {
        Collection<RSSInstance> tmpList =
                this.getTenantMetadataRepository(tid).getRSSInstances().values();
        List<RSSInstanceMetaData> rssInstances = new ArrayList<RSSInstanceMetaData>();
        for (RSSInstance tmpIns : tmpList) {
            RSSInstanceMetaData rssIns = RSSManagerUtil.convertRSSInstanceToMetadata(tmpIns);
            rssInstances.add(rssIns);
        }
        return rssInstances;
    }

    public List<DatabaseMetaData> getDatabases(int tid) throws RSSManagerException {
        List<DatabaseMetaData> databases = new ArrayList<DatabaseMetaData>();
        Collection<Database> tmpCol = this.getTenantMetadataRepository(tid).getDatabases().values();
        for (Database database : tmpCol) {
            DatabaseMetaData metadata = RSSManagerUtil.convertDatabaseToMetadata(database);
            databases.add(metadata);
        }
        return databases;
    }

    public List<DatabaseUserMetaData> getDatabaseUsers(int tid) throws RSSManagerException {
        Collection<DatabaseUser> tmpList =
                this.getTenantMetadataRepository(tid).getDatabaseUsers().values();
        List<DatabaseUserMetaData> users = new ArrayList<DatabaseUserMetaData>();
        for (DatabaseUser tmpUser : tmpList) {
            DatabaseUserMetaData user = RSSManagerUtil.convertToDatabaseUserMetadata(tmpUser);
            users.add(user);
        }
        return users;
    }

    public RSSInstance getRoundRobinAssignedDatabaseServer() throws
            RSSManagerException {
        RSSInstance rssIns = null;
        List<RSSInstance> rdsInstances = this.getDAO().getAllSystemRSSInstances();
        int count = this.getDAO().getSystemRSSDatabaseCount();

        for (int i = 0; i < rdsInstances.size(); i++) {
            if (i == count % rdsInstances.size()) {
                rssIns = rdsInstances.get(i);
                if (rssIns != null) {
                    return rssIns;
                }
            }
        }
        return rssIns;
    }

    public void createDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        int tid = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        this.getDAO().createDatabasePrivilegesTemplate(template);
        this.getTenantMetadataRepository(tid).addPrivilegeTemplate(template);
    }

    public void editDatabasePrivilegesTemplate(DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        int tid = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        template.setTenantId(tid);
        this.getDAO().editDatabasePrivilegesTemplate(template);
    }

    public RSSMetaDataRepository getTenantMetadataRepository(int tenantId) {
        return this.getMetadataRepositoryMap().get(tenantId);

    }

    public synchronized Map<Integer, RSSMetaDataRepository> getMetadataRepositoryMap() {
        return metadataRepositoryMap;
    }

    public void setMetaDataRepository(int tid, RSSMetaDataRepository repository) {
        this.getMetadataRepositoryMap().put(tid, repository);
    }

    public RSSInstance getRSSInstance(String rssInstanceName) {
        return this.getTenantMetadataRepository(this.getCurrentTenantId()).
                getRSSInstance(rssInstanceName);
    }

    public Database getDatabase(String rssInstanceName, String databaseName) {
        return this.getTenantMetadataRepository(this.getCurrentTenantId()).
                getDatabase(rssInstanceName, databaseName);
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName, String username) {
        return this.getTenantMetadataRepository(this.getCurrentTenantId()).
                getDatabaseUser(rssInstanceName, username);
    }

    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException {
        this.getDAO().dropDatabasePrivilegesTemplate(templateName);
        this.getTenantMetadataRepository(this.getCurrentTenantId()).getPrivilegeTemplates().
                remove(templateName);
    }

    public List<DatabasePrivilegeTemplate> getDatabasePrivilegeTemplates() {
        List<DatabasePrivilegeTemplate> templates = new ArrayList<DatabasePrivilegeTemplate>();
        Collection<DatabasePrivilegeTemplate> tmpCol =
                this.getTenantMetadataRepository(this.getCurrentTenantId()).
                        getPrivilegeTemplates().values();
        templates.addAll(tmpCol);
        return templates;
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(String templateName) {
        return this.getTenantMetadataRepository(this.getCurrentTenantId()).
                getDatabasePrivilegeTemplate(templateName);
    }

    protected int getCurrentTenantId() {
        return CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
    }

    protected RSSDAO getDAO() {
        return dao;
    }

    public int getSystemRSSInstanceCount() throws RSSManagerException {
        return this.getDAO().getAllSystemRSSInstances().size();
    }

    public List<String> getUsersAttachedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        return this.getDAO().getUsersAssignedToDatabase(rssInstanceName, databaseName);
    }

    public List<String> getAvailableUsersToAttachToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        List<String> availableUsers = new ArrayList<String>();
        List<String> existingUsers =
                this.getUsersAttachedToDatabase(rssInstanceName, databaseName);
        for (DatabaseUser user : this.getDAO().getUsersByRSSInstance(rssInstanceName)) {
            String username = user.getUsername();
            if (!existingUsers.contains(username)) {
                availableUsers.add(username);
            }
        }
        return availableUsers;
    }
    
    
}
