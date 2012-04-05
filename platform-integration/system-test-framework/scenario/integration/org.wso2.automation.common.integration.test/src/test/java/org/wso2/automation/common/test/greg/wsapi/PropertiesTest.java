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
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;


import java.util.List;

public class PropertiesTest {
    private static final Log log = LogFactory.getLog(PropertiesTest.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "add properties to root level", priority = 1)
    private void testRootLevelProperties() throws RegistryException {
        Resource root;
        try {
            root = registry.get("/");
            root.addProperty("p1", "v1");
            registry.put("/", root);

            Resource rootb = registry.get("/");
            assertEquals(rootb.getProperty("p1"), "v1", "Root should have a property named p1 with value v1");
            log.info("************WS-API root Level Properties test -Passed*************");
        } catch (RegistryException e) {
            log.error("WS-API rootLevelProperties fail :" + e.getMessage());
            throw new RegistryException("WS-API rootLevelProperties fail :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add single value properties", priority = 2)
    private void testSingleValuedProperties() throws RegistryException {
        String path = "/propTest/r2";
        Resource r2 = registry.newResource();
        try {
            r2.setContent("Some content for r2");

            r2.addProperty("p1", "p1v1");
            registry.put(path, r2);

            Resource r2b = registry.get(path);
            String p1Value = r2b.getProperty("p1");
            assertEquals(p1Value, "p1v1", "Property p1 of /propTest/r2 should contain the value p1v1");
            deleteResources("/propTest");
            log.info("**********WS-API Single Valued Properties test - Passed**********");
        } catch (RegistryException e) {
            log.error("WS-API Single Valued Properties test fail:" + e.getMessage());
            throw new RegistryException("WS-API Single Valued Properties test fail :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add multiple value properties", priority = 3)
    private void testMultiValuedProperties() throws RegistryException {
        String path = "/propTest/r1";
        Resource r1 = registry.newResource();

        try {
            r1.setContent("Some content for r1");

            r1.addProperty("p1", "p1v1");
            r1.addProperty("p1", "p1v2");
            registry.put(path, r1);

            Resource r1b = registry.get(path);
            List propValues = r1b.getPropertyValues("p1");
            assertTrue(propValues.contains("p1v1"), "Property p1 of /propTest/r1 should contain the value p1v1");
            assertTrue(propValues.contains("p1v2"), "Property p1 of /propTest/r1 should contain the value p1v2");
            deleteResources("/propTest");
            log.info("************WS-API Multi Valued Properties test- Passed ***********");
        } catch (RegistryException e) {
            log.error("WS-API Multi Valued Properties test fail :" + e.getMessage());
            throw new RegistryException("WS-API Multi Valued Properties test fail :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Remove properties", priority = 4)
    private void testRemovingProperties() throws RegistryException {
        String path = "/props/t1/r1";
        Resource r1 = registry.newResource();
        try {
            r1.setContent("r1 content");
            r1.setProperty("p1", "v1");
            r1.setProperty("p2", "v2");
            registry.put(path, r1);

            Resource r1e1 = registry.get(path);
            r1e1.setContent("r1 content");
            r1e1.removeProperty("p1");
            registry.put(path, r1e1);

            Resource r1e2 = registry.get(path);
            assertEquals(r1e2.getProperty("p1"), null, "Property is not removed.");
            assertNotNull(r1e2.getProperty("p2"), "Wrong property is removed.");
            deleteResources("/props");
            log.info("***********WS-API Removing Properties - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Removing Properties fail :" + e.getMessage());
            throw new RegistryException("WS-API Removing Properties fail :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Remove Multiple Value Properties", priority = 5)
    private void testRemovingMultivaluedProperties() throws RegistryException {
        String path = "/props/t2/r1";
        Resource r1 = registry.newResource();
        try {
            r1.setContent("r1 content");
            r1.addProperty("p1", "v1");
            r1.addProperty("p1", "v2");
            registry.put(path, r1);

            Resource r1e1 = registry.get(path);
            r1e1.setContent("r1 content updated");
            r1e1.removePropertyValue("p1", "v1");
            registry.put(path, r1e1);

            Resource r1e2 = registry.get(path);
            assertFalse(r1e2.getPropertyValues("p1").contains("v1"), "Property is not removed.");
            assertTrue(r1e2.getPropertyValues("p1").contains("v2"), "Wrong property is removed.");
            deleteResources("/props");
            log.info("***********WS-API Removing Multivalued Properties test - Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API Removing Multivalued Properties test -fail :" + e.getMessage());
            throw new RegistryException("WS-API Removing Multivalued Properties test- fail :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Edit Multiple Value Properties", priority = 6)
    private void testEditingMultivaluedProperties() throws RegistryException {
        String path = "/props/t3/r1";
        Resource r1 = registry.newResource();
        try {
            r1.setContent("r1 content");
            r1.addProperty("p1", "v1");
            r1.addProperty("p1", "v2");
            r1.setProperty("test", "value2");
            r1.setProperty("test2", "value2");
            registry.put(path, r1);

            Resource r1e1 = registry.get(path);
            r1e1.setContent("r1 content");
            r1e1.editPropertyValue("p1", "v1", "v3");
            registry.put(path, r1e1);
            Resource r1e2 = registry.get(path);
            assertFalse(r1e2.getPropertyValues("p1").contains("v1"), "Property is not edited.");
            assertTrue(r1e2.getPropertyValues("p1").contains("v3"), "Property is not edited.");
            assertTrue(r1e2.getPropertyValues("p1").contains("v2"), "Wrong property is removed.");
            deleteResources("/props");
            log.info("*************WS-API Editing Multivalued Properties test - Passed************");
        } catch (RegistryException e) {
            log.error("WS-API Editing Multivalued Properties test- fail:" + e.getMessage());
            throw new RegistryException("WS-API Editing Multivalued Properties test- fail :" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/propTest");
        deleteResources("/props");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e.getMessage());
            throw new RegistryException("deleteResources RegistryException thrown:" + e.getMessage());
        }

    }

}
