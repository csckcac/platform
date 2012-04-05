/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.activity.mediation.data.publisher.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.bam.activity.mediation.data.publisher.data.MessageActivity;
import org.wso2.carbon.bam.activity.mediation.data.publisher.queue.ActivityQueue;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


public class ActivityPublisherUtils {

    private static final Log log = LogFactory.getLog(ActivityPublisherUtils.class);

    private static ActivityQueue activityQueue;
    private static ServerConfiguration serverConfiguration;
    private static ConfigurationContextService configContextService;
    private static PublisherConfiguration publisherConfiguration;

    public static void setActivityQueue(ActivityQueue activityQueue) {
        ActivityPublisherUtils.activityQueue = activityQueue;
    }

    public static void setServerConfiguration(ServerConfiguration serverConfiguration) {
        ActivityPublisherUtils.serverConfiguration = serverConfiguration;
    }

    public static void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        configContextService = cfgCtxService;
    }

    public static void publishEvent(MessageTraceLog traceLog) {
        MessageActivity activity = newActivity(traceLog);
        activityQueue.enqueue(activity);
    }

    public static void publishEvent(MessageContext synCtx, long currentTime,
                                    boolean extractSoapBody, boolean request) {
        MessageActivity activity = newActivity(synCtx, currentTime, extractSoapBody, request);
        if (activity != null) {
            activityQueue.enqueue(activity);
        }
    }


    public static MessageActivity newActivity(MessageContext synCtx, long currentTime,
                                              boolean extractSoapBody, boolean request) {
        MessageActivity activity = null;
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        activity = new MessageActivity();

        activity.setTenantId(tenantID);
        activity.setDirection(request ?
                              ActivityPublisherConstants.DIRECTION_IN : ActivityPublisherConstants.DIRECTION_OUT);
        activity.setService(msgCtx.getAxisService().getName());
        activity.setOperation(msgCtx.getAxisOperation().getName().getLocalPart());

        Map<String, Object> hashMap = ((Axis2MessageContext) synCtx).getProperties();
        for (Map.Entry entry : hashMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String && entry.getKey().toString().startsWith(
                    ActivityPublisherConstants.BAM_PREFIX)) {
                activity.setProperty(entry.getKey().toString(), value.toString());
            }
        }

        if (extractSoapBody) {
            SOAPEnvelope soapEnvelope = msgCtx.getEnvelope();
            SOAPVersion soapVersion = soapEnvelope.getVersion();
            activity.setSoapEnvelopNamespaceURI(soapVersion.getEnvelopeURI());
            activity.setPayload(soapEnvelope.getBody().toString());
        }

        Timestamp timestamp = new Timestamp(currentTime);
        activity.setTimestamp(timestamp);

        if (msgCtx.getMessageID() != null) {
            activity.setMessageId(msgCtx.getMessageID());
        } else {
            String messageId = UUID.randomUUID().toString();
            msgCtx.setMessageID(messageId);
            activity.setMessageId(messageId);
        }


        SOAPHeader header = msgCtx.getEnvelope().getHeader();
        QName bamEventQName = new QName(ActivityPublisherConstants.BAM_HEADER_NAMESPACE_URI,
                                        ActivityPublisherConstants.BAM_EVENT);

        OMElement bamEvent = header.getFirstChildWithName(bamEventQName);
        if (bamEvent != null) {
            QName activityIdQName = new QName(ActivityPublisherConstants.ACTIVITY_ID);
            String activityId = bamEvent.getAttributeValue(activityIdQName);
            if (activityId != null) {
                activity.setActivityId(activityId);
            }
        } else {
            log.warn("BAMEvent header not found on the message");
        }

        return activity;
    }


    private static MessageActivity newActivity(MessageTraceLog traceLog) {
        Map<String, Object> properties = traceLog.getProperties();

        MessageActivity activity = new MessageActivity();
        activity.setService(traceLog.getType().toString());
        activity.setOperation(traceLog.getResourceId());
        activity.setActivityId(properties.get(BAMDataPublisherConstants.MSG_ACTIVITY_ID).toString());
        activity.setMessageId(traceLog.getMessageId());
        activity.setRequestStatus(traceLog.getRequestFaultStatus());
        activity.setResponseStatus(traceLog.getResponseFaultStatus());

        Object arrivalTime = properties.get(ActivityPublisherConstants.PROP_MSG_ARRIVAL_TIME);
        Timestamp timestamp;
        if (arrivalTime != null) {
            timestamp = new Timestamp(Long.parseLong(arrivalTime.toString()));
            activity.setTimestamp(timestamp);
        } else {
            Date currentDate = new java.util.Date();
            timestamp = new Timestamp(currentDate.getTime());
            activity.setTimestamp(timestamp);
        }

        if (properties.containsKey(ActivityPublisherConstants.PROP_BAM_MESSAGE_BODY)) {
            activity.setPayload(properties.get(ActivityPublisherConstants.PROP_BAM_MESSAGE_BODY).toString());
        }
        return activity;
    }

    public static void setPublisherConfiguration(PublisherConfiguration configuration) {
        publisherConfiguration = configuration;
    }

    public static PublisherConfiguration getPublisherConfiguration(){
        return publisherConfiguration;
    }
}
