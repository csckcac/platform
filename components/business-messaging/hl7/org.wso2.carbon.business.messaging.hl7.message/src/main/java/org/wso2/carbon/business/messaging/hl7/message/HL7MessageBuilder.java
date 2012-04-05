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

import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.hl7.HL7Constants;

import java.io.InputStream;

import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.ValidationException;
import ca.uhn.hl7v2.validation.impl.ConformanceProfileRule;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

import javax.xml.stream.XMLStreamException;

public class HL7MessageBuilder implements Builder {
    private static final Log log = LogFactory.getLog(HL7MessageBuilder.class);

    /** {@inheritdoc }**/
    public OMElement processDocument(InputStream inputStream,
                                     String contentType,
                                     MessageContext messageContext) throws AxisFault {
        OMElement hl7Element = null;
        String hl7String = parseToString(inputStream);
        if (log.isDebugEnabled()) {
            log.debug("HL7 String : "+hl7String);
        }
        String hl7Xml = serializeHL7toXML(hl7String);
        /* set the raw HL7 message in message context to be used if needed later */
        messageContext.setProperty(HL7Constants.HL7_RAW_MESSAGE_PROPERTY_NAME, hl7String);
        try {
            hl7Element = AXIOMUtil.stringToOM(hl7Xml);
        } catch (XMLStreamException e) {
            handleException("Error when creating the OMNode with HL7",e);
        }
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMNamespace ns = fac.createOMNamespace("http://wso2.org/hl7", "hl7");
        OMElement messageEl = fac.createOMElement("message", ns);
        messageEl.addChild(hl7Element);
        return messageEl;
    }

    /**
     * Parse the input stream to an string
     * @param is inputstream
     * @return String value
     */
    private String parseToString(InputStream is) {
        java.io.DataInputStream din = new java.io.DataInputStream(is);
        StringBuffer sb = new StringBuffer();
        try {
            String line;
            while ((line = din.readLine()) != null) {
                sb.append(line+"\r");
            }
        } catch (Exception ex) {
            handleException("Error when reading the stream",ex);
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
                handleException("Error when InputStream closing",ex);
            }
        }
        return sb.toString();
    }

    /**
     * Serialize HL7 message to XML
     * @param rowHL7
     * @return XML String 
     */
    private String serializeHL7toXML(String rowHL7) {
        Parser xmlParser = new DefaultXMLParser();
        Parser ediParser = new PipeParser();
        ediParser.setValidationContext(new NoValidation());
        String xmlDoc = null;
        try {
            Message message = ediParser.parse(rowHL7);
            ConformanceProfileRule rule = new ConformanceProfileRule();
    		ValidationException[] exs = rule.test(message);
    		if (exs != null && exs.length > 0) {
    			throw new HL7Exception(exs[0].getMessage());
    		}	
            if(log.isDebugEnabled()){
                log.debug("HL7 parsing completed." + message);
            }
            xmlDoc = xmlParser.encode(message);
        } catch (HL7Exception e) {
            handleException("Error on converting to HL7",e);
        }
        return xmlDoc;
    }
    
    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message,e);
    }
}
