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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.SocketException;
import java.rmi.RemoteException;

public class CliCommandManager {
    static String session = null;
    static ConfigurationContext configurationContext;
    static String backEndServerUrl = null;

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".zip"};

    private static final String AXIS2_XML_FILE = "conf/axis2_client.xml";
    protected static final Log log = LogFactory.getLog(CliCommandManager.class);

    HostingAdminClient client;

    public CliCommandManager(){

        client = new HostingAdminClient(CliCommandManager.session,
                                                           CliCommandManager.configurationContext,
                                                           CliCommandManager.backEndServerUrl);
    }

    /**
     * Call to authenticate against ADS (Artifact Deployment Server)
     * @param host
     * @param port
     * @param userName
     * @param passWord
     * @param domainName
     * @return true if authentication successful
     */
    public boolean loggingToRemoteServer(String host, String port, String userName, String passWord,
                                       String domainName) throws CliToolException {

        boolean successfullyLoggedIn = false;
        backEndServerUrl = "https://" + host + ":" + port;
        String serverUrl =  backEndServerUrl + "/services/AuthenticationAdmin";
        AuthenticationClient authClient = new AuthenticationClient();

        try {
            CliCommandManager.session = authClient.getSessionCookie(serverUrl, userName, passWord,
                                                                    host, domainName);
            CliCommandManager.configurationContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, AXIS2_XML_FILE);
            successfullyLoggedIn = true;

        } catch (Exception e) {
            handleException("Authentication error " , e);
        }

        return successfullyLoggedIn;
    }


    private void handleException(String msg, Exception e) throws CliToolException {
        log.error(msg, e);
        throw new CliToolException(msg, e);
    }

    public void uploadApps(String apps, String cartridgeType) throws CliToolException {
        System.out.println("Uploading apps! " + apps + " for cartridge " + cartridgeType);
        String[] applicationPaths = getApplicationPathsArray(apps);
        try {
            HostingAdminClient client = new HostingAdminClient(CliCommandManager.session,
                                                               CliCommandManager.configurationContext,
                                                               CliCommandManager.backEndServerUrl);


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
            handleException("Error while calling backend service",  axisFault);
        }catch (FileUploadException e) {
            handleException("Error while uploading files " , e);
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



    public void getCartridges() throws CliToolException {
        try {

            String cartridges[] = client.getCartridges();
            for(String cartridge:cartridges ){
                System.out.println(cartridge);
            }
        } catch (AxisFault axisFault) {
            handleException("Error while calling backend serice " , axisFault);
        }

    }

    public void register(String cartridgeType, int min, int max, String optionalName, boolean attachVolume) {
            try{
                client.register(cartridgeType, min, max, optionalName, attachVolume);
            }catch (Exception e){
                String msg = "Error while calling auto scaler to start instance";
                log.error(msg);
            }


    }
}
