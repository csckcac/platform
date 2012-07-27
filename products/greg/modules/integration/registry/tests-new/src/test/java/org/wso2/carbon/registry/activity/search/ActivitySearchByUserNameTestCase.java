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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.user.mgt.stub.GetAllRolesNamesUserAdminExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchByUserNameTestCase {
    private static final Log log = LogFactory.getLog(ActivitySearchByUserNameTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private UserManagementClient userManagementClient;
    private ManageEnvironment environment;
    private int userId = 0;
    UserInfo userInfo;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
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
        userManagementClient =
                new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                         environment.getGreg().getSessionCookie());
    }


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
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath + resourceName)[0].getAuthorUserName().
                contains(userInfo.getUserName()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivityByAvailableUser() throws RegistryExceptionException, RemoteException,
                                                       ResourceAdminServiceExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(),
                                                               userInfo.getUserName(),
                                                               "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/" +
                                                               resourceName, "", "",
                                                               "", 0).getActivity());
        resourceAdminServiceClient.deleteResource(wsdlPath + resourceName);
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivityByUnAvailableUser()
            throws RegistryExceptionException, RemoteException,
                   GetAllRolesNamesUserAdminExceptionException {
        int id = 0;
        String unAvailableUser = "testUser" + id;
        while (userManagementClient.userNameExists(unAvailableUser, unAvailableUser)) {
            if (!userManagementClient.userNameExists(unAvailableUser, unAvailableUser)) {
                id++;
            }
            unAvailableUser = "testUser" + id;
        }
        assertNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(),
                                                            unAvailableUser, "", "", "", "", 0).getActivity());
    }
}

