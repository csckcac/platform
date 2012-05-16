package org.wso2.automation.common.test.greg.multitenancy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserCreator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;


import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyUploadServiceTestClient {
    private static final Log log = LogFactory.getLog(PolicyUploadServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static WSRegistryServiceClient registry_testUser = null;
    private static WSRegistryServiceClient registry_diffDomainUser1 = null;
    private static Registry governance = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, RemoteException, UserAdminException, LoginAuthenticationExceptionException {
        int tenantId = 3;
        int diff_Domainuser = 6;
        int tenantID_testUser = 3;
        String userID = "testuser1";
        String userPassword = "test123";
        String roleName = "admin";

        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        registry_diffDomainUser1 = new RegistryProvider().getRegistry(diff_Domainuser, ProductConstant.GREG_SERVER_NAME);

        GregUserCreator GregUserCreator = new GregUserCreator();
        GregUserCreator.deleteUsers(tenantID_testUser, userID);
        GregUserCreator.addUser(tenantID_testUser, userID, userPassword, roleName);
        registry_testUser = GregUserCreator.getRegistry(tenantID_testUser, userID, userPassword);
        governance = new RegistryProvider().getGovernance(registry, tenantId);
        //delete policies
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario adding a policy ", priority = 1)
    private void testaddPolicy() throws org.wso2.carbon.registry.api.RegistryException {
        String policy_url = "https://wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/policy.xml";
        String policy_path = "/_system/governance/trunk/policies/policy.xml";
        String property1 = "1.0.0";
        String property2 = "Aaaa";
        String keyword1 = "UTOverTransport";
        String keyword2 = "Basic256";
        try {
            //Add Policy
            createPolicy(policy_url);
            //assert policy exists
            verifyResourceExists(policy_path);
            //Assert Properties
            propertyAssertion(policy_path, property1, property2);
            //Assert Schema content
            policyContentAssertion(policy_path, keyword1, keyword2);
            //delete resources
            removeResource();
            //assert resouces deleted propely
            verifyResourceDelete(policy_path);
            log.info("Multi Tenancy PolicyUploadServiceTestClient - Passed");
        } catch (RegistryException e) {
            log.error("verifyResourceExists Exception thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("verifyResourceExists Exception thrown:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("verifyResourceExists RegistryException thrown:" + e);
        }
    }


    public void propertyAssertion(String policy_path, String property1, String property2) throws RegistryException {
        Resource resource_adminUser = null;

        try {
            resource_adminUser = registry.get(policy_path);
            assertEquals(resource_adminUser.getProperty("version"), property1, "Policy Property - targetNamespace");
            assertEquals(resource_adminUser.getProperty("creator"), property2, "Policy Property - Creator");
        } catch (RegistryException e) {
            log.error("Property does not exsits:" + e);
            throw new RegistryException("Property does not exsits:" + e);
        }

        try {
            registry_testUser.get(policy_path);
            assertEquals(resource_adminUser.getProperty("version"), property1, "Policy Property - targetNamespace");
            assertEquals(resource_adminUser.getProperty("creator"), property2, "Policy Property - Creator");
        } catch (RegistryException e) {
            log.error("Property does not exsits:" + e);
            throw new RegistryException("Property does not exsits:" + e);
        }

        try {
            assertNotNull(registry_diffDomainUser1.get(policy_path), "Resource object shouldn't be null");
        } catch (RegistryException e) {
            log.info("Can not get registry resource by different tenant");
        }
    }


    public void createPolicy(String policy_url) throws GovernanceException {
        PolicyManager policyManager = new PolicyManager(governance);
        Policy policy;
        try {
            policy = policyManager.newPolicy(policy_url);
            policy.addAttribute("creator", "Aaaa");
            policy.addAttribute("version", "1.0.0");
            policyManager.addPolicy(policy);
            log.info("Policy was added successfully");
        } catch (GovernanceException e) {
            log.error("createPolicy Exception thrown:" + e);
            throw new GovernanceException("createPolicy Exception thrown:" + e);
        }
    }

    private void verifyResourceExists(String policy_path) throws RegistryException {
        try {
            assertTrue(registry.resourceExists(policy_path), "wsdl Exists :");                   //Assert admin user -admin123@wso2manualQA0006.org
            assertTrue(registry_testUser.resourceExists(policy_path), "wsdl exists:");           // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_diffDomainUser1.resourceExists(policy_path), "wsdl exists:");  // Assert differnt doamin user 1
        } catch (RegistryException e) {
            log.error("verifyResourceExists RegistryException thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e);
        }
    }

    public void policyContentAssertion(String policy_path, String keyword1, String keyword2) throws org.wso2.carbon.registry.api.RegistryException {
        String content_adminUser;
        String content_testUser;

        try {
            Resource r1;
            r1 = registry.get(policy_path);
            content_adminUser = new String((byte[]) r1.getContent());
            assertTrue(content_adminUser.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content_adminUser.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("schemaContentAssertion adminUser Registry Exception thrown:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("schemaContentAssertion adminUser Registry Exception thrown:" + e);
        }


        try {
            Resource r2;
            r2 = registry_testUser.get(policy_path);
            content_testUser = new String((byte[]) r2.getContent());
            assertTrue(content_testUser.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content_testUser.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("schemaContentAssertion testUser Registry Exception thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e);
        }

        try {
            assertNotNull(registry_diffDomainUser1.get(policy_path));

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.info("Cannot read the policy by different tenant");
        }
    }

    private void verifyResourceDelete(String policy_path) throws RegistryException {
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            assertFalse(registry.resourceExists(policy_path), "wsdl Exists :");
            // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_testUser.resourceExists(policy_path), "wsdl exists:");
            // Assert differnt doamin user 1
            assertFalse(registry_diffDomainUser1.resourceExists(policy_path), "wsdl exists:");
        } catch (RegistryException e) {
            log.error("verifyResourceExists Exception thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e);
        }
    }



    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
            if (registry_diffDomainUser1.resourceExists(resourceName)) {
                registry_diffDomainUser1.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/_system/governance/trunk/policies");       //delete policies
    }
}
