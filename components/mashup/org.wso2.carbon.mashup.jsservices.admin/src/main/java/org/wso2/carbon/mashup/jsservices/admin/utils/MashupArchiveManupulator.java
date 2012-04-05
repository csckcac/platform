/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mashup.jsservices.admin.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.mashup.jsservices.JSConstants;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This is a helper class to facilitate the sharing of Mashups. This contains
 * utilities to bundle and upload a deployed mashup service to a destination
 * Mashup server.
 */
public class MashupArchiveManupulator extends ArchiveManipulator {

    /**
     * Archives the mashup service JS file together with the
     * {servicename}.resource folder and creates a DataHandler for that archive.
     *
     * @param mashupService -
     *                      should be a Mashup Service deployed using the JSDeployer
     * @param configCtx     - The configuration context
     * @return the DataHandler containing the archived Mashup Service
     * @throws CarbonException - Thrown in case an Exception occurs
     */
    /*public DataHandler createMashupArchiveDataHandler(AxisService mashupService,
                                                      ConfigurationContext configCtx,
                                                      String migrateTags)
            throws CarbonException {
        // Use the WSAS work dir to create the temporary archive file
        Object workDirObject = configCtx.getProperty(ServerConstants.WORK_DIR);
        if (workDirObject != null) {
            DataSource dataSource;
            String workDir = (String) workDirObject;
            // Directory to store the temp archive
            File tempDir = new File(workDir);
            tempDir.mkdirs();
            try {
                // Creating a FileDataSource
                dataSource =
                        new FileDataSource(File.createTempFile("mashup", "upload", tempDir));
                // write the service js file & the contents of the
                // service.resources folder to the outstream of the data source

                createMashupArchive(mashupService, configCtx, dataSource.getOutputStream(),
                                    migrateTags);
            } catch (IOException e) {
                throw new CarbonException(e);
            }
            return new DataHandler(dataSource);
        }
        throw new CarbonException("Server work directory cannot be found.");
    }*/

    /*public void createMashupArchive(AxisService mashupService, ConfigurationContext configCtx,
                                    OutputStream outputStream, String migrateTags)
            throws CarbonException {
        File serviceFile;
        File serviceResourceFolder = null;

        String serviceType =
                (String) mashupService.getParameterValue(ServerConstants.SERVICE_TYPE);
        if (MashupConstants.JS_SERVICE.equals(serviceType)) {
            // Get the service js file for the given service
            Parameter serviceJSParameter = mashupService
                    .getParameter(MashupConstants.SERVICE_JS);
            Object value = serviceJSParameter.getValue();
            if (serviceJSParameter != null && value != null) {
                serviceFile = (File) value;
            } else {
                throw new CarbonException("Service you are trying to share is not a Mashup Service.");
            }
        } else {
            Parameter serviceJSParameter = mashupService
                    .getParameter(DBConstants.DB_SERVICE_CONFIG_FILE);
            String value = (String) serviceJSParameter.getValue();
            if (serviceJSParameter != null && value != null) {
                serviceFile = new File(value);
            } else {
                throw new CarbonException("Service you are trying to share is not a Mashup Service.");
            }
        }

        // Access the resources folder for the service
        Parameter serviceResourceFolderParameter = mashupService
                .getParameter(MashupConstants.MASHUP_RESOURCES_FOLDER);
        if (serviceResourceFolderParameter.getValue() != null) {
            serviceResourceFolder = (File) serviceResourceFolderParameter.getValue();
        }

        Parameter myRegistryPath =
                mashupService.getParameter(MashupConstants.REGISTRY_MASHUP_PATH);
        ByteArrayInputStream byteArrayInputStream = null;
        if ("true".equals(migrateTags) && myRegistryPath != null) {
            String mashupPath = (String) myRegistryPath.getValue();
            EmbeddedRegistry embeddedRegistry =
                    (EmbeddedRegistry) configCtx.getAxisConfiguration().getParameterValue(
                            RegistryConstants.REGISTRY);

            // Check weather there any any tags associated with this mashup.
            // If so we need to pack them into the zip too. The following is a sample tags file
            // <tags>
            //      <tag>test</tag>
            //      <tag>fun</tag>
            // </tags>
            try {
                UserRegistry systemRegistry = embeddedRegistry.getSystemRegistry();
                Tag[] tags = systemRegistry.getTags(mashupPath);
                if (tags != null && tags.length > 0) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("<tags>");
                    for (int i = 0; i < tags.length; i++) {
                        Tag tag = tags[i];
                        buffer.append("<tag>").append(tag.getTagName()).append("</tag>");
                    }
                    buffer.append("</tags>");
                    byteArrayInputStream = new ByteArrayInputStream(buffer.toString().getBytes());
                }
            } catch (RegistryException e) {
                throw new CarbonException(
                        "Error occured while retrieving tags of Mashup " + mashupService, e);
            }
        }

        if (serviceFile != null && serviceResourceFolder != null) {
            try {
                // Creating a FileDataSource
                ZipOutputStream zos = new ZipOutputStream(outputStream);
                // write the service js file & the contents of the
                // service.resources folder to the outstream of the data source

                createMashupArchive(zos, serviceFile, serviceResourceFolder, byteArrayInputStream);
                return;
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        }
        throw new CarbonException("Mashup Service Resources cannot be found.");
    }*/

