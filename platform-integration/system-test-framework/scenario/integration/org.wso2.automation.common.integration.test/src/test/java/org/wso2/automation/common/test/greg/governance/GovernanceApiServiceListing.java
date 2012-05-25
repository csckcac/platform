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
/**
 * Covers the public JIRA https://wso2.org/jira/browse/CARBON-12871 , https://wso2.org/jira/browse/REGISTRY-723
 */


public class GovernanceApiServiceListing {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(GovernanceApiServiceCreation.class);
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
    private Service[] serviceList;


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

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"})
    public void addsService() throws InterruptedException, RemoteException,
                                     MalformedURLException, GovernanceException {
        service = serviceManager.newService(new QName("http://my.service.ns1", "MyService1"));
        serviceManager.addService(service);
        Service service2 = serviceManager.newService(new QName("http://my.service.ns2", "MyService2"));
        Service service4 = serviceManager.newService(new QName("http://my.service.ns4", "MyService4"));
        Service service3 = serviceManager.newService(new QName("http://my.service.ns3", "MyService3"));

        Service service5 = serviceManager.newService(new QName("http://my.service.ns5", "MyService5"));
        Service service6 = serviceManager.newService(new QName("http://my.service.ns6", "MyService6"));

        Service service8 = serviceManager.newService(new QName("http://my.service.ns8", "MyService8"));
        Service service7 = serviceManager.newService(new QName("http://my.service.ns7", "MyService7"));
        Service service9 = serviceManager.newService(new QName("http://my.service.ns9", "MyService9"));
        Service service1 = serviceManager.newService(new QName("http://my.service.ns0", "MyService0"));
        serviceManager.addService(service1);
        serviceManager.addService(service2);
        serviceManager.addService(service7);
        serviceManager.addService(service4);
        serviceManager.addService(service5);
        serviceManager.addService(service6);
        serviceManager.addService(service8);
        serviceManager.addService(service3);
        serviceManager.addService(service9);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, priority = 1)
    public void testListServices() throws Exception, RemoteException {

        service.addAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);

        serviceList = serviceManager.getAllServices();
        for (int index = 1; index <= serviceList.length - 1; index++) {
            String serviceName = serviceList[index].getQName().toString();
            if (serviceName.contains("MyService")) {
                if (serviceList[index - 1].getQName().toString().contains("MyService")) {
                    String qnameprevious = serviceList[index - 1].getQName().toString();
                    String qnamenext = serviceList[index].getQName().toString();
                    if (qnameprevious.length() >= qnameprevious.indexOf("MyService") + 9 && qnamenext.length() >= qnamenext.indexOf("MyService") +9) {
                        int previousindex = Integer.valueOf(qnameprevious.substring(qnameprevious.indexOf("MyService") + 9));
                        int nextIndexindex = Integer.valueOf(qnamenext.substring(qnamenext.indexOf("MyService") + 9));
                        if (previousindex >= nextIndexindex) {
                            Assert.fail("Sorting is not applied");
                        }

                    }
                }
            }

        }


    }


    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, priority = 2)
    public void testCheckVersioning() throws Exception, RemoteException {

        service.addAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);
        serviceManager.getService(service.getId()).createVersion();
    }


    @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts() throws GovernanceException {
        for (Service serviceList : serviceManager.getAllServices()) {
            if (serviceList.getQName().toString().contains("MyService")) {
                serviceManager.removeService(serviceList.getId());
            }
        }

    }


}
