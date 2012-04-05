/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.samples.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Prepares the WSO2 BRS for test runs, starts the server, and stops the server after
 * test runs
 */
public class BRSTestServerManager extends TestServerManager {
    public static final Log log = LogFactory.getLog(BRSTestServerManager.class);


    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        return super.startServer();
    }

    @Override
    @AfterSuite(timeOut = 60000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {
        String deploymentDir = computeDestDirPath(carbonHome);

        /*Banking service Sample*/
        copySampleFile(computeSourcePath("banking.service/target/BankingService.aar"), deploymentDir);

////        GetQuote Service Sample
        copySampleFile(computeSourcePath("quotation.service/target/GetQuoteService.aar"), deploymentDir);

//        Call Charging service sample
        copySampleFile(computeSourcePath("callcharging.service/target/CallChargingService.aar"), deploymentDir);

//        Car Rental Service Sample
        copySampleFile(computeSourcePath("carrental.service/target/CarRentalService.aar"), deploymentDir);

//        Health care service sample
        copySampleFile(computeSourcePath("heathcare.service/target/HealthCareService.aar"), deploymentDir);

//        Insurance Service Sample
        copySampleFile(computeSourcePath("insurance.service/target/InsuranceService.aar"), deploymentDir);

//       MIP Calculating service Sample
        copySampleFile(computeSourcePath("MIPCalculate.service/target/MIPCalculateService.aar"), deploymentDir);

//        Order approval service sample
        copySampleFile(computeSourcePath("orderApproval.service/target/OrderApprovalService.aar"), deploymentDir);

//        Shopping service sample
        copySampleFile(computeSourcePath("shopping.service/target/ShoppingService.aar"), deploymentDir);
    }

    private void copySampleFile(String sourceFilePath, String destDirPath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File destFile = new File(destDirPath);
        log.info("Copying " + sourceFile.getAbsolutePath() + " => " + destFile.getAbsolutePath());
        FileManipulator.copyFileToDir(sourceFile, destFile);
    }

    private String computeSourcePath(String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + fileName;
    }

    private String computeDestDirPath(String carbonHome) {
        /* First create the deployment folder in the server if it doesn't already exist */
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                                + "deployment" + File.separator + "server" + File.separator +
                                "ruleservices";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath;
    }
}
