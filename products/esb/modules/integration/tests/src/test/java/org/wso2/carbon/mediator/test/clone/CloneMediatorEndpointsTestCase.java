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

package org.wso2.carbon.mediator.test.clone;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

public class CloneMediatorEndpointsTestCase extends ESBMediatorTest{

	String nhttpPort = null;
	String hostName = null;
	SampleAxis2Server axis2Server;
	AuthenticatorClient adminServiceAuthentication;

	@BeforeClass
	public void setEnvironmentHTTP() throws Exception {
		init();
		nhttpPort=esbServer.getProductVariables().getNhttpPort();
		hostName=esbServer.getProductVariables().getHostName();
		ESBTestCaseUtils caseUtils = new ESBTestCaseUtils();
	}

	@Test(groups = "wso2.esb.http", description = "Tests http adress")
	public void testHTTP() throws Exception, InterruptedException {
		loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/clone/clone_http.xml");
		OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://"
				+ hostName + ":" + nhttpPort, null, "WSO2");

		Assert.assertTrue(response.toString().contains("WSO2"));

	}

	@Test(groups = "wso2.esb.https", description = "Tests https adress")
	public void testHTTPS() throws Exception, InterruptedException {
		loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/clone/clone_https.xml");
		OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://"
				+ hostName + ":" + nhttpPort, null, "WSO2");
		Assert.assertTrue(response.toString().contains("WSO2"));

	}

	@AfterTest(groups = "wso2.esb")
	public void close() throws Exception {
		super.cleanup();
	}

}
