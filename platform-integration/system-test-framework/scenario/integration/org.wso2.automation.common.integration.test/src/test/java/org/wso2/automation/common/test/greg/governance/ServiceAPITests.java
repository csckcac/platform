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
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;

public class ServiceAPITests {

    String service_namespace = "http://example.com/demo/services";
    String service_name = "GovernanceAPITestService";
    public static ServiceManager serviceManager;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws MalformedURLException, RegistryException {
        GregRemoteRegistryProvider gregRemoteRegistryProvider = new GregRemoteRegistryProvider();
        serviceManager = new ServiceManager(gregRemoteRegistryProvider.getRegistry(0));
        cleanService();
    }

    //ToDo need remove specified services
    private void cleanService() throws GovernanceException {
        String[] serviceID = serviceManager.getAllServiceIds();
        for (String s : serviceID) {
            serviceManager.removeService(s);
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

    @Test(enabled = false,groups = {"wso2.governance.api"}, description = "Testing updateService", priority = 4)
    public void testUpdateService() throws GovernanceException {
        Service[] service = serviceManager.getAllServices();
        for (int serviceID = 0; serviceID <= service.length - 1; serviceID++) {
            if (service[serviceID].getQName().getLocalPart().equalsIgnoreCase(service_name)) {
                service[serviceID].activate();
                serviceManager.updateService(service[serviceID]);
            }
        }
        for (int serviceID = 0; serviceID <= service.length - 1; serviceID++) {
            if (service[serviceID].getQName().getLocalPart().equalsIgnoreCase(service_name)) {
                assertTrue(service[serviceID].isActive(), "Error occurred while executing updateService API method");
            }
        }
    }
}
