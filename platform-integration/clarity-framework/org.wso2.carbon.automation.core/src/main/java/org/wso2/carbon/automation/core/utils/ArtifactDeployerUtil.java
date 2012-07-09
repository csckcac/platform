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
package org.wso2.carbon.automation.core.utils;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.automation.api.clients.aar.services.AARServiceUploaderClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelUploaderClient;
import org.wso2.carbon.automation.api.clients.dataservices.DataServiceFileUploaderClient;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.api.clients.eventing.EventSourceAdminClient;
import org.wso2.carbon.automation.api.clients.jarservices.JARServiceUploaderClient;
import org.wso2.carbon.automation.api.clients.localentry.LocalEntriesAdminClient;
import org.wso2.carbon.automation.api.clients.mashup.MashupFileUploaderClient;
import org.wso2.carbon.automation.api.clients.mediation.MassageStoreAdminClient;
import org.wso2.carbon.automation.api.clients.mediation.MessageProcessorClient;
import org.wso2.carbon.automation.api.clients.mediation.SynapseConfigAdminClient;
import org.wso2.carbon.automation.api.clients.priority.executor.PriorityMediationAdminClient;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.rule.RuleServiceFileUploadAdminClient;
import org.wso2.carbon.automation.api.clients.sequences.SequenceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.tasks.TaskAdminClient;
import org.wso2.carbon.automation.api.clients.webapp.mgt.JAXWSWebappAdminClient;
import org.wso2.carbon.automation.api.clients.webapp.mgt.WebAppAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.rule.service.stub.fileupload.ExceptionException;
import org.wso2.carbon.sequences.stub.types.SequenceEditorException;
import org.wso2.carbon.task.stub.TaskManagementException;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.automation.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.carbon.automation.core.utils.endpointutils.EsbEndpointSetter;
import org.wso2.carbon.automation.core.utils.environmentutils.ClusterReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.fileutils.FolderTraversar;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * contains utility methods to deploy different artifacts
 */
public class ArtifactDeployerUtil {

    private static final Log log = LogFactory.getLog(ArtifactDeployerUtil.class);
    private static final String PROXY_SERVICES_DIR = "proxy-services";
    private static final String ENDPOINT_DIR = "endpoints";
    private static final String MESSAGESTORE_DIR = "message-store";
    private static final String MESSAGEPROCESSOR_DIR = "message-processors";
    private static final String EVENTSORCE_DIR = "event-sources";
    private static final String LOCALENTRY_DIR = "local-entries";
    private static final String PRIORITYEXECUTOR_DIR = "priority-executors";
    private static final String SEQUENCES_DIR = "sequences";
    private static final String CLASSES_DIR = "classes";
    private static final String CONFIG_DIR = "conf";
    private static final String JAR_DIR = "jars";
    private static final String TASKS_DIR = "tasks";

    public void carFileUploader(String sessionCookie, String hostName, URL url,
                                Artifact artifact) throws RemoteException {
        DataHandler dh = new DataHandler(url);
        CarbonAppUploaderClient adminServiceCarbonAppUploader =
                new CarbonAppUploaderClient(hostName);
        adminServiceCarbonAppUploader.uploadCarbonAppArtifact(sessionCookie, artifact.getArtifactName(), dh);
    }

