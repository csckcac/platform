/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.registry.version.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class MetaDataTestCase {

    private ResourceAdminServiceClient resourceAdminClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetaDataServiceClient;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getBackEndUrl(),
                                            userInfo.getUserName(), userInfo.getPassword());
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
    }


    @Test(groups = {"wso2.greg"}, description = "add a policy at leaf level and version it")
    public void testVersionPolicy()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        Boolean nameExists = false;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addPolicy("desc 1", dataHandler);
        PolicyBean policyBean = listMetaDataServiceClient.listPolicies();
        String[] names = policyBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("policy.xml")) {
                nameExists = true;
            }
        }

        assertTrue(nameExists);
        resourceAdminClient.createVersion("/_system/governance/trunk/policies/policy.xml");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/policies/policy.xml");
        assertEquals(1, vp1.length);
        assertEquals(null, deleteVersion("/_system/governance/trunk/policies/policy.xml"));


    }


    @Test(groups = {"wso2.greg"}, description = "add a wsdl at leaf level and version it")
    public void testVersionWSDL()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        Boolean nameExists = false;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addWSDL("desc 1", dataHandler);

        WSDLBean wsdlBean = listMetaDataServiceClient.listWSDLs();
        String[] names = wsdlBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("echo.wsdl")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        resourceAdminClient.createVersion("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services" +
                                          "/echo/echo.wsdl");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/wsdls/org/wso2/carbon" +
                                                                "/core/services/echo/echo.wsdl");
        assertEquals(1, vp1.length);

        assertEquals(null, deleteVersion("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl"));

    }


    @Test(groups = {"wso2.greg"}, description = "add a schema at leaf level and version it")
    public void testVerSchema()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        Boolean nameExists = false;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "schema" + File.separator + "calculator.xsd";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addSchema("desc 1", dataHandler);
        SchemaBean schemaBean = listMetaDataServiceClient.listSchemas();

        String[] names = schemaBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("calculator.xsd")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);

        resourceAdminClient.createVersion("/_system/governance/trunk/schemas/org/charitha/calculator.xsd");
        VersionPath[] vp1 =
                resourceAdminClient.getVersionPaths("/_system/governance/trunk/schemas/org/charitha/calculator.xsd");
        assertEquals(1, vp1.length);

        assertEquals(null, deleteVersion("/_system/governance/trunk/schemas/org/charitha/calculator.xsd"));

    }


    @Test(groups = {"wso2.greg"}, description = "add a service and create a version of it")
    public void testAddService()
            throws XMLStreamException, IOException, AddServicesServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        Boolean nameExists = false;
        String serviceContent = null;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "services" + File.separator + "service.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        serviceContent = parseISToString(dataHandler.getInputStream());
        governanceServiceClient.addService(AXIOMUtil.stringToOM(serviceContent));

        ServiceBean serviceBean = listMetaDataServiceClient.listServices(null);

        String[] names = serviceBean.getNames();
        System.out.println("name: " + names[0]);
        for (String name : names) {
            if (name.equalsIgnoreCase("abc")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        resourceAdminClient.createVersion("/_system/governance/trunk/wsdls/com/foo/abc.wsdl");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/wsdls/com/foo/abc.wsdl");

        assertEquals(1, vp1.length);

        assertEquals(null, deleteVersion("/_system/governance/trunk/wsdls/com/foo/abc.wsdl"));

    }


    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        int length = resourceAdminClient.getVersionPaths(path).length;
        for (int i = 0; i < length; i++) {
            long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
            String snapshotId = String.valueOf(versionNo);
            resourceAdminClient.deleteVersionHistory(path, snapshotId);
        }
        VersionPath[] vp2 = null;
        vp2 = resourceAdminClient.getVersionPaths(path);

        return vp2;

    }


    @AfterClass
    public void testCleanResources()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource("/_system/governance/trunk/wsdls/com/foo/abc.wsdl");
        resourceAdminClient.deleteResource("/_system/governance/trunk/schemas/org/charitha/calculator.xsd");
        resourceAdminClient.deleteResource("/_system/governance/trunk/policies/policy.xml");
        resourceAdminClient.deleteResource("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl");
        resourceAdminClient.deleteResource("/_system/governance/trunk/services/com/abb/abc");
    }


    private String parseISToString(java.io.InputStream is) {
        java.io.DataInputStream din = new java.io.DataInputStream(is);
        StringBuilder sb = new StringBuilder();
        try {
            String line = null;
            while ((line = din.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }

}
