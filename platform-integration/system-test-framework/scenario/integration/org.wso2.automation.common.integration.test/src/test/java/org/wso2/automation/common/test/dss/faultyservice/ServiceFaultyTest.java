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
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

public class ServiceFaultyTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(ServiceFaultyTest.class);

    private final String serviceFile = "FaultyDataService.dbs";

    @Override
    protected void setServiceName() {
        serviceName = "FaultyDataService";
    }

    @Test(priority = 0)
    @Override
    public void serviceDeployment()
            throws ServiceAdminException, RemoteException, ExceptionException {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact;
        try {
            dhArtifact = createArtifact(serviceFile, getSqlScript());
        } catch (RSSAdminRSSDAOExceptionException e) {
            throw new RuntimeException(e);
        }

        adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact);
        isServiceDeployed(serviceName);
        setServiceEndPointHttp(serviceName);
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void serviceInvocation() throws AxisFault {
        OMElement response;
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            response = axisServiceClient.sendReceive(getPayload(), serviceEndPoint, "showAllOffices");
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Service invocation success");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceInvocation"})
    public void faultyService() throws RemoteException, XMLStreamException {
        String serviceContent;
        String newServiceContent = null;
        DataServiceAdminService dataServiceAdminService = new DataServiceAdminService(dssBackEndUrl);
        Assert.assertTrue(adminServiceClientDSS.isServiceExist(sessionCookie, serviceName), "Service not in faulty service list");

        serviceContent = dataServiceAdminService.getDataServiceContent(sessionCookie, serviceName);

        try {
            OMElement dbsFile = AXIOMUtil.stringToOM(serviceContent);
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("org.wso2.ws.dataservice.user".equals(value)) {
                    property.setText("invalidUser");

                } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                    property.setText("password");
                }
            }
            log.debug(dbsFile);
            newServiceContent = dbsFile.toString();
        } catch (XMLStreamException e) {
            log.error("XMLStreamException while handling data service content " + e);
            throw new XMLStreamException("XMLStreamException while handling data service content " , e);
        }
        Assert.assertNotNull("Could not edited service content", newServiceContent);
        dataServiceAdminService.editDataService(sessionCookie, serviceName, "", newServiceContent);
        log.info(serviceName + " edited");
    }

    @Test(priority = 3, dependsOnMethods = {"faultyService"})
    public void isServiceFaulty() throws RemoteException {
        adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " is faulty");

    }

    @Test(priority = 4, dependsOnMethods = {"isServiceFaulty"})
    public void deleteFaultyService() throws RemoteException {
        FaultyService faultyService;
        faultyService = adminServiceClientDSS.getFaultyServiceData(sessionCookie, serviceName);
        adminServiceClientDSS.deleteFaultyService(sessionCookie, faultyService.getArtifact());
        log.info(serviceName + " deleted");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("InterruptedException " + e.getMessage());
            Assert.fail("InterruptedException " + e.getMessage());
        }
        Assert.assertFalse(adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName), "Service Still in service list");
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
