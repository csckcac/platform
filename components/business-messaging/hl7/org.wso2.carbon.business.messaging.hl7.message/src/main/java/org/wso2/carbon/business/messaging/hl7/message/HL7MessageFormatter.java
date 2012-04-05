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
import org.wso2.carbon.business.messaging.hl7.HL7Constants;

import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
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

        if (log.isDebugEnabled()) {
            log.debug("Inside the HL7 formatter" + omElement.toString());
        }
        if (msgCtx.getFLOW() == MessageContext.OUT_FAULT_FLOW || msgCtx.getEnvelope().hasFault()) {
            SOAPFault soapFault = msgCtx.getEnvelope().getBody().getFault();
            ACK ack = new ACK();
            try {
                //TODO : need to complete the values of the complete hl7 message, this will not properly construct by the parser
                ack.getMSH().getFieldSeparator().setValue(HL7Constants.HL7_FIELD_SEPARATOR);
                ack.getMSH().getEncodingCharacters().setValue(HL7Constants.HL7_ENCODING_CHARS);
                ack.getMSA().getAcknowledgmentCode().setValue(HL7Constants.HL7_ACK_CODE_AR);
                ack.getERR().getErrorCodeAndLocation(0).getCodeIdentifyingError().getIdentifier()
                        .setValue("Backend service reject the value");
                String msg = new PipeParser().encode(ack);
                if (log.isDebugEnabled()) {
                    log.debug("Generate HL7 error : " + ack);
                }
                outputStream.write(msg.getBytes());
                outputStream.flush();
                outputStream.close();
                msgCtx.setProperty(Constants.Configuration.CONTENT_TYPE,HL7Constants.HL7_CONTENT_TYPE);
            } catch (DataTypeException e) {
                handleException("Error on creating HL7 Error segment", e);
            } catch (HL7Exception e) {
                handleException("Error on creating HL7 Error segment", e);
            } catch (IOException e) {
                handleException("Error on writing HL7 Error to output stream", e);
            }
        } else {
            try {
                String xmlFormat = omElement.toString();
                Message message = new DefaultXMLParser().parse(xmlFormat);
                String msg = new PipeParser().encode(message);
                if (log.isDebugEnabled()) {
                    log.debug("Message inside the formatter : " + message);
                }
                outputStream.write(msg.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                handleException("Ecepion occured during HL7 message creation", e);
            }
        }
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

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }
}
