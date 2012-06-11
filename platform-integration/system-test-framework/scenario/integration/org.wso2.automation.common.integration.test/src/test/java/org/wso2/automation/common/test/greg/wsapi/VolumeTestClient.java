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

package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class VolumeTestClient {
    private static final Log log = LogFactory.getLog(TestTagging.class);
    private static WSRegistryServiceClient registry = null;
    private static String userName = null;
    private final int loopCount = 100000;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        UserInfo userInfo = UserListCsvReader.getUserInfo(tenantId);

        if (environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            userName = userInfo.getUserName().substring(0, userInfo.getUserName().lastIndexOf('@'));
        } else {
            userName = userInfo.getUserName();
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Test add another resource", priority = 1)
    public void testAddLargeSetOfResource() throws RegistryException {
        Resource r1 = registry.newResource();
        for (int index = 0; index <= loopCount; index++) {
            String content = "this is my content2";

            try {
                r1.setContent(content.getBytes());
                r1.setDescription("r2 file description");
                r1.setProperty("key" + index, "value" + index);
                r1.setProperty("key" + index + 1, "value" + index + 1);
                String path = "/automation/test/r" + index;
                try {
                    registry.put(path, r1);
                } catch (RegistryException e) {
                    fail("Couldn't put content to path /automation/test/r" + index);
                }

                Resource r1_actual = registry.newResource();
                try {
                    r1_actual = registry.get("/automation/test/r" + index);
                } catch (RegistryException e) {
                    fail("Couldn't put content to path /automation/test/r" + index);
                }

                assertEquals(new String((byte[]) r1.getContent()),
                             new String((byte[]) r1_actual.getContent()), "Content is not equal.");
                assertEquals(userName, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
                assertEquals("/automation/test/r" + index, r1_actual.getPath(), "Can not get Resource path");
                assertEquals("/automation/test", r1_actual.getParentPath(), "Can not get Resource parent path");
                assertEquals(r1.getDescription(),
                             r1_actual.getDescription(), "Resource description is not equal");
                assertEquals(userName, r1_actual.getAuthorUserName(), "Author is not equal");
                assertEquals(r1.getProperty("key" + index),
                             r1_actual.getProperty("key" + index), "Resource properties are equal");
                assertEquals(r1.getProperty("key" + index + 1),
                             r1_actual.getProperty("key" + index + 1), "Resource properties are equal");
            } catch (RegistryException e) {
                log.error("WS-API Add an Another Resource test - Failed:" + e);
                throw new RegistryException("WS-API Add an Another Resource test-Failed:" + e);
            }

        }
//        deleteResources("/automation");
    }


    @Test(groups = {"wso2.greg"}, description = "Test add another resource", priority = 2)
    public void testDeleteLargeSetOfResource() throws Exception {

        for (int index = 0; index <= loopCount-1; index++) {
            String path = "/automation/test/r" + index;
            for (String resourcePath : registry.getCollectionContent("/automation/test")) {
                if (resourcePath.equals(path)) {
                    registry.delete(path);
                    System.out.println("Delete resource :" + index);
                    break;
                }
            }
            boolean resourceExists = true;
            if(registry.getCollectionContent("/automation/test")==null)
                break;
            for (String resourcePath : registry.getCollectionContent("/automation/test")) {
                if (resourcePath.equals(path)) {
                    resourceExists = false;
                    break;
                }

            }
            assertTrue(resourceExists, "Resource" + path + " " +
                                       "is not deleted");
        }
    }
    
    
}
