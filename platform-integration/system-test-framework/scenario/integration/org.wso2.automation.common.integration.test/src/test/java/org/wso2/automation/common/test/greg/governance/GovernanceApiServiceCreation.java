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

package org.wso2.automation.common.test.greg.governance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.governanceapi.GovApiUpdateservice;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class GovernanceApiServiceCreation {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(GovApiUpdateservice.class);
    String service_namespace = "http://example.com/demo/services";
    String service_name = "GovernanceAPITestService";
    String backEndUrl = null;
    String serviceUrl = null;
    Registry governanceRegistry;
    WSRegistryServiceClient registryWS;
    GovernanceArtifact artifact;
    ServiceManager serviceManager;
    Service service;
    WsdlManager wsdlMgr;
    Wsdl wsdl;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   MalformedURLException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(4);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getGreg().getBackEndUrl();
        int userId = new GregUserIDEvaluator().getTenantID();
        serviceUrl = environment.getGreg().getServiceUrl();
        sessionCookie = environment.getGreg().getSessionCookie();
        registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registryWS, UserListCsvReader.getUserInfo(4).getUserName());
        serviceManager = new ServiceManager(governanceRegistry);


        wsdlMgr = new WsdlManager(governanceRegistry);
    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException, GovernanceException {
        wsdl = wsdlMgr.newWsdl("http://ws.strikeiron.com/donotcall2_5?WSDL");
        wsdlMgr.addWsdl(wsdl);
        service = serviceManager.newService(new QName("http://my.service.ns1", "MyService"));

    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, priority = 1)
    public void testUpdateSevice() throws Exception, RemoteException {

        service.addAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);


        String service_namespace = "http://example.com/demo/services";
        String service_name = "ExampleService";
        String service_path = "/_system/governance/trunk/services/com/example/demo/services/ExampleService";

        Service service2 = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service2);

        assertTrue(registryWS.resourceExists(service_path), "Service Exists");


    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, priority = 2)
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

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, priority = 2)
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

    @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts() throws GovernanceException {
        serviceManager.removeService(service.getId());
    }


}