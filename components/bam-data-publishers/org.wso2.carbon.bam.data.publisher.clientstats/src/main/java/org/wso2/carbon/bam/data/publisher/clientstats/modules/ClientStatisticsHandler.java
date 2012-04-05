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

package org.wso2.carbon.bam.data.publisher.clientstats.modules;

/* This handler is used to generate operation level user defined data. (Client Statistics)
 * Details are stored with BAM DB(BAM_OPERATION_USERDEFINED_DATA).
 * 
 * Redirecting the request to BAM common subscriber endpoint, while Service request is allowed  to go in its path.
 * 
 * The event format for the operation level user defined data is as follows.
 * (Collecting statistics logic is followed from system statistics module.)
 */

/* 
 *(1)<oprusrdata:Event xmlns:oprusrdata="http://wso2.org/ns/2009/09/bam/operation/user-defined/data">
 *(2)	<oprusrdata:OperationUserDefinedData>
 *(3)		<oprusrdata:OperationName>echo</oprusrdata:OperationName>
 *(4)		<oprusrdata:ServiceName>MyService</oprusrdata:ServiceName>
 *(5)		<oprusrdata:ServerName>https://10.100.1.143:9444</oprusrdata:ServerName>
 *(6)		<oprusrdata:Data>
 *(7)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_MaxProcessingTime</oprusrdata:Key>
 *(8)			<oprusrdata:Value>1937</oprusrdata:Value>
 *(9)		</oprusrdata:Data>
 *(11)		<oprusrdata:Data>
 *(12)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_AverageProcessingTime</oprusrdata:Key>
 *(13)			<oprusrdata:Value>1937.0</oprusrdata:Value>
 *(14)		</oprusrdata:Data>
 *(15)		<oprusrdata:Data>
 *(16)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_MinProcessingTime</oprusrdata:Key>
 *(17)			<oprusrdata:Value>1937</oprusrdata:Value>
 *(18)		</oprusrdata:Data>
 *(19)		<oprusrdata:Data>
 *(20)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_RequestCount</oprusrdata:Key>
 *(21)			<oprusrdata:Value>1</oprusrdata:Value>
 *(22)		</oprusrdata:Data>
 *(23)		<oprusrdata:Data>
 *(24)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_ResponseCount</oprusrdata:Key>
 *(25)			<oprusrdata:Value>1</oprusrdata:Value>
 *(26) 		</oprusrdata:Data>
 *(27)	<oprusrdata:Data>
 *(28)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_FaultCount</oprusrdata:Key>
 *(29)			<oprusrdata:Value>0</oprusrdata:Value>
 *(30)		</oprusrdata:Data>
 *(31)	<oprusrdata:Data>
 *(32)			<oprusrdata:Key>orderclient_ca3a545c-64b7-4d0d-8b6e-fcd33b249322_IPAddress</oprusrdata:Key>
 *(33)			<oprusrdata:Value></oprusrdata:Value>
 *(34)		</oprusrdata:Data>
 *(35)	</oprusrdata:OperationUserDefinedData>
 *(36)</oprusrdata:Event>
 */

import java.io.StringReader;
import java.util.UUID;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.clientstats.ResponseTimeProcessor;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;
import org.wso2.carbon.bam.data.publisher.clientstats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.clientstats.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;
import org.wso2.carbon.bam.data.publisher.clientstats.events.ClientStatisticsEvent;
import org.wso2.carbon.bam.data.publisher.clientstats.events.ClientStatisticsThresholdEvent;
import org.wso2.carbon.bam.data.publisher.clientstats.services.ClientStatisticsPublisherAdmin;

