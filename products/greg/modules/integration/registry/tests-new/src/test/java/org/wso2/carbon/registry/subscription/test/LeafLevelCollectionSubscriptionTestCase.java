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

import java.rmi.RemoteException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.subscription.test.util.JMXClient;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

public class LeafLevelCollectionSubscriptionTestCase {

    private ManageEnvironment environment;
    private int userID = 0;
    private UserInfo userInfo;
    private String loggedInSessionCookie = "";
    private JMXClient jmxClient;
    private String collectionPath = "/_system/governance/event";

    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        loggedInSessionCookie = environment.getGreg().getSessionCookie();
    }

    @Test(groups = "wso2.greg", description = "Add role")
    public void testAddRole() throws Exception {
        UserManagementClient userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         userInfo.getUserName(), userInfo.getPassword());
        userManagementClient.addRole("RoleSubscriptionTest", new String[]{userInfo.getUserName()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSubscriptionTest"));
    }


    @Test(groups = "wso2.greg", description = "ManagementConsole subscription", dependsOnMethods = "testAddRole")
    public void testSubscribeCollectionMgtConsole() throws RegistryException, RemoteException {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        SubscriptionBean bean = infoServiceAdminClient.subscribe(collectionPath, "work://SubscriptionTestRole",
                                                                 "CollectionUpdated", loggedInSessionCookie);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    @Test(groups = "wso2.greg", description = "JMX subscription")
    public void testJMXCollectionSubscription() throws RegistryException, RemoteException {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        SubscriptionBean bean = infoServiceAdminClient.subscribe(collectionPath, "jmx://", "CollectionUpdated",
                                                                 loggedInSessionCookie);
        assertTrue(bean.getSubscriptionInstances() != null);
    }


    @Test(groups = "wso2.greg", description = "Update Collection",
          dependsOnMethods = {"testSubscribeCollectionMgtConsole", "testJMXCollectionSubscription"})
    public void testUpdateCollection() throws Exception {
        jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword());
        jmxClient.registerNotificationListener(collectionPath);
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 userInfo.getUserName(), userInfo.getPassword());

        propertiesAdminServiceClient.setProperty(collectionPath, "TestProperty", "TestValue");

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

        assertTrue(resourceAdminServiceClient.getProperty(collectionPath, "TestProperty").equals("TestValue"));
        jmxClient.getNotifications();
    }


    @Test(groups = "wso2.greg", description = "Get JMX Notification", dependsOnMethods = {"testUpdateCollection"})
    public void testGetJMXNotification() {
        assertTrue(JMXClient.isSuccess());
    }


    @Test(groups = "wso2.greg", description = "Get Notification", dependsOnMethods = {"testUpdateCollection"})
    public void testGetNotification()
            throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault,
                   InterruptedException {
        boolean success = false;
        HumanTaskAdminClient humanTaskAdminClient =
                new HumanTaskAdminClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(),
                                         userInfo.getPassword());
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

        for (WorkItem workItem : workItems) {
            if ((workItem.getPresentationSubject().toString()).equals("The collection at path " +
                                                                      collectionPath + " was updated.")) {
                success = true;
                break;
            }
        }
        assertTrue(success);
    }

    @AfterClass()
    public void clean() throws Exception {
        UserManagementClient userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         userInfo.getUserName(), userInfo.getPassword());
        userManagementClient.deleteRole("RoleSubscriptionTest");
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient.removeProperty(collectionPath, "TestProperty");
    }


}
