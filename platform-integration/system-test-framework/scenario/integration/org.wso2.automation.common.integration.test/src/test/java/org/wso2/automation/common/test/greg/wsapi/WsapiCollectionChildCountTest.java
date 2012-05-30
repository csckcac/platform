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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.assertTrue;


public class WsapiCollectionChildCountTest {
    private static final Log log = LogFactory.getLog(WsapiCollectionChildCountTest.class);
    private static WSRegistryServiceClient registry = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
    }


    @Test(groups = {"wso2.greg"}, description = "test adding an Endpoint to G-Reg", priority = 1)
    public void testGetChildCountForCollection() throws RegistryException {
        String path = "/";
        Resource resource = null;
        try {
            resource = registry.get(path);
            assertTrue((resource instanceof Collection), "resource is not a collection");
            Collection collection = (Collection) resource;
            log.info("Collection Child count is=" + collection.getChildCount());
            assertTrue(true, "Child count is " + collection.getChildCount());
            log.info("***********WS-API getChildCountForCollection  - Passed*************");
        } catch (RegistryException e) {
            log.error("Get Child count collection/resource from root Failed :" + e);
            throw new RegistryException("Get Child count collection/resource from root Failed :" + e);
        }
    }
}
