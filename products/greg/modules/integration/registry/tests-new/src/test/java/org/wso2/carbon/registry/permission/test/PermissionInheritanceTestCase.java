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

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class PermissionInheritanceTestCase {

    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;

    private static final String TEST_DIR_PATH = "/_system/config/";
    private static final String DENIED_DIR = "dirDenied";
    private static final String ALLOWED_DIR = "dirAllowed";

    private static final String NON_ADMIN_ROLE = "testRole";
    private static final String EVERYONE_ROLE = "everyone";

    private static final String READ_ACTION = "2";
    private static final String WRITE_ACTION = "3";
    private static final String DELETE_ACTION = "4";
    private static final String AUTHORIZE_ACTION = "5";
    private static final String PERMISSION_ENABLED = "1";
    private static final String PERMISSION_DISABLED = "0";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException,
                                    ResourceAdminServiceExceptionException, MalformedURLException,
                                    LogoutAuthenticationExceptionException, RegistryException,
                                    ResourceAdminServiceResourceServiceExceptionException {
        int userId = 0;
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(userId);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(1);
        ManageEnvironment nonAdminEnvironment = builderNonAdmin.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());

        //set up resources
        adminResourceAdminClient.addCollection(TEST_DIR_PATH, DENIED_DIR, "plain/text",
                                               "Test dir for deny permission inheritance");

        adminResourceAdminClient.addCollection(TEST_DIR_PATH, ALLOWED_DIR, "plain/text",
                                               "Test dir for allow permission inheritance");

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));

        adminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/test.txt", "text/plain",
                                             "Dummy non root file", dataHandler);

        adminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt", "text/plain",
                                             "Dummy non root file", dataHandler);

        //Deny all permission to allow test collection
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       NON_ADMIN_ROLE, READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       EVERYONE_ROLE, READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       NON_ADMIN_ROLE, WRITE_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       NON_ADMIN_ROLE, DELETE_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       NON_ADMIN_ROLE, AUTHORIZE_ACTION, PERMISSION_DISABLED);

        //deny all permission to allow test resource
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt",
                                                       NON_ADMIN_ROLE, READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt",
                                                       EVERYONE_ROLE, READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt",
                                                       NON_ADMIN_ROLE, WRITE_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt",
                                                       NON_ADMIN_ROLE, DELETE_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt",
                                                       NON_ADMIN_ROLE, AUTHORIZE_ACTION, PERMISSION_DISABLED);
    }

    @Test(groups = "wso2.greg", description = "Test read access inheritance")
    public void testDenyReadPermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException,
                   RegistryException {
        //deny read permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, NON_ADMIN_ROLE,
                                                       READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, EVERYONE_ROLE,
                                                       READ_ACTION, PERMISSION_DISABLED);

        //test access
        Assert.assertNull(nonAdminResourceAdminClient.getResource(TEST_DIR_PATH + DENIED_DIR + "/test.txt"));

        //restore permissions
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, NON_ADMIN_ROLE,
                                                       READ_ACTION, PERMISSION_ENABLED);
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, EVERYONE_ROLE,
                                                       READ_ACTION, PERMISSION_ENABLED);

    }

    @Test(groups = "wso2.greg", description = "Test write access inheritance", expectedExceptions = AxisFault.class)
    public void testDenyWritePermission()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceResourceServiceExceptionException {
        //create a new collection in the test directory and disable permission at root
        adminResourceAdminClient.addCollection(TEST_DIR_PATH + DENIED_DIR, "testdir", "text/plain", "");
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       NON_ADMIN_ROLE, WRITE_ACTION, PERMISSION_DISABLED);

        //try to add a new resource to the new sub collection
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        nonAdminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/testdir/test2.txt",
                                                "text/plain", "Denied", dataHandler);

        //restore permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, NON_ADMIN_ROLE,
                                                       WRITE_ACTION, PERMISSION_ENABLED);
    }

    @Test(groups = "wso2.greg", description = "Test delete access inheritance", expectedExceptions = AxisFault.class, dependsOnMethods = "testDenyReadPermission")
    public void testDenyDeletePermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {
        //deny permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, NON_ADMIN_ROLE,
                                                       DELETE_ACTION, PERMISSION_DISABLED);
        try {
            //try to delete
            nonAdminResourceAdminClient.deleteResource(TEST_DIR_PATH + DENIED_DIR + "/test.txt");
        } finally {
            //restore permission
            adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                           NON_ADMIN_ROLE, DELETE_ACTION, PERMISSION_ENABLED);
        }


    }

    //This test Fails for now. Reported: REGISTRY-1173
    @Test(groups = "wso2.greg", description = "Test authorization access inheritance", dependsOnMethods = "testDenyReadPermission")
    public void testDenyAuthPermission()
            throws Exception, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {

        //Deny permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR, NON_ADMIN_ROLE,
                                                       AUTHORIZE_ACTION, PERMISSION_DISABLED);

        //try to change permission as non admin
        nonAdminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR + "/test.txt",
                                                          NON_ADMIN_ROLE, AUTHORIZE_ACTION, PERMISSION_ENABLED);

        //check permission
        PermissionBean permissionBean = nonAdminResourceAdminClient.getPermission(TEST_DIR_PATH +
                                                                                  DENIED_DIR + "/test.txt");
        Assert.assertFalse(permissionBean.getAuthorizeAllowed());

    }

    //Fails due to REGISTRY-1174
    @Test(groups = "wso2.greg", description = "Test read access inheritance")
    public void testAllowReadPermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException,
                   RegistryException {
        //allow denied read permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR, NON_ADMIN_ROLE,
                                                       READ_ACTION, PERMISSION_ENABLED);

        //test access
        Assert.assertNotNull(nonAdminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "/test.txt")[0]);
    }

    //Fails due to REGISTRY-1174
    @Test(groups = "wso2.greg", description = "Test write access inheritance")
    public void testAllowWritePermission()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceResourceServiceExceptionException {
        //create a new collection in the test directory and disable permission at root
        adminResourceAdminClient.addCollection(TEST_DIR_PATH + ALLOWED_DIR, "testdir", "text/plain", "");
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR + "/testdir",
                                                       NON_ADMIN_ROLE, WRITE_ACTION, PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR, NON_ADMIN_ROLE,
                                                       WRITE_ACTION, PERMISSION_ENABLED);

        //try to add a new resource to the new sub collection
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        nonAdminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/testdir/test2.txt", "text/plain", "Denied", dataHandler);
        Assert.assertTrue(adminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "test2.txt").length > 0);
    }

    //Fails due to REGISTRY-1174
    @Test(groups = "wso2.greg", description = "Test delete access inheritance", dependsOnMethods = "testAllowReadPermission")
    public void testAllowDeletePermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        nonAdminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/test2.txt", "text/plain", "", dataHandler);

        //allow denied permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       NON_ADMIN_ROLE, DELETE_ACTION, PERMISSION_ENABLED);

        //try to delete
        nonAdminResourceAdminClient.deleteResource(TEST_DIR_PATH + ALLOWED_DIR + "/test2.txt");
        Assert.assertTrue(adminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "test2.txt").length == 0);
    }

    //Fails due to REGISTRY-1174
    @Test(groups = "wso2.greg", description = "Test authorization access inheritance")
    public void testAllowAuthPermission()
            throws Exception, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {

        //Allow denied permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR, NON_ADMIN_ROLE,
                                                       AUTHORIZE_ACTION, PERMISSION_ENABLED);

        //check permission
        PermissionBean permissionBean = nonAdminResourceAdminClient.getPermission(TEST_DIR_PATH +
                                                                                  ALLOWED_DIR + "/test.txt");
        Assert.assertTrue(permissionBean.getAuthorizeAllowed());
    }
}
