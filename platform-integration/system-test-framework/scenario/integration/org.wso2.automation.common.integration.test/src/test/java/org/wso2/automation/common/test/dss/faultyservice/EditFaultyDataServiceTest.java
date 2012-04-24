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
package org.wso2.automation.common.test.dss.faultyservice;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.DataServiceAdminService;
import org.wso2.carbon.dataservices.ui.fileupload.stub.ExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.automation.common.test.dss.utils.DataServiceUtility;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

public class EditFaultyDataServiceTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(EditFaultyDataServiceTest.class);
    private final String serviceFile = "FaultyDataService.dbs";

    @Override
    protected void setServiceName() {
        serviceName = "FaultyDataService";
    }

    @Override
    @Test(priority = 0)
    public void serviceDeployment()
            throws ServiceAdminException, RemoteException, ExceptionException,
                   MalformedURLException {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact;
        try {
            dhArtifact = new DataHandler(new URL("file://" + serviceFileLocation + File.separator + serviceFile));
        } catch (MalformedURLException e) {
            log.error("Resource file Not Found ", e);
            throw e;
        }
        Assert.assertNotNull(dhArtifact, "Service File Not Found");
        Assert.assertTrue(adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact)
                , "Service Deployment Failed while uploading service file");
        log.info(serviceName + " uploaded");
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void isServiceFaulty() throws RemoteException {
        adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " is faulty");

    }

    @Test(priority = 2, dependsOnMethods = {"isServiceFaulty"})
    public void editFaultyService()
            throws RemoteException, RSSAdminRSSDAOExceptionException, XMLStreamException {
        DataServiceAdminService dataServiceAdminService = new DataServiceAdminService(dssBackEndUrl);
        String serviceContent;
        String newServiceContent = null;
        SqlDataSourceUtil dssUtil = new SqlDataSourceUtil(sessionCookie, dssBackEndUrl, FrameworkFactory.getFrameworkProperties("DSS"), Integer.parseInt(userInfo.getUserId()));
        dssUtil.createDataSource(getSqlScript());
        serviceContent = dataServiceAdminService.getDataServiceContent(sessionCookie, serviceName);

        try {
            OMElement dbsFile = AXIOMUtil.stringToOM(serviceContent);
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("org.wso2.ws.dataservice.protocol".equals(value)) {
                    property.setText(dssUtil.getJdbcUrl());

                } else if ("org.wso2.ws.dataservice.user".equals(value)) {
                    property.setText(dssUtil.getDatabaseUser());

                } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                    property.setText(dssUtil.getDatabasePassword());
                }
            }
            log.debug(dbsFile);
            newServiceContent = dbsFile.toString();
        } catch (XMLStreamException e) {
            log.error("XMLStreamException while handling data service content ", e);
            throw new XMLStreamException("XMLStreamException while handling data service content ", e);
        }
        Assert.assertNotNull("Could not edited service content", newServiceContent);
        dataServiceAdminService.editDataService(sessionCookie, serviceName, "", newServiceContent);
        log.info(serviceName + " edited");

    }

    @Test(priority = 4, dependsOnMethods = {"editFaultyService"})
    public void serviceReDeployment() throws RemoteException {
        adminServiceClientDSS.isServiceDeployed(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " redeployed");
        //todo this sleep should be removed after fixing CARBON-11900 gira
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Assert.fail("Thread InterruptedException");
        }


    }

    @Test(priority = 5, dependsOnMethods = {"serviceReDeployment"})
    public void serviceInvocation() throws RemoteException, ServiceAdminException {
        OMElement response;
        String serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            response = axisServiceClient.sendReceive(getPayload(), serviceEndPoint, "showAllOffices");
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("service invocation success");
    }

    @Test(priority = 6, dependsOnMethods = {"serviceInvocation"})
    public void deleteService() throws ServiceAdminException, RemoteException {
        deleteService(serviceName);
        log.info(serviceName + " deleted");
    }

    private OMElement getPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/faulty_dataservice", "ns1");
        return fac.createOMElement("showAllOffices", omNs);
    }

    private ArrayList<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(new File(resourceFileLocation + File.separator + "sql" + File.separator + "MySql" + File.separator + "CreateTables.sql"));
        al.add(new File(resourceFileLocation + File.separator + "sql" + File.separator + "MySql" + File.separator + "Offices.sql"));
        return al;
    }

}
