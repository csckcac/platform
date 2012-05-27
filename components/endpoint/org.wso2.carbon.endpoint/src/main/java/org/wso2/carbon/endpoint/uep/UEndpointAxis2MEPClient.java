package org.wso2.carbon.endpoint.uep;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.AnonymousServiceFactory;
import org.apache.synapse.core.axis2.AsyncCallback;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.util.MessageHelper;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/**
 * This is a simple client that handles both in only and in out for UEndpoints.
 * here we are not going to use <code>Synapse.Axis2FlexibleMEPClient</code>
 */
public class UEndpointAxis2MEPClient {
	private static final Log log = LogFactory.getLog(UEndpointAxis2MEPClient.class);

	// here we don't need to set  endpoint configurations to
	// messagecontext,UEP handler handles it.

	public static void send(UnifiedEndpoint endpoint, EndpointDefinition endpointDefinition,
	                        org.apache.synapse.MessageContext synapseOutMessageContext)
	                                                                                   throws AxisFault {

		if(log.isDebugEnabled()){
			log.debug("UEndpointAxis2Client is executing..");
		}
		
		boolean wsSecurityEnabled = false;
		boolean wsAddressingEnabled = false;
		boolean wsRMEnabled = false;
		boolean separateListener = false;

		if (endpointDefinition != null) {
			if (endpointDefinition.isAddressingOn()) {
				wsAddressingEnabled = true;
			}
			if (endpointDefinition.isSecurityOn()) {
				wsSecurityEnabled = true;
			}
			if (endpointDefinition.isReliableMessagingOn()) {
				wsRMEnabled = true;
			}
			if (endpointDefinition.isUseSeparateListener()) {
				separateListener = true;
			}
		}

		// save the original message context without altering it, so we can tie
		// the response with this
		MessageContext originalInMsgCtx =
		                                  ((Axis2MessageContext) synapseOutMessageContext).getAxis2MessageContext();

		// create a new MessageContext to be sent out as this should not corrupt
		// the original we need to create the response to the original message
		// later on
		String preserveAddressingProperty =
		                                    (String) synapseOutMessageContext.getProperty(SynapseConstants.PRESERVE_WS_ADDRESSING);
		MessageContext axisOutMsgCtx = cloneForSend(originalInMsgCtx, preserveAddressingProperty);

		// set 'To' to Unified endpoint since UEP handler needs to process the message.

		axisOutMsgCtx.setTo(endpoint);

		if (log.isDebugEnabled()) {
			log.debug("Message [Original Request Message ID : " +
			          synapseOutMessageContext.getMessageID() + "]" +
			          " [New Cloned Request Message ID : " + axisOutMsgCtx.getMessageID() + "]");
		}

		// remove the headers if we don't need to preserve them.
		// determine weather we need to preserve the processed headers
		String preserveHeaderProperty =
		                                (String) synapseOutMessageContext.getProperty(SynapseConstants.PRESERVE_PROCESSED_HEADERS);
		if (preserveHeaderProperty == null || !Boolean.parseBoolean(preserveHeaderProperty)) {
			// default behaviour is to remove the headers
			MessageHelper.removeProcessedHeaders(axisOutMsgCtx,
			                                     (preserveAddressingProperty != null && Boolean.parseBoolean(preserveAddressingProperty)));
		}

		ConfigurationContext axisCfgCtx = axisOutMsgCtx.getConfigurationContext();
		AxisConfiguration axisCfg = axisCfgCtx.getAxisConfiguration();

		AxisService anonymousService =
		                               AnonymousServiceFactory.getAnonymousService(synapseOutMessageContext.getConfiguration(),
		                                                                           axisCfg,
		                                                                           wsAddressingEnabled,
		                                                                           wsRMEnabled,
		                                                                           wsSecurityEnabled);
		// mark the anon services created to be used in the client side of
		// synapse as hidden from the server side of synapse point of view
		anonymousService.getParent().addParameter(SynapseConstants.HIDDEN_SERVICE_PARAM, "true");

		// Engage UEP module
		anonymousService.engageModule(axisCfg.getModule(UEndpointConstants.UNIFIED_ENDPOINT_MODULE));

		ServiceGroupContext sgc =
		                          new ServiceGroupContext(
		                                                  axisCfgCtx,
		                                                  (AxisServiceGroup) anonymousService.getParent());
		ServiceContext serviceCtx = sgc.getServiceContext(anonymousService);

		boolean outOnlyMessage =
		                         "true".equals(synapseOutMessageContext.getProperty(SynapseConstants.OUT_ONLY));

		// get a reference to the DYNAMIC operation of the Anonymous Axis2
		// service
		AxisOperation axisAnonymousOperation =
		                                       anonymousService.getOperation(outOnlyMessage
		                                                                                   ? new QName(
		                                                                                               AnonymousServiceFactory.OUT_ONLY_OPERATION)
		                                                                                   : new QName(
		                                                                                               AnonymousServiceFactory.OUT_IN_OPERATION));

		Options clientOptions = MessageHelper.cloneOptions(originalInMsgCtx.getOptions());
		clientOptions.setUseSeparateListener(separateListener);

		OperationClient mepClient = axisAnonymousOperation.createClient(serviceCtx, clientOptions);
		mepClient.addMessageContext(axisOutMsgCtx);
		axisOutMsgCtx.setAxisMessage(axisAnonymousOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));

