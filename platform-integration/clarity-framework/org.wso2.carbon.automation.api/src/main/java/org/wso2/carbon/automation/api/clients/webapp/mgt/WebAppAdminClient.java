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
package org.wso2.carbon.automation.api.clients.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;


public class WebAppAdminClient {

    private final Log log = LogFactory.getLog(WebAppAdminClient.class);

    private WebappAdminStub webappAdminStub;
    private final String serviceName = "WebappAdmin";

    public WebAppAdminClient(String backendUrl, String sessionCookie) throws AxisFault {

        String endPoint = backendUrl + serviceName;
        webappAdminStub = new WebappAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, webappAdminStub);
    }

    public void warFileUplaoder(String filePath) throws RemoteException {
        File file = new File(filePath);
        String fileName = file.getName();
        URL url = null;
        try {
            url = new URL("file://" + filePath);
        } catch (MalformedURLException e) {
            log.error("Malformed URL " + e);
        }
        DataHandler dh = new DataHandler(url);
        WebappUploadData webApp;
        webApp = new WebappUploadData();
        webApp.setFileName(fileName);
        webApp.setDataHandler(dh);

        try {
            assert webappAdminStub.uploadWebapp(new WebappUploadData[]{webApp}) : "webapp upload unsuccessful";
        } catch (RemoteException e) {
            log.error("Fail to upload webapp file :" + e);
            throw new RemoteException("Fail to upload webapp file :" + e);
        }
    }

    public void deleteWebAppFile(String fileName) throws RemoteException {
        webappAdminStub.deleteStartedWebapps(new String[]{fileName});
    }

    public void deleteStoppedWebapps(String fileName) throws RemoteException {

        webappAdminStub.deleteStoppedWebapps(new String[]{fileName});
    }

    public void stopWebapps(String fileName) throws RemoteException {
        webappAdminStub.stopAllWebapps();
        WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName);

    }

    public boolean stopWebApp(String fileName) throws RemoteException {
        webappAdminStub.stopWebapps(new String[]{fileName});
        WebappMetadata webappMetadata = webappAdminStub.getStoppedWebapp(fileName);
        if (webappMetadata.getWebappFile().equals(fileName)) {
            return true;
        }
        return false;
    }


}
