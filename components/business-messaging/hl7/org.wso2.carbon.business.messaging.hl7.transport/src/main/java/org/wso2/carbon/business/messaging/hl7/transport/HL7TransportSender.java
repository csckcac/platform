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

package org.wso2.carbon.business.messaging.hl7.transport;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
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
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;

import javax.xml.stream.XMLStreamException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HL7TransportSender extends AbstractTransportSender {

    private Parser parser = new DefaultXMLParser();

    @Override
    public void sendMessage(MessageContext messageContext, String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {

        if (targetEPR == null) {
            handleException("Unable to send HL7 message without target EPR information");
        }

        if (log.isDebugEnabled()) {
            log.debug("Send HL7 message using EPR :" + targetEPR);
        }

        OMElement omElement = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
        String xmlFormat = omElement.toString();
        Message returnMsg = null;

        Map<String,String> params = getURLParameters(targetEPR);

        try {
            Message message = parser.parse(xmlFormat);
            ConnectionHub connectionHub = ConnectionHub.getInstance();
            Connection connection = getConnection(targetEPR, connectionHub);
            Initiator initiator = connection.getInitiator();
            String timeout = params.get(HL7Constants.TIMEOUT_PARAM);
            if (timeout != null) {
                initiator.setTimeoutMillis(Integer.parseInt(timeout));
            } else {
                initiator.setTimeoutMillis(HL7Constants.DEFAULT_TIMEOUT);
            }

            returnMsg = initiator.sendAndReceive(message);
            connectionHub.detach(connection);

            if (log.isDebugEnabled()) {
                log.debug("HL7 message successfully dispatched to URL " + targetEPR);
                log.debug("Response message received from target EP : " + returnMsg.toString());
            }

        } catch (Exception e) {
            handleException("Error while sending an HL7 message", e);
        }

        if (returnMsg != null) {
            processResponse(returnMsg, messageContext);
        } else {
            handleException("A response not received from the target HL7 endpoint");
        }
    }

    private void processResponse(Message returnMsg,
                                 MessageContext messageContext) throws AxisFault {
        try {
            MessageContext rmc = createResponseMessageContext(messageContext);
            SOAPEnvelope soapEnvelope = createEnvelope(returnMsg);
            rmc.setEnvelope(soapEnvelope);
            AxisEngine.receive(rmc);
        } catch (Exception e) {
            handleException("Error while processing the response HL7 message", e);
        }
    }

    private Connection getConnection(String targetEPR, ConnectionHub hub) throws AxisFault {
        try {
            URI url = new URI(targetEPR);
            String targetHost = url.getHost();
            int targetPort = url.getPort();
            return hub.attach(targetHost, targetPort, new PipeParser(),
                    MinLowerLayerProtocol.class);
        } catch (URISyntaxException e) {
            handleException("Malformed HL7 URI syntax: " + targetEPR, e);
        } catch (HL7Exception e) {
            handleException("Error while obtaining HL7 connection to: " + targetEPR, e);
        }
        return null;
    }

    private Map<String,String> getURLParameters(String url) throws AxisFault {
        try {
            Map<String,String> params = new HashMap<String,String>();
            URI hl7Url = new URI(url);
            String query = hl7Url.getQuery();
            if (query != null) {
                String[] paramStrings = query.split("&");
                for (String p : paramStrings) {
                    int index = p.indexOf('=');
                    params.put(p.substring(0, index), p.substring(index+1));
                }
            }
            return params;
        } catch (URISyntaxException e) {
            handleException("Malformed HL7 url", e);
        }
        return null;
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
}
