/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.samples.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.rule.service.stub.fileupload.ExceptionException;
import org.wso2.carbon.rule.service.stub.fileupload.RuleServiceFileUploadAdminStub;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class GreetingServiceDeploymentTestCase {

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private RuleServiceFileUploadAdminStub ruleServiceFileUploadAdminStub;

    @BeforeClass(groups = {"wso2.brs"})
    public void login() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();
        ruleServiceFileUploadAdminStub =
                new RuleServiceFileUploadAdminStub("https://localhost:9443/services/RuleServiceFileUploadAdmin");
        ServiceClient client = ruleServiceFileUploadAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.brs"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

    @Test(groups = {"wso2.brs"})
    public void uploadGreetingService() throws ExceptionException, RemoteException {
        String samplesDir = System.getProperty("samples.dir");
        String greetingServiceAAR = samplesDir + File.separator + "GreetingService.aar";

        FileDataSource fileDataSource = new FileDataSource(greetingServiceAAR);
        DataHandler dataHandler = new DataHandler(fileDataSource);

        ruleServiceFileUploadAdminStub.uploadService("GreetingService.aar", dataHandler);
    }

    @Test(groups = {"wso2.brs"}, dependsOnMethods = {"uploadGreetingService"})
    public void callGreet() throws XMLStreamException, AxisFault {
        boolean invocationComplete = false;
        int tries = 0;
        OMElement result = null;
        while (!invocationComplete && tries <= 10) {
            try {
                ServiceClient serviceClient = new ServiceClient();
                Options options = new Options();
                options.setTo(new EndpointReference("http://localhost:9763/services/GreetingService"));
                options.setAction("urn:greetMe");
                serviceClient.setOptions(options);

                result = serviceClient.sendReceive(createPayload());
                invocationComplete = true;
            } catch (Exception e) {
                tries++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        assertNotNull(result, "Result cannot be null");
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:greetMeRequest xmlns:p=\"http://brs.carbon.wso2.org\">" +
                         "  <p:User>" +
                         "      <xs:name xmlns:xs=\"http://greeting.samples/xsd\">shammi</xs:name>" +
                         "   </p:User>\n" +
                         "</p:greetMeRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
