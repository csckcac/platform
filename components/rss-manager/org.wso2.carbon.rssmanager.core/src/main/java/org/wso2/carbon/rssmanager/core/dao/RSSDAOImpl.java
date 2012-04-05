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
package org.wso2.carbon.rssmanager.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerUtil;
import org.wso2.carbon.rssmanager.core.description.*;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO implementation for DSSDAO interface.
 */
public class RSSDAOImpl implements RSSDAO {

    private static Log log = LogFactory.getLog(RSSDAOImpl.class);

    @Override
    public void addRSSInstance(RSSInstance rssInst) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO RSS_INSTANCE (name, server_url, dbms_type, " +
                            "instance_type, server_category, admin_username, admin_password, tenant_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"rss_instance_id"});
            stmt.setString(1, rssInst.getName());
            stmt.setString(2, rssInst.getServerURL());
            stmt.setString(3, rssInst.getDbmsType());
            stmt.setString(4, rssInst.getInstanceType());
            stmt.setString(5, rssInst.getServerCategory());
            stmt.setString(6, rssInst.getAdminUsername());
            stmt.setString(7, rssInst.getAdminPassword());
            stmt.setInt(8, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
            rssInst.setRssInstanceId(this.getGeneratedKey(stmt));
        } catch (SQLException e) {
            throw new RSSDAOException("Error in adding new RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private int getGeneratedKey(Statement stmt) throws SQLException {
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    @Override
    public List<RSSInstanceEntry> getAllTenantSpecificRSSInstances() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, admin_username, admin_password, " +
                    "tenant_id FROM RSS_INSTANCE WHERE tenant_id = ?");
            stmt.setInt(1, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            List<RSSInstanceEntry> result = new ArrayList<RSSInstanceEntry>();
            while (rs.next()) {
                result.add(this.createRSSInstanceEntryFromRS(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving all RSS instances", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void updateRSSInstance(RSSInstance rssInst) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE RSS_INSTANCE SET name = ?, " +
                    "server_url = ?, dbms_type = ?, instance_type = ?, server_category = ?, " +
                    "admin_username = ?, admin_password = ? WHERE rss_instance_id = ? AND tenant_id = ?");
            stmt.setString(1, rssInst.getName());
            stmt.setString(2, rssInst.getServerURL());
            stmt.setString(3, rssInst.getDbmsType());
            stmt.setString(4, rssInst.getInstanceType());
            stmt.setString(5, rssInst.getServerCategory());
            stmt.setString(6, rssInst.getAdminUsername());
            stmt.setString(7, rssInst.getAdminPassword());
            stmt.setInt(8, rssInst.getRssInstanceId());
            stmt.setInt(9, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public RSSInstanceEntry getRSSInstanceEntry(int rssInstId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, " +
                    "tenant_id FROM RSS_INSTANCE WHERE rss_instance_id = ?");
            stmt.setInt(1, rssInstId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createRSSInstanceEntryFromRS(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public RSSInstance getRSSInstanceById(int rssInstId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, admin_username, admin_password, " +
                    "tenant_id FROM RSS_INSTANCE WHERE rss_instance_id = ?");
            stmt.setInt(1, rssInstId);
            //stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createRSSInstanceFromRS(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseUserEntry> getUsersByDatabaseInstanceId(int dbInstId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        List<DatabaseUserEntry> users = new ArrayList<DatabaseUserEntry>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT d.user_id, d.db_username, d.rss_instance_id FROM DATABASE_USER d, USER_DATABASE_ENTRY u WHERE u.database_instance_id=? AND d.user_id=u.user_id AND d.user_tenant_id=?");
            stmt.setInt(1, dbInstId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserEntryFromRS(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabaseInstanceEntry getDatabaseInstanceEntryById(int dbInsId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT d.database_instance_id, d.name, d.tenant_id, r.rss_instance_id, r.name, r.server_url, r.tenant_id FROM RSS_INSTANCE r, DATABASE_INSTANCE d WHERE r.rss_instance_id=(SELECT rss_instance_id FROM DATABASE_INSTANCE WHERE tenant_id=? AND database_instance_id=?) AND d.tenant_id=? AND d.database_instance_id=? AND d.rss_instance_id=r.rss_instance_id");
            stmt.setInt(1, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.setInt(2, dbInsId);
            stmt.setInt(3, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.setInt(4, dbInsId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createDatabaseInstanceEntryFromRS(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving database instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabaseInstance getDatabaseInstanceById(int dbInsId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT database_instance_id, name, " +
                    "rss_instance_id, tenant_id FROM DATABASE_INSTANCE WHERE " +
                    "database_instance_id = ? AND tenant_id = ?");
            stmt.setInt(1, dbInsId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createDatabaseInstanceFromRS(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve database instance data");
        }
    }

    @Override
    public DatabaseUser getUserById(int userId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        DatabaseUser user = new DatabaseUser();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT user_id, db_username, rss_instance_id FROM DATABASE_USER " +
                            "where user_id = ? AND user_tenant_id=?",
                    new String[]{"user_id"});
            stmt.setInt(1, userId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.createDatabaseUserFromRS(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new RSSDAOException("Error while retrieving user", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabaseUserEntry getUserEntry(int userId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        DatabaseUserEntry user = new DatabaseUserEntry();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT user_id, rss_instance_id FROM DATABASE_USER " +
                            "where user_id = ? AND user_tenant_id=?",
                    new String[]{"user_id"});
            stmt.setInt(1, userId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.createDatabaseUserEntryFromRS(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new RSSDAOException("Error while retrieving user", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void addDatabaseInstance(DatabaseInstance dbInst)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO DATABASE_INSTANCE (name, rss_instance_id, tenant_id) " +
                            "VALUES (?, ?, ?)",
                    new String[]{"database_instance_id"});
            stmt.setString(1, dbInst.getName());
            stmt.setInt(2, dbInst.getRssInstanceId());
            stmt.setInt(3, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
            dbInst.setDatabaseInstanceId(this.getGeneratedKey(stmt));
            this.setDatabaseInstanceProperties(conn, dbInst);
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in adding new database instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void setDatabaseInstanceProperties(Connection conn,
                                               DatabaseInstance dbInst) throws SQLException {
        Map<String, String> existingProps = this.getDatabaseInstanceProperties(conn,
                dbInst.getDatabaseInstanceId());
        Map<String, String> newProps = dbInst.getProperties();
        Map<String, String> toBeRemovedProps = new HashMap<String, String>(existingProps);
        if (newProps != null) {
            Map<String, String> toBeAddedProps = new HashMap<String, String>(newProps);
            String lhs, rhs;
            for (String key : newProps.keySet()) {
                if (existingProps.containsKey(key)) {
                    lhs = existingProps.get(key);
                    rhs = newProps.get(key);
                    if (lhs == null) {
                        if (rhs == null) {
                            toBeAddedProps.remove(key);
                            toBeRemovedProps.remove(key);
                        }
                    } else if (lhs.equals(rhs)) {
                        toBeAddedProps.remove(key);
                        toBeRemovedProps.remove(key);
                    }
                }
            }
            for (String key : toBeRemovedProps.keySet()) {
                this.deleteDatabaseInstanceProperty(conn, dbInst.getDatabaseInstanceId(), key);
            }
            for (Map.Entry<String, String> entry : toBeAddedProps.entrySet()) {
                this.addDatabaseInstanceProperty(conn, dbInst.getDatabaseInstanceId(),
                        entry.getKey(), entry.getValue());
            }
        }
    }

    private void addDatabaseInstanceProperty(Connection conn,
                                             int databaseInstanceId, String key, String value)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO DATABASE_INSTANCE_PROPERTY (prop_name, prop_value, " +
                        "database_instance_id) VALUES (?, ?, ?)");
        stmt.setString(1, key);
        stmt.setString(2, value);
        stmt.setInt(3, databaseInstanceId);
        stmt.executeUpdate();
    }

    private void deleteDatabaseInstanceProperty(
            Connection conn, int databaseInstanceId, String key) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM DATABASE_INSTANCE_PROPERTY " +
                "WHERE prop_name = ? AND database_instance_id = ?");
        stmt.setString(1, key);
        stmt.setInt(2, databaseInstanceId);
        stmt.executeUpdate();
    }

    private Map<String, String> getDatabaseInstanceProperties(
            Connection conn, int databaseInstanceId) throws SQLException {
        Map<String, String> props = new HashMap<String, String>();
        PreparedStatement stmt = conn.prepareStatement("SELECT prop_name, prop_value FROM " +
                "DATABASE_INSTANCE_PROPERTY WHERE database_instance_id = ?");
        stmt.setInt(1, databaseInstanceId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            props.put(rs.getString("prop_name"), rs.getString("prop_value"));
        }
        return props;
    }

    @Override
    public void updateDatabaseInstance(DatabaseInstance dbInst)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE DATABASE_INSTANCE SET name = ?, rss_instance_id = ? WHERE " +
                            "database_instance_id = ? AND tenant_id = ?");
            stmt.setString(1, dbInst.getName());
            stmt.setInt(2, dbInst.getRssInstanceId());
            stmt.setInt(3, dbInst.getDatabaseInstanceId());
            stmt.setInt(4, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
            this.setDatabaseInstanceProperties(conn, dbInst);
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating database instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private RSSInstanceEntry createRSSInstanceEntryFromRS(ResultSet rs) throws SQLException,
            RSSDAOException {
        int rssInstanceId = rs.getInt("rss_instance_id");
        String name = rs.getString("name");
        String serverURL = rs.getString("server_url");
        String instanceType = rs.getString("instance_type");
        String serverCategory = rs.getString("server_category");
        String tenantDomainName = RSSManagerUtil.getTenantDomain(rs.getInt("tenant_id"));
        return new RSSInstanceEntry(rssInstanceId, name, serverURL, instanceType, serverCategory,
                tenantDomainName);
    }

    private RSSInstance createRSSInstanceFromRS(ResultSet rs) throws SQLException {
        int rssInstanceId = rs.getInt("rss_instance_id");
        String name = rs.getString("name");
        String serverURL = rs.getString("server_url");
        String instanceType = rs.getString("instance_type");
        String serverCategory = rs.getString("server_category");
        String adminUsername = rs.getString("admin_username");
        String adminPassword = rs.getString("admin_password");
        String dbmsType = rs.getString("dbms_type");
        return new RSSInstance(rssInstanceId, name, serverURL, dbmsType, instanceType,
                serverCategory, adminUsername, adminPassword, -1);
    }

    private DatabaseInstance createDatabaseInstanceFromRS(ResultSet rs) throws SQLException {
        int databaseInstanceId = rs.getInt("database_instance_id");
        String name = rs.getString("name");
        int rssInstanceId = rs.getInt("rss_instance_id");
        int tenantId = rs.getInt("tenant_id");
        return new DatabaseInstance(databaseInstanceId, name, rssInstanceId, tenantId);
    }

    @Override
    public List<RSSInstance> getAllServiceProviderHostedRSSInstances() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, admin_username, admin_password" +
                    " FROM RSS_INSTANCE WHERE instance_type = ? AND tenant_id = ?");
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            ResultSet rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving WSO2 RSS instances", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void deleteRSSInstance(int rssInstanceId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            List<DatabaseUserEntry> users = this.getDatabaseUsersByRSSInstanceId(conn, rssInstanceId);
            if (users.size() > 0) {
                for (DatabaseUserEntry user : users) {
                    this.deleteUser(user.getUserId());
                }
            }
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM RSS_INSTANCE WHERE " +
                    "rss_instance_id = ? AND tenant_id = ?");
            stmt.setInt(1, rssInstanceId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in deleting RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private List<DatabaseUserEntry> getDatabaseUsersByRSSInstanceId(Connection conn, int rssInsId)
            throws SQLException {
        PreparedStatement stmt =
                conn.prepareStatement("SELECT user_id, rss_instance_id, db_username " +
                        "FROM DATABASE_USER WHERE rss_instance_id=? AND user_tenant_id = ?");
        stmt.setInt(1, rssInsId);
        stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
        ResultSet rs = stmt.executeQuery();
        List<DatabaseUserEntry> users = new ArrayList<DatabaseUserEntry>();
        while (rs.next()) {
            users.add(this.createDatabaseUserEntryFromRS(rs));
        }
        return users;
    }

    @Override
    public List<DatabaseInstanceEntry> getAllDatabaseInstanceEntries() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT d.database_instance_id, d.name, d.tenant_id, r.rss_instance_id, r.name, r.server_url, r.tenant_id FROM RSS_INSTANCE r, DATABASE_INSTANCE d WHERE r.rss_instance_id IN (SELECT rss_instance_id FROM DATABASE_INSTANCE) AND r.rss_instance_id=d.rss_instance_id");
            ResultSet rs = stmt.executeQuery();
            List<DatabaseInstanceEntry> result = new ArrayList<DatabaseInstanceEntry>();
            while (rs.next()) {
                DatabaseInstanceEntry entry = this.createDatabaseInstanceEntryFromRS(rs);
                if (entry != null) {
                    result.add(entry);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving all database instances", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseInstance> getAllDatabaseInstancesByRSSInstanceId(int rssInsId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        List<DatabaseInstance> dbs = new ArrayList<DatabaseInstance>();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, database_instance_id, name, tenant_id FROM DATABASE_INSTANCE WHERE rss_instance_id = ? AND tenant_id = ?");
            stmt.setInt(1, rssInsId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseInstance db = this.createDatabaseInstanceFromRS(rs);
                if (db != null) {
                    dbs.add(db);
                }
            }
            return dbs;
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve database instance list");
        }
    }

    @Override
    public void deleteDatabaseInstance(int databaseInstanceId)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM DATABASE_INSTANCE WHERE " +
                    "database_instance_id = ? AND tenant_id = ?");
            stmt.setInt(1, databaseInstanceId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in deleting database instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public int addUser(DatabaseUser user) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO DATABASE_USER (db_username, rss_instance_id, " +
                            "user_tenant_id) " +
                            "VALUES (?, ?, ?)",
                    new String[]{"user_id"});
            stmt.setString(1, user.getUsername());
            stmt.setInt(2, user.getRssInstanceId());
            stmt.setInt(3, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
            int recordId = this.getGeneratedKey(stmt);
            user.setUserId(recordId);
            return recordId;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in adding new database user", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void deleteUser(int userId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM DATABASE_USER WHERE " +
                    "user_id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in deleting database user", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void updateUser(DatabaseUser user) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE DATABASE_USER SET db_username = ?," +
                            "rss_instance_id = ? WHERE user_id = ?");
            stmt.setString(1, user.getUsername());
            stmt.setInt(2, user.getRssInstanceId());
            stmt.setInt(3, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating database user", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void addUserDatabaseEntry(UserDatabaseEntry userDBEntry)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO USER_DATABASE_ENTRY (user_id, database_instance_id) " +
                            "VALUES (?, ?)");
            stmt.setInt(1, userDBEntry.getUserId());
            stmt.setInt(2, userDBEntry.getDatabaseInstanceId());
            stmt.executeUpdate();
            this.setUserDatabasePermissions(conn, userDBEntry);
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in adding new user-database-entry", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseInstance> getTenantsDatabaseInstanceList() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT database_instance_id, name, " +
                    "rss_instance_id, tenant_id FROM DATABASE_INSTANCE WHERE tenant_id = ?");
            stmt.setInt(1, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            List<DatabaseInstance> result = new ArrayList<DatabaseInstance>();
            while (rs.next()) {
                DatabaseInstance inst = this.createDatabaseInstanceFromRS(rs);
                inst.setProperties(this.getDatabaseInstanceProperties(conn,
                        inst.getDatabaseInstanceId()));
                result.add(inst);
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving all database instances", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    public List<DatabaseUserEntry> getUsersByDatabaseName(int tenantId, int rssInstId,
                                                          int databaseInstId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT user_id,db_username, rss_instance_id FROM DATABASE_USER " +
                            "where rss_instance_id=? AND tenant_id=?",
                    new String[]{"user_id"});
            stmt.setInt(1, rssInstId);
            stmt.setInt(2, databaseInstId);
            stmt.setInt(3, tenantId);
            ResultSet rs = stmt.executeQuery();
            List<DatabaseUserEntry> result = new ArrayList<DatabaseUserEntry>();
            while (rs.next()) {
                DatabaseUserEntry user = this.createDatabaseUserEntryFromRS(rs);
                result.add(user);
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in while retrieving users", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private DatabaseUserEntry createDatabaseUserEntryFromRS(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("db_username");
        int rssInstId = rs.getInt("rss_instance_id");
        return new DatabaseUserEntry(userId, username, rssInstId);
    }

    private DatabaseUser createDatabaseUserFromRS(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String dbUsername = rs.getString("db_username");
        int rssInstId = rs.getInt("rss_instance_id");
        return new DatabaseUser(userId, dbUsername, rssInstId);
    }

    @Override
    public void updateUserDatabaseEntry(UserDatabaseEntry userDBEntry)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            this.setUserDatabasePermissions(conn, userDBEntry);
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating user-database-entry", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void deleteUserDatabaseEntry(int userId, int databaseInstanceId)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            /* delete permissions first */
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_DATABASE_PERMISSION " +
                    "WHERE user_id = ? AND database_instance_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, databaseInstanceId);
            stmt.executeUpdate();
            /* now delete the user-database-entry */
            stmt = conn.prepareStatement("DELETE FROM USER_DATABASE_ENTRY WHERE user_id = ? AND " +
                    "database_instance_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, databaseInstanceId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in deleting user-database-entry", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void incrementServiceProviderHostedRSSDatabaseInstanceCount() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " +
                    "WSO2_RSS_DATABASE_INSTANCE_COUNT");
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                stmt = conn.prepareStatement("INSERT INTO WSO2_RSS_DATABASE_INSTANCE_COUNT (" +
                        "instance_count) VALUES (0)");
                stmt.executeUpdate();
            }
            stmt = conn.prepareStatement("UPDATE WSO2_RSS_DATABASE_INSTANCE_COUNT SET " +
                    "instance_count = instance_count + 1");
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in incrementing WSO2 RSS database instance count", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public int getServiceProviderHostedRSSDatabaseInstanceCount() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT instance_count FROM " +
                    "WSO2_RSS_DATABASE_INSTANCE_COUNT");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving WSO2 RSS database instance count", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void setUserDatabasePermissions(Connection conn,
                                            UserDatabaseEntry userDBEntry) throws
            RSSDAOException, SQLException {
        Map<String, Object> existingPerms = this.getUserDatabasePermissions(userDBEntry.getUserId(),
                userDBEntry.getDatabaseInstanceId());
        Map<String, Object> newPermissions = userDBEntry.getPermissions();
        Map<String, Object> toBeRemovedPerms = new HashMap<String, Object>(existingPerms);
        Map<String, Object> toBeAddedPerms = new HashMap<String, Object>(newPermissions);
        String lhs, rhs;
        for (String key : newPermissions.keySet()) {
            if (existingPerms.containsKey(key)) {
                lhs = existingPerms.get(key).toString();
                rhs = newPermissions.get(key).toString();
                if (lhs == null) {
                    if (rhs == null) {
                        toBeAddedPerms.remove(key);
                        toBeRemovedPerms.remove(key);
                    }
                } else if (lhs.equals(rhs)) {
                    toBeAddedPerms.remove(key);
                    toBeRemovedPerms.remove(key);
                }
            }
        }
        for (String permName : toBeRemovedPerms.keySet()) {
            this.deleteUserDatabasePermission(conn, userDBEntry.getUserId(),
                    userDBEntry.getDatabaseInstanceId(), permName);
        }
        for (Map.Entry<String, Object> entry : toBeAddedPerms.entrySet()) {
            this.addUserDatabasePermission(conn, userDBEntry.getUserId(),
                    userDBEntry.getDatabaseInstanceId(), entry.getKey(),
                    entry.getValue().toString());
        }
    }

    private void addUserDatabasePermission(Connection conn, int userId, int databaseInstanceId,
                                           String permName, String permValue)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO USER_DATABASE_PERMISSION (perm_name, perm_value, user_id, " +
                        "database_instance_id) VALUES (?, ?, ?, ?)");
        stmt.setString(1, permName);
        stmt.setString(2, permValue);
        stmt.setInt(3, userId);
        stmt.setInt(4, databaseInstanceId);
        stmt.executeUpdate();
    }

    private void deleteUserDatabasePermission(Connection conn, int userId, int databaseInstanceId,
                                              String permName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_DATABASE_PERMISSION " +
                "WHERE perm_name = ? AND user_id = ? AND database_instance_id = ?");
        stmt.setString(1, permName);
        stmt.setInt(2, userId);
        stmt.setInt(3, databaseInstanceId);
        stmt.executeUpdate();
    }

    @Override
    public Map<String, Object> getUserDatabasePermissions(int userId, int databaseInstanceId)
            throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        Map<String, Object> permissions = new HashMap<String, Object>();
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement("SELECT perm_name, perm_value FROM " +
                    "USER_DATABASE_PERMISSION WHERE user_id = ? AND database_instance_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, databaseInstanceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                permissions.put(rs.getString("perm_name"), rs.getString("perm_value"));
            }
            return permissions;
        } catch (SQLException e) {
            log.error(e);
            throw new RSSDAOException("Error while retrieving user permissions", e);
        }

    }

    @Override
    public List<RSSInstanceEntry> getAllRSSInstances() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, tenant_id " +
                    "FROM RSS_INSTANCE WHERE tenant_id=?");
            stmt.setInt(1, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            List<RSSInstanceEntry> result = new ArrayList<RSSInstanceEntry>();
            while (rs.next()) {
                result.add(this.createRSSInstanceEntryFromRS(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving all RSS instances", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<UserDatabaseEntry> getUserDatabaseEntriesByDatabaseInstanceId(
            int dbInsId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT user_id, database_instance_id " +
                    "FROM USER_DATABASE_ENTRY where database_instance_id = ? ");
            stmt.setInt(1, dbInsId);
            ResultSet rs = stmt.executeQuery();
            List<UserDatabaseEntry> result = new ArrayList<UserDatabaseEntry>();
            while (rs.next()) {
                result.add(this.createUserDatabaseEntry(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving User database entries", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private UserDatabaseEntry createUserDatabaseEntry(ResultSet rs) throws SQLException {
        int dbInsId = rs.getInt("database_instance_id");
        int userId = rs.getInt("user_id");
        return new UserDatabaseEntry(userId, dbInsId);
    }

    public void updateDatabaseUserPermission(Connection conn, String permName, String permValue,
                                             int userId, int dbInsId) throws RSSDAOException {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE USER_DATABASE_PERMISSION " +
                    "SET perm_value=? WHERE user_id=? AND database_instance_id=? AND perm_name=?");
            stmt.setString(1, permValue);
            stmt.setInt(2, userId);
            stmt.setInt(3, dbInsId);
            stmt.setString(4, permName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating user database permission", e);
        }
    }

    @Override
    public void updateUser(DatabasePermissions permissions, int userId, int dbInsId) throws
            RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        List<String> permissionNames = this.getExistingDatabasePermissions(conn, userId, dbInsId);
        for (Map.Entry entry : permissions.getPrivilegeMap().entrySet()) {
            String permName = entry.getKey().toString();
            if (permissionNames.contains(permName)) {
                String permValue = entry.getValue().toString();
                this.updateDatabaseUserPermission(conn, permName, permValue, userId, dbInsId);
            }
        }
    }

    @Override
    public void addUserPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            if (!this.validatePrivGroupName(conn, privGroup.getPrivGroupName())) {
                conn.setAutoCommit(false);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO USER_PRIVILEGE_GROUP(priv_group_name, tenant_id) VALUES(?, ?)",
                        new String[]{"priv_group_id"});
                stmt.setString(1, privGroup.getPrivGroupName());
                stmt.setInt(2, SuperTenantCarbonContext.getCurrentContext().getTenantId());
                stmt.executeUpdate();
                privGroup.setPrivGroupId(this.getGeneratedKey(stmt));
                this.setPrivilegeGroupProperties(conn, privGroup);
                conn.commit();
            } else {
                throw new RSSDAOException("A privilege group with the same name already exists");
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in adding new database instance", e);
        }
    }

    private boolean validatePrivGroupName(Connection conn, String privGroupName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT priv_group_name FROM " +
                "USER_PRIVILEGE_GROUP WHERE tenant_id=?");
        stmt.setInt(1, SuperTenantCarbonContext.getCurrentContext().getTenantId());
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            if (privGroupName != null && privGroupName.equals(rs.getString("priv_group_name"))) {
                return true;
            }
        }
        return false;
    }

    private void setPrivilegeGroupProperties(Connection conn, PrivilegeGroup privGroup) throws SQLException {
        for (DatabasePrivilege priv : privGroup.getPrivs()) {
            if (priv != null) {
                this.addPrivilegeGroupProperty(conn, privGroup.getPrivGroupId(), priv);
            }
        }
    }

    private void addPrivilegeGroupProperty(Connection conn, int privGroupId, DatabasePrivilege priv)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO USER_PRIVILEGE_GROUP_ENTRY(" +
                "priv_group_id, perm_name, perm_value) VALUES(?,?,?)");
        ps.setInt(1, privGroupId);
        ps.setString(2, priv.getPrivName());
        ps.setString(3, priv.getPrivValue());
        ps.executeUpdate();
    }

    @Override
    public void removeUserPrivilegeGroup(int privGroupId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            this.removeUserPrivilegeGroupEntries(conn, privGroupId);
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_PRIVILEGE_GROUP WHERE "
                    + "priv_group_id = ? AND tenant_id = ?");
            stmt.setInt(1, privGroupId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error in removing user privilege group", e);
        }
    }

    private void removeUserPrivilegeGroupEntries(Connection conn, int privGroupId) throws
            SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM USER_PRIVILEGE_GROUP_ENTRY " +
                "WHERE priv_group_id=?");
        stmt.setInt(1, privGroupId);
        stmt.executeUpdate();
    }

    @Override
    public void editUserPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            for (DatabasePrivilege priv : privGroup.getPrivs()) {
                this.updatePrivilege(conn, privGroup.getPrivGroupId(), priv);
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in updating RSS instance", e);
        }
    }

    private void updatePrivilege(Connection conn, int privGroupId,
                                 DatabasePrivilege priv) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE USER_PRIVILEGE_GROUP_ENTRY " +
                "SET perm_value=? WHERE perm_name = ? and priv_group_id = ?");
        stmt.setString(1, priv.getPrivValue());
        stmt.setString(2, priv.getPrivName());
        stmt.setInt(3, privGroupId);
        stmt.executeUpdate();
    }

