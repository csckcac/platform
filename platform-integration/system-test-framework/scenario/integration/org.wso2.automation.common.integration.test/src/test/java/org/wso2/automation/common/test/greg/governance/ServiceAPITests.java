package org.wso2.automation.common.test.greg.governance;
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.xmlbeans.impl.piccolo.xml.XMLStreamReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.common.GovernanceArtifactFilter;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import java.io.StringReader;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ServiceAPITests {

    String service_namespace = "http://example.com/demo/services";
    String service_name = "GovernanceAPIAutomatedTestService";
    public static ServiceManager serviceManager;
    private Service referenceService;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        serviceManager = new ServiceManager(governance);
        cleanService();
    }

    //ToDo need remove specified services
    private void cleanService() throws GovernanceException {
        Service[] service = serviceManager.getAllServices();
        for (int i = 0; i <= service.length - 1; i++) {
            if (service[i].getQName().getLocalPart().contains(service_name)) {
                System.out.println(service[i].getId());
                serviceManager.removeService(service[i].getId());
            }
        }
    }

    @Test(groups = {"wso2.governance.api"}, description = "Testing addService API method", priority = 1)
    public void testAddService() throws GovernanceException {
        boolean isServiceFound = false;
        Service service = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service);
        Service[] availableServices = serviceManager.getAllServices();
        for (int i = 0; i <= availableServices.length - 1; i++) {
            if (availableServices[i].getQName().getLocalPart().equalsIgnoreCase(service_name)) {
                isServiceFound = true;
            }
        }
        assertTrue(isServiceFound, "Error occured while adding new service from governance API : " +
                "please check newService/addService and getAllService methods");
    }

    @Test(groups = {"wso2.governance.api"}, description = "Testing getAllServicePath API method", priority = 2)
    public void testGetAllServicePaths() throws GovernanceException {
        boolean isServicePathFound = false;
        String[] servicePath = serviceManager.getAllServicePaths();
        for (String s : servicePath) {
            if (s.contains("/services/" + service_name)) {
                isServicePathFound = true;
            }
        }
        assertTrue(isServicePathFound, "Error occurred in GetAllServicePath method");
    }


    @Test(enabled = false, groups = {"wso2.governance.api"}, description = "Testing addNewService with inline service content", priority = 3)
    public void testNewServiceWithOMElement() throws XMLStreamException, GovernanceException {
        boolean isServiceFound = false;
        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">\n" +
                "    <overview>\n" +
                "        <name>GovernanceAPITestService_Inline</name>\n" +
                "        <namespace>http://example.com/demo/services</namespace>\n" +
                "        <axis2ns14:version xmlns:axis2ns14=\"http://www.wso2.org/governance/metadata\">\n" +
                "            1.0.0-SNAPSHOT\n" +
                "        </axis2ns14:version>\n" +
                "    </overview>\n" +
                "</serviceMetaData>";
        OMElement contentElement = AXIOMUtil.stringToOM(content);
        Service service = serviceManager.newService(contentElement);
        serviceManager.addService(service);

        Service[] availableServices = serviceManager.getAllServices();
        for (int i = 0; i <= availableServices.length - 1; i++) {
            if (availableServices[i].getQName().getLocalPart().equalsIgnoreCase("GovernanceAPITestService_Inline")) {
                isServiceFound = true;
            }
        }
        assertTrue(isServiceFound, "Error occured while adding a service with inline service content");
    }


    //https://wso2.org/jira/browse/CARBON-13194
    @Test(groups = {"wso2.governance.api"}, description = "Testing updateService", priority = 4)
    public void testUpdateService() throws GovernanceException {
        Service[] service = serviceManager.getAllServices();
        for (int serviceID = 0; serviceID <= service.length - 1; serviceID++) {
            if (service[serviceID].getQName().getLocalPart().equalsIgnoreCase(service_name)) {
                service[serviceID].setQName(new QName(service_namespace, "GovernanceAPIAutomatedTestService2"));
                serviceManager.updateService(service[serviceID]);
            }
        }
        service = serviceManager.getAllServices();
        for (int serviceID = 0; serviceID <= service.length - 1; serviceID++) {
            System.out.println(service[serviceID].getQName().getLocalPart());
            if (service[serviceID].getQName().getLocalPart().equalsIgnoreCase(service_name)) {
                assertFalse(service[serviceID].getQName().getLocalPart().equalsIgnoreCase(service_name),
                        "Old service QName not updated.");
            }
        }
    }

    @Test(groups = {"wso2.governance.api"}, description = "Testing getService", priority = 5)
    public void testGetService() throws GovernanceException {
        Service service;
        boolean isServiceFound = false;
        String[] serviceId = serviceManager.getAllServiceIds();
        for (String s : serviceId) {
             service = serviceManager.getService(s);
            if(service.getQName().getLocalPart().contains(service_name)){
                isServiceFound = true;
            }
        }
        assertTrue(isServiceFound,"getService governance API method does not work.");
    }

//    @Test(groups = {"wso2.governance.api"}, description = "Testing removeService", priority = 6)
//    public void testFindService() throws GovernanceException, XMLStreamException {
//
//        javax.xml.stream.XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(service_name));
//        StAXOMBuilder builder = new StAXOMBuilder(reader);
//        OMElement referenceServiceElement = builder.getDocumentElement();
//        referenceService = serviceManager.newService(referenceServiceElement);
//
//        //ListServiceFilter listServiceFilter = new ListServiceFilter(referenceService);
//        ServiceFilter listServiceFilter = new ServiceFilter() {
//            GovernanceArtifactFilter filter = new GovernanceArtifactFilter(referenceService);
//            public boolean matches(Service service) throws GovernanceException {
//                return filter.matches(service);
//            }
//        };
//
//        serviceManager.findServices(listServiceFilter) ;
//    }


    @Test(groups = {"wso2.governance.api"}, description = "Testing removeService", priority = 7)
    public void testRemoveService() throws GovernanceException {
        Service[] service = serviceManager.getAllServices();
        for (int i = 0; i <= service.length - 1; i++) {
            if (service[i].getQName().getLocalPart().contains(service_name)) {
                serviceManager.removeService(service[i].getId());
            }
        }
        service = serviceManager.getAllServices();
        for (int i = 0; i <= service.length - 1; i++) {
            if (service[i].getQName().getLocalPart().contains(service_name)) {
                Assert.fail("removeService API method does not work");
            }
        }
    }
}
