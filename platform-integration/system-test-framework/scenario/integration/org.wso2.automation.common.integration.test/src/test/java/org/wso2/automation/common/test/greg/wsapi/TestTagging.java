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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

public class TestTagging {
    private static final Log log = LogFactory.getLog(TestTagging.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }


    @Test(groups = {"wso2.greg"}, description = "Apply a Tag", priority = 1)
    private void testAddTagging() throws RegistryException {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();

        try {
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
                    assertEquals("/d11/r1", path.getResourcePath(), "Path are not matching");
                    artifactFound = true;

                }
            }
            assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");

            Tag[] tags = null;

            try {
                tags = registry.getTags("/d11/r1");
            } catch (Exception e) {
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
            log.info("**************WS-API Apply Tag test - Passed************");
        } catch (RegistryException e) {
            log.error("*WS-API Apply Tag test -Failed:" + e);
            throw new RegistryException("*WS-API Apply Tag test - Failed:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Apply duplicate Tag", priority = 2)
    private void testDuplicateTagging() throws RegistryException {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();

        try {
            r1.setContent(r1content);
            registry.put("/d12/r1", r1);
            registry.applyTag("/d12/r1", "tag1");
            registry.applyTag("/d12/r1", "tag2");

            Tag[] tags = registry.getTags("/d12/r1");

            boolean tagFound = false;

            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;


                    break;

                }
            }
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d12/r1");
            deleteResources("/d12");
            log.info("************WS-API Duplicate Tagging test - Passed*************");
        } catch (RegistryException e) {
            log.error("WS-API Duplicate Tagging test -Failed:" + e);
            throw new RegistryException("WS-API Duplicate Tagging test - Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Apply Tag to a Collection", priority = 3)
    private void testAddTaggingCollection() throws RegistryException {
        Collection r1 = registry.newCollection();

        try {
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
            log.info("**************WS-API Apply Tag to Collection test - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Apply Tag to Collection test - Failed:" + e);
            throw new RegistryException("WS-API Apply Tag to Collection test -Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Edit a Tag", priority = 4)
    private void testEditTagging() throws RegistryException {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();

        try {
            r1.setContent(r1content);
            registry.put("/d14/d13/r1", r1);
            registry.applyTag("/d14/d13/r1", "tag1");
            registry.applyTag("/d14/d13/r1", "tag2");

            Tag[] tags = registry.getTags("/d14/d13/r1");

            boolean tagFound = false;
            for (Tag tag : tags) {
                if (tag.getTagName().equals("tag1")) {
                    tagFound = true;
                    //System.out.println(tag.getTagName());
                    assertEquals("tag1", tag.getTagName(), "Tag names are not equals");
                    //System.out.println(tag.getCategory());
                    assertEquals(1, tag.getCategory(), "Tag category not equals");
                    //System.out.println(tag.getTagCount());
                    assertEquals(1, (int) (tag.getTagCount()), "Tag count not equals");
                    //System.out.println(tags.length);
                    assertEquals(2, tags.length, "Tag length not equals");

                    tag.setTagName("tag1_updated");
                    break;

                }
            }

            TaggedResourcePath[] paths = null;
            try {
                paths = registry.getResourcePathsWithTag("tag1");
            } catch (Exception e) {
                fail("Failed to get resources with tag 'tag1'");
            }
            boolean artifactFound = false;
            for (TaggedResourcePath path : paths) {
                if (path.getResourcePath().equals("/d14/d13/r1")) {
                    assertEquals("/d14/d13/r1", path.getResourcePath(), "Path are not matching");
                    assertEquals(1, (int) (paths[0].getTagCount()), "Tag count not equals");
                    artifactFound = true;
                    //break;
                }
            }
            assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");
            assertTrue(tagFound, "tag 'col_tag1' is not associated with the artifact /d14/d13/r1");
            deleteResources("/d14");
            log.info("*************WS-API Edit Tag Test - Passed *****************");
        } catch (RegistryException e) {
            log.error("WS-API Edit Tag Test -Fail:" + e);
            throw new RegistryException("WS-API Edit Tag Test  -Failed:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Remove a Tag from Resource", priority = 5)
    private void testRemoveResourceTagging() throws RegistryException {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();

        try {
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
//
            boolean artifactFound = false;

            if (paths != null) {

                for (TaggedResourcePath path : paths) {

                    if (path.getResourcePath().equals("/d15/d14/r1")) {

                        artifactFound = true;

                    }
                }
            }
            assertFalse(artifactFound, "/d15/d14/r1 is not tagged with the tag \"tag1\"");
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");
            deleteResources("/d15");
            log.info("**************WS-API Remove Resource Tagging test - Passed*************");
        } catch (RegistryException e) {
            log.error("WS-API Remove Resource Tagging test - Failed:" + e);
            throw new RegistryException("WS-API Remove Resource Tagging test -Failed:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Remove a Tag from Collection", priority = 6)
    private void testRemoveCollectionTagging() throws RegistryException {
        CollectionImpl r1 = new CollectionImpl();
        r1.setAuthorUserName("Author q1 remove");

        try {
            registry.put("/d15/d14/d13/d12", r1);
            registry.applyTag("/d15/d14/d13", "tag1");
            registry.applyTag("/d15/d14/d13", "tag2");
            registry.applyTag("/d15/d14/d13", "tag3");

            Tag[] tags = registry.getTags("/d15/d14/d13");
            //System.out.println("getTagCount:" + tags[0].getTagCount());

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

            //System.out.println("Path tag counts:" + paths.length);
            boolean artifactFound = false;

            if (paths != null) {
                for (TaggedResourcePath path : paths) {


                    if (path.getResourcePath().equals("/d15/d14/d13")) {

                        artifactFound = true;

                    }
                }
            }
            assertFalse(artifactFound, "/d15/d14/d13 is not tagged with the tag \"tag1\"");
            assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");
            deleteResources("/d15");
            log.info("*************WS-API Remove Tag from Collection test - Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API Remove Tag from Collection test - Failed:" + e);
            throw new RegistryException("WS-API Remove Tag from Collection test -Failed:" + e);
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Remove a Tag from Collection", priority = 7)
    private void testTagging() throws RegistryException {
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();

        try {
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

            TaggedResourcePath[] paths2 = registry.getResourcePathsWithTag("jsp");

            assertEquals(paths2, null, "Tag based search should not return paths of deleted resources.");

            deleteResources("/d11");
            log.info("*****************WS-API tagging test- Passed********************");
        } catch (RegistryException e) {
            log.error("tagging Registry Exception thrown:" + e);
            throw new RegistryException("WS-API Remove Tag from Collection test -Failed:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/d11");
        deleteResources("/d12");
        deleteResources("/d13");
        deleteResources("/d14");
        deleteResources("/d15");
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
