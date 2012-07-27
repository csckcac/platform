/*
Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/


package org.wso2.carbon.registry.activity.search;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

public class ActivitySearchByFilterTestCase {

    private static final Log log = LogFactory.getLog(ActivitySearchByFilterTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationalServiceClient;

    private ManageEnvironment environment;
    UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

        activityAdminServiceClient =
                new ActivityAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());

        relationalServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
    }

//

    @Test(groups = {"wso2.greg"})
    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath + resourceName)[0].
                getAuthorUserName().contains(userInfo.getUserName()));


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void activitySearchFilterByAdd() throws InterruptedException, MalformedURLException,
                                                   ResourceAdminServiceExceptionException,
                                                   RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.ADD_RESOURCE, 0).getActivity());
    }

    @Test(groups = "wso2.greg", description = "add property", dependsOnMethods = "addResource")
    public void testPropertyAddition() throws Exception {
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient.setProperty(wsdlPath + resourceName, "Author", "TestValuse");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"testPropertyAddition"})
    public void activitySearchFilterByUpdate() throws InterruptedException, MalformedURLException,
                                                      ResourceAdminServiceExceptionException,
                                                      RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.UPDATE_RESOURCE, 0).getActivity());


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"activitySearchFilterByUpdate"})
    public void activitySearchFilterByDelete() throws InterruptedException, MalformedURLException,
                                                      ResourceAdminServiceExceptionException,
                                                      RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.DELETE_RESOURCE, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void addResourceAgain() throws InterruptedException, MalformedURLException,
                                          ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResourceAgain"})
    public void restoreResource() throws InterruptedException, MalformedURLException,
                                         ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.createVersion(wsdlPath + resourceName);
        VersionsBean bean = resourceAdminServiceClient.getVersionsBean(wsdlPath + resourceName);
        for (VersionPath path : bean.getVersionPaths()) {
            if (path.isActiveResourcePathSpecified()) {
                resourceAdminServiceClient.restoreVersion(path.getCompleteVersionPath());
            }
        }

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"restoreResource"})
    public void activitySearchFilterByRestores() throws InterruptedException, MalformedURLException,
                                                        ResourceAdminServiceExceptionException,
                                                        RemoteException,
                                                        RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.RESTORE_RESOURCES, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void addComment() throws RegistryException, AxisFault {
        infoServiceAdminClient.addComment("this is comment", "", environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addComment"})
    public void activitySearchFilterByComment() throws InterruptedException, MalformedURLException,
                                                       ResourceAdminServiceExceptionException,
                                                       RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.COMMENTS, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void addTag() throws RegistryException, AxisFault {
        infoServiceAdminClient.addTag("this is tag", wsdlPath, environment.getGreg().getSessionCookie());
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addTag"})
    public void activitySearchFilterByTagging() throws InterruptedException, MalformedURLException,
                                                       ResourceAdminServiceExceptionException,
                                                       RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.TAGGING, 0).getActivity());

    }

    @Test(groups = {"wso2.greg"})
    public void rateResource() throws RegistryException, AxisFault,
                                      org.wso2.carbon.registry.info.stub.RegistryExceptionException {
        infoServiceAdminClient.rateResource("1", wsdlPath + resourceName, environment.getGreg().getSessionCookie());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"rateResource"})
    public void activitySearchFilterByRating() throws InterruptedException, MalformedURLException,
                                                      ResourceAdminServiceExceptionException,
                                                      RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.RATINGS, 0).getActivity());
    }


    @Test(groups = {"wso2.greg"})
    public void createSymbolicLink() throws RegistryException, RemoteException,
                                            org.wso2.carbon.registry.info.stub.RegistryExceptionException,
                                            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.addSymbolicLink("/_system/governance/trunk/wsdls/eu/dataaccess/",
                                                   "SymbolicName", "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/" +
                                                                   resourceName);
    }


    @Test(groups = {"wso2.greg"})
    public void activitySearchFilterBySymbolLink()
            throws InterruptedException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.CREATE_SYMBOLIC_LINK, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void deleteLink() throws RegistryException, RemoteException,
                                    org.wso2.carbon.registry.info.stub.RegistryExceptionException,
                                    ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(wsdlPath + resourceName);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"deleteLink"})
    public void activitySearchFilterByRemoveLink()
            throws InterruptedException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.REMOVE_LINK, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void addResourceForAssociation() throws InterruptedException, MalformedURLException,
                                                   ResourceAdminServiceExceptionException,
                                                   RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "info.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + "info.wsdl",
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource", "addResourceForAssociation"})
    public void addAssociation() throws InterruptedException, MalformedURLException,
                                        ResourceAdminServiceExceptionException, RemoteException,
                                        AddAssociationRegistryExceptionException {
        relationalServiceClient.addAssociation(wsdlPath + resourceName, "depends", "/_system/governance/", "add");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociation"})
    public void activitySearchFilterByAddAssociation()
            throws InterruptedException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.ADD_ASSOCIATION, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void removeAssociation() throws InterruptedException, MalformedURLException,
                                           ResourceAdminServiceExceptionException, RemoteException,
                                           AddAssociationRegistryExceptionException {
        relationalServiceClient.addAssociation(wsdlPath + resourceName, "depends", "/_system/governance/", "remove");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"removeAssociation"})
    public void activitySearchFilterByRemoveAssociation()
            throws InterruptedException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.REMOVE_ASSOCIATION, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"removeAssociation"})
    public void activitySearchFilterByAssociationAspect()
            throws InterruptedException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               ActivitySearchUtil.ASSOCIATE_ASPECT, 0).getActivity());
    }
}




