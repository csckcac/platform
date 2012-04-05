/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.automation.common.test.greg.governance;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class SchemaImportServiceTestClient {
    private static final Log log = LogFactory.getLog(SchemaImportServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    private static Registry remote_registry = null;
    String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault, MalformedURLException {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);
        String registryURL;

        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String username = userDetails.getUserName();
        String password = userDetails.getPassword();
//
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(ProductConstant.
                                                                                         GREG_SERVER_NAME);
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryURL = "https://" + properties.getProductVariables().getHostName() + "/t/" +
                          userDetails.getDomain() + "/registry/";
        } else {
            if (properties.getProductVariables().getWebContextRoot() != null) {
                registryURL = "https://" + properties.getProductVariables().getHostName() + ":" +
                              properties.getProductVariables().getHttpsPort() + "/" +
                              properties.getProductVariables().getWebContextRoot() +
                              "/registry/";
                log.info("Web Context Root has been defined :" +
                         properties.getProductVariables().getWebContextRoot());
            } else {
                registryURL = "https://" + properties.getProductVariables().getHostName() + ":" +
                              properties.getProductVariables().getHttpsPort() + "/registry/";
            }
        }
        log.info("Registry URL is :" + registryURL);

        try {
            remote_registry = new RemoteRegistry(new URL(registryURL), username, password);
        } catch (RegistryException e) {
            log.error("Failed to create Remote Registry:" + e.getMessage());
            throw new RegistryException("Failed to create Remote Registry:" + e.getMessage());
        } catch (MalformedURLException e) {
            log.error("Failed to create Remote Registry:" + e.getMessage());
            throw new MalformedURLException("Failed to create Remote Registry:" + e.getMessage());
        }

        deleteSchema();   //  Delete Schemas already existing
    }

    //ToDo test will be enabled after Jira  https://wso2.org/jira/browse/REGISTRY-693 is fixed
    @Test(groups = {"wso2.greg"}, enabled = false, description = "upload Patient Schema sample", priority = 1)
    public void testAddGeoIPServiceSchema() throws RegistryException {
        String schema_url = "http://www.restfulwebservices.net/wcf/GeoIPService.svc?xsd=xsd0";
        String schema_path1 = "/_system/governance/trunk/schemas/net/restfulwebservices/www/" +
                              "datacontracts/_2008/_01/GeoIPService.svc.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/net/restfulwebservices/www/" +
                              "servicecontracts/_2008/_01/GeoIPService1.xsd";
        String schema1_property1 = "http://www.restfulwebservices.net/DataContracts/2008/01";
        String property3 = "Aaaa";
        String schema2_property1 = "Valid";
        String schema2_property2 = "http://www.restfulwebservices.net/ServiceContracts/2008/01";
        String keyword1 = "Registry";
        String keyword2 = "CountryCode";

        try {
            createSchema(governance, schema_url);                                                 //Add Schema
            assertTrue(registry.resourceExists(schema_path1), "GeoIPService.svc.xsd Not Present "); //Assert Schema exist
            assertTrue(registry.resourceExists(schema_path2), "GeoIPService1c.xsd Not Present ");
            propertyAssertion(schema_path1, "", schema1_property1, property3);                    //Assert Properties
            propertyAssertion(schema_path2, schema2_property1, schema2_property2, property3);
            schemaContentAssertion(schema_path1, keyword1, keyword2);                              //Assert Schema content
            registry.delete(schema_path1);                                                         //Remove Schema
            registry.delete(schema_path2);
            assertFalse(registry.resourceExists(schema_path1), "Schema exists at " + schema_path1);  //Assert Resource was deleted successfully
            assertFalse(registry.resourceExists(schema_path2), "Schema exists at " + schema_path2);
            log.info("SchemaImportServiceTestClient testAddPatientSchema()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Patient Schema  :" + e.getMessage());
            throw new RegistryException("Failed to add Patient Schema  :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "upload Book Schema sample", priority = 1)
    public void testAddBookSchema() throws RegistryException {
        String schema_url = "http://people.wso2.com/~evanthika/schemas/books.xsd";
        String schema_path = "/_system/governance/trunk/schemas/books/books.xsd";
        String property1 = "Valid";
        String property2 = "urn:books";
        String property3 = "Aaaa";
        String keyword1 = "bks:BookForm";
        String keyword2 = "author";

        try {
            createSchema(governance, schema_url);                              //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Book Schema exist ");  //Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            registry.delete(schema_path);                                      //Remove Schema
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddBookSchema()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Book Schema  :" + e.getMessage());
            throw new RegistryException("Failed to add Book Schema  :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "upload Listing3 Schema sample", priority = 2)
    public void testAddListing3Schema() throws RegistryException {
        String schema_url = "http://people.wso2.com/~evanthika/schemas/listing3.xsd";
        String schema_path = "/_system/governance/trunk/schemas/listing3/listing3.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/listing4/listing4.xsd";
        String property1 = "Valid";
        String property2 = "urn:listing3";
        String property3 = "Aaaa";
        String keyword1 = "areaCode";
        String keyword2 = "exchange";

        try {

            createSchema(governance, schema_url);                         //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Listing3 Schema exist ");//Assert Schema exist
            assertTrue(registry.resourceExists(schema_path2), "Listing4 Schema exist ");
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            Association[] associations = registry.getAllAssociations(schema_path2); //Assert Association
            assertTrue(associations[1].getDestinationPath().equalsIgnoreCase(schema_path), "Association Exsists");
            schemaContentAssertion(schema_path, keyword1, keyword2);              //Assert Schema content
            registry.delete(schema_path);                                         //Remove Registry
            registry.delete(schema_path2);
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddListing3Schema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Listing3 Schema:" + e.getMessage());
            throw new GovernanceException("Failed to add Listing3 Schema:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Failed to add Listing3 Schema:" + e.getMessage());
            throw new RegistryException("Failed to add Listing3 Schema:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Listing4 Schema sample", priority = 3)
    public void testAddListing4Schema() throws RegistryException {
        String schema_url = "http://people.wso2.com/~evanthika/schemas/listing4.xsd";
        String schema_path = "/_system/governance/trunk/schemas/listing4/listing4.xsd";
        String property1 = "Valid";
        String property2 = "urn:listing4";
        String property3 = "Aaaa";
        String keyword1 = "areaCode2";
        String keyword2 = "exchange2";

        try {
            createSchema(governance, schema_url);                            //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Listing4 Schema exist "); //Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            registry.delete(schema_path);                                      //Remove Registry
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddListing4Schema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Listing4 Schema:" + e.getMessage());
            throw new GovernanceException("Failed to add Listing4 Schema:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Failed to add Listing4 Schema:" + e.getMessage());
            throw new RegistryException("Failed to add Listing4 Schema:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Listing4 Schema sample", priority = 4)
    public void testAddPurchasingSchema() throws RegistryException {
        String schema_url = "http://people.wso2.com/~evanthika/schemas/purchasing.xsd";
        String schema_path = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        String property1 = "Valid";
        String property2 = "http://bar.org/purchasing";
        String property3 = "Aaaa";
        String keyword1 = "productQueryResult";
        String keyword2 = "invalidProductId";

        try {
            createSchema(governance, schema_url);                                 //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Purchasing Schema exist ");//Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);      //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);              //Assert Schema content
            registry.delete(schema_path);                                          //Remove Registry
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddPurchasingSchema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Purchasing Schema:" + e.getMessage());
            throw new GovernanceException("Failed to add Purchasing Schema:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Failed to add Purchasing Schema:" + e.getMessage());
            throw new RegistryException("Failed to add Purchasing Schema:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Schema from File", priority = 5)
    public void testAddSchemafromFile() throws RegistryException {
        String filePath = resourcePath + File.separator + "artifacts" + File.separator + "GREG" +
                          File.separator + "Person.xsd";
        String toPath = "/_system/governance/trunk/schemas/";
        String schema_path = "/_system/governance/trunk/schemas/Person.xsd";
        String keyword1 = "Name";
        String keyword2 = "SSN";
        File file = new File(filePath);

        try {
            RegistryClientUtils.importToRegistry(file, toPath, remote_registry);//Upload Schema from file
            assertTrue(remote_registry.resourceExists(schema_path), "Person Schema exist ");//Assert Resource Exists
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            remote_registry.delete(schema_path);                               //Delete Schema
            assertFalse(remote_registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddSchemafromFile()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Purchasing Schema from File:" + e.getMessage());
            throw new RegistryException("Failed to add Purchasing Schema from File:" + e.getMessage());
        }
    }

    public void createSchema(Registry governance, String schema_url) throws GovernanceException {
        SchemaManager schemaManager = new SchemaManager(governance);
        Schema schema = null;
        try {
            schema = schemaManager.newSchema(schema_url);
            schema.addAttribute("creator", "Aaaa");
            schema.addAttribute("version", "1.0.0");
            schemaManager.addSchema(schema);
            log.info("Schema was added successfully");
        } catch (GovernanceException e) {
            log.error("Failed to create Schema:" + e.getMessage());
            throw new GovernanceException("Failed to create Schema:" + e.getMessage());
        }
    }

    public void deleteSchema() throws RegistryException {
        try {
            if (registry.resourceExists("/_system/governance/trunk/schemas")) {
                registry.delete("/_system/governance/trunk/schemas");
            }
        } catch (RegistryException e) {
            log.error("Failed to Delete Schemas :" + e.getMessage());
            throw new RegistryException("Failed to Delete Schemas :" + e.getMessage());
        }
    }


    public void propertyAssertion(String schema_path, String property1, String property2,
                                  String property3) throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(schema_path);
            assertEquals(resource.getProperty("Schema Validation"), property1, "Schema Property - Schema Validation");
            assertEquals(resource.getProperty("targetNamespace"), property2, "Schema Property - targetNamespace");
            assertEquals(resource.getProperty("creator"), property3, "Schema Property - Creator");
        } catch (RegistryException e) {
            log.error("Failed to assert Schema Properties:" + e.getMessage());
            throw new RegistryException("Failed to assert Schema Properties:" + e.getMessage());
        }
    }

    public void schemaContentAssertion(String schema_path, String keyword1, String keyword2)
            throws RegistryException {
        String content;
        try {
            Resource r1 = registry.newResource();
            r1 = registry.get(schema_path);
            content = new String((byte[]) r1.getContent());
            assertTrue(content.indexOf(keyword1) > 0, "Assert Content Schema file - key word 1");
            assertTrue(content.indexOf(keyword2) > 0, "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to assert Schema content :" + e.getMessage());
            throw new RegistryException("Failed to assert Schema content :" + e.getMessage());
        }
    }

}
