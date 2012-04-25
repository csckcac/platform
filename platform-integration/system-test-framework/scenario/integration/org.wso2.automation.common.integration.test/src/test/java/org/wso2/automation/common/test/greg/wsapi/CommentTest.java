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
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import org.testng.annotations.*;


import java.util.ArrayList;
import java.util.List;

public class CommentTest {
    private static final Log log = LogFactory.getLog(CommentTest.class);
    private static WSRegistryServiceClient registry = null;
    private String username = null;
    private String password = null;
    String tenantId;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);

        UserInfo tenantDetails = UserListCsvReader.getUserInfo(tenantId);
        username = tenantDetails.getUserName();
        password = tenantDetails.getPassword();
        removeResource();       //delete existing resources
    }


    @Test(groups = {"wso2.greg"}, description = "Add Comment to Collection")
    public void testAddComment() throws Exception {
        try {
            Resource r1 = registry.newResource();
            String path = "/d112/r3";
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            registry.put(path, r1);

            String comment1 = "this is qa comment 4";
            String comment2 = "this is qa comment 5";
            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment");

            c1.setUser(username);

            registry.addComment(path, c1);
            registry.addComment(path, new Comment(comment1));
            registry.addComment(path, new Comment(comment2));

            Comment[] comments = registry.getComments(path);

            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());

                }
                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }
                if (comment.getText().equals("This is default comment")) {
                    commentFound = true;
                    assertEquals("This is default comment", comment.getText());
                }
            }
            assertTrue(commentFound, "No comment is associated with the resource" + path);

            Resource commentsResource = registry.get("/d112/r3;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");

            comments = (Comment[]) commentsResource.getContent();
            List<String> commentTexts = new ArrayList<String>();
            for (Comment comment : comments) {

                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent().toString());
            }

            assertTrue(commentTexts.contains(comment1),
                    comment1 + " is not associated with the resource /d112/r3.");
            assertTrue(commentTexts.contains(comment2), comment2 + " is not associated with the resource /d112/r3.");

            deleteResources("/d112");                 //delete Resource
            assertFalse(registry.resourceExists(path), path + "has not been deleted properly");    //assert resource has been properly deleted
            log.info("***********WS-API CommentTest AddComment - Passed**********");
        } catch (Exception e) {
            log.error("WS-API add comment Failed :" + e.getMessage());
            throw new Exception("WS-API add comment Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add Comment to Resource")
    public void testAddCommentToResource() throws Exception {
        try {
            String path = "/d1/r3";
            Resource r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            registry.put(path, r1);

            String comment1 = "this is qa comment 4";
            String comment2 = "this is qa comment 5";
            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment");
            c1.setUser(username);

            registry.addComment(path, c1);
            registry.addComment(path, new Comment(comment1));
            registry.addComment(path, new Comment(comment2));

            Comment[] comments = registry.getComments("/d1/r3");

            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }
                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }

                if (comment.getText().equals("This is default comment")) {
                    commentFound = true;
                    assertEquals("This is default comment", comment.getText());
                }
            }

            assertTrue(commentFound, "comment '" + comment1 +
                    " is not associated with the artifact /d1/r3");
            Resource commentsResource = registry.get("/d1/r3;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List<Object> commentTexts = new ArrayList<Object>();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }
            assertTrue(commentTexts.contains(comment1),
                    comment1 + " is not associated for resource /d1/r3.");
            assertTrue(commentTexts.contains(comment2),
                    comment2 + " is not associated for resource /d1/r3.");

            deleteResources("/d1");                 //delete Resource
            assertFalse(registry.resourceExists(path), path + "has not been deleted properly");                  //assert resource has been properly deleted
            log.info("**********WS-API add Comment To Resource - passed**********");
        } catch (Exception e) {
            log.error("WS-API add Comment To Resource  Failed:" + e.getMessage());
            throw new Exception("WS-API add Comment To Resource Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add Comment to Collection")
    public void testAddCommentToCollection() throws Exception {
        try {
            String path = "/d11/d12";

            Resource r1 = registry.newCollection();
            r1.setDescription("this is a collection to add comment");
            registry.put(path, r1);

            String comment1 = "this is qa comment 1 for collection d12";
            String comment2 = "this is qa comment 2 for collection d12";

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment for d12");
            c1.setUser(username);

            try {
                registry.addComment(path, c1);
                registry.addComment(path, new Comment(comment1));
                registry.addComment(path, new Comment(comment2));
            } catch (RegistryException e) {
                fail("Valid commenting for resources scenario failed");
            }

            Comment[] comments = null;
            try {
                comments = registry.getComments(path);
            } catch (RegistryException e) {
                fail("Failed to get comments for the resource /d11/d12");
            }

            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }
                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }

                if (comment.getText().equals(c1.getText())) {
                    commentFound = true;
                    assertEquals("This is default comment for d12", comment.getText());
                }
            }

            assertTrue(commentFound, "comment '" + comment1 +
                    " is not associated with the artifact /d11/d12");

            try {
                Resource commentsResource = registry.get("/d11/d12;comments");
                assertTrue(commentsResource instanceof Collection,
                        "Comment collection resource should be a directory.");
                comments = (Comment[]) commentsResource.getContent();

                List commentTexts = new ArrayList();
                for (Comment comment : comments) {
                    Resource commentResource = registry.get(comment.getPath());
                    commentTexts.add(commentResource.getContent());
                }

                assertTrue(commentTexts.contains(comment1),
                        comment1 + " is not associated for resource /d11/d12.");
                assertTrue(commentTexts.contains(comment2),
                        comment2 + " is not associated for resource /d11/d12.");
            } catch (RegistryException e) {
                e.printStackTrace();
                fail("Failed to get comments form URL: /d11/d12;comments");
            }

            deleteResources("/d11");                           //Delete Resource
            assertFalse(registry.resourceExists(path), path + "Resource Deleted");                    //Assert resource has been properly deleted
            log.info("**********WS-API add Comment to Collection - Passed**********");
        } catch (Exception e) {
            log.error("WS-API add Comment to Collection Failed:" + e.getMessage());
            throw new Exception("WS-API add Comment to Collection Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add Comment to Root")
    public void testAddCommenttoRoot() throws Exception {
        String comment1 = "this is qa comment 1 for root";
        String comment2 = "this is qa comment 2 for root";
        try {
            Comment c1 = new Comment();
            c1.setResourcePath("/");
            c1.setText("This is default comment for root");
            c1.setUser(username);

            try {
                registry.addComment("/", c1);
                registry.addComment("/", new Comment(comment1));
                registry.addComment("/", new Comment(comment2));
            } catch (RegistryException e) {
                fail("Valid commenting for resources scenario failed");
            }

            Comment[] comments = null;
            try {
                comments = registry.getComments("/");
            } catch (RegistryException e) {
                fail("Failed to get comments for the resource /");
            }

            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals("/", comment.getResourcePath());
                }

                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals("/", comment.getResourcePath());
                }

                if (comment.getText().equals(c1.getText())) {
                    commentFound = true;
                    assertEquals("This is default comment for root", comment.getText());
                }
            }

            assertTrue(commentFound, "comment '" + comment1 +
                    " is not associated with the artifact /");

            try {
                Resource commentsResource = registry.get("/;comments");
                assertTrue(commentsResource instanceof Collection,
                        "Comment collection resource should be a directory.");
                comments = (Comment[]) commentsResource.getContent();

                List commentTexts = new ArrayList();
                for (Comment comment : comments) {
                    Resource commentResource = registry.get(comment.getPath());
                    commentTexts.add(commentResource.getContent());
                }

                assertTrue(commentTexts.contains(comment1),
                        comment1 + " is not associated for resource /.");
                assertTrue(commentTexts.contains(comment2),
                        comment2 + " is not associated for resource /.");

                //Remove comments added to root
                for (int i = 0; i < comments.length; i++) {
                    registry.removeComment(comments[i].getPath());
                }
            } catch (RegistryException e) {
                fail("Failed to get comments form URL: /;comments");
            }
            log.info("***********WS-API add Comment to Root - passed**********");
        } catch (Exception e) {
            log.error("WS-API add Comment to Root Failed:" + e.getMessage());
            throw new Exception("WS-API add Comment to Root Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Edit comment in a collection")
    public void testEditComment() throws Exception {
        String path = "/c101/c11/r1";
        try {
            Resource r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            r1.setDescription("this is a resource to edit comment");
            registry.put(path, r1);

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment " + path);
            c1.setUser(username);

            registry.addComment(path, c1);

            Comment[] comments = registry.getComments(path);

            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(c1.getText())) {
                    commentFound = true;
                    assertEquals("This is default comment " + path, comment.getText());
                    assertEquals(username, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }
            }
            assertTrue(commentFound, "comment:" + c1.getText() +
                    " is not associated with the artifact /c101/c11/r1");

            try {
                Resource commentsResource = registry.get("/c101/c11/r1;comments");
                assertTrue(commentsResource instanceof Collection,
                        "Comment resource should be a directory.");
                comments = (Comment[]) commentsResource.getContent();

                List<Object> commentTexts = new ArrayList<Object>();
                for (Comment comment : comments) {
                    Resource commentResource = registry.get(comment.getPath());
                    commentTexts.add(commentResource.getContent());
                }
                assertTrue(commentTexts.contains(c1.getText()),
                        c1.getText() + " is not associated for resource /c101/c11/r1.");
                registry.editComment(comments[0].getPath(), "This is the edited comment");
                comments = registry.getComments(path);

                Resource resource = registry.get(comments[0].getPath());
                assertEquals("This is the edited comment", resource.getContent());

                registry.editComment(path, "This is the edited comment");           /*Edit comment goes here*/
                deleteResources("/c101");                //Delete resource
                assertFalse(registry.resourceExists(path), "Resource Deleted:");           //Assert resource has been properly deleted
                log.info("**********WS-API Edit Comment -Passed***********");
            } catch (RegistryException e) {
                e.printStackTrace();
                fail("Failed to get comments form URL:/c101/c11/r1;comments");
            }
        } catch (Exception e) {
            log.error("WS-API Edit Comment Failed:" + e.getMessage());
            throw new Exception("WS-API Edit Comment Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Delete a comment from collection")
    public void testCommentDelete() throws Exception {
        String r1Path = "/c1d1/c1";
        Collection r1 = registry.newCollection();
        try {
            registry.put(r1Path, r1);
            String c1Path = registry.addComment(r1Path, new Comment("test comment1"));
            registry.addComment(r1Path, new Comment("test comment2"));

            Comment[] comments1 = registry.getComments(r1Path);

            assertEquals(comments1.length, 2, "There should be two comments.");
            String[] cTexts1 = {comments1[0].getText(), comments1[1].getText()};
            assertTrue(containsString(cTexts1, "test comment1"), "comment is missing");
            assertTrue(containsString(cTexts1, "test comment2"), "comment is missing");

            registry.delete(c1Path);

            Comment[] comments2 = registry.getComments(r1Path);

            assertEquals(1, comments2.length, "There should be one comment.");

            String[] cTexts2 = {comments2[0].getText()};

            assertTrue(containsString(cTexts2, "test comment2"), "comment is missing");
            assertTrue(!containsString(cTexts2, "test comment1"), "deleted comment still exists");

            registry.delete("/c1d1");            //Delete Resource
            assertFalse(registry.resourceExists(r1Path), "Resource has been properly deleted:");      // assert Resource properly deleted
            log.info("***********WS-API Delete comment from a collection - Passed***********");
        } catch (RegistryException e) {
            log.error("WS-API Delete comment from a collection Failed:" + e.getMessage());
            throw new Exception("WS-API Delete comment from a collection Failed:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/d112");
        deleteResources("/d1");
        deleteResources("/d11");
        deleteResources("/c101");
        deleteResources("/c1d1");
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

    private boolean containsString(String[] array, String value) {
        boolean found = false;
        for (String anArray : array) {
            if (anArray.startsWith(value)) {
                found = true;
                break;
            }
        }
        return found;
    }
}
