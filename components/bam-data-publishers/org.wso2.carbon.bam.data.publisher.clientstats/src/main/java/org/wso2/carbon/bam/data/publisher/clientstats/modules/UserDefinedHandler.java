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

import java.io.StringReader;
import java.util.UUID;

import org.apache.axiom.soap.SOAPFactory;
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
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;
import org.wso2.carbon.bam.data.publisher.clientstats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.clientstats.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.clientstats.events.ClientStatisticsEvent;
import org.wso2.carbon.bam.data.publisher.clientstats.events.ClientStatisticsThresholdEvent;
import org.wso2.carbon.bam.data.publisher.clientstats.services.ClientStatisticsPublisherAdmin;

/* This handler is used to generate events which contain user identification params and
 * his/her service+operation details to be stored with BAM DB(BAM_SERVER_USERDEFINED_DATA).
 * 
 * Redirecting the request to BAM common subscriber endpoint, while Service request is allowed  to go in its path.
 * 
 * Event format would be as follows...
 */

/*<?xml version='1.0' encoding='utf-8'?>
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
 * <soapenv:Header>
 *	 <ns:topic xmlns:ns="http://wso2.org/bam/service/statistics/notify">/carbon/bam/data/publishers/service_stats/ChildDeleted</ns:topic>
 * </soapenv:Header>
 * <soapenv:Body>
 * 	<svrusrdata:Event xmlns:svrusrdata="http://wso2.org/ns/2009/09/bam/server/user-defined/data">
 * 	<svrusrdata:ServerUserDefinedData>
 * 		<svrusrdata:ServerName>https://10.100.1.143:9443</svrusrdata:ServerName>
 * 		<svrusrdata:Data>
 * 			<svrusrdata:Key>john_9d90b047-48dc-4df0-8a60-6a677a5689d5_https://10.100.1.143:9443</svrusrdata:Key>
 *	 		<svrusrdata:Value>anonService2_anonOutInOp</svrusrdata:Value>
 * 		</svrusrdata:Data>
 * </svrusrdata:ServerUserDefinedData>
 * </svrusrdata:Event>
 * </soapenv:Body>
 * </soapenv:Envelope>
 */
public class UserDefinedHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(UserDefinedHandler.class);
    String service = "";
    String operation = "";
    String userParam = "";
    EndpointReference epr = null;
    String bam_httpsServer = "";
    String remoteIPAddress = "";
    String wsas_server = "";

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        UUID uuid_random = UUID.randomUUID();
        String uuid = uuid_random.toString();
        remoteIPAddress = (String) messageContext.getConfigurationContext().getProperty(
                "REMOTE_ADDR");

        if (messageContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_HTTPS_SERVER_PROPERTY) != null) {
            bam_httpsServer = messageContext
                    .getConfigurationContext()
                    .getProperty(
                                 ClientStatisticsPublisherConstants.BAM_USER_DEFINED_HTTPS_SERVER_PROPERTY)
                    .toString();
        } else {
            log.error("User has not defined BAM https server property");
        }
        if (messageContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_WSAS_SERVER_PROPERTY) != null) {
            wsas_server = messageContext
                    .getConfigurationContext()
                    .getProperty(
                                 ClientStatisticsPublisherConstants.BAM_USER_DEFINED_WSAS_SERVER_PROPERTY)
                    .toString();
        } else {
            log.error("User has not defined WSAS Server property");
        }
        if (messageContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY) != null) {
            service = messageContext.getConfigurationContext()
                    .getProperty(
                                 ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY)
                    .toString();
        } else {
            log.error("User has not defined serviceName property");
        }
        if (messageContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY) != null) {
            operation = messageContext.getConfigurationContext()
                    .getProperty(
                                 ClientStatisticsPublisherConstants.BAM_USER_DEFINED_OPERATION_PROPERTY)
                    .toString();
        } else {
            log.error("User has not defined operationName property");
        }

        if (messageContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_USER_PARAM_PROPERTY) != null) {
            userParam = messageContext.getConfigurationContext()
                    .getProperty(
                                 ClientStatisticsPublisherConstants.BAM_USER_DEFINED_USER_PARAM_PROPERTY)
                    .toString();
        } else {
            userParam = uuid;
        }
        AxisService axisService = messageContext.getAxisService();
        Parameter param_admin = axisService.getParameter("adminService");
        Parameter param_hidden = axisService.getParameter("hiddenService");

        boolean notification = true;

        if (messageContext                
                .getProperty(ClientStatisticsPublisherConstants.BAM_USER_DEFINED_EVENT_NOTIFICATION_PROPERTY) == null) {
            if (param_admin == null || !"true".equals(param_admin.getValue().toString())) {
                if (param_hidden == null || !"true".equals(param_hidden.getValue().toString())) {

                    SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
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

                    eventMessageContext.setOperationContext(messageContext.getOperationContext());
                    eventMessageContext.setMessageID(UUID.randomUUID().toString());
                    eventMessageContext.setTransportOut(messageContext.getTransportOut());
                    eventMessageContext.setOptions(new Options());
                    eventMessageContext.setEnvelope(newEnv);
                    ClientStatisticsPublisherAdmin clientStatisticsPublisherAdmin = new ClientStatisticsPublisherAdmin();
                    PublisherUtils
                            .setClientStatisticsPublisherAdmin(clientStatisticsPublisherAdmin);
                    
                    EventingConfigData eventingConfigData = PublisherUtils
                            .getClientStatisticsPublisherAdmin().getEventingConfigData();

                    if (eventingConfigData != null && eventingConfigData.eventingEnabled()) {

                        OMElement statMessage = PublisherUtils.getEventPayload(wsas_server,
                                                                               userParam, uuid,
                                                                               service, operation);

                        setProperties(messageContext, uuid, remoteIPAddress);
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
                            SOAPHeaderBlock soapHeaderBlock = eventEnv
                                    .getHeader()
                                    .addHeaderBlock(
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
                            messageContext
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
                                           "ServerUserDefinedHandler - Unable to send notification for stat threshold",
                                           e);
                        }
                    }
                }
            }
        } else {
            log.warn("MessageContext Event Notification Property has not been set");
        }
        return InvocationResponse.CONTINUE;
    }

    public static OMElement createOMElement(String xml) {
        try {

            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement omElement = builder.getDocumentElement();
            return omElement;

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * set the same Client/BAM properties to the ConfigurationContext. Need to be accessed by the
     * ClientStatistics handler
     */
    public void setProperties(MessageContext messageContext, String uuid, String remoteIPAddress) {

        ConfigurationContext configContext = messageContext.getConfigurationContext();

        configContext.setProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_UUID_PROPERTY, uuid);
        configContext
                .setProperty(
                        ClientStatisticsPublisherConstants.BAM_USER_DEFINED_REMOTE_IPADDRESS_PROPERTY,
                        remoteIPAddress);

    }

}
