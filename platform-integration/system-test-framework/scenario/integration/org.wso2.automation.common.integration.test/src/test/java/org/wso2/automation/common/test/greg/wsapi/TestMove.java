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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;


public class TestMove {
    private static final Log log = LogFactory.getLog(TestMove.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Move a resource from Root", priority = 1)
    private void testResourceMoveFromRoot() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        try {
            r1.setContent("c");
            registry.put("/move1", r1);

            Collection c1 = registry.newCollection();
            registry.put("/test/move", c1);
            registry.move("/move1", "/test/move/move1");

            Resource newR1 = registry.get("/test/move/move1");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");
            assertFalse(registry.resourceExists("/move1"), "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("************WS-API Move resource from Root test - Passed*************");
        } catch (RegistryException e) {
            log.error("WS-API Move resource from Root test -Failed:" + e);
            throw new RegistryException("WS-API Move resource from Root test -Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Move a resource to Root", priority = 2)
    private void testResourceMoveToRoot() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        try {
            r1.setContent("c");

            registry.put("/test/move/move2", r1);
            registry.move("/test/move/move2", "/move2");

            Resource newR1 = registry.get("/move2");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");
            assertFalse(registry.resourceExists("/test/move/move2"), "Moved resource should not be accessible from the old path.");
            deleteResources("/move2");
            log.info("************WS-API Resource Move To Root test - Passed ************");
        } catch (RegistryException e) {
            log.error("WS-API Resource Move To Root test -Failed:" + e);
            throw new RegistryException("WS-API Resource Move To Root test-Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Move a resource to a Collection", priority = 3)
    private void testGeneralResourceMove() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");

        try {
            r1.setContent("c");
            registry.put("/test/c1/move/move3", r1);
            Collection c2 = registry.newCollection();
            registry.put("/test/c2/move", c2);
            registry.move("/test/c1/move/move3", "/test/c2/move/move3");

            Resource newR1 = registry.get("/test/c2/move/move3");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");
            assertFalse(registry.resourceExists("/test/c1/move/move3"), "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("*************WS-API General Resource Move test - Passed*************");
        } catch (RegistryException e) {
            log.error("generalResourceMove RegistryException thrown:" + e);
            throw new RegistryException("WS-API Resource Move To Root test-Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Move a collection to another Collection", priority = 4)
    private void testGeneralCollectionMove() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");

        try {
            r1.setContent("c");
            registry.put("/test/c1/move5/move/dummy", r1);
            Collection c2 = registry.newCollection();
            registry.put("/test/c3", c2);
            registry.move("/test/c1/move5", "/test/c3/move5");
            Resource newR1 = registry.get("/test/c3/move5/move/dummy");
            assertEquals(newR1.getProperty("test"), "move", "Moved resource should have a property named 'test' with value 'move'.");
            assertFalse(registry.resourceExists("/test/c1/move5/move/dummy"), "Moved resource should not be accessible from the old path.");
            deleteResources("/test");
            log.info("*************WS-API General Collection Move test - Passed *************");
        } catch (RegistryException e) {
            log.error("WS-API General Collection Move test - Failed:" + e);
            throw new RegistryException("WS-API General Collection Move test-Failed:" + e);
        }
    }


    private void removeResource() throws RegistryException {
        deleteResources("/move1");
        deleteResources("/test");
        deleteResources("/move2");
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
