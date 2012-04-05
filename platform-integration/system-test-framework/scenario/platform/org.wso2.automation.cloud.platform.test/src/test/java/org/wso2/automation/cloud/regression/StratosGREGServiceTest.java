/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.cloud.regression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.net.MalformedURLException;
import java.net.URL;

public class StratosGREGServiceTest {
    private static final Log log = LogFactory.getLog(StratosGREGServiceTest.class);
    private RemoteRegistry registry = null;
    private String httpGovernanceUrl;
    private UserInfo userInfo;

    @BeforeClass
    public void init() {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(4);
        EnvironmentVariables gregServer = builder.build().getGreg();
        userInfo = UserListCsvReader.getUserInfo(4);
        httpGovernanceUrl = "http://" + gregServer.getProductVariables().getHostName()
                              + "/t/" + userInfo.getDomain();
    }
    @Test()
    public void runSuccessCase() throws MalformedURLException, RegistryException {
        remoteRegistryClientTest();
    }


    private void remoteRegistryClientTest() throws MalformedURLException, RegistryException {

        String path = "/_system/local/registry.txt";
        boolean getValue = false;
        boolean putValue = false;
        boolean deleteValue = false;
        registry = new RemoteRegistry(new URL(httpGovernanceUrl + "/registry"),
                                      userInfo.getUserName(), userInfo.getPassword());

        /*put resource */

        Resource r1 = registry.newResource();
        r1.setContent("test content".getBytes());
        r1.setMediaType("text/plain");
        String pathValue = registry.put(path, r1);

        if (pathValue.equalsIgnoreCase(path)) {
            log.info("Resource successfully uploaded to registry");
            putValue = true;
        }
        Assert.assertTrue(putValue, "Failed to upload resource to registry");

        /*get resource */

        Resource r2 = registry.get(path);

        if (r2.getMediaType().equalsIgnoreCase("text/plain")) {
            getValue = true;
        }
        Assert.assertTrue(getValue, "Failed to get resource media type");


        /*Delete resource */

        registry.delete(path);

        if (!registry.resourceExists(path)) {
            deleteValue = true;
        }
        Assert.assertTrue(deleteValue, "Failed to delete resource");

    }

}