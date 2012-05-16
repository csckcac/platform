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
package org.wso2.automation.common.test.greg.multitenancy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregUserCreator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import java.io.*;
import java.rmi.RemoteException;


public class GarfileUploadServiceTestClient {
    private static final Log log = LogFactory.getLog(GarfileUploadServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static WSRegistryServiceClient registry_testUser = null;
    private static WSRegistryServiceClient registry_diffDomainUser1 = null;
    String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, LoginAuthenticationExceptionException, RemoteException, UserAdminException {
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

        removeResource();       //delete Resources
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
        //delete wsdls
        deleteResources("/_system/governance/trunk/wsdls");
        deleteResources("/_system/governance/trunk/wsdls");
        deleteResources("/_system/governance/trunk/wsdls");
        //delete services
        deleteResources("/_system/governance/trunk/services");
        deleteResources("/_system/governance/trunk/services");
        deleteResources("/_system/governance/trunk/services");
        //delete schemas
        deleteResources("/_system/governance/trunk/schemas");
        deleteResources("/_system/governance/trunk/schemas");
        deleteResources("/_system/governance/trunk/schemas");
        deleteResources("/a1");
    }

    private void uploadGarFile(String filePath) throws org.wso2.carbon.registry.api.RegistryException, FileNotFoundException {
        try {
            // Create Collection
            Collection collection = registry.newCollection();
            registry.put("/a1/a2/a3", collection);
            //Create Resource
            Resource r1 = registry.newResource();
            //create an Input Stream
            InputStream is = new BufferedInputStream(new FileInputStream(filePath));
            r1.setContentStream(is);
            r1.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r1", r1);
        } catch (FileNotFoundException e) {
            log.error("Registry Put GAR File upload -Failed:" + e);
            throw new FileNotFoundException("Registry Put GAR File upload -Failed:" + e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Registry Put GAR File upload -Failed:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Registry Put GAR File upload -Failed:" + e);
        }
    }

    private void verifyResourceExists(String artifact) throws RegistryException {
        try {
            //  admin user admin123@wso2manualQA0006.org
            assertTrue(registry.resourceExists(artifact), "WSDL does not exists for super user :");
            //  test user testuser123@wso2manualQA0006.org
            assertTrue(registry_testUser.resourceExists(artifact), "WSDL does not exists for test user :");
            // admin user - admin123@wso2autoQA0008.org
            assertFalse(registry_diffDomainUser1.resourceExists(artifact), "WSDL Exists for different domain user:");
        } catch (RegistryException e) {
            log.error("verifyResourceExists RegistryException thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException throw:" + e);
        }
    }

    private void verifyResourceDeleted(String artifact) throws RegistryException {
        try {
            //  admin user admin123@wso2manualQA0006.org
            assertFalse(registry.resourceExists(artifact), "WSDL has not been properly deleted for super user :");
            //  test user testuser123@wso2manualQA0006.org
            assertFalse(registry_testUser.resourceExists(artifact), "WSDL has not been properly deleted for test user :");
            // admin user - admin123@wso2autoQA0008.org
            assertFalse(registry_diffDomainUser1.resourceExists(artifact), "WSDL has not been properly deleted for different domain user:");
        } catch (RegistryException e) {
            log.error("Resource has not been properly deleted -Failed:" + e);
            throw new RegistryException("Resource has not been properly deleted -Failed:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario in axis2 service ", priority = 1)
    private void testAddAxis2ServiceGarFile() throws RegistryException, FileNotFoundException {
        String filePath = resourcePath + File.separator + "artifacts" + File.separator + "GREG" + File.separator + "Axis2Service.gar";
        String service_path = "/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service";
        String wsdl_path = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl";
        String schema_path = "/_system/governance/trunk/schemas/org/wso2/carbon/service/axis2serviceschema.xsd";

        //upload Gar file
        try {
            uploadGarFile(filePath);
            verifyResourceExists(wsdl_path);         //assert wsdl exists
            verifyResourceExists(service_path);      // assert services exists
            verifyResourceExists(schema_path);       // assert schema exists
            removeResource();                        //Delete resources
            verifyResourceDeleted(wsdl_path);        //assert wsdl was deleted
            verifyResourceDeleted(service_path);     //assert services was deleted
            verifyResourceDeleted(schema_path);      //assert schema was deleted
            log.info("Multi Tenancy GarfileUploadServiceTestClient - Passed ");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Resource has not been properly deleted -Failed:" + e);
            throw new RegistryException("Resource has not been properly deleted -Failed:" + e);
        } catch (FileNotFoundException e) {
            log.error("Resource has not been properly deleted -Failed:" + e);
            throw new FileNotFoundException("Resource has not been properly deleted -Failed:" + e);
        }
    }
}
