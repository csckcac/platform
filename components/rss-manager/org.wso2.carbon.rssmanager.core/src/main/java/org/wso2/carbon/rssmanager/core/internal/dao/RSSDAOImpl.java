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
package org.wso2.carbon.rssmanager.core.internal.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.internal.util.RSSConfig;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public void createRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "INSERT INTO RM_SERVER_INSTANCE (name, server_url, dbms_type, instance_type, server_category, admin_username, admin_password, tenant_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setString(2, rssInstance.getServerURL());
            stmt.setString(3, rssInstance.getDbmsType());
            stmt.setString(4, rssInstance.getInstanceType());
            stmt.setString(5, rssInstance.getServerCategory());
            stmt.setString(6, rssInstance.getAdminUsername());
            stmt.setString(7, rssInstance.getAdminPassword());
            stmt.setInt(8, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating the RSS instance '" +
                    rssInstance.getName() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<RSSInstance> getAllTenantSpecificRSSInstances() throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "SELECT name, server_url, dbms_type, instance_type, server_category, admin_username, admin_password, tenant_id FROM RM_SERVER_INSTANCE WHERE tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving all RSS instances", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void updateRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "UPDATE RM_SERVER_INSTANCE SET server_url = ?, dbms_type = ?, instance_type = ?, server_category = ?, admin_username = ?, admin_password = ? WHERE name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getServerURL());
            stmt.setString(2, rssInstance.getDbmsType());
            stmt.setString(3, rssInstance.getInstanceType());
            stmt.setString(4, rssInstance.getServerCategory());
            stmt.setString(5, rssInstance.getAdminUsername());
            stmt.setString(6, rssInstance.getAdminPassword());
            stmt.setString(7, rssInstance.getName());
            stmt.setInt(8, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while editing the RSS instance '" +
                    rssInstance.getName() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public RSSInstance getRSSInstance(String rssInstanceName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        RSSInstance rssInstance = null;
        try {
            String sql = "SELECT name, server_url, dbms_type, instance_type, server_category, tenant_id FROM RM_SERVER_INSTANCE WHERE name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rssInstance = this.createRSSInstanceFromRS(rs);
            }
            rs.close();
            stmt.close();
            return rssInstance;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the configuration of " +
                    "RSS instance '" + rssInstanceName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseUser> getUsersByDatabase(String databaseName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        try {
            String sql = "SELECT d.username, d.db_username, d.rss_instance_name FROM RM_DATABASE_USER d, RM_USER_DATABASE_ENTRY u WHERE u.database_name=? AND d.username=u.username AND d.tenant_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserFromRS(rs));
            }
            rs.close();
            stmt.close();
            return users;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the users attached to " +
                    "the database '" + databaseName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public Database getDatabase(String databaseName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        Database database = null;
        try {
            int tenantID = CarbonContext.getCurrentContext().getTenantId();
            String sql = "SELECT d.name, d.tenant_id, r.name, r.server_url, r.tenant_id FROM RM_SERVER_INSTANCE r, RM_DATABASE d WHERE r.name=(SELECT rss_instance_name FROM RM_DATABASE WHERE tenant_id=? AND name=?) AND d.tenant_id=? AND d.name=? AND d.rss_instance_name=r.name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantID);
            stmt.setString(2, databaseName);
            stmt.setInt(3, tenantID);
            stmt.setString(4, databaseName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                database = this.createDatabaseFromRS(rs);
            }
            rs.close();
            stmt.close();
            return database;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the configuration of " +
                    "database '" + databaseName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabaseUser getDatabaseUser(String username) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        DatabaseUser user = new DatabaseUser();
        try {
            String sql = "SELECT username, rss_instance_name FROM RM_DATABASE_USER WHERE username = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.createDatabaseUserFromRS(rs);
            }
            rs.close();
            stmt.close();
            return user;
        } catch (SQLException e) {
            throw new RSSManagerException("Error while occurred while retrieving information of " +
                    "the database user '" + user.getUsername() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void createDatabase(Database database) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO RM_DATABASE (name, rss_instance_name, tenant_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, database.getName());
            stmt.setString(2, database.getRssInstanceName());
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            this.setDatabaseInstanceProperties(conn, database);

            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating the database " +
                    database.getName() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void setDatabaseInstanceProperties(Connection conn, Database database) throws
            SQLException {
        Map<String, String> existingProps = this.getDatabaseProperties(conn, database.getName());
        Map<String, String> newProps = database.getProperties();
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
                this.deleteDatabaseProperty(conn, database.getName(), key);
            }
            for (Map.Entry<String, String> entry : toBeAddedProps.entrySet()) {
                this.addDatabaseProperty(conn, database.getName(), entry.getKey(),
                        entry.getValue());
            }
        }
    }

    private void addDatabaseProperty(Connection conn, String databaseName, String key,
                                     String value) throws SQLException {
        String sql = "INSERT INTO RM_DATABASE_PROPERTY (name, value, database_name) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, key);
        stmt.setString(2, value);
        stmt.setString(3, databaseName);
        stmt.executeUpdate();
        stmt.close();
    }

    private void deleteDatabaseProperty(
            Connection conn, String databaseName, String key) throws SQLException {
        String sql = "DELETE FROM RM_DATABASE_PROPERTY WHERE name = ? AND database_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, key);
        stmt.setString(2, databaseName);
        stmt.executeUpdate();
        stmt.close();
    }

    private Map<String, String> getDatabaseProperties(
            Connection conn, String databaseName) throws SQLException {
        Map<String, String> props = new HashMap<String, String>();
        String sql = "SELECT name, value FROM RM_DATABASE_PROPERTY WHERE database_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, databaseName);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            props.put(rs.getString("name"), rs.getString("value"));
        }
        rs.close();
        stmt.close();
        return props;
    }

    private RSSInstance createRSSInstanceFromRS(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String serverURL = rs.getString("server_url");
        String instanceType = rs.getString("instance_type");
        String serverCategory = rs.getString("server_category");
        String adminUsername = rs.getString("admin_username");
        String adminPassword = rs.getString("admin_password");
        String dbmsType = rs.getString("dbms_type");
        int tenantId = rs.getInt("tenant_id");
        return new RSSInstance(name, serverURL, dbmsType, instanceType, serverCategory,
                adminUsername, adminPassword, tenantId);
    }

    @Override
    public List<RSSInstance> getAllSystemRSSInstances() throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT name, " +
                    "server_url, dbms_type, instance_type, server_category, admin_username, admin_password, tenant_id" +
                    " FROM RM_SERVER_INSTANCE WHERE instance_type = ? AND tenant_id = ?");
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            ResultSet rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving system RSS " +
                    "instances", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void dropRSSInstance(String rssInstanceName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            List<DatabaseUser> users =
                    this.getDatabaseUsersByRSSInstance(conn, rssInstanceName);
            if (users.size() > 0) {
                for (DatabaseUser user : users) {
                    this.dropDatabaseUser(rssInstanceName, user.getUsername());
                }
            }
            String sql = "DELETE FROM RM_SERVER_INSTANCE WHERE name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the RSS instance '" +
                    rssInstanceName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private List<DatabaseUser> getDatabaseUsersByRSSInstance(
            Connection conn, String rssInstanceName) throws SQLException {
        String sql = "SELECT username, rss_instance_name, tenant_id FROM RM_DATABASE_USER WHERE rss_instance_name=? AND tenant_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, rssInstanceName);
        stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
        ResultSet rs = stmt.executeQuery();
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        while (rs.next()) {
            users.add(this.createDatabaseUserFromRS(rs));
        }
        rs.close();
        stmt.close();
        return users;
    }

    @Override
    public List<Database> getAllDatabases(int tid) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "SELECT d.name, d.tenant_id, r.name, r.server_url, r.tenant_id FROM RM_SERVER_INSTANCE r, RM_DATABASE d WHERE r.name IN (SELECT rss_instance_name FROM RM_DATABASE) AND r.name=d.rss_instance_name AND d.tenant_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tid);
            ResultSet rs = stmt.executeQuery();
            List<Database> result = new ArrayList<Database>();
            while (rs.next()) {
                Database entry = this.createDatabaseFromRS(rs);
                if (entry != null) {
                    result.add(entry);
                }
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving all databases", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<Database> getAllDatabasesByRSSInstance(String rssInstanceName) throws
            RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        List<Database> dbs = new ArrayList<Database>();
        try {
            String sql = "SELECT rss_instance_name, name, tenant_id FROM RM_DATABASE WHERE rss_instance_name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Database db = this.createDatabaseFromRS(rs);
                if (db != null) {
                    dbs.add(db);
                }
            }
            rs.close();
            stmt.close();
            return dbs;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving database list " +
                    "created in the RSS instance '" + rssInstanceName + "'");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void dropDatabase(String rssInstanceName, String databaseName)
            throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "DELETE FROM RM_DATABASE WHERE name = ? AND tenant_id = ? AND rss_instance_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            stmt.setString(3, rssInstanceName);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the database '" +
                    databaseName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        String sql = "INSERT INTO RM_DATABASE_USER (username, rss_instance_name, tenant_id) VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getRssInstanceName());
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            
            stmt.execute();
            stmt.close();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RSSManagerException("Error occurred while creating the database user '" +
                    user.getUsername() + "'");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    @Override
    public void dropDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "DELETE FROM RM_DATABASE_USER WHERE username = ? AND rss_instance_name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, rssInstanceName);
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the database user '" +
                    username + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void updateDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "UPDATE RM_DATABASE_USER SET rss_instance_name = ? WHERE username = ? and tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getRssInstanceName());
            stmt.setString(2, user.getUsername());
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while updating the database user '" +
                    user.getUsername() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseUser> getAllDatabaseUsers(int tid) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        try {
            String sql = "SELECT username, rss_instance_name, tenant_id FROM RM_DATABASE_USER WHERE tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserFromRS(rs));
            }
            rs.close();
            stmt.close();
            return users;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database users ", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabaseUser> getUsersByRSSInstance(String rssInstanceName) throws
            RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        try {
            String sql = "SELECT username, rss_instance_name, tenant_id FROM RM_DATABASE_USER WHERE tenant_id = ? AND rss_instance_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, CarbonContext.getCurrentContext().getTenantId());
            stmt.setString(2, rssInstanceName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserFromRS(rs));
            }
            rs.close();
            stmt.close();
            return users;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database users ", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<String> getAvailableUsersToBeAssigned(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        List<String> availableUsers = new ArrayList<String>();
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        String sql = "(SELECT username FROM RM_DATABASE_USER WHERE rss_instance_name = ? AND tenant_id = ?) INTERSECT (SELECT username, rss_instance_name FROM RM_USER_DATABASE_ENTRY WHERE rss_instance_name = ? AND database_name = ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            stmt.setString(3, rssInstanceName);
            stmt.setString(4, databaseName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availableUsers.add(rs.getString("username"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the users assigned " +
                    "to the database '" + databaseName + "'", e);
        }
        return availableUsers;
    }

    @Override
    public List<String> getUsersAssignedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        List<String> attachedUsers = new ArrayList<String>();
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        String sql = "SELECT username FROM RM_USER_DATABASE_ENTRY WHERE rss_instance_name = ? AND database_name = ? AND tenant_id = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setString(2, databaseName);
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attachedUsers.add(rs.getString("username"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the users assigned " +
                    "to the database '" + databaseName + "'", e);
        }
        return attachedUsers;
    }


    @Override
    public void addUserDatabaseEntry(UserDatabaseEntry userDBEntry)
            throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO RM_USER_DATABASE_ENTRY (username, database_name, rss_instance_name, tenant_id) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userDBEntry.getUsername());
            stmt.setString(2, userDBEntry.getDatabaseName());
            stmt.setString(3, userDBEntry.getRssInstanceName());
            stmt.setInt(4, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            this.setUserDatabasePrivileges(conn, userDBEntry);

            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while adding new user-database-entry", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private DatabaseUser createDatabaseUserFromRS(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String rssInstName = rs.getString("rss_instance_name");
        int tenantId = rs.getInt("tenant_id");
        return new DatabaseUser(username, null, rssInstName, tenantId);
    }

    @Override
    public void updateUserDatabaseEntry(UserDatabaseEntry userDBEntry)
            throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            this.updateUserDatabasePrivileges(conn, userDBEntry);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while updating user-database-entry", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void deleteUserDatabaseEntry(String rssInstanceName, String username)
            throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);
            /* delete permissions first */
            String sql = "DELETE FROM RM_USER_DATABASE_PRIVILEGE WHERE username = ? AND rss_instance_name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, rssInstanceName);
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();

            /* now delete the user-database-entry */
            sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE username = ? AND rss_instance_name = ? AND tenant_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, rssInstanceName);
            stmt.setInt(3, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e);
            }
            throw new RSSManagerException("Error occurred while deleting user-database-entry", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void incrementSystemRSSDatabaseCount() throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            String sql = "SELECT * FROM RM_SYSTEM_DATABASE_COUNT";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                sql = "INSERT INTO RM_SYSTEM_DATABASE_COUNT (count) VALUES (0)";
                stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();
            }
            sql = "UPDATE RM_SYSTEM_DATABASE_COUNT SET count = count + 1";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            conn.commit();

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while incrementing system RSS " +
                    "database count", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public int getSystemRSSDatabaseCount() throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        int count = 0;
        try {
            String sql = "SELECT count FROM RM_SYSTEM_DATABASE_COUNT";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving system RSS database " +
                    "count", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName, String username)
            throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        PreparedStatement stmt;
        try {
            String sql = "SELECT select_priv, insert_priv, update_priv, delete_priv, create_priv, drop_priv, grant_priv, references_priv, index_priv, alter_priv, create_tmp_table_priv, lock_tables_priv, create_view_priv, show_view_priv, create_routine_priv, alter_routine_priv, execute_priv, event_priv, trigger_priv FROM RM_USER_DATABASE_PRIVILEGE WHERE username = ? AND database_name = ? AND rss_instance_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, databaseName);
            stmt.setString(3, rssInstanceName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                privileges = this.createUserDatabasePrivilegeSetFromRS(rs);
            }
            rs.close();
            stmt.close();
            return privileges;
        } catch (SQLException e) {
            log.error(e);
            throw new RSSManagerException("Error occurred while retrieving user permissions " +
                    "granted for the database user '" + username + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }

    }

    @Override
    public List<RSSInstance> getAllRSSInstances(int tid) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "SELECT name, " +
                    "server_url, dbms_type, instance_type, server_category, tenant_id, admin_username, admin_password " +
                    "FROM RM_SERVER_INSTANCE WHERE tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tid);
            ResultSet rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving all RSS instances", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<UserDatabaseEntry> getUserDatabaseEntriesByDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            String sql = "SELECT username, database_name, rss_instance_name FROM RM_USER_DATABASE_ENTRY where database_name = ? AND rss_instance_name = ? ";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setString(2, rssInstanceName);
            ResultSet rs = stmt.executeQuery();
            List<UserDatabaseEntry> result = new ArrayList<UserDatabaseEntry>();
            while (rs.next()) {
                result.add(this.createUserDatabaseEntry(rs));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving User database " +
                    "entries", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private UserDatabaseEntry createUserDatabaseEntry(ResultSet rs) throws SQLException {
        String databaseName = rs.getString("database_name");
        String username = rs.getString("username");
        String rssInstanceName = rs.getString("rss_instance_name");
        return new UserDatabaseEntry(username, databaseName, rssInstanceName);
    }

    public void updateDatabaseUserPermission(Connection conn,
                                             String permName,
                                             String permValue,
                                             String username,
                                             String databaseName) throws RSSManagerException {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE RM_USER_DATABASE_PRIVILEGE " +
                    "SET perm_value=? WHERE username=? AND database_name=? AND perm_name=?");
            stmt.setString(1, permValue);
            stmt.setString(2, username);
            stmt.setString(3, databaseName);
            stmt.setString(4, permName);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while updating user database " +
                    "permission", e);
        }
    }

    @Override
    public void updateDatabaseUser(DatabasePermissions permissions, String username,
                                   String databaseName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        List<String> permissionNames =
                this.getExistingDatabasePermissions(conn, username, databaseName);
        for (Map.Entry entry : permissions.getPrivilegeMap().entrySet()) {
            String permName = entry.getKey().toString();
            if (permissionNames.contains(permName)) {
                String permValue = entry.getValue().toString();
                this.updateDatabaseUserPermission(conn, permName, permValue, username,
                        databaseName);
            }
        }
    }

    @Override
    public void createDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE(name, tenant_id) VALUES(?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, template.getName());
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            stmt.execute();

            this.setDatabasePrivilegeTemplateProperties(conn, template);

            conn.commit();
            stmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while creating database privilege " +
                    "template '" + template.getName() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void setDatabasePrivilegeTemplateProperties(
            Connection conn, DatabasePrivilegeTemplate template) throws SQLException {
        DatabasePrivilegeSet privileges = template.getPrivileges();

        String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE_ENTRY(template_name, tenant_id, select_priv, insert_priv, update_priv, delete_priv, create_priv, drop_priv, grant_priv, references_priv, index_priv, alter_priv, create_tmp_table_priv, lock_tables_priv, create_view_priv, show_view_priv, create_routine_priv, alter_routine_priv, execute_priv, event_priv, trigger_priv) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, template.getName());
        stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
        stmt.setString(3, privileges.getSelectPriv());
        stmt.setString(4, privileges.getInsertPriv());
        stmt.setString(5, privileges.getUpdatePriv());
        stmt.setString(6, privileges.getDeletePriv());
        stmt.setString(7, privileges.getCreatePriv());
        stmt.setString(8, privileges.getDropPriv());
        stmt.setString(9, privileges.getGrantPriv());
        stmt.setString(10, privileges.getReferencesPriv());
        stmt.setString(11, privileges.getIndexPriv());
        stmt.setString(12, privileges.getAlterPriv());
        stmt.setString(13, privileges.getCreateTmpTablePriv());
        stmt.setString(14, privileges.getLockTablesPriv());
        stmt.setString(15, privileges.getCreateViewPriv());
        stmt.setString(16, privileges.getShowViewPriv());
        stmt.setString(17, privileges.getCreateRoutinePriv());
        stmt.setString(18, privileges.getAlterRoutinePriv());
        stmt.setString(19, privileges.getExecutePriv());
        stmt.setString(20, privileges.getEventPriv());
        stmt.setString(21, privileges.getTriggerPriv());
        stmt.executeUpdate();
        
        stmt.close();
    }

    private void setUserDatabasePrivileges(
            Connection conn, UserDatabaseEntry entry) throws SQLException {
        DatabasePrivilegeSet privileges = entry.getPrivileges();

        String sql = "INSERT INTO RM_USER_DATABASE_PRIVILEGE(username, database_name, rss_instance_name, tenant_id, select_priv, insert_priv, update_priv, delete_priv, create_priv, drop_priv, grant_priv, references_priv, index_priv, alter_priv, create_tmp_table_priv, lock_tables_priv, create_view_priv, show_view_priv, create_routine_priv, alter_routine_priv, execute_priv, event_priv, trigger_priv) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, entry.getUsername());
        stmt.setString(2, entry.getDatabaseName());
        stmt.setString(3, entry.getRssInstanceName());
        stmt.setInt(4, CarbonContext.getCurrentContext().getTenantId());
        stmt.setString(5, privileges.getSelectPriv());
        stmt.setString(6, privileges.getInsertPriv());
        stmt.setString(7, privileges.getUpdatePriv());
        stmt.setString(8, privileges.getDeletePriv());
        stmt.setString(9, privileges.getCreatePriv());
        stmt.setString(10, privileges.getDropPriv());
        stmt.setString(11, privileges.getGrantPriv());
        stmt.setString(12, privileges.getReferencesPriv());
        stmt.setString(13, privileges.getIndexPriv());
        stmt.setString(14, privileges.getAlterPriv());
        stmt.setString(15, privileges.getCreateTmpTablePriv());
        stmt.setString(16, privileges.getLockTablesPriv());
        stmt.setString(17, privileges.getCreateViewPriv());
        stmt.setString(18, privileges.getShowViewPriv());
        stmt.setString(19, privileges.getCreateRoutinePriv());
        stmt.setString(20, privileges.getAlterRoutinePriv());
        stmt.setString(21, privileges.getExecutePriv());
        stmt.setString(22, privileges.getEventPriv());
        stmt.setString(23, privileges.getTriggerPriv());
        stmt.executeUpdate();
        stmt.close();
    }

    private void updateUserDatabasePrivileges(Connection conn,
                                             UserDatabaseEntry userDBEntry) throws SQLException {
        DatabasePrivilegeSet privileges = userDBEntry.getPrivileges();

        String sql = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET select_priv = ?, insert_priv = ?, update_priv = ?, delete_priv = ?, create_priv = ?, drop_priv = ?, grant_priv = ?, references_priv = ?, index_priv = ?, alter_priv = ?, create_tmp_table_priv = ?, lock_tables_priv = ?, create_view_priv = ?, show_view_priv = ?, create_routine_priv = ?, alter_routine_priv = ?, execute_priv = ?, event_priv = ?, trigger_priv = ? WHERE username = ? AND database_name = ? AND rss_instance_name = ? AND tenant_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, privileges.getSelectPriv());
        stmt.setString(2, privileges.getInsertPriv());
        stmt.setString(3, privileges.getUpdatePriv());
        stmt.setString(4, privileges.getDeletePriv());
        stmt.setString(5, privileges.getCreatePriv());
        stmt.setString(6, privileges.getDropPriv());
        stmt.setString(7, privileges.getGrantPriv());
        stmt.setString(8, privileges.getReferencesPriv());
        stmt.setString(9, privileges.getIndexPriv());
        stmt.setString(10, privileges.getAlterPriv());
        stmt.setString(11, privileges.getCreateTmpTablePriv());
        stmt.setString(12, privileges.getLockTablesPriv());
        stmt.setString(13, privileges.getCreateViewPriv());
        stmt.setString(14, privileges.getShowViewPriv());
        stmt.setString(15, privileges.getCreateRoutinePriv());
        stmt.setString(16, privileges.getAlterRoutinePriv());
        stmt.setString(17, privileges.getExecutePriv());
        stmt.setString(18, privileges.getEventPriv());
        stmt.setString(19, privileges.getTriggerPriv());
        stmt.setString(20, userDBEntry.getUsername());
        stmt.setString(21, userDBEntry.getDatabaseName());
        stmt.setString(22, userDBEntry.getRssInstanceName());
        stmt.setInt(23, CarbonContext.getCurrentContext().getTenantId());

        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        try {
            conn.setAutoCommit(false);

            this.removeDatabasePrivilegesTemplateEntries(conn, templateName);
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE WHERE name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();
            conn.commit();

            stmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while dropping the database privilege " +
                    "template '" + templateName + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void removeDatabasePrivilegesTemplateEntries(
            Connection conn, String templateName) throws SQLException {
        String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE template_name=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, templateName);
        stmt.executeUpdate();

        stmt.close();
    }

    @Override
    public void editDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        DatabasePrivilegeSet privileges = template.getPrivileges();
        try {
            String sql = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET select_priv = ?, insert_priv = ?, update_priv = ?, delete_priv = ?, create_priv = ?, drop_priv = ?, grant_priv = ?, references_priv = ?, index_priv = ?, alter_priv = ?, create_tmp_table_priv = ?, lock_tables_priv = ?, create_view_priv = ?, show_view_priv = ?, create_routine_priv = ?, alter_routine_priv = ?, execute_priv = ?, event_priv = ?, trigger_priv = ? WHERE template_name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, privileges.getSelectPriv());
            stmt.setString(2, privileges.getInsertPriv());
            stmt.setString(3, privileges.getUpdatePriv());
            stmt.setString(4, privileges.getDeletePriv());
            stmt.setString(5, privileges.getCreatePriv());
            stmt.setString(6, privileges.getDropPriv());
            stmt.setString(7, privileges.getGrantPriv());
            stmt.setString(8, privileges.getReferencesPriv());
            stmt.setString(9, privileges.getIndexPriv());
            stmt.setString(10, privileges.getAlterPriv());
            stmt.setString(11, privileges.getCreateTmpTablePriv());
            stmt.setString(12, privileges.getLockTablesPriv());
            stmt.setString(13, privileges.getCreateViewPriv());
            stmt.setString(14, privileges.getShowViewPriv());
            stmt.setString(15, privileges.getCreateRoutinePriv());
            stmt.setString(16, privileges.getAlterRoutinePriv());
            stmt.setString(17, privileges.getExecutePriv());
            stmt.setString(18, privileges.getEventPriv());
            stmt.setString(19, privileges.getTriggerPriv());
            stmt.setString(20, template.getName());
            stmt.setInt(21, CarbonContext.getCurrentContext().getTenantId());
            stmt.executeUpdate();

            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while editing the database privilege " +
                    "template '" + template.getName() + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public List<DatabasePrivilegeTemplate> getAllDatabasePrivilegesTemplates(int tid) throws
            RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        PreparedStatement stmt;
        try {
            String sql = "SELECT name, tenant_id FROM RM_DB_PRIVILEGE_TEMPLATE WHERE tenant_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tid);
            ResultSet rs = stmt.executeQuery();
            List<DatabasePrivilegeTemplate> result = new ArrayList<DatabasePrivilegeTemplate>();
            while (rs.next()) {
                result.add(this.createDatabasePrivilegeTemplateFromRS(rs));
            }
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving database privilege " +
                    "templates", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    @Override
    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            String templateName) throws RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        PreparedStatement stmt;
        DatabasePrivilegeTemplate template = null;
        try {
            String sql = "SELECT name, tenant_id FROM RM_DB_PRIVILEGE_TEMPLATE WHERE name = ? AND tenant_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, CarbonContext.getCurrentContext().getTenantId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                template = this.createDatabasePrivilegeTemplateFromRS(rs);
            }
            rs.close();
            stmt.close();
            return template;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving database privilege " +
                    "template information", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private DatabasePrivilegeSet getUserPrivilegeGroupEntries(String templateName, int tenantId)
            throws SQLException, RSSManagerException {
        Connection conn = RSSConfig.getInstance().getRSSDBConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT select_priv, insert_priv, update_priv, delete_priv, create_priv, drop_priv, grant_priv, references_priv, index_priv, alter_priv, create_tmp_table_priv, lock_tables_priv, create_view_priv, show_view_priv, create_routine_priv, alter_routine_priv, execute_priv, event_priv, trigger_priv " +
                "FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE template_name = ? AND tenant_id = ?");
        stmt.setString(1, templateName);
        stmt.setInt(2, tenantId);
        ResultSet rs = stmt.executeQuery();
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        if (rs.next()) {
            privileges = this.createUserDatabasePrivilegeSetFromRS(rs);
        }
        rs.close();
        stmt.close();
        return privileges;
    }

    private DatabasePrivilegeSet createUserDatabasePrivilegeSetFromRS(ResultSet rs) throws
            SQLException {
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        privileges.setSelectPriv(rs.getString("select_priv"));
        privileges.setInsertPriv(rs.getString("insert_priv"));
        privileges.setUpdatePriv(rs.getString("update_priv"));
        privileges.setDeletePriv(rs.getString("delete_priv"));
        privileges.setCreatePriv(rs.getString("create_priv"));
        privileges.setDropPriv(rs.getString("drop_priv"));
        privileges.setGrantPriv(rs.getString("grant_priv"));
        privileges.setReferencesPriv(rs.getString("references_priv"));
        privileges.setIndexPriv(rs.getString("index_priv"));
        privileges.setAlterPriv(rs.getString("alter_priv"));
        privileges.setCreateTmpTablePriv(rs.getString("create_tmp_table_priv"));
        privileges.setLockTablesPriv(rs.getString("lock_tables_priv"));
        privileges.setCreateViewPriv(rs.getString("create_view_priv"));
        privileges.setShowViewPriv(rs.getString("show_view_priv"));
        privileges.setCreateRoutinePriv(rs.getString("create_routine_priv"));
        privileges.setAlterRoutinePriv(rs.getString("alter_routine_priv"));
        privileges.setExecutePriv(rs.getString("execute_priv"));
        privileges.setEventPriv(rs.getString("event_priv"));
        privileges.setTriggerPriv(rs.getString("trigger_priv"));

        return privileges;
    }

    private DatabasePrivilegeTemplate createDatabasePrivilegeTemplateFromRS(ResultSet rs) throws
            SQLException, RSSManagerException {
        String templateName = rs.getString("name");
        int tid = rs.getInt("tenant_id");
        DatabasePrivilegeSet privileges = this.getUserPrivilegeGroupEntries(templateName, tid);
        return new DatabasePrivilegeTemplate(templateName, privileges);
    }

    private List<String> getExistingDatabasePermissions(
            Connection conn, String username, String databaseName) throws RSSManagerException {
        try {
            String sql = "SELECT perm_name " +
                    "FROM RM_USER_DATABASE_PRIVILEGE WHERE username=? AND database_name=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, databaseName);
            ResultSet rs = stmt.executeQuery();
            List<String> permNames = new ArrayList<String>();
            while (rs.next()) {
                permNames.add(rs.getString("perm_name"));
            }
            rs.close();
            stmt.close();
            return permNames;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving existing database " +
                    "permissions granted for the database user '" + username + "'", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private Database createDatabaseFromRS(ResultSet rs) throws SQLException,
            RSSManagerException {
        String dbName = rs.getString(1);
        int dbTenantId = rs.getInt(2);
        String rssName = rs.getString(3);
        String rssServerUrl = rs.getString(4);
        int rssTenantId = rs.getInt(5);

        if (rssTenantId == MultitenantConstants.SUPER_TENANT_ID &&
                dbTenantId != MultitenantConstants.SUPER_TENANT_ID) {
            rssName = RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE;
        }
        String url = rssServerUrl + "/" + dbName;
        return new Database(dbName, rssName, url, rssTenantId);
    }

}
