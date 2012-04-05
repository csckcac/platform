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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class TestMove {
    private static final Log log = LogFactory.getLog(TestMove.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }


    @Test(groups = {"wso2.greg"}, description = "test move a resource from root ", priority = 1)
    public void testResourceMoveFromRootTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "move");
            r1.setContent("c");
            registry.put("/move1", r1);

            Collection c1 = registry.newCollection();
            registry.put("/test/move", c1);
            registry.move("/move1", "/test/move/move1");
            Resource newR1 = registry.get("/test/move/move1");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");

            boolean failed = false;
            try {
                registry.get("/move1");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("***************Registry API Resource Move From Root Test - Passed**************");
        } catch (RegistryException e) {
            log.error("Registry API Resource Move From Root Test - Failed:" + e.getMessage());
            throw new RegistryException("Registry API Resource Move From Root Test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test move a resource to root ", priority = 2)
    public void testResourceMoveToRoot() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "move");
            r1.setContent("c");
            registry.put("/test/move/move2", r1);
            registry.move("/test/move/move2", "/move2");

            Resource newR1 = registry.get("/move2");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");

            boolean failed = false;
            try {
                registry.get("/test/move/move2");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            deleteResources("/move2");
            log.info("**************Registry API Resource Move To Root Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Resource Move To Root Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Resource Move To Root Test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test move a resource to general location ", priority = 3)
    public void testGeneralResourceMoveTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "move");
            r1.setContent("c");
            registry.put("/test/c1/move/move3", r1);

            Collection c2 = registry.newCollection();
            registry.put("/test/c2/move", c2);
            registry.move("/test/c1/move/move3", "/test/c2/move/move3");

            Resource newR1 = registry.get("/test/c2/move/move3");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");

            boolean failed = false;
            try {
                registry.get("/test/c1/move/move3");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("***************Registry API General Resource Move Test -Passed *************");
        } catch (RegistryException e) {
            log.error("Registry API -GeneralResourceMoveTest RegistryException thrown :" + e.getMessage());
            throw new RegistryException("Registry API Resource Move To Root Test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test move a resource to general location ", priority = 4)
    public void testGeneralCollectionMoveTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "move");
            r1.setContent("c");
            registry.put("/test/c1/move5/move/dummy", r1);

            Collection c2 = registry.newCollection();
            registry.put("/test/c3", c2);

            registry.move("/test/c1/move5", "/test/c3/move5");

            Resource newR1 = registry.get("/test/c3/move5/move/dummy");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");

            boolean failed = false;
            try {
                registry.get("/test/c1/move5/move/dummy");
            } catch (RegistryException e) {
                failed = true;
            }
            assertTrue(failed, "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("***************Registry API General Collection Move Test - Passed ***************");
        } catch (RegistryException e) {
            log.error("Registry API General Collection Move Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API General Collection Move Test - Failed:" + e.getMessage());
        }

    }

    private void removeResource() throws RegistryException {
        deleteResources("/test");
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
