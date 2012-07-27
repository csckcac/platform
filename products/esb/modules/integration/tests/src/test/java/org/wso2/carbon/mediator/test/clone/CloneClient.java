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

package org.wso2.carbon.mediator.test.clone;
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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/*
 * This class can be used to send a simple stock quote request and get the
 * response as a string. Response will contain entire SOAP envelope.
 * Default StockQuote client gives only the first OMElement. So this can be used
 * in situations which we need all the SOAP envelope.
 */

public class CloneClient {
	private String repositoryPath = "samples" + File.separator + "axis2Client" + File.separator +
	                                "client_repo";
	private File repository = new File(repositoryPath);

	private MessageContext outMsgCtx;
	private ConfigurationContext cfgCtx;
	private ServiceClient serviceClient;
	private SOAPFactory fac;
	private SOAPEnvelope envelope;

	public String getResponse(String adress, String symbol) throws IOException {
		cfgCtx =
		         ConfigurationContextFactory.createConfigurationContextFromFileSystem(repository.getCanonicalPath(),
		                                                                              null);
		serviceClient = new ServiceClient(cfgCtx, null);
		OperationClient operationClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
		setMessageContext(adress);
		outMsgCtx.setEnvelope(createSOAPEnvelope(symbol));
		operationClient.addMessageContext(outMsgCtx);
		operationClient.execute(true);
		MessageContext inMsgtCtx = operationClient.getMessageContext("In");
		SOAPEnvelope response = inMsgtCtx.getEnvelope();
		return response.toString();

	}

	private OMElement createSimpleQuoteRequestBody(String symbol) {
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
		OMElement method = fac.createOMElement("getQuote", omNs);
		OMElement value1 = fac.createOMElement("request", omNs);
		OMElement value2 = fac.createOMElement("symbol", omNs);
		value2.addChild(fac.createOMText(value1, symbol));
		value1.addChild(value2);
		method.addChild(value1);
		return method;
	}

	private void setMessageContext(String adress) {
		outMsgCtx = new MessageContext();
		// assigning message context&rsquo;s option object into instance
		// variable
		Options opts = outMsgCtx.getOptions();
		// setting properties into option
		opts.setTo(new EndpointReference(adress));
		opts.setAction("urn:getQuote");
	}

	private SOAPEnvelope createSOAPEnvelope(String symbol) {
		fac = OMAbstractFactory.getSOAP11Factory();
		envelope = fac.getDefaultEnvelope();
		envelope.getBody().addChild(createSimpleQuoteRequestBody(symbol));
		return envelope;
	}

	public OMElement toOMElement(String s) throws XMLStreamException {
		return AXIOMUtil.stringToOM(s);
	}

}
