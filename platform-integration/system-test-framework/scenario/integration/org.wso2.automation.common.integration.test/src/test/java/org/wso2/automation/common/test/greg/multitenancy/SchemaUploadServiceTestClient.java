package org.wso2.automation.common.test.greg.multitenancy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.api.Resource;
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

public class SchemaUploadServiceTestClient {
    private static final Log log = LogFactory.getLog(SchemaUploadServiceTestClient.class);
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

        deleteSchema();       //Delete Schemas
    }

    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario adding a schema ", priority = 1)
    public void testaddSchema() throws RegistryException {
        String schema_url = "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/Patient.xsd";

        String schema_path = "/_system/governance/trunk/schemas/org/ihc/xsd/Patient.xsd";
        String property1 = "Valid";
        String property2 = "http://ihc.org/xsd?patient";
        String property3 = "Aaaa";
        String keyword1 = "attributeFormDefault";
        String keyword2 = "languageCommunication";

        try {
            createSchema(governance, schema_url);                                     //Add Schema
            verifyResourceExists(schema_path);                                        //assert resource exists
            propertyAssertion(schema_path, property1, property2, property3);          //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);                  //Assert Schema content
            deleteSchema();                                                           //delete schemasa
            verifyResourceDelete(schema_path);                                        //assert resources have been proprly deleted
            log.info("***************Multi Tenancy Schema Upload Service Test Client - Passed*****************");
        } catch (RegistryException e) {
            log.error("Multi Tenancy Schema Upload Service Test Client - Failed:" + e);
            throw new RegistryException("Multi Tenancy Schema Upload Service Test Client - Failed:" + e);
        }
    }


    private void deleteSchema() throws RegistryException {
        try {
            registry.delete("/_system/governance/trunk/schemas");
            registry_diffDomainUser1.delete("/_system/governance/trunk/schemas");
        } catch (RegistryException e) {
            log.error("Schema Delete functionality -Failed:" + e);
            throw new RegistryException("Schema Delete Functionality -Failed:" + e);
        }
    }

    public void createSchema(Registry governance, String schema_url) throws RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        Schema schema;
        try {
            schema = schemaManager.newSchema(schema_url);
            schema.addAttribute("creator", "Aaaa");
            schema.addAttribute("version", "1.0.0");
            schemaManager.addSchema(schema);
            log.info("Schema was added successfully");
        } catch (GovernanceException e) {
            log.error("Unable to create Schema:" + e);
            throw new RegistryException("Unable to Create Schema:" + e);
        }
    }

    public void propertyAssertion(String schema_path, String property1, String property2, String property3) throws RegistryException {
        Resource resource_adminUser;
        Resource resource_testUser;
        Resource resource_diffDomainUser = null;
        try {
            resource_adminUser = registry.get(schema_path);
            assertEquals(resource_adminUser.getProperty("Schema Validation"), property1, "Schema Property - Schema Validation");
            assertEquals(resource_adminUser.getProperty("targetNamespace"), property2, "Schema Property - targetNamespace");
            assertEquals(resource_adminUser.getProperty("creator"), property3, "Schema Property - Creator");
        } catch (RegistryException e) {
            log.error("propertyAssertion adminUser Exception thrown:" + e);
            throw new RegistryException("propertyAssertion adminUser Exception thrown:" + e);
        }

        try {
            resource_testUser = registry_testUser.get(schema_path);
            assertEquals(resource_testUser.getProperty("Schema Validation"), property1, "Schema Property - Schema Validation");
            assertEquals(resource_testUser.getProperty("targetNamespace"), property2, "Schema Property - targetNamespace");
            assertEquals(resource_testUser.getProperty("creator"), property3, "Schema Property - Creator");
        } catch (RegistryException e) {
            log.error("propertyAssertion testUser Exception thrown:" + e);
            throw new RegistryException("propertyAssertion testUser Exception thrown:" + e);
        }

        try {
            resource_diffDomainUser = registry_diffDomainUser1.get(schema_path);
        } catch (RegistryException e) {
            log.info("propertyAssertion diffDomainUser Exception thrown:" + e);
            //registry null exception is caught to assert resource does not exists:
            assertNull(resource_diffDomainUser);
        }
    }

    private void verifyResourceExists(String schema_path) throws RegistryException {
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            assertTrue(registry.resourceExists(schema_path), "schema doesn't exists:");
            // Assert Test user - testuser1@wso2manualQA0006.org
            assertTrue(registry_testUser.resourceExists(schema_path), "schema doesn't exists:");
            // Assert differnt doamin user 1
            assertFalse(registry_diffDomainUser1.resourceExists(schema_path), "schema exists:");
        } catch (RegistryException e) {
            log.error("verifyResourceExists Exception thrown:" + e);
            throw new RegistryException("verifyResourceExists Exception thrown:" + e);
        }
    }

    private void verifyResourceDelete(String schema_path) throws RegistryException {
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            assertFalse(registry.resourceExists(schema_path), "wsdl Exists :");
            // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_testUser.resourceExists(schema_path), "wsdl exists:");
            // Assert differnt doamin user 1
            assertFalse(registry_diffDomainUser1.resourceExists(schema_path), "wsdl exists:");
        } catch (RegistryException e) {
            log.error("verifyResourceExists Exception thrown:" + e);
            throw new RegistryException("verifyResourceExists Exception thrown:" + e);
        }
    }


    public void schemaContentAssertion(String schema_path, String keyword1, String keyword2) throws RegistryException {
        String content_adminUser = null;
        String content_testUser;
        String content_diffDomainUser = null;

        try {
            Resource r1;
            r1 = registry.get(schema_path);
            content_adminUser = new String((byte[]) r1.getContent());
            assertTrue(content_adminUser.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content_adminUser.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("schemaContentAssertion adminUser Registry Exception thrown:" + e);
            throw new RegistryException("schemaContentAssertion adminUser Registry Exception thrown:" + e);
        }

        try {
            Resource r2;
            r2 = registry_testUser.get(schema_path);
            content_testUser = new String((byte[]) r2.getContent());
            assertTrue(content_testUser.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content_testUser.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("schemaContentAssertion testUser Registry Exception thrown:" + e);
            throw new RegistryException("schemaContentAssertion testUser Registry Exception thrown:" + e);
        }

        try {
            Resource r3;
            r3 = registry_diffDomainUser1.get(schema_path);
            content_diffDomainUser = new String((byte[]) r3.getContent());
            assertTrue(content_adminUser.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content_adminUser.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.info("propertyAssertion diffDomainUser Exception thrown:" + e);
            //registry null exception is caught to assert resource does not exists:
            assertNull(content_diffDomainUser);
        }
    }


}
