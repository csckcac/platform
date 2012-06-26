/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.bps.integration.tests.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated;
import org.wso2.carbon.bpel.stub.mgt.types.PackageType;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;

/**
 * Utility functions for BPEL deployment and undeployment
 */
public class DeploymentAdminServiceUtils {
    private static final Log log = LogFactory.getLog(DeploymentAdminServiceUtils.class);

    private static String SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
            FrameworkSettings.HTTPS_PORT + "/services/";
    final static String UPLOADER_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
            ":" + FrameworkSettings.HTTPS_PORT + "/services/BPELUploader";
    final static String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
            ":" + FrameworkSettings.HTTPS_PORT +
            "/services/BPELPackageManagementService";


    private static final int FIRST_PAGE = 0;

    public static void deployPackage(String packageName, String serviceToExposeProcess,
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

    private static UploadedFileItem getUploadedFileItem(DataHandler dataHandler, String fileName,
                                                        String fileType) {
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);

        return uploadedFileItem;
    }

    public static void checkProcessDeployment(String packageName, boolean success,
                                              BPELPackageManagementServiceStub
                                                      bpelPackageManagementServiceStub)
            throws PackageManagementException, RemoteException {

        DeployedPackagesPaginated deployedPackages = bpelPackageManagementServiceStub.
                listDeployedPackagesPaginated(FIRST_PAGE);

        boolean packageDeployed = false;
        for (PackageType bpelPackage : deployedPackages.get_package()) {
            log.info(bpelPackage.getName());
            if (bpelPackage.getName().equals(packageName) && bpelPackage.getErrorLog() == null) {
                log.info(packageName + " has deployed successfully");
                packageDeployed = true;
            }
        }
        if (success) {
            assertFalse(!packageDeployed, packageName + " deployment failed");
        } else {
            assertFalse(packageDeployed, packageName + " deployment is expected to be failed. " +
                    "But the deployment is successful");
        }
    }

    public static void checkProcessDeployment(String packageName,
                                              BPELPackageManagementServiceStub
                                                      bpelPackageManagementServiceStub)
            throws PackageManagementException, RemoteException {
        checkProcessDeployment(packageName, true, bpelPackageManagementServiceStub);
    }

    public static void undeploy(String packageName,
                          BPELPackageManagementServiceStub bpelPackageManagementServiceStub)
            throws PackageManagementException, RemoteException, InterruptedException {

        bpelPackageManagementServiceStub.undeployBPELPackage(packageName);

        Thread.sleep(10000);

        DeployedPackagesPaginated deployedPackages = bpelPackageManagementServiceStub.
                listDeployedPackagesPaginated(0);

        boolean packageUndeployed = true;
        if (deployedPackages.get_package() != null) {
            for (PackageType bpelPackage : deployedPackages.get_package()) {
                log.info(bpelPackage.getName());
                if (bpelPackage.getName().equals(packageName)) {
                    log.info(packageName + " has un-deployed successfully");
                    packageUndeployed = false;
                }
            }
        }
        assertFalse(!packageUndeployed, packageName + " un-deployment failed");
    }


    public static BPELUploaderStub getBpelUploaderStub() throws Exception {
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(UPLOADER_SERVICE_URL);
        ServiceClient serviceClient = bpelUploaderStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", serviceClient);
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        return bpelUploaderStub;
    }

    public static BPELPackageManagementServiceStub getPackageManagementStub() throws AxisFault {
        BPELPackageManagementServiceStub serviceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);
        ServiceClient serviceClient = serviceStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", serviceClient);
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        return serviceStub;
    }
}
