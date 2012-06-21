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
package org.wso2.carbon.registry.capp.deployment.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceApplicationAdmin;
import org.wso2.carbon.admin.service.AdminServiceCarbonAppUploader;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class UploadCarFileHavingTextResourcesTestCase {
    private String sessionCookie;
    private WSRegistryServiceClient registry;
    private AdminServiceCarbonAppUploader cAppUploader;
    private AdminServiceApplicationAdmin adminServiceApplicationAdmin;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        registry = GregTestUtils.getRegistry();
        cAppUploader = new AdminServiceCarbonAppUploader(SERVER_URL);
        adminServiceApplicationAdmin = new AdminServiceApplicationAdmin(SERVER_URL);

    }

    @Test(priority = 1, description = "Upload CApp having Text Resources")
    public void uploadCApplicationWithTextResource()
            throws MalformedURLException, RemoteException, InterruptedException {
        String filePath = GregTestUtils.getResourcePath() + File.separator +
                          "car" + File.separator + "text_resources_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact(sessionCookie, "text_resources_1.0.0.car",
                                             new DataHandler(new URL("file://" + filePath)));
        Thread.sleep(10000);

    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadCApplicationWithTextResource"})
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

        Assert.assertFalse(registry.resourceExists("/_system/capps/text_files.xml"));
        Assert.assertFalse(registry.resourceExists("/_system/capps/buggggg.txt"));

    }

    @AfterClass
    public void destroy() {
        sessionCookie = null;
        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        registry = null;
    }
}
