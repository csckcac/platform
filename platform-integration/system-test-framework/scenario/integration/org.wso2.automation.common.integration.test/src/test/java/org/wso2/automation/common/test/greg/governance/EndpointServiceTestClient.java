/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.automation.common.test.greg.governance;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;


public class EndpointServiceTestClient {
    private static final Log log = LogFactory.getLog(EndpointServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    private static WsdlManager wsdlManager;
    private static EndpointManager endpointManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test adding an Endpoint to G-Reg", priority = 1)
    private void testAddEndpoint() throws RegistryException {
        String endpoint_url = "http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistry";
        String endpoint_path = "/_system/governance/trunk/endpoints/com/strikeiron/ws/strikeiron/donotcall2_5/ep-DoNotCallRegistry";
        String property1 = "QA";
        String property2 = "Dev";

        //Create Endpoint
        createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(endpoint_path), "Endpoint Resource Does not exists :");

        propertyAssertion(endpoint_path, property1, property2);
        deleteResources(endpoint_path);
        log.info("EndpointServiceTestClient -testAddEndpoint() Passed");

    }

    @Test(groups = {"wso2.greg"}, description = "test adding an WSDL with Endpoints to G-Reg", priority = 2)
    private void testAddWsdlWithEndpoints() throws Exception {
        String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
        String endpoint_path = "http://people.wso2.com:9763/services/BizService";

        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdlManager.addWsdl(wsdl);
            log.info("EndpointServiceTestClient - WSDL was successfully added");
            Endpoint[] endpoints = testVerifyEndpoints(endpoint_path, wsdl);
            endpointManager = removeEndpoint(endpoints[0]);
            GovernanceArtifact[] artifacts = wsdl.getDependents();
            wsdlManager.removeWsdl(wsdl.getId());// delete the WSDL
            removeServices(artifacts);
            endpointManager.removeEndpoint(endpoints[0].getId());// now try to remove the endpoint
            deleteResources(endpoint_path);
            log.info("EndpointServiceTestClient testAddWsdlWithEndpoints()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add WSDL:" + e);
            throw new RegistryException("Failed to add WSDL:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "adding a service with Endpoints to G-Reg", priority = 3)
    private void testAddServiceWithEndpoints() throws GovernanceException, RegistryException {
        String service_namespace = "http://wso2.com/test/examples";
        String service_name = "myService";
        String service_path = "/_system/governance/trunk/services/com/wso2/test/examples/myService";
        String endpoint_path1 = "/_system/governance/trunk/endpoints/ep-endpoint1";
        String endpoint_path2 = "/_system/governance/trunk/endpoints/ep-endpoint2";

        ServiceManager serviceManager = new ServiceManager(governance);
        try {
            Service service = serviceManager.newService(new QName(service_namespace, service_name));
            service.addAttribute("endpoints_entry", ":http://endpoint1");
            service.addAttribute("endpoints_entry", "QA:http://endpoint2");
            serviceManager.addService(service);

            Endpoint[] endpoints = service.getAttachedEndpoints();
            assertEquals(2, endpoints.length, "Endpoint length does not match:");
            assertEquals(endpoints[0].getUrl(), "http://endpoint1", "Endpoint URL element 0 does not match :");
            assertEquals(endpoints[0].getAttributeKeys().length, 0, "Endpoint element 0 service does not exists:");
            assertEquals(endpoints[1].getUrl(), "http://endpoint2", "Endpoint URL element 1 does not exists:");
            assertEquals(endpoints[1].getAttributeKeys().length, 1, "Endpoint element 1 service does not exists:");

            deleteResources(service_path);
            deleteResources(endpoint_path1);
            deleteResources(endpoint_path2);
            log.info("EndpointServiceTestClient testAddServiceWithEndpoints()- Passed");
        } catch (GovernanceException e) {
            log.error("testAddServiceWithEndpoints GovernanceException Exception thrown:" + e);
            throw new GovernanceException("testAddServiceWithEndpoints-Governance Registry Exception thrown:" + e);
        } catch (RegistryException e) {
            log.error("testAddServiceWithEndpoints RegistryException Exception thrown:" + e);
            throw new RegistryException("testAddServiceWithEndpoints-Registry Exception thrown:" + e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Attach an Endpoint to a service ", priority = 4)
    private void testAttachEndpointsToService() throws RegistryException {
        String service_namespace = "http://wso2.com/test234/xxxxx";
        String service_name = "myServicxcde";
        String service_path = "/_system/governance/trunk/services/com/wso2/test234/xxxxx/myServicxcde";
        String endpoint_path1 = "/_system/governance/trunk/endpoints/ep-endpoint1xx";
        String endpoint_path2 = "/_system/governance/trunk/endpoints/ep-endpoint2xx";

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        try {
            service = serviceManager.newService(new QName(service_namespace, service_name));
            serviceManager.addService(service);

            EndpointManager endpointManager = new EndpointManager(governance);
            Endpoint ep1 = endpointManager.newEndpoint("http://endpoint1xx");
            endpointManager.addEndpoint(ep1);

            Endpoint ep2 = endpointManager.newEndpoint("http://endpoint2xx");
            endpointManager.addEndpoint(ep2);

            service.attachEndpoint(ep1);
            service.attachEndpoint(ep2);

            Endpoint[] endpoints = service.getAttachedEndpoints();
            assertEquals(2, endpoints.length);
            assertEquals("http://endpoint1xx", endpoints[0].getUrl());
            assertEquals("http://endpoint2xx", endpoints[1].getUrl());

            //Detach Endpoint one
            service.detachEndpoint(ep1.getId());
            endpoints = service.getAttachedEndpoints();
            assertEquals(1, endpoints.length);
            deleteResources(service_path);
            deleteResources(endpoint_path1);
            deleteResources(endpoint_path2);
            log.info("EndpointServiceTestClient testAttachEndpointsToService()- Passed");
        } catch (GovernanceException e) {
            log.error("testAttachEndpointsToService GovernanceException Exception thrown:" + e);
            throw new RegistryException("testAttachEndpointsToService-Registry Exception thrown:" + e);
        }
    }

    private void removeServices(GovernanceArtifact[] artifacts) throws GovernanceException {
        ServiceManager serviceManager = new ServiceManager(governance);

        for (GovernanceArtifact artifact : artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service) artifact;
                serviceManager.removeService(service2.getId());
            }
        }
    }

    private EndpointManager removeEndpoint(Endpoint endpoint) throws Exception {
        try {
            endpointManager.removeEndpoint(endpoint.getId());
            assertTrue(registry.resourceExists(endpoint.getPath()), "EPR exists in the registry");
        } catch (Exception ignored) {
            log.info("Can't remove Endpoint yet because of service & wsdl Exists");
        }
        return endpointManager;
    }

    private Endpoint[] testVerifyEndpoints(String endpoint_path, Wsdl wsdl) throws GovernanceException {
        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        assertEquals(1, endpoints.length, "Endpoint length does not match :");
        assertEquals(endpoints[0].getUrl(), endpoint_path, "Endpoint Path does not exsits");
        assertEquals(endpoints[0].getAttributeKeys().length, 1, "Endpoint Element 0 does not exists:");
        return endpoints;
    }


    private void removeResource() throws RegistryException {
        deleteResources("/_system/governance/trunk/services");
        deleteResources("/_system/governance/trunk/wsdls");
        deleteResources("/_system/governance/trunk/endpoints");
    }

    private void deleteResources(String resourceName) throws RegistryException {
        if (registry.resourceExists(resourceName)) {
            registry.delete(resourceName);
        }
    }

    private void createEndpoint(String endpoint_url) throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(registry);
        Endpoint endpoint1;
        try {
            endpoint1 = endpointManager.newEndpoint(endpoint_url);
            endpoint1.addAttribute("status1", "QA");
            endpoint1.addAttribute("status2", "Dev");
            endpointManager.addEndpoint(endpoint1);
            log.info("Endpoint was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable add Endpoint:" + e);
            throw new GovernanceException("Unable to add Endpoint:" + e);
        }
    }

    private void propertyAssertion(String endpoint_path, String property1, String property2) throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(endpoint_path);
            assertEquals(resource.getProperty("status1"), property1, "Endpoint Property - Status1 does not Exists :");
            assertEquals(resource.getProperty("status2"), property2, "Endpoint Property - Status2 does not Exists :");
        } catch (RegistryException e) {
            log.error("propertyAssertion Exception thrown:" + e);
            throw new RegistryException("propertyAssertion-Registry Exception thrown:" + e);
        }
    }

}
