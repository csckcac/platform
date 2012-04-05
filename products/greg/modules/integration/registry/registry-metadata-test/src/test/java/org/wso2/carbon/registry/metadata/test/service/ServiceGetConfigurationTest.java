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

package org.wso2.carbon.registry.metadata.test.service;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.services.stub.AddServicesServiceStub;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;


public class ServiceGetConfigurationTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(ServiceEditTest.class);
    private AddServicesServiceStub addServicesServiceStub;

    @Override
    public void init() {
        log.info("Initializing Get Service Configuration Tests");
        log.debug("Add Service Resource Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        addServicesServiceStub = TestUtils.getAddServicesServiceStub(sessionCookie);

        try {
            String serviceConfiguration = addServicesServiceStub.getServiceConfiguration();

            if (serviceConfiguration.indexOf("<table name=\"Service Lifecycle\">") != -1) {
                log.info("service configuration content found");

            } else {
                log.error("service configuration content not found");
                Assert.fail("service configuration content not found");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    @Override
    public void runFailureCase() {

    }

    @Override
    public void cleanup() {

    }
}
