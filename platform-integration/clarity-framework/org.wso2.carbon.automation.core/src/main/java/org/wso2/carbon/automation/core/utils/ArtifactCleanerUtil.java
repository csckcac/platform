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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.mediation.SynapseConfigAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.service.mgt.ServiceAdminClient;
import org.wso2.carbon.automation.api.clients.webapp.mgt.JAXWSWebappAdminClient;
import org.wso2.carbon.automation.api.clients.webapp.mgt.WebAppAdminClient;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.sequences.stub.types.SequenceEditorException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.utils.ServerConstants;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
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
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import static java.io.File.separator;

public class ArtifactCleanerUtil {
    private static final Log log = LogFactory.getLog(ArtifactCleanerUtil.class);

    public void deleteAllCarArtifacts(String serviceURL, String sessionCookie) throws Exception {
        String carAppList[] = getAllCarArtifactList(sessionCookie, serviceURL);
        ApplicationAdminClient adminServiceApplicationAdmin;
        try {
            adminServiceApplicationAdmin = new ApplicationAdminClient(serviceURL, sessionCookie);
        } catch (AxisFault axisFault) {
            log.error("AxisFault " + axisFault.getMessage());
            throw new AxisFault("AxisFault :" + axisFault.getMessage());
        }

        if (carAppList != null) {
            for (String aCarAppList : carAppList) {
                try {
                    adminServiceApplicationAdmin.deleteApplication(aCarAppList);
                } catch (ApplicationAdminExceptionException e) {
                    log.error("CApp deployment error " + e);
                    throw new ApplicationAdminExceptionException("CApp deployment error :" + e);
                } catch (RemoteException e) {
                    log.error("CApp deployment error " + e);
                    throw new RemoteException("CApp deployment error :" + e);
                }
            }
        }
        waitForCarUndeployment(sessionCookie, serviceURL);
    }

    public static String[] getAllCarArtifactList(String sessionCookie, String serviceURL)
            throws ApplicationAdminExceptionException, RemoteException {
        String[] appList;
        ApplicationAdminClient adminServiceApplicationAdmin;
        try {
            adminServiceApplicationAdmin = new ApplicationAdminClient(serviceURL, sessionCookie);
            appList = adminServiceApplicationAdmin.listAllApplications(sessionCookie);

        } catch (ApplicationAdminExceptionException e) {
            log.error("Error occurred while getting CApp list " + e);
            throw new ApplicationAdminExceptionException("Error occurred while getting CApp list " + e);
        } catch (RemoteException e) {
            log.error("Error occurred while getting CApp list " + e);
            throw new RemoteException("Error occurred while getting CApp list " + e);
        }
        return appList;
    }

    public String[] deleteAllNotAdminServices(String sessionCookie, String serviceURL)
            throws RemoteException {
        String[] appList = null;
        ServiceAdminClient adminServiceService;
        adminServiceService = new ServiceAdminClient(serviceURL, sessionCookie);
        adminServiceService.deleteAllNonAdminServiceGroups();

        return appList;

    }


    public void waitForCarUndeployment(String sessionCookie, String ServiceURL) throws Exception {
        int serviceTimeOut = 0;
        while (getAllCarArtifactList(sessionCookie, ServiceURL) != null) {
            if (serviceTimeOut == 0) {
            } else if (serviceTimeOut > 200) { //Check for the applist for 100 seconds
                // if Service not available after timeout.
                throw new Exception("Car artifact clearance failed");
            }
            try {
                Thread.sleep(500);
                serviceTimeOut++;
            } catch (InterruptedException ignored) {
            }
        }
    }


    public void deleteMatchingCarArtifact(String sessionCookie, String backendURL, String appName)
            throws RemoteException, ApplicationAdminExceptionException {
        ApplicationAdminClient carMgtAdmin = new ApplicationAdminClient(backendURL, sessionCookie);
        carMgtAdmin.deleteMatchingApplication(sessionCookie, appName);
    }

    public void deleteWebApp(String sessionCookie, String fileName, String backendURL)
            throws RemoteException {
        WebAppAdminClient adminServiceWebAppAdmin = new WebAppAdminClient(backendURL, sessionCookie);
        adminServiceWebAppAdmin.deleteWebAppFile(fileName);
    }