    /**
     * Upload a archived mashupService included in the dataHanlder to a remote
     * Mashup Server donated by the destinationServerAddress.
     *
     * @param destinationServerAddress -
     *                                 remote mashup server address (eg: https://mooshup.com)
     * @param dataHandler              -
     *                                 contains the archived mashup service
     * @param serviceJsFileName        - The file name of the JS service
     * @param mashupServiceName        - The name of the mashup service
     * @throws AxisFault - Thrown in case an Exception Occurs
     */
   /* public void uploadMashupService(String destinationServerAddress, DataHandler dataHandler,
                                    String serviceJsFileName, String mashupServiceName,
                                    String username, String password, String overwriteExisting,
                                    String localUserName, String mode, String basicAuthUsername,
                                    String basicAuthPassword)
            throws IOException{

        if (destinationServerAddress.startsWith("http://")) {
            // We should call this service in https mode as we are sending the users username
            // and password as plain text.
            throw new CarbonException("Cannot share via http please use Https.");
        }
        // We use the RPCClient to call the sharing service
        RPCServiceClient serviceClient =
                new RPCServiceClient(MashupUtils.getClientConfigurationContext(), null);
        Options options = serviceClient.getOptions();
        EndpointReference sharingServiceEPR;
        // Check whether used has given the HostAdress with or without the
        // trailing '/'
        if (destinationServerAddress.endsWith("/")) {
            sharingServiceEPR = new EndpointReference(destinationServerAddress
                    + "services/MashupSharingService/shareMashup");
        } else {
            sharingServiceEPR = new EndpointReference(destinationServerAddress
                    + "/services/MashupSharingService/shareMashup");
        }

        if (MashupConstants.HTTP_AUTH_REQUIRED.equals(mode)){
            HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
            authenticator.setUsername(basicAuthUsername);
            authenticator.setPassword(basicAuthPassword);
            authenticator.setPreemptiveAuthentication(true);
            options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        }

        options.setTo(sharingServiceEPR);
        options.setAction("urn:shareMashup");
        options.setProperty(HTTPConstants.CHUNKED, "false");
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        // Creating a custom protocol based on the user's keystores and trusted certs within
        if (localUserName != null) {
            ProtocolSocketFactory psf = new CustomProtocolSocketFactory(localUserName);
            Protocol protocol = new Protocol("custom-https", psf, 443);
            options.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, protocol);
        }

        QName opAddEntry = new QName("http://service.share.mashup.wso2.org/xsd", "shareMashup");

        // parameters to the service ServiceName string, service file name,
        // dataHandler of the bundled archive
        Object[] opAddEntryArgs =
                new Object[] { mashupServiceName, username, password, serviceJsFileName,
                        dataHandler, overwriteExisting };
        try {
            serviceClient.invokeRobust(opAddEntry, opAddEntryArgs);
        } catch (AxisFault axisFault) {
            OperationContext operationContext = serviceClient.getLastOperationContext();
            if (operationContext != null) {
                MessageContext messageContext =
                        operationContext.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                if (messageContext != null) {
                    SOAPEnvelope envelope = messageContext.getEnvelope();
                    if (envelope != null) {
                        if (envelope.getBody().hasFault()) {
                            throw new AxisFault(envelope.getBody().getFault());
                        }
                    }
                }
            }
            throw AxisFault.makeFault(axisFault);
        }
    }*/

