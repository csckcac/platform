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

import org.apache.axiom.om.*;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.internal.StatisticsServiceComponent;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.CommonConstants;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.statistics.StatisticsConstants;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


public class ActivityInHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(StatisticsHandler.class);


    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        AxisConfiguration axisConfiguration = messageContext.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<Integer, EventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        EventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);

        if (eventingConfigData != null && eventingConfigData.isMsgDumpingEnable()) {
            Timestamp timestamp;
            AxisService service = messageContext.getAxisService();
            Parameter adminServiceParam = service.getParameter(CommonConstants.ADMIN_SERVICE_PARAMETER);
            Parameter hiddenServiceParam = service.getParameter(CommonConstants.HIDDEN_SERVICE_PARAMETER);

            if (adminServiceParam == null && hiddenServiceParam == null) {

                SOAPFactory soapFactory = null;
                SOAPHeaderBlock soapHeaderBlock = null;
                SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
                String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
                UUID activityUUID = UUID.randomUUID();

                if (messageContext.getMessageID() == null) {
                    UUID msgUUID = UUID.randomUUID();
                    messageContext.setMessageID(msgUUID.toString());
                }

                if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    soapFactory = OMAbstractFactory.getSOAP11Factory();
                } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    soapFactory = OMAbstractFactory.getSOAP12Factory();
                } else {
                    log.error("Not a standard soap message");
                }

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
                        soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityUUID.toString(), null);
                    } else {
                        OMElement element = (OMElement) itr.next();
                        String aid = element.getAttributeValue(new QName(ActivityPublisherConstants.ACTIVITY_ID));
                        if (aid != null) {
                            if (aid.equals("")) {
                                element.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityUUID.toString(), null);
                            }
                        } else {
                            element.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, activityUUID.toString(), null);
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
                                                     activityUUID.toString(), null);
                    }
                }

                MessageContext inMessageContext = messageContext.getOperationContext().getMessageContext(
                        WSDL2Constants.MESSAGE_LABEL_IN);
                EventData eventData = new EventData();
                if (inMessageContext != null) {
                    //Get timestamp value set from system-statistics module
                    timestamp = new Timestamp(Long.parseLong(inMessageContext.getProperty(
                            StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                    Object requestProperty = inMessageContext.getProperty(
                            HTTPConstants.MC_HTTP_SERVLETREQUEST);
                    ServiceAgentUtil.extractInfoFromHttpHeaders(eventData, requestProperty);
                } else {
                    Date currentDate = new Date();
                    timestamp = new Timestamp(currentDate.getTime());
                }

                addDetailsOfTheMessage(eventData, timestamp, activityUUID, messageContext);
                BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);

                PublishData publishData = new PublishData();
                publishData.setEventData(eventData);
                publishData.setBamServerInfo(bamServerInfo);

                if (isInOnlyMEP(messageContext)) {
                    StatisticsServiceComponent.getAgent().publish(ServiceAgentUtil.makeEventList(publishData, eventingConfigData),
                            ServiceAgentUtil.constructEventReceiver(publishData.getBamServerInfo()));
//                    ServiceAgentUtil.publishEvent(publishData);

                } else {
                    inMessageContext.setProperty(BAMDataPublisherConstants.PUBLISH_DATA, publishData);
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }


    private EventData addDetailsOfTheMessage(EventData eventData, Timestamp timestamp,
                                                UUID randomUUID,
                                                MessageContext messageContext) {
        eventData.setActivityId(randomUUID.toString());
        eventData.setTimestamp(timestamp);
        String msgBody = null;
        try {
            msgBody = messageContext.getEnvelope().getBody().toString();
        } catch (OMException e) {
            log.warn("Exception occurred while getting soap envelop", e);
        }
        eventData.setInMessageBody(msgBody);
        //eventData.setMessageDirection(ActivityPublisherConstants.ACTIVITY_DATA_MESSAGE_DIRECTION_IN);
        eventData.setServiceName(messageContext.getAxisService().getName());
        eventData.setOperationName(messageContext.getAxisOperation().getName().getLocalPart());
        eventData.setInMessageId(messageContext.getMessageID());

        return eventData;

    }

    private boolean isInOnlyMEP(MessageContext messageContext) {
        String mep = messageContext.getOperationContext().getAxisOperation().getMessageExchangePattern();

        if (mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
            mep.equals(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT) ||
            mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY) ) {

            return true;
        }

        return false;

    }
}