    /**
     * Delete JaxWs webapp file
     *
     * @param sessionCookie - login session
     * @param backendURL    backend URL of the service
     * @param webappName    webapp name
     * @throws RemoteException if war file undeployment fails
     */
    public void deleteJaxWsWebapp(String sessionCookie, String backendURL, String webappName)
            throws RemoteException {
        JAXWSWebappAdminClient jaxwsWebappAdminClient = new JAXWSWebappAdminClient(backendURL, sessionCookie);
        jaxwsWebappAdminClient.deleteStartedWebapps(new String[]{webappName});
    }

    public void deleteBpel(String sessionCookie, String backendURL, String fileName)
            throws PackageManagementException, InterruptedException, RemoteException {
        BpelPackageManagementClient packageManager = new BpelPackageManagementClient(backendURL, sessionCookie);
        packageManager.undeployBPEL(fileName);
    }

    protected String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AuthenticatorClient loginClient = new AuthenticatorClient(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void deleteServiceByGroup(String sessionCookie, String backendURL, String artifactName,
                                     String productName) throws RemoteException {

        if (productName.equals(ProductConstant.AXIS2_SERVER_NAME)) {
            String axisServiceRepo = System.getProperty(ServerConstants.CARBON_HOME) + separator +
                                     "samples" + separator + "axis2Server" + separator +
                                     "repository" + separator + "services";

            File artifactFile = new File(axisServiceRepo + artifactName);
            artifactFile.delete();

        } else {
            ServiceAdminClient serviceAdmin = new ServiceAdminClient(backendURL, sessionCookie);
            String serviceFileName = getServiceName(artifactName);
            log.info("Service Name " + serviceFileName);
            serviceAdmin.deleteMatchingServiceByGroup(serviceFileName);
        }
    }

    public void deleteDataService(String sessionCookie, String backEndUrl, String artifactName,
                                  List<ArtifactDependency> artifactDependencyList)
            throws RemoteException, ServiceAdminException, ResourceAdminServiceExceptionException {
        ServiceAdminClient serviceAdmin = new ServiceAdminClient(backEndUrl, sessionCookie);
        ServiceMetaData serviceInfo = serviceAdmin.getServicesData(artifactName.substring(0, artifactName.indexOf(".dbs")));
        serviceAdmin.deleteService(new String[]{serviceInfo.getServiceGroupName()});

        Iterator iterator = artifactDependencyList.iterator();
        while (iterator.hasNext()) {
            ArtifactDependency dependency = (ArtifactDependency) iterator.next();
            if (artifactDependencyList != null || artifactDependencyList.size() > 0) {
                if (ArtifactType.sql == dependency.getDepArtifactType()) {
                    //TODO - drop database
                } else {
                    ResourceAdminServiceClient adminServiceResourceAdmin = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
                    if (!adminServiceResourceAdmin.deleteResource("/_system/governance/automation/resources/"
                                                                  + dependency.getDepArtifactName())) {
                        log.error(dependency.getDepArtifactName() + " Deletion failed");

                    }
                }
            }
        }
    }

    public void deleteAllServicesByType(String sessionCookie, String type, String backendURL)
            throws RemoteException {
        ServiceAdminClient serviceAdmin = new ServiceAdminClient(backendURL, sessionCookie);
        serviceAdmin.deleteAllServicesByType(type);
    }

    public String getServiceName(String artifactName) {
        String fileName = artifactName;
        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        if (fileName.indexOf(".") != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public void restToDefaultConfiguration(String sessionCookie, String backendURL)
            throws IOException, SequenceEditorException, XMLStreamException,
                   ParserConfigurationException,
                   SAXException, TransformerException, ServletException {

        String configLocation = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME)
                                + separator + "synapseconfig" + separator +
                                "defaultconfig" + separator + "default-synapse.xml";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(new File(configLocation));
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter StringContainer = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(StringContainer));


        SynapseConfigAdminClient synapseConfigAdmin = new SynapseConfigAdminClient(backendURL,sessionCookie);
        synapseConfigAdmin.updateConfiguration(StringContainer.toString());

    }
}



