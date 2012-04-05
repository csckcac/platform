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

package org.wso2.automation.common.test.as.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;


/*
Test multi-tenancy of services - Deploy services from one tenant and check whether those are available in other tenants
 */
public class AARServiceUploadTest {

    private static final Log log = LogFactory.getLog(AARServiceUploadTest.class);
    private static String AXIS2SERVICE_EPR;
    private static boolean stratosStatus;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        log.info("Running AAR service upload test...");
        int userId = 1;
        String serviceName = "Axis2Service";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        stratosStatus = builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
    }


    @Test(groups = {"wso2.as"}, description = "AAR upload and invocation", priority = 1)
    public void testAarUpload() throws InterruptedException, AxisFault {
        log.info("Running AAR upload test...");
        String operation = "echoInt";
        String expectedValue = "123";

        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait till service deployment
        OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                               AXIS2SERVICE_EPR, operation);
        log.debug("Response returned " + result);
        assertTrue((result.toString().indexOf(expectedValue) >= 1));
    }

    @Test(groups = {"wso2.as"}, description = "Test simple aar service multitenancy ", priority = 2)
    public void testServiceMultitenancy() throws AxisFault {
        String operation = "echoInt";
        String expectedValue = "123";
        if (stratosStatus) {
            //check service existence though other tenant login.
            int MultitenancyCheckerTenantId = 12;
            builder = new EnvironmentBuilder().as(MultitenancyCheckerTenantId);
            OMElement response =
                    new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                        AXIS2SERVICE_EPR, operation);
            log.debug("Response returned " + response);
            assertTrue((response.toString().indexOf(expectedValue) >= 1));
        }
    }

    private static OMElement createPayLoad(String operation, String expectedValue) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns1");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("x", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        log.debug("Created payload is :" + method);
        return method;
    }
}
