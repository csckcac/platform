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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import org.testng.annotations.*;

import java.net.MalformedURLException;


public class ContinuousOperations {
    private static final Log log = LogFactory.getLog(ContinuousOperations.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();

    }

    @Test(groups = {"wso2.greg"}, description = "test continuous delete ", priority = 1)
    public void testContinousDelete() throws RegistryException, InterruptedException {
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            Resource res1;
            try {
                res1 = registry.newResource();
                byte[] r1content = "R2 content".getBytes();
                res1.setContent(r1content);
                String path = "/con-delete/test/" + i + 1;
                registry.put(path, res1);

                Resource resource1 = registry.get(path);
                assertEquals(new String((byte[]) resource1.getContent()),
                        new String((byte[]) res1.getContent()), "File content is not matching");
                registry.delete(path);
                               assertTrue(registry.resourceExists(path), "Resource not found at the path");
                res1.discard();
                resource1.discard();
                Thread.sleep(100);
                deleteResources("/con-delete");
            } catch (RegistryException e) {
                log.error("Registry API Continous Delete test - Failed :" + e.getMessage());
                throw new RegistryException("Registry API Continous Delete test - Failed" + e.getMessage());
            } catch (InterruptedException e) {
                log.error("Registry API Continous Delete test - Failed :" + e.getMessage());
                throw new InterruptedException("Registry API Continous Delete test - Failed:" + e.getMessage());
            }
        }
        log.info("****************Registry API Continous Delete test -Passed***************");
    }

    @Test(groups = {"wso2.greg"}, description = "test continuous Update ", priority = 2)
    public void ContinuousUpdate() throws RegistryException, InterruptedException {
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            Resource res1;
            try {
                res1 = registry.newResource();
                byte[] r1content = "R2 content".getBytes();
                res1.setContent(r1content);
                String path = "/con-delete/test-update/" + i + 1;

                registry.put(path, res1);
                Resource resource1 = registry.get(path);
                assertEquals(new String((byte[]) resource1.getContent()),
                        new String((byte[]) res1.getContent()), "File content is not matching");

                Resource resource = new ResourceImpl();
                byte[] r1content1 = "R2 content updated".getBytes();
                resource.setContent(r1content1);
                resource.setProperty("abc", "abc");
                registry.put(path, resource);
                Resource resource2 = registry.get(path);
                assertEquals(new String((byte[]) resource.getContent()),
                        new String((byte[]) resource2.getContent()), "File content is not matching");
                resource.discard();
                res1.discard();
                resource1.discard();
                resource2.discard();
                Thread.sleep(100);
                deleteResources("/con-delete");
            } catch (RegistryException e) {
                log.error("Registry API Continous Update test - Failed :" + e.getMessage());
                throw new RegistryException("Registry API Continous Update test - Failed:" + e.getMessage());
            } catch (InterruptedException e) {
                log.error("Registry API Continous Update test - Failed :" + e.getMessage());
                throw new InterruptedException("Registry API Continous Update test - Failed:" + e.getMessage());
            }

        }
        log.info("****************Registry API Continous Update test -Passed***************");
    }

    private void removeResource() throws RegistryException {
        deleteResources("/con-delete");

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
