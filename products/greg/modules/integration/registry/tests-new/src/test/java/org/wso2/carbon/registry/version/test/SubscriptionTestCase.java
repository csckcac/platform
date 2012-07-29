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
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class SubscriptionTestCase {
    private UserInfo userInfo;
    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH1 = "/testResource";
    private String COLLECTION_PATH_ROOT = "/";
    private InfoServiceAdminClient infoServiceAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            userInfo.getUserName(), userInfo.getPassword());

        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "dir1", "text/plain", "Desc1");


        testAddRole();
    }

    @Test(groups = {"wso2.greg"}, description = "Subscribe for a resource and version it")
    public void testSubscriptionVersioning() throws Exception {
        SubscriptionBean sb1 = null;
        SubscriptionBean sb2 = null;

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new java.net.URL("file:///" + path));
        resourceAdminClient.addResource(PATH1, "text/plain", "desc", dataHandler);


        assertTrue(resourceAdminClient.getResource(PATH1)[0].getAuthorUserName().contains(userInfo.getUserName()));

        // testAddRole();
        SubscriptionBean bean = testMgtConsoleResourceSubscription();
        assertTrue(bean.getSubscriptionInstances() != null);
        sb1 = infoServiceAdminClient.getSubscriptions(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("admin", sb1.getUserName());
        resourceAdminClient.createVersion(PATH1);
        sb1 = null;
        sb1 = infoServiceAdminClient.getSubscriptions(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("admin", sb1.getUserName());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        sb2 = infoServiceAdminClient.getSubscriptions(verPath, environment.getGreg().getSessionCookie());
        assertEquals(null, deleteVersion(PATH1));

        // logViewerClient.getLogs("warn", "Versioned resources cannot have subscriptions, instead returns the subscription from the actual resource");

    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with subscriptions, version and restore to the previous version ")
    public void testSubscriptionRestore()
            throws Exception, RemoteException, MalformedURLException, RegistryException,
                   RegistryExceptionException {
        String PATH = COLLECTION_PATH_ROOT + "dir1";
        SubscriptionBean sb1 = null;
        SubscriptionBean sb2 = null;


        SubscriptionBean bean = testMgtConsoleResourceSubscription();
        assertTrue(bean.getSubscriptionInstances() != null);
        sb1 = infoServiceAdminClient.getSubscriptions(PATH, environment.getGreg().getSessionCookie());
        assertEquals("admin", sb1.getUserName());
        resourceAdminClient.createVersion(PATH);
        sb1 = null;
        sb1 = infoServiceAdminClient.getSubscriptions(PATH, environment.getGreg().getSessionCookie());
        assertEquals("admin", sb1.getUserName());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        resourceAdminClient.restoreVersion(verPath);
        sb2 = infoServiceAdminClient.getSubscriptions(PATH, environment.getGreg().getSessionCookie());
        assertEquals("admin", sb2.getUserName());
        assertEquals(null, deleteVersion(PATH));

    }


    public SubscriptionBean testMgtConsoleResourceSubscription()
            throws RegistryException, RemoteException {
        return infoServiceAdminClient.subscribe(PATH1, "work://SubscriptionTestRole", "ResourceUpdated", environment.getGreg().getSessionCookie());
    }

    public void testAddRole() throws Exception {
        UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        userManagementClient.addRole("SubscriptionTestRole", new String[]{userInfo.getUserName()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("SubscriptionTestRole"));
    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        int length = resourceAdminClient.getVersionPaths(path).length;
        for (int i = 0; i < length; i++) {
            long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
            String snapshotId = String.valueOf(versionNo);
            resourceAdminClient.deleteVersionHistory(path, snapshotId);
        }
        VersionPath[] vp2 = null;
        vp2 = resourceAdminClient.getVersionPaths(path);

        return vp2;
    }

    @AfterClass
    public void clear() throws ResourceAdminServiceExceptionException, RemoteException {


        resourceAdminClient.deleteResource(COLLECTION_PATH_ROOT + "dir1");
        resourceAdminClient.deleteResource(PATH1);


    }

}
