/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.test.iterate;

import java.io.File;
import java.net.URL;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;
import org.wso2.carbon.mediator.test.ESBMediatorTest;
import org.apache.commons.io.FileUtils;

/* Tests sending different number of small messages through iterate mediator */

public class IterateSmallMessageTestCase extends ESBMediatorTest {

	private String nhttpPort = null;
	private String hostName = null;
	private StockQuoteClient client;

	@BeforeClass
	public void setEnvironment() throws Exception {
		init();
		client = new StockQuoteClient();
		nhttpPort = esbServer.getProductVariables().getNhttpPort();
		hostName = esbServer.getProductVariables().getHostName();

	}

	@Test(groups = "wso2.esb", description = "Tests small message in small number ~20")
	public void testSmallNumbers() throws Exception, InterruptedException {
		loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/iterate/simple_iterator.xml");
		URL url = getClass().getResource("/artifacts/ESB/mediatorconfig/iterate/iterate_small.txt");
		String symbol = FileUtils.readFileToString(new File(url.toURI()));
		OMElement response = null;
		for (int i = 0; i < 20; i++) {
			response =
			           client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort,
			                                              null, symbol);
			Assert.assertNotNull(response);
			Assert.assertTrue(response.toString().contains("WSO2"));
		}
	}

	@Test(groups = "wso2.esb", description = "Tests small message in small number ~500")
	public void testLargeNumbers() throws Exception, InterruptedException {
		loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/iterate/simple_iterator.xml");
		URL url = getClass().getResource("/artifacts/ESB/mediatorconfig/iterate/iterate_small.txt");
		String symbol = FileUtils.readFileToString(new File(url.toURI()));
		OMElement response = null;
		for (int i = 0; i < 500; i++) {
			response =
			           client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort,
			                                              null, symbol);
			Assert.assertNotNull(response);
			Assert.assertTrue(response.toString().contains("WSO2"));
		}
	}

	@AfterClass
	public void close() throws Exception {
		super.cleanup();
	}

}
