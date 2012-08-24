/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.hosting.mgt.cli;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliCommandManager {
    static String session = null;
    static ConfigurationContext configurationContext;
    static String backEndServerUrl = null;

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".zip"};

    protected static final Log log = LogFactory.getLog(CliCommandManager.class);


    public boolean loggingToRemoteServer(String host, String port, String userName, String passWord,
                                       String domainName) {
        boolean successfullyLoggedIn = false;
        backEndServerUrl = "https://" + host + ":" + port;
        String serverUrl =  backEndServerUrl + "/services/AuthenticationAdmin";
        AuthenticationClient authClient = new AuthenticationClient();
        //
        try {
            CliCommandManager.session = getSessionCookie(serverUrl, userName, passWord, host, domainName);
            CliCommandManager.configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                                null, "axis2_client.xml");
            successfullyLoggedIn = true;
//            System.out.println("context root  " + configurationContext.getContextRoot());
//            System.out.println("service conxt path " + configurationContext.getServiceContextPath());
//            System.out.println("config context " + configurationContext.toString());

//            System.out.println("Tenant id" + MultitenantUtils.getTenantId(configurationContext));
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (LoginAuthenticationExceptionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return successfullyLoggedIn;
    }


    public void uploadApps(String apps, String cartridgeType){
        System.out.println("uploading apps! " + apps + "for cartridge " + cartridgeType);
        String[] applicationPaths = getApplicationPathsArray(apps);
        try {
            HostingAdminClient client = new HostingAdminClient(CliCommandManager.session,
                                                               CliCommandManager.configurationContext,
                                                               CliCommandManager.backEndServerUrl);

//            Map<String, ArrayList<FileItemData>> fileItemsMap = new HashMap<String, ArrayList<FileItemData>>();
//            if (fileItemsMap == null || fileItemsMap.isEmpty()) {
//                String msg = "No file specified.";
//            }
//            List<FileItemData> tempDataList = fileItemsMap.get("warFileName");
//            List<FileUploadData> fileUploadDataList = new ArrayList<FileUploadData>();
            if(applicationPaths != null){
                FileUploadData[] fileUploadData = new FileUploadData[applicationPaths.length];
                for(int i = 0; i < applicationPaths.length; i++){

                        checkFileExtensionValidity(getFileName(applicationPaths[i]), ALLOWED_FILE_EXTENSIONS);


                    File repositoryItemFile = new File(applicationPaths[i]);
                    DataHandler repositoryItem = new DataHandler(new FileDataSource(repositoryItemFile));
                    fileUploadData[i] = new FileUploadData();
                    fileUploadData[i].setDataHandler(repositoryItem);
                    fileUploadData[i].setFileName(getFileName(applicationPaths[i]));
                    
                }
                client.uploadCartridgeApps(fileUploadData, cartridgeType);
            }
            

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (FileUploadException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    protected String getFileName(String fileName) {
        String fileNameOnly;
        if (fileName.indexOf("\\") < 0) {
            fileNameOnly = fileName.substring(fileName.lastIndexOf('/') + 1,
                                              fileName.length());
        } else {
            fileNameOnly = fileName.substring(fileName.lastIndexOf("\\") + 1,
                                              fileName.length());
        }
        return fileNameOnly;
    }

    protected void checkFileExtensionValidity(String fileExtension,
                                                     String[] allowedExtensions)
            throws FileUploadException {
        boolean isExtensionValid = false;
        StringBuffer allowedExtensionsStr = new StringBuffer();
        for (String allowedExtension : allowedExtensions) {
            allowedExtensionsStr.append(allowedExtension).append(",");
            if (fileExtension.endsWith(allowedExtension)) {
                isExtensionValid = true;
                break;
            }
        }
        if (!isExtensionValid) {
            throw new FileUploadException(" Illegal file type." +
                                          " Allowed file extensions are " + allowedExtensionsStr);
        }
    }
    
    private String[] getApplicationPathsArray(String apps){
         
        String[] applicationPaths;
        if(apps.indexOf(',') != -1){
            applicationPaths = apps.split(",");
        }else if (apps != null && apps.trim() != ""){
            applicationPaths = new String[1];
            applicationPaths[0] = apps;
        } else {
            applicationPaths = null;
            System.out.println("No applications found to be uploaded");
        }
        return applicationPaths;
    }



    public void getCartridges(){
        try {
            HostingAdminClient client = new HostingAdminClient(CliCommandManager.session,
                                                               CliCommandManager.configurationContext,
                                                               CliCommandManager.backEndServerUrl);

            String cartridges[] = client.getCartridges();
            for(String cartridge:cartridges ){
                System.out.println(cartridge);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }



    /**
         * Returns the session cookie for subsequent invocations
         *
         * @param serverUrl  the url of the server to authenticate
         * @param userName   username
         * @param passWord   password
         * @param hostName   the host name of the remote server
         * @param domainName domain name of the tenant
         * @return the session cookie
         * @throws LoginAuthenticationExceptionException
         *                                  throws in case of an auth error
         * @throws java.rmi.RemoteException throws in case of a connection error
         * @throws java.net.SocketException throws in case of a socket error
         */
        public String getSessionCookie(String serverUrl,
                                       String userName,
                                       String passWord,
                                       String hostName,
                                       String domainName)
                throws RemoteException, SocketException, LoginAuthenticationExceptionException {

        AuthenticationClient authenticationClient = new AuthenticationClient();
                  try {
                AuthenticationAdminStub authenticationAdminStub =
                        authenticationClient.getLoggedAuthAdminStub(serverUrl, userName, passWord, hostName, domainName);
                ServiceContext serivceContext = authenticationAdminStub._getServiceClient().
                        getLastOperationContext().getServiceContext();
                System.out.println((String) serivceContext.getProperty(HTTPConstants.COOKIE_STRING));

                return (String) serivceContext.getProperty(HTTPConstants.COOKIE_STRING);

            } catch (CSGException ex) {
                throw new AxisFault(ex.getMessage(), ex);
            }
        }

}
