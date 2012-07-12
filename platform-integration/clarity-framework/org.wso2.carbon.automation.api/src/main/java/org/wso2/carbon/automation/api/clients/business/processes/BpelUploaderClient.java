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
package org.wso2.carbon.automation.api.clients.business.processes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelUploaderClient {
    String ServiceEndPoint = null;
    String resourcePath = null;
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelUploaderClient.class);
    BPELPackageManagementServiceStub bpelPackageManagementServiceStub;

    public BpelUploaderClient(String serviceEndPoint, String resourceLocation, String sessionCookie) {
        this.ServiceEndPoint = serviceEndPoint;
        this.resourcePath = resourceLocation;
        this.sessionCookie = sessionCookie;
    }


    public boolean deployBPEL(String packageName)
            throws RemoteException, MalformedURLException, InterruptedException,
                   PackageManagementException {

        final String uploaderServiceURL = ServiceEndPoint + "BPELUploader";
        BpelPackageManagementClient manager = new BpelPackageManagementClient(ServiceEndPoint, sessionCookie);

        boolean success = false;
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(uploaderServiceURL);
        AuthenticateStub.authenticateStub(sessionCookie, bpelUploaderStub);
        deployPackage(packageName, bpelUploaderStub);
        Thread.sleep(10000);
        success = manager.checkProcessDeployment(packageName);
        return success;
    }

    public boolean deployBPEL(String packageName, String dirPath)
            throws RemoteException, InterruptedException, PackageManagementException {

        final String uploaderServiceURL = ServiceEndPoint + "BPELUploader";
        BpelPackageManagementClient manager = new BpelPackageManagementClient(ServiceEndPoint, sessionCookie);
        boolean success = false;
        AuthenticateStub authenticateStub = new AuthenticateStub();
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(uploaderServiceURL);
        authenticateStub.authenticateStub(sessionCookie, bpelUploaderStub);
        deployPackage(packageName, dirPath, bpelUploaderStub);
        Thread.sleep(10000);
        success = manager.checkProcessDeployment(packageName);
        return success;
    }

    private UploadedFileItem getUploadedFileItem(DataHandler dataHandler, String fileName,
                                                 String fileType) {
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);

        return uploadedFileItem;
    }

    public void deployPackage(String packageName,
                              BPELUploaderStub bpelUploaderStub)
            throws MalformedURLException, RemoteException, InterruptedException {
        String sampleArchiveName = packageName + ".zip";
        File bpelZipArchive =
                new File(resourcePath + File.separator + "artifacts" + File.separator
                         + "BPS" + File.separator + "bpel" + File.separator + sampleArchiveName);
        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];
        uploadedFileItems[0] = getUploadedFileItem(new DataHandler(bpelZipArchive.toURI().toURL()),
                                                   sampleArchiveName,
                                                   "zip");
        log.info("Deploying " + sampleArchiveName);
        bpelUploaderStub.uploadService(uploadedFileItems);
        Thread.sleep(10000);
    }

    public void deployPackage(String packageName, String resourceDir,
                              BPELUploaderStub bpelUploaderStub)
            throws RemoteException, InterruptedException {

        String sampleArchiveName = packageName + ".zip";
        log.info(resourceDir + File.separator + sampleArchiveName);
        DataSource bpelDataSource = new FileDataSource(resourceDir + File.separator + sampleArchiveName);
        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];
        uploadedFileItems[0] = getUploadedFileItem(new DataHandler(bpelDataSource),
                                                   sampleArchiveName,
                                                   "zip");
        log.info("Deploying " + sampleArchiveName);
        bpelUploaderStub.uploadService(uploadedFileItems);
        Thread.sleep(10000);
    }
}
