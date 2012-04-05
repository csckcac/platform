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

public class TestPaths {
    private static final Log log = LogFactory.getLog(TestPaths.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }

    private void removeResource() throws RegistryException {
        deleteResources("/testkrishantha");
        deleteResources("/testkrishantha1");
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


    @Test(groups = {"wso2.greg"}, description = "test get on paths test ", priority = 1)
    public void testGetOnPathsTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            registry.put("/testkrishantha/paths/r1", r1);
            assertTrue(registry.resourceExists("/testkrishantha"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha/"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha/paths/r1"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha/paths/r1/"),"Resource not found.");

            registry.get("/testkrishantha");
            registry.get("/testkrishantha/");
            registry.get("/testkrishantha/paths/r1");
            registry.get("/testkrishantha/paths/r1/");
            deleteResources("/testkrishantha");
            log.info("****************Registry API Get On Paths Test - Passed ***************");
        } catch (RegistryException e) {
            log.error("Registry API Get On Paths Test -Failed:" + e.getMessage());
            throw new RegistryException("Registry API Get On Paths Test -Failed:" + e.getMessage());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "test put on path tests ", priority = 2)
    public void testPutOnPathsTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setContent("some content");
            registry.put("/testkrishantha1/paths2/r1", r1);

            Resource r2 = registry.newResource();
            r2.setContent("another content");
            registry.put("/testkrishantha1/paths2/r2", r2);

            Collection c1 = registry.newCollection();
            registry.put("/testkrishantha1/paths2/c1", c1);

            Collection c2 = registry.newCollection();
            registry.put("/testkrishantha1/paths2/c2", c2);

            assertTrue(registry.resourceExists("/testkrishantha1/paths2/r1"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha1/paths2/r2"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha1/paths2/c1"),"Resource not found.");
            assertTrue(registry.resourceExists("/testkrishantha1/paths2/c2"),"Resource not found.");
            deleteResources("/testkrishantha1");
            log.info("**************Registry API Put On Paths Test - Passed *****************");
        } catch (RegistryException e) {
            log.error("Registry API Put On Paths Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Put On Paths Test - Failed:" + e.getMessage());
        }
    }
}
