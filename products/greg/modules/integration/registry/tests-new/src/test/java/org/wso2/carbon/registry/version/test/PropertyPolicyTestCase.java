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
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
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
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
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
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PropertyPolicyTestCase {

    private static String LC_NAME = "MultiplePromoteDemoteLC";
    private org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean lifeCycle;

    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private String PATH1;
    private String PATH2;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         ResourceAdminServiceExceptionException,
                                         MalformedURLException, RegistryException {

        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

        listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                        userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient = new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    userInfo.getUserName(), userInfo.getPassword());

        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            userInfo.getUserName(), userInfo.getPassword());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  userInfo.getUserName(), userInfo.getPassword());
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                      userInfo.getUserName(), userInfo.getPassword());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        addPolicy();
    }


    public void addPolicy()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        Boolean nameExists = false;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addPolicy("desc 1", dataHandler);
        PolicyBean policyBean = listMetaDataServiceClient.listPolicies();
        String[] names = policyBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("policy.xml")) {
                nameExists = true;
            }
        }

        assertTrue(nameExists);

        String[] policyNames = listMetaDataServiceClient.listPolicies().getPath();
        for (String policyName : policyNames) {
            if (policyName.contains("policy.xml")) {

                PATH1 = "/_system/governance" + policyName;
                System.out.println("PATH1: " + PATH1);
            }
        }


        nameExists = false;
        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "policy" + File.separator + "UTPolicy.xml";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addPolicy("desc 1", dataHandler2);
        PolicyBean policyBean2 = listMetaDataServiceClient.listPolicies();
        String[] names2 = policyBean2.getName();

        for (String name : names2) {
            if (name.equalsIgnoreCase("UTPolicy.xml")) {
                nameExists = true;
            }
        }

        assertTrue(nameExists);

        String[] policyNames2 = listMetaDataServiceClient.listPolicies().getPath();
        for (int i = 0; i < policyNames2.length; i++) {
            if (policyNames2[i].contains("UTPolicy.xml")) {
                PATH2 = "/_system/governance" + policyNames2[i];
            }
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Add a property to a WSDL,version it and check it")
    public void TestVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException {
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATH1, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATH1, "true").getProperties();

        for (int i = 0; i < properties1.length; i++) {

            if (properties1[i].getKey().equals("name1")) {
                count++;
            }
            if (properties1[i].getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        Property[] properties2 = propertiesAdminServiceClient.getProperty(verPath, "true").getProperties();

        for (int i = 0; i < properties2.length; i++) {

            if (properties2[i].getKey().equals("name1")) {
                count++;
            }
            if (properties2[i].getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(PATH1));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a association to a WSDL file,version it and check that WSDL")
    public void TestVersionAssociation()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATH1, "usedBy", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        System.out.println("Association tree: " + resourcePath);
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH2));
        assertEquals(null, deleteVersion(PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a WSDL ,version it and check for that dependency")
    public void TestVersionDependency()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATH1, "depends", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "depends");
        String resourcePath = associationTreeBean.getAssociationTree();
        System.out.println("Association tree: " + resourcePath);
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "depends").getAssociationTree().contains(PATH2));
        assertEquals(null, deleteVersion(PATH1));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a WSDL ,version it and check that tag ")
    public void TestVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", PATH1, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATH1, environment.getGreg().getSessionCookie());
        Tag[] tags1 = tagBean.getTags();

        for (int i = 0; i < tags1.length; i++) {

            if (tags1[i].getTagName().equals("testTag")) {
                status = true;
            }
        }
        assertTrue(status);
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("testTag", infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).
                getTags()[0].getTagName());
        assertEquals(null, deleteVersion(PATH1));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a WSDL file,version it and check that comment ")
    public void TestVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATH1, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment",
                     infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).
                             getComments()[0].getContent());
        assertEquals(null, deleteVersion(PATH1));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a WSDL ,version it and check ratings ")
    public void TestVersionRatingR() throws RegistryException, RegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATH1, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATH1, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertEquals(null, deleteVersion(PATH1));

    }


    @Test(groups = {"wso2.greg"}, description = "Add description to a WSDL file,version it and check description ")
    public void testVersionDescription()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {
        boolean status = false;
        ResourceData[] resource1 = resourceAdminClient.getResource(PATH1);
        for (int i = 0; i < resource1.length; i++) {
            if (resource1[i].getDescription().equals("desc 1")) {
                status = true;
            }
        }
        assertTrue(status);
        status = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();

        ResourceData[] resource2 = resourceAdminClient.getResource(verPath);
        for (int i = 0; i < resource2.length; i++) {
            if (resource2[i].getDescription().equals("desc 1")) {
                status = true;
            }
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a WSDL file,version it and check retention ")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATH1, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATH1).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATH1).getToDate());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertEquals(null, deleteVersion(PATH1));

    }

    private void testAddNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileReader.readFile(resourcePath);
        Thread.sleep(20000);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a WSDL")
    public void testAddLc() throws RegistryException, IOException,
                                   CustomLifecyclesChecklistAdminServiceExceptionException,
                                   ListMetadataServiceRegistryExceptionException,
                                   ResourceAdminServiceExceptionException,
                                   LifeCycleManagementServiceExceptionException,
                                   InterruptedException {

        testAddNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH1, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);

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
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
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
        assertEquals(null, deleteVersion(PATH1));
    }


    @AfterClass
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException {
        resourceAdminClient.deleteResource(PATH1);
        resourceAdminClient.deleteResource(PATH2);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
        String snapshotId = String.valueOf(versionNo);
        resourceAdminClient.deleteVersionHistory(path, snapshotId);
        VersionPath[] vp2 = null;
        vp2 = resourceAdminClient.getVersionPaths(path);
        return vp2;
    }

}
