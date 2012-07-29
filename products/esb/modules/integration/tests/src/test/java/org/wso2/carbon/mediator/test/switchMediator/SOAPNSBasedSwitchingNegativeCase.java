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
package org.wso2.carbon.mediator.test.switchMediator;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

public class SOAPNSBasedSwitchingNegativeCase {
	protected Log log = LogFactory.getLog(getClass());
	protected StockQuoteClient axis2Client;
	protected EnvironmentVariables esbServer;

	@BeforeClass
	public void beforeClass() throws Exception {
		axis2Client = new StockQuoteClient();
		EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);

		esbServer = builder.build().getEsb();
		uploadSynapseConfig();
	}

	@AfterClass
	public void afterClass() {
		axis2Client.destroy();
	}

	@Test(groups = { "wso2.esb" }, description = "Write xpath expression using SOAP 1.1/1.2 NS Send SOAP 1.1/1.2 response and assert switch")
	public void testXPathOnDifferentSOAPNS() throws AxisFault {
		OMElement response1 = null;
		OMElement response2 = null;
		response1 =
		            axis2Client.sendSimpleStockQuoteSoap11("http://localhost:8280/services/switchSoap11SampleNegative",
		                                                   "http://localhost:9000/services/SimpleStockQuoteService",
		                                                   "IBM");
		assertTrue(response1.toString().contains("IBM"));

		try {
			response2 =
			            axis2Client.sendSimpleStockQuoteSoap12("http://localhost:8280/services/switchSoap11SampleNegatives",
			                                                   "http://localhost:9000/services/SimpleStockQuoteService",
			                                                   "IBM");

			fail("Test failed : SOAP 1.2 request is switched into SOAP 1.1 also.");
		} catch (AxisFault expected) {
			assertTrue(expected.getReason()
			                   .contains("The input stream for an incoming message is null") &&
			           response2 == null);

		}
	}

	protected void uploadSynapseConfig() throws Exception {
		loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/filters/switchMediator/SOAP11_SOAP12_XPath_nagative_case.xml");
	}

	protected void loadESBConfigurationFromClasspath(String filePath) throws Exception {
		ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
		esbUtils.loadESBConfigurationFromClasspath(filePath, esbServer.getBackEndUrl(),
		                                           esbServer.getSessionCookie());
	}

	protected String getMainSequenceURL() {
		return "http://" + esbServer.getProductVariables().getHostName() + ":" +
		       esbServer.getProductVariables().getNhttpPort();
	}
}
