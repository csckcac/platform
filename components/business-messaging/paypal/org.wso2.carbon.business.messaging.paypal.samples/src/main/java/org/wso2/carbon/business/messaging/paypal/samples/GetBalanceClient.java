/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.business.messaging.paypal.samples;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.business.messaging.paypal.samples.sample1.Sample1Client;

import javax.xml.stream.XMLStreamException;

/**
 * See build.xml for options
 */
public class GetBalanceClient {

	protected String getProperty(String name, String def) {
		String result = System.getProperty(name);
		if (result == null || result.length() == 0) {
			result = def;
		}
		return result;
	}

	public static void main(String[] args) {

		System.out.println("Start");
		try {
			new GetBalanceClient().executeClient();
//			new GetBalanceClient().executePaypalWSTestClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("End");
		System.exit(0);
	}

	private static void printResult(OMElement result) throws Exception {
		/*System.out.println("**** PayPal Account Info ****");
		System.out.println("Balance :"
				+ GetBalanceHandler.parseGetBalanceResponse(result));
		System.out.println("********* Thank You *********");*/
        System.out.println(result);
	}

    /**
     * paypal proxy Client
     * @throws Exception
     */
	public void executeClient() throws Exception {

		// defaults
        OMElement payload = getPayload();

        String soapVer = getProperty("soapver", "soap11");
        String trpUrl = getProperty("trpurl",
				"http://localhost:8280/services/paypalproxy");

        String repo = getProperty("repository", "/home/usw/axis_demo/traning/binary/axis2-SNAPSHOT/repository/");

        ConfigurationContext configContext = null;

        Options options = new Options();
        ServiceClient serviceClient;

        if (repo != null && !"null".equals(repo)) {
            configContext = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repo, repo + "../conf/axis2.xml");
            serviceClient = new ServiceClient(configContext, null);
        } else {
            serviceClient = new ServiceClient();
        }


		if (trpUrl != null && !"null".equals(trpUrl)) {
			options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
		}

		if ("soap12".equals(soapVer)) {
			options
					.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		}

		serviceClient.setOptions(options);
        
		OMElement element = serviceClient.sendReceive(payload);
		printResult(element);

		Thread.sleep(3600);

		serviceClient.cleanup();

	}

    /**
     * test method for do paypal WS API calls
     * @throws Exception
     */
	public void executePaypalWSTestClient() throws Exception {

		// defaults
        OMElement payload ;
        payload = getPaypalWSTestPayload();

        String soapVer = getProperty("soapver", "soap11");
        String trpUrl = getProperty("trpurl",
				"https://api.sandbox.paypal.com/2.0/");

        String repo = getProperty("repository", "/home/usw/axis_demo/traning/binary/axis2-SNAPSHOT/repository/");
        ConfigurationContext configContext = null;

        Options options = new Options();
        ServiceClient serviceClient;

        if (repo != null && !"null".equals(repo)) {
            configContext = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repo, repo + "../conf/axis2.xml");
            serviceClient = new ServiceClient(configContext, null);
        } else {
            serviceClient = new ServiceClient();
        }


		if (trpUrl != null && !"null".equals(trpUrl)) {
//			options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
		}

		if ("soap12".equals(soapVer)) {
			options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		}

        //set options
        options.setTo(new EndpointReference(trpUrl));
//		options.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,Boolean.TRUE);
		options.setProperty(HTTPConstants.CHUNKED, false);

		serviceClient.setOptions(options);
        serviceClient.addHeader(getPaypalWSTestHeader());
		OMElement element = serviceClient.sendReceive(payload);
		printResult(element);

		Thread.sleep(3600);

		serviceClient.cleanup();

	}

    public OMElement getPayload() {
        String version = getProperty("version", "61");
        String password = getProperty("password", "1265369211");
        String username = getProperty("username",
                "fazlan_1265369202_biz_api1.wso2.com");
        String signature = getProperty("signature",
				"AaNvupC2HsVPs-d5iU9.YgFyjltMAh4wuG8d7jqGMZAIuMO8mvGVtKzd");

        OMElement payload = null;
        payload = new GetBalanceHandler().createRequestPayload(version, username,
                                                               password, signature);
        return payload;
    }

    public OMElement getPaypalWSTestPayload() {
        String payload = "<urn:GetBalanceReq xmlns:urn=\"urn:ebay:api:PayPalAPI\">" +
                         "<urn:GetBalanceRequest><urn1:DetailLevel xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">" +
                         "full</urn1:DetailLevel><urn1:ErrorLanguage xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">" +
                         "en</urn1:ErrorLanguage><urn1:Version xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">61.0" +
                         "</urn1:Version><urn:ReturnAllCurrencies>?</urn:ReturnAllCurrencies>" +
                         "</urn:GetBalanceRequest></urn:GetBalanceReq>";
        try {
            OMElement payloadEl = AXIOMUtil.stringToOM(OMAbstractFactory.getOMFactory(),payload);
            return payloadEl;
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public OMElement getPaypalWSTestHeader() {
        String header = "<urn:RequesterCredentials xmlns:urn=\"urn:ebay:api:PayPalAPI\">" +
                        "<urn1:eBayAuthToken xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">" +
                        "</urn1:eBayAuthToken>" +
                        "<urn1:HardExpirationWarning xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">" +
                        "</urn1:HardExpirationWarning>" +
                        "<urn1:Credentials xmlns:urn1=\"urn:ebay:apis:eBLBaseComponents\">" +
                        "<urn1:AppId></urn1:AppId><urn1:DevId></urn1:DevId><urn1:AuthCert>" +
                        "</urn1:AuthCert><urn1:Username>uswick_1291532762_biz_api1.wso2.com</urn1:Username>" +
                        "<urn1:Password>1291532800</urn1:Password>" +
                        "<urn1:Signature>AQU0e5vuZCvSg-XJploSa.sGUDlpATwhZYmAXFUIuNss83luEA0voic8" +
                        "</urn1:Signature><urn1:Subject></urn1:Subject><urn1:AuthToken></urn1:AuthToken>" +
                        "</urn1:Credentials></urn:RequesterCredentials>";
        try {
            OMElement headerEl = AXIOMUtil.stringToOM(OMAbstractFactory.getOMFactory(),header);
            return headerEl;
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
