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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
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
import static org.testng.Assert.assertTrue;

public class PropertyEndpointTestCase {


    private static Registry governance = null;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final Log log = LogFactory.getLog(ListMetaDataServiceClient.class);
    private static WSRegistryServiceClient registry = null;
    private Endpoint endpoint;
    private static final String PATHROOT = "/testResource1";

    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ManageEnvironment environment;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;


    private static String LC_NAME = "MultiplePromoteDemoteLC";

    @BeforeClass
    public void initializeRegistry()
            throws RegistryException, RemoteException, LoginAuthenticationExceptionException,
                   MalformedURLException, ResourceAdminServiceExceptionException {

        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                        userInfo.getUserName(), userInfo.getPassword());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  userInfo.getUserName(), userInfo.getPassword());
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                      userInfo.getUserName(), userInfo.getPassword());
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        relationAdminServiceClient = new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    userInfo.getUserName(), userInfo.getPassword());

        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            userInfo.getUserName(), userInfo.getPassword());

        String endpoint_url = "http://DoNotCallRegistryUnique";

        endpoint = createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(endpoint.getPath()), "Endpoint Resource Does not exists :");


        //Create a resource at root level

        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(PATHROOT, "text/plain", "desc", dataHandler1);
        Assert.assertTrue(resourceAdminClient.getResource(PATHROOT)[0].getAuthorUserName().contains(userInfo.getUserName()));
    }


    private Endpoint createEndpoint(String endpoint_url) throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint1;
        try {
            endpoint1 = endpointManager.newEndpoint(endpoint_url);
            endpoint1.associateRegistry(registry);
            endpoint1.addAttribute("status1", "QA");
            endpoint1.addAttribute("status2", "Dev");
            endpointManager.addEndpoint(endpoint1);
            log.info("Endpoint was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable add Endpoint:" + e);
            throw new GovernanceException("Unable to add Endpoint:" + e);
        }
        return endpoint1;
    }

    @Test(groups = {"wso2.greg"}, description = "Add a property to an Endpoint,version it and check it")
    public void TestVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException {
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(endpoint.getPath(), "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(endpoint.getPath(), "true").getProperties();

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
        Assert.assertTrue(status);
        status = false;
        count = 0;
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
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
        Assert.assertTrue(status);
        assertEquals(null, deleteVersion(endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a association to an endpoint,version it and check that WSDL")
    public void TestVersionAssociation()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(endpoint.getPath(), "usedBy", PATHROOT, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(endpoint.getPath(), "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        System.out.println("Association tree: " + resourcePath);
        Assert.assertTrue(resourcePath.contains(PATHROOT));
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        Assert.assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATHROOT));


        assertEquals(null, deleteVersion(endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to an endpoint ,version it and check for that dependency")
    public void TestVersionDependency()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(endpoint.getPath(), "depends", PATHROOT, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(endpoint.getPath(), "depends");
        String resourcePath = associationTreeBean.getAssociationTree();
        System.out.println("Association tree: " + resourcePath);
        Assert.assertTrue(resourcePath.contains(PATHROOT));
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        Assert.assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "depends").getAssociationTree().contains(PATHROOT));
        assertEquals(null, deleteVersion(endpoint.getPath()));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to an endpoint ,version it and check that tag ")
    public void TestVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", endpoint.getPath(), environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(endpoint.getPath(), environment.getGreg().getSessionCookie());
        Tag[] tags1 = tagBean.getTags();

        for (Tag aTags1 : tags1) {

            if (aTags1.getTagName().equals("testTag")) {
                status = true;
            }
        }
        Assert.assertTrue(status);
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("testTag", infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags()[0].getTagName());
        assertEquals(null, deleteVersion(endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to an endpoint,version it and check that comment ")
    public void TestVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", endpoint.getPath(),
                                          environment.getGreg().getSessionCookie());
        CommentBean commentBean =
                infoServiceAdminClient.getComments(endpoint.getPath(),
                                                   environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertEquals(null, deleteVersion(endpoint.getPath()));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to an endpoint ,version it and check ratings ")
    public void TestVersionRatingR() throws RegistryException, RegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", endpoint.getPath(), sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(endpoint.getPath(), sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertEquals(null, deleteVersion(endpoint.getPath()));

    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to an endpoint,version it and check retention ")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, GovernanceException {
        propertiesAdminServiceClient.setRetentionProperties(endpoint.getPath(), "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(endpoint.getPath()).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(endpoint.getPath()).getToDate());
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertEquals(null, deleteVersion(endpoint.getPath()));

    }

    //    @Test(groups = "wso2.greg", description = "Create new life cycle")
    private void testCreateNewLifeCycle()
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
        Assert.assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to an endpoint")
    public void testAddLc() throws RegistryException, IOException,
                                   CustomLifecyclesChecklistAdminServiceExceptionException,
                                   ListMetadataServiceRegistryExceptionException,
                                   ResourceAdminServiceExceptionException,
                                   LifeCycleManagementServiceExceptionException {

        testCreateNewLifeCycle();
        wsRegistryServiceClient.associateAspect(endpoint.getPath(), LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(endpoint.getPath());

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        Assert.assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        Assert.assertTrue(lcStatus);
        assertEquals(null, deleteVersion(endpoint.getPath()));
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

    @AfterClass
    public void clear()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException,
                   LifeCycleManagementServiceExceptionException {
        System.out.println("XXXXXXXX inside clear");
        resourceAdminClient.deleteResource(PATHROOT);
        resourceAdminClient.deleteResource(endpoint.getPath());
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }
}
