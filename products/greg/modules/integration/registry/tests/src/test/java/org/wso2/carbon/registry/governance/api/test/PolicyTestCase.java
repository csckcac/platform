/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.governance.api.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class PolicyTestCase {

    private Registry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        registry = TestUtils.getRegistry();
        try {
            TestUtils.cleanupResources(registry);
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void testAddPolicy() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        Assert.assertEquals("it is me", newPolicy.getAttribute("creator"));
        Assert.assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        Assert.assertEquals(oldPolicyPath, "/trunk/policies/policy.xml");
        Assert.assertTrue(registry.resourceExists("/trunk/policies/policy.xml"));

        newPolicy.setName("my-policy.xml");
        policyManager.updatePolicy(newPolicy);

        Assert.assertEquals("/trunk/policies/my-policy.xml", newPolicy.getPath());
        Assert.assertFalse(registry.resourceExists("/trunk/policies/policy.xml"));

        // doing an update without changing anything.
        policyManager.updatePolicy(newPolicy);

        Assert.assertEquals("/trunk/policies/my-policy.xml", newPolicy.getPath());
        Assert.assertEquals("0.01", newPolicy.getAttribute("version"));

        newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals("it is me", newPolicy.getAttribute("creator"));
        Assert.assertEquals("0.01", newPolicy.getAttribute("version"));

        Policy[] policies = policyManager.findPolicies(new PolicyFilter() {
            public boolean matches(Policy policy) throws GovernanceException {
                if (policy.getAttribute("version").equals("0.01")) {
                    return true;
                }
                return false;
            }
        });
        Assert.assertEquals(1, policies.length);
        Assert.assertEquals(newPolicy.getId(), policies[0].getId());

        // deleting the policy
        policyManager.removePolicy(newPolicy.getId());
        Policy deletedPolicy = policyManager.getPolicy(newPolicy.getId());
        Assert.assertNull(deletedPolicy);
    }
}
