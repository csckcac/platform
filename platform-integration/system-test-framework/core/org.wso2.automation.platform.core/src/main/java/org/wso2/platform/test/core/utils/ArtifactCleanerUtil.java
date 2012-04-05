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

package org.wso2.platform.test.core.utils;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.sequences.stub.types.SequenceEditorException;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.platform.test.core.ProductConstant;
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

public class ArtifactCleanerUtil {
    private static final Log log = LogFactory.getLog(ArtifactCleanerUtil.class);

    public void deleteAllCarArtifacts(String serviceURL, String sessionCookie) throws Exception {
        String carAppList[] = getAllCarArtifactList(sessionCookie, serviceURL);
        AdminServiceApplicationAdmin adminServiceApplicationAdmin;
        try {
            adminServiceApplicationAdmin = new AdminServiceApplicationAdmin(serviceURL);
        } catch (AxisFault axisFault) {
            log.error("AxisFault " + axisFault.getMessage());
            throw new AxisFault("AxisFault :" + axisFault.getMessage());
        }

        if (carAppList != null) {
            for (String aCarAppList : carAppList) {
                try {
                    adminServiceApplicationAdmin.deleteApplication(sessionCookie, aCarAppList);
                } catch (ApplicationAdminExceptionException e) {
                    log.error("CApp deployment error " + e.getMessage());
                    throw new ApplicationAdminExceptionException("CApp deployment error :" + e.getMessage());
                } catch (RemoteException e) {
                    log.error("CApp deployment error " + e.getMessage());
                    throw new RemoteException("CApp deployment error :" + e.getMessage());
                }
            }
        }
        waitForCarUndeployment(sessionCookie, serviceURL);
    }

    public static String[] getAllCarArtifactList(String sessionCookie, String serviceURL)
            throws ApplicationAdminExceptionException, RemoteException {
        String[] appList;
        AdminServiceApplicationAdmin adminServiceApplicationAdmin;
        try {
            adminServiceApplicationAdmin = new AdminServiceApplicationAdmin(serviceURL);
            appList = adminServiceApplicationAdmin.listAllApplications(sessionCookie);

        } catch (ApplicationAdminExceptionException e) {
            log.error("Error occurred while getting CApp list " + e.getMessage());
            throw new ApplicationAdminExceptionException("Error occurred while getting CApp list " + e.getMessage());
        } catch (RemoteException e) {
            log.error("Error occurred while getting CApp list "+ e.getMessage());
            throw new RemoteException("Error occurred while getting CApp list " + e.getMessage());
        }
        return appList;
    }

    public String[] deleteAllNotAdminServices(String sessionCookie, String serviceURL) {
        String[] appList = null;
        AdminServiceService adminServiceService;
        adminServiceService = new AdminServiceService(serviceURL);
        adminServiceService.deleteAllNonAdminServiceGroups(sessionCookie);

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
        AdminServiceApplicationAdmin carMgtAdmin = new AdminServiceApplicationAdmin(backendURL);
        carMgtAdmin.deleteMatchingApplication(sessionCookie, appName);
    }

    public void deleteWebApp(String sessionCookie, String fileName, String backendURL)
            throws RemoteException {
        AdminServiceWebAppAdmin AdminServiceWebAppAdmin = new AdminServiceWebAppAdmin(backendURL);
        AdminServiceWebAppAdmin.deleteWebAppFile(sessionCookie, fileName);
    }

    public void deleteBpel(String sessionCookie, String backendURL, String fileName)
            throws PackageManagementException, InterruptedException, RemoteException {
        AdminServiceBpelPackageManager packageManager = new AdminServiceBpelPackageManager(backendURL, sessionCookie);
        packageManager.undeployBPEL(fileName);
    }

    protected String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void deleteServiceByGroup(String sessionCookie, String backendURL, String artifactName)
            throws RemoteException {
        AdminServiceService serviceAdmin = new AdminServiceService(backendURL);
        String serviceFileName = getServiceName(artifactName);
        log.info("Service Name " + serviceFileName);
        serviceAdmin.deleteMatchingServiceByGroup(sessionCookie, serviceFileName);
    }

    public void deleteDataService(String sessionCookie, String backEndUrl, String artifactName,
                                  List<ArtifactDependency> artifactDependencyList) {
        AdminServiceService serviceAdmin = new AdminServiceService(backEndUrl);
        ServiceMetaData serviceInfo = serviceAdmin.getServicesData(sessionCookie, artifactName.substring(0, artifactName.indexOf(".dbs")));
        serviceAdmin.deleteService(sessionCookie, new String[]{serviceInfo.getServiceGroupName()});

        Iterator iterator = artifactDependencyList.iterator();
        while (iterator.hasNext()) {
            ArtifactDependency dependency = (ArtifactDependency) iterator.next();
            if (artifactDependencyList != null || artifactDependencyList.size() > 0) {
                if (ArtifactType.sql == dependency.getDepArtifactType()) {
                    //TODO - drop database
                } else {
                    AdminServiceResourceAdmin adminServiceResourceAdmin = new AdminServiceResourceAdmin(backEndUrl);
                    adminServiceResourceAdmin.deleteResource(sessionCookie, "/_system/governance/automation/resources/" + dependency.getDepArtifactName());
                }
            }
        }
    }

    public void deleteAllServicesByType(String sessionCookie, String type, String backendURL)
            throws RemoteException {
        AdminServiceService serviceAdmin = new AdminServiceService(backendURL);
        serviceAdmin.deleteAllServicesByType(sessionCookie, type);
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
            throws IOException, SequenceEditorException, XMLStreamException,ParserConfigurationException,
                   SAXException, TransformerException, ServletException {

        String configLocation = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME)
                                + File.separator + "synapseconfig" + File.separator +
                                "defaultconfig" + File.separator + "default-synapse.xml";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(new File(configLocation));
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter StringContainer = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(StringContainer));


        AdminServiceSynapseConfigAdmin synapseConfigAdmin = new AdminServiceSynapseConfigAdmin(sessionCookie, backendURL);
        synapseConfigAdmin.updateConfiguration(StringContainer.toString());

    }
}



