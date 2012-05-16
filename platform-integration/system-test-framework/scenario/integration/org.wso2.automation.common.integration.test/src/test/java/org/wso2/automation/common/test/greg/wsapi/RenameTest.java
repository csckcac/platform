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
package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

public class RenameTest {
    private static final Log log = LogFactory.getLog(RenameTest.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Rename a resource in root level", priority = 1)
    private void testRootLevelResourceRename() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");

        try {
            r1.setContent("some text");
            registry.put("/rename2", r1);
            registry.rename("/rename2", "/rename4");

            assertFalse(registry.resourceExists("/rename2"), "Resource should not be accessible from the old path after renaming.");

            Resource newR1 = registry.get("/rename4");

            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/rename4");
            log.info("*************WS-API Root Level Resource Rename test - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Root Level Resource Rename test -fail :" + e);
            throw new RegistryException("WS-API Root Level Resource Rename test- fail:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Resource renaming", priority = 2)
    private void testGeneralResourceRename() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");

        try {
            r1.setContent("some text");
            registry.put("/tests/rename1", r1);
            registry.rename("/tests/rename1", "rename2");
            assertFalse(registry.resourceExists("/test/rename1"), "Resource should not be accessible from the old path after renaming.");

            Resource newR1 = registry.get("/tests/rename2");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/tests");
            log.info("***************WS-API General Resource Rename test- Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API General Resource Rename test - Fail" + e);
            throw new RegistryException("WS-API General Resource Rename test- Fail" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Rename Collection in root level", priority = 3)
    private void testRootLevelCollectionRename() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");

        try {
            r1.setContent("some text");
            registry.put("/rename34k/c1/dummy", r1);
            registry.rename("/rename34k", "/rename44k");

            assertFalse(registry.resourceExists("/rename34k/c1/dummy"), "Resource should not be " +
                    "accessible from the old path after renaming the parent.");
            Resource newR1 = registry.get("/rename44k/c1/dummy");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/rename44k");
            log.info("************WS-API Root Level Collection Rename test - Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API Root Level Collection Rename test-fail :" + e);
            throw new RegistryException("WS-API Root Level Collection Rename test - Fail" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Rename a General Collection", priority = 4)
    private void testGeneralCollectionRename() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");

        try {
            r1.setContent("some text");
            registry.put("/c2/rename3/c1/dummy", r1);
            registry.rename("/c2/rename3", "rename4");

            assertFalse(registry.resourceExists("/c2/rename3/c1/dummy"), "Resource should not be " +
                    "accessible from the old path after renaming the parent.");
            Resource newR1 = registry.get("/c2/rename4/c1/dummy");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/c2");
            log.info("**************WS-API General Collection Rename test - Passed **********************");
        } catch (RegistryException e) {
            log.error("WS-API General Collection Rename test-fail :" + e);
            throw new RegistryException("WS-API General Collection Rename test-fail" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/rename2");
        deleteResources("/rename4");
        deleteResources("/tests");
        deleteResources("/rename34k");
        deleteResources("/rename44k");
        deleteResources("/c2");

    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }
}
