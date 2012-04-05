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
package org.wso2.carbon.rssmanager.core.connections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.core.description.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;

import java.sql.*;

/**
 * This class contains the methods handling JDBC connections.
 */
public class DBConnectionHandler {

    private static Connection connection;

    private static PreparedStatement preparedStatement;

    private static final Log log = LogFactory.getLog(DBConnectionHandler.class);

    /**
     * Creates the JDBC connection depending upon the data provided and exposes it to be accessible
     * for other functionalities.
     * @param rssIns Details of the RSS intance to which the connection should be established.
     * @return JDBC connection.
     * @throws RSSDAOException rssDaoException.
     */
    public static Connection getConnection(RSSInstance rssIns) throws RSSDAOException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(RSSManagerCommonUtil.getDatabaseDriver(rssIns.getServerURL()));
                connection = DriverManager.getConnection(rssIns.getServerURL(),
                        rssIns.getAdminUsername(), rssIns.getAdminPassword());
                return connection;
            }else{
                return connection;
            }
        } catch (SQLException e) {
            String msg="Unable To Establish The Connection";
            log.error(msg, e);
            throw new RSSDAOException(msg,e);
        } catch (ClassNotFoundException e) {
            String msg="Driver Class Not Found";
            log.error(msg, e);
            throw new RSSDAOException(msg,e);
        }
    }

    /**
     * Closing te existing database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Connection Cannot Be Closed", e);
            }
        }
    }

    /**
     * Retrieving database meta associated with a particular JDBC connection.
     * @return DatabaseMetadata object.
     * @throws SQLException sqlException.
     */
    public static DatabaseMetaData getDatabaseMetadata () throws SQLException {
        if (connection != null) {
            try {
                return connection.getMetaData();
            } catch (SQLException e) {
                String msg = "Unable to Retrieve Database Metadata";
                throw new SQLException(msg, e);
            }
        }
        return null;
    }

}
