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
package org.wso2.automation.common.test.as.capp;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

/**
 * Test multi tenancy of carbon applications (CAR) - Deploy a car from one tenant and
 * check whether it is available in other tenants
 */
public class CarFileMultitenancyTest {
    private static final Log log = LogFactory.getLog(CarFileMultitenancyTest.class);
    private static String AXIS2SERVICE_EPR;
    private static boolean stratosStatus;
    private String backEndUrl = null;
    private EnvironmentBuilder builder;
    private String serviceName;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running CarFileMultitenancyTest test...");
        int userId = 13;
        serviceName = "Calculator";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getAs().getBackEndUrl();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        stratosStatus = builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
    }

    @Test(groups = {"wso2.as"}, description = "Deploy simeple axis2 service using CApp", priority = 1)
    public void testCarfileDeployment() throws Exception {
        String operationName = "add";
        String expectedIntValue = "420";
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take
        OMElement result =
                new AxisServiceClient().sendReceive(createPayLoad(), AXIS2SERVICE_EPR, operationName);
        assertTrue((result.toString().indexOf(expectedIntValue) >= 1));
    }

    @Test(groups = {"wso2.as"}, description = " Check service existence though other tenant login.", priority = 2)
    public void testServiceMultitenancy()
            throws LoginAuthenticationExceptionException, RemoteException {
        if (stratosStatus) {
            int userIdOtherTenant = 12;
            EnvironmentBuilder builder = new EnvironmentBuilder().as(userIdOtherTenant);
            ManageEnvironment environment = builder.build();
            String serviceEPROfSecoundTenant = environment.getAs().getServiceUrl() + "/" + serviceName;

            assertFalse(AxisServiceClientUtils.isServiceAvailable(serviceEPROfSecoundTenant),
                        "Same service deployed in other tenants");
            log.info("CApp multitenancy test passed...");
        }
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://test.com", "test");
        OMElement method = fac.createOMElement("add", omNs);
        OMElement valueOfa = fac.createOMElement("a", omNs);
        OMElement valueOfb = fac.createOMElement("b", omNs);
        valueOfa.addChild(fac.createOMText(valueOfa, "200"));
        valueOfb.addChild(fac.createOMText(valueOfb, "220"));
        method.addChild(valueOfa);
        method.addChild(valueOfb);

        return method;
    }
}
