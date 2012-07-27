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
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.JMXClient;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

public class LeafLevelResourceSubscriptionTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private String loggedInSessionCookie = "";
    private JMXClient jmxClient;
    private String resourcePathName = "/_system/";

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"testresource.txt"}, new Object[]{"pom.xml"}, new Object[]{"Person.xsd"}
        };
    }


    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        int userID = 0;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        loggedInSessionCookie = environment.getGreg().getSessionCookie();
    }


    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String resourceName)
            throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + resourceName;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(resourcePathName + resourceName, "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(resourcePathName + resourceName)[0].getAuthorUserName().contains(userInfo.getUserName()));
    }


    @Test(groups = "wso2.greg", description = "Add role")
    public void testAddRole() throws Exception {
        UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        userManagementClient.addRole("RoleSubscriptionTest", new String[]{userInfo.getUserName()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSubscriptionTest"));
    }

    @Test(groups = "wso2.greg", description = "ManagementConsole subscription", dependsOnMethods = {"testAddRole", "testAddResource"}, dataProvider = "ResourceDataProvider")
    public void testMgtConsoleResourceSubscription(String resourceName)
            throws RegistryException, RemoteException {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        SubscriptionBean bean = infoServiceAdminClient.subscribe(resourcePathName + resourceName, "work://RoleSubscriptionTest", "ResourceUpdated", loggedInSessionCookie);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    @Test(groups = "wso2.greg", description = "JMX subscription", dependsOnMethods = "testAddResource", dataProvider = "ResourceDataProvider")
    public void testJMXResourceSubscription(String resourceName)
            throws RegistryException, RemoteException {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        SubscriptionBean bean = infoServiceAdminClient.subscribe(resourcePathName + resourceName, "jmx://", "ResourceUpdated", loggedInSessionCookie);
        assertTrue(bean.getSubscriptionInstances() != null);
    }


    @Test(groups = "wso2.greg", description = "Update resource",
          dependsOnMethods = {"testMgtConsoleResourceSubscription", "testJMXResourceSubscription"})
    public void testUpdateResourceOne() throws Exception {
        jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword());
        String resourceName = "testresource.txt";
        jmxClient.registerNotificationListener(resourcePathName + resourceName);
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 userInfo.getUserName(), userInfo.getPassword());

        propertiesAdminServiceClient.setProperty(resourcePathName + resourceName, "TestProperty", "TestValue");

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

        assertTrue(resourceAdminServiceClient.getProperty(resourcePathName + resourceName, "TestProperty").equals("TestValue"));
        jmxClient.getNotifications();
    }


    @Test(groups = "wso2.greg", description = "Get JMX Notification", dependsOnMethods = {"testUpdateResourceOne"})
    public void testGetJMXNotificationResourceOne() {
        assertTrue(JMXClient.isSuccess());
    }

    @Test(groups = "wso2.greg.update.root.resource", description = "Update resource", dependsOnMethods = {"testMgtConsoleResourceSubscription", "testJMXResourceSubscription"})
    public void testUpdateResourceTwo() throws Exception {
        jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword());
        String resourceName = "pom.xml";
        jmxClient.registerNotificationListener(resourcePathName + resourceName);
        PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient.setProperty(resourcePathName + resourceName, "TestProperty", "TestValue");
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        assertTrue(resourceAdminServiceClient.getProperty(resourcePathName + resourceName, "TestProperty").equals("TestValue"));
        jmxClient.getNotifications();
    }


    @Test(groups = "wso2.greg", description = "Get JMX Notification", dependsOnMethods = {"testUpdateResourceTwo"})
    public void testGetJMXNotificationResourceTwo() {
        assertTrue(JMXClient.isSuccess());
    }

    @Test(groups = "wso2.greg.update.root.resource", description = "Update resource", dependsOnMethods = {"testMgtConsoleResourceSubscription", "testJMXResourceSubscription"})
    public void testUpdateResourceThree() throws Exception {
        jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword());
        String resourceName = "Person.xsd";
        jmxClient.registerNotificationListener(resourcePathName + resourceName);
        PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient.setProperty(resourcePathName + resourceName, "TestProperty", "TestValue");
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        assertTrue(resourceAdminServiceClient.getProperty(resourcePathName + resourceName, "TestProperty").equals("TestValue"));
        jmxClient.getNotifications();
    }


    @Test(groups = "wso2.greg", description = "Get JMX Notification", dependsOnMethods = {"testUpdateResourceThree"})
    public void testGetJMXNotificationThree() {
        assertTrue(JMXClient.isSuccess());
    }


    @Test(groups = "wso2.greg", description = "Get Notification", dependsOnMethods = {"testUpdateResourceOne", "testUpdateResourceTwo", "testUpdateResourceThree"}, dataProvider = "ResourceDataProvider")
    public void testGetNotification(String resourceName)
            throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault,
                   InterruptedException {
        boolean success = false;
        HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

        for (WorkItem workItem : workItems) {
            if ((workItem.getPresentationSubject().toString()).equals("The resource at path " + resourcePathName + resourceName + " was updated.")) {
                success = true;
                break;
            }
        }
        assertTrue(success);
    }


    @AfterClass()
    public void clean() throws Exception {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        resourceAdminServiceClient.deleteResource(resourcePathName + "testresource.txt");
        resourceAdminServiceClient.deleteResource(resourcePathName + "pom.xml");
        resourceAdminServiceClient.deleteResource(resourcePathName + "Person.xsd");
        UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        userManagementClient.deleteRole("RoleSubscriptionTest");
    }


}
