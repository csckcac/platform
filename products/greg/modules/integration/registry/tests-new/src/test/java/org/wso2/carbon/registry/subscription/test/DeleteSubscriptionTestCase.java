/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.util.ManagementConsoleSubscription;

public class DeleteSubscriptionTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String RESOURCE_PATH_NAME = "/";


    @BeforeClass
    public void initialize()
            throws RemoteException, LoginAuthenticationExceptionException, RegistryException {
        int userID = 0;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
    }

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"service.metadata.xml", "application/xml", "services"},
                              new Object[]{"policy.xml", "application/xml", "policy"},
                              new Object[]{"test.map", "Unknown", "mediatypes"},
                              new Object[]{"Person.xsd", "application/x-xsd+xml", "schema"},
                              new Object[]{"AmazonWebServices.wsdl", "application/wsdl+xml", "wsdl"},
        };
    }

    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String name, String type, String folder)
            throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator + folder + File.separator + name;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(RESOURCE_PATH_NAME + name, type, "testDesc", dh);
        if (!(folder.equals("wsdl") || folder.equals("schema"))) {
            assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME +
                                                              name)[0].getAuthorUserName().contains(userInfo.getUserName()));
        } else {
            assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME +
                                                              name)[0].getAuthorUserName().contains("wso2.system.user"));
        }
    }


    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dataProvider = "ResourceDataProvider", dependsOnMethods = "testAddResource")
    public void testConsoleSubscription(String name, String type, String folder) throws Exception {
        assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME + name,
                                                      "ResourceUpdated", environment, userInfo));

    }

    @Test(groups = "wso2.greg", description = "unsubscribe", dataProvider = "ResourceDataProvider",
          dependsOnMethods = {"testAddResource", "testConsoleSubscription"})
    public void testDeleteSubscription(String name, String type, String folder) throws Exception {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        String sessionID = environment.getGreg().getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME + name, sessionID);
        infoServiceAdminClient.unsubscribe(RESOURCE_PATH_NAME + name, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME + name, sessionID);
        assertTrue(sBean.getSubscriptionInstances() == null);
    }

    @Test(groups = "wso2.greg", description = "Get Management Console Notification for Collection")
    public void testCollectionConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME, "CollectionUpdated", environment, userInfo));

    }

    @Test(groups = "wso2.greg", description = "unsubscribe", dependsOnMethods = "testCollectionConsoleSubscription")
    public void testDeleteCollectionSubscription() throws Exception {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        String sessionID = environment.getGreg().getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME, sessionID);
        infoServiceAdminClient.unsubscribe(RESOURCE_PATH_NAME, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME, sessionID);
        assertTrue(sBean.getSubscriptionInstances() == null);
    }

    @AfterClass()
    public void clean() throws RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "service.metadata.xml");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "policy.xml");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "Person.xsd");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "test.map");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "AmazonWebServices.wsdl");

    }
}
