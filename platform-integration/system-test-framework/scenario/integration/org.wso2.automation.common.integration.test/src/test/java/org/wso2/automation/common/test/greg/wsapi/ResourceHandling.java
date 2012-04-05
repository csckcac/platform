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
import org.wso2.platform.test.core.ProductConstant;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class ResourceHandling {
    private static final Log log = LogFactory.getLog(ResourceHandling.class);
    private static WSRegistryServiceClient registry = null;
    String password = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        password = UserListCsvReader.getUserInfo(tenantId).getPassword();
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "New Collection", priority = 1)
    private void testNewCollection() throws RegistryException {
        Collection collection = registry.newCollection();
        try {

            assertEquals(0, collection.getChildCount(), "Invalid Child Count for new collection");
            assertNotNull(collection.getChildren(), "The children for a new collection cannot be null");
            assertEquals(0, collection.getChildren().length, "Invalid Child Count for new collection");

            registry.put("/f1012/col", collection);
            Collection collection_new = (Collection) registry.get("/f1012/col");

            assertEquals(0, collection_new.getChildCount(), "Invalid Child Count for new collection");
            assertNotNull(collection_new.getChildren(), "The children for a new collection cannot be null");
            assertEquals(0, collection_new.getChildren().length, "Invalid Child Count for new collection");

            deleteResources("/f1012");
            log.info("*************WS-API New Collection test -Passed *************");
        } catch (RegistryException e) {
            log.error("WS-API New Collection test - Fail:" + e.getMessage());
            throw new RegistryException("WS-API New Collection test - Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Copy Resource", priority = 2)
    private void testResourceCopy() throws Exception {
        try {
            String path = "/f95/f2/r1";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/f96/f2/r1";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newResource();
            r1.setDescription("This is a file to be renamed");
            byte[] r1content = "R2 content".getBytes();
            r1.setContent(r1content);
            r1.setMediaType("txt");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is a test comment1");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is a test comment2");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals(r1.getProperty("key1"), r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1.getProperty("key2"), r2.getProperty("key2"), "Properties are not equal");
            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r2.getContent()), "File content is not matching");
            assertTrue(containsComment(commentPath, c1.getText()),
                    c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(commentPath, c2.getText()),
                    c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not exist");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not exist");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not exist");

            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");
            assertEquals(r1.getMediaType(), r2.getMediaType(), "Media type not exist");
            assertEquals(r1.getDescription(), r2.getDescription(), "Description is not exist");

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path, new_path_returned, "New resource path is not equal");


            Resource r1Renamed = registry.get(new_path);

            assertEquals(new String((byte[]) r2.getContent()),
                    new String((byte[]) r1Renamed.getContent()), "File content is not matching");
            assertEquals(r2.getProperty("key1"),
                    r1Renamed.getProperty("key1"), "Properties are not equal");
            assertEquals(r2.getProperty("key2"),
                    r1Renamed.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPathNew, c1.getText()),
                    c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(commentPathNew, c2.getText()),
                    c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");

            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");
            assertEquals(r2.getMediaType(), r1Renamed.getMediaType(), "Media type not copied");
            assertEquals(r2.getAuthorUserName(), r1Renamed.getAuthorUserName(), "Authour Name is not copied");
            assertEquals(r2.getDescription(), r1Renamed.getDescription(), "Description is not exist");
            deleteResources("/f95");
            deleteResources("/f96");
            log.info("**************WS-API Resource Copy test -Passed *************");
        } catch (RegistryException e) {
            log.error("WS-API Resource Copy test -Fail:" + e.getMessage());
            throw new RegistryException("WS-API Resource Copy test - Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Resource Copy test - Fail:" + e.getMessage());
            throw new Exception("WS-API Resource Copy test - Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Copy a collection", priority = 3)
    private void testCollectionCopy() throws Exception {

        try {

            String path = "/c9011/c1/c2";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/c9111/c1/c3";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newCollection();
            r1.setDescription("This is a file to be renamed");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is first test comment");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is secound test comment");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals(r1.getProperty("key1"), r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1.getProperty("key2"), r2.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPath, c1.getText()),
                    c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(commentPath, c2.getText()),
                    c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not copied");

            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path, new_path_returned, "New resource path is not equal");

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals(r2.getProperty("key1"), r1Renamed.getProperty("key1"), "Properties are not equal");
            assertEquals(r2.getProperty("key2"), r1Renamed.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPathNew, c1.getText()), c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(commentPathNew, c2.getText()), c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");

            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");
            deleteResources("/c9011");
            deleteResources("/c9111");
            log.info("*************WS-API Collection Copy test -Passed ************");
        } catch (RegistryException e) {
            log.error("WS-API Collection Copy test- Fail:" + e.getMessage());
            throw new RegistryException("WS-API Collection Copy test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Collection Copy test-Fail" + e.getMessage());
            throw new Exception("WS-API Collection Copy test- Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Resource Operation", priority = 4)
    private void testGetResourceoperation() throws RegistryException {
        Resource r2 = registry.newResource();
        String path = "/testk/testa/derby.log";
        try {
            r2.setContent(new String("this is the content").getBytes());

            r2.setDescription("this is test desc this is test desc this is test desc this is test desc this is test desc " +
                    "this is test desc this is test desc this is test desc this is test descthis is test desc ");
            r2.setMediaType("plain/text");
            registry.put(path, r2);

            r2.discard();

            Resource r3 = registry.newResource();
            r3 = registry.get(path);

            assertEquals(password, r3.getAuthorUserName(), "Author User names are not Equal");
            assertNotNull(r3.getCreatedTime(), "Created time is null");
            assertEquals(password, r3.getAuthorUserName(), "Author User names are not Equal");
            assertEquals("this is test desc this is test desc this is test desc this is test" +
                    " desc this is test desc this is test desc this is test desc this is test desc this is test descthis is " +
                    "test desc ", r3.getDescription(), "Description is not Equal");
            assertNotNull(r3.getId(), "Get Id is null");
            assertNotNull(r3.getLastModified(), "LastModifiedDate is null");
            assertEquals(password, r3.getLastUpdaterUserName(), "Last Updated names are not Equal");
            //System.out.println(r3.getMediaType());
            assertEquals("plain/text", r3.getMediaType(), "Media Type is not equal");
            assertEquals("/testk/testa", r3.getParentPath(), "parent Path is not equal");
            assertEquals(path, r3.getPath(), "parent Path is not equal");
            assertEquals(0, r3.getState(), "Get stated wrong");

            String st = r3.getPermanentPath();

            deleteResources("/testk");
            log.info("**************WS-API Get Resource Operation test -Passed *************");
        } catch (RegistryException e) {
            log.error("WS-API Get Resource Operation test- Fail:" + e.getMessage());
            throw new RegistryException("WS-API Get Resource Operation test- Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Get Collection Operation", priority = 5)
    private void testGetCollectionoperation() throws RegistryException {
        Resource r2 = registry.newCollection();
        String path = "/testk2/testa/testc";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");
        try {
            registry.put(path, r2);

            r2.discard();

            Resource r3 = registry.get(path);
            assertNotNull(r3.getId(), "Get Id is null");
            assertNotNull(r3.getLastModified(), "LastModifiedDate is null");
            assertEquals(password, r3.getLastUpdaterUserName(), "Last Updated names are not Equal");
            assertEquals("/testk2/testa", r3.getParentPath(), "parent Path is not equal");
            assertEquals(0, r3.getState(), "Get stated wrong");

            registry.createVersion(path);
            assertEquals("/testk2/testa", r3.getParentPath(), "Permenent path doesn't contanin the string");
            assertEquals(path, r3.getPath(), "Path doesn't contanin the string");

            deleteResources("/testk2");
            log.info("*************WS-API Get Collection Operation test - Passed *************");
        } catch (RegistryException e) {
            log.error("WS-API Get Collection Operation test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Get Collection Operation test- Fail:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/f1012");
        deleteResources("/f95");
        deleteResources("/c9011");
        deleteResources("/c9011");
        deleteResources("/testk");
        deleteResources("/testk2");
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


    private boolean containsComment(String pathValue, String commentText) throws Exception {

        Comment[] commentsArray = null;
        List commentTexts = new ArrayList();

        try {
            Resource commentsResource = registry.get(pathValue);
            commentsArray = (Comment[]) commentsResource.getContent();
            for (Comment comment : commentsArray) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean found = false;
        if (commentTexts.contains(commentText)) {
            found = true;
        }

        return found;
    }

    private boolean containsTag(String tagPath, String tagText) throws Exception {

        Tag[] tags = null;

        try {
            tags = registry.getTags(tagPath);
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean tagFound = false;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(tagText)) {
                tagFound = true;
                break;
            }
        }

        return tagFound;
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
