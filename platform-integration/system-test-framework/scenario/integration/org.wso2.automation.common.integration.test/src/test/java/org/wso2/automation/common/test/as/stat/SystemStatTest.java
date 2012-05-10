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
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceStatistic;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.*;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * Deploy a service from one tenant and invoke it multiple times.
 * Then check the system statistics for request, response and faults.
 */
public class SystemStatTest {
    private static final Log log = LogFactory.getLog(SystemStatTest.class);
    private static AdminServiceStatistic adminServiceStatistics;
    private static String AXIS2SERVICE_EPR;
    private SystemStatistics systemStatisticsBeforeExecution;
    private SystemStatistics systemStatisticsAfterExecution;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running AAR service stat test...");
        int userId = 15;
        String serviceName = "Axis2Service";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        String sessionCookie = environment.getAs().getSessionCookie();
        adminServiceStatistics = new AdminServiceStatistic(environment.getAs().getBackEndUrl(), sessionCookie);
    }

    @Test(groups = {"wso2.as"}, description =
            "Send 100 request to the service and check stats", priority = 1)
    public void testServiceStats() throws InterruptedException, RemoteException {
        String operation = "echoInt";
        String expectedValue = "123";
        int numberOfRequests = 100;

        log.info("Wait for service deployment..");
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take


        systemStatisticsBeforeExecution = adminServiceStatistics.getSystemStatistics();

        //invoke service for 100 times
        for (int i = 0; i < numberOfRequests; i++) {
            OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                                   AXIS2SERVICE_EPR, operation);
            Assert.assertTrue((result.toString().indexOf(expectedValue) >= 1));
        }
        //get system stats again after 100 service runs   x
        Thread.sleep(5000);
        systemStatisticsAfterExecution = adminServiceStatistics.getSystemStatistics();

        if (log.isDebugEnabled()) {
            log.debug("Request count is incorrect : " +
                      systemStatisticsAfterExecution.getTotalRequestCount());
            log.debug("Request count before execution: " +
                      systemStatisticsBeforeExecution.getTotalRequestCount());
        }
        assertEquals((getStatDifference(systemStatisticsAfterExecution.getTotalRequestCount(),
                                        systemStatisticsBeforeExecution.getTotalRequestCount())),
                     numberOfRequests, "Request count is incorrect  ");
        log.info("Request count verification passed");

        if (log.isDebugEnabled()) {
            log.debug("Response count after execution: " +
                      systemStatisticsAfterExecution.getTotalResponseCount());
            log.debug("Response count before execution: " +
                      systemStatisticsBeforeExecution.getTotalResponseCount());
        }

        assertEquals(
                (getStatDifference(systemStatisticsAfterExecution.getTotalResponseCount(),
                                   systemStatisticsBeforeExecution.getTotalResponseCount())),
                numberOfRequests, "Response count is incorrect ");
        log.info("Response count verification passed");

        if (log.isDebugEnabled()) {
            log.debug("Fault count after execution" +
                      systemStatisticsAfterExecution.getTotalFaultCount());
            log.debug("Fault count after execution " +
                      systemStatisticsBeforeExecution.getTotalFaultCount());
        }

        assertEquals(
                (getStatDifference(systemStatisticsAfterExecution.getTotalFaultCount(),
                                   systemStatisticsBeforeExecution.getTotalFaultCount())),
                0, "Fault count incorrect ");
        log.info("Fault count verification passed");
    }

    @Test(groups = {"wso2.as"}, description = "Send 100 invalid request to the service and check stats",
          dependsOnMethods = "testServiceStats", priority = 2)
    public void testInvalidRequestStats()
            throws RemoteException, InterruptedException, XMLStreamException {
        //introducing faults and verity fault count
        String operation = "echoInt";
        int numberOfRequests = 100;
        String invalidIntNumber = "test";
        Thread.sleep(10000);
        systemStatisticsBeforeExecution = adminServiceStatistics.getSystemStatistics();
        Thread.sleep(1000);

        EndpointReference epr = new EndpointReference(AXIS2SERVICE_EPR);
        for (int i = 0; i < numberOfRequests; i++) {
            OMElement result =
                    AxisServiceClientUtils.sendRequest
                            (createPayLoad(operation, invalidIntNumber).toString(), epr);
            Assert.assertTrue((result.toString().indexOf("Fault") >= 1));

        }
        Thread.sleep(5000);
        systemStatisticsAfterExecution = adminServiceStatistics.getSystemStatistics();

        if (log.isDebugEnabled()) {
            log.debug("Fault count after execution " +
                      systemStatisticsBeforeExecution.getTotalFaultCount());
        }

        assertEquals(
                (getStatDifference(systemStatisticsAfterExecution.getTotalFaultCount(),
                                   systemStatisticsBeforeExecution.getTotalFaultCount())),
                numberOfRequests, "Fault count incorrect ");
        log.info("Fault count verification passed");
    }


    private static OMElement createPayLoad(String operation, String expectedValue) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns1");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("x", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        return method;
    }

    private int getStatDifference(int after, int before) {
        return after - before;
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
