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

package org.wso2.carbon.registry.permission.test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class ExtensionPermissionTestCase {

    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws Exception {

        PermissionTestUtil.setUpTestRoles();

        //Setup environments
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(0);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(2);
        ManageEnvironment nonAdminEnvironment = builderNonAdmin.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());
    }

    @Test
    public void testAdminExtensionAddPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "reports" + File.separator + "TestingLCReportGenerator.jar";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addExtension("TestingLCReportGenerator.jar", dataHandler);
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            boolean pass = false;
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    pass = true;
                }
            }
            assertTrue(pass);
        } else {
            fail("Test Extension adding has failed");
        }
    }

    @Test(dependsOnMethods = "testAdminExtensionAddPermissions")
    public void testAdminExtensionDeletePermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        adminResourceAdminClient.removeExtension("TestingLCReportGenerator.jar");
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            boolean pass = true;
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    pass = false;
                }
            }
            assertTrue(pass);
        }
    }

    @Test(expectedExceptions = RemoteException.class)
    public void testNonAdminExtensionAddPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "reports" + File.separator + "TestingLCReportGenerator.jar";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        nonAdminResourceAdminClient.addExtension("TestingLCReportGenerator.jar", dataHandler);  //Not allowed
    }

    @Test(expectedExceptions = RemoteException.class)
    public void testNonAdminListExtensionPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        nonAdminResourceAdminClient.listExtensions(); //Not allowed
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    adminResourceAdminClient.removeExtension("TestingLCReportGenerator.jar");
                }
            }
        }
        PermissionTestUtil.resetTestRoles();
    }
}
