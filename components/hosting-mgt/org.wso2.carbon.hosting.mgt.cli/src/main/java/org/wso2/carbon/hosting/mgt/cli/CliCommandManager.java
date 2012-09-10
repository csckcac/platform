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
import org.wso2.carbon.hosting.mgt.stub.types.carbon.AppsWrapper;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public class CliCommandManager {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".zip"};

    private static final String AXIS2_XML_FILE = "axis2_client.xml";


    HostingAdminClient client;

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
                                       String domainName) {

        boolean successfullyLoggedIn = false;
        String backEndServerUrl = "https://" + host + ":" + port;
        String serverUrl =  backEndServerUrl + "/services/AuthenticationAdmin";
        AuthenticationClient authClient = new AuthenticationClient();

        try {
            String session = authClient.getSessionCookie(serverUrl, userName, passWord,
                                                                    host, domainName);
            ConfigurationContext configurationContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, AXIS2_XML_FILE);
            successfullyLoggedIn = true;

            //initialize the client using authenticated details
            client = new HostingAdminClient(session,
                                            configurationContext,
                                            backEndServerUrl);
        } catch (Exception e) {
            String msg = "Error while Authenticating ";
            System.err.println(msg);
        }

        return successfullyLoggedIn;
    }


    public void uploadApps(String apps, String cartridgeType) {
        System.out.println("Uploading apps! " + apps + " for cartridge " + cartridgeType);
        String[] applicationPaths = getApplicationPathsArray(apps);
        try {

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
            System.err.println("Error while calling backend service");
        }catch (FileUploadException e) {
            System.err.println("Error while uploading files ");
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



    public void getCartridges() {
        try {
            System.out.println("Available cartridge types : ");
            String cartridges[] = client.getCartridges();
            for(String cartridge:cartridges ){
                System.out.println(cartridge);
            }
        } catch (AxisFault axisFault) {
            String msg = "Error while calling backend service ";
            System.err.println(msg);
        }

    }

    public void register(String cartridgeType, String min, String max, String svnPassword,
                         String volume ) {
        try{

            int minimum = 1, maximum =1; //define default values
            boolean isAttachVolume = false;
            try{
                minimum = Integer.parseInt(min);
                maximum = Integer.parseInt(max);
                if(minimum < 0 || maximum < 0){
                    System.err.println("Enter positive numbers for min and max");
                }
                isAttachVolume = Boolean.parseBoolean(volume);
            }catch (NumberFormatException e){
                System.err.println("please enter valid arguments");
            }
            if(minimum <= maximum){
                //correctly defined max and min
                System.out.println("type  " + cartridgeType + " min " + min + " max " + max + " svnp "
                    + svnPassword + " isatavol " + isAttachVolume);
                client.register(cartridgeType, minimum, maximum, svnPassword, isAttachVolume );
            } else {
                System.err.println("Minimum is larger than Maximum, please recheck the values passed");
            }

        }catch (Exception e){
            String msg = "Error while calling auto scaler to start instance";
            System.err.println(msg);
        }


    }

    public void listApps(String cartridge) {
        try{
            AppsWrapper appsWrapper = client.getAppsSummary(cartridge);

            String apps[] = appsWrapper.getApps();

            if(apps == null){
                System.out.println("No Applications available in " + cartridge + " cartridge");
                return;
            }
            else if(apps[0] == null){
                System.out.println("No Applications available in " + cartridge + " cartridge");
                return;
            }
            else {
                System.out.println("Applications available in " + cartridge + " cartridge");
            }
            for(int i = 0; i < apps.length; i++){

                System.out.println(apps[i]);
            }
        }catch (Exception e){
            String msg = "Error while calling backend service";
            System.err.println(msg);
        }
    }
    
    public void deleteAllApps(String cartridge){
        try{
            System.out.println("Deleting all the apps deployed in " + cartridge + " cartridge");
            client.deleteAllApps(cartridge);

        }catch (Exception e){
            String msg = "Error while calling backend service";
            System.err.println(msg);
        }
    }
    
    public void deleteApps(String apps, String cartridge){
        try{
            System.out.println("Deleting the following apps deployed in " + cartridge + "cartridge");

            String appsList[] = apps.split(",");   //deleting list

            AppsWrapper appsWrapper = client.getAppsSummary(cartridge);
            List<String> availableAppsList = Arrays.asList(appsWrapper.getApps()); //available list

            for(String app: appsList){
                System.out.println(app);
                if(!availableAppsList.contains(app)){
                    String msg = "At least one application name is wrong, cancelled deleting action";
                    System.err.println(msg);
                    return;
                }
            }
            client.deleteApps(appsList, cartridge);
            System.out.println();
            listApps(cartridge);
        }catch (Exception e){
            String msg = "Error while calling backend service";
            System.err.println(msg);
        }
    }
}
