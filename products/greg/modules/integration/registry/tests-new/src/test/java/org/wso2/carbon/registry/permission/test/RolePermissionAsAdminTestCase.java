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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RolePermissionAsAdminTestCase {

    private static final String NEW_RESOURCE_PATH = "/_system/config/testRolePermissionDummy.txt";
    private static final String EXISTING_RESOURCE_PATH = "/_system/config/repository/components/" +
                                                         "org.wso2.carbon.governance/configuration/uri";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private String serverUrl;

    private static final String WEB_APP_RESOURCE_URL = "/registry/resource";

    private static final String NON_ADMIN_ROLE = "testRole";
    private static final String EVERYONE_ROLE = "everyone";

    private static final String READ_ACTION = "2";
    private static final String PERMISSION_ENABLED = "1";
    private static final String PERMISSION_DISABLED = "0";


    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException {

        //Setup environments
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(0);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(1);
        ManageEnvironment nonAdminEnvironment = builderNonAdmin.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());

        //setup server url
        String backEndUrl = adminEnvironment.getGreg().getBackEndUrl();
        backEndUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));
        serverUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));

        //Add a new resource
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, EVERYONE_ROLE,
                                                       READ_ACTION, PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, EVERYONE_ROLE,
                                                       READ_ACTION, PERMISSION_DISABLED);
    }

    @Test(expectedExceptions = IOException.class, description = "Test deny access to new resources")
    public void testUserDenyAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, NON_ADMIN_ROLE, READ_ACTION,
                                                       PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(expectedExceptions = IOException.class, description = "Test allow access to new resources")
    public void testUserAllowAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, NON_ADMIN_ROLE, READ_ACTION,
                                                       PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(expectedExceptions = IOException.class, description = "Test deny access to existing resources")
    public void testUserDenyAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, NON_ADMIN_ROLE, READ_ACTION,
                                                       PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(expectedExceptions = IOException.class, description = "Test allow access to existing resources")
    public void testUserAllowAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, NON_ADMIN_ROLE, READ_ACTION,
                                                       PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when everyone is allowed for resources")
    public void testDenyAccessToAnonymousWhenEveryoneAllowed()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, EVERYONE_ROLE, READ_ACTION,
                                                       PERMISSION_ENABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when everyone is denied for resources")
    public void testDenyAccessToAnonymousWhenEveryoneDenied()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, EVERYONE_ROLE, READ_ACTION,
                                                       PERMISSION_DISABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

}