    @Override
    public List<PrivilegeGroup> getAllUserPrivilegeGroups() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement("SELECT priv_group_id, priv_group_name, tenant_id " +
                    "FROM USER_PRIVILEGE_GROUP WHERE tenant_id = ?");
            stmt.setInt(1, SuperTenantCarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            List<PrivilegeGroup> result = new ArrayList<PrivilegeGroup>();
            while (rs.next()) {
                result.add(this.createPrivGroupFromRS(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve user privilege groups", e);
        }
    }

    @Override
    public PrivilegeGroup getPrivilegeGroupById(int privGroupId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement("SELECT priv_group_id, priv_group_name, tenant_id " +
                    "FROM USER_PRIVILEGE_GROUP WHERE priv_group_id = ? AND tenant_id=?");
            stmt.setInt(1, privGroupId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createPrivGroupFromRS(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve user privilege group data", e);
        }
    }

    private DatabasePrivilege[] getUserPrivilegeGroupEntries(int privGroupId)
            throws SQLException, RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT perm_name, perm_value " +
                "FROM USER_PRIVILEGE_GROUP_ENTRY WHERE priv_group_id=?");
        stmt.setInt(1, privGroupId);
        ResultSet rs = stmt.executeQuery();
        List<DatabasePrivilege> result = new ArrayList<DatabasePrivilege>();
        while (rs.next()) {
            DatabasePrivilege dp = new DatabasePrivilege(rs.getString("perm_name"),
                    rs.getString("perm_value"));
            result.add(dp);
        }
        return result.toArray(new DatabasePrivilege[result.size()]);
    }

    private PrivilegeGroup createPrivGroupFromRS(ResultSet rs) throws SQLException, RSSDAOException {
        int privGroupId = rs.getInt("priv_group_id");
        String privGroupName = rs.getString("priv_group_name");
        DatabasePrivilege[] privileges = this.getUserPrivilegeGroupEntries(privGroupId);
        return new PrivilegeGroup(privGroupId, privGroupName, privileges);
    }

    private List<String> getExistingDatabasePermissions(Connection conn, int userId,
                                                        int dbInsId) throws RSSDAOException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT perm_name " +
                    "FROM USER_DATABASE_PERMISSION WHERE user_id=? AND database_instance_id=?");
            stmt.setInt(1, userId);
            stmt.setInt(2, dbInsId);
            ResultSet rs = stmt.executeQuery();
            List<String> permNames = new ArrayList<String>();
            while (rs.next()) {
                permNames.add(rs.getString("perm_name"));
            }
            return permNames;
        } catch (SQLException e) {
            throw new RSSDAOException("Error while retrieving existing database permissions", e);
        }
    }

    @Override
    public List<DatabaseInstanceEntry> getAllTenantSpecificDatabaseInstanceEntries() throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        List<DatabaseInstanceEntry> entries = new ArrayList<DatabaseInstanceEntry>();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT d.database_instance_id, d.name, d.tenant_id, r.rss_instance_id, r.name, r.server_url, r.tenant_id FROM RSS_INSTANCE r, DATABASE_INSTANCE d WHERE r.rss_instance_id IN (SELECT rss_instance_id FROM DATABASE_INSTANCE WHERE tenant_id = ?) AND d.tenant_id = ? AND d.rss_instance_id=r.rss_instance_id");
            stmt.setInt(1, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseInstanceEntry entry = this.createDatabaseInstanceEntryFromRS(rs);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return entries;
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve database instance entry list");
        }
    }

    private DatabaseInstanceEntry createDatabaseInstanceEntryFromRS(
            ResultSet rs) throws SQLException, RSSDAOException {
        int dbInsId = rs.getInt(1);
        String dbName = rs.getString(2);
        int dbTenantId = rs.getInt(3);
        int rssInsId = rs.getInt(4);
        String rssName = rs.getString(5);
        String rssServerUrl = rs.getString(6);
        int rssTenantId = rs.getInt(7);

        if (rssTenantId == MultitenantConstants.SUPER_TENANT_ID &&
                dbTenantId != MultitenantConstants.SUPER_TENANT_ID) {
            rssName = RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE;
        }
        return new DatabaseInstanceEntry(dbInsId, dbName, rssServerUrl + "/" + dbName, rssInsId,
                rssName, RSSManagerUtil.getTenantDomain(rssTenantId));
    }

    public RSSInstance getRSSInstanceDataById(int rssInsId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT rss_instance_id, name, " +
                    "server_url, dbms_type, instance_type, server_category, admin_username, admin_password, " +
                    "tenant_id FROM RSS_INSTANCE WHERE rss_instance_id = ? AND tenant_id = ?");
            stmt.setInt(1, rssInsId);
            stmt.setInt(2, CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.createRSSInstanceFromRS(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error in retrieving RSS instance", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

}
