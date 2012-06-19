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
package org.wso2.automation.common.test.greg.cappDeployment;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceApplicationAdmin;
import org.wso2.carbon.admin.service.AdminServiceCarbonAppUploader;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/*
This Class tests Car File Deployment of Text Resources
*/
public class UploadCarFileHavingTextResourcesTest {
    private String sessionCookie;
    private WSRegistryServiceClient registry;
    private AdminServiceCarbonAppUploader cAppUploader;
    private AdminServiceApplicationAdmin adminServiceApplicationAdmin;

    @BeforeClass
    public void init()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        cAppUploader = new AdminServiceCarbonAppUploader(gregServer.getBackEndUrl());
        adminServiceApplicationAdmin = new AdminServiceApplicationAdmin(gregServer.getBackEndUrl());

    }

    @Test(priority = 1, description = "Upload CApp having Text Resources")
    public void uploadCApplication()
            throws MalformedURLException, RemoteException, InterruptedException {
        String filePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME) + File.separator +
                          "car" + File.separator + "text_resources_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact(sessionCookie, "text_resources_1.0.0.car",
                                             new DataHandler(new URL("file://" + filePath)));
        Thread.sleep(10000);

    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadCApplication"})
    public void isResourcesExist() throws RegistryException {

        registry.get("/_system/capps/text_files.xml");
        registry.get("/_system/capps/buggggg.txt");

    }

    @Test(description = "Delete Carbon Application ", dependsOnMethods = {"isResourcesExist"})
    public void deleteCApplication()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException {
        adminServiceApplicationAdmin.deleteApplication(sessionCookie, "text_resources");
        Thread.sleep(20000);
    }

    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"deleteCApplication"})
    public void isResourcesDeleted() throws RegistryException {

        try {
            registry.get("/_system/capps/text_files.xml");
            Assert.fail("Resource Not Deleted");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to perform get operation"));
        }
        try {
            registry.get("/_system/capps/buggggg.txt");
            Assert.fail("Resource Not Deleted");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to perform get operation"));
        }
    }

    @AfterClass
    public void destroy() {
        sessionCookie = null;
        registry = null;
    }
}
