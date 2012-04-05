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
package org.wso2.carbon.hdfs.mgt.ui;


import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminHDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminStub;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.rmi.RemoteException;


/**
 * Client to Access HDFS Admin seervices
 */
public class HDFSAdminClient {
    private static final Log log = LogFactory.getLog(HDFSAdminClient.class);

    private HDFSAdminStub hdfsAdminStub;

    public HDFSAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public HDFSAdminClient(javax.servlet.ServletContext servletContext,
                           javax.servlet.http.HttpSession httpSession) throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "HDFSAdmin";
        hdfsAdminStub = new HDFSAdminStub(ctx, serviceURL);
        ServiceClient client = hdfsAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }


    public FolderInformation[] getCurrentUserFSObjects(String fsObjectPath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {

        try {
            return hdfsAdminStub.getCurrentUserFSObjects(fsObjectPath);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (HDFSAdminHDFSServerManagementException e) {
            e.printStackTrace();
        }
        return new FolderInformation[0];
    }

    public boolean deleteFile(String filePath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        return hdfsAdminStub.deleteFile(filePath);
    }

    public boolean deleteFolder(String folderPath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        return hdfsAdminStub.deleteFolder(folderPath);
    }

    public boolean createFolder(String folderPath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        return hdfsAdminStub.makeDirectory(folderPath);
    }

    public boolean createFile(String filePath, byte [] fileContent)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        DataSource dataSource = (DataSource) new ByteArrayDataSource(fileContent,"application/octet-stream");
        DataHandler dataHandler = new DataHandler(dataSource);
        return  hdfsAdminStub.createFile(filePath, dataHandler);
    }

    public boolean renameFolder(String srcPath, String dstPath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        return hdfsAdminStub.renameFolder(srcPath, dstPath);
    }

    public boolean renameFile(String srcPath, String dstPath)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        return hdfsAdminStub.renameFile(srcPath, dstPath);
    }

    public void chageGroup(String fsPath, String group)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        hdfsAdminStub.setGroup(fsPath,group);
    }

    public void changeOwner(String fsPath,String owner)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        hdfsAdminStub.setOwner(fsPath,owner);
    }

    public void chagePermission(String fsPath, String fsPermission)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        hdfsAdminStub.setPermission(fsPath,fsPermission);

    }

    public void copyFile(String srcFile, String dstFile)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        hdfsAdminStub.copy(srcFile,dstFile);
    }

    public void copyFolder(String srcFolder, String dstFolder)
            throws HDFSAdminHDFSServerManagementException, RemoteException {
        hdfsAdminStub.copy(srcFolder,dstFolder);
    }

}
