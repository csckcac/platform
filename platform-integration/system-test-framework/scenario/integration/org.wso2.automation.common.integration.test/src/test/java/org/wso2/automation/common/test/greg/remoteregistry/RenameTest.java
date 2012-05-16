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
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class RenameTest {
    private static final Log log = LogFactory.getLog(RenameTest.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test rename a resource at root level ", priority = 1)
    public void testRootLevelResourceRenameTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "rename");
            r1.setContent("some text");
            registry.put("/rename2", r1);
            registry.rename("/rename2", "/rename4");

            boolean failed = false;
            try {
                registry.get("/rename2");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Resource should not be accessible from the old path after renaming.");
            Resource newR1 = registry.get("/rename4");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/rename4");
            log.info("**************Registry API Root Level Resource Rename Test - Passed ***************");
        } catch (RegistryException e) {
            log.error("Registry API Root Level Resource Rename Test-Failed :" + e);
            throw new RegistryException("Registry API Root Level Resource Rename Test-Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test rename a resource at general level ", priority = 2)
    public void testGeneralResourceRenameTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "rename");
            r1.setContent("some text");
            registry.put("/tests/rename1", r1);
            registry.rename("/tests/rename1", "rename2");

            boolean failed = false;
            try {
                registry.get("/tests/rename1");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Resource should not be accessible from the old path after renaming.");
            Resource newR1 = registry.get("/tests/rename2");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/tests");
            log.info("*************Registry API General Resource Rename Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API General Resource Rename Test-Failed :" + e);
            throw new RegistryException("Registry API General Resource Rename Test-Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test rename a collection at root level ", priority = 3)
    public void testRootLevelCollectionRenameTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "rename");
            r1.setContent("some text");
            registry.put("/rename34k/c1/dummy", r1);
            registry.rename("/rename34k", "/rename44k");

            boolean failed = false;
            try {
                registry.get("/rename34k/c1/dummy");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Resource should not be " +
                    "accessible from the old path after renaming the parent.");
            Resource newR1 = registry.get("/rename44k/c1/dummy");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/rename44k");
            log.info("**************Registry API Root Level Collection Rename Test - Passed**************");
        } catch (RegistryException e) {
            log.error("Registry API Root Level Collection Rename Test - Failed :" + e);
            throw new RegistryException("Registry API Root Level Collection Rename Test-Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test rename a collection at general level ", priority = 4)
    public void testGeneralCollectionRenameTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "rename");
            r1.setContent("some text");
            registry.put("/c2/rename3/c1/dummy", r1);

            registry.rename("/c2/rename3", "rename4");

            boolean failed = false;
            try {
                registry.get("/c2/rename3/c1/dummy");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Resource should not be " +
                    "accessible from the old path after renaming the parent.");

            Resource newR1 = registry.get("/c2/rename4/c1/dummy");
            assertEquals(newR1.getProperty("test"), "rename", "Resource should contain a property with name test and value rename.");
            deleteResources("/c2");
            log.info("******************Registry API General Collection Rename Test - Passed*****************");
        } catch (RegistryException e) {
            log.error("Registry API -GeneralCollectionRenameTest RegistryException thrown :" + e);
            throw new RegistryException("Registry API Root Level Collection Rename Test-Failed:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/tests");
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
