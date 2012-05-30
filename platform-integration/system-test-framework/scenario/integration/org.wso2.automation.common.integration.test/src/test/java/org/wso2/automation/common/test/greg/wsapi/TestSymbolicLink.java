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

package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

public class TestSymbolicLink {

    private static final Log log = LogFactory.getLog(TestSymbolicLink.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault, InterruptedException {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Resource r1 = registry.newResource();
        r1.setProperty("test", "symbolicLink");
        r1.setContent("link1");
        Resource r2 = registry.newResource();
        r2.setProperty("test", "targetLink");
        r2.setContent("link2");
        registry.put("/test1/originalSource/put2", r1);
        registry.toString();
        registry.put("/test1/targetSource/put4", r2);
        registry.put("/test1/targetSource1/symlink", r2);

    }


    @Test(alwaysRun = true, description = "Checking carbon patch released to add WSDL issue ", priority = 1)
    public void retriveFromInvalidPath() throws Exception {
        Boolean exceptionOccured = false;

            registry.createLink("/test1/targetSource1/simlink", "/test1/originalSource/put2");
       Resource linkResource =registry.get("/test1/targetSource/putLink");


    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts() throws RegistryException {
        deleteResources("test1");
        deleteResources("test2");
    }


    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }

}
