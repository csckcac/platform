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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.DBConfiguration;
import org.wso2.carbon.utils.FileUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class APIMgtDBUtil {

    private static Log log = LogFactory.getLog(APIMgtDBUtil.class);
    private static volatile BasicDataSource dataSource = null;

    private APIMgtDBUtil() {
    }

    /**
     * Initializes the data source
     *
     * @param dbConfigurationPath db Config path
     * @throws APIManagementException if an error occurs while loading DB configuration
     */
    public static void initialize(String dbConfigurationPath) throws APIManagementException {
        if (dataSource != null) {
            return;
        }
        synchronized (APIMgtDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                DBConfiguration configuration = getDBConfig(dbConfigurationPath);
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
     * @param configPath  DB config file path
     * @return DBConfiguration
     * @throws APIManagementException  if failed to get DBConfiguration
     */
    private static DBConfiguration getDBConfig(String configPath) throws APIManagementException {
        DBConfiguration dbConfiguration = new DBConfiguration();
        try {
            String config = FileUtil.readFileToString(configPath);

            OMElement omElement = AXIOMUtil.stringToOM(config);
            OMElement dbConfigOMElement = omElement.getFirstChildWithName(new QName("dbConfig"));
            dbConfiguration.setDbUrl(
                    dbConfigOMElement.getFirstChildWithName(new QName("url")).getText());
            dbConfiguration.setDriverName(
                    dbConfigOMElement.getFirstChildWithName(new QName("driverName")).getText());
            dbConfiguration.setUserName(
                    dbConfigOMElement.getFirstChildWithName(new QName("userName")).getText());
            dbConfiguration.setPassword(
                    dbConfigOMElement.getFirstChildWithName(new QName("password")).getText());
        } catch (XMLStreamException e) {
            handleException("Failed to get db configuration", e);
        } catch (IOException e) {
            handleException("Error while reading the configuration file: " + configPath, e);
        }
        return dbConfiguration;
    }

    private static void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
