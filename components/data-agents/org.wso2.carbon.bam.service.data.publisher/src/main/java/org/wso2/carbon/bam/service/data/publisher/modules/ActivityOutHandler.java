/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.modules;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.publish.EventPublisher;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.CommonConstants;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ActivityOutHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(ActivityOutHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        AxisConfiguration axisConfiguration = messageContext.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<Integer, EventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        EventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);

        if (eventingConfigData != null && eventingConfigData.isMsgDumpingEnable()) {

            AxisService service = messageContext.getAxisService();
            Parameter adminServiceParam = service.getParameter(CommonConstants.ADMIN_SERVICE_PARAMETER);
            Parameter hiddenServiceParam = service.getParameter(CommonConstants.HIDDEN_SERVICE_PARAMETER);

            if (adminServiceParam == null && hiddenServiceParam == null) {

                if (messageContext.getMessageID() == null) {
                    UUID msgUUID = UUID.randomUUID();
                    messageContext.setMessageID(msgUUID.toString());
                }
                //get IN Message Context from OutMessageContext to track request and response
                MessageContext inMessageContext = messageContext.getOperationContext().getMessageContext(
                        WSDL2Constants.MESSAGE_LABEL_IN);

                Iterator itr = inMessageContext.getEnvelope().getHeader().getChildrenWithName(new QName(
                        ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, ActivityPublisherConstants.ACTIVITY_ID_HEADER_BLOCK_NAME));
                String activityID = null;
                if (itr.hasNext()) {
                    OMElement element = (OMElement) itr.next();
                    activityID = element.getAttributeValue(new QName(ActivityPublisherConstants.ACTIVITY_ID));
                }

                PublishData publishData = null;
                Timestamp timestamp = null;
                if (inMessageContext != null) {
                    publishData = (PublishData) inMessageContext.getProperty(
                            BAMDataPublisherConstants.PUBLISH_DATA);
                } else {
                    Date date = new Date();
                    timestamp = new Timestamp(date.getTime());
                }

                // If already set in the INFLOW get it or create new publish data
                EventData eventData;
                if (publishData != null) {
                    eventData = publishData.getEventData();
                } else {
                    publishData = new PublishData();
                    eventData = new EventData();
                }

/*                if (inMessageContext != null) {
                    timestamp = new Timestamp(Long.parseLong(inMessageContext.getProperty(
                            StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                    Object requestProperty = inMessageContext.getProperty(
                            HTTPConstants.MC_HTTP_SERVLETREQUEST);
                    extractInfoFromHttpHeaders(eventData, requestProperty);
                } else {
                    Date date = new Date();
                    timestamp = new Timestamp(date.getTime());
                }*/

                addDetailsOfTheMessage(eventData, timestamp, activityID, messageContext);

                publishData.setEventData(eventData);

                // Skip setting bam server info if already set in the INFLOW
                if (!isInFlowDataPresent(messageContext)) {
                    BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                    publishData.setBamServerInfo(bamServerInfo);
                }

                // If service statistics is not enabled publish the event. Else let service stat
                // handler do the job.
                if (!eventingConfigData.isServiceStatsEnable()) {
                    Event  event = ServiceAgentUtil.makeEventList(publishData);
                    EventPublisher publisher = new EventPublisher();
                    publisher.publish(event,eventingConfigData);
                } else {
                    messageContext.setProperty(BAMDataPublisherConstants.PUBLISH_DATA, publishData);
                }

                // Now set all values to response
                engageSOAPHeaders(messageContext, activityID);

            }
        }


        return InvocationResponse.CONTINUE;
    }



    private void engageSOAPHeaders(MessageContext messageContext, String activityID) {
        SOAPFactory soapFactory = null;
        SOAPHeaderBlock soapHeaderBlock = null;
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();

        if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Not a standard soap message");
        }

        // If header is not null check for BAM headers
        if (soapEnvelope.getHeader() != null) {
            Iterator itr = soapEnvelope.getHeader().getChildrenWithName(new QName(
                    ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI,
                    ActivityPublisherConstants.ACTIVITY_ID_HEADER_BLOCK_NAME));
            //Go through the header and see whether the AID is present or not. If not add.
            if (!itr.hasNext()) {
                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMNamespace omNs = fac.createOMNamespace(
                        ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(
                        ActivityPublisherConstants.ACTIVITY_ID_HEADER_BLOCK_NAME, omNs);
                soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityID, null);
            } else {
                OMElement element = (OMElement) itr.next();
                String aid = element.getAttributeValue(new QName(ActivityPublisherConstants.ACTIVITY_ID));
                if (aid != null) {
                    if (aid.equals("")) {
                        element.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityID, null);
                    }
                } else {
                    element.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityID, null);
                }
            }
        } else {
            if (soapFactory != null) {
                soapFactory.createSOAPHeader(soapEnvelope);
                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMNamespace omNs = fac.createOMNamespace(
                        ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(
                        ActivityPublisherConstants.ACTIVITY_ID_HEADER_BLOCK_NAME, omNs);
                soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID,
                                             activityID, null);
            }
        }
    }

    private EventData addDetailsOfTheMessage(EventData eventData, Timestamp timestamp,
                                             String activityID,
                                             MessageContext outMessageContext) throws AxisFault {

        // Check and skip if these details already set in the INFLOW
        if (!isInFlowDataPresent(outMessageContext)) {
            eventData.setTimestamp(timestamp);
            eventData.setActivityId(activityID);
            eventData.setOperationName(outMessageContext.getAxisService().getName());
            eventData.setServiceName(outMessageContext.getAxisOperation().getName().getLocalPart());
        }

        eventData.setOutMessageId(outMessageContext.getMessageID());
        //eventData.setMessageDirection(ActivityPublisherConstants.ACTIVITY_DATA_MESSAGE_DIRECTION_OUT);
        eventData.setOutMessageBody(outMessageContext.getEnvelope().getBody().toString());

        return eventData;
    }

    private boolean isInFlowDataPresent(MessageContext outMessageContext) throws AxisFault {
        MessageContext inMessageContext = outMessageContext.getOperationContext().getMessageContext(
                WSDL2Constants.MESSAGE_LABEL_IN);

        if (inMessageContext != null &&
            inMessageContext.getProperty(BAMDataPublisherConstants.PUBLISH_DATA) != null) {
            return true;
        }

        return false;
    }

}
