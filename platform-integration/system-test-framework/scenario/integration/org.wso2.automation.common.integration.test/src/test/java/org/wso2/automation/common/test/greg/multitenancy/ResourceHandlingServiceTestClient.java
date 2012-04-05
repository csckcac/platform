package org.wso2.automation.common.test.greg.multitenancy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserCreator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import java.rmi.RemoteException;

import static org.testng.Assert.*;


public class ResourceHandlingServiceTestClient {
    private static final Log log = LogFactory.getLog(ResourceHandlingServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static WSRegistryServiceClient registry_testUser = null;
    private static WSRegistryServiceClient registry_diffDomainUser1 = null;
    String admin_username;
    String diffDomain_username1;
    String admin_password;
    String comment1 = "check multitenancy feature 1";
    String comment2 = "check multitenancy feature 2";
    String tag1 = "multi tenancy tag 1";
    String tag2 = "multi tenancy tag 2";


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, RemoteException, LoginAuthenticationExceptionException, UserAdminException {
        int tenantId = 3;
        int diff_Domainuser = 6;
        int tenantID_testUser = 3;
        String userID = "testuser1";
        String userPassword = "test123";
        String roleName = "admin";

        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        registry_diffDomainUser1 = new RegistryProvider().getRegistry(diff_Domainuser, ProductConstant.GREG_SERVER_NAME);

        GregUserCreator GregUserCreator = new GregUserCreator();
        GregUserCreator.deleteUsers(tenantID_testUser, userID);
        GregUserCreator.addUser(tenantID_testUser, userID, userPassword, roleName);
        registry_testUser = GregUserCreator.getRegistry(tenantID_testUser, userID, userPassword);

        //Admin Tenant Details
        UserInfo tenantDetails = UserListCsvReader.getUserInfo(tenantId);
        admin_username = tenantDetails.getUserName();
        admin_password = tenantDetails.getPassword();

        //different domain user1
        UserInfo tenantDetails_diffDomainUser1 = UserListCsvReader.getUserInfo(diff_Domainuser);
        diffDomain_username1 = tenantDetails_diffDomainUser1.getUserName();
        // Delete Resource
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario add resource ", priority = 1)
    private void testAddResource() throws RegistryException {
        String path = "/d1/d2/d3/d4/r1";

        try {
            uploadResource(path);
            verifyResourceExists(path);           //assert resource exists
            addComment(path);                     //add comments
            verifyComments(path);                 // Assert comments
            addTag(path);                         // add Tag
            verifyTag(path);                      //Assert Tag paths
            removeResource();                     //Delete resource
            verifyResourceDelete(path);           //assert Resources have been deleted properly
            log.info("***********************Multi Tenancy Resource Handling Service Test Client Test - Passed******************");
        } catch (RegistryException e) {
            log.error("Multi Tenancy Resource Handling Service Test Client Test -Failed:" + e.getMessage());
            throw new RegistryException("Multi Tenancy Resource Handling Service Test Client Test -Failed:" + e.getMessage());
        }
    }


    private void uploadResource(String path) throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");
            registry.put(path, r1);
        } catch (RegistryException e) {
            log.error("Unable to upload a resource :" + e.getMessage());
            throw new RegistryException("Unable to upload a resource :" + e.getMessage());
        }
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
            if (registry_diffDomainUser1.resourceExists(resourceName)) {
                registry_diffDomainUser1.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e.getMessage());
            throw new RegistryException("deleteResources RegistryException thrown:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        //delete wsdls
        deleteResources("/d1");
    }


    private void verifyResourceExists(String path) throws RegistryException {
        try {
            assertTrue(registry.resourceExists(path), "Resource Exists :");                    //Assert admin user -admin123@wso2manualQA0006.org
            assertTrue(registry_testUser.resourceExists(path), "Resource exists:");            // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_diffDomainUser1.resourceExists(path), "Resource exists:");   // Assert differnt doamin user 1
        } catch (RegistryException e) {
            log.error("verifyResourceExists RegistryException thrown:" + e.getMessage());
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e.getMessage());
        }
    }

    private void verifyResourceDelete(String path) throws RegistryException {
        try {
            assertFalse(registry.resourceExists(path), "Resource Exists :");                       //Assert admin user -admin123@wso2manualQA0006.org
            assertFalse(registry_testUser.resourceExists(path), "Resource exists:");               // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_diffDomainUser1.resourceExists(path), "Resource exists:");       // Assert differnt doamin user 1
        } catch (RegistryException e) {
            log.error("verifyResourceDelete RegistryException thrown:" + e.getMessage());
            throw new RegistryException("verifyResourceDelete RegistryException thrown:" + e.getMessage());
        }
    }

