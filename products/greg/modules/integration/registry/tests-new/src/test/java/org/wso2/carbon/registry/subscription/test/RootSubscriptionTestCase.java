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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.subscription.util.JMXSubscription;
import org.wso2.carbon.registry.subscription.util.ManagementConsoleSubscription;


public class RootSubscriptionTestCase {
    private ManageEnvironment environment;
    private int userID = 0;
    private UserInfo userInfo;

    private static final String ROOT = "/";

    @BeforeClass
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
    }

    @Test(groups = "wso2.greg", description = "Get Management Console Notification")
    public void testConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT, "CollectionUpdated", environment, userInfo));
    }

    @Test(groups = "wso2.greg", description = "Get JMX Notification")
    public void testJMXSubscription() throws Exception {
        assertTrue(JMXSubscription.init(ROOT, "CollectionUpdated", environment, userInfo));
    }


}
