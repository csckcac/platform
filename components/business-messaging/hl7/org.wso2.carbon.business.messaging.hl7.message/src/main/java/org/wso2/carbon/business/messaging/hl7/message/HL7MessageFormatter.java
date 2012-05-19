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
package org.wso2.carbon.business.messaging.hl7.message;

import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.hl7.common.HL7Constants;
import org.wso2.carbon.business.messaging.hl7.common.HL7ProcessingContext;

import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import ca.uhn.hl7v2.HL7Exception;

/*
*  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
public class HL7MessageFormatter implements MessageFormatter {
	
    private static final Log log = LogFactory.getLog(HL7MessageFormatter.class);
    
    private DefaultXMLParser xmlParser;
    
    private PipeParser pipeParser;
    
    public HL7MessageFormatter() {
    	this.xmlParser = new DefaultXMLParser();
    	this.pipeParser = new PipeParser();
    	this.xmlParser.setValidationContext(new NoValidation());
    	this.pipeParser.setValidationContext(new NoValidation());
    }

    /**
     * {@inheritdoc }*
     */
    public byte[] getBytes(MessageContext messageContext, OMOutputFormat omOutputFormat)
            throws AxisFault {
        return new byte[0];
    }

    /**
     * {@inheritdoc }*
     */
    public void writeTo(MessageContext msgCtx,
                        OMOutputFormat omOutputFormat,
                        OutputStream outputStream,
                        boolean b) throws AxisFault {
        OMElement omElement = msgCtx.getEnvelope().getBody().getFirstElement().getFirstElement();
        HL7ProcessingContext processingCtx;
        try {
        	/* The AxisService retrieved at this point from the 
        	 * message context is not the correct one */
        	processingCtx = new HL7ProcessingContext(true, false, null, null);
        } catch (Exception e) {
			throw new AxisFault(e.getMessage(), e);
		}
        if (log.isDebugEnabled()) {
            log.debug("Inside the HL7 formatter: " + omElement.toString());
        }
        if (msgCtx.getFLOW() == MessageContext.OUT_FAULT_FLOW || msgCtx.getEnvelope().hasFault()) {
            SOAPFault soapFault = msgCtx.getEnvelope().getBody().getFault();
            try {
                Message message = processingCtx.createNack((Message) msgCtx.getProperty(
                		HL7Constants.HL7_MESSAGE_OBJECT), soapFault.getReason().getText());
                if (log.isDebugEnabled()) {
                    log.debug("Generate HL7 error: " + message);
                }
                this.writeMessageOut(message, outputStream, msgCtx);
            } catch (HL7Exception e) {
                throw new AxisFault("Error on creating HL7 Error segment: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new AxisFault("Error on writing HL7 Error to output stream: " + 
                        e.getMessage(), e);
            }
        } else {
            try {
            	String xmlFormat = omElement.toString();
                Message message = this.xmlParser.parse(xmlFormat);
                if (log.isDebugEnabled()) {
                    log.debug("Message inside the formatter: " + message);
                }
            	if (this.isGenerateMessageAck(msgCtx)) {
            		message = this.createGeneratedMessageAck(processingCtx, message, msgCtx);
            	}
                this.writeMessageOut(message, outputStream, msgCtx);
            } catch (Exception e) {
                throw new AxisFault("Error occured during HL7 message creation: " + 
                        e.getMessage(), e);
            }
        }
    }
    
    private Message createGeneratedMessageAck(HL7ProcessingContext processingCtx, 
    		Message message, MessageContext msgCtx) throws HL7Exception {
    	try {
    		return processingCtx.handleHL7Result(msgCtx, message);
    	} catch (HL7Exception e) {
			return processingCtx.createNack(null, e.getMessage());
		}
    }
    
    private boolean isGenerateMessageAck(MessageContext msgCtx) {
    	Object param = msgCtx.getProperty(HL7Constants.HL7_GENERATE_ACK);
    	if (param != null) {
    		return Boolean.parseBoolean(param.toString());
    	} else {
    	    return false;
    	}
    }
    
    private void writeMessageOut(Message message, OutputStream out, 
    		MessageContext msgCtx) throws IOException, HL7Exception {
    	msgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, 
        		HL7Constants.HL7_CONTENT_TYPE);
    	String txtMsg = this.pipeParser.encode(message);
        out.write(txtMsg.getBytes());
        out.flush();
    }

    /**
     * {@inheritdoc }*
     */
    public String getContentType(MessageContext msgCtx,
                                 OMOutputFormat omOutputFormat,
                                 String s) {
        String contentType = (String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE);
        if (contentType == null) {
            contentType = HL7Constants.HL7_CONTENT_TYPE;
        }

        String encoding = omOutputFormat.getCharSetEncoding();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        return contentType;
    }

    /**
     * {@inheritdoc }*
     */
    public URL getTargetAddress(MessageContext messageContext,
                                OMOutputFormat omOutputFormat,
                                URL targetURL) throws AxisFault {
        return URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);
    }

    /**
     * {@inheritdoc }*
     */
    public String formatSOAPAction(MessageContext messageContext,
                                   OMOutputFormat omOutputFormat,
                                   String soapAction) {
        return soapAction;
    }

}
