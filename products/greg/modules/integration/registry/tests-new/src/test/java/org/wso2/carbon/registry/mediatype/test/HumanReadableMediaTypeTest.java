/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.mediatype.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.testng.Assert.assertTrue;


public class HumanReadableMediaTypeTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    public void predefinedMediaTypeTest() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdl/AmazonWebServices.wsdl",
                                               "application/wsdl+xml", "desc", dh);
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("wsdl"));
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("wsdl").contains("application/wsdl+xml"));
    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    public void notPredefinedMediaTypeTest() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "mediatypes" + File.separator + "test.map";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test/test.map", "test/map", "desc", dh);
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("map").contains("test/map"));
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("map"));
    }
}
