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
package org.wso2.bps.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * Prepares the WSO2 BPS for test runs, starts the server, and stops the server after
 * test runs
 */
public class BPSSecurityServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(BPSSecurityServerManager.class);

    @Override
    @BeforeSuite
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        log.info("Starting Server for security tests...");
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite
    public void stopServer() throws Exception {
        log.info("Stopping Server after security tests...");
        super.stopServer();
    }


    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {

        String fileName = "SecurePartnerService";

        for (int i = 1; i <= 15; i++) {
            copyFile(computeSourcePath(fileName + i + ".aar"),
                     computeDestPath("services", "SecurePartnerService" + i + ".aar", carbonHome));
        }

        copyPWCBJar(carbonHome);
        copyClientJKS(carbonHome);
        copyServiceJKS(carbonHome);
        copySampleProcess(carbonHome, "HelloWorld2");
        copySampleProcess(carbonHome, "SecuredWithServiceDescriptorProcess");
        copySampleProcess(carbonHome, "SecurePartnerBPEL");


    }

    private void copySampleProcess(String carbonHome, String packageName) throws IOException {
        //copy + deploy sample process
        File[] samples = FileManipulator.getMatchingFiles(BPSTestUtils.getBpelSampleLocation(carbonHome),
                                                          packageName, "zip");
        File bpelRepo = new File(carbonHome + File.separator + "repository" + File.separator +
                                 "deployment" + File.separator +
                                 "server" + File.separator + "bpel" + File.separator);

        FileManipulator.copyFileToDir(samples[0], bpelRepo);
    }

    private String computeDestPath(String deploymentFolder, String fileName, String carbonHome) {


        String deploymentPath;
        //only  SecurePartnerService1.aar deployed on BPS, other services will be deployed on Axis2Server
        if (fileName.equals("SecurePartnerService1.aar")) {
            deploymentPath = carbonHome + File.separator + "repository" + File.separator
                             + "deployment" + File.separator + "server" + File.separator + "axis2services";

        } else {
            deploymentPath = carbonHome + File.separator + "samples" + File.separator + "axis2Server" + File.separator +
                             "repository" + File.separator + deploymentFolder;
        }
        // First create the deployment folder in the server if it doesn't already exist
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath + File.separator + fileName;
    }

    private String computeSourcePath(String fileName) {
        String sourcePath = BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + "partnerServices" +
                            File.separator + fileName;
        log.info("Source Path :" + sourcePath);
        return sourcePath;
    }

    private void copyFile(String sourcePath, String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);

        try {
            //FileManipulator.copyFile(sourceFile, destFile);
            FileManipulator.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void copyPWCBJar(String carbonHome) {
        String componentLibPath = carbonHome + File.separator + "repository" + File.separator
                                  + "components" + File.separator + "lib";
        log.info("componentLibPath : " + componentLibPath);
        String jarFilePath = BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + "partnerServices" +
                             File.separator + "PWCBHandler.jar";
        log.info("jarFilePath :" + jarFilePath);
        copyFileToDir(jarFilePath, componentLibPath);
    }

    protected void copyClientJKS(String carbonHome) {
        String securityFilePath = carbonHome + File.separator + "repository" + File.separator
                                  + "resources" + File.separator + "security";
        log.info("securityFilePath :" + securityFilePath);
        String clientJksPath = BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + "partnerServices" +
                               File.separator + "client.jks";
        log.info("clientJksPath :" + clientJksPath);
        copyFileToDir(clientJksPath, securityFilePath);
    }

    protected void copyServiceJKS(String carbonHome) {
        String securityFilePath = carbonHome + File.separator + "repository" + File.separator
                                  + "resources" + File.separator + "security";
        log.info("securityFilePath :" + securityFilePath);
        String serviceJksPath = BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + "partnerServices" +
                                File.separator + "service.jks";
        log.info("serviceJksPath :" + serviceJksPath);
        copyFileToDir(serviceJksPath, securityFilePath);
    }

    private void copyFileToDir(String sourcePath, String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);

        try {
            //FileManipulator.copyFile(sourceFile, destFile);
            FileManipulator.copyFileToDir(sourceFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
