/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.appserver.sample.sts.client;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.util.Base64;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TokenStorage;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.client.STSClient;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.Constants;
import org.opensaml.XML;
import java.io.File;

public class Client {


	final static String SERVICE_EPR = "http://localhost:9763/services/HelloService";
        final static String STS_EPR = "http://localhost:9763/services/wso2carbon-sts";

        private static final String wso2appserverHome = System.getProperty("wso2appserver.home");

	public static void main(String[] args) throws Exception {
		ServiceClient client = null;
		Options options = null;
		ConfigurationContext ctx = null;
		Policy stsPolicy = null;
		STSClient stsClient = null;
		Policy servicePolicy = null;
		Token responseToken = null;
		TokenStorage store = null;
                String serviceEpr = null;
                String stsEpr = null;

                if (args.length > 1) {
                   stsEpr = args[0];
                   serviceEpr = args[1];
                } else {
                   stsEpr = STS_EPR ;
                   serviceEpr = SERVICE_EPR ;
                }

		ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem("repository");

		stsClient = new STSClient(ctx);

		stsClient.setRstTemplate(getRSTTemplate());
		stsClient.setAction(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_SCT);

		stsPolicy = loadPolicy(wso2appserverHome + "/samples/STS/conf/sts.policy.xml");

		servicePolicy = loadPolicy(wso2appserverHome + "/samples/STS/conf/service.policy.xml");

		responseToken = stsClient.requestSecurityToken(servicePolicy, stsEpr, stsPolicy,
				serviceEpr);

		System.out.println("RECEIVED SECRET: " + Base64.encode(responseToken.getSecret()) + "\n");
		System.out.println("RECEIVED TOKEN: " + responseToken.getToken() + "\n");

		// Store token
		store = TrustUtil.getTokenStore(ctx);
		store.add(responseToken);

		client = new ServiceClient(ctx, null);
		client.engageModule("rampart");
		client.engageModule("addressing");
		options = new Options();
		options.setAction("urn:greet");
		options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, servicePolicy);
		options.setProperty(RampartMessageData.SCT_ID, responseToken.getId());
		options.setTo(new EndpointReference(serviceEpr ));
		client.setOptions(options);

		System.out.println(client.sendReceive(getPayload("Hello")));

	}

	private static OMElement getPayload(String value) {
		OMFactory factory = null;
		OMNamespace ns = null;
		OMElement elem = null;
		OMElement childElem = null;

		factory = OMAbstractFactory.getOMFactory();
		ns = factory.createOMNamespace("http://www.wso2.org/types", "ns1");
		elem = factory.createOMElement("greet", ns);
		childElem = factory.createOMElement("name", null);
		childElem.setText(value);
		elem.addChild(childElem);

		return elem;
	}

	private static Policy loadPolicy(String xmlPath) throws Exception {

		StAXOMBuilder builder = null;
		Policy policy = null;
		RampartConfig rc = null;
		CryptoConfig sigCryptoConfig = null;
		String keystore = null;
		Properties merlinProp = null;
		CryptoConfig encrCryptoConfig = null;

		builder = new StAXOMBuilder(xmlPath);
		policy = PolicyEngine.getPolicy(builder.getDocumentElement());

		rc = new RampartConfig();

		rc.setUser("client");
		rc.setEncryptionUser("wso2carbon");
		rc.setPwCbClass(PWCBHandler.class.getName());

		keystore = wso2appserverHome + File.separator + "samples" +
                               File.separator + "STS" + File.separator +
                               "conf" + File.separator + "client-truststore.jks";

		merlinProp = new Properties();
		merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
		merlinProp.put("org.apache.ws.security.crypto.merlin.file", keystore);
		merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");

		sigCryptoConfig = new CryptoConfig();
		sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
		sigCryptoConfig.setProp(merlinProp);

		encrCryptoConfig = new CryptoConfig();
		encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
		encrCryptoConfig.setProp(merlinProp);

		rc.setSigCryptoConfig(sigCryptoConfig);
		rc.setEncrCryptoConfig(encrCryptoConfig);

		policy.addAssertion(rc);

		return policy;
	}

	private static OMElement getRSTTemplate() throws Exception {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement elem = fac.createOMElement(Constants.RST_TEMPLATE);
		TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, elem).setText(XML.SAML_NS);
		TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, elem,
				RahasConstants.KEY_TYPE_SYMM_KEY);
		TrustUtil.createKeySizeElement(RahasConstants.VERSION_05_02, elem, 256);
		return elem;
	}
}
