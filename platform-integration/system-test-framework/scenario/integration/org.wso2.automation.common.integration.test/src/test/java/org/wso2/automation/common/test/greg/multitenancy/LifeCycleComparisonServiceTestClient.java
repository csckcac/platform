package org.wso2.automation.common.test.greg.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserCreator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import java.rmi.RemoteException;

import static org.testng.Assert.*;


public class LifeCycleComparisonServiceTestClient {
    private static final Log log = LogFactory.getLog(LifeCycleComparisonServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static WSRegistryServiceClient registry_diffDomainUser1 = null;
    private static Registry governance_admin1 = null;
    private static Registry governance_admin2 = null;
    private static final String StateProperty_admin1 = "registry.lifecycle.ServiceLifeCycle.state";
    private static final String StateProperty_admin2 = "registry.lifecycle.ServiceLifeCycle.state";


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, RemoteException, UserAdminException,
                              LoginAuthenticationExceptionException {
        int tenantId = 3;
        int diff_Domainuser = 2;
        int tenantID_testUser = 3;
        String userID = "testuser1";
        String userPassword = "test123";
        String roleName = "admin";

        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        registry_diffDomainUser1 = new RegistryProvider().getRegistry(diff_Domainuser, ProductConstant.GREG_SERVER_NAME);

        GregUserCreator GregUserCreator = new GregUserCreator();
        GregUserCreator.deleteUsers(tenantID_testUser, userID);
        GregUserCreator.addUser(tenantID_testUser, userID, userPassword, roleName);

        governance_admin1 = new RegistryProvider().getGovernance(registry, tenantId);
        governance_admin2 = new RegistryProvider().getGovernance(registry_diffDomainUser1, tenantId);
        removeResource();      //delete artifacts
    }

    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario promoting & demoting life cycles ", priority = 1)
    private void testchecklifeCycleComparison() throws RegistryException {
        String wsdl_url_admin1 = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
        String wsdl_url_admin2 = "http://geocoder.us/dist/eg/clients/GeoCoder.wsdl";
        String wsdl_path_admin1 = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";
        String wsdl_path_admin2 = "/_system/governance/trunk/wsdls/us/geocoder/rpc/geo/coder/us/GeoCoder.wsdl";

        try {
            addWSDL(governance_admin1, wsdl_url_admin1);
            addWSDL(governance_admin2, wsdl_url_admin2);                       //add wsdl - admin2
            verifyResourceExists(wsdl_path_admin1, wsdl_path_admin2);            //assert resource exists:
            promoteLifeCycle(wsdl_path_admin1, wsdl_path_admin2);                // assert promote life
            demoteLifeCycle(wsdl_path_admin1, wsdl_path_admin2);                 // assert demote life
            removeResource();                                                    //delete life cycle
            verifyResourceDeleted(wsdl_path_admin1, wsdl_path_admin2);           //assert resource deleted successfully
            log.info("***************Multi Tenancy Life Cycle Comparison Service Test Client - Passed***************");
        } catch (GovernanceException e) {
            log.error("Multi Tenancy Life Cycle Comparison Service Test Client - Failed:" + e.getMessage());
            throw new GovernanceException("Multi Tenancy Life Cycle Comparison Service Test Client - Failed :" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Multi Tenancy Life Cycle Comparison Service Test Client - Failed:" + e.getMessage());
            throw new RegistryException("Multi Tenancy Life Cycle Comparison Service Test Client - Failed :" + e.getMessage());
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
            log.error("deleteResources RegistryException thrown:" + e.getMessage());
            throw new RegistryException("deleteResources RegistryException thrown:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/_system/governance/trunk/wsdls");       //delete wsdls
        deleteResources("/_system/governance/trunk/services");     //delete services
    }

    public void addWSDL(Registry governance, String wsdl_url) throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdl.addAttribute("creator2", "it is me");
            wsdl.addAttribute("version2", "0.01");
            wsdlManager.addWsdl(wsdl);
            log.info("Add wsdl");
        } catch (GovernanceException e) {
            log.error("Failed to add WSDL:" + e.getMessage());
            throw new GovernanceException("Failed to add WSDL:" + e.getMessage());
        }
    }

    private void verifyResourceExists(String wsdl_path_admin1, String wsdl_path_admin2) throws RegistryException {
        try {
            assertTrue(registry.resourceExists(wsdl_path_admin1), "admin1 resource exist:");
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path_admin1), "admin2 resource exist:");
            assertFalse(registry.resourceExists(wsdl_path_admin2), "admin1 resource exist:");
            assertTrue(registry_diffDomainUser1.resourceExists(wsdl_path_admin2), "admin2 resource exist:");
        } catch (RegistryException e) {
            log.error("Resource does not Exists -Failed:" + e.getMessage());
            throw new RegistryException("Resource does not Exists -Failed:" + e.getMessage());
        }
    }


    private void verifyResourceDeleted(String wsdl_path_admin1, String wsdl_path_admin2) throws RegistryException {
        try {
            assertFalse(registry.resourceExists(wsdl_path_admin1), "admin1 resource exist:");
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path_admin1), "admin2 resource exist:");
            assertFalse(registry.resourceExists(wsdl_path_admin2), "admin1 resource exist:");
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path_admin2), "admin2 resource exist:");
        } catch (RegistryException e) {
            log.error("Resource has not been properly deleted : " + e.getMessage());
            throw new RegistryException("Resource has not been properly deleted :" + e.getMessage());
        }
    }

    private void promoteLifeCycle(String wsdl_path_admin1, String wsdl_path_admin2) throws RegistryException {
        String admin1_state = null;
        String admin2_state = null;

        try {
            registry.associateAspect(wsdl_path_admin1, "ServiceLifeCycle");
            registry_diffDomainUser1.associateAspect(wsdl_path_admin2, "ServiceLifeCycle");

            // assert admin 1 & admin 2 life cycle states
            assertEquals(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "Domain user1 & Domain user2 are in different states:");

//                  //promote two steps - admin 1 life cycle
            registry.invokeAspect(wsdl_path_admin1, "ServiceLifeCycle", "Promote");
            registry.invokeAspect(wsdl_path_admin1, "ServiceLifeCycle", "Promote");

            //promote one step -- admin 2 life cycle
            registry_diffDomainUser1.invokeAspect(wsdl_path_admin2, "ServiceLifeCycle", "Promote");

            // assert admin 1 & admin 2 life cycle states are different
            assertNotSame(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "Domain user1 & Domain user2 are in same states :");

            //promote from anothe two steps - admin 2 life cycle
            registry_diffDomainUser1.invokeAspect(wsdl_path_admin2, "ServiceLifeCycle", "Promote");

            // assert admin 1 & admin 2 life cycle states are the same
            assertEquals(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "Domain user1 & Domain user2 are not in production states:");
        } catch (RegistryException e) {
            log.error("promoteLifeCycle RegistryExceptio thrown:" + e.getMessage());
            throw new RegistryException("Resource has not been properly deleted :" + e.getMessage());
        }


        try {
            // assert admin 2 life cycle state is visible from admin 1
            admin1_state = registry.get(wsdl_path_admin2).getProperty(StateProperty_admin2);
        } catch (RegistryException e) {

            log.info("promoteLifeCycle RegistryExceptio Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert life cycle does not appear to admin1:
            assertNull(admin1_state);
            log.info("admin 1 is unable to view admin2 life cycle state");
        }

        try {
            // assert admin 2 life cycle state is visible from admin 1
            admin2_state = registry_diffDomainUser1.get(wsdl_path_admin1).getProperty(StateProperty_admin1);
        } catch (RegistryException e) {
            log.info("promoteLifeCycle RegistryExceptio Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert life cycle does not appear to admin1:
            assertNull(admin2_state);
            log.info("admin 2 is unable to view admin1 life cycle state");
        }
    }

    private void demoteLifeCycle(String wsdl_path_admin1, String wsdl_path_admin2) throws RegistryException {
        String admin1_state = null;
        String admin2_state = null;

        try {
            registry.associateAspect(wsdl_path_admin1, "ServiceLifeCycle");
            registry_diffDomainUser1.associateAspect(wsdl_path_admin2, "ServiceLifeCycle");

            // assert admin 1 & admin 2 life cycle states
            assertEquals(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "After Demoting service Life cycle User1 & user 2 are in different states:");
//            //demote two steps - admin 1 life cycle
            registry.invokeAspect(wsdl_path_admin1, "ServiceLifeCycle", "Demote");
            registry.invokeAspect(wsdl_path_admin1, "ServiceLifeCycle", "Demote");
            //demote one step -- admin 2 life cycle
            registry_diffDomainUser1.invokeAspect(wsdl_path_admin2, "ServiceLifeCycle", "Demote");
            // assert admin 1 & admin 2 life cycle states are different
            assertNotSame(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "After Demoting service Life cycle User1 & user 2 are in same states:");
            //demote from anothe two steps - admin 2 life cycle
            registry_diffDomainUser1.invokeAspect(wsdl_path_admin2, "ServiceLifeCycle", "Demote");
//            registry_diffDomainUser1.invokeAspect(wsdl_path_admin2, "ServiceLifeCycle", "Demote");
            // assert admin 1 & admin 2 life cycle states are the same
            assertEquals(registry.get(wsdl_path_admin1).getProperty(StateProperty_admin1), registry_diffDomainUser1.get(wsdl_path_admin2).getProperty(StateProperty_admin2), "After Demoting service Lifecycle user1 & user2 are not in development stage:");
        } catch (RegistryException e) {
            log.error("promoteLifeCycle RegistryExceptio thrown:" + e.getMessage());
            throw new RegistryException("Resource has not been properly deleted :" + e.getMessage());
        }

        try {
            // assert admin 2 life cycle state is visible from admin 1
            admin1_state = registry.get(wsdl_path_admin2).getProperty(StateProperty_admin2);
        } catch (RegistryException e) {
            log.info("promoteLifeCycle RegistryExceptio Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert life cycle does not appear to admin1:
            assertNull(admin1_state);
            log.info("Demote - admin 1 is unable to view admin2 life cycle state");
        }

        try {
            // assert admin 2 life cycle state is visible from admin 1
            admin2_state = registry_diffDomainUser1.get(wsdl_path_admin1).getProperty(StateProperty_admin1);

        } catch (RegistryException e) {
            log.info("promoteLifeCycle RegistryExceptio Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert life cycle does not appear to admin1:
            assertNull(admin2_state);
            log.info("Demote -admin 2 is unable to view admin1 life cycle state");
        }
    }


}
