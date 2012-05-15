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

package org.wso2.carbon.business.messaging.hl7.transport.utils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.hl7.transport.HL7Constants;
import org.wso2.carbon.business.messaging.hl7.transport.HL7Endpoint;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class HL7MessageProcessor implements Application {

    private static final Log log = LogFactory.getLog(HL7MessageProcessor.class);

    private HL7Endpoint endpoint;

    public HL7MessageProcessor(HL7Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Message processMessage(Message message) throws ApplicationException, HL7Exception {
        if (log.isDebugEnabled()) {
            log.debug("Received HL7 message: " + message.toString());
        }
        try {
            MessageContext messageContext = endpoint.createMessageContext();
            messageContext.setIncomingTransportName(HL7Constants.TRANSPORT_NAME);
            messageContext.setEnvelope(createEnvelope(message));
            /* set the raw HL7 message in message context to be used if needed later */
            messageContext.setProperty(HL7Constants.HL7_RAW_MESSAGE_PROPERTY_NAME, 
            		new PipeParser().encode(message));
            AxisEngine.receive(messageContext);
            return this.handleHL7Result(messageContext, message);
        } catch (AxisFault axisFault) {
            return createNAck(message, "Error while processing the HL7 message " +
                    "through the engine", axisFault);
        } catch (XMLStreamException e) {
            return createNAck(message, "IO error while processing the HL7 content", e);
        }
    }
    
    private Message handleHL7Result(MessageContext ctx, Message hl7Msg) throws HL7Exception {
    	if (this.endpoint.isAutoAck()) {
    		return this.createAck(hl7Msg);
    	} else {
    		String resultMode = (String) ctx.getProperty(HL7Constants.HL7_RESULT_MODE);
    		if (resultMode != null) {
    			if (HL7Constants.HL7_RESULT_MODE_ACK.equals(resultMode)) {
    				return this.createAck(hl7Msg);
    			} else if (HL7Constants.HL7_RESULT_MODE_NACK.equals(resultMode)) {
    				String nackMessage = (String) ctx.getProperty(HL7Constants.HL7_NACK_MESSAGE);
    				return this.createNAck(hl7Msg, nackMessage, new Exception(nackMessage));
    			}
    		}
    	}
    	throw new HL7Exception("Application Error");
    }

    private Message createAck(Message message) throws HL7Exception {
        try {
            return message.generateACK();
        } catch (IOException e) {
            String msg = "Error while constructing an HL7 ACK";
            log.error(msg, e);
            throw new HL7Exception(msg, e);
        }
    }

    private Message createNAck(Message message, String error, Throwable t) throws HL7Exception {
        log.error(error, t);

        try {
            return message.generateACK("AE", new HL7Exception(error, t));
        } catch (IOException e) {
            String msg = "Error while constructing an HL7 NACK";
            log.error(msg, e);
            throw new HL7Exception(msg, e);
        }
    }

    private SOAPEnvelope createEnvelope(Message message) throws HL7Exception, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        Parser xmlParser = new DefaultXMLParser();
        String xmlDoc = xmlParser.encode(message);

        OMElement hl7Element = AXIOMUtil.stringToOM(xmlDoc);
        OMNamespace ns = fac.createOMNamespace("http://wso2.org/hl7", "hl7");
        OMElement topicOm = fac.createOMElement("message", ns);
        topicOm.addChild(hl7Element);
        envelope.getBody().addChild(topicOm);
        return envelope;
    }

    public boolean canProcess(Message message) {
        return true;
    }

}
