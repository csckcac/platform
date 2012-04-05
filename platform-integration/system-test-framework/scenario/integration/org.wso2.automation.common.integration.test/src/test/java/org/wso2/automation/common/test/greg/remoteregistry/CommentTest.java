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
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class CommentTest {
    private static final Log log = LogFactory.getLog(CommentTest.class);
    public RemoteRegistry registry;
    String username;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        //Tenant Details
        UserInfo tenantDetails = UserListCsvReader.getUserInfo(tenantId);
        username = tenantDetails.getUserName();
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a comment", priority = 1)
    public void testAddComment() throws RegistryException {
        Resource r1;
        try {
            r1 = registry.newResource();
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
            assertTrue(commentsResource instanceof Collection, "Comment collection resource should be a directory.");
            deleteResources("/d112");
            assertFalse(registry.resourceExists(path), path + "has not been deleted properly");
            log.info("**************Registry API Add Comment test - Passed******************");
        } catch (RegistryException e) {
            log.error("Registry API Add Comment test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Add Comment test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a comment to Resource", priority = 2)
    public void testAddCommentToResource() throws RegistryException {
        String path = "/d1/r3";
        Resource r1;
        try {
            r1 = registry.newResource();
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

            assertTrue(commentFound, "comment '" + comment1 +
                    " is not associated with the artifact /d1/r3");
            Resource commentsResource = registry.get("/d1/r3;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(new String((byte[]) commentResource.getContent()));
            }

            assertTrue(commentTexts.contains(comment1), comment1 + " is not associated for resource /d1/r3.");
            assertTrue(commentTexts.contains(comment2), comment2 + " is not associated for resource /d1/r3.");
            deleteResources("/d1");
            log.info("**************Registry API Add Comment To Resource test - Passed****************");
        } catch (RegistryException e) {
            log.error("Registry API Add Comment To Resource test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Add Comment To Resource test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a comment to collection", priority = 3)
    public void testAddCommentToCollection() throws RegistryException {
        String path = "/d11/d12";
        Resource r1;
        try {
            r1 = registry.newCollection();
            r1.setDescription("this is a collection to add comment");
            registry.put(path, r1);

            String comment1 = "this is qa comment 1 for collection d12";
            String comment2 = "this is qa comment 2 for collection d12";

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment for d12");
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

                if (comment.getText().equals(c1.getText())) {
                    commentFound = true;
                    assertEquals("This is default comment for d12", comment.getText());
                }
            }
            assertTrue(commentFound, "comment '" + comment1 +
                    " is not associated with the artifact /d11/d12");

            Resource commentsResource = registry.get("/d11/d12;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(new String((byte[]) commentResource.getContent()));
            }

            assertTrue(commentTexts.contains(comment1), comment1 + " is not associated for resource /d11/d12.");
            assertTrue(commentTexts.contains(comment2), comment2 + " is not associated for resource /d11/d12.");
            deleteResources("/d11");
            log.info("**************Registry API Add Comment To Collection test - Passed****************");
        } catch (RegistryException e) {
            log.error("Registry API Add Comment To Collection test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Add Comment To Collection test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test adding a comment to root", priority = 4)
    public void testAddCommenttoRoot() throws RegistryException {
        String comment1 = "this is qa comment 1 for root";
        String comment2 = "this is qa comment 2 for root";

        Comment c1 = new Comment();
        c1.setResourcePath("/");
        c1.setText("This is default comment for root");
        c1.setUser(username);
        try {
            registry.addComment("/", c1);
            registry.addComment("/", new Comment(comment1));
            registry.addComment("/", new Comment(comment2));

            Comment[] comments;
            comments = registry.getComments("/");
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


            Resource commentsResource = registry.get("/;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment collection resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(new String((byte[]) commentResource.getContent()));
            }

            assertTrue(commentTexts.contains(comment1), comment1 + " is not associated for resource /.");
            assertTrue(commentTexts.contains(comment2), comment2 + " is not associated for resource /.");

            for (int i = 0; i < comments.length; i++) {
                registry.removeComment(comments[i].getPath());
            }
            log.info("**************Registry API Add Comment To Root test- Passed********************");
        } catch (RegistryException e) {
            log.error("Registry API Add Comment To Root test-Failed:" + e.getMessage());
            throw new RegistryException("Registry API Add Comment To Root test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test editing a comment ", priority = 5)
    public void testEditComment() throws RegistryException {
        String path = "/c101/c11/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            r1.setDescription("this is a resource to edit comment");
            registry.put(path, r1);

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is default comment");
            c1.setUser(username);

            registry.addComment(path, c1);
            Comment[] comments = registry.getComments(path);
            boolean commentFound = false;

            for (Comment comment : comments) {
                if (comment.getText().equals(c1.getText())) {
                    commentFound = true;
                    assertEquals("This is default comment", comment.getText());
                }
            }

            assertTrue(commentFound, "comment:" + c1.getText() +
                    " is not associated with the artifact /c101/c11/r1");

            Resource commentsResource = registry.get("/c101/c11/r1;comments");
            assertTrue(commentsResource instanceof Collection,
                    "Comment resource should be a directory.");
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(new String((byte[]) commentResource.getContent()));
            }

            assertTrue(commentTexts.contains(c1.getText()),
                    c1.getText() + " is not associated for resource /c101/c11/r1.");
            registry.editComment(comments[0].getPath(), "This is the edited comment");
            comments = registry.getComments(path);
            Resource resource = registry.get(comments[0].getPath());
            assertEquals(new String((byte[]) resource.getContent()), "This is the edited comment");
            registry.editComment("/c101/c11/r1", "This is the edited comment");
            deleteResources("/c101");
            log.info("***************Registry API Edit Comment test - Passed****************");
        } catch (RegistryException e) {
            log.error("Registry API Edit Comment test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Edit Comment test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test delete a comment ", priority = 6)
    public void testCommentDelete() throws RegistryException {
        String r1Path = "/c1d1/c1";
        Collection r1;
        try {
            r1 = registry.newCollection();
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
            deleteResources("/c1d1");
            log.info("****************Registry API Delete Comment test - Passed********************");
        } catch (RegistryException e) {
            log.error("Registry API Delete Comment test - Failed:" + e.getMessage());
            throw new RegistryException("Registry API Delete Comment test-Failed:" + e.getMessage());
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
