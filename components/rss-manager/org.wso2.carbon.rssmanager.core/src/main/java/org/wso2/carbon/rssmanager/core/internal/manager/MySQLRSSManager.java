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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.*;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MySQLRSSManager extends RSSManager {

    private static RSSManager rssManager = new MySQLRSSManager();

    private static final Log log = LogFactory.getLog(MySQLRSSManager.class);

    private MySQLRSSManager() {
    }

    public static synchronized RSSManager getMySQLRSSManager() {
        return rssManager;
    }

    @Override
    public void createDatabase(Database database) throws RSSManagerException {
        RSSInstance rssIns = this.lookupRSSInstance(database.getRssInstanceName());
        Connection conn = null;
        try {
            conn = rssIns.getDataSource().getConnection();
            conn.setAutoCommit(false);

            String qualifiedDatabaseName =
                    RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();

            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssIns.getName());
            String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssIns, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            /* Sets the tenant id under which the database is created */
            database.setTenantId(this.getCurrentTenantId());

            /* creates a reference to the database inside the metadata repository */
            this.getDAO().createDatabase(database);
            this.getDAO().incrementSystemRSSDatabaseCount();
            /* committing the changes to RSS instance */
            conn.commit();

            this.getTenantMetadataRepository(this.getCurrentTenantId()).addDatabase(database);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while creating the database '" +
                    database.getName() + " on RSS instance '" + rssIns.getName() + "'", e);
        } catch (RSSManagerException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
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
    public void dropDatabase(String rssInstanceName, String databaseName) throws
            RSSManagerException {
        RSSInstance rssIns = this.lookupRSSInstance(rssInstanceName);
        Connection conn = null;
        try {
            conn = rssIns.getDataSource().getConnection();
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE " + databaseName;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();

            List<UserDatabaseEntry> userDatabaseEntries =
                    this.getDAO().getUserDatabaseEntriesByDatabase(rssInstanceName, databaseName);
            /* removing the entries in the UserDatabaseEntry table which has a foreign key
             * constraint to the Database table in the rss metadata repository. These
             * entries correspond to the user-databaseInstance relationship that exists between a
             * particular database and a set of database users */
            if (userDatabaseEntries.size() > 0) {
                for (UserDatabaseEntry userDatabaseEntry : userDatabaseEntries) {
                    this.getDAO().deleteUserDatabaseEntry(userDatabaseEntry.getRssInstanceName(),
                            userDatabaseEntry.getUsername());
                }
            }
            this.getDAO().dropDatabase(rssInstanceName, databaseName);

            conn.commit();

            this.getTenantMetadataRepository(this.getCurrentTenantId()).getDatabases().
                    remove(new MultiKey(rssInstanceName, databaseName));
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while dropping the database '" + databaseName +
                    " on RSS instance '" + rssIns.getName() + "'", e);
        } catch (RSSManagerException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
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
    public void createDatabaseUser(DatabaseUser user) throws
            RSSManagerException {
        RSSInstance rssIns = this.lookupRSSInstance(user.getRssInstanceName());
        Connection conn = null;
        try {
            conn = rssIns.getDataSource().getConnection();
            conn.setAutoCommit(false);

            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getUsername());
            String sql =
                    "CREATE USER '" + qualifiedUsername + "'@'%' IDENTIFIED BY '" +
                            user.getPassword() + "'";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();

            /* Sets the fully qualified username */
            user.setUsername(qualifiedUsername);
            /* Sets the tenant id under which the database is created */
            user.setTenantId(this.getCurrentTenantId());
            user.setRssInstanceName(rssIns.getName());
            this.getDAO().createDatabaseUser(user);
            conn.commit();

            this.getTenantMetadataRepository(this.getCurrentTenantId()).addDatabaseUser(user);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while creating the database user '" +
                    user.getUsername() + " on RSS instance '" + user.getRssInstanceName() + "'", e);
        } catch (RSSManagerException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
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
        RSSInstance rssIns = this.lookupRSSInstance(rssInstanceName);
        Connection conn = null;
        try {
            conn = rssIns.getDataSource().getConnection();
            conn.setAutoCommit(false);
            String sql = "DROP USER '" + username + "'@'%'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();

            this.getDAO().deleteUserDatabaseEntry(rssInstanceName, username);
            this.getDAO().dropDatabaseUser(rssInstanceName, username);

            conn.commit();

            this.getTenantMetadataRepository(this.getCurrentTenantId()).getDatabaseUsers().
                    remove(new MultiKey(rssInstanceName, username));
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while dropping the database user '" + username +
                    " on RSS instance '" + rssIns.getName() + "'", e);
        } catch (RSSManagerException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
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
    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
         //this.getDAO().updateDatabaseUser();
    }

    @Override
    public void attachUserToDatabase(String rssInstanceName,
                                     String databaseName,
                                     String username,
                                     String templateName) throws RSSManagerException {
        Connection con = null;
        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new RSSManagerException("Invalid database name provided");
        }
        DatabasePrivilegeTemplate template =
                this.getDAO().getDatabasePrivilegesTemplate(templateName);
        if (template == null) {
            throw new RSSManagerException("Invalid database privilege template name provided");
        }

        RSSInstance rssInstance = this.getRSSInstance(rssInstanceName);
        try {
            con = rssInstance.getDataSource().getConnection();
            con.setAutoCommit(false);

            PreparedStatement stmt =
                    this.composePreparedStatement(con, databaseName, username, template);
            stmt.execute();
            this.flushPrivileges(con);

            this.createUserDatabaseEntry(username, databaseName, rssInstanceName, template);
            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error occurred while attaching the database user '" +
                    username + "' to the database '" + databaseName + "'", e);
        } catch (RSSManagerException e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    @Override
    public void detachUserFromDatabase(String rssInstanceName,
                                       String databaseName,
                                       String username) throws RSSManagerException {
        Connection con = null;
        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new RSSManagerException("Invalid database name is provided");
        }
        RSSInstance rssInstance = this.getRSSInstance(rssInstanceName);
        try {
            con = rssInstance.getDataSource().getConnection();
            con.setAutoCommit(false);

            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, username);
            stmt.setString(3, databaseName);
            stmt.execute();

            this.flushPrivileges(con);

            this.getDAO().deleteUserDatabaseEntry(rssInstanceName, username);

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error occurred while attaching the database user '" +
                    username + "' to the database '" + databaseName + "'", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    private PreparedStatement composePreparedStatement(Connection con,
                                                       String databaseName,
                                                       String username,
                                                       DatabasePrivilegeTemplate template) throws
            SQLException, RSSManagerException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        String sql = "INSERT INTO mysql.db VALUES(?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, "%");
        stmt.setString(2, databaseName);
        stmt.setString(3, username);
        stmt.setString(4, privileges.getSelectPriv());
        stmt.setString(5, privileges.getInsertPriv());
        stmt.setString(6, privileges.getUpdatePriv());
        stmt.setString(7, privileges.getDeletePriv());
        stmt.setString(8, privileges.getCreatePriv());
        stmt.setString(9, privileges.getDropPriv());
        stmt.setString(10, privileges.getGrantPriv());
        stmt.setString(11, privileges.getReferencesPriv());
        stmt.setString(12, privileges.getIndexPriv());
        stmt.setString(13, privileges.getAlterPriv());
        stmt.setString(14, privileges.getCreateTmpTablePriv());
        stmt.setString(15, privileges.getLockTablesPriv());
        stmt.setString(16, privileges.getCreateViewPriv());
        stmt.setString(17, privileges.getShowViewPriv());
        stmt.setString(18, privileges.getCreateRoutinePriv());
        stmt.setString(19, privileges.getAlterRoutinePriv());
        stmt.setString(20, privileges.getExecutePriv());
        stmt.setString(21, privileges.getEventPriv());
        stmt.setString(22, privileges.getTriggerPriv());

        return stmt;
    }

    private void flushPrivileges(Connection con) throws SQLException {
        String sql = "FLUSH PRIVILEGES";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.execute();
    }

    private RSSInstance lookupRSSInstance(String rssInstanceName) throws RSSManagerException {
        return (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) ?
                this.getRoundRobinAssignedDatabaseServer() : this.getRSSInstance(rssInstanceName);
    }

    private void createUserDatabaseEntry(String username,
                                         String databaseName,
                                         String rssInstanceName,
                                         DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        UserDatabaseEntry ude = new UserDatabaseEntry(username, databaseName, rssInstanceName);
        ude.setPrivileges(template.getPrivileges());
        this.getDAO().addUserDatabaseEntry(ude);
    }


}
