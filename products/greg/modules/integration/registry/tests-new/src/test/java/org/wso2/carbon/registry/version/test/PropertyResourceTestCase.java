/*
* Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.version.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.governance.utils.FileReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

//import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;


public class PropertyResourceTestCase {
    private UserInfo userInfo;

    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATHROOT = "/testResource1";
    private static final String PATHLEAF = "/_system/config/testResource2";
    private static final String PATH = "/_system/config/testResource3";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static String LC_NAME = "MultiplePromoteDemoteLC";

    private org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean lifeCycle;


    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   ResourceAdminServiceExceptionException, MalformedURLException {

        int userId = 1;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                        userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient = new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    userInfo.getUserName(), userInfo.getPassword());

        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            userInfo.getUserName(), userInfo.getPassword());
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                      userInfo.getUserName(), userInfo.getPassword());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  userInfo.getUserName(), userInfo.getPassword());

        addResource();
    }


    private void addResource()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(PATHROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(PATHROOT)[0].getAuthorUserName().contains(userInfo.getUserName()));
        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(PATHLEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(PATHLEAF)[0].getAuthorUserName().contains(userInfo.getUserName()));

        String path3 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler3 = new DataHandler(new URL("file:///" + path3));
        resourceAdminClient.addResource(PATH, "text/plain", "desc", dataHandler3);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(userInfo.getUserName()));

    }

    @Test(groups = {"wso2.greg"}, description = "Add a property to a resource,version it and check that property at root level")
    public void TestVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   InterruptedException {

        /*propertiesAdminServiceClient.setProperty(PATHROOT, "name1", "value1");

        Assert.assertTrue(propertiesAdminServiceClient.getProperty(PATHROOT,
                "true").getProperties()[0].getKey().equals("name1"));
        Assert.assertTrue(propertiesAdminServiceClient.getProperty(PATHROOT,
                "true").getProperties()[0].getValue().equals("value1"));

        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersion(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();

        Assert.assertTrue(propertiesAdminServiceClient.getProperty(verPath,
                "true").getProperties()[0].getKey().equals("name1"));
        Assert.assertTrue(propertiesAdminServiceClient.getProperty(verPath,
                "true").getProperties()[0].getValue().equals("value1"));

        assertEquals(null, deleteVersion(PATHROOT));*/

        ///////////////////////////////////////////////////////


        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATHROOT, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATHROOT, "true").getProperties();

        for (Property aProperties1 : properties1) {

            if (aProperties1.getKey().equals("name1")) {
                count++;
            }
            if (aProperties1.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;

        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        Property[] properties2 = propertiesAdminServiceClient.getProperty(verPath, "true").getProperties();

        for (Property aProperties2 : properties2) {

            if (aProperties2.getKey().equals("name1")) {
                count++;
            }
            if (aProperties2.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(PATHROOT));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a property to a resource,version it and check that property at leaf level",
          dependsOnMethods = "TestVersionPropertyRoot")
    public void TestVersionPropertyLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException {

        /*   propertiesAdminServiceClient.setProperty(PATHLEAF, "name1", "value1");

    Assert.assertTrue(propertiesAdminServiceClient.getProperty(PATHLEAF,
             "true").getProperties()[0].getKey().equals("name1"));
     Assert.assertTrue(propertiesAdminServiceClient.getProperty(PATHLEAF,
             "true").getProperties()[0].getValue().equals("value1"));

     resourceAdminClient.createVersion(PATHLEAF);
     VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);

     String verPath = vp1[0].getCompleteVersionPath();

     Assert.assertTrue(propertiesAdminServiceClient.getProperty(verPath,
             "true").getProperties()[0].getKey().equals("name1"));
     Assert.assertTrue(propertiesAdminServiceClient.getProperty(verPath,
             "true").getProperties()[0].getValue().equals("value1"));


     assertEquals(null, deleteVersion(PATHLEAF));  */

        ////////////////////////////////////////////////////////////


        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATHLEAF, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATHLEAF, "true").getProperties();

        for (Property aProperties1 : properties1) {

            if (aProperties1.getKey().equals("name1")) {
                count++;
            }
            if (aProperties1.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;

        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        Property[] properties2 = propertiesAdminServiceClient.getProperty(verPath, "true").getProperties();

        for (Property aProperties2 : properties2) {

            if (aProperties2.getKey().equals("name1")) {
                count++;
            }
            if (aProperties2.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(PATHLEAF));


    }


    @Test(groups = {"wso2.greg"},
          description = "Add a association to a resource  at root level,version it and check that association",
          dependsOnMethods = "TestVersionPropertyLeaf")
    public void TestVersionAssociationRoot()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATHROOT, "usedBy", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHROOT, "usedBy");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHROOT, resourcePath);
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getResourcePath());


        assertEquals(null, deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"},
          description = "Add a association to a resource at leaf level,version it and check that association",
          dependsOnMethods = "TestVersionAssociationRoot")
    public void TestVersionAssociationLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATHLEAF, "usedBy", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHLEAF, "usedBy");
        System.out.println("Association tree: " + associationTreeBean.getAssociationTree());

        String resourcePath = associationTreeBean.getResourcePath();
        System.out.println("resource path: " + resourcePath);
        assertEquals(PATHLEAF, resourcePath);
        resourceAdminClient.createVersion(PATHLEAF);

        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getResourcePath());

        assertEquals(null, deleteVersion(PATHLEAF));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a resource at root level,version it and check for that dependency",
          dependsOnMethods = "TestVersionAssociationLeaf")
    public void TestVersionDependencyRoot()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATHROOT, "depends", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHROOT, "depends");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHROOT, resourcePath);
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "depends").getResourcePath());
        assertEquals(null, deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a resource at leaf level,version it and check for that dependency ",
          dependsOnMethods = "TestVersionDependencyRoot")
    public void TestVersionDependencyLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATHLEAF, "depends", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHLEAF, "depends");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHLEAF, resourcePath);
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "depends").getResourcePath());
        assertEquals(null, deleteVersion(PATHLEAF));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a resource at root level,version it and check that tag ",
          dependsOnMethods = "TestVersionDependencyLeaf")
    public void TestVersionTagRoot()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        infoServiceAdminClient.addTag("testTag", PATHROOT, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATHROOT, environment.getGreg().getSessionCookie());
        Tag[] tags = tagBean.getTags();
        System.out.println("Tag name:  " + tags[0].getTagName());
        assertEquals("testTag", tags[0].getTagName());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("testTag", infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags()[0].getTagName());
        assertEquals(null, deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a tag to a resource at leaf level,version it and check that tag ",
          dependsOnMethods = "TestVersionTagRoot")
    public void TestVersionTagLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException,
                   RegistryExceptionException {
        infoServiceAdminClient.addTag("testTag", PATHLEAF, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATHLEAF, environment.getGreg().getSessionCookie());
        Tag[] tags = tagBean.getTags();
        System.out.println("Tag name:  " + tags[0].getTagName());
        assertEquals("testTag", tags[0].getTagName());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("testTag", infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags()[0].getTagName());
        assertEquals(null, deleteVersion(PATHLEAF));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a resource at root level,version it and check that comment ",
          dependsOnMethods = "TestVersionTagLeaf")
    public void TestVersionCommentRoot()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATHROOT, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATHROOT, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertEquals(null, deleteVersion(PATHROOT));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a resource at leaf level,version it and check that comment ",
          dependsOnMethods = "TestVersionCommentRoot")
    public void TestVersionCommentLeaf()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        infoServiceAdminClient.addComment("This is a comment", PATHLEAF, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATHLEAF, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertEquals(null, deleteVersion(PATHLEAF));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a resource at root level,version it and check ratings",
          dependsOnMethods = "TestVersionCommentLeaf")
    public void TestVersionRatingRoot() throws RegistryException, RegistryExceptionException,
                                               ResourceAdminServiceExceptionException,
                                               RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATHROOT, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATHROOT, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertEquals(null, deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add ratings to a resource at leaf level,version it and check ratings ",
          dependsOnMethods = "TestVersionRatingRoot")
    public void TestVersionRatingLeaf() throws RegistryException, RegistryExceptionException,
                                               ResourceAdminServiceExceptionException,
                                               RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATHLEAF, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATHLEAF, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertEquals(null, deleteVersion(PATHLEAF));

    }

    @Test(groups = {"wso2.greg"}, description = "Add description to a resource at root level,version it and check description ",
          dependsOnMethods = "TestVersionRatingLeaf")
    public void testVersionDescriptionRoot()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {

        assertEquals("desc", resourceAdminClient.getResource(PATHROOT)[0].getDescription());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("desc", resourceAdminClient.getResource(verPath)[0].getDescription());
        assertEquals(null, deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"}, description = "Add description to a resource at leaf level,version it and check description ",
          dependsOnMethods = "testVersionDescriptionRoot")
    public void testVersionDescriptionLeaf()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {

        assertEquals("desc", resourceAdminClient.getResource(PATHLEAF)[0].getDescription());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("desc", resourceAdminClient.getResource(verPath)[0].getDescription());
        assertEquals(null, deleteVersion(PATHLEAF));
    }

    @Test(groups = {"wso2.greg"}, description = "Add retention to a resource at root level,version it and check retention ",
          dependsOnMethods = "testVersionDescriptionLeaf")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATHROOT, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATHROOT).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATHROOT).getToDate());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertEquals(null, deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a resource at leaf level,version it and check retention ",
          dependsOnMethods = "testVersionDescriptionLeaf")
    public void testVersionRetentionLeaf()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATHLEAF, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATHLEAF).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATHLEAF).getToDate());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertEquals(null, deleteVersion(PATHLEAF));

    }


    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testVersionRetentionLeaf")
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileReader.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeClycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a resource at root level",
          dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcRoot() throws RegistryException, IOException,
                                       CustomLifecyclesChecklistAdminServiceExceptionException,
                                       ListMetadataServiceRegistryExceptionException,
                                       ResourceAdminServiceExceptionException,
                                       LifeCycleManagementServiceExceptionException {

        wsRegistryServiceClient.associateAspect(PATHROOT, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATHROOT);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        assertEquals(null, deleteVersion(PATHROOT));
    }


    @Test(groups = "wso2.greg", description = "Add lifecycle to a resource at leaf level", dependsOnMethods = "testAddLcRoot")
    public void testAddLcLeaf() throws RegistryException, IOException,
                                       CustomLifecyclesChecklistAdminServiceExceptionException,
                                       ListMetadataServiceRegistryExceptionException,
                                       ResourceAdminServiceExceptionException,
                                       LifeCycleManagementServiceExceptionException {

        wsRegistryServiceClient.associateAspect(PATHLEAF, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATHLEAF);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        assertEquals(null, deleteVersion(PATHLEAF));
    }

    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException {
        resourceAdminClient.deleteResource(PATHLEAF);
        resourceAdminClient.deleteResource(PATHROOT);
        resourceAdminClient.deleteResource(PATH);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
        String snapshotId = String.valueOf(versionNo);
        resourceAdminClient.deleteVersionHistory(path, snapshotId);
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(path);
        return vp2;
    }
}


