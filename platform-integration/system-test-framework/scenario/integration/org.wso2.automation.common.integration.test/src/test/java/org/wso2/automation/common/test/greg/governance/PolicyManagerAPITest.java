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
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.assertTrue;

public class PolicyManagerAPITest {
    public static PolicyManager policyManager;
    public static Policy policyObj;
    public static Policy[] policies;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.
                GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        policyManager = new PolicyManager(governance);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newPolicy API method", priority = 1)
    public void testNewPolicy() throws GovernanceException {
        try {
            policyObj = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                    "platform-integration/system-test-framework/core/org.wso2.automation.platform.core/" +
                    "src/main/resources/artifacts/GREG/policy/UTPolicy.xml");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:newPolicy method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewPolicy"}, description = "Testing " +
            "addPolicy API method", priority = 2)
    public void testAddPolicy() throws GovernanceException {
        try {
            cleanPolicies();
            policyManager.addPolicy(policyObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:addPolicy method" + e);
        }
    }


    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddPolicy"}, description = "Testing " +
            "getAllPolicies API method", priority = 3)
    public void testGetAllPolicies() throws GovernanceException {
        try {
            policies = policyManager.getAllPolicies();
            assertTrue(policies.length > 0, "Error occurred while executing PolicyManager:" +
                    "getAllPolicies method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:PolicyManager method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetAllPolicies"}, description = "Testing " +
            "getPolicy API method", priority = 4)
    public void testGetPolicy() throws GovernanceException {
        try {
            policyObj = policyManager.getPolicy(policies[0].getId());
            assertTrue(policyObj.getQName().getLocalPart().equalsIgnoreCase("UTPolicy.xml"), "PolicyManager:" +
                    "getPolicy API method not contain expected policy name");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:getPolicy method" + e);
        }
    }


    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetPolicy"}, description = "Testing " +
            "updatePolicy API method", priority = 5)
    public void testUpdatePolicy() throws GovernanceException {
        try {
            policyObj.setName("SamplePolicy");
            policyManager.updatePolicy(policyObj);
            Policy localPolicy = policyManager.getPolicy(policyObj.getId());
            assertTrue(localPolicy.getQName().getLocalPart().equalsIgnoreCase("SamplePolicy.xml"), "Updated policy doesn't " +
                    "have Updated Information.PolicyManager:updatePolicy didn't work");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:updatePolicy method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing " +
            "removePolicy API method", priority = 6)
    public void testRemovePolicy() throws GovernanceException {
        try {
            policyManager.removePolicy(policyObj.getId());
            policies = policyManager.getAllPolicies();
            for (Policy s : policies) {
                assertTrue(s.getId().equalsIgnoreCase(policyObj.getId()), "PolicyManager:removePolicy API method having error");
            }

        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:removePolicy method" + e);
        }
    }

    private void cleanPolicies() throws GovernanceException {
        policies = policyManager.getAllPolicies();
        for (int i = 0; i <= policies.length - 1; i++) {
            if (policies[i].getQName().getLocalPart().contains("UTPolicy.xml")) {
                policyManager.removePolicy(policies[i].getId());
            }
        }
    }

}
