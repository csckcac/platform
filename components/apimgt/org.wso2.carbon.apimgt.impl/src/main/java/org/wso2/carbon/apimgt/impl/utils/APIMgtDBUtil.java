/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.DBConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class APIMgtDBUtil {

    private static final Log log = LogFactory.getLog(APIMgtDBUtil.class);

    private static volatile BasicDataSource dataSource = null;
    
    private static final String DB_CONFIG = "Database.";
    private static final String DB_DRIVER = DB_CONFIG + "Driver";
    private static final String DB_URL = DB_CONFIG + "URL";
    private static final String DB_USER = DB_CONFIG + "Username";
    private static final String DB_PASSWORD = DB_CONFIG + "Password";

    /**
     * Initializes the data source
     *
     * @throws APIManagementException if an error occurs while loading DB configuration
     */
    public static void initialize() throws APIManagementException {
        if (dataSource != null) {
            return;
        }

        synchronized (APIMgtDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                DBConfiguration configuration = getDBConfig();
                String dbUrl = configuration.getDbUrl();
                String driver = configuration.getDriverName();
                String username = configuration.getUserName();
                String password = configuration.getPassword();
                if (dbUrl == null || driver == null || username == null || password == null) {
                    throw new APIManagementException("Required DB configuration parameters unspecified");
                }
                dataSource = new BasicDataSource();
                dataSource.setDriverClassName(driver);
                dataSource.setUrl(dbUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
            }
        }
    }

    public static boolean checkDBConfiguration() {
        DBConfiguration configuration = getDBConfig();
        String dbUrl = configuration.getDbUrl();
        String driver = configuration.getDriverName();
        String username = configuration.getUserName();
        String password = configuration.getPassword();
        return dbUrl != null && driver != null && username != null && password != null;
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Utility method to close the connection streams.
     * @param preparedStatement PreparedStatement
     * @param connection Connection
     * @param resultSet ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Return the DBConfiguration
     *
     * @return DBConfiguration
     */
    private static DBConfiguration getDBConfig() {
        DBConfiguration dbConfiguration = new DBConfiguration();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        dbConfiguration.setDbUrl(config.getFirstProperty(DB_URL));
        dbConfiguration.setDriverName(config.getFirstProperty(DB_DRIVER));
        dbConfiguration.setUserName(config.getFirstProperty(DB_USER));
        dbConfiguration.setPassword(config.getFirstProperty(DB_PASSWORD));
        return dbConfiguration;
    }
}
