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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.util.LifecycleUtil;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class NotificationEventTypeChangingTestCase {

    private ManageEnvironment environment;
    private int userID = 0;
    private UserInfo userInfo;

    private static final String RESOURCE_PATH_NAME = "/";


    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"service.metadata.xml", "application/xml", "services"},
                              new Object[]{"info.wsdl", "application/wsdl+xml", "wsdl"},
                              new Object[]{"policy.xml", "application/xml", "policy"},
                              new Object[]{"test.map", "Unknown", "mediatypes"}
        };
    }


    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
    }

    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String name, String type, String folder)
            throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + folder + File.separator + name;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(RESOURCE_PATH_NAME + name, type, "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME + name)[0].getAuthorUserName().contains(userInfo.getUserName()));
    }


    @Test(groups = "wso2.greg", description = "Get JMX Notification when event type change", dataProvider = "ResourceDataProvider", dependsOnMethods = "testAddResource")
    public void testSubscriptionEventTypeChange(String name, String type, String folder)
            throws Exception {
        assertTrue(LifecycleUtil.init(RESOURCE_PATH_NAME + name, environment, userInfo));
    }

    @AfterClass()
    public void clean() throws AxisFault, NumberFormatException, RegistryException {
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(Integer.parseInt(userInfo.getUserId()), ProductConstant.GREG_SERVER_NAME);
        wsRegistryServiceClient.removeAspect("StateDemoteLC");
    }

}
