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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import java.net.MalformedURLException;


public class TestTagging {
    private static final Log log = LogFactory.getLog(TestTagging.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }

    private void removeResource() throws RegistryException {
        deleteResources("/d11");
        deleteResources("/d12");
        deleteResources("/d13");
        deleteResources("/d15");
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

    @Test(groups = {"wso2.greg"}, description = "test add a tag ", priority = 1)
    public void testAddTaggingTest() throws RegistryException {
        // add a resource
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "q1 content".getBytes();
            r1.setContent(r1content);
            registry.put("/d11/r1", r1);

            Resource r2 = registry.newResource();
            byte[] r2content = "q2 content".getBytes();
            r2.setContent(r2content);
            registry.put("/d11/r2", r2);

            Resource r3 = registry.newResource();
            byte[] r3content = "q3 content".getBytes();
            r3.setContent(r3content);
            registry.put("/d11/r3", r3);

            registry.applyTag("/d11/r1", "jsp");
            registry.applyTag("/d11/r2", "jsp");
            registry.applyTag("/d11/r3", "java long tag");

            TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
            boolean artifactFound = false;
            for (TaggedResourcePath path : paths) {


                if (path.getResourcePath().equals("/d11/r1")) {
                    assertEquals("/d11/r1", path.getResourcePath(),"Path are not matching");
                    artifactFound = true;
                }
            }
            assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");

            Tag[] tags = null;
            try {
                tags = registry.getTags("/d11/r1");
            }
            catch (RegistryException e) {
                fail("Failed to get tags for the resource /d11/r1");
            }

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("jsp")) {
                    tagFound = true;
                    break;
                }
            }
            assertTrue(tagFound, "tag 'jsp' is not associated with the artifact /d11/r1");
            registry.getResourcePathsWithTag("jsp");
            deleteResources("/d11");
            log.info("***************Registry API Add Tagging Test - Passed ********************");
        } catch (RegistryException e) {
            log.error("Registry API Add Tagging Test - Failed:" + e.getMessage());
            throw new RegistryException("Registry API Add Tagging Test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test duplicate test tag ", priority = 2)
    public void testDuplicateTaggingTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "q1 content".getBytes();
            r1.setContent(r1content);
            registry.put("/d12/r1", r1);

            registry.applyTag("/d12/r1", "tag1");
            registry.applyTag("/d12/r1", "tag2");

            Tag[] tags = registry.getTags("/d12/r1");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;
                }
            }
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d12/r1");
            deleteResources("/d12");
            log.info("****************Registry API Duplicate Tagging Test - Passed ***********************");
        } catch (RegistryException e) {
            log.error("Registry API Duplicate Tagging Test- Failed :" + e.getMessage());
            throw new RegistryException("Registry API Duplicate Tagging Test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a tag to collection ", priority = 3)
    public void testAddTaggingCollectionTest() throws RegistryException {
        Collection r1;
        try {
            r1 = registry.newCollection();
            registry.put("/d13/d14", r1);
            registry.applyTag("/d13/d14", "col_tag1");

            Tag[] tags = registry.getTags("/d13/d14");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("col_tag1")) {
                    tagFound = true;
                    break;
                }
            }
            assertTrue(tagFound, "tag 'col_tag1' is not associated with the artifact /d13/d14");
            deleteResources("/d13");
            log.info("***************Registry API Add Tagging Collection Test - Passed ********************");
        } catch (RegistryException e) {
            log.error("Registry API Add Tagging Collection Test - Failed:" + e.getMessage());
            throw new RegistryException("Registry API Add Tagging Collection Test -Failed :" + e.getMessage());
        }
    }

