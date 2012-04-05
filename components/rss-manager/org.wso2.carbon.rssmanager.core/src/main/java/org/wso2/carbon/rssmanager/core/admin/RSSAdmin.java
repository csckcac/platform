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
package org.wso2.carbon.rssmanager.core.admin;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.factory.DataSourceInformationFactory;
import org.apache.synapse.commons.datasource.serializer.DataSourceInformationSerializer;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.datasource.DataSourceInformationManager;
import org.wso2.carbon.datasource.DataSourceManagementHandler;
import org.wso2.carbon.datasource.MiscellaneousHelper;
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerUtil;
import org.wso2.carbon.rssmanager.core.connections.DBConnectionHandler;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.RSSManager;
import org.wso2.carbon.rssmanager.core.description.*;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.securevault.secret.SecretInformation;

import javax.xml.stream.XMLStreamException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Admin Class which exposes all the functionality that are supposed to be accessible
 * via the UI
 */
public class RSSAdmin extends AbstractAdmin {

    private static final String RSS_MANAGER_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/rssmanager";
    private static final OMFactory FACTORY = OMAbstractFactory.getOMFactory();

    private static final OMNamespace RSS_MANAGER_OM_NAMESPACE = FACTORY.createOMNamespace(
            RSS_MANAGER_EXTENSION_NS, "instance");

    private static final Log log = LogFactory.getLog(RSSAdmin.class);

    private RSSManager rssManager = RSSManager.getInstance();

    /**
     * Adds a RSS instance to metadata.
     *
     * @param rssIns RSSInstance object containing details of the instance to be added.
     * @throws RSSDAOException rssDaoException
     */
    public void addRSSInstance(RSSInstance rssIns) throws RSSDAOException {
        if (RSSManagerUtil.isSuperTenant()) {
            if (RSSManagerConstants.LOCAL.equals(rssIns.getServerCategory())) {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            }
        }
        rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
        RSSDAOFactory.getRSSDAO().addRSSInstance(rssIns);
    }