    /*public void uploadMashupService(String destinationServerAddress, DataHandler dataHandler,
                                    String serviceJsFileName,String mashupServiceName,
                                    String infoCardToken,String overwriteExisting,
                                    String localUserName, String mode, String basicAuthUsername,
                                    String basicAuthPassword)
            throws AxisFault {

        if (destinationServerAddress.startsWith("http://")) {
            // We should call this service in https mode as we are sending the users username
            // and password as plain text.
            throw new CarbonException("Cannot share via http please use Https.");
        }
        // We use the RPCClient to call the sharing service
        RPCServiceClient serviceClient =
                new RPCServiceClient(MashupUtils.getClientConfigurationContext(), null);
        Options options = serviceClient.getOptions();
        EndpointReference sharingServiceEPR;
        // Check whether used has given the HostAdress with or without the
        // trailing '/'
        if (destinationServerAddress.endsWith("/")) {
            sharingServiceEPR = new EndpointReference(destinationServerAddress
                    + "services/MashupSharingService/shareMashupIC");
        } else {
            sharingServiceEPR = new EndpointReference(destinationServerAddress
                    + "/services/MashupSharingService/shareMashupIC");
        }

        if (MashupConstants.HTTP_AUTH_REQUIRED.equals(mode)){
            HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
            authenticator.setUsername(basicAuthUsername);
            authenticator.setPassword(basicAuthPassword);
            authenticator.setPreemptiveAuthentication(true);
            options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        }

        options.setTo(sharingServiceEPR);
        options.setAction("urn:shareMashupIC");
        options.setProperty(HTTPConstants.CHUNKED, "false");
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        // Creating a custom protocol based on the user's keystores and trusted certs within
        if (localUserName != null) {
            ProtocolSocketFactory psf = new CustomProtocolSocketFactory(localUserName);
            Protocol protocol = new Protocol("custom-https", psf, 443);
            options.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, protocol);
        }

        QName opAddEntry = new QName("http://service.share.mashup.wso2.org/xsd", "shareMashupIC");

        // parameters to the service ServiceName string, service file name,
        // dataHandler of the bundled archive
        Object[] opAddEntryArgs =
                new Object[] { mashupServiceName, infoCardToken, serviceJsFileName, dataHandler,
                        overwriteExisting };
        try {
            serviceClient.invokeRobust(opAddEntry, opAddEntryArgs);
        } catch (AxisFault axisFault) {
            OperationContext operationContext = serviceClient.getLastOperationContext();
            if (operationContext != null) {
                MessageContext messageContext =
                        operationContext.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                if (messageContext != null) {
                    SOAPEnvelope envelope = messageContext.getEnvelope();
                    if (envelope != null) {
                        if (envelope.getBody().hasFault()) {
                            throw new AxisFault(envelope.getBody().getFault());
                        }
                    }
                }
            }
            throw AxisFault.makeFault(axisFault);
        }
    }*/