		// set the SEND_TIMEOUT for transport sender
		if (endpointDefinition != null && endpointDefinition.getTimeoutDuration() > 0) {
			axisOutMsgCtx.setProperty(SynapseConstants.SEND_TIMEOUT,
			                          endpointDefinition.getTimeoutDuration());
		}

		Options mepClientOptions = mepClient.getOptions();

		mepClientOptions.setTo(endpoint);

		if (!outOnlyMessage) {
			// always set a callback as we decide if the send is blocking or non
			// blocking within the MEP client. 
			AsyncCallback callback = new AsyncCallback(synapseOutMessageContext);
			if (endpointDefinition != null) {
				// set the timeout time and the timeout action to the callback,
				// so that the' TimeoutHandler' can detect timed out callbacks and take
				// approprite action.
				callback.setTimeOutOn(System.currentTimeMillis() +
				                      endpointDefinition.getTimeoutDuration());
				callback.setTimeOutAction(endpointDefinition.getTimeoutAction());
			} else {
				callback.setTimeOutOn(System.currentTimeMillis());
			}
			mepClient.setCallback(callback);
		}

		// this is a temporary fix for converting messages from HTTP 1.1
		// chunking to HTTP 1.0.
		// Without this HTTP transport can block & become unresponsive because
		// we are streaming
		// HTTP 1.1 messages and HTTP 1.0 require the whole message to caculate
		// the content length
		if (originalInMsgCtx.isPropertyTrue(UEndpointConstants.NHTTP_FORCE_HTTP_1_0)) {
			synapseOutMessageContext.getEnvelope().toString();
		}

		// with the nio transport, this causes the listener not to write a 202
		// Accepted response, as this implies that Synapse does not yet know if
		// a 202 or 200 response would be written back.
		originalInMsgCtx.getOperationContext()
		                .setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN, "SKIP");

		// if the transport out is explicitly set use it
		Object o = originalInMsgCtx.getProperty("TRANSPORT_OUT_DESCRIPTION");
		if (o != null && o instanceof TransportOutDescription) {
			axisOutMsgCtx.setTransportOut((TransportOutDescription) o);
			clientOptions.setTransportOut((TransportOutDescription) o);
			clientOptions.setProperty("TRANSPORT_OUT_DESCRIPTION", o);
		}

		mepClient.execute(true);
		if (wsRMEnabled) {
			Object rm11 = clientOptions.getProperty(SandeshaClientConstants.RM_SPEC_VERSION);
			if ((rm11 != null) && rm11.equals(Sandesha2Constants.SPEC_VERSIONS.v1_1)) {
				ServiceClient serviceClient =
				                              new ServiceClient(
				                                                axisOutMsgCtx.getConfigurationContext(),
				                                                axisOutMsgCtx.getAxisService());
				serviceClient.setTargetEPR(new EndpointReference(
				                                                 endpointDefinition.getAddress(synapseOutMessageContext)));
				serviceClient.setOptions(clientOptions);
				serviceClient.getOptions()
				             .setTo(new EndpointReference(
				                                          endpointDefinition.getAddress(synapseOutMessageContext)));
				SandeshaClient.terminateSequence(serviceClient);
			}
		}
	}
	

	
	
	
	private static MessageContext cloneForSend(MessageContext ori, String preserveAddressing)
	                                                                                         throws AxisFault {

		MessageContext newMC = MessageHelper.clonePartially(ori);

		newMC.setEnvelope(ori.getEnvelope());
		if (preserveAddressing != null && Boolean.parseBoolean(preserveAddressing)) {
			newMC.setMessageID(ori.getMessageID());
		} else {
			MessageHelper.removeAddressingHeaders(newMC);
		}

		newMC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
		                  ori.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));

		return newMC;
	}

	public static void clearSecurtityProperties(Options options) {

		Options current = options;
		while (current != null && current.getProperty(SynapseConstants.RAMPART_POLICY) != null) {
			current.setProperty(SynapseConstants.RAMPART_POLICY, null);
			current = current.getParent();
		}
	}
}