    public String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AuthenticatorClient loginClient = new AuthenticatorClient(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void warFileUploder(String sessionCookie, String backendURL, String filePath)
            throws RemoteException {
        WebAppAdminClient AdminServiceWebAppAdmin = new WebAppAdminClient(backendURL);
        AdminServiceWebAppAdmin.warFileUplaoder(sessionCookie, filePath);
    }

    public void aarFileUploader(String sessionCookie, String backEndUrl, String artifactName,
                                String filePath, String productName)
            throws IOException, org.wso2.carbon.aarservices.stub.ExceptionException {

        if (productName.equals(ProductConstant.AXIS2_SERVER_NAME)) {

            String axisServiceRepo = System.getProperty(ServerConstants.CARBON_HOME) + File.separator +
                                     "samples" + File.separator + "axis2Server" + File.separator +
                                     "repository" + File.separator + "services";

            File axisServiceRepoFile = new File(axisServiceRepo);
            File artifactFile = new File(filePath);

            //create service directory if not exists
            if (!axisServiceRepoFile.exists()) {
                axisServiceRepoFile.mkdir();
            }
            FileManipulator.copyFileToDir(artifactFile, axisServiceRepoFile);
        } else {
            AARServiceUploaderClient adminServiceAARServiceUploader =
                    new AARServiceUploaderClient(backEndUrl);
            adminServiceAARServiceUploader.uploadAARFile(sessionCookie, artifactName, filePath, "");
        }
    }

    public void jaxwsFileUploader(String sessionCookie, String backendURL, String artifactName,
                                  String artifactLocation)
            throws RemoteException, MalformedURLException {

        JAXWSWebappAdminClient jaxwsWebappAdminClient = new JAXWSWebappAdminClient(backendURL, sessionCookie);
        jaxwsWebappAdminClient.uploadWebapp(artifactLocation, artifactName);
    }

    public void jarFileUploader(String sessionCookie, String backEndUrl, String artifactLocation,
                                Artifact artifact) throws Exception {

        String serviceGroupName = getJarServiceGroup(artifact.getArtifactName());
        JARServiceUploaderClient jarServiceUploader =
                new JARServiceUploaderClient(backEndUrl);
        List<DataHandler> dhJarList = new ArrayList<DataHandler>();
        List<ArtifactDependency> artifactDependencyList = artifact.getDependencyArtifactList();
        List<ArtifactAssociation> artifactAssociationList = artifact.getAssociationList();
        String location = getJarArtifactLocation(artifactAssociationList);
        DataHandler wsdlDataHandler = null;

        //add jar service file to array list
        URL jarServiceURL = new URL(("file:///" + artifactLocation + File.separator + "jar" +
                                     File.separator + location + File.separator + artifact.getArtifactName()));
        DataHandler dhJarService = new DataHandler(jarServiceURL);
        dhJarList.add(dhJarService);


        if (!artifactDependencyList.isEmpty()) {
            for (ArtifactDependency anArtifactDependencyList : artifactDependencyList) {
                //add all jar dependencies to the array list
                if (ArtifactTypeFactory.getTypeInString
                        (anArtifactDependencyList.getDepArtifactType()).equals("jar")) {
                    String jarLocation = artifactLocation + File.separator + "jar" + File.separator +
                                         location + File.separator +
                                         anArtifactDependencyList.getDepArtifactName();
                    URL jarURL = new URL(("file:///" + jarLocation));
                    DataHandler dhJar = new DataHandler(jarURL);
                    dhJarList.add(dhJar);
                    //add WSDL dependencies to data handler
                } else if (ArtifactTypeFactory.getTypeInString
                        (anArtifactDependencyList.getDepArtifactType()).equals("wsdl")) {
                    String wsdlLocation = artifactLocation + File.separator + "jar" + File.separator +
                                          location + File.separator +
                                          anArtifactDependencyList.getDepArtifactName();
                    URL wsdlURL = new URL(("file:///" + wsdlLocation));
                    wsdlDataHandler = new DataHandler(wsdlURL);

                }
            }
        }
        jarServiceUploader.uploadJARServiceFile(sessionCookie, serviceGroupName, dhJarList, wsdlDataHandler);
    }

    public void brsFileUploader(String sessionCookie, String artifactName, String artifactLocation,
                                String backendURL) throws RemoteException, MalformedURLException,
                                                          ExceptionException {
        URL artifactURL = new URL("file:///" + artifactLocation);
        DataHandler brsdh = new DataHandler(artifactURL);
        RuleServiceFileUploadAdminClient brsUploader = new RuleServiceFileUploadAdminClient(backendURL);
        brsUploader.uploadRuleFile(sessionCookie, artifactName, brsdh);
    }

    public void javaScriptServiceUploader(String sessionCookie, String artifactName,
                                          String artifactLocation, String backendURL)
            throws MalformedURLException, RemoteException,
                   org.wso2.carbon.mashup.jsservices.stub.fileupload.ExceptionException {
        URL artifactURL = new URL("file:///" + artifactLocation);
        DataHandler msDataHandler = new DataHandler(artifactURL);
        MashupFileUploaderClient jsUploader = new MashupFileUploaderClient(backendURL);
        jsUploader.uploadMashUpFile(sessionCookie, artifactName, msDataHandler);
    }


    public void bpelFileUploader(String sessionCookie, String backEndUrl, String artifactLocation,
                                 String artifactName)
            throws InterruptedException, RemoteException, PackageManagementException {
        BpelUploaderClient bpelUploader = new BpelUploaderClient(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelUploader.deployBPEL(artifactName.substring(0, artifactName.indexOf(".")), artifactLocation,
                                sessionCookie);
    }

    public void dbsFileUploader(String sessionCookie, String backEndUrl, Artifact artifact,
                                String artifactLocation, FrameworkProperties frameworkProperties)
            throws IOException, RSSAdminRSSDAOExceptionException,
                   org.wso2.carbon.dataservices.ui.fileupload.stub.ExceptionException,
                   ClassNotFoundException, SQLException, XMLStreamException,
                   ResourceAdminServiceExceptionException {
        String dbsFilePath;
        List<ArtifactDependency> artifactDependencyList = artifact.getDependencyArtifactList();
        List<File> sqlFileLis = null;
        DataHandler dbs;
        DataServiceFileUploaderClient adminServiceDataServiceFileUploader =
                new DataServiceFileUploaderClient(backEndUrl);

        dbsFilePath = artifactLocation + File.separator + "dbs" + File.separator +
                      getPath(artifact.getArtifactLocation()) + File.separator + artifact.getArtifactName();
        if (artifactDependencyList != null || artifactDependencyList.size() > 0) {
            Iterator iterator = artifactDependencyList.iterator();
            sqlFileLis = new ArrayList<File>();
            while (iterator.hasNext()) {
                ArtifactDependency dependency = (ArtifactDependency) iterator.next();
                String dependencyFilePath = artifactLocation + File.separator +
                                            getPath(dependency.getDepArtifactLocation() + File.separator
                                                    + dependency.getDepArtifactName());
                if (ArtifactType.sql == dependency.getDepArtifactType()) {
                    sqlFileLis.add(new File(dependencyFilePath));
                } else {
                    ResourceAdminServiceClient adminServiceResourceAdmin =
                            new ResourceAdminServiceClient(backEndUrl);
                    if (!adminServiceResourceAdmin.addResource(sessionCookie,
                                                               "/_system/governance/automation/resources/"
                                                               + dependency.getDepArtifactName(),
                                                               getMediaType(dependency.getDepArtifactType()), "",
                                                               new DataHandler(new URL("file://" + dependencyFilePath)))) {
                        log.error(dependency.getDepArtifactName() + " Resource Adding Failed");
                    }
                }
            }
        }
        if (sqlFileLis.size() > 0) {
            SqlDataSourceUtil dssUtil =
                    new SqlDataSourceUtil(sessionCookie, backEndUrl, frameworkProperties, artifact.getUserId());
            dssUtil.createDataSource(sqlFileLis);
            dbs = dssUtil.createArtifact(dbsFilePath);
        } else {
            dbs = new DataHandler(new URL("file://" + dbsFilePath));
        }
        adminServiceDataServiceFileUploader.uploadDataServiceFile(sessionCookie, artifact.getArtifactName(), dbs);
    }

    public void springServiceUpload(String sessionCookie, Artifact artifact,
                                    String artifactLocation, String backendURL)
            throws Exception {

        String contextXMLName = getContextXMLName(artifact.getDependencyArtifactList());
        String location = getSpringArtifactLocation(artifact.getAssociationList());
        String springBeanFilePath = artifactLocation + File.separator + "spring" + File.separator +
                                    location + File.separator + artifact.getArtifactName();
        String springContextFilePath = artifactLocation + File.separator + "spring" + File.separator +
                                       location + File.separator + contextXMLName;
        SpringServiceMaker newMarker = new SpringServiceMaker();
        newMarker.createAndUploadSpringBean(springContextFilePath, springBeanFilePath, sessionCookie, backendURL);

    }

    private String getSpringArtifactLocation(List<ArtifactAssociation> artifactAssociationList)
            throws Exception {
        String location;
        if (!artifactAssociationList.isEmpty()) {
            assert artifactAssociationList.get(0).getAssociationName().equals("location");
            location = artifactAssociationList.get(0).getAssociationValue();
            return location;
        } else {
            throw new Exception("Spring service location not found in scenario config");
        }
    }

    private String getJarArtifactLocation(List<ArtifactAssociation> artifactAssociationList)
            throws Exception {
        String location;
        if (!artifactAssociationList.isEmpty()) {
            assert artifactAssociationList.get(0).getAssociationName().equals("location");
            location = artifactAssociationList.get(0).getAssociationValue();
            return location;
        } else {
            throw new Exception("Spring service location not found in scenario config");
        }
    }

    private String getContextXMLName(List<ArtifactDependency> artifactDependencyList)
            throws Exception {
        String contextXML;
        if (!artifactDependencyList.isEmpty()) {
            contextXML = artifactDependencyList.get(0).getDepArtifactName();
            return contextXML;
        } else {
            throw new Exception("Context XML name not found in test scenario config");
        }
    }

    private String getJarServiceGroup(String artifactName) {
        String fileName = artifactName;
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        if (fileName.indexOf(".") != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        fileName = fileName + new Random().nextInt(100);
        return fileName;
    }

    private static String convertXMLFileToString(String fileName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            InputStream inputStream = new FileInputStream(new File(fileName));
            org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(stw));
            return stw.toString();
        } catch (Exception e) {
            log.error("Error converting XML file to String");
        }
        return null;
    }

    public void updateESBConfiguration(final String sessionCookie, String backendURL,
                                       String configName, String artifactLocation,
                                       String productName)
            throws IOException, AxisFault, XMLStreamException,
                   ProxyServiceAdminProxyAdminException, SequenceEditorException,
                   EndpointAdminEndpointAdminException, LocalEntryAdminException,
                   TaskManagementException, TransformerException, SAXException, ServletException,
                   ParserConfigurationException {


        String scenarioConfigDir = artifactLocation + File.separator + "synapseconfig" + File.separator +
                                   configName;
        FolderTraversar dirTraversar = new FolderTraversar(new File(scenarioConfigDir));
        String[] configDir = dirTraversar.getConfigDirectories();
        for (String aConfigDir : configDir) {
            copyExtenders(scenarioConfigDir, aConfigDir, productName);
            copyClassesDir(scenarioConfigDir, aConfigDir, productName);
            copyConfigurationDir(scenarioConfigDir, aConfigDir, productName);
            deployProxyServices(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployEndPoints(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deploySequences(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployLocalEntry(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployEventSource(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployPriorityExecutors(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployMessageStore(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployMessageProcessor(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
            deployTaskScheduler(sessionCookie, backendURL, scenarioConfigDir, dirTraversar, aConfigDir);
        }
        updateSynapseConfig(sessionCookie, backendURL, scenarioConfigDir);//update synapse xml
    }

    private void copyExtenders(String scenarioConfigDir, String aConfigDir, String productName)
            throws IOException {
        if (aConfigDir.equalsIgnoreCase(JAR_DIR)) {
            File jarDir = new File(scenarioConfigDir + File.separator + aConfigDir);
            String productHome = ProductConstant.getCarbonHome(productName);

            assert new File(productHome).exists() : productName + " must be local a installation. Framework " +
                                                    "cannot copy config files " + "to remote servers";

            File componentLib = new File(productHome + File.separator + "repository" + File.separator +
                                         "components" + File.separator + "lib");

            File dropinsLib = new File(File.separator + "repository" + File.separator + "components" +
                                       File.separator + "dropins");

            FolderTraversar traversar = new FolderTraversar(jarDir);
            String[] jarList = traversar.getConfigDirectories();
            for (String folderName : jarList) {
                if (folderName.contains("lib")) {
                    File lib = new File(jarDir + File.separator + "lib");
                    if (lib.list().length != 0) {
                        FileManipulator.copyDir(lib, componentLib);
                    }
                }

                if (folderName.contains("dropins")) {
                    File dropins = new File(jarDir + File.separator + "dropins");
                    if (dropins.list().length != 0) {
                        FileManipulator.copyDir(dropins, dropinsLib);
                    }
                }
            }
        }
    }

    private void copyConfigurationDir(String scenarioConfigDir, String aConfigDir,
                                      String productName) throws IOException {
        if (aConfigDir.equalsIgnoreCase(CONFIG_DIR)) {
            File configDir = new File(scenarioConfigDir + File.separator + aConfigDir);
            File productHomeDir = new File(ProductConstant.getCarbonHome(productName) + File.separator +
                                           "repository" + File.separator + "conf");
            assert productHomeDir.exists() : productName + " must be local installation, framework " +
                                             "cannot copy config files " + "to remote servers";
            String[] configList = configDir.list();
            if (configDir.exists() && configList.length != 0 && productHomeDir.exists()) {

                FileManipulator.copyDir(configDir, productHomeDir);
            }
        }
    }

    private void copyClassesDir(String scenarioConfigDir, String aConfigDir,
                                String productName)
            throws IOException {

        if (aConfigDir.equalsIgnoreCase(CLASSES_DIR)) {
            File classesDir = new File(scenarioConfigDir + File.separator + aConfigDir);
            File productConfigDir = new File(ProductConstant.getCarbonHome(productName) +
                                             File.separator + "lib" + File.separator + "core" +
                                             File.separator + "WEB-INF" + File.separator + "classes");
            assert productConfigDir.exists() : productName + " must be started locally, framework " +
                                               "cannot copy config files " + "to remote servers";
            String[] classesFileList = classesDir.list();

            if (classesDir.exists() && classesFileList.length != 0 && productConfigDir.exists()) {
                FileManipulator.copyDir(classesDir, productConfigDir);
            }
        }
    }

    public void updateSynapseConfig(String sessionCookie, String backendURL,
                                    String scenarioConfigDir)
            throws IOException, ParserConfigurationException, SAXException, XMLStreamException,
                   ServletException, TransformerException {

        SynapseConfigAdminClient synapseConfigAdmin =
                new SynapseConfigAdminClient(sessionCookie, backendURL);
        String synapseXmlPath = scenarioConfigDir + File.separator + "synapse.xml";
        File synapseXmlFile = new File(synapseXmlPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        if (synapseXmlFile.exists()) {
            Document doc = builder.parse(synapseXmlFile);
            String fileContent = getStringFromDocument(doc);
            assert fileContent != null : "Synapse file content empty";
            synapseConfigAdmin.updateConfiguration(fileContent);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ignored) {

            }
        }
    }

    private String getStringFromDocument(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    private void deployEndPoints(String sessionCookie, String backendURL,
                                 String scenarioConfigDir, FolderTraversar dirTraversar,
                                 String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException {

        if (aConfigDir.equalsIgnoreCase(ENDPOINT_DIR)) {
            String[] endPointFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + ENDPOINT_DIR));
            for (String aEndPointFiles : endPointFiles) {
                log.info("Deploying endpoint configuration in " + aEndPointFiles);
                EndPointAdminClient endPointAdmin = new EndPointAdminClient(backendURL, sessionCookie);
                URL endpoint = new URL("file:///" + scenarioConfigDir + File.separator +
                                       ENDPOINT_DIR + File.separator + aEndPointFiles);

                DataHandler endpointDh = new DataHandler(endpoint);
                EsbEndpointSetter esbendpointSetter = new EsbEndpointSetter();
                endPointAdmin.addEndPoint(esbendpointSetter.setEndpointURL(endpointDh));
            }
        }
    }

    private void deployLocalEntry(String sessionCookie, String backendURL,
                                  String scenarioConfigDir, FolderTraversar dirTraversar,
                                  String aConfigDir)
            throws IOException, XMLStreamException, LocalEntryAdminException {
        if (aConfigDir.equalsIgnoreCase(LOCALENTRY_DIR)) {
            String[] eventSourceFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + LOCALENTRY_DIR));
            for (String eventSourceFile : eventSourceFiles) {
                log.info("Deploying Local Entry configuration in " + eventSourceFile);

                LocalEntriesAdminClient localEntryAdmin =
                        new LocalEntriesAdminClient(backendURL, sessionCookie);
                URL localEntry = new URL("file:///" + scenarioConfigDir + File.separator +
                                         LOCALENTRY_DIR + File.separator + eventSourceFile);
                DataHandler endpointDh = new DataHandler(localEntry);
                localEntryAdmin.addLocalEntery(endpointDh);
            }
        }
    }

    private void deployMessageStore(String sessionCookie, String backendURL,
                                    String scenarioConfigDir, FolderTraversar dirTraversar,
                                    String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException,
                   LocalEntryAdminException {
        if (aConfigDir.equalsIgnoreCase(MESSAGESTORE_DIR)) {
            String[] messageStoreFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + MESSAGESTORE_DIR));
            for (String messageStoreFile : messageStoreFiles) {
                log.info("Deploying Message Store configuration in " + messageStoreFile);

                MassageStoreAdminClient messageStoreAdmin =
                        new MassageStoreAdminClient(backendURL, sessionCookie);
                URL messageStore = new URL("file:///" + scenarioConfigDir + File.separator +
                                           MESSAGESTORE_DIR + File.separator + messageStoreFile);
                DataHandler messageStoreDh = new DataHandler(messageStore);
                messageStoreAdmin.addMessageStore(messageStoreDh);
            }
        }
    }

    private void deployMessageProcessor(String sessionCookie, String backendURL,
                                        String scenarioConfigDir, FolderTraversar dirTraversar,
                                        String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException,
                   LocalEntryAdminException {
        if (aConfigDir.equalsIgnoreCase(MESSAGEPROCESSOR_DIR)) {
            String[] messageProcessorFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + MESSAGEPROCESSOR_DIR));
            for (String messageProcessorFile : messageProcessorFiles) {
                log.info("Deploying Message Processor configuration in " + messageProcessorFile);

                MessageProcessorClient messageProcessorAdmin =
                        new MessageProcessorClient(backendURL, sessionCookie);
                URL endpoint = new URL("file:///" + scenarioConfigDir + File.separator +
                                       MESSAGEPROCESSOR_DIR + File.separator + messageProcessorFile);
                DataHandler messageProcessorDh = new DataHandler(endpoint);
                messageProcessorAdmin.addMessageProcessor(messageProcessorDh);
            }
        }
    }

    private void deployPriorityExecutors(String sessionCookie, String backendURL,
                                         String scenarioConfigDir, FolderTraversar dirTraversar,
                                         String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException {
        if (aConfigDir.equalsIgnoreCase(PRIORITYEXECUTOR_DIR)) {
            String[] prorityExeFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + PRIORITYEXECUTOR_DIR));
            for (String prorityExeFile : prorityExeFiles) {
                log.info("Deploying Priority Executor configuration in " + prorityExeFile);

                PriorityMediationAdminClient priorityMediationAdminClient =
                        new PriorityMediationAdminClient(backendURL, sessionCookie);
                URL endpoint = new URL("file:///" + scenarioConfigDir + File.separator +
                                       PRIORITYEXECUTOR_DIR + File.separator + prorityExeFile);
                DataHandler priorityDh = new DataHandler(endpoint);
                priorityMediationAdminClient.addPriorityMediator("", priorityDh);
            }
        }
    }

    private void deployTaskScheduler(String sessionCookie, String backendURL,
                                     String scenarioConfigDir, FolderTraversar dirTraversar,
                                     String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException,
                   SequenceEditorException, TaskManagementException {
        if (aConfigDir.equalsIgnoreCase(TASKS_DIR)) {
            String[] tasksFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + TASKS_DIR));
            for (String tasksFile : tasksFiles) {
                log.info("Deploying Tasks configuration in " + tasksFile);
                TaskAdminClient taskAdmin =
                        new TaskAdminClient(backendURL);
                URL endpoint = new URL("file:///" + scenarioConfigDir + File.separator +
                                       TASKS_DIR + File.separator + tasksFile);
                DataHandler taskDh = new DataHandler(endpoint);


                taskAdmin.addTask(sessionCookie, taskDh);
            }
        }
    }

    private void deployEventSource(String sessionCookie, String backendURL,
                                   String scenarioConfigDir, FolderTraversar dirTraversar,
                                   String aConfigDir)
            throws IOException, XMLStreamException, EndpointAdminEndpointAdminException {
        if (aConfigDir.equalsIgnoreCase(EVENTSORCE_DIR)) {
            String[] eventSourceFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + EVENTSORCE_DIR));
            for (String eventSourceFile : eventSourceFiles) {
                log.info("Deploying event source configuration in " + eventSourceFile);
                EventSourceAdminClient eventSourceAdmin =
                        new EventSourceAdminClient(backendURL, sessionCookie);
                URL endpoint = new URL("file:///" + scenarioConfigDir + File.separator +
                                       EVENTSORCE_DIR + File.separator + eventSourceFile);
                DataHandler endpointDh = new DataHandler(endpoint);
                eventSourceAdmin.addEventSource();
            }
        }
    }


    private void deployProxyServices(String sessionCookie, String backendURL,
                                     String scenarioConfigDir, FolderTraversar dirTraversar,
                                     String aConfigDir)
            throws IOException, XMLStreamException, ProxyServiceAdminProxyAdminException {
        if (aConfigDir.equalsIgnoreCase(PROXY_SERVICES_DIR)) {
            String[] proxyServiceFiles = dirTraversar.getConfigFiles
                    (new File(scenarioConfigDir + File.separator + PROXY_SERVICES_DIR));
            for (String aProxyServiceFiles : proxyServiceFiles) {
                log.info("Deploying proxy configuration in " + aProxyServiceFiles);
                ProxyServiceAdminClient proxyAdmin = new ProxyServiceAdminClient(backendURL);
                URL proxy = new URL("file:///" + scenarioConfigDir + File.separator +
                                    PROXY_SERVICES_DIR + File.separator + aProxyServiceFiles);
                DataHandler proxyDh = new DataHandler(proxy);
                EsbEndpointSetter endpointSetter = new EsbEndpointSetter();
                proxyAdmin.addProxyService(sessionCookie, endpointSetter.setEndpointURL(proxyDh));
            }
        }
    }

    private void deploySequences(String sessionCookie, String backendURL,
                                 String scenarioConfigDir, FolderTraversar dirTraversar,
                                 String aConfigDir)
            throws IOException, SequenceEditorException, XMLStreamException {

        if (aConfigDir.equalsIgnoreCase(SEQUENCES_DIR)) {
            String[] sequenceConfigFiles =
                    dirTraversar.getConfigFiles(new File(scenarioConfigDir + File.separator + SEQUENCES_DIR));

            for (String aSequenceConfigFiles : sequenceConfigFiles) {
                log.info("Deploying sequence in " + aSequenceConfigFiles);
                SequenceAdminServiceClient adminServiceSequenceAdmin = new SequenceAdminServiceClient(backendURL);
                URL sequence = new URL("file:///" + scenarioConfigDir + File.separator +
                                       SEQUENCES_DIR + File.separator + aSequenceConfigFiles);
                String sequenceConfig = convertXMLFileToString(scenarioConfigDir + File.separator +
                                                               SEQUENCES_DIR + File.separator
                                                               + aSequenceConfigFiles);
                DataHandler sequenceDh = new DataHandler(sequence);

                if (sequenceConfig.contains("name=\"main\"")) {
                    adminServiceSequenceAdmin.updateDynamicSequence(sessionCookie, "main",
                                                                    AXIOMUtil.stringToOM(sequenceConfig));
                }
                if (sequenceConfig.contains("name=\"fault\"")) {
                    adminServiceSequenceAdmin.updateDynamicSequence(sessionCookie, "fault",
                                                                    AXIOMUtil.stringToOM(sequenceConfig));
                }
                adminServiceSequenceAdmin.addSequence(sessionCookie, sequenceDh);
            }
        }
    }

    private String getPath(String path) {
        String value = "";
        if (path != null) {
            value = path;
            value = value.replaceAll("[\\\\/]", File.separator);
            if (value.startsWith(File.separator)) {
                value = value.substring(1);
            }
            if (value.endsWith(File.separator)) {
                value = value.substring(0, value.length() - 1);
            }
        }
        return value;
    }

    private String getMediaType(ArtifactType fileType) {
        if (ArtifactType.csv == fileType) {
            return "text/comma-separated-values";
        } else if (ArtifactType.excel == fileType) {
            return "application/vnd.ms-excel";
        } else if (ArtifactType.xslt == fileType) {
            return "application/xml";
        } else {
            return null;
        }
    }

    public EnvironmentVariables getProductEnvironment(String productName, int userId)
            throws LoginAuthenticationExceptionException, RemoteException {
        if (ProductConstant.APP_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().as(userId);
            return builder.build().getAs();

        } else if (ProductConstant.ESB_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().esb(userId);
            return builder.build().getEsb();

        } else if (ProductConstant.BPS_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().bps(userId);
            return builder.build().getBps();

        } else if (ProductConstant.DSS_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().dss(userId);
            return builder.build().getDss();

        } else if (ProductConstant.GREG_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
            return builder.build().getGreg();

        } else if (ProductConstant.BRS_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().brs(userId);
            return builder.build().getBrs();

        } else if (ProductConstant.MS_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().ms(userId);
            return builder.build().getMs();

        } else if (ProductConstant.MB_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().mb(userId);
            return builder.build().getMb();

        } else if (ProductConstant.CEP_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().cep(userId);
            return builder.build().getCep();

        } else if (ProductConstant.AXIS2_SERVER_NAME.equals(productName)) {
            EnvironmentBuilder builder = new EnvironmentBuilder().axis2(userId);
            return builder.build().getAxis2();
        }

        return null;
    }

    public EnvironmentVariables getClusterEnvironment(String productName, int userId)
            throws LoginAuthenticationExceptionException, RemoteException {
        ClusterReader reader = new ClusterReader();
        String pid = reader.getActiveClusterNode(productName);
        EnvironmentBuilder builder = new EnvironmentBuilder().clusterNode(pid, userId);
        return builder.build().getClusterNode(pid);
    }
}
