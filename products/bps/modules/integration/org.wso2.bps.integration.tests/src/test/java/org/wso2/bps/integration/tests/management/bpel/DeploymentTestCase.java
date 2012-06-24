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

package org.wso2.bps.integration.tests.management.bpel;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated;
import org.wso2.carbon.bpel.stub.mgt.types.PackageType;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;

//import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
//    import org.wso2.carbon.integration.framework.utils.

public class DeploymentTestCase {
    private static final int FIRST_PAGE = 0;
    private static String SERVICE_URL_PREFIX;


    final String UPLOADER_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                        ":" + FrameworkSettings.HTTPS_PORT + "/services/BPELUploader";
    final String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                  ":" + FrameworkSettings.HTTPS_PORT +
                                                  "/services/BPELPackageManagementService";

    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    private BPELUploaderStub bpelUploaderStub = null;
    private BPELPackageManagementServiceStub bpelPackageManagementServiceStub = null;

    @BeforeClass(groups = {"wso2.bps", "a"})
    public void login() throws java.lang.Exception {
        log.info("Logging in for Deployment Test");
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();

        bpelUploaderStub = new BPELUploaderStub(UPLOADER_SERVICE_URL);
        ServiceClient bpelUploaderServiceClient = bpelUploaderStub._getServiceClient();
        Options bpelUploaderServiceClientOptions = bpelUploaderServiceClient.getOptions();
        bpelUploaderServiceClientOptions.setManageSession(true);
        bpelUploaderServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                     loggedInSessionCookie);

        bpelPackageManagementServiceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);
        ServiceClient bpelPackageManagementClient = bpelPackageManagementServiceStub._getServiceClient();
        Options bpelPckgMgmtClientOptions = bpelPackageManagementClient.getOptions();
        bpelPckgMgmtClientOptions.setManageSession(true);
        bpelPckgMgmtClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                              loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        util.logout();
        log.info("Logging out in Deployment Test...");
    }

    @Test(groups = {"wso2.bps", "a"}, description = "Process Deplyment tests")

    public void deploymentTestService() throws Exception {
        log.info("Starting Deployment Tests...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";

//        if (System.getProperty("bps.sample.location") == null) {
//            log.info("System property: bps.sample.location cannot be null");
//            fail("System property: bps.sample.location cannot be null");
//        }


        deployPackage("HelloWorld2", "HelloService", bpelUploaderStub);
        checkProcessDeployment("HelloWorld2", bpelPackageManagementServiceStub);

        deployPackage("TestPickOneWay", "PickService", bpelUploaderStub);
        checkProcessDeployment("TestPickOneWay", bpelPackageManagementServiceStub);

        deployPackage("CleanUpTest1", "CleanUpTest1Service", bpelUploaderStub);
        checkProcessDeployment("CleanUpTest1", bpelPackageManagementServiceStub);


    }

    private void deployPackage(String packageName, String serviceToExposeProcess,
                               BPELUploaderStub bpelUploaderStub)
            throws RemoteException, InterruptedException, MalformedURLException {

        String carbonHome = System.getProperty("carbon.home");
        String sampleArchiveName = packageName + ".zip";
        File bpelZipArchive = new File(BPSTestUtils.getBpelSampleLocation(carbonHome) + sampleArchiveName);
        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];
        uploadedFileItems[0] = getUploadedFileItem(new DataHandler(bpelZipArchive.toURI().toURL()),
                                                   sampleArchiveName,
                                                   "zip");
        log.info("Deploying " + sampleArchiveName);
        bpelUploaderStub.uploadService(uploadedFileItems);

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


    private void checkProcessDeployment(String packageName,
                                        BPELPackageManagementServiceStub
                                                bpelPackageManagementServiceStub)
            throws PackageManagementException, RemoteException {

        DeployedPackagesPaginated deployedPackages = bpelPackageManagementServiceStub.
                listDeployedPackagesPaginated(FIRST_PAGE);

        boolean packageDeployed = false;
        for (PackageType bpelPackage : deployedPackages.get_package()) {
            log.info(bpelPackage.getName());
            if (bpelPackage.getName().equals(packageName)) {
                log.info(packageName + " has deployed successfully");
                packageDeployed = true;
            }
        }
        assertFalse(!packageDeployed, packageName + " deployment failed");

    }
}

