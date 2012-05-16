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
package org.wso2.automation.common.perf.as;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.TestExceptionHandler;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

/*
Deploy an axis2 service and do load test against that service (c = 100 and n = 100)
 */
public class AarServiceLoadTestClient {
    private static final Log log = LogFactory.getLog(AarServiceLoadTestClient.class);


    private static String AXIS2SERVICE_EPR;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running AAR service Load test...");
        int userId = 1;
        String serviceName = "Axis2Service";
        builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
    }


    @Test(groups = {"wso2.as"}, description = "Running AAR load test with c=100 n=100", priority = 1)
    public void testAarUpload() throws InterruptedException, AxisFault {
        log.info("Running AAR load test...");
        String operation = "echoString";
        String expectedValue = "Hello World";
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait till service deployment
        OMElement response =
                new AxisServiceClient().sendReceive(createPayLoad(),
                                                    AXIS2SERVICE_EPR, operation);
        log.info("Initial request was successful");
        assertTrue((response.toString().indexOf(expectedValue) >= 1));
    }

    @Test(groups = {"wso2.as"}, description = "Running AAR load test with c=100 n=100",
          priority = 2)
    public void runSuccessCase() throws AxisFault, InterruptedException {
        log.info("Running AAR load test with c=50 n=10. Test will take few minutes to complete, " +
                 "please be patience");

        loadTestAxis2Service();

    }

    private void loadTestAxis2Service() throws AxisFault, InterruptedException {
        final int concurrencyNumber = 50;
        final int numberOfIterations = 10   ;
        final ServiceClient serviceclient = new ServiceClient();
        Thread[] clientThread = new Thread[concurrencyNumber];
        TestExceptionHandler exHandler = new TestExceptionHandler();

        for (int i = 0; i < concurrencyNumber; i++) {
            clientThread[i] = new Thread() {

                public void run() {
                    for (int i = 0; i < numberOfIterations; i++) {
                        try {
                            assertTrue(axis2ServiceTest(serviceclient), "Load test on axis2service failed");
                        } catch (Exception e) {
                            fail("Error on service invocation ", e);
                        }
                    }
                }
            };
            clientThread[i].setUncaughtExceptionHandler(exHandler);
            clientThread[i].start();
            clientThread[i].join();
        }

//        for (int i = 0; i < concurrencyNumber; i++) {
//            try {
//                clientThread[i].join();
//            } catch (InterruptedException e) {
//                fail("Thread join operation interrupted");
//                log.error("Thread join operation interrupted " + e);
//            }
//        }
    }

    private boolean axis2ServiceTest(ServiceClient serviceclient) throws Exception {
        boolean axis2ServiceStatus = false;
        OMElement result;
        try {
            OMElement payload = createPayLoad();
            Options opts = new Options();

            opts.setTo(new EndpointReference(AXIS2SERVICE_EPR));
            opts.setAction("http://service.carbon.wso2.org/echoString");
            opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
            serviceclient.setOptions(opts);
            log.debug("Axis2Service EPR " + AXIS2SERVICE_EPR);
            result = serviceclient.sendReceive(payload);

            if ((result.toString().indexOf("Hello World")) > 0) {
                axis2ServiceStatus = true;
            }
            assertTrue(axis2ServiceStatus, "Axis2Service invocation failed");

        } catch (AxisFault axisFault) {
            log.error("Axis2Service invocation failed :" + axisFault.getMessage());
            throw new Exception("Axis2Service invocation failed :" + axisFault.getMessage());
        }
        System.out.println(axis2ServiceStatus);
        return axis2ServiceStatus;
    }

    private static OMElement createPayLoad() {
        log.debug("Creating payload");
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns1");
        OMElement method = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("s", omNs);
        value.addChild(fac.createOMText(value, "Hello World"));
        method.addChild(value);
        return method;
    }

}