    /**
     * Removes a particular RSS instance from metadata.
     *
     * @param rssInsId Id of the RSS instance to be removed.
     * @throws RSSDAOException rssDaoException
     */
    public void removeRSSInstance(int rssInsId) throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        List<DatabaseInstance> dbs = dao.getAllDatabaseInstancesByRSSInstanceId(rssInsId);
        for (DatabaseInstance db : dbs) {
            if (db != null) {
                List<DatabaseUserEntry> users =
                        dao.getUsersByDatabaseInstanceId(db.getDatabaseInstanceId());
                for (DatabaseUserEntry user : users) {
                    /* users should be detached rather than deleting. */
                    if (user != null) {
                        rssManager.dropUser(user.getUserId(), db.getDatabaseInstanceId());
                    }
                }
                rssManager.dropDatabase(db.getDatabaseInstanceId());
            }
        }
        /* deleting the RSS instance from the metadata repository */
        dao.deleteRSSInstance(rssInsId);
    }

    /**
     * Edits metadata corresponding to a particular RSS instance.
     *
     * @param rssIns RSS instance information to be edited.
     * @throws RSSDAOException rssDaoException
     */
    public void editRSSInstance(RSSInstance rssIns) throws RSSDAOException {
        if (RSSManagerUtil.isSuperTenant()) {
            if (RSSManagerConstants.LOCAL.equals(rssIns.getServerCategory())) {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            }
        }
        rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
        RSSDAOFactory.getRSSDAO().updateRSSInstance(rssIns);
    }

    public RSSInstance getRSSInstanceDataById(int rssInsId) throws RSSDAOException {
        return RSSDAOFactory.getRSSDAO().getRSSInstanceDataById(rssInsId);
    }

    /**
     * Returns the list of RSS instances accessible for a particular tenant.
     *
     * @return RSS instance list
     * @throws RSSDAOException rssDaoException
     */
    public RSSInstanceEntry[] getRSSInstanceList() throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        List<RSSInstanceEntry> instanceList;

        if (RSSManagerUtil.isSuperTenant()) {
            instanceList = dao.getAllRSSInstances();
            return instanceList.toArray(new RSSInstanceEntry[instanceList.size()]);
        }

        instanceList = dao.getAllTenantSpecificRSSInstances();
        return instanceList.toArray(new RSSInstanceEntry[instanceList.size()]);
    }

    /**
     * Retrieves the RDS instances chosen in the round robin manner to be used in
     * the database instance creation.
     *
     * @return round robin assigned RDS instance
     * @throws RSSDAOException rssDaoException
     */
    public RSSInstanceEntry getRoundRobinAssignedRSSInstance() throws RSSDAOException {
        int insCount;
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        List<RSSInstance> rdsInstances = dao.getAllServiceProviderHostedRSSInstances();
        insCount = dao.getServiceProviderHostedRSSDatabaseInstanceCount();

        for (int i = 0; i < rdsInstances.size(); i++) {
            if (i == insCount % rdsInstances.size()) {
                RSSInstance rssIns = rdsInstances.get(i);
                if (rssIns != null) {
                    RSSInstanceEntry rssEntry =
                            RSSManagerUtil.createRSSInstanceEntryFromRSSInstanceData(rssIns);
                    rssEntry.setName(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                    return rssEntry;
                }
            }
        }
        return null;
    }

    /**
     * Creates the required database upon a particular tenant.
     *
     * @param dbIns serialized data corresponding to the database need to be created
     * @throws RSSDAOException rssDaoException
     */
    public void createDatabase(OMElement dbIns) throws RSSDAOException {
        DatabaseInstance db = RSSManagerUtil.buildDatabaseInstance(dbIns);
        rssManager.createDatabase(db);
    }

    /**
     * drops the database which carries the given RSS instance id and the database instance id.
     *
     * @param dbInsId id of the database instance to be dropped.
     * @throws RSSDAOException rssDaoException
     */
    public void dropDatabase(int dbInsId) throws RSSDAOException {
        if (!RSSManagerUtil.dbBelongsToCurrentTenant(RSSDAOFactory.getRSSDAO().
                getDatabaseInstanceById(dbInsId))) {
            throw new RSSDAOException("Database does not belong to the current tenant");
        }
        rssManager.dropDatabase(dbInsId);
    }

    /**
     * Retrieves the database instance list corresponding to a particular tenant.
     *
     * @return List of database instance data as a string.
     * @throws RSSDAOException rssDaoException
     */
    public DatabaseInstanceEntry[] getDatabaseInstanceList() throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        List<DatabaseInstanceEntry> entries;

        if (RSSManagerUtil.isSuperTenant()) {
            entries = dao.getAllDatabaseInstanceEntries();
            return entries.toArray(new DatabaseInstanceEntry[entries.size()]);
        }

        entries = dao.getAllTenantSpecificDatabaseInstanceEntries();
        return entries.toArray(new DatabaseInstanceEntry[entries.size()]);
    }

    /**
     * Retrieves information of a particular database instance by its id.
     *
     * @param dbInsId Id of the database instance.
     * @return Database instance information as a string.
     * @throws RSSDAOException rssDaoException
     */
    public DatabaseInstanceEntry getDatabaseInstanceById(int dbInsId) throws RSSDAOException {
        return RSSDAOFactory.getRSSDAO().getDatabaseInstanceEntryById(dbInsId);
    }

    /**
     * Creates a database user assigning the privileges identified by the given privilege id and
     * assign the user to the database identified by the given database instance id.
     *
     * @param user        User data to be added.
     * @param privGroupId Id of the privilege group that should be assigned to the database user.
     * @param dbInsId     Id of the database instance to which the database user should be attached.
     * @throws RSSDAOException rssDaoException.
     */
    public void createUser(DatabaseUser user, int privGroupId, int dbInsId) throws RSSDAOException {
        try {
            rssManager.createUser(user, privGroupId, dbInsId);
        } catch (SQLException e) {
            handleException("Unable to create user " + user.getUsername(), e);
        }
    }

    /**
     * Drops a particular user.
     *
     * @param userId  Id of the user to be dropped.
     * @param dbInsId Id of the database instance that the user is attached to.
     * @throws RSSDAOException rssDaoException
     */
    public void dropUser(int userId, int dbInsId) throws RSSDAOException {
        rssManager.dropUser(userId, dbInsId);
    }

    /**
     * Edits the privileges assigned to a particular user.
     *
     * @param privs   set of privileges to be edited as a string.
     * @param user    information of the user to be edited.
     * @param dbInsId id of the database instance to which the user is attached to.
     * @throws RSSDAOException rssDaoException
     */
    public void editUserPrivileges(String privs, DatabaseUser user,
                                   int dbInsId) throws RSSDAOException {
        try {
            if (privs != null) {
                DatabasePermissions permissions = RSSManagerUtil.getPermissionObject(
                        AXIOMUtil.stringToOM(privs));
                rssManager.editUserPrivileges(permissions, user, dbInsId);
            }
        } catch (XMLStreamException e) {
            handleException("Unable to edit user " + user.getUsername(), e);
        }
    }

    /**
     * Retrieves database user by name.
     *
     * @param userId userId
     * @return Database user information.
     * @throws RSSDAOException rssDaoException
     */
    public DatabaseUser getDatabaseUserById(int userId) throws RSSDAOException {
        return RSSDAOFactory.getRSSDAO().getUserById(userId);
    }

    /**
     * Retrieves the list of users attached to a particular database instance.
     *
     * @param dbInstId id of the instance to which the user are assigned to.
     * @return Array of Database users.
     * @throws RSSDAOException rssDaoException
     */
    public DatabaseUserEntry[] getUsersByDatabaseInstanceId(int dbInstId) throws RSSDAOException {
        List<DatabaseUserEntry> users =
                RSSDAOFactory.getRSSDAO().getUsersByDatabaseInstanceId(dbInstId);
        return users.toArray(new DatabaseUserEntry[users.size()]);
    }

    /**
     * Retrieves the set of permissions assigned to a particular user.
     *
     * @param userId  id of the user to whom the privileges are assigned to.
     * @param dbInsId id of the database instance to which the user is assigned to.
     * @return The set of assigned privilges and their values as a serialized OMElement.
     * @throws RSSDAOException rssDaoException
     */
    public String getUserDatabasePermissions(int userId, int dbInsId) throws RSSDAOException {
        if (!RSSManagerUtil.userBelongsToCurrentTenant(userId)) {
            throw new RSSDAOException("Database user does not belong to the current tenant");
        }
        if (!RSSManagerUtil.dbBelongsToCurrentTenant(RSSDAOFactory.getRSSDAO().
                getDatabaseInstanceById(dbInsId))) {
            throw new RSSDAOException("Database does not belong to the current tenant");
        }
        Map<String, Object> permissions = RSSDAOFactory.getRSSDAO().getUserDatabasePermissions(
                userId, dbInsId);
        return RSSManagerUtil.serializeUserPermissions(
                RSS_MANAGER_OM_NAMESPACE, permissions).toString();
    }

    /**
     * Creates a database privilege group for a particular tenant.
     *
     * @param privGroup privilege group bean containing all the details about the privilege group
     *                  to be added.
     * @throws RSSDAOException rssDaoException
     */
    public void createPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException {
        RSSDAOFactory.getRSSDAO().addUserPrivilegeGroup(privGroup);
    }

    /**
     * Removes a the privilege group which carries the given id, from the privilege groups belongs
     * to the current tenant.
     *
     * @param privGroupId Id of the privilege group.
     * @throws RSSDAOException rssDaoException.
     */
    public void removePrivilegeGroup(int privGroupId) throws RSSDAOException {
        RSSDAOFactory.getRSSDAO().removeUserPrivilegeGroup(privGroupId);
    }

    /**
     * Edits the privileges of the given privilege group with the modified values..
     *
     * @param privGroup privilege group containing the modified values for each database privilege.
     * @throws RSSDAOException rssDAOException.
     */
    public void editPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException {
        if (!RSSManagerUtil.privilegeGroupBelongsToCurrentTenant(privGroup.getPrivGroupId())) {
            throw new RSSDAOException("Privilege group does not belong to the current tenant");
        }
        RSSDAOFactory.getRSSDAO().editUserPrivilegeGroup(privGroup);
    }

    /**
     * Retrieves the set of privilege groups belong to the current tenant.
     *
     * @return An array of privilege groups.
     * @throws RSSDAOException rssDaoException.
     */
    public PrivilegeGroup[] getPrivilegeGroups() throws RSSDAOException {
        List<PrivilegeGroup> privGroups = RSSDAOFactory.getRSSDAO().getAllUserPrivilegeGroups();
        return privGroups.toArray(new PrivilegeGroup[privGroups.size()]);
    }

    /**
     * Retrieves the data corresponding to the privilege group identified by the given privilege
     * group Id.
     *
     * @param privGroupId Id of the privilege group to be retrieved.
     * @return Privilege group bean object containing the required data of the privilege group.
     * @throws RSSDAOException rssDaoException.
     */
    public PrivilegeGroup getPrivilegeGroupById(int privGroupId) throws RSSDAOException {
        return RSSDAOFactory.getRSSDAO().getPrivilegeGroupById(privGroupId);
    }

    /**
     * Creates a datasource based on a particular database instance.
     *
     * @return Datasource information object.
     */
    private DataSourceInformation createDSInfo(DatabaseInstanceEntry entry, DatabaseUser user) {
        DataSourceInformation dsInfo = new DataSourceInformation();
        String dsName = entry.getDbName() + "_" + user.getUserId();
        dsInfo.setAlias(dsName);
        dsInfo.setDatasourceName(dsName);
        dsInfo.setDriver(RSSManagerCommonUtil.getDatabaseDriver(entry.getDbUrl()));
        dsInfo.setType(DataSourceInformation.BASIC_DATA_SOURCE);
        dsInfo.setRepositoryType(DataSourceConstants.PROP_REGISTRY_MEMORY);
        dsInfo.setUrl(entry.getDbUrl());
        SecretInformation secInfo = new SecretInformation();
        secInfo.setUser(user.getUsername());
        secInfo.setAliasSecret(user.getPassword());
        dsInfo.setSecretInformation(secInfo);
        /* the following step is required, so we don't have to mess with creating properties and all,
           * and also security information specifics */
        Properties props = DataSourceInformationSerializer.serialize(dsInfo);
        return DataSourceInformationFactory.createDataSourceInformation(dsName, props);
    }

    /**
     * Create Carbon data source with given the data source information.
     *
     * @return Success status
     * @throws RSSDAOException rssDaoException
     */
    public String createCarbonDSFromDatabaseUserEntry(int dbInsId, int userId) throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        try {
            int tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            DatabaseInstanceEntry entry = dao.getDatabaseInstanceEntryById(dbInsId);
            if (entry == null) {
                throw new RSSDAOException("Database instance does not exist");
            }
            DatabaseUser user = dao.getUserById(userId);
            DataSourceInformation dsInfo = createDSInfo(entry, user);
            Properties dsProps = DataSourceInformationSerializer.serialize(dsInfo);
            OMElement dsEl = MiscellaneousHelper.createOMElement(dsProps);

            DataSourceInformationManager dsInfoManager =
                    DataSourceManagementHandler.getInstance().getTenantDataSourceInformationManager();

            dsInfoManager.addDataSourceInformation(dsInfo);
            String dsName = entry.getDbName() + "_" + user.getUserId();
            dsInfoManager.persistDataSourceInformation(dsName, dsEl);
            return "Carbon data source with the name '" + dsName + "' has been successfully created";
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    /**
     * Test the RSS instance connection using a mock database connection test.
     *
     * @param driverClass JDBC Driver class.
     * @param jdbcURL     JDBC url.
     * @param username    username.
     * @param password    password.
     * @return success or failure message.
     * @throws RSSDAOException rssDaoException
     */
    public String testConnection(String driverClass, String jdbcURL, String username,
                                 String password) throws RSSDAOException {
        int tenantId =
                SuperTenantCarbonContext.getCurrentContext(this.getConfigContext()).getTenantId();

        if (driverClass == null || driverClass.length() == 0) {
            String message = "Driver class is missing";
            log.debug(message);
            return message;
        }
        if (jdbcURL == null || jdbcURL.length() == 0) {
            String message = "Driver connection URL is missing";
            log.debug(message);
            return message;
        }

        try {
            SuperTenantCarbonContext.startTenantFlow();
            CarbonContextHolder.getCurrentCarbonContextHolder().setTenantId(tenantId);

            RSSInstance rssIns = new RSSInstance();
            rssIns.setServerURL(jdbcURL);
            rssIns.setAdminUsername(username);
            rssIns.setAdminPassword(password);
            DBConnectionHandler.getConnection(rssIns);
            String message = "Database connection is successful with driver class " + driverClass
                    + " , JDBC url " + jdbcURL + " and username " + username;
            log.debug(message);
            return message;
        } finally {
            DBConnectionHandler.closeConnection();
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    private void handleException(String msg, Exception e) throws RSSDAOException {
        log.error(msg, e);
        throw new RSSDAOException(msg, e);
    }

}
