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

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
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
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class PropertyServiceTestCase {

    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH1 = "/_system/governance/trunk/services/com/abb/abc";
    private static final String PATH2 = "/_system/governance/trunk/services/com/def/def";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ServiceManager serviceManager;

    private ListMetaDataServiceClient listMetaDataServiceClient;
    private GovernanceServiceClient governanceServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static String LC_NAME = "MultiplePromoteDemoteLC";

    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, IOException, RegistryException,
                   ResourceAdminServiceExceptionException, XMLStreamException,
                   AddServicesServiceRegistryExceptionException {

        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
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
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  userInfo.getUserName(), userInfo.getPassword());
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getBackEndUrl(),
                                            userInfo.getUserName(), userInfo.getPassword());

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);


        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                      userInfo.getUserName(), userInfo.getPassword());
        addResource();
    }


    public void addResource()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException,
                   AddServicesServiceRegistryExceptionException {
        Boolean nameExists = false;
        String serviceContent;
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "services" + File.separator + "service.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        serviceContent = parseISToString(dataHandler.getInputStream());
        governanceServiceClient.addService(AXIOMUtil.stringToOM(serviceContent));

        ServiceBean serviceBean = listMetaDataServiceClient.listServices(null);


        String[] names = serviceBean.getNames();

        System.out.println("name: " + names[0]);
        for (String name : names) {
            if (name.equalsIgnoreCase("abc")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        nameExists = false;
        serviceContent = null;

        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "services" + File.separator + "xservice.xml";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        serviceContent = parseISToString(dataHandler2.getInputStream());
        governanceServiceClient.addService(AXIOMUtil.stringToOM(serviceContent));

        ServiceBean serviceBean2 = listMetaDataServiceClient.listServices(null);


        String[] names2 = serviceBean2.getNames();

        System.out.println("name: " + names[0]);
        for (String name : names2) {
            if (name.equalsIgnoreCase("def")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);

    }


    @Test(groups = {"wso2.greg"}, description = "Add a property to a service,version it and check it")
    public void TestVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException {
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATH1, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATH1, "true").getProperties();

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
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
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
        assertEquals(null, deleteVersion(PATH1));


    }

    private String parseISToString(java.io.InputStream is) {
        java.io.DataInputStream din = new java.io.DataInputStream(is);
        StringBuilder sb = new StringBuilder();
        try {
            String line = null;
            while ((line = din.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }

    @Test(groups = {"wso2.greg"}, description = "Add a association to a service ,version it and check that association")
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


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a service ,version it and check for that dependency")
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


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a service,version it and check that tag ")
    public void TestVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", PATH1, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATH1, environment.getGreg().getSessionCookie());
        Tag[] tags1 = tagBean.getTags();

        for (Tag aTags1 : tags1) {

            if (aTags1.getTagName().equals("testTag")) {
                status = true;
            }
        }
        assertTrue(status);
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("testTag", infoServiceAdminClient.getTags(verPath,
                                                               environment.getGreg().getSessionCookie()).getTags()[0].getTagName());
        assertEquals(null, deleteVersion(PATH1));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a comment to a service file,version it and check that comment ")
    public void TestVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATH1, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertEquals(null, deleteVersion(PATH1));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a service ,version it and check ratings ")
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


    @Test(groups = {"wso2.greg"}, description = "Add description to a service ," +
                                                "version it and check description ")
    public void testVersionDescriptionLeaf()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException, GovernanceException {
        Service testService = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("abc");
            }
        })[0];
        String descS = testService.getAttribute("overview_description");
        assertTrue(testService.getAttribute("overview_description").equals("this is description 1"));

    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a service,version it and check retention ")
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


//    @Test(groups = "wso2.greg", description = "Create new life cycle")
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
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

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service, version it and check again")
    public void testAddLcToService() throws RegistryException, IOException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            LifeCycleManagementServiceExceptionException {

        testCreateNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH1, LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);

        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = null;
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();
        lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        assertEquals(null, deleteVersion(PATH1));
    }


    //Delete the created version fpr the service
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
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException {
        resourceAdminClient.deleteResource(PATH1);
        resourceAdminClient.deleteResource(PATH2);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }
}
