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

package org.wso2.carbon.registry.handler.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

public class HandlerUpdateTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(HandlerUpdateTest.class);


    @Override
    public void init() {
        log.info("Initializing Update Handler Test");
        log.debug("Update Handler Test Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        String sampleHandlerName = "sample-handler.xml";
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        HandlerManagementServiceStub handlerManagementServiceStub =
                TestUtils.getHandlerManagementServiceStub(sessionCookie);

        String handlerResource = TestUtils.getHandlerResourcePath(frameworkPath);
        try {


            System.out.println(handlerResource);
            handlerManagementServiceStub.createHandler(HandlerAddTest.fileReader(handlerResource));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to add handler configuration " + e);
        }

        try {
            handlerManagementServiceStub.updateHandler(handlerName, HandlerAddTest.fileReader(handlerResource));

            String handlerContent = handlerManagementServiceStub.getHandlerConfiguration(handlerName);
            if (handlerContent.indexOf("org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler") != -1) {
                log.info("Handler Configuration not updated");

            } else {
                log.error("Handler configuration not updated");
                Assert.fail("Handler configuration not updated");
            }

            try {
                handlerManagementServiceStub.deleteHandler(handlerName);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Failed to delete the handler" + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to add handler configuration " + e);
        }
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }
}
