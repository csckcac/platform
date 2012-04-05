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
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.assertTrue;

public class SpringServiceUploadTest {

    private static final Log log = LogFactory.getLog(SpringServiceUploadTest.class);
    private static String SPRING_SERVICE_EPR;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        log.info("Running Spring service upload test...");
        int userId = 1;
        String serviceName = "SpringBean";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        SPRING_SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        log.debug("ServiceURL " + SPRING_SERVICE_EPR);
    }

    @Test(groups = {"wso2.as"}, description = "Upload spring service and invoke it", priority = 1)
    public void testSpringServiceDeployment() throws Exception {
        String operationName = "echoInt";
        String expectedIntValue = "451";
        String namespaceOfService = "http://service.carbon.wso2.org";
        log.info("Waiting for services to get deployed ..");
        AxisServiceClientUtils.waitForServiceDeployment(SPRING_SERVICE_EPR);
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take
        OMElement resultJarService1 =
                new AxisServiceClient().sendReceive(createPayLoad(operationName, expectedIntValue,
                                                                  namespaceOfService), SPRING_SERVICE_EPR, operationName);
        log.debug("Response returned " + resultJarService1);
        assertTrue((resultJarService1.toString().indexOf(expectedIntValue) >= 1));
        log.info("Test was successful.....");
    }

    private static OMElement createPayLoad(String operation, String expectedValue,
                                           String namespace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(namespace, "p");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("arg0", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        log.debug("Created payload is :" + method);
        return method;
    }


    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