public class ClientStatisticsHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(ClientStatisticsHandler.class);
    static ResponseTimeProcessor processor = new ResponseTimeProcessor();

    private String uuid = "";
    private String userParam = "";
    private String service = "";
    private String operation = "";
    private String bam_httpsServer = "";
    private String remoteIPAddress = "";
    private String wsas_server = "";
  
    static int requestCount = 0;
    static int responseCount = 0;
    static int faultCount = 0;

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
     
        boolean notification = true;

        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_HTTPS_SERVER_PROPERTY) != null) {
            bam_httpsServer = msgContext
                    .getConfigurationContext()
                    .getProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_HTTPS_SERVER_PROPERTY)
                    .toString();
        } else {
            log.error("BAM https server property not found");
        }
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_WSAS_SERVER_PROPERTY) != null) {
            wsas_server = msgContext
                    .getConfigurationContext()
                    .getProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_WSAS_SERVER_PROPERTY)
                    .toString();
        } else {
            log.error("https server property not found");
        }

        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY) != null) {
            service = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY)
                    .toString();
        } else {
            log.error("ServiceName property not found");
        }
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY) != null) {
            operation = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY)
                    .toString();
        } else {
            log.error("OperationName property not found");
        }
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_UUID_PROPERTY) != null) {
            uuid = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_UUID_PROPERTY)
                    .toString();
        } else {
            log.error("UUID property not found");
        }
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_USER_PARAM_PROPERTY) != null) {
            userParam = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_USER_PARAM_PROPERTY)
                    .toString();
        } else {
            log.error("userParam property not found");
        }
        if (msgContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_REMOTE_IPADDRESS_PROPERTY) != null) {
            remoteIPAddress = msgContext
                    .getConfigurationContext()
                    .getProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_REMOTE_IPADDRESS_PROPERTY)
                    .toString();
        } else {
            log.error("RemoteIPAddress property not found");
        }

        SOAPEnvelope soapEnvelope = msgContext.getEnvelope();
        String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
        SOAPFactory soapFactory = null;
        if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Not a standard soap message");
        }
        SOAPEnvelope newEnv = soapFactory.createSOAPEnvelope();
        soapFactory.createSOAPHeader(newEnv);
        soapFactory.createSOAPBody(newEnv);

        MessageContext eventMessageContext = new MessageContext();

        eventMessageContext.setOperationContext(msgContext.getOperationContext());
        eventMessageContext.setMessageID(UUID.randomUUID().toString());
        eventMessageContext.setTransportOut(msgContext.getTransportOut());
        eventMessageContext.setOptions(new Options());
        eventMessageContext.setEnvelope(newEnv);

        ClientStatisticsPublisherAdmin clientStatisticsPublisherAdmin = new ClientStatisticsPublisherAdmin();
        PublisherUtils.setClientStatisticsPublisherAdmin(clientStatisticsPublisherAdmin);

        EventingConfigData eventingConfigData = PublisherUtils.getClientStatisticsPublisherAdmin()
                .getEventingConfigData();

        if (eventingConfigData != null && eventingConfigData.eventingEnabled()) {

            faultCount = getOperationFaultCount(msgContext);
            requestCount = getOperationRequestCount(msgContext);
            responseCount = getOperationResponseCount(msgContext);
            double averageResponseTime = getAvgOperationResponseTime(msgContext);
            long maxResponseTime = getMaxOperationResponseTime(msgContext);
            long minResponseTime = getMinOperationResponseTime(msgContext);

            OMElement statMessage = PublisherUtils.getEventPayload(userParam, uuid,
                    averageResponseTime, minResponseTime, maxResponseTime, requestCount,
                    responseCount, faultCount, service, operation, remoteIPAddress, wsas_server);

            ClientStatisticsEvent<OMElement> event = null;
            try {
                event = new ClientStatisticsThresholdEvent<OMElement>(statMessage);
                SOAPEnvelope eventEnv = eventMessageContext.getEnvelope();

                String resourcePath = ClientStatisticsPublisherConstants.BAM_REG_PATH;

                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                OMNamespace omNs = omFactory
                        .createOMNamespace(
                                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_NOTIFICATION_NAMESPACE,
                                "ns");
                SOAPHeaderBlock soapHeaderBlock = eventEnv.getHeader().addHeaderBlock(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_TOPIC,
                        omNs);
                soapHeaderBlock
                        .setText(resourcePath
                                + ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_TOPIC_SEPARATOR
                        + ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_NAME);

                SOAPBody body = eventEnv.getBody();
                body.addChild(createOMElement(event.getMessage().toString()));
                eventMessageContext
                        .getOptions()
                        .setAction(
                                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_PUBLISH_ACTION);

                eventMessageContext
                        .setTo(new EndpointReference(
                                bam_httpsServer
                                        + ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_COMMON_SUBSCRIBER_SERVICE));
                eventMessageContext.setEnvelope(eventEnv);
                eventMessageContext
                        .setProperty(
                                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_NOTIFICATION_PROPERTY,
                                notification);
                msgContext
                        .setProperty(
                                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_NOTIFICATION_PROPERTY,
                                notification);
                try {
                    AxisEngine.send(eventMessageContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                log
                        .error(
                                "ClientResponseTimeHandler - Unable to send notification for stat threshold",
                                e);
            }
        }

        return InvocationResponse.CONTINUE;
    }

    public static OMElement createOMElement(String xml) {
        try {

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement omElement = builder.getDocumentElement();
            return omElement;

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSystemRequestCount(MessageContext messageContext) {
        Object globalCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_REQUEST_COUNTER_PROPERTY);
        if (globalCounterObj != null) {
            if (globalCounterObj instanceof Counter) {
                return ((Counter) globalCounterObj).getCount();
            }
        }
        return 0;
    }

    public int getSystemFaultCount(MessageContext messageContext) {
        Object globalCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_FAULT_COUNTER_PROPERTY);
        if (globalCounterObj != null) {
            if (globalCounterObj instanceof Counter) {
                return ((Counter) globalCounterObj).getCount();
            }
        }
        return 0;
    }

    public int getSystemResponseCount(MessageContext messageContext) {
        Object globalCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_RESPONSE_COUNTER_PROPERTY);
        if (globalCounterObj != null) {
            if (globalCounterObj instanceof Counter) {
                return ((Counter) globalCounterObj).getCount();
            }
        }
        return 0;
    }

    public double getAvgSystemResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getAvgResponseTime();
            }
        }
        return 0;
    }

    public long getMaxSystemResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMaxResponseTime();
            }
        }
        return 0;
    }

    public long getMinSystemResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMinResponseTime();
            }
        }
        return 0;
    }

    public int getServiceRequestCount(MessageContext messageContext) {
        Object serviceCounterObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_REQUEST_COUNTER_PROPERTY);
        if (serviceCounterObj != null) {
            if (serviceCounterObj instanceof Counter) {
                return ((Counter) serviceCounterObj).getCount();
            }
        }
        return 0;
    }

    public int getServiceFaultCount(MessageContext messageContext) {
        Object serviceCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_FAULT_COUNTER_PROPERTY);
        if (serviceCounterObj != null) {
            if (serviceCounterObj instanceof Counter) {
                return ((Counter) serviceCounterObj).getCount();
            }
        }
        return 0;
    }

    // public int getServiceResponseCount(MessageContext messageContext) {
    // int count = 0;
    // for (Iterator opIter = axisService.getOperations(); opIter.hasNext();) {
    // AxisOperation axisOp = (AxisOperation) opIter.next();
    // Parameter parameter = axisOp
    // .getParameter(ClientStatisticsPublisherConstants.BAM_SERVER_USER_DEFINED_OUT_OPERATION_COUNTER);
    // if (parameter != null) {
    // count += ((Counter) parameter.getValue()).getCount();
    // }
    // }
    // return count;
    // }

    public long getMaxServiceResponseTime(MessageContext messageContext) {

        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMaxResponseTime();
            }
        }
        return 0;
    }

    public long getMinServiceResponseTime(MessageContext messageContext) {

        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMinResponseTime();
            }
        }
        return 0;
    }

    public double getAvgServiceResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getAvgResponseTime();
            }
        }
        return 0;
    }

    public int getOperationRequestCount(MessageContext messageContext) {
        Object operationCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_IN_OPERATION_COUNTER_PROPERTY);

        if (operationCounterObj != null) {
            if (operationCounterObj instanceof Counter) {
                return ((Counter) operationCounterObj).getCount();
            }
        }
        return 0;
    }

    public int getOperationFaultCount(MessageContext messageContext) {
        Object operationCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_FAULT_COUNTER_PROPERTY);
        if (operationCounterObj != null) {
            if (operationCounterObj instanceof Counter) {
                return ((Counter) operationCounterObj).getCount();
            }
        }
        return 0;
    }

    public int getOperationResponseCount(MessageContext messageContext) {
        Object operationCounterObj = messageContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OUT_OPERATION_COUNTER_PROPERTY);
        if (operationCounterObj != null) {
            if (operationCounterObj instanceof Counter) {
                return ((Counter) operationCounterObj).getCount();
            }
        }
        return 0;
    }

    public long getMaxOperationResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMaxResponseTime();
            }
        }
        return 0;
    }

    public long getMinOperationResponseTime(MessageContext messageContext) {
        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getMinResponseTime();
            }
        }
        return 0;
    }

    public double getAvgOperationResponseTime(MessageContext messageContext) {

        Object responseTimeProcessorObj = messageContext
                .getConfigurationContext()
                .getProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_RESPONSE_TIME_PROCESSOR_PROPERTY);
        if (responseTimeProcessorObj != null) {
            if (responseTimeProcessorObj instanceof ResponseTimeProcessor) {
                return ((ResponseTimeProcessor) responseTimeProcessorObj).getAvgResponseTime();
            }
        }
        return 0;
    }
}
