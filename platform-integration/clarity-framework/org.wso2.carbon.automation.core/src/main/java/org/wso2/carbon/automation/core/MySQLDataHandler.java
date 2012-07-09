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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.DirectoryScanner;
import org.wso2.carbon.automation.core.utils.dashboardutils.DashboardVariables;
import org.wso2.carbon.automation.core.utils.dbutils.MySqlDatabaseManager;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;

import java.io.File;
import java.sql.SQLException;

/**
 * loading each of the test result xml to the data processing class
 */
public class MySQLDataHandler {
    private static final Log log = LogFactory.getLog(MySQLDataHandler.class);

    /**
     * loading testNG result xml by traversing all the directories and
     * send it to data processing class
     */
    public void writeResultData() {

//        DatabaseLoader databaseLoader = new DatabaseLoader();
        TestResultDeployer testResultDeployer = new TestResultDeployer();
        MySqlDatabaseManager mySqlDatabaseManager;
        try {
            EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
            DashboardVariables dashboardVariables = environmentBuilder.getFrameworkSettings().getDashboardVariables();
            if (dashboardVariables.getEnableDashboard().equalsIgnoreCase("true")) {
                String databaseName = dashboardVariables.getDbName();

                mySqlDatabaseManager = new MySqlDatabaseManager(dashboardVariables.getJdbcUrl(), dashboardVariables.
                        getDbUserName(), dashboardVariables.getDbPassword());
//            databaseLoader.createDatabase();
                mySqlDatabaseManager.execute("INSERT INTO " + databaseName + ".WA_BUILD_HISTORY VALUES()");
                DirectoryScanner scan = new DirectoryScanner();
                scan.setBasedir(ProductConstant.REPORT_LOCATION + File.separator + "reports" + File.separator);
                String[] fileList = new String[]{"*/testng-results.xml"};
                scan.setIncludes(fileList);
                scan.scan();
                String[] fileset = scan.getIncludedFiles();
                for (int i = 0; i <= fileset.length - 1; i++) {
                    testResultDeployer.writeResult(ProductConstant.REPORT_LOCATION + "reports" +
                                                   File.separator + fileset[i]);
                    log.info(fileset[i] + " write to database");
                }
            }
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (SQLException e) {
            log.error(e);
        } /*catch (IOException e) {
            log.error(e);
        }*/
    }

    public static void main(String[] args) {
        new MySQLDataHandler().writeResultData();
    }


}
