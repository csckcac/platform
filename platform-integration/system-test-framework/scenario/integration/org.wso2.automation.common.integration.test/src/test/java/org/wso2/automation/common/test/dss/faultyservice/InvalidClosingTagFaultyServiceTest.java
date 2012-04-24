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

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;
import org.wso2.carbon.admin.service.AdminServiceCarbonServerAdmin;
import org.wso2.carbon.dataservices.ui.fileupload.stub.ExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.platform.test.core.utils.ClientConnectionUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

//https://wso2.org/jira/browse/CARBON-11692
public class InvalidClosingTagFaultyServiceTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(InvalidClosingTagFaultyServiceTest.class);

    private final String serviceFile = "FaultyDataService.dbs";

    @Override
    protected void setServiceName() {
        serviceName = "FaultyDataService";
    }

    @Test(priority = 0)
    @Override
    public void serviceDeployment()
            throws ServiceAdminException, IOException, ExceptionException,
                   RSSAdminRSSDAOExceptionException, XMLStreamException, ClassNotFoundException,
                   SQLException {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact;
        dhArtifact = createArtifact(serviceFile, getSqlScript());

        String content = "";
        ByteArrayDataSource dbs;
        try {
            InputStreamReader isReader = new InputStreamReader(dhArtifact.getInputStream());
            BufferedReader br = new BufferedReader(isReader);

            String line;
            while ((line = br.readLine()) != null) {
                content = content + line;
            }
        } catch (IOException e) {
            log.error("Exception Occurred while processing input Stream", e);
            throw new IOException("Exception Occurred while processing input Stream", e);
        }
        Assert.assertTrue(content.contains("</query>"), "query tag missing");
        content = content.replaceFirst("</query>", "</que>");
        dbs = new ByteArrayDataSource(content.getBytes());
        dhArtifact = new DataHandler(dbs);

        Assert.assertTrue(adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact)
                        , "Service Deployment Failed while uploading service file");

    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void isServiceFaulty() throws RemoteException {
        adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " is faulty");
    }

    @Test(priority = 2, dependsOnMethods = {"isServiceFaulty"})
    public void ServerRestarting()
            throws org.wso2.carbon.server.admin.stub.Exception, RemoteException,
                   InterruptedException {
        Thread.sleep(10000);
        log.info("Restarting Server.....");
        restartServer();
        logIn();
    }

    @Test(priority = 3, dependsOnMethods = {"ServerRestarting"})
    public void isServiceFaultyAfterServerRestarting() throws RemoteException {
        adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " is faulty");
    }

    @Test(priority = 4, dependsOnMethods = {"isServiceFaultyAfterServerRestarting"})
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

    private void restartServer() throws RemoteException,
                                        org.wso2.carbon.server.admin.stub.Exception,
                                        InterruptedException {
        AdminServiceCarbonServerAdmin adminServiceCarbonServerAdmin = new AdminServiceCarbonServerAdmin(dssBackEndUrl);
        adminServiceCarbonServerAdmin.restartGracefully(sessionCookie);
        Thread.sleep(30000);
        ClientConnectionUtil.waitForPort(Integer.parseInt(dssServer.getProductVariables().getHttpsPort()),
                                         dssServer.getProductVariables().getHostName());

    }

    private ArrayList<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(new File(resourceFileLocation + File.separator + "sql" + File.separator + "MySql"
                        + File.separator + "CreateTables.sql"));
        al.add(new File(resourceFileLocation + File.separator + "sql" + File.separator + "MySql"
                        + File.separator + "Offices.sql"));
        return al;
    }
}