    private void addComment(String path) throws RegistryException {
        Comment c1 = new Comment();
        c1.setResourcePath(path);
        c1.setText("This is default comment");
        c1.setUser(admin_username);
        try {
            registry.addComment(path, c1);
            registry.addComment(path, new Comment(comment1));
            registry.addComment(path, new Comment(comment2));
        } catch (RegistryException e) {
            log.error("Unable to add an comment :" + e.getMessage());
            throw new RegistryException("Unable to add an comment:" + e.getMessage());
        }
    }

    private void verifyComments(String path) throws RegistryException {
        Comment[] comments;
        try {
            comments = registry.getComments(path);
            boolean commentFound = false;
            for (Comment comment : comments) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(admin_password, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }
                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(admin_password, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }

                if (comment.getText().equals("This is default comment")) {
                    commentFound = true;
                    assertEquals(comment.getText(), "This is default comment");
                }
            }
            assertTrue(commentFound, "Comment not found");
        } catch (RegistryException e) {
            log.error("verifyComments by admin123@wso2manualQA0006.org RegistryException thrown:" + e.getMessage());
            throw new RegistryException("verifyComments by admin123@wso2manualQA0006.org RegistryException thrown:" + e.getMessage());
        }
        // verify admin user comments : testuser1@wso2manualQA0006.org
        Comment[] comments_testUser;
        try {
            comments_testUser = registry_testUser.getComments(path);
            boolean commentFound = false;
            for (Comment comment : comments_testUser) {
                if (comment.getText().equals(comment1)) {
                    commentFound = true;
                    assertEquals(comment1, comment.getText());
                    assertEquals(admin_password, comment.getUser());
                    assertEquals(path, comment.getResourcePath());

                }

                if (comment.getText().equals(comment2)) {
                    commentFound = true;
                    assertEquals(comment2, comment.getText());
                    assertEquals(admin_password, comment.getUser());
                    assertEquals(path, comment.getResourcePath());
                }

                if (comment.getText().equals("This is default comment")) {
                    commentFound = true;
                    assertEquals("This is default comment", comment.getText());
                }
            }
            assertTrue(commentFound, "Comment not found");
        } catch (RegistryException e) {
            log.error("verifyComments by testuser1@wso2manualQA0006.org RegistryException thrown:" + e.getMessage());
            throw new RegistryException("verifyComments by testuser1@wso2manualQA0006.org RegistryException thrown:" + e.getMessage());
        }
        // verify by different domain user
        Comment[] comments_diffDomainUser = new Comment[0];
        try {
            comments_diffDomainUser = registry_diffDomainUser1.getComments(path);
            // assert array lenght zero
            assertEquals(comments_diffDomainUser.length, 0, "Comments Array lenght 0 :");
            comments_diffDomainUser[0].getText();
        } catch (Exception e) {
            log.info("verifyComments by diffDomainUser Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert resource does not exists:
            assertNotNull(comments_diffDomainUser);
        }
    }

    private void addTag(String path) throws RegistryException {
        try {
            registry.applyTag(path, tag1);
            registry.applyTag(path, tag2);
        } catch (RegistryException e) {
            log.error("addTag RegistryException thrown:" + e.getMessage());
            throw new RegistryException("addTag RegistryException thrown:" + e.getMessage());
        }
    }

    private void verifyTag(String path) throws RegistryException {
        try {
            TaggedResourcePath[] tagPath_admin1 = registry.getResourcePathsWithTag(tag1);
            TaggedResourcePath[] tagPath_admin2 = registry.getResourcePathsWithTag(tag2);
            //assert admin user :admin123@wso2manualQA0006.org
            assertEquals("Tag path 1", tagPath_admin1[0].getResourcePath(), path);
            assertEquals("Tag path 1", tagPath_admin2[0].getResourcePath(), path);
            // assrt test user :testuser1@wso2manualQA0006.org
            TaggedResourcePath[] tagPath_testuser1 = registry_testUser.getResourcePathsWithTag(tag1);
            TaggedResourcePath[] tagPath_testUser2 = registry_testUser.getResourcePathsWithTag(tag2);
            assertEquals("Tag path 1", tagPath_testuser1[0].getResourcePath(), path);
            assertEquals("Tag path 1", tagPath_testUser2[0].getResourcePath(), path);
        } catch (RegistryException e) {
            log.error("verifyTag RegistryException thrown:" + e.getMessage());
            throw new RegistryException("verifyTag RegistryException thrown:" + e.getMessage());
        }
        TaggedResourcePath[] tagPath_diffDomainUser1 = new TaggedResourcePath[0];
        try {
            tagPath_diffDomainUser1 = registry_diffDomainUser1.getResourcePathsWithTag(tag1);
            tagPath_diffDomainUser1[0].getResourcePath();
        } catch (Exception e) {
            log.info("verifyTag by diffDomainUser Exception thrown:" + e.getMessage());
            //registry null exception is caught to assert resource does not exists:
            assertNull(tagPath_diffDomainUser1);
        }

    }


}
