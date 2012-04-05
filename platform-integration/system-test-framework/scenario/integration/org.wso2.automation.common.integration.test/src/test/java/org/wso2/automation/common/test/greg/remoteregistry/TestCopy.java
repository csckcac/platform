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


public class TestCopy {
    private static final Log log = LogFactory.getLog(TestCopy.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test copy a resource ", priority = 1)
    public void testResourceCopyTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "copy");
            r1.setContent("c");
            registry.put("/test1/copy/c1/copy1", r1);

            Collection c1 = registry.newCollection();
            registry.put("/test1/move", c1);
            registry.copy("/test1/copy/c1/copy1", "/test1/copy/c2/copy2");

            Resource newR1 = registry.get("/test1/copy/c2/copy2");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/c1/copy1");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");

            String newContent = new String((byte[]) newR1.getContent());
            String oldContent = new String((byte[]) oldR1.getContent());
            assertEquals(newContent, oldContent, "Contents are not equal in copied resources");
            deleteResources("/test1");
            log.info("***************Registry API Resource Copy Test - Passed *******************");
        } catch (RegistryException e) {
            log.error("Registry API Resource Copy Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Resource Copy Test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test copy a collection ", priority = 2)
    public void testCollectionCopyTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            r1.setProperty("test", "copy");
            r1.setContent("c");
            registry.put("/test1/copy/copy3/c3/resource1", r1);

            Collection c1 = registry.newCollection();
            registry.put("/test1/move", c1);
            registry.copy("/test1/copy/copy3", "/test1/newc/copy3");

            Resource newR1 = registry.get("/test1/newc/copy3/c3/resource1");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/copy3/c3/resource1");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");
            deleteResources("/test1");
            log.info("***************Registry API Collection Copy Test - Failed *****************");
        } catch (RegistryException e) {
            log.error("Registry API Collection Copy Test-Failed :" + e.getMessage());
            throw new RegistryException("Registry API Collection Copy Test - Failed:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/test1");
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
