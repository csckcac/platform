package org.wso2.automation.common.test.greg.governance;/*
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

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import static org.testng.Assert.assertTrue;

/**
 * Class will test all API methods of WSDL manager
 */
public class WSDLManagerAPITest {

    public static WsdlManager wsdlManager;
    private static Wsdl wsdlObj;
    private static Wsdl[] wsdlArray;
    private String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private String wsdlName = "donotcall2_5.wsdl";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        wsdlManager = new WsdlManager(governance);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newWsdl API method", priority = 1)
    public void testNewWsdl() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewWsdl"}, description = "Testing " +
            "addWsdl API method", priority = 2)
    public void testAddWsdl() throws GovernanceException {
        try {
            wsdlManager.addWsdl(wsdlObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:addWsdl method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
            "getAllWsdls API method", priority = 3)
    public void testGetAllWsdl() throws GovernanceException {
        boolean isWsdlFound = false;
        try {
            wsdlArray = wsdlManager.getAllWsdls();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing EndpointManager:addWsdl method" + e);
        }
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                isWsdlFound = true;
            }
        }
        assertTrue(isWsdlFound, "Return object of getAllWsdls" +
                " method doesn't have all information ");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
            "getWsdl API method", priority = 4)
    public void testGetWsdl() throws GovernanceException {
        Wsdl localWsdlObj = null;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                try {
                    localWsdlObj = wsdlManager.getWsdl(w.getId());

                } catch (GovernanceException e) {
                    throw new GovernanceException("Error occurred while executing EndpointManager:getWsdl method" + e);
                }
            }
        }
        if (localWsdlObj != null) {
            assertTrue(localWsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName), "getWsdl method doesn't work");
        }
    }

}