//    @Test(groups = {"wso2.greg"}, description = "test edit a tag in a collection ", priority = 4)
    public void testEditTaggingTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "q1 content".getBytes();
            r1.setContent(r1content);
            registry.put("/d14/d13/r1", r1);

            registry.applyTag("/d14/d13/r1", "tag1");
            registry.applyTag("/d14/d13/r1", "tag2");

            Tag[] tags = registry.getTags("/d14/d13/r1");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;
                    assertEquals("tag1", tag.getTagName(), "Tag names are not equals");
                    assertEquals(1, tag.getCategory(), "Tag category not equals");
                    assertEquals(1, (int) (tag.getTagCount()), "Tag count not equals");
                    assertEquals(2, tags.length, "Tag length not equals");
                    tag.setTagName("tag1_updated");
                    break;
                }
            }

            TaggedResourcePath[] paths = null;
            try

            {
                paths = registry.getResourcePathsWithTag("tag1");

            } catch (RegistryException e) {
                fail("Failed to get resources with tag 'tag1'");
            }

            boolean artifactFound = false;
            for (
                    TaggedResourcePath path
                    : paths)

            {
                if (path.getResourcePath().equals("/d14/d13/r1")) {
                    assertEquals("/d14/d13/r1", path.getResourcePath(), "Path are not matching");
                    assertEquals(0, (int) (paths[0].getTagCount()), "Tag count not equals");
                    artifactFound = true;
                }
            }
            assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");
            assertTrue(tagFound, "tag 'col_tag1' is not associated with the artifact /d14/d13/r1");
            deleteResources("/d14");
            log.info("************Registry API Edit Tagging Test - Passed ******************");
        } catch (RegistryException e) {
            log.error("Registry API Edit Tagging Test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Edit Tagging Test-Failed:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test a tag from resource ", priority = 5)
    public void RemoveResourceTaggingTest() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "q1 content".getBytes();
            r1.setContent(r1content);
            registry.put("/d15/d14/r1", r1);

            registry.applyTag("/d15/d14/r1", "tag1");
            registry.applyTag("/d15/d14/r1", "tag2");

            Tag[] tags = registry.getTags("/d15/d14/r1");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;
                }
            }

            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");

            /*remove tag goes here*/
            registry.removeTag("/d15/d14/r1", "tag1");

            TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");
            boolean artifactFound = false;
            for (TaggedResourcePath path : paths) {
                if (path.getResourcePath().equals("/d15/d14/r1")) {
                    artifactFound = true;
                }
            }
            assertFalse(artifactFound, "/d15/d14/r1 is not tagged with the tag \"tag1\"");
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");
            deleteResources("/d15");
            log.info("************ Registry API Remove Resource Tagging Test - Passed ******************");
        } catch (RegistryException e) {
            log.error("Registry API Remove Resource Tagging Test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Remove Resource Tagging Test-Failed:" + e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test a tag from collection ", priority = 6)
    public void testRemoveCollectionTaggingTest() throws RegistryException {
        CollectionImpl r1 = new CollectionImpl();
        r1.setAuthorUserName("Author q1 remove");
        try {
            registry.put("/d15/d14/d13/d12", r1);
            registry.applyTag("/d15/d14/d13", "tag1");
            registry.applyTag("/d15/d14/d13", "tag2");
            registry.applyTag("/d15/d14/d13", "tag3");

            Tag[] tags = registry.getTags("/d15/d14/d13");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;
                }
            }

            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");

            /*remove tag goes here*/

            registry.removeTag("/d15/d14/d13", "tag1");

            TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");

            boolean artifactFound = false;
            for (TaggedResourcePath path : paths) {
                if (path.getResourcePath().equals("/d15/d14/d13")) {
                    artifactFound = true;
                }
            }
            assertFalse(artifactFound, "/d15/d14/d13 is not tagged with the tag \"tag1\"");
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");
            deleteResources("/d15");
            log.info("***************Registry API Remove Collection Tagging Test - Passed ******************");
        } catch (RegistryException e) {
            log.error("Registry API Remove Collection Tagging Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Remove Collection Tagging Test - Failed :" + e.getMessage());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "test a tag ", priority = 7)
    public void testTaggingTest() throws RegistryException {
        // add a resource
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            registry.put("/d11/r1", r1);

            Resource r2 = registry.newResource();
            byte[] r2content = "R2 content".getBytes();
            r2.setContent(r2content);
            registry.put("/d11/r2", r2);

            Resource r3 = registry.newResource();
            byte[] r3content = "R3 content".getBytes();
            r3.setContent(r3content);
            registry.put("/d11/r3", r3);

            registry.applyTag("/d11/r1", "JSP");
            registry.applyTag("/d11/r2", "jsp");
            registry.applyTag("/d11/r3", "jaVa");

            registry.applyTag("/d11/r1", "jsp");
            Tag[] r11Tags = registry.getTags("/d11/r1");
            assertEquals(1, r11Tags.length);

            TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
            boolean artifactFound = false;
            for (TaggedResourcePath path : paths) {
                if (path.getResourcePath().equals("/d11/r1")) {
                    artifactFound = true;
                    break;
                }
            }
            assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");

            Tag[] tags = registry.getTags("/d11/r1");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equalsIgnoreCase("jsp")) {
                    tagFound = true;
                    break;
                }
            }
            assertTrue(tagFound, "tag 'jsp' is not associated with the artifact /d11/r1");
            deleteResources("/d11");
            log.info("******************Registry API Tagging Test - Passed ********************");
        } catch (RegistryException e) {
            log.error("Registry API Tagging Test -Failed:" + e.getMessage());
            throw new RegistryException("Registry API Tagging Test -Failed :" + e.getMessage());
        }
    }
}
