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
package org.wso2.automation.common.test.as.soaptracer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceTracerAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.tracer.stub.types.carbon.MessagePayload;
import org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

/*
enable soap tracing in one tenant and invoke services in a different tenant.
Then test the message tracing to see whether soap tracing is multi-tenanted correctly
 */

public class SoapTracerTest {
    private static final Log log = LogFactory.getLog(SoapTracerTest.class);
    private static String AXIS2SERVICE_EPR;
    private static AdminServiceTracerAdmin firstUserSoupTrackerAdmin;
    private static AdminServiceTracerAdmin secondUserSoupTrackerAdmin;
    private EnvironmentBuilder builderUser1;
    private EnvironmentBuilder builderUser2;
    private static final String SOAP_TRACKER_ON_FLAG = "ON";
    private static final String SOAP_TRACKER_OFF_FLAG = "OFF";


    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running SOAP Tracer test...");
        int userIdOfUser1 = 1;
        int userIdOfUser2 = 2;
        String serviceName = "Axis2Service";
        builderUser1 = new EnvironmentBuilder().as(userIdOfUser1);
        builderUser2 = new EnvironmentBuilder().as(userIdOfUser2);
        ManageEnvironment environmentUser1 = builderUser1.build();
        ManageEnvironment environmentUser2 = builderUser2.build();
        AXIS2SERVICE_EPR = environmentUser1.getAs().getServiceUrl() + "/" + serviceName;

        firstUserSoupTrackerAdmin =
                new AdminServiceTracerAdmin(environmentUser1.getAs().getBackEndUrl(),
                                            environmentUser1.getAs().getSessionCookie());
        secondUserSoupTrackerAdmin =
                new AdminServiceTracerAdmin(environmentUser2.getAs().getBackEndUrl(),
                                            environmentUser2.getAs().getSessionCookie());
    }

    @Test(groups = "wso2.as", description = "soapTracer test with single user", priority = 1)
    public void testSoapTracer() throws InterruptedException, AxisFault {
        String operation = "echoInt";
        String expectedValue = "1234556";
        int noOfMessagesToRetrieve = 200;

        TracerServiceInfo firstTenantSoapTracerServiceInfo;
        //Enable soap tracer of first tenant
        firstTenantSoapTracerServiceInfo = firstUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_ON_FLAG);
        assertEquals(SOAP_TRACKER_ON_FLAG, firstTenantSoapTracerServiceInfo.getFlag(), "Soap tracer ON flag not set");

        log.info("Wait for service deployment");
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builderUser1.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take

        OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                               AXIS2SERVICE_EPR, operation);
        assertTrue((result.toString().indexOf(expectedValue) >= 1));

        MessagePayload messagePayload;
        //get message by giving operation name as the filter.
        firstTenantSoapTracerServiceInfo =
                firstUserSoupTrackerAdmin.getMessages(noOfMessagesToRetrieve, operation);
        builderUser1.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();//wait
        messagePayload = firstTenantSoapTracerServiceInfo.getLastMessage();
        assertTrue((messagePayload.getRequest().indexOf(expectedValue) >= 1));
        assertTrue((messagePayload.getResponse().indexOf(expectedValue) >= 1));

        log.info("SOAP Tracer message assertion passed");
        if (log.isDebugEnabled()) {
            log.debug("Request Payload" + messagePayload.getRequest());
            log.debug("Response Payload" + messagePayload.getResponse());
        }

        firstTenantSoapTracerServiceInfo =
                firstUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_OFF_FLAG);
        assertEquals(SOAP_TRACKER_OFF_FLAG, firstTenantSoapTracerServiceInfo.getFlag(),
                     "Soap tracer OFF flag not set");
    }

    @Test(groups = "wso2.as", description = "soapTracer mulitenancy test", priority = 2)
    public void testSoapTracerMultitenancy() throws AxisFault {

        String operation = "echoInt";
        String expectedValue = "1234556";
        int noOfMessagesToRetrieve = 200;

        TracerServiceInfo firstTenantSoapTracerServiceInfo;
        TracerServiceInfo secondTenantSoapTracerServiceInfo;

        boolean stratosStatus =
                builderUser1.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        log.info("Stratos Status" + stratosStatus);

        if (stratosStatus) {
            firstTenantSoapTracerServiceInfo =
                    firstUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_ON_FLAG); //Enable soap tracer of first tenant
            secondTenantSoapTracerServiceInfo =
                    secondUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_ON_FLAG); //Enable soap tracer of second tenant

            assertEquals(SOAP_TRACKER_ON_FLAG, firstTenantSoapTracerServiceInfo.getFlag(),
                         "Soap tracer ON flag not set");
            assertEquals(SOAP_TRACKER_ON_FLAG, secondTenantSoapTracerServiceInfo.getFlag(),
                         "Soap tracer ON flag not set");

            log.info("Wait for service deployment");
            AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment

            OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                                   AXIS2SERVICE_EPR, operation);
            assertTrue((result.toString().indexOf(expectedValue) >= 1));

            MessagePayload messagePayload;
            //get message by giving operation name as the filter.
            firstTenantSoapTracerServiceInfo =
                    firstUserSoupTrackerAdmin.getMessages(noOfMessagesToRetrieve, operation);
            secondTenantSoapTracerServiceInfo =
                    secondUserSoupTrackerAdmin.getMessages(noOfMessagesToRetrieve, operation);

            messagePayload = firstTenantSoapTracerServiceInfo.getLastMessage();
            assertTrue((messagePayload.getRequest().indexOf(expectedValue) >= 1));
            assertTrue((messagePayload.getResponse().indexOf(expectedValue) >= 1));
            log.info("Soap traser message assertion passed");
            if (log.isDebugEnabled()) {
                log.debug("Request Payload" + messagePayload.getRequest());
                log.debug("Response Payload" + messagePayload.getResponse());
            }

            messagePayload = secondTenantSoapTracerServiceInfo.getLastMessage();

            assertNull(secondTenantSoapTracerServiceInfo.getLastMessage(),
                       "Message found in second tenant soap tracer ");
            log.info("No Messages found in secound tenant soap tracer");

            //if last message exists then check message body for result value.
            if (secondTenantSoapTracerServiceInfo.getLastMessage() != null) {
                assertFalse((messagePayload.getRequest().indexOf(expectedValue) >= 1));
                assertFalse((messagePayload.getResponse().indexOf(expectedValue) >= 1));
                log.info("Messages do not contain expected value, hence test passed");
            }

            firstTenantSoapTracerServiceInfo =
                    firstUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_OFF_FLAG);
            assertEquals(SOAP_TRACKER_OFF_FLAG, firstTenantSoapTracerServiceInfo.getFlag(),
                         "Soap tracer OFF flag not set");

            secondTenantSoapTracerServiceInfo =
                    secondUserSoupTrackerAdmin.setMonitoring(SOAP_TRACKER_OFF_FLAG);
            assertEquals(SOAP_TRACKER_OFF_FLAG, secondTenantSoapTracerServiceInfo.getFlag(),
                         "Soap tracer OFF flag not set");
        }
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
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


}
