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
package org.wso2.automation.common.test.ms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class JsServiceUplodeTest {

    private static final Log log = LogFactory.getLog(JsServiceUplodeTest.class);
    private static String SCHEMA_SERVICE_EPR;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running JS service upload test...");
        int userId = 1;
        String serviceName = "schemaTest1";
        EnvironmentBuilder builder = new EnvironmentBuilder().ms(userId);
        environment = builder.build();
        SCHEMA_SERVICE_EPR = environment.getMs().getServiceUrl() + "/" + "admin" + "/" + serviceName;
        log.debug("ServiceURL " + SCHEMA_SERVICE_EPR);
    }

    @Test(groups = {"wso2.ms"}, description = "upload Java Script service and invoke it", priority = 1)
    public void testJSServiceDeployment() throws Exception {
        String operationName = "echoJSString";
        String expectedIntValue = "HelloWorld";
        String namespaceOfService1 = "http://services.mashup.wso2.org/schemaTest1?xsd";
        log.info("Waiting for services to get deployed ..");
        AxisServiceClientUtils.waitForServiceDeployment(SCHEMA_SERVICE_EPR);
        Thread.sleep(builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay());
        OMElement resultJarService1 = new AxisServiceClient().
                sendReceive(createPayLoad(operationName, expectedIntValue, namespaceOfService1),
                            SCHEMA_SERVICE_EPR, operationName);
        log.debug("Response returned " + resultJarService1);
        assertTrue((resultJarService1.toString().indexOf(expectedIntValue) >= 1));
    }

    private static OMElement createPayLoad(String operation, String expectedValue,
                                           String namespace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(namespace, "p");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("param", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        log.debug("Created payload is :" + method);
        return method;
    }
}
