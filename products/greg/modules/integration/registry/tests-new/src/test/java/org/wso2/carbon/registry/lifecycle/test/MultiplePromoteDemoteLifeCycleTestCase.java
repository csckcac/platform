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

package org.wso2.carbon.registry.lifecycle.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.governance.utils.FileReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.xml.sax.InputSource;

public class MultiplePromoteDemoteLifeCycleTestCase {

    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private String serviceString;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String LC_NAME = "MultiplePromoteDemoteLC";
    private static final String LC_STATE0 = "Commencement";
    private static final String LC_STATE1 = "Creation";
    private static final String LC_STATE2 = "Development";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String ACTION_TYPE = "type";
    private static final String USER = "user";
    private static final String STATE = "state";
    private static final String TARGET_STATE = "targetState";
    private static final String TYPE = "transition";
    private static final String ACTION_NAME = "name";

    private LifecycleBean lifeCycle;
    private ServiceBean service;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    @BeforeClass
    public void init()
            throws RemoteException, LoginAuthenticationExceptionException, RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                userInfo.getUserName(), userInfo.getPassword());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            userInfo.getUserName(), userInfo.getPassword());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

    }


    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                             "GREG" + File.separator + "services" + File.separator + "intergalacticService.metadata.xml";
        String serviceContent = FileReader.readFile(servicePath);
        OMElement service = AXIOMUtil.stringToOM(serviceContent);
        governanceServiceClient.addService(service);
        ServiceBean bean = listMetadataServiceClient.listServices(null);
        String[] names = bean.getNames();
        boolean serviceStatus = false;
        for (String name : names) {
            if (name.contains(SERVICE_NAME)) {
                serviceStatus = true;
            }
        }

        assertTrue(serviceStatus, "Service not found");

    }

    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileReader.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeClycles) {

            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        String servicePathDev[] = service.getPath();
        serviceString = servicePathDev[0];
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" + serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }


    @Test(groups = "wso2.greg", description = "Promote from Commencement to Creation to Development",
          dependsOnMethods = "testAddLcToService")
    public void testPromoteLC()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   LifeCycleManagementServiceExceptionException, RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME, ACTION_PROMOTE, null);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" + serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1), "LifeCycle not promoted to Creation");
            }
        }

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME, ACTION_PROMOTE, null);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" + serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE2), "LifeCycle not promoted to Development");
            }
        }

    }


    @Test(groups = "wso2.greg", description = "Demote from Development to Creation", dependsOnMethods = "testPromoteLC")
    public void testDemoteLC()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   LifeCycleManagementServiceExceptionException, RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME, ACTION_DEMOTE, null);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" + serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1), "LifeCycle not demoted to Creation");
            }
        }

    }

    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testDemoteLC")
    public void testVerifyAuditAdmin() throws Exception {

        String auditPath = "/_system/governance/repository/components/org.wso2.carbon.governance/lifecycles/history/" +
                           "__system_governance_trunk_services_com_abb_IntergalacticService";

        assertEquals(getAuditRecords(auditPath, 2, USER, false), userInfo.getUserName(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 2, STATE, false), LC_STATE0, "State before transition is not Commencement");
        assertEquals(getAuditRecords(auditPath, 2, TARGET_STATE, false), LC_STATE1, "State after transition is not Creation");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_TYPE, true), TYPE, "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_NAME, true), ACTION_PROMOTE, "Transition is not PROMOTE");

        assertEquals(getAuditRecords(auditPath, 1, USER, false), userInfo.getUserName(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 1, STATE, false), LC_STATE1, "State before transition is not Creation");
        assertEquals(getAuditRecords(auditPath, 1, TARGET_STATE, false), LC_STATE2, "State after transition is not Development");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_TYPE, true), TYPE, "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_NAME, true), ACTION_PROMOTE, "Transition is not PROMOTE");

        assertEquals(getAuditRecords(auditPath, 0, USER, false), userInfo.getUserName(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false), LC_STATE2, "State before transition is not Development");
        assertEquals(getAuditRecords(auditPath, 0, TARGET_STATE, false), LC_STATE1, "State after transition is not Creation");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true), TYPE, "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true), ACTION_DEMOTE, "Transition is not DEMOTE");

    }

    public String getAuditRecords(String auditPath, int node, String item, boolean isChild)
            throws Exception {

        byte[] string = wsRegistryServiceClient.getContent(auditPath);
        String xml = new String(string);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        rootElement.getFirstChild().getNodeValue();
        NodeList nd = rootElement.getChildNodes();

        assertTrue(!(node >= nd.getLength()), "Record does not exist");

        if (!isChild) {
            return nd.item(node).getAttributes().getNamedItem(item).getNodeValue();
        } else {
            return nd.item(node).getFirstChild().getAttributes().getNamedItem(item).getNodeValue();
        }
    }

    @AfterClass()
    public void clear() throws Exception {
        String servicePathToDelete = "/_system/governance/" + serviceString;
        resourceAdminServiceClient.deleteResource(servicePathToDelete);
        SchemaBean schema = listMetadataServiceClient.listSchemas();
        String schemaPathToDelete = "/_system/governance/" + schema.getPath()[0];
        resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        WSDLBean wsdl = listMetadataServiceClient.listWSDLs();
        String wsdlPathToDelete = "/_system/governance/" + wsdl.getPath()[0];
        resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }

}