    /**
     * Create a archive containing the Mashup service JS and the resources.
     * Writes the Mashup Service JavaScript file and the service.resources
     * directory to the given ZipOutputStream. folder of the mashupService.
     *
     * @param zos          - The outputStrean for the zip
     * @param jsFile       - The handle to the JS File
     * @param resourcesDir - A handle to the resources directory
     * @throws IOException                   - Thrown in case the js file cannot be read
     * @throws java.io.FileNotFoundException - Thrown in case the js file cannot be found
     */
    /*private void createMashupArchive(ZipOutputStream zos, File jsFile, File resourcesDir,
                                     ByteArrayInputStream byteArrayInputStream)
            throws IOException {
        if (!resourcesDir.isDirectory()) {
            throw new CarbonException(resourcesDir.getPath() + " is not a directory");
        }
        archiveSourceDir = resourcesDir.getParent();
        // place the zip entry in the ZipOutputStream object
        zos.putNextEntry(new ZipEntry(getZipEntryPath(jsFile)));
        // if we reached here, the File object f was not a directory
        // create a FileInputStream on top of f
        FileInputStream fis = new FileInputStream(jsFile);

        // now write the content of the file to the ZipOutputStream
        byte[] readBuffer = new byte[40960];
        int bytesIn;
        while ((bytesIn = fis.read(readBuffer)) != -1) {
            zos.write(readBuffer, 0, bytesIn);
        }
        // close the Stream
        fis.close();

        // We are adding in the tags file here
        if (byteArrayInputStream != null) {
            String shortFileName = DescriptionBuilder.getShortFileName(jsFile.getName());
            File tagsFile = new File(resourcesDir.getParentFile(),
                                     shortFileName + MashupConstants.TAGS_File);
            zos.putNextEntry(new ZipEntry(getZipEntryPath(tagsFile)));
            // now write the content of the file to the ZipOutputStream
            readBuffer = new byte[40960];
            bytesIn = 0;
            while ((bytesIn = byteArrayInputStream.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            // close the Stream
            byteArrayInputStream.close();
        }

        zipDir(resourcesDir, zos);
        zos.close();
    }
*/
    /**
     * Downloads the MashupService denoted by the serviceName from the
     * remoteServer and deploys it in this server.
     *
     * @param remoteServer      - The address of the remote Mashup Server
     * @param serviceName       - The name of the service to be shared
     * @param axisConfiguration - The corresponding AxisConfiguration
     * @throws AxisFault - Thrown in case an exception occurs
     */
    /*public void downloadMashupService(String remoteServer, String serviceName, String username,
                                      String password, AxisConfiguration axisConfiguration)
            throws AxisFault {
        // We use the RPCClient to call the download service
        RPCServiceClient serviceClient =
                new RPCServiceClient(MashupUtils.getClientConfigurationContext(), null);
        Options options = serviceClient.getOptions();
        EndpointReference remoteSharingServiceEPR;
        // Check whether used has given the HostAdress with or without the
        // trailing '/'
        if (remoteServer.endsWith("/")) {
            remoteSharingServiceEPR = new EndpointReference(remoteServer
                    + "services/MashupSharingService/getMashup");
        } else {
            remoteSharingServiceEPR = new EndpointReference(remoteServer
                    + "/services/MashupSharingService/getMashup");
        }

        options.setTo(remoteSharingServiceEPR);
        options.setAction("urn:getMashup");
        QName opAddEntry = new QName("http://service.share.mashup.wso2.org/xsd", "getMashup");
        Object[] opAddEntryArgs = new Object[] { serviceName };

        // There is a bug in Axis2 which prevents us from using
        // invokeBlocking(opAddEntry, opAddEntryArgs,class[]);
        // Axis2 fails on handling the DataHandler responses in the RPCClient.So
        // we use OMElements
        OMElement response = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs);

        if (response != null) {
            OMElement responseElement = response.getFirstElement();
            if (responseElement != null) {
                OMElement archiveElement = responseElement.getFirstChildWithName(new QName(
                        "http://utils.mashup.wso2.org/xsd", "mashupArchive"));
                if (archiveElement != null) {
                    OMText textNode = (OMText) archiveElement.getFirstOMChild();
                    textNode.setBinary(true);
                    DataHandler dataHandler = (DataHandler) textNode.getDataHandler();
                    OMElement fileNameElement = responseElement.getFirstChildWithName(new QName(
                            "http://utils.mashup.wso2.org/xsd", "serviceJSFileName"));
                    if (fileNameElement != null) {
                        String fileName = fileNameElement.getText();
                        System.out.println(fileName + dataHandler);
                        try {

                            //Authenticating the user
                            boolean authenticated;
                            try {
                                authenticated = MashupUtils.authenticateUser(username, password);
                            } catch (AuthenticatorException e) {
                                throw new CarbonException(e);
                            }
                            if (!authenticated) {
                                throw new CarbonException(
                                        "Cannot authenticate user. Username or password is incorrect");
                            }

                            deploySharedService(fileName, username, dataHandler, axisConfiguration);
                        } catch (IOException e) {
                            throw new CarbonException(e);
                        }
                        return;
                    }
                }
            }
        }
        throw new CarbonException("Malformed response from the remote Mashup server.");
    }*/

