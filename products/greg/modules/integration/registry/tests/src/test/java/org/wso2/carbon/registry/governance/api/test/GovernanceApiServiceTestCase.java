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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * covers https://wso2.org/jira/browse/CARBON-11573
 * https://wso2.org/jira/browse/CARBON-11256
 */

public class GovernanceApiServiceTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiServiceTestCase.class);
    private WSRegistryServiceClient registryWS;
    private ServiceManager serviceManager;
    private Service service;
    private WsdlManager wsdlMgr;
    private Wsdl wsdl;


    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException, RegistryException {
        Registry governanceRegistry = TestUtils.getRegistry();
        registryWS = TestUtils.getWSRegistry();
        TestUtils.cleanupResources(governanceRegistry);
        serviceManager = new ServiceManager(governanceRegistry);
        wsdlMgr = new WsdlManager(governanceRegistry);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreate"})
    public void testUpdateService() throws Exception {

        wsdl = wsdlMgr.newWsdl("http://ws.strikeiron.com/donotcall2_5?WSDL");
        wsdlMgr.addWsdl(wsdl);
        service = serviceManager.newService(new QName("http://my.service.ns1", "MyService"));

        service.setAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);


        String service_namespace = "http://example.com/demo/services";
        String service_name = "ExampleService";
        String service_path = "/_system/governance/trunk/services/com/example/demo/services/ExampleService";

        Service service2 = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service2);

        assertTrue(registryWS.resourceExists(service_path), "Service Exists");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, dependsOnMethods = "testUpdateService")
    public void testAddWsdlToService() throws Exception {

        service.attachWSDL(wsdl);
        serviceManager.updateService(service);
        serviceManager.addService(service);
        service.getId();
        boolean wsdlfound = false;
        for (Service gregService : serviceManager.getAllServices()) {
            if (gregService.getId().equals(service.getId())) {
                for (Wsdl serviceWsdl : gregService.getAttachedWsdls()) {
                    if (serviceWsdl.getId().equals(wsdl.getId())) {
                        log.info("Wsdl is attached to the service");
                        wsdlfound = true;
                    }
                }
            }
        }
        Assert.assertTrue(wsdlfound, "Wsdl is not listed in Registry");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, dependsOnMethods = "testAddWsdlToService")
    public void testRemoveWsdlFromService() throws Exception {
        service.detachWSDL(wsdl.getId());
        serviceManager.updateService(service);
        boolean wsdlfound = true;
        for (Service gregService : serviceManager.getAllServices()) {
            if (gregService.getId().equals(service.getId())) {
                for (Wsdl serviceWsdl : gregService.getAttachedWsdls()) {
                    if (serviceWsdl.getId().equals(wsdl.getId())) {
                        log.info("Wsdl is attached to the service");
                        wsdlfound = false;
                    }
                }
            }
        }
        Assert.assertFalse(wsdlfound, "Wsdl is not listed in Registry");
    }

    @AfterClass(alwaysRun = true, groups = {"wso2.greg"})
    public void removeArtifacts() throws GovernanceException {
        serviceManager.removeService(service.getId());
    }


}