/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.management.humantask;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.bps.integration.tests.util.HumanTaskTestConstants;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.mgt.PackageManagementException;
import org.wso2.carbon.humantask.stub.mgt.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.stub.mgt.types.TaskDefinition_type0;
import org.wso2.carbon.humantask.stub.upload.HumanTaskUploaderStub;
import org.wso2.carbon.humantask.stub.upload.types.UploadedFileItem;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;


public class DeploymentTestCase {
    private static final int FIRST_PAGE = 0;
    private static String SERVICE_URL_PREFIX;


    final String UPLOADER_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                        ":" + FrameworkSettings.HTTPS_PORT + "/services/HumanTaskUploader";
    final String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                  ":" + FrameworkSettings.HTTPS_PORT +
                                                  "/services/HumanTaskPackageManagement";

    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    private HumanTaskUploaderStub humanTaskUploaderStub = null;
    private HumanTaskPackageManagementStub humanTaskPackageManagementStub = null;

    @BeforeClass(groups = {"wso2.bps", "f"})
    public void login() throws Exception {
        log.info("Logging in for Deployment Test");
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();

        humanTaskUploaderStub = new HumanTaskUploaderStub(UPLOADER_SERVICE_URL);
        ServiceClient humanTaskUploaderServiceClient = humanTaskUploaderStub._getServiceClient();
        Options humanTaskUploaderServiceClientOptions = humanTaskUploaderServiceClient.getOptions();
        humanTaskUploaderServiceClientOptions.setManageSession(true);
        humanTaskUploaderServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                          loggedInSessionCookie);

        humanTaskPackageManagementStub =
                new HumanTaskPackageManagementStub(PACKAGE_MANAGEMENT_SERVICE_URL);
        ServiceClient humantaskPackageManagementClient = humanTaskPackageManagementStub._getServiceClient();
        Options humanTaskPckgMgmtClientOptions = humantaskPackageManagementClient.getOptions();
        humanTaskPckgMgmtClientOptions.setManageSession(true);
        humanTaskPckgMgmtClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                   loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        util.logout();
        log.info("Logging out in Deployment Test...");
    }

    @Test(groups = {"wso2.bps", "f"}, description = "HumanTask Deployment tests")
    public void deploymentTestService() throws Exception {
        log.info("Starting Deployment Tests...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";

        if (System.getProperty("bps.humantask.sample.location") == null) {
            log.info("System property: bps.humantask.sample.location cannot be null");
            fail("System property: bps.humantask.sample.location cannot be null");
        }


        deployPackage(HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME, "ClaimService");
        checkTaskDeployment(HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME);
    }

    private void deployPackage(String packageName, String serviceToExposeProcess)
            throws RemoteException, InterruptedException, MalformedURLException {

        String sampleArchiveName = packageName + ".zip";
        File humanTaskZipArchive = new File(BPSTestUtils.HUMANTASK_SAMPLE_LOCATION + sampleArchiveName);
        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];
        uploadedFileItems[0] = getUploadedFileItem(new DataHandler(humanTaskZipArchive.toURI().toURL()),
                                                   sampleArchiveName,
                                                   "zip");
        log.info("Deploying " + sampleArchiveName);
        humanTaskUploaderStub.uploadHumanTask(uploadedFileItems);

        BPSTestUtils.waitForServiceDeployment(SERVICE_URL_PREFIX + serviceToExposeProcess);
        Thread.sleep(5000);
    }

    public UploadedFileItem getUploadedFileItem(DataHandler dataHandler, String fileName,
                                                String fileType) {
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);

        return uploadedFileItem;
    }


    private void checkTaskDeployment(String packageName)
            throws PackageManagementException, RemoteException {

        DeployedTaskDefinitionsPaginated deployedPackages = humanTaskPackageManagementStub.
                listDeployedTaskDefinitionsPaginated(FIRST_PAGE);

        boolean packageDeployed = false;
        for (TaskDefinition_type0 humanTaskPackage : deployedPackages.getTaskDefinition()) {
            log.info(humanTaskPackage.getPackageName());
            if (humanTaskPackage.getPackageName().equals(packageName)) {
                log.info(packageName + " has deployed successfully");
                packageDeployed = true;
            }
        }
        assertFalse(!packageDeployed, packageName + " deployment failed");

    }
}

