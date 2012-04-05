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
package org.wso2.automation.common.test.as.stat;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceStatistic;
import org.wso2.carbon.statistics.stub.types.carbon.ServiceStatistics;
import org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.*;

import java.rmi.RemoteException;

/**
 * Deploy a service from one tenant and invoke it multiple times. Then check the system statistics of other tenants
 */
public class ServiceStatTest {
    private static final Log log = LogFactory.getLog(ServiceStatTest.class);
    private static AdminServiceStatistic adminServiceStatistics;
    private static String AXIS2SERVICE_EPR;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private String serviceName;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        log.info("Running AAR service stat test...");
        int userId = 12;
        serviceName = "Axis2Service";
        builder = new EnvironmentBuilder().as(userId);
        environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        String sessionCookie = environment.getAs().getSessionCookie();
        adminServiceStatistics = new AdminServiceStatistic(environment.getAs().getBackEndUrl(), sessionCookie);
    }


    @Test(groups = {"wso2.as"}, description =
            "Send 100 request to the service and check service stats", priority = 1)
    public void testServiceStats() throws InterruptedException, RemoteException {
        log.info("Running Service Stat test...");
        String operation = "echoInt";
        String expectedValue = "123";
        int numberOfRequests = 100;

        log.info("Wait for service deployment");
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take


        SystemStatistics serviceStatisticsBeforeExecution =
                adminServiceStatistics.getSystemStatistics();

        //invoke service for 100 times
        for (int i = 0; i < numberOfRequests; i++) {
            OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                                   AXIS2SERVICE_EPR, operation);
            log.debug("Response for request " + i + " " + result);
            assertTrue((result.toString().indexOf(expectedValue) >= 1));
        }
        //get system stats again after 100 service runs
        ServiceStatistics serviceStatisticsAfterExecution =
                adminServiceStatistics.getServiceStatistics(serviceName);


        log.debug("Request count after execution: " + serviceStatisticsAfterExecution.getTotalRequestCount());
        log.debug("Request count before execution: " + serviceStatisticsBeforeExecution.getTotalRequestCount());
        assertTrue((getStatDifference(serviceStatisticsAfterExecution.getTotalRequestCount(),
                                      serviceStatisticsBeforeExecution.getTotalRequestCount())
                    < numberOfRequests), "Expected request count not available");
        log.info("Request count verification passed");

        log.debug("Response count after execution: " + serviceStatisticsAfterExecution.getTotalResponseCount());
        log.debug("Response count before execution: " + serviceStatisticsBeforeExecution.getTotalResponseCount());
        assertTrue((getStatDifference(serviceStatisticsAfterExecution.getTotalResponseCount(),
                                      serviceStatisticsBeforeExecution.getTotalResponseCount())
                    < numberOfRequests), "Expected Response count not available");
        log.info("Response count verification passed");

        log.debug("Fault count after execution" + serviceStatisticsAfterExecution.getTotalFaultCount());
        log.debug("Fault count after execution " + serviceStatisticsBeforeExecution.getTotalFaultCount());
        assertTrue((getStatDifference(serviceStatisticsAfterExecution.getTotalFaultCount(),
                                      serviceStatisticsBeforeExecution.getTotalFaultCount())
                    < numberOfRequests), "Expected False count not available ");
        log.info("Fault count verification passed");

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

    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    private int getStatDifference(int after, int before) {
        return after - before;
    }

}

