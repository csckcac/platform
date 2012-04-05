/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.paypal.mediator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.paypal.mediator.factory.OperationHeaderFactory;
import org.wso2.carbon.business.messaging.paypal.mediator.factory.OperationPayloadFactory;

import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * Mediates the requests which are extracted from the
 * <code>MessageContext</code> using the specified particular source inputs and
 * maps the response back to the <code>MessageContext</code> using the outputs
 * specified.
 * </p>
 * 
 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class PaypalMediator extends AbstractMediator implements
		ManagedLifecycle {

	/** Service client for actual PayPal service. */
	private ServiceClient sc = null;

	/** Service URL of the actual PayPal service. */
	private String serviceURL = null;

	/** Holds the location of the client repository configuration. */
	private String clientRepository = null;

	/** Holds the location of the axis2 configuration. */
	private String axis2xml = null;

	/** Holds the default value of the PayPal service EPR. */
	private final static String DEFAULT_SERVICE_URL = "https://api.sandbox.paypal.com/2.0/";

	/**
	 * The credential required for the <code>Operation</code> to be invoked.
	 * 
	 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
	 */
	private RequestCredential requestCredential;
	/**
	 * The <code>Operation</code> to be invoked.
	 * 
	 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
	 */
	private Operation operation;

    public final static String PAYPAL_RESPONSE_PROPERTY = "SYNAPSE_PAYPAL_RESPONSE_PROPERTY";

	/**
	 * <p>
	 * This extracts the <code>Input</code>'s from the
	 * <code>MessageContext</code>, invokes the <code>PayPal</code> specified
	 * <code>Operation</code> and formats the response to the specified format
	 * in <code>Output</code>.
	 * </p>
	 * 
	 * @param synCtx
	 *            message to be routed
	 * @return whether to continue further mediaiton or not as a boolean value
	 * 
	 * @see org.apache.synapse.Mediator#mediate(org.apache.synapse.MessageContext)
	 */
	public boolean mediate(MessageContext synCtx) {

		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Start : PayPal mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}
		dispatch(synCtx);
		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("End : PayPal mediator");
		}

		return true;
	}

	/**
	 * This method will invoke the actual service call.
	 * 
	 * @param synCtx
	 *            the message context.
	 */
	private void dispatch(MessageContext synCtx) {

		try {

			SynapseLog synLog = getLog(synCtx);
			if (synLog.isTraceOrDebugEnabled()) {
				synLog.traceOrDebug("Start dispatching operation: "
						+ operation.getName());
			}
            //create service client options for paypal ws API calls
			Options options = getOptions(synCtx);
			sc.setOptions(options);

            //generate headers and payload based on paypal config
			OMElement requestHeader = handleHeader(synCtx);
			OMElement requestPayload = handlePayload(synCtx);
			System.out.println("Printing RequestHeader:");
			System.out.println(requestHeader);

            if (synLog.isTraceOrDebugEnabled()) {
                final String caption = operation
				.getAction() != null ? " with action : " + operation.getAction() : "";
                synLog.traceOrDebug(String.format(
						"About to invoke service : %s", serviceURL, caption ));
                if (synLog.isTraceTraceEnabled()) {
                    synLog.traceTrace("Request message header  : "
							+ requestHeader);
                    synLog.traceTrace("Request message payload : "
							+ requestPayload);
                }
            }

            sc.addHeader(requestHeader);
            System.out.println("Printing RequestPayload :" + requestPayload);

            OMElement result = sc.sendReceive(requestPayload);
            //OMElement result = sendRecieve();

            System.out.println("Printing the result:");
            System.out.println(result);
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Response payload received : " + result);
            }

            if (result != null) {
                handleResponse(result, synCtx);
            } else {
                synLog.traceOrDebug("Service returned a null response");
            }

			if (synLog.isTraceOrDebugEnabled()) {
				synLog.traceOrDebug("End dispatching operation: "
						+ operation.getName());
			}
		} catch (JaxenException e) {
			handleException("Error handling service response", e, synCtx);
		} catch (AxisFault e) {
			final String caption = operation.getAction() != null ? " with action : "
					+ operation.getAction() : "";
			handleException(String.format("Error invoking service : %s",
					serviceURL,
					caption), e, synCtx);
		}
	}

    /**
     * mock service call for testing purposes
     * TODO remove
     * @return mock soap msg
     */
    private OMElement sendRecieve(){
        //string representation of the message
/*
        String SOAP = "<?xml version=”1.0”?>\n" +
                      "<SOAP-ENV:Envelope\n" +
                      "xmlns:SOAP-ENV=”http://schemas.xmlsoap.org/soap/envelope/”\n" +
                      " xmlns:SOAP-ENC=”http://schemas.xmlsoap.org/soap/encoding/”\n" +
                      " xmlns:xsi=”http://www.w3.org/2001/XMLSchema-instance”\n" +
                      " xmlns:xsd=”http://www.w3.org/2001/XMLSchema”\n" +
                      " xmlns:xs=”http://www.w3.org/2001/XMLSchema”\n" +
                      " xmlns:cc=”urn:ebay:apis:CoreComponentTypes”\n" +
                      " xmlns:wsu=”http://schemas.xmlsoap.org/ws/2002/07/utility”\n" +
                      " xmlns:saml=”urn:oasis:names:tc:SAML:1.0:assertion”\n" +
                      " xmlns:ds=”http://www.w3.org/2000/09/xmldsig#”\n" +
                      " xmlns:wsse=”http://schemas.xmlsoap.org/ws/2002/12/secext”\n" +
                      " xmlns:ebl=”urn:ebay:apis:eBLBaseComponents”\n" +
                      " xmlns:ns=”urn:ebay:api:PayPalAPI”>\n" +
                      " <SOAP-ENV:Header>\n" +
                      "    <Security xmlns=”http://schemas.xmlsoap.org/ws/2002/12/secext”\n" +
                      "xsi:type=”wsse:SecurityType”/>\n" +
                      "    <RequesterCredentials xmlns=”urn:ebay:api:PayPalAPI”\n" +
                      "xsi:type=”ebl:CustomSecurityHeaderType”>\n" +
                      "      <Credentials xmlns=”urn:ebay:apis:eBLBaseComponents”\n" +
                      "xsi:type=”ebl:UserIdPasswordType”/>\n" +
                      "    </RequesterCredentials>\n" +
                      " </SOAP-ENV:Header>\n" +
                      " <SOAP-ENV:Body id=”_0”>\n" +
                      " <GetBalanceResponse xmlns=”urn:ebay:api:PayPalAPI”>\n" +
                      "    <Timestamp>dateTime_in_UTC/GMT</Timestamp>\n" +
                      "    <Ack xmlns=”urn:ebay:apis:eBLBaseComponents”>Success</Ack>\n" +
                      "   <Version xmlns=”urn:ebay:apis:eBLBaseComponents”>2.0</Version>\n" +
                      "   <CorrelationId\n" +
                      "xmlns=”urn:ebay:apis:eBLBaseComponents”>234440d</CorrelationID>\n" +
                      "   <Build xmlns=”urn:ebay:apis:eBLBaseComponents”>33345</Build>\n" +
                      "            <balance>\n" +
                      "                  4700\n" +
                      "            </balance>\n" +
                      " </GetBalanceResponse>\n" +
                      " </SOAP-ENV:Body>\n" +
                      "</SOAP-ENV:Envelope>";
*/
        String SOAP = " <GetBalanceResponse xmlns=\"urn:ebay:api:PayPalAPI\" >" +
                      "    <Timestamp>dateTime_in_UTC/GMT</Timestamp>" +
                      "    <Ack xmlns=\"urn:ebay:apis:eBLBaseComponents\" >Success</Ack>" +
                      "   <Version xmlns=\"urn:ebay:apis:eBLBaseComponents\" >2.0</Version>" +
                      "   <CorrelationId" +
                      " xmlns=\"urn:ebay:apis:eBLBaseComponents\" >234440d</CorrelationID>" +
                      "   <Build xmlns=\"urn:ebay:apis:eBLBaseComponents\">33345</Build>" +
                      "            <balance>" +
                      "                  4700" +
                      "            </balance>" +
                      " </GetBalanceResponse>"
                     ;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("urn:ebay:api:PayPalAPI","ns");
        OMNamespace ns1 = factory.createOMNamespace("urn:ebay:apis:eBLBaseComponents","ns1");

        OMElement getBalElem = factory.createOMElement("GetBalanceResponse",ns);

        OMElement timestampElem = factory.createOMElement("Timestamp",ns);
        timestampElem.setText("4 3 01_15_30");
        OMElement ackElem = factory.createOMElement("Ack",ns1);
        ackElem.setText("Success");
        OMElement versionElem = factory.createOMElement("Version",ns1);
        versionElem.setText("2.0");
        OMElement corrIdElem = factory.createOMElement("CorrelationId",ns1);
        corrIdElem.setText("2334440d");
        OMElement buildElem = factory.createOMElement("Build",ns1);
        buildElem.setText("33345");
        OMElement balanceElem = factory.createOMElement("balance",ns);
        balanceElem.setText("4700");

        getBalElem.addChild(timestampElem);
        getBalElem.addChild(ackElem);
        getBalElem.addChild(versionElem);
        getBalElem.addChild(corrIdElem);
        getBalElem.addChild(buildElem);
        getBalElem.addChild(balanceElem);
        return getBalElem;
    }
	/**
	 * This method will populate all the <code>Option</code>'s required to
	 * invoke the service.
	 * 
	 * @param synCtx
	 *            the message context.
	 * @return the populated options.
	 * @throws AxisFault
	 *             the exception.
	 */
	private Options getOptions(MessageContext synCtx) throws AxisFault {

		Options options = new Options();
		options.setTo(new EndpointReference(serviceURL));

		if (null != operation.getAction()) {
			options.setAction(operation.getAction());
		} else {
			if (synCtx.isSOAP11()) {
				options.setProperty(
						Constants.Configuration.DISABLE_SOAP_ACTION, true);
			} else {
				Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
				org.apache.axis2.context.MessageContext axis2MessageCtx = axis2smc
						.getAxis2MessageContext();
				axis2MessageCtx.getTransportOut().addParameter(
						new org.apache.axis2.description.Parameter(
								HTTPConstants.OMIT_SOAP_12_ACTION, true));
			}
		}

		options.setProperty(
				AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES,
				Boolean.TRUE);
		options.setProperty(HTTPConstants.CHUNKED, false);
		return options;
	}

	/**
	 * <p>
	 * If both the <code>expression</code> provided then the evaluated string
	 * value of the <code>expression</code> over the message will be returned.
	 * </p>
	 * 
	 * @param synCtx
	 *            message to be evaluated.
	 * @return the evaluated string value of the <code>expression</code>
	 */
	private OMElement handlePayload(MessageContext synCtx) {

		log.debug("Start building request from operation" + operation);

		OMElement payload = OperationPayloadFactory.getInstance().buildPayload(
				operation, synCtx);

		log.debug("End building request from operation" + operation.getName());
		return payload;
	}
	
	/**
	 * @param synCtx
	 * @return
	 */
	public OMElement handleHeader(MessageContext synCtx) {

		log.debug("Start building request header for operation");

		OMElement headerElem = OperationHeaderFactory.getInstance().buildHeader(requestCredential, synCtx);

		log.debug("End building request header for operation");
		return headerElem;
	}
	
	/**
     * @param result
     * @param synCtx
     * @throws JaxenException
     */
    public void handleResponse(OMElement result, MessageContext synCtx) throws JaxenException {

	log.debug("Start handling response from operation" + operation.getName());
    //TODO - remove this? we no longer need paypal output handling
	for (Output output : operation.getOutputs()) {

	    if (null != output.getTargetKey()) {
	    	synCtx.setProperty(output.getTargetKey(), result);
	    } else {

		OMElement sourceNode = output.evaluate(result);
		OMElement targetNode = output.evaluate(synCtx);
		targetNode.addChild(sourceNode);
	    }

	}
    //set output collected as a property to manipulate response as required
    //use xpath extension variable $PAYPAL_RESPONSE to handle outputs if required     
    synCtx.setProperty(PAYPAL_RESPONSE_PROPERTY,result);    

	log.debug("End handling response from operation" + operation.getName());
    }
	/**
	 * Initializes the service call.
	 * 
	 * @param synEnv
	 *            the synapse environment.
	 */
	public void init(SynapseEnvironment synEnv) {

		try {
            ConfigurationContext cfgCtx = null;
            if(clientRepository!=null && axis2xml!=null){
                cfgCtx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(clientRepository, axis2xml);
            }

            if (cfgCtx!=null) {
                sc = new ServiceClient(cfgCtx, null);
            }
            else{
                sc = new ServiceClient();
            }

            serviceURL = (String) synEnv.getSynapseConfiguration()
					.getEntryDefinition("service_url").getValue();
			serviceURL = (null == serviceURL) ? DEFAULT_SERVICE_URL
					: serviceURL;
		} catch (AxisFault e) {
			handleException("Error initializing paypal mediator", e, synEnv
					.createMessageContext());
		}
	}

	/**
	 * Cleanses the reference to the service client.
	 */
	public void destroy() {
		try {
			sc.cleanup();
		} catch (AxisFault ignore) {
		}
	}

	/**
	 * Getter method for <code>clientRepository</code>
	 * 
	 * @return the client repository location.
	 */
	public String getClientRepository() {
		return clientRepository;
	}

	/**
	 * Setter method for <code>clientRepository</code>
	 * 
	 * @param clientRepository
	 *            the client repository location.
	 */
	public void setClientRepository(String clientRepository) {
		this.clientRepository = clientRepository;
	}

	/**
	 * Getter method for <code>axis2xml</code>
	 * 
	 * @return the axis2 xml configuration location.
	 */
	public String getAxis2xml() {
		return axis2xml;
	}

	/**
	 * Setter method for <code>axis2xml</code>
	 * 
	 * @param axis2xml
	 *            the axis2 xml configuration location.
	 */
	public void setAxis2xml(String axis2xml) {
		this.axis2xml = axis2xml;
	}

	/**
	 * @return the requestCredential
	 */
	public RequestCredential getRequestCredential() {
		return requestCredential;
	}

	/**
	 * @param requestCredential
	 *            the requestCredential to set
	 */
	public void setRequestCredential(RequestCredential requestCredential) {
		this.requestCredential = requestCredential;
	}

	/**
	 * Getter method for <code>operation</code>
	 * 
	 * @return the operation the operation to invoke.
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Setter method for <code>operation</code>
	 * 
	 * @param operation
	 *            the operation to invoke
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
