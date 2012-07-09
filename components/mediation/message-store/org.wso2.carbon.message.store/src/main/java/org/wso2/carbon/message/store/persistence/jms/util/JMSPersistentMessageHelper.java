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
package org.wso2.carbon.message.store.persistence.jms.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.util.UUIDGenerator;
import org.wso2.carbon.message.store.persistence.jms.message.JMSPersistentAxis2Message;
import org.wso2.carbon.message.store.persistence.jms.message.JMSPersistentMessage;
import org.wso2.carbon.message.store.persistence.jms.message.JMSPersistentSynapseMessage;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

public class JMSPersistentMessageHelper {

    private SynapseEnvironment synapseEnvironment;

    private Log log = LogFactory.getLog(JMSPersistentMessage.class);

    public JMSPersistentMessageHelper(SynapseEnvironment se) {
        this.synapseEnvironment = se;
    }


    public MessageContext createMessageContext(JMSPersistentMessage message) {

        SynapseConfiguration configuration = synapseEnvironment.getSynapseConfiguration();

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2SynapseEnvironment)
                synapseEnvironment).getAxis2ConfigurationContext().createMessageContext();


        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        JMSPersistentAxis2Message jmsAxis2MessageContext = message.getJmsPersistentAxis2Message();
        SOAPEnvelope envelope = getSoapEnvelope(jmsAxis2MessageContext.getSoapEnvelope());

        try {

            msgCtx.setEnvelope(envelope);
            // set the RMSMessageDto properties
            msgCtx.getOptions().setAction(jmsAxis2MessageContext.getAction());
            if (jmsAxis2MessageContext.getRelatesToMessageId() != null) {
                msgCtx.addRelatesTo(new RelatesTo(jmsAxis2MessageContext.getRelatesToMessageId()));
            }
            msgCtx.setMessageID(jmsAxis2MessageContext.getMessageID());
            msgCtx.getOptions().setAction(jmsAxis2MessageContext.getAction());

            AxisService axisService =
                    axisConfiguration.getServiceForActivation(jmsAxis2MessageContext.getService());

            AxisOperation axisOperation =
                    axisService.getOperation(jmsAxis2MessageContext.getOperationName());

            msgCtx.setFLOW(jmsAxis2MessageContext.getFLOW());
            ArrayList executionChain = new ArrayList();
            if (jmsAxis2MessageContext.getFLOW() ==
                    org.apache.axis2.context.MessageContext.OUT_FLOW) {
                executionChain.addAll(axisOperation.getPhasesOutFlow());
                executionChain.addAll(axisConfiguration.getOutFlowPhases());

            } else if (jmsAxis2MessageContext.getFLOW() ==
                    org.apache.axis2.context.MessageContext.OUT_FAULT_FLOW) {
                executionChain.addAll(axisOperation.getPhasesOutFaultFlow());
                executionChain.addAll(axisConfiguration.getOutFlowPhases());
            }

            msgCtx.setExecutionChain(executionChain);


            ConfigurationContext configurationContext = msgCtx.getConfigurationContext();

            msgCtx.setAxisService(axisService);
            ServiceGroupContext serviceGroupContext =
                    configurationContext.createServiceGroupContext(axisService.getAxisServiceGroup());
            ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);

            OperationContext operationContext = serviceContext.createOperationContext(
                    jmsAxis2MessageContext.getOperationName());
            msgCtx.setServiceContext(serviceContext);
            msgCtx.setOperationContext(operationContext);

            msgCtx.setAxisService(axisService);
            msgCtx.setAxisOperation(axisOperation);
            if (jmsAxis2MessageContext.getReplyToAddress() != null) {
                msgCtx.setReplyTo(new EndpointReference(jmsAxis2MessageContext.getReplyToAddress().trim()));
            }

            if (jmsAxis2MessageContext.getFaultToAddress() != null) {
                msgCtx.setFaultTo(new EndpointReference(jmsAxis2MessageContext.getFaultToAddress().trim()));
            }

            if (jmsAxis2MessageContext.getFromAddress() != null) {
                msgCtx.setFrom(new EndpointReference(jmsAxis2MessageContext.getFromAddress().trim()));
            }

            if (jmsAxis2MessageContext.getToAddress() != null) {
                msgCtx.getOptions().setTo(new EndpointReference(jmsAxis2MessageContext.getToAddress().trim()));
            }

            msgCtx.setProperties(jmsAxis2MessageContext.getProperties());
            msgCtx.setTransportIn(axisConfiguration.
                    getTransportIn(jmsAxis2MessageContext.getTransportInName()));
            msgCtx.setTransportOut(axisConfiguration.
                    getTransportOut(jmsAxis2MessageContext.getTransportOutName()));

            JMSPersistentSynapseMessage jmsSynpaseMessageContext
                    = message.getJmsPersistentSynapseMessage();

            org.apache.synapse.MessageContext synCtx =
                    new Axis2MessageContext(msgCtx, configuration, synapseEnvironment);
            synCtx.setTracingState(jmsSynpaseMessageContext.getTracingState());

            Iterator<String> it = jmsSynpaseMessageContext.getProperties().keySet().iterator();

            while (it.hasNext()) {
                String key = it.next();
                Object value = jmsSynpaseMessageContext.getProperties().get(key);

                synCtx.setProperty(key, value);
            }

            synCtx.setFaultResponse(jmsSynpaseMessageContext.isFaultResponse());
            synCtx.setResponse(jmsSynpaseMessageContext.isResponse());

            return synCtx;
        } catch (Exception e) {
            log.error("Error while deserializing the JMS Message " + e);
            return null;
        }

    }


    public JMSPersistentMessage createPersistentMessage(MessageContext synCtx) {

        JMSPersistentMessage jmsMsg = new JMSPersistentMessage();
        JMSPersistentAxis2Message jmsAxis2MessageContext = new JMSPersistentAxis2Message();
        JMSPersistentSynapseMessage jmsSynpaseMessageContext = new JMSPersistentSynapseMessage();

        Axis2MessageContext axis2MessageContext = null;
        if (synCtx instanceof Axis2MessageContext) {

            /**
             * Serializing the Axis2 Message Context
             */
            axis2MessageContext = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext msgCtx =
                    axis2MessageContext.getAxis2MessageContext();

            jmsAxis2MessageContext.setMessageID(UUIDGenerator.getUUID());
            jmsAxis2MessageContext.setOperationAction(msgCtx.getAxisOperation().getSoapAction());
            jmsAxis2MessageContext.setOperationName(msgCtx.getAxisOperation().getName());

            jmsAxis2MessageContext.setAction(msgCtx.getOptions().getAction());
            jmsAxis2MessageContext.setService(msgCtx.getAxisService().getName());

            if (msgCtx.getRelatesTo() != null) {
                jmsAxis2MessageContext.setRelatesToMessageId(msgCtx.getRelatesTo().getValue());
            }
            if (msgCtx.getReplyTo() != null) {
                jmsAxis2MessageContext.setReplyToAddress(msgCtx.getReplyTo().getAddress());
            }
            if (msgCtx.getFaultTo() != null) {
                jmsAxis2MessageContext.setFaultToAddress(msgCtx.getFaultTo().getAddress());
            }
            if (msgCtx.getTo() != null) {
                jmsAxis2MessageContext.setToAddress(msgCtx.getTo().getAddress());
            }

            jmsAxis2MessageContext.setDoingPOX(msgCtx.isDoingREST());
            jmsAxis2MessageContext.setDoingMTOM(msgCtx.isDoingMTOM());
            jmsAxis2MessageContext.setDoingSWA(msgCtx.isDoingSwA());

            String soapEnvelope = msgCtx.getEnvelope().toString();
            jmsAxis2MessageContext.setSoapEnvelope(soapEnvelope);
            jmsAxis2MessageContext.setFLOW(msgCtx.getFLOW());


            jmsAxis2MessageContext.setTransportInName(msgCtx.getTransportIn().getName());
            jmsAxis2MessageContext.setTransportOutName(msgCtx.getTransportOut().getName());
            Iterator<String> it = msgCtx.getProperties().keySet().iterator();

            while (it.hasNext()) {
                String key = it.next();
                Object v = msgCtx.getProperty(key);

                String value=null;

                if( v!= null) {
                    value = v.toString();
                }

                jmsAxis2MessageContext.addProperty(key, value);
            }

            jmsMsg.setJmsPersistentAxis2Message(jmsAxis2MessageContext);

            jmsSynpaseMessageContext.setFaultResponse(synCtx.isFaultResponse());
            jmsSynpaseMessageContext.setTracingState(synCtx.getTracingState());
            jmsSynpaseMessageContext.setResponse(synCtx.isResponse());


            Iterator<String> its = synCtx.getPropertyKeySet().iterator();
            while (its.hasNext()) {

                String key = its.next();
                Object v = synCtx.getProperty(key);

                String value = null;

                if(v!=null){
                    value =v.toString();
                }
                jmsSynpaseMessageContext.addPropertie(key, value);

            }

            jmsMsg.setJmsPersistentSynapseMessage(jmsSynpaseMessageContext);

        } else {
            throw new SynapseException("Only Axis2 Messages are supported with JMSMessage store");
        }


        return jmsMsg;
    }

    private SOAPEnvelope getSoapEnvelope(String soapEnvelpe) {
        try {
            //This is a temporary fix for ESBJAVA-1157 for Andes based(QPID) Client libraries
            Thread.currentThread().setContextClassLoader(SynapseEnvironment.class.getClassLoader());
            XMLStreamReader xmlReader =
                    StAXUtils.createXMLStreamReader(new ByteArrayInputStream(getUTF8Bytes(soapEnvelpe)));
            StAXBuilder builder = new StAXSOAPModelBuilder(xmlReader);
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) builder.getDocumentElement();
            soapEnvelope.build();
            String soapNamespace = soapEnvelope.getNamespace().getNamespaceURI();
            if (soapEnvelope.getHeader() == null) {
                SOAPFactory soapFactory = null;
                if (soapNamespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    soapFactory = OMAbstractFactory.getSOAP12Factory();
                } else {
                    soapFactory = OMAbstractFactory.getSOAP11Factory();
                }
                soapFactory.createSOAPHeader(soapEnvelope);
            }
            return soapEnvelope;
        } catch (XMLStreamException e) {
            log.error("Error while deserializing the SOAP " + e);
            return null;
        }
    }
    
	private byte[] getUTF8Bytes(String soapEnvelpe) {
		byte[] bytes = null;
		try {
			bytes = soapEnvelpe.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to extract bytes in UTF-8 encoding. "
					+ "Extracting bytes in the system default encoding"
					+ e.getMessage());
			bytes = soapEnvelpe.getBytes();
		}
		return bytes;
	}


}
