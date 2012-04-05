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
package org.wso2.automation.common.test.as.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.assertTrue;

public class JarServiceUploadTest {

    private static final Log log = LogFactory.getLog(JarServiceUploadTest.class);
    private static String JAR_SERVICE_EPR1;
    private static String JAR_SERVICE_EPR2;
    private EnvironmentBuilder builder;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        log.info("Running Jar service upload test...");
        int userId = 1;
        String jarServiceName1 = "JarService1";
        String jarServiceName2 = "JarService2";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        JAR_SERVICE_EPR1 = environment.getAs().getServiceUrl() + "/" + jarServiceName1;
        JAR_SERVICE_EPR2 = environment.getAs().getServiceUrl() + "/" + jarServiceName2;
        log.debug("ServiceURL of service1" + JAR_SERVICE_EPR1);
        log.debug("ServiceURL of service2" + JAR_SERVICE_EPR2);
    }

    @Test(groups = {"wso2.as"}, description =
            "Upload jar service with multiple service classes", priority = 1)
    public void testJarDeployment() throws Exception {
        String operationName = "echoString";
        String expectedIntValue = "HelloWorld";
        String namespaceOfService1 = "http://service.carbon.wso2.org";
        String namespaceOfService2 = "http://jarservice.carbon.wso2.org";

        log.info("Waiting for services to get deployed ..");
        AxisServiceClientUtils.waitForServiceDeployment(JAR_SERVICE_EPR1); // wait for service deployment

        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take

        OMElement resultJarService1 = new AxisServiceClient().sendReceive
                (createPayLoad(operationName, expectedIntValue, namespaceOfService1),
                 JAR_SERVICE_EPR1, operationName);
        log.debug("Response returned " + resultJarService1);
        assertTrue((resultJarService1.toString().indexOf(expectedIntValue) >= 1));

        OMElement resultJarService2 = new AxisServiceClient().sendReceive
                (createPayLoad(operationName, expectedIntValue, namespaceOfService2),
                 JAR_SERVICE_EPR2, operationName);
        log.debug("Response returned " + resultJarService2);
        assertTrue((resultJarService2.toString().indexOf(expectedIntValue) >= 1));
    }

    private static OMElement createPayLoad(String operation, String expectedValue,
                                           String namespace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(namespace, "p");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("s", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        log.debug("Created payload is :" + method);
        return method;
    }
}
