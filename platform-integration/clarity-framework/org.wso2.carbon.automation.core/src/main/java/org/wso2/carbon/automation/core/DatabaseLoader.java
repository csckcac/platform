/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.automation.core;

import org.wso2.carbon.automation.core.utils.dashboardutils.DashboardVariables;
import org.wso2.carbon.automation.core.utils.dbutils.MySqlDatabaseManager;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 * Class will create database
 */

public class DatabaseLoader {
    /**
     * create database method
     *
     * @throws ClassNotFoundException exception
     * @throws java.sql.SQLException  exception
     * @throws java.io.IOException    exception
     */
    public void createDatabase()
            throws ClassNotFoundException, SQLException, IOException {

        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        DashboardVariables dashboardVariables = environmentBuilder.getFrameworkSettings().getDashboardVariables();

        MySqlDatabaseManager dbm = new MySqlDatabaseManager(dashboardVariables.getJdbcUrl(),
                                                            dashboardVariables.getDbUserName(), dashboardVariables.getDbPassword());

        FileInputStream fileInputStream = new FileInputStream(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                                              + File.separator + "dashboard" + File.separator
                                                              + "scripts" + File.separator + "mysql.sql");
        // Get the object of DataInputStream
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        String strLine;
        while ((strLine = bufferedReader.readLine()) != null) {
            strLine = strLine.trim();
            dbm.execute(strLine.trim());
        }

    }
}
