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
package org.wso2.carbon.automation.core.utils.dbutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.DataSource;

import java.sql.SQLException;

public class DatabaseFactory {
    private static final Log log = LogFactory.getLog(DatabaseFactory.class);

    private static final DataSource dbConfig = new EnvironmentBuilder().getFrameworkSettings().getDataSource();

    private static final String JDBC_URL = dbConfig.getDbUrl();
    private static final String JDBC_DRIVER = dbConfig.getM_dbDriverName();
    private static final String DB_USER = dbConfig.getDbUser();
    private static final String DB_PASSWORD = dbConfig.getDbPassword();

    public static DatabaseManager getDatabaseConnector(String databaseDriver, String jdbcUrl,
                                                       String userName, String passWord)
            throws ClassNotFoundException, SQLException {

        return new SqlDatabaseManager(databaseDriver, jdbcUrl, userName, passWord);

    }

    public static DatabaseManager getDatabaseConnector(String jdbcUrl,
                                                       String userName, String passWord)
            throws ClassNotFoundException, SQLException {

        return new SqlDatabaseManager(JDBC_DRIVER, jdbcUrl, userName, passWord);

    }

    public static DatabaseManager getDatabaseConnector()
            throws ClassNotFoundException, SQLException {

        return new SqlDatabaseManager(JDBC_DRIVER, JDBC_URL, DB_USER, DB_PASSWORD);

    }
}
