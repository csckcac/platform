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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.util.ArrayList;
import java.util.List;


public class AARServiceSpaceInFileNameTest {

    private static final Log log = LogFactory.getLog(AARServiceSpaceInFileNameTest.class);
    private static String AXIS2SERVICE_EPR;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        int userId = 0;
        String serviceName = "Axis2Service";
        EnvironmentBuilder builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
    }


    @Test(groups = {"wso2.as"}, description = "AAR service with space in file name and invocation", priority = 1)
    public void testAARWithSpaceUpload() throws Exception {
        log.info("Running AAR service with space in file name test...");
        String operation = "echoInt";
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR);
        System.out.println("EPR is " + AXIS2SERVICE_EPR);
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">123<");
        AxisServiceClientUtils.sendRequest(AXIS2SERVICE_EPR, operation, createPayLoad().toString(), 1,
                expectedOutput, true);
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "p");
        OMElement method = fac.createOMElement("echoInt", omNs);
        OMElement value = fac.createOMElement("x", omNs);
        value.addChild(fac.createOMText(value, "123"));
        method.addChild(value);
        return method;
    }
}
