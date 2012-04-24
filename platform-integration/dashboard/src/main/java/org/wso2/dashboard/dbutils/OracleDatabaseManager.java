/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.dashboard.dbutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

public class OracleDatabaseManager implements DatabaseManager {
    private static final Log log = LogFactory.getLog(OracleDatabaseManager.class);

    private Connection connection;

    public OracleDatabaseManager(String jdbcUrl, String userName, String passWord)
            throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        //url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber + ":" + sid;
        log.debug("JDBC Url: " + jdbcUrl);
        connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
        log.info("Connected to database");
    }

    public void executeUpdate(String sql) throws SQLException {
        Statement st = null;
        log.debug(sql);
        try {
            st = connection.createStatement();
            st.executeUpdate(sql);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    //can do nothing
                }
            }
        }
        log.debug("Sql update Success");

    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement st = connection.createStatement();
        log.debug(sql);
        return st.executeQuery(sql);

    }

    public void execute(String sql) throws SQLException {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.execute(sql);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    //can do nothing
                }
            }
        }
        log.debug("Sql execution Success");
    }

    public void disconnect() throws SQLException {
        connection.close();
        log.info("Disconnected from database");
    }

    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    protected void finalize() throws Throwable {
        try {
            if (!connection.isClosed()) {
                disconnect();
            }

        } catch (SQLException e) {
            log.error("Error while disconnecting from database");
            throw new SQLException("Error while disconnecting from database");
        }
        super.finalize();
    }

}
