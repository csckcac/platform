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

import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Test case for governance registry adding wsdl to service
 * https://wso2.org/jira/browse/CARBON-11997
 * https://wso2.org/jira/browse/CARBON-11012
 */
public class GovernanceApiWsdlTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiWsdlTestCase.class);
    Registry governanceRegistry;
    WSRegistryServiceClient registryWS;

    ServiceManager serviceManager;
    Service service;
    WsdlManager wsdlMgr;
    Wsdl wsdl;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   MalformedURLException {
        governanceRegistry = TestUtils.getRegistry();
        TestUtils.cleanupResources(governanceRegistry);
        wsdlMgr = new WsdlManager(governanceRegistry);
    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException, GovernanceException {
        wsdl = wsdlMgr.newWsdl("http://ws.strikeiron.com/donotcall2_5?WSDL");
        wsdlMgr.addWsdl(wsdl);
       // service = serviceManager.newService(new QName("http://my.service.ns1", "MyService"));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"}, description = "Invike combine URL Bpel")
    public void testAddWsdl() throws Exception, RemoteException {

        boolean wsdlExists = false;
        for (Wsdl wsdlList : wsdlMgr.getAllWsdls()) {
            if (wsdlList.getId().equals(wsdl.getId())) {
                wsdlExists = true;
                log.info(wsdl.getPath() + "--exists");
            }
        }
        Assert.assertTrue(wsdlExists, "Wsdl is not listed in Registry");
    }

    @AfterClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"})
    public void removeArtifacts() throws GovernanceException {
        wsdlMgr.removeWsdl(wsdl.getId());
    }
}
