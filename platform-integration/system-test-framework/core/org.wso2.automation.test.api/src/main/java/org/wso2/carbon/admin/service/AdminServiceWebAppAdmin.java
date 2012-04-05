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
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class AdminServiceWebAppAdmin {

    private final Log log = LogFactory.getLog(AdminServiceWebAppAdmin.class);

    private WebappAdminStub webappAdminStub;

    public AdminServiceWebAppAdmin(String backendUrl) throws AxisFault {
        String serviceName = "WebappAdmin";
        String endPoint = backendUrl + serviceName;
        try {
            webappAdminStub = new WebappAdminStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Fail to initialize WebappAdminStub : " + axisFault.getMessage());
            throw new AxisFault("Fail to initialize WebappAdminStub : " + axisFault.getMessage());
        }
    }

    public void warFileUplaoder(String sessionCookie, String filePath) throws RemoteException {
        File file = new File(filePath);
        String fileName = file.getName();
        URL url = null;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("Malformed URL " + e.getMessage());
        }
        DataHandler dh = new DataHandler(url);
        WebappUploadData webApp;
        webApp = new WebappUploadData();
        webApp.setFileName(fileName);
        webApp.setDataHandler(dh);
        new AuthenticateStub().authenticateStub(sessionCookie, webappAdminStub);
        try {
            assertTrue(webappAdminStub.uploadWebapp(new WebappUploadData[]{webApp})
                    , "webapp upload unsuccessful");
        } catch (RemoteException e) {
            log.error("Fail to upload webapp file :" + e.getMessage());
            throw new RemoteException("Fail to upload webapp file :" + e.getMessage());
        }
    }

    public void deleteWebAppFile(String sessionCookie, String fileName) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, webappAdminStub);
        try {
            webappAdminStub.deleteStartedWebapps(new String[]{fileName});
        } catch (RemoteException e) {
            log.error("Webapp deletion error:" + e.getMessage());
            throw new RemoteException("Webapp deletion error:" + e.getMessage());
        }
    }

    public void deleteStoppedWebapps(String sessionCookie, String fileName) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, webappAdminStub);

        try {
            webappAdminStub.deleteStoppedWebapps(new String[]{fileName});
        } catch (RemoteException e) {
            log.info("Cannot delete all stopped webapps", e);
            throw new RemoteException("Cannot delete all stopped webapps", e);
        }
    }

    public void stopWebapps(String sessionCookie, String fileName) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, webappAdminStub);
        try {
            webappAdminStub.stopAllWebapps();
            WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName);
        } catch (RemoteException e) {
            log.info("can not stop webapp", e);
            throw new RemoteException("Cannot stop webapp", e);
        }
    }

    public boolean stopWebApp(String sessionCookie, String fileName) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, webappAdminStub);
        try {
            webappAdminStub.stopWebapps(new String[]{fileName});
            WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName);
            if (webappMetadata.getWebappFile().equals(fileName)) {
                return true;
            }
        } catch (RemoteException e) {
            log.error("Cannot stop webapp", e);
            throw new RemoteException("Cannot stop webapp", e);
        }
        return false;
    }


}
