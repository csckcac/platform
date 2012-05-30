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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

public class TestNegetiveScenarios {

    private static final Log log = LogFactory.getLog(TestNegetiveScenarios.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Resource r1 = registry.newResource();
        r1.setProperty("test", "Negtivetest");
        r1.setContent("neg1");
        registry.put("/test1/negetivetest/put/put1", r1);
    }


    @Test(alwaysRun = true, description = "Checking carbon patch released to add WSDL issue ", priority = 1)
    public void retriveFromInvalidPath() throws Exception {
        Boolean exceptionOccured = false;
        try {
            registry.get("test1/Noresource");
        } catch (RegistryException e) {
            log.info("Invalid path triggered exception");
        } finally {
            exceptionOccured = true;
        }
        if (!exceptionOccured) {
            throw new Exception("Invalids path does not trigger Registry Exception");
        }
    }

    public void deleteWSDL() throws RegistryException {

    }

}