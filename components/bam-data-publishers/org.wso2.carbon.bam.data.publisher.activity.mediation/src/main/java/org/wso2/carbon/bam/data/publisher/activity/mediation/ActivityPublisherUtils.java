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

package org.wso2.carbon.bam.data.publisher.activity.mediation;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jaxen.JaxenException;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.XPathConfigData;
import org.wso2.carbon.bam.data.publisher.activity.mediation.services.ActivityPublisherAdmin;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.net.SocketException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActivityPublisherUtils {

    private static final Log log = LogFactory.getLog(ActivityPublisherUtils.class);

    //private static EventBroker eventBroker;
    private static LightWeightEventBroker lightWeightEventBroker;
    private static ConfigurationContextService cfgCtxService;
    private static ServerConfiguration serverConfiguration;
    private static ActivityQueue activityQueue;
    private static ActivityPublisherAdmin activityPublisherAdmin;

    private static OMFactory fac = OMAbstractFactory.getOMFactory();

    public static void setEventBroker(LightWeightEventBroker eventBroker) {
        ActivityPublisherUtils.lightWeightEventBroker = eventBroker;
    }

    public static LightWeightEventBroker getEventBroker() {
        return lightWeightEventBroker;
    }

    public static ActivityQueue getActivityQueue() {
        return activityQueue;
    }

    public static void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        ActivityPublisherUtils.cfgCtxService = cfgCtxService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public static void setServerConfiguration(ServerConfiguration serverConfiguration) {
        ActivityPublisherUtils.serverConfiguration = serverConfiguration;
    }

    public static void setActivityQueue(ActivityQueue activityQueue) {
        ActivityPublisherUtils.activityQueue = activityQueue;
    }

    public static void publishEvent(MessageContext synCtx, boolean request) {
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        AxisService service = msgCtx.getAxisService();
        if (service == null || service.getParameter("adminService") != null ||
            service.getParameter("hiddenService") != null) {
            return;
        }

        MessageActivity activity = newActivity(synCtx, request);
        activityQueue.enqueue(activity);
    }

    public static void publishEvent(MessageTraceLog traceLog) {
        MessageActivity activity = newActivity(traceLog);
        activityQueue.enqueue(activity);
    }

    public static MessageActivity newActivity(MessageContext synCtx, boolean request) {
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        MessageActivity activity = new MessageActivity();
        activity.setDirection(request ?
                              ActivityPublisherConstants.DIRECTION_IN : ActivityPublisherConstants.DIRECTION_OUT);
        activity.setService(msgCtx.getAxisService().getName());
        activity.setOperation(msgCtx.getAxisOperation().getName().getLocalPart());

        // Enable message dumping.... (this should be done conditionally)
        EventingConfigData data = activityPublisherAdmin.getEventingConfigData();
        if (data != null && data.messageDumpingEnabled()) {
            activity.setPayload(msgCtx.getEnvelope().getBody().toString());
        }

        if (data != null && data.messageLookupEnabled()) {

            XPathConfigData[] xpathConfigs = null;
            try {
                xpathConfigs = ActivityPublisherUtils.getActivityPublisherAdmin().getXPathData();
            } catch (Exception ignored) {

            }

            try {

                String messageBody = msgCtx.getEnvelope().getBody().toString();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBody.getBytes());
                StAXBuilder builder = new StAXOMBuilder(byteArrayInputStream);
                OMElement root = builder.getDocumentElement();

                if (xpathConfigs != null) {
                    for (XPathConfigData xpathConfig : xpathConfigs) {
                        AXIOMXPath xpath = new AXIOMXPath(xpathConfig.getXpath());
                        String[] namespaces = xpathConfig.getNameSpaces();

                        if (namespaces != null && namespaces.length > 0) {
                            for (String ns : namespaces) {
                                String tokens[] = ns.split("@");

                                if (tokens != null && tokens.length >= 2) {
                                    xpath.addNamespace(tokens[0], tokens[1]);
                                }
                            }
                        }

                        List listOfNodes = xpath.selectNodes(root);
                        StringBuffer value = new StringBuffer();
                        if (listOfNodes != null && listOfNodes.size() > 0) {
                            for (int i = 0; i < listOfNodes.size(); i++) {
                                OMContainer omContainer = (OMContainer) listOfNodes.get(i);

                                if (omContainer instanceof OMDocument) {
                                    omContainer = ((OMDocument) omContainer).getOMDocumentElement();
                                }

                                String xpathValue = omContainer.toString();
                                value.append(xpathValue);
                            }
                        }

                        activity.setXpath(xpathConfig, value.toString());

                    }
                }
            } catch (XMLStreamException e) {
                log.error("Error building the xpath..", e);
            } catch (JaxenException e) {
                log.error("Error building the xpath..", e);
            }
        }

        Object arrivalTime = synCtx.getProperty(ActivityPublisherConstants.PROP_MSG_ARRIVAL_TIME);
        if (arrivalTime != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(arrivalTime.toString()));
            activity.setTimestamp(BAMCalendar.getInstance(cal));
        } else {
            activity.setTimestamp(BAMCalendar.getInstance());
        }

        if (msgCtx.getMessageID() != null) {
            activity.setMessageId(msgCtx.getMessageID());
        } else {
            String messageId = UUID.randomUUID().toString();
            msgCtx.setMessageID(messageId);
            activity.setMessageId(messageId);
        }

        Object senderHost = synCtx.getProperty(ActivityPublisherConstants.PROP_REMOTE_ADDRESS);
        if (senderHost != null) {
            activity.setSenderHost(senderHost.toString());
        } else {
            activity.setSenderHost((String) msgCtx.getProperty(
                    ActivityPublisherConstants.PROP_REMOTE_ADDRESS));
        }

        // activity.setSenderHost((String)synCtx.getProperty(ActivityPublisherConstants.PROP_REMOTE_ADDRESS));

        Object receiverHost = msgCtx.getProperty(ActivityPublisherConstants.PROP_RECEIVER_ADDRESS);
        if (receiverHost != null) {
            activity.setReceiverHost(receiverHost.toString());
        } else {
            activity.setReceiverHost((String) msgCtx.getProperty(
                    ActivityPublisherConstants.PROP_RECEIVER_ADDRESS));
        }

        SOAPHeader header = msgCtx.getEnvelope().getHeader();
        OMElement bamEvent = header.getFirstChildWithName(ActivityPublisherConstants.BAM_EVENT_QNAME);
        if (bamEvent != null) {
            String activityId = bamEvent.getAttributeValue(ActivityPublisherConstants.ACTIVITY_ID_QNAME);
            if (activityId != null) {
                activity.setActivityId(activityId);
            }

            // BAMEvent header has a child element which contains 'parent' details
            OMElement propertyElement = bamEvent.getFirstElement();
            if (propertyElement != null) {
                String name = propertyElement.getAttributeValue(new QName("name"));
                String value = propertyElement.getAttributeValue(new QName("value"));
                activity.setProperty(name, value);
            }
        } else {
            log.warn("BAMEvent header not found on the message");
        }


        // TODO: userAgent, activityName, activityDescription

        // Setting message properties into the activity
        // Arc Key, Technical Failure and Application Failure MUST be set on all activities
        Object arcKey = synCtx.getProperty(ActivityPublisherConstants.PROP_ARC_KEY);
        if (arcKey != null) {
            activity.setProperty(ActivityPublisherConstants.PROP_ARC_KEY, arcKey.toString());
        }

        Object appFailure = synCtx.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE);
        if (JavaUtils.isTrueExplicitly(appFailure)) {
            Object errorDetail = synCtx.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE_DETAIL);
            if (errorDetail != null) {
                activity.setProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE,
                                     errorDetail.toString());
            } else {
                activity.setProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE,
                                     synCtx.getEnvelope().getBody().getFirstOMChild().toString());
            }
        }

        Object techFailure = synCtx.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE);
        if (JavaUtils.isTrueExplicitly(techFailure)) {
            Object errorDetail = synCtx.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE_DETAIL);
            if (errorDetail != null) {
                activity.setProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE,
                                     errorDetail.toString());
            } else {
                activity.setProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE,
                                     synCtx.getEnvelope().getBody().getFirstOMChild().toString());
            }
        }

        // Set other optional properties
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_ACTIVITY_TYPE);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_MESSAGE_TYPE);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_MESSAGE_FORMAT);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_ARC_STATUS);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_ARC_DETAIL);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_FAILURE_UUID);
        setActivityProperty(synCtx, activity, ActivityPublisherConstants.PROP_FAILURE_REPLAY_OPERATION);

        return activity;
    }

    /**
     * Serialize the given MessageActivity instance into XML. The serialized XML will contain
     * all the metadata, payload information and properties enclosed in the activity.
     *
     * @param activity MessageActivity instance to be serialized
     * @return the XML (OMElement) representation of the activity
     */

    public static OMElement serialize(MessageActivity activity) {
        OMNamespace activityNamespace = fac.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                                                              ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);
        OMElement activityDataElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT, activityNamespace);

        OMElement serverNameElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVER_NAME, activityNamespace);
        serverNameElement.setText(getServerName());
        activityDataElement.addChild(serverNameElement);

        OMElement serviceNameElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVICE_NAME, activityNamespace);
        serviceNameElement.setText(activity.getService());
        activityDataElement.addChild(serviceNameElement);

        OMElement operationNameElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_OPERATION_NAME, activityNamespace);
        operationNameElement.setText(activity.getOperation());
        activityDataElement.addChild(operationNameElement);

        OMElement activtyIdElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_ID, activityNamespace);
        activtyIdElement.setText(activity.getActivityId());
        activityDataElement.addChild(activtyIdElement);

        OMElement messageIDElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_ID, activityNamespace);
        messageIDElement.setText(activity.getMessageId());
        activityDataElement.addChild(messageIDElement);

        int direction = activity.getDirection();
        if (direction != ActivityPublisherConstants.DIRECTION_IN_OUT) {
            OMElement messageDirectionElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION, activityNamespace);
            if (direction == ActivityPublisherConstants.DIRECTION_IN) {
                messageDirectionElement.setText("Request");
            } else {
                messageDirectionElement.setText("Response");
            }
            activityDataElement.addChild(messageDirectionElement);
        }

        OMElement timeStampElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_TIMESTAMP, activityNamespace);
        timeStampElement.setText(activity.getTimestamp().getBAMTimestamp());
        activityDataElement.addChild(timeStampElement);

        // Now add optional stuff...
        if (activity.getSenderHost() != null) {
            OMElement remoteIPElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_REMOTE_IP_ADDRESS, activityNamespace);
            remoteIPElement.setText(activity.getSenderHost());
            activityDataElement.addChild(remoteIPElement);
        }

        if (activity.getUserAgent() != null) {
            OMElement userAgentElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_USER_AGENT, activityNamespace);
            userAgentElement.setText(activity.getUserAgent());
            activityDataElement.addChild(userAgentElement);
        }

        if (activity.getActivityName() != null) {
            OMElement activityNameElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME, activityNamespace);
            activityNameElement.setText(activity.getActivityName());
            activityDataElement.addChild(activityNameElement);
        }

        if (activity.getDescription() != null) {
            OMElement activityDescriptionElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_DESCRIPTION, activityNamespace);
            activityDescriptionElement.setText(activity.getDescription());
            activityDataElement.addChild(activityDescriptionElement);
        }

        if (activity.getPayload() != null) {
            OMElement messageBodyElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_BODY, activityNamespace);
            messageBodyElement.setText(activity.getPayload());
            activityDataElement.addChild(messageBodyElement);
        }

        if (activity.getRequestStatus() != -1) {
            OMElement requestStatusElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_REQUEST_MESSAGE_STATUS, activityNamespace);
            requestStatusElement.setText(String.valueOf(activity.getRequestStatus()));
            activityDataElement.addChild(requestStatusElement);
        }

        if (activity.getResponseStatus() != -1) {
            OMElement responseStatusElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_RESPONSE_MESSAGE_STATUS, activityNamespace);
            responseStatusElement.setText(String.valueOf(activity.getResponseStatus()));
            activityDataElement.addChild(responseStatusElement);
        }

        OMElement propertiesElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTIES, activityNamespace);

        for (String key : activity.getPropertyKeys()) {
            OMElement propertyElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY, activityNamespace);
            OMElement propertyChildElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_CHILD, activityNamespace);
            propertyChildElement.setText(key);
            OMElement propertyValue = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_VALUE, activityNamespace);
            propertyValue.setText(activity.getProperty(key));
            propertyElement.addChild(propertyChildElement);
            propertyElement.addChild(propertyValue);
            propertiesElement.addChild(propertyElement);
        }
        activityDataElement.addChild(propertiesElement);

        OMElement xpathExpressionsElement = fac.createOMElement(
                ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSIONS, activityNamespace);

        for (XPathConfigData data : activity.getXpathKeys()) {
            OMElement xpathExpressionElement = fac
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSION, activityNamespace);
            OMElement keyElement = fac.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION_KEY,
                                                       activityNamespace);
            fac.createOMText(keyElement, data.getKey());

            OMElement expressionElement = fac.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION, activityNamespace);
            fac.createOMText(expressionElement, data.getXpath());

            OMElement xpathValue = fac.createOMElement(ActivityPublisherConstants.XPATH_VALUE, activityNamespace);
            fac.createOMText(xpathValue, activity.getXpath(data));

            OMElement aliasElement = fac.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_ALIAS,
                                                         activityNamespace);
            fac.createOMText(aliasElement, data.getAlias());


            OMElement namespacesElement = fac.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACES,
                                                              activityNamespace);

            if (data.getNameSpaces() != null) {
                for (String ns : data.getNameSpaces()) {
                    OMElement namespaceElement = fac.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACE,
                                                                     activityNamespace);
                    fac.createOMText(namespaceElement, ns);
                    namespacesElement.addChild(namespaceElement);
                }
            }


            xpathExpressionElement.addChild(keyElement);
            xpathExpressionElement.addChild(expressionElement);
            xpathExpressionElement.addChild(aliasElement);
            xpathExpressionElement.addChild(namespacesElement);
            xpathExpressionElement.addChild(xpathValue);
            xpathExpressionsElement.addChild(xpathExpressionElement);

        }
        activityDataElement.addChild(xpathExpressionsElement);

        return activityDataElement;
    }

    public static MessageActivity newActivity(MessageTraceLog traceLog) {
        Map<String, Object> properties = traceLog.getProperties();

        MessageActivity activity = new MessageActivity();
        activity.setService(traceLog.getType().toString());
        activity.setOperation(traceLog.getResourceId());
        activity.setActivityId(properties.get(ActivityPublisherConstants.PROP_ACTIVITY_ID).toString());
        activity.setMessageId(traceLog.getMessageId());
        activity.setRequestStatus(traceLog.getRequestFaultStatus());
        activity.setResponseStatus(traceLog.getResponseFaultStatus());

        Object arrivalTime = properties.get(ActivityPublisherConstants.PROP_MSG_ARRIVAL_TIME);
        if (arrivalTime != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(arrivalTime.toString()));
            activity.setTimestamp(BAMCalendar.getInstance(cal));
        } else {
            activity.setTimestamp(BAMCalendar.getInstance());
        }

        if (properties.containsKey(ActivityPublisherConstants.PROP_BAM_MESSAGE_BODY)) {
            activity.setPayload(properties.get(ActivityPublisherConstants.PROP_BAM_MESSAGE_BODY).toString());
        }

        // Setting message properties into the activity
        // Arc Key, Technical Failure and Application Failure MUST be set on all activities
        Object arcKey = properties.get(ActivityPublisherConstants.PROP_ARC_KEY);
        if (arcKey != null) {
            activity.setProperty(ActivityPublisherConstants.PROP_ARC_KEY, arcKey.toString());
        }

        Object appFailure = properties.get(ActivityPublisherConstants.PROP_APPLICATION_FAILURE);
        if (JavaUtils.isTrueExplicitly(appFailure)) {
            Object errorDetail = properties.get(ActivityPublisherConstants.PROP_APPLICATION_FAILURE_DETAIL);
            if (errorDetail != null) {
                activity.setProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE,
                                     errorDetail.toString());
            }
        }

        Object techFailure = properties.get(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE);
        if (JavaUtils.isTrueExplicitly(techFailure)) {
            Object errorDetail = properties.get(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE_DETAIL);
            if (errorDetail != null) {
                activity.setProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE,
                                     errorDetail.toString());
            }
        }

        // Set other optional properties
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_ACTIVITY_TYPE);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_MESSAGE_TYPE);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_MESSAGE_FORMAT);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_ARC_STATUS);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_ARC_DETAIL);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_FAILURE_UUID);
        setActivityProperty(traceLog, activity, ActivityPublisherConstants.PROP_FAILURE_REPLAY_OPERATION);

        return activity;
    }

    private static void setActivityProperty(MessageContext synCtx, MessageActivity activity,
                                            String key) {

        Object obj = synCtx.getProperty(key);
        if (obj != null && !"".equals(obj)) {
            activity.setProperty(key, obj.toString());
        }
    }

    private static void setActivityProperty(MessageTraceLog traceLog, MessageActivity activity,
                                            String key) {
        Object obj = traceLog.getProperties().get(key);
        if (obj != null && !"".equals(obj)) {
            activity.setProperty(key, obj.toString());
        }
    }

    public static ActivityPublisherAdmin getActivityPublisherAdmin() {
        return activityPublisherAdmin;
    }

    public static void setActivityPublisherAdmin(ActivityPublisherAdmin activityPublisherAdmin) {
        ActivityPublisherUtils.activityPublisherAdmin = activityPublisherAdmin;
    }

    public static String getServerName() {
        String TRANSPORT = "https";
        String serverName;
        try {
            String carbonHttpsPort = System.getProperty("carbon." + TRANSPORT + ".port");
            if (carbonHttpsPort == null) {
                AxisConfiguration axisConfig = cfgCtxService.getServerConfigContext().getAxisConfiguration();
                carbonHttpsPort = (String) axisConfig.getTransportIn(TRANSPORT).getParameter("port").getValue();
            }
            String context = serverConfiguration.getFirstProperty("WebContextRoot");
            if (context == null || context.equals("/")) {
                context = "";
            }
            serverName = TRANSPORT + "://" + NetworkUtils.getLocalHostname() + ":" +
                         carbonHttpsPort + context;

        } catch (SocketException ignored) {
            serverName = "https://localhost:9443";
        }

        return serverName;
    }

}