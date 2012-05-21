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
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.aarservices.stub.ServiceUploaderStub;
import org.wso2.carbon.aarservices.stub.types.carbon.AARServiceData;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;


public class AdminServiceAARServiceUploader {
    private static final Log log = LogFactory.getLog(AdminServiceAARServiceUploader.class);

    private ServiceUploaderStub serviceUploaderStub;

    public AdminServiceAARServiceUploader(String backEndUrl) throws AxisFault {
        String serviceName = "ServiceUploader";
        String endPoint = backEndUrl + serviceName;
        try {
            serviceUploaderStub = new ServiceUploaderStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
            throw new AxisFault("ServiceUploaderStub Initialization fail " + axisFault.getMessage());
        }
    }

    public void uploadAARFile(String sessionCookie, String fileName, String filePath,
                              String serviceHierarchy) throws ExceptionException, RemoteException {
        AARServiceData aarServiceData;
        AuthenticateStub.authenticateStub(sessionCookie, serviceUploaderStub);
        aarServiceData = new AARServiceData();
        aarServiceData.setFileName(fileName);
        aarServiceData.setDataHandler(createDataHandler(filePath));
        aarServiceData.setServiceHierarchy(serviceHierarchy);
        serviceUploaderStub.uploadService(new AARServiceData[]{aarServiceData});
    }

    private DataHandler createDataHandler(String filePath) {
        URL url = null;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("File path URL is invalid" + e);
            Assert.fail("File path URL is invalid" + e);
        }
        DataHandler dh = new DataHandler(url);
        return dh;
    }
}
