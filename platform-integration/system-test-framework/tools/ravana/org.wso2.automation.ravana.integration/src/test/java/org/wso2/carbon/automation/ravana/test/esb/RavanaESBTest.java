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

package org.wso2.carbon.automation.ravana.test.esb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import org.wso2.carbon.automation.ravana.test.ravanautils.RavanaTestMaster;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RavanaESBTest extends RavanaTestMaster {
    private static final Log log = LogFactory.getLog(RavanaESBTest.class);
    private static ResultSet resultSet;


    @Test(groups = "wso2.ravana", description = "create process builder to run ravana script file",
          priority = 1)
    public void createProcessBuilder() throws IOException, InterruptedException {
        String scenarioName = "esb_direct_string";
        FrameworkProperties frameworkProperties = new FrameworkProperties();
        String ravanaFrameworkPath = frameworkProperties.getRavana().getFrameworkPath();
        log.info("Running " + scenarioName + "scenario ..");
        ProcessBuilder pb = new ProcessBuilder("./test.sh", "wso2/" + scenarioName);
        log.info("Scenario execution finish ..");
        pb.directory(new File(ravanaFrameworkPath));
        Process proc;
        proc = pb.start();
        int exitVal = proc.waitFor();
        log.info("Proc wait for status " + exitVal);
    }

    @Test(groups = "wso2.ravana", description = "execute the query to analyse data",
          priority = 2)
    public void executeSql() throws SQLException {
        String queryString = "select TPS, TPS_MIN from TEST, SCENARIO,  SVN, " +
                             "SVN_URL where PRODUCT='wso2esb-3.0.1' " +
                             "and SCENARIO.VERSION='3.0.1' and " +
                             "SVN.VERSION=(select max(VERSION) from SVN) and CONCURRENCY=20 " +
                             "and MESSAGE_SIZE=297 and " +
                             "TEST.SCENARIO_ID=SCENARIO.ID and TEST.SVN_ID=SVN.ID;";
        resultSet = mysqlDBMgt.executeQuery(queryString);
    }

    @Test(groups = "wso2.ravana", description = "verify Ravana test results",
          priority = 3)
    public void testVerifyRavanaResult() throws SQLException {
        log.info("Running Ravana ESB Test..");
        if (resultSet.next()) {
            int tps = resultSet.getInt("TPS");
            int tpsThreshold = resultSet.getInt("TPS_MIN");
            log.info("Actual TPS:" + tps);
            log.info("Expected TPS minimum value :" + tpsThreshold);
            assertTrue(tps > tpsThreshold, "Actual - " + tps + " TPS is less than expected - " +
                                           tpsThreshold + " TPS minimum value");
        } else {
            assertNotNull(resultSet, "Result set is empty");
        }
    }
}
