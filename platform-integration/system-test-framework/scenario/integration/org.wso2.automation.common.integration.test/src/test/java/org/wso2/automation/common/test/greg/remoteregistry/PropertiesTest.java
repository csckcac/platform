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

package org.wso2.automation.common.test.greg.remoteregistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.util.List;

public class PropertiesTest {
    private static final Log log = LogFactory.getLog(PropertiesTest.class);
    public RemoteRegistry registry;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, MalformedURLException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();

    }

    @Test(groups = {"wso2.greg"}, description = "test adding a property to root", priority = 1)
    public void testRootLevelPropertiesTest() throws RegistryException {
        Resource root;
        try {
            root = registry.get("/");
            root.addProperty("p1", "v1");
            registry.put("/", root);
            Resource rootb = registry.get("/");
            assertEquals(rootb.getProperty("p1"), "v1", "Root should have a property named p1 with value v1");
            Resource r1e1 = registry.get("/");
            r1e1.removeProperty("p1");
            log.info("**************Registry API Root Level Properties Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Root Level Properties Test -Failed:" + e);
            throw new RegistryException("Registry API Root Level Properties Test -Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a single property to resource", priority = 2)
    public void testSingleValuedPropertiesTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newResource();
            r2.setContent("Some content for r2");
            r2.addProperty("p1", "p1v1");
            registry.put("/propTest/r2", r2);
            Resource r2b = registry.get("/propTest/r2");
            String p1Value = r2b.getProperty("p1");
            assertEquals(p1Value, "p1v1", "Property p1 of /propTest/r2 should contain the value p1v1");
            deleteResources("/propTest");
            log.info("**************Registry API Single Valued Properties Test  - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Single Valued Properties Test -Failed :" + e);
            throw new RegistryException("Registry API Single Valued Properties Test -Failed:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test adding a single property to resource", priority = 3)
    public void testMultiValuedPropertiesTest() throws RegistryException {
        String path = "/propTest/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setContent("Some content for r1");
            r1.addProperty("p1", "p1v1");
            r1.addProperty("p1", "p1v2");
            registry.put(path, r1);

            Resource r1b = registry.get(path);
            List propValues = r1b.getPropertyValues("p1");

            assertTrue(propValues.contains("p1v1"), "Property p1 of /propTest/r1 should contain the value p1v1");
            assertTrue(propValues.contains("p1v2"), "Property p1 of /propTest/r1 should contain the value p1v2");
            deleteResources("/propTest");
            log.info("***************Registry API Multi Valued Properties Test - Passed************************");
        } catch (RegistryException e) {
            log.error("Registry API Multi Valued Properties Test_Failed :" + e);
            throw new RegistryException("Registry API Multi Valued Properties Test -Failed:" + e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test adding a null property to resource", priority = 4)
    public void testNullValuedPropertiesTest() throws RegistryException {
        String path = "/propTest3/r2";
        Resource r2;
        try {
            r2 = registry.newResource();
            r2.setContent("Some content for r2");
            r2.addProperty("p1", null);
            registry.put(path, r2);
            Resource r2b = registry.get(path);
            String p1Value = r2b.getProperty("p1");
            assertEquals(p1Value, null, "Property p1 of /propTest3/r2 should contain the value null");
            deleteResources("/propTest3");
            log.info("***************Registry API Null Valued Properties Test - Passed******************");
        } catch (RegistryException e) {
            log.error("Registry API Null Valued Properties Test - Failed :" + e);
            throw new RegistryException("Registry API Null Valued Properties Test - Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a null multivalued property to resource", priority = 5)
    public void testNullMultiValuedPropertiesTest() throws RegistryException {
        String path = "/propTest4/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setContent("Some content for r1");
            r1.addProperty("p1", null);
            r1.addProperty("p1", null);
            registry.put(path, r1);
            Resource r1b = registry.get(path);
            List propValues = r1b.getPropertyValues("p1");
            String value = "";
            try {
                value = (String) propValues.get(0);
            } catch (NullPointerException e) {
                assertTrue(true, "Property p1 of /propTest4/r1 should contain the value null");
            }
            deleteResources("/propTest4");
            log.info("***************Registry API Null Multi Valued Properties Test - Passed****************");
        } catch (RegistryException e) {
            log.error("Registry API Null Multi Valued Properties Test - Failed :" + e);
            throw new RegistryException("Registry API Null Multi Valued Properties Test - Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test removing property from a resource", priority = 6)
    public void testRemovingPropertiesTest() throws RegistryException {
        String path = "/props/t1/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
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
            log.info("***************Registry API Removing Properties Test - Passed*****************");
        } catch (RegistryException e) {
            log.error("Registry API Removing Properties Test-Failed :" + e);
            throw new RegistryException("Registry API Removing Properties Test - Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test removing property from a resource", priority = 7)
    public void testRemovingMultivaluedPropertiesTest() throws RegistryException {
        String path = "/props/t2/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setContent("r1 content");
            r1.addProperty("p1", "v1");
            r1.addProperty("p1", "v2");
            registry.put(path, r1);
            Resource r1e1 = registry.get("/props/t2/r1");
            r1e1.setContent("r1 content updated");
            r1e1.removePropertyValue("p1", "v1");
            registry.put(path, r1e1);
            Resource r1e2 = registry.get(path);
            assertFalse(r1e2.getPropertyValues("p1").contains("v1"), "Property is not removed.");
            assertTrue(r1e2.getPropertyValues("p1").contains("v2"), "Wrong property is removed.");
            deleteResources("/props");
            log.info("***************Registry API Removing Multivalued Properties Test - Passed ***********************");
        } catch (RegistryException e) {
            log.error("Registry API Removing Multivalued Properties Test-Failed :" + e);
            throw new RegistryException("Registry API Removing Multivalued Properties Test - Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test edit multi valued property from a resource", priority = 8)
    public void testEditingMultivaluedPropertiesTest() throws RegistryException {
        String path = "/props";
        Resource r1;
        try {
            r1 = registry.newResource();
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
            log.info("***************Registry API Editing Multivalued Properties Test - Passed *******************");
        } catch (RegistryException e) {
            log.error("Registry API Editing Multivalued Properties Test -Failed :" + e);
            throw new RegistryException("Registry API Editing Multivalued Properties Test - Failed :" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/propTest");
        deleteResources("/propTest3");
        deleteResources("/propTest4");
        deleteResources("/props");

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