    /**
     * Extracts the content in the DataHandler to the scripts folder of this
     * Mashup Server.
     *
     * @param fileName          -
     *                          filename of the service js file (to validate whether a file
     *                          with same name exists)
     * @param userdir          - The user direcotry name
     * @param dataHandler       -
     *                          archived mashup service Js & the resources
     * @param axisConfiguration - Axis Configuration
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public void deploySharedService(String userdir, String fileName, DataHandler dataHandler,
                                    AxisConfiguration axisConfiguration)
            throws AxisFault, CarbonException {

        URL repository = axisConfiguration.getRepository();
        if (repository != null) {
            // Access the scripts deployment folder
            File repo = new File(repository.getFile());
            File scriptsFolder = new File(repo, JSConstants.JS_SERVICES_REPO + File.separator + userdir);

            // Checking whether a file with the same name exists
            File file = new File(scriptsFolder, fileName);
            if (file.exists() && !file.delete()) {
                /*throw new CarbonException("A Service JavaScript file with the same name already " +
                        "exists in the remote Mashup Server.");  */
                throw new CarbonException("Unable to delete file " + file.getName());
            }
            MashupArchiveManupulator archiveManupulator = new MashupArchiveManupulator();
            // Extract the uploaded mashup archive to the scripts folder.
            try {
                archiveManupulator.extractFromStream(dataHandler.getInputStream(), scriptsFolder
                        .getAbsolutePath());
            } catch (IOException e) {
                throw new CarbonException(e);
            }
        } else {
            throw new CarbonException("Cannot find the repository in the Mashup server.");
        }
    }

    // This was copied from org.wso2.utils.ArchiveManipulator, had to copy it over cause we want to skip zipping up a
    // particullar folder
    /*protected void zipDir(File zipDir, ZipOutputStream zos) throws IOException {
        //get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[MashupConstants.BUFFER_SIZE];
        int bytesIn;
        //loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            String dir = dirList[i];
            // If its the private directory we dont need to zip it up
            if (MashupConstants.MASHUP_PRIVATE_FOLDER_NAME.equals(dir)) {
                continue;
            }
            File f = new File(zipDir, dir);
            //place the zip entry in the ZipOutputStream object
            zos.putNextEntry(new ZipEntry(getZipEntryPath(f)));
            if (f.isDirectory()) {
                //if the File object is a directory, call this
                //function again to add its content recursively
                zipDir(f, zos);
                //loop again
                continue;
            }
            //if we reached here, the File object f was not a directory
            //create a FileInputStream on top of f
            FileInputStream fis = new FileInputStream(f);

            //now write the content of the file to the ZipOutputStream
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            //close the Stream
            fis.close();
        }
    }*/
}