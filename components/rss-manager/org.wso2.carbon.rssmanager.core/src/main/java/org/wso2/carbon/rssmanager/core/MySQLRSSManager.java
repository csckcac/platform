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
package org.wso2.carbon.rssmanager.core;

import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.description.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class carries all the RSS manager related functionality implemented upon Mysql.
 */
public class MySQLRSSManager implements RSSManager {

    private static RSSManager thisInstance = new MySQLRSSManager();

    private MySQLRSSManager() {
    }

    public static synchronized RSSManager getInstance() {
        return thisInstance;
    }

    /**
     * Creates the database in the desired database server instance.
     *
     * @param db Information of the database to be created.
     * @throws RSSDAOException rssDaoException.
     */
    public void createDatabase(DatabaseInstance db) throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        String fullyQualifiedDbName = RSSManagerUtil.getFullyQualifiedDatabaseName(db.getName());

        try {
            if (RSSManagerUtil.databaseExists(fullyQualifiedDbName)) {
                throw new RSSDAOException("Database " + fullyQualifiedDbName + " already exists");
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to check for the existence of the database " + 
                    fullyQualifiedDbName);
        }

        /* creates the database with its fully qualified name in the database server*/
        createDatabaseEntryInSystemTables(db.getRssInstanceId(), fullyQualifiedDbName);
        db.setName(fullyQualifiedDbName);

        /* adds the database information to the rss_database which is  */
        dao.addDatabaseInstance(db);
        dao.incrementServiceProviderHostedRSSDatabaseInstanceCount();
    }

    /**
     * Creates the database physically in the database server and updates the mysql system tables
     * with the details of the created database.
     *
     * @param rssInsId Id of the RSS instance that includes the database.
     * @param dbName   Name of the database.
     * @throws RSSDAOException rssDAOException.
     */
    private void createDatabaseEntryInSystemTables(int rssInsId,
                                                   String dbName) throws RSSDAOException {
        RSSInstance rssIns = RSSDAOFactory.getRSSDAO().getRSSInstanceById(rssInsId);
        if (rssIns == null) {
            throw new RSSDAOException("RSS instance does not exist");
        }
        String sqlQuery = "CREATE DATABASE " + dbName;
        try {
            PreparedStatement stmt =
                    DBConnectionHandler.getConnection(rssIns).prepareStatement(sqlQuery);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to create database " + dbName, e);
        } finally {
            DBConnectionHandler.closeConnection();
        }
    }

    /**
     * Drops the database from the system databases of the given sql server instance.
     *
     * @param dbInsId  Id of the database instance to be dropped.
     * @throws RSSDAOException rssDaoException.
     */
    public void dropDatabase(int dbInsId) throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        List<UserDatabaseEntry> entries =
                dao.getUserDatabaseEntriesByDatabaseInstanceId(dbInsId);

        /* the real dropping of the database from the database server happens here */
        try {
            dropDatabaseFromSystemTables(dbInsId);
        } catch (SQLException e) {
            throw new RSSDAOException("Error while dropping the database", e);
        }

        /* removing the entries in the UserDatabaseEntry table which has a foreign key constraint to
         * to the DatabaseInstance table in the rss metadata repository. These entries correspond to
         * the user-databaseInstance relationship that exists between a particular database and a
         * set of database users */
        if (entries != null && entries.size() > 0) {
            for (UserDatabaseEntry entry : entries) {
                dao.deleteUserDatabaseEntry(entry.getUserId(), entry.getDatabaseInstanceId());
            }
        }
        /* deleting the entry corresponds to the given database from the rss metadata repository */
        dao.deleteDatabaseInstance(dbInsId);
    }

    /**
     * Removes the entries corresponding to the database from mysql system tables.
     *
     * @param dbInsId  Id of the database.
     * @throws RSSDAOException rssDaoException
     * @throws SQLException    sqlException
     */
    private void dropDatabaseFromSystemTables(int dbInsId) throws RSSDAOException,
            SQLException {
        PreparedStatement stmt = null;
        RSSDAO dao = RSSDAOFactory.getRSSDAO();

        DatabaseInstance db = dao.getDatabaseInstanceById(dbInsId);
        if (db == null) {
            throw new RSSDAOException("Database instance does not exist");
        }
        RSSInstance rssIns = dao.getRSSInstanceById(db.getRssInstanceId());
        if (rssIns == null) {
            throw new RSSDAOException("Database server does not exist");
        }

        String dropDatabaseQuery = "DROP DATABASE " + db.getName();
        try {
            stmt = DBConnectionHandler.getConnection(rssIns).prepareStatement(dropDatabaseQuery);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Error while dropping the database " + db.getName(), e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            DBConnectionHandler.closeConnection();
        }
    }

    /**
     * Creating a database user to be used in carrying out database manipulations.
     *
     * @param user        Details of the user to be added.
     * @param privGroupId Id of the privilege group that carries the necessary privileges to be assigned.
     * @param dbInsId     Id of the database that the user should be assigned to.
     * @throws RSSDAOException rssDaoException
     * @throws SQLException throws a SQLException if the the database related executions become
     *                      unsuccessful.
     */
    public void createUser(DatabaseUser user, int privGroupId,
                           int dbInsId) throws RSSDAOException, SQLException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        Connection conn = null;
        try {
            List<DatabasePrivilege> privs =
                    Arrays.asList(dao.getPrivilegeGroupById(privGroupId).getPrivs());
            DatabaseInstanceEntry db = dao.getDatabaseInstanceEntryById(dbInsId);
            if (db == null) {
                throw new RSSDAOException("Unable to create user " + user.getUsername() + ". " +
                        "The referred database instance does not exist");
            }
            /* Setting the fully qualified username corresponds to the database user */
            user.setUsername(RSSManagerUtil.getFullyQualifiedUsername(user.getUsername()));
            user.setRssInstanceId(db.getRssInstanceId());

            RSSInstance rssIns = dao.getRSSInstanceById(db.getRssInstanceId());
            if (RSSManagerUtil.validateUser(rssIns, user.getUsername())) {
                throw new RSSDAOException("A user with the name " + user.getUsername() +
                        " already exists");
            }

            String systemDbUrl = RSSManagerUtil.processJdbcUrl(rssIns.getServerURL(),
                    RSSManagerConstants.MYSQL_SYSTEM_DB);
            rssIns.setServerURL(systemDbUrl);
            conn = DBConnectionHandler.getConnection(rssIns);

            /* Adding the user to mysql system tables */
            conn.setAutoCommit(false);
            insertIntoUserTable(conn, user);
            insertIntoDbTable(conn, db.getDbName(), user.getUsername(), privs);
            flushPrivileges(conn);
            conn.commit();

            /* Adding the user to metadata */
            int addedRecordNum = RSSDAOFactory.getRSSDAO().addUser(user);
            UserDatabaseEntry ude = new UserDatabaseEntry(addedRecordNum, dbInsId);
            ude.setPermissions(RSSManagerUtil.convertPrivListToMap(privs));
            dao.addUserDatabaseEntry(ude);
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new RSSDAOException("Unable to create the database user", e);
        } finally {
            DBConnectionHandler.closeConnection();
        }
    }

    /**
     * Updates the mysq.user table with the details of the newly created database user.
     *
     * @param conn Connection to the mysql system database.
     * @param user Details of the newly created database user.
     * @throws SQLException throws a SQLException if the the database related executions become
     *                      unsuccessful.
     * @throws org.wso2.carbon.rssmanager.core.RSSDAOException rssDAOException.
     */
    private void insertIntoUserTable(Connection conn, DatabaseUser user)
            throws RSSDAOException, SQLException {
        String sql = "CREATE USER '" + user.getUsername() + "'@'%' IDENTIFIED BY '" + user.getPassword() + "'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    /**
     * Updates the mysql.db system table with the details of the user assigned to the given database.
     *
     * @param conn     Database connection to the mysql system database.
     * @param dbName   Name of the database to which the database user is assigned to.
     * @param username Username of the database user.
     * @param privs    The set of database level privileges to be assigned to the database user.
     * @throws SQLException throws a SQLException if the the database related executions become
     *                      unsuccessful.
     */
    private void insertIntoDbTable(Connection conn, String dbName, String username,
                                   List<DatabasePrivilege> privs) throws SQLException {
        String sql = "INSERT INTO mysql.db VALUES(?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%");
        stmt.setString(2, dbName);
        stmt.setString(3, username);
        List<String> dbPrivs = RSSManagerCommonUtil.getDatabasePrivilegeList();
        Map<String, Object> privMap = RSSManagerUtil.convertPrivListToMap(privs);
        for (int i = 4; i < dbPrivs.size() + 4; i++) {
            if (privMap.containsKey(dbPrivs.get(i - 4))) {
                stmt.setString(i, privMap.get(dbPrivs.get(i - 4)).toString());
            }
        }
        stmt.execute();
    }

    /**
     * Flushes the system tables after the updates.
     *
     * @param conn Connection to the mysql system database.
     * @throws SQLException throws a SQLException if the the database related executions become
     *                      unsuccessful.
     */
    private void flushPrivileges(Connection conn) throws SQLException {
        String sql = "FLUSH PRIVILEGES";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    /**
     * Deletes a particular user from a RSS instance and detach from the database instance to
     * which it has been assigned to.
     *
     * @param userId  Id of the user to be removed.
     * @param dbInsId database instance id.
     * @throws RSSDAOException rssDaoException.
     */
    public void dropUser(int userId, int dbInsId) throws RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        try {
            dropUserFromSystemTables(userId);
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to drop user", e);
        }

        dao.deleteUserDatabaseEntry(userId, dbInsId);
        dao.deleteUser(userId);
    }

    /**
     * Drops the entries corresponding to the user from myql system tables.
     *
     * @param userId Id of the database user.
     * @throws RSSDAOException rssDaoException.
     * @throws SQLException    sqlException.
     */
    private void dropUserFromSystemTables(int userId) throws RSSDAOException, SQLException {
        PreparedStatement stmt = null;
        RSSDAO dao = RSSDAOFactory.getRSSDAO();

        DatabaseUser user = dao.getUserById(userId);
        if (user == null) {
            throw new RSSDAOException("Database user does not exist");
        }
        RSSInstance rssIns = dao.getRSSInstanceById(user.getRssInstanceId());
        if (rssIns == null) {
            throw new RSSDAOException("RSS instance does not exist");
        }

        String dropUserQuery = "DROP USER '" + user.getUsername() + "'@'%'";
        try {
            stmt = DBConnectionHandler.getConnection(rssIns).prepareStatement(dropUserQuery);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to delete user " + user.getUsername(), e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            DBConnectionHandler.closeConnection();
        }
    }

    /**
     * Edits the user privileges replacing the existing values depending upon the user request.
     *
     * @param permissions The set of database privileges to be assigned.
     * @param user        Information of the user to whom the privileges should be assigned to.
     * @param dbInsId     Id of the database instance to which the user is assigned to.
     * @throws RSSDAOException rssDaoException.
     */
    public void editUserPrivileges(DatabasePermissions permissions, DatabaseUser user,
                                   int dbInsId) throws RSSDAOException {
        Connection conn;
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        if (!RSSManagerUtil.userBelongsToCurrentTenant(user.getUserId())) {
            throw new RSSDAOException("User " + user.getUsername() +
                    " does not belong to the current tenant.");
        }
        try {
            DatabaseInstanceEntry db = dao.getDatabaseInstanceEntryById(dbInsId);
            if (db == null) {
                throw new RSSDAOException("Database instance does not exist");
            }
            RSSInstance rssIns = dao.getRSSInstanceById(db.getRssInstanceId());
            if (rssIns == null) {
                throw new RSSDAOException("RSS instance does not exist");
            }
            String systemDbUrl = RSSManagerUtil.processJdbcUrl(rssIns.getServerURL(),
                    RSSManagerConstants.MYSQL_SYSTEM_DB);
            rssIns.setServerURL(systemDbUrl);
            conn = DBConnectionHandler.getConnection(rssIns);

            /* updating the privileges in mysql system tables */
            conn.setAutoCommit(false);
            updatePrivilegesInSystemTables(conn, permissions, user, dbInsId);
            /* updating the password in mysql system table */
            updatePasswordInSystemTable(conn, user, dbInsId);
            flushPrivileges(conn);
            conn.commit();

            /* updating meta data with the changed privileges */
            dao.updateUser(permissions, user.getUserId(), dbInsId);
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to edit user privileges", e);
        } finally {
            DBConnectionHandler.closeConnection();
        }
    }

    /**
     * Updates the mysql system tables of the changed privileges.
     *
     * @param conn        Connection to the mysql system database.
     * @param permissions The set of permissions to be updated.
     * @param user        Information of the user to whom the privileges are applied to.
     * @param dbInsId     Id of the database to which the user is assigned to.
     * @throws RSSDAOException rssDaoException.
     * @throws SQLException throws a SQLException if the the database related executions become
     *                      unsuccessful.
     */
    private void updatePrivilegesInSystemTables(Connection conn,
                                                DatabasePermissions permissions,
                                                DatabaseUser user,
                                                int dbInsId) throws SQLException, RSSDAOException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        String prefixDbUpdateQuery = "UPDATE mysql.db SET ";
        String suffixDbUpdateQuery = " WHERE host=? AND db=? AND user=?";
        String sql =
                RSSManagerUtil.prepareUpdateSQLString(prefixDbUpdateQuery, suffixDbUpdateQuery,
                        RSSManagerCommonUtil.getDatabasePrivilegeList());
        PreparedStatement stmt = conn.prepareStatement(sql);
        int dbPrivCount = 0;
        for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
            dbPrivCount++;
            stmt.setString(dbPrivCount, permissions.getPermission(priv).toString());
        }
        stmt.setObject(dbPrivCount + 1, "%");
        stmt.setObject(dbPrivCount + 2, dao.getDatabaseInstanceById(dbInsId).getName());
        stmt.setObject(dbPrivCount + 3, user.getUsername());
        stmt.execute();
    }

    private void updatePasswordInSystemTable(Connection conn, DatabaseUser user, int dbInsId)
            throws RSSDAOException, SQLException {
        RSSDAO dao = RSSDAOFactory.getRSSDAO();
        String query = "UPDATE mysql.user SET Password = Password('" + user.getPassword() +
                "') WHERE user = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, user.getUsername());
        stmt.executeUpdate();
    }

}
