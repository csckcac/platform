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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Covers the public JIRA https://wso2.org/jira/browse/CARBON-12871 ,
 * https://wso2.org/jira/browse/REGISTRY-723
 */


public class GovernanceApiServiceListingTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiServiceListingTestCase.class);
    private ServiceManager serviceManager;
    private Service service;
    private Wsdl wsdl;
    private Registry governanceRegistry;


    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"})
    public void addService() throws InterruptedException, RemoteException,
                                    MalformedURLException, RegistryException {
        governanceRegistry = TestUtils.getRegistry();
        TestUtils.cleanupResources(governanceRegistry);
        serviceManager = new ServiceManager(governanceRegistry);
        WsdlManager wsdlMgr = new WsdlManager(governanceRegistry);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, priority = 1)
    public void testAddServices() throws GovernanceException {
        service = serviceManager.newService(new QName("http://my.service.ns1", "MyService1"));
        serviceManager.addService(service);
        for (int serviceNo = 0; serviceNo <= 9; serviceNo++) {
            Service service2 = serviceManager.newService(new QName("http://my.service.ns" + serviceNo, "MyService" + serviceNo));
            serviceManager.addService(service2);
            log.info("Added service" + service2.getPath());
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, priority = 2)
    public void testListServices() throws Exception {

        service.addAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);

        Service[] serviceList = serviceManager.getAllServices();
        for (int index = 1; index <= serviceList.length - 1; index++) {
            String serviceName = serviceList[index].getQName().toString();
            if (serviceName.contains("MyService")) {
                if (serviceList[index - 1].getQName().toString().contains("MyService")) {
                    String qnameprevious = serviceList[index - 1].getQName().toString();
                    String qnamenext = serviceList[index].getQName().toString();
                    if (qnameprevious.length() >= qnameprevious.indexOf("MyService") + 9 && qnamenext.length() >= qnamenext.indexOf("MyService") + 9) {
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


    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiServiceListing"}, priority = 3)
    public void testCheckVersioning() throws Exception {
        service.addAttribute("Owner", "Financial Department");
        serviceManager.updateService(service);
        serviceManager.getService(service.getId()).createVersion();
    }


    @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts() throws RegistryException {
        for (Service serviceList : serviceManager.getAllServices()) {
            if (serviceList.getQName().toString().contains("MyService")) {
                serviceManager.removeService(serviceList.getId());
            }
        }
        TestUtils.cleanupResources(governanceRegistry);
    }


}
