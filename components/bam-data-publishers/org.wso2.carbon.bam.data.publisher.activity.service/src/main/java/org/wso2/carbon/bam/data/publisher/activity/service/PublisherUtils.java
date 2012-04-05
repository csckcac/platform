/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.data.publisher.activity.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.config.XPathConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.services.ActivityPublisherAdmin;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.base.ServerConfiguration;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * This class is used to generate event payload.
 * 
 */

public class PublisherUtils {

    private static Log log = LogFactory.getLog(PublisherUtils.class);
    private static ConfigurationContext configurationContext;
    //private static EventBroker eventBroker;
    private static LightWeightEventBrokerInterface lightWeightEventBroker;
    private static ServerConfiguration serverConfiguration;
    private static ActivityPublisherAdmin activityPublisherAdmin = null;
    private static String serverName;

    private static final String TRANSPORT = "https";

    // To store stats of multiple messages
    private static ConcurrentMap<String, Map<String, OMElement>> messageMap = new ConcurrentHashMap<String, Map<String, OMElement>>();
    // To store stats of multiple messages' Data
    private static ConcurrentMap<String, Map<String, OMElement>> messageDataMap = new ConcurrentHashMap<String, Map<String, OMElement>>();
    // To store xpath stats of multiple xpath' Data
    private static ConcurrentMap<String, Map<String, OMElement>> xpathMap = new ConcurrentHashMap<String, Map<String, OMElement>>();

    // synchorinize the map further till we pass the reference to the event generation thread

    public static synchronized ConcurrentMap<String, Map<String, OMElement>> getMessageMap() {
        return messageMap;
    }

    public static synchronized void setMessageMap(
            ConcurrentMap<String, Map<String, OMElement>> map) {
        messageMap = map;
    }

    public static synchronized ConcurrentMap<String, Map<String, OMElement>> getMessageDataMap() {
        return messageDataMap;
    }

    public static synchronized void setMessageDataMap(
            ConcurrentMap<String, Map<String, OMElement>> map) {
        messageDataMap = map;
    }

    public static synchronized ConcurrentMap<String, Map<String, OMElement>> getXpathMap() {
        return xpathMap;
    }

    public static synchronized void setXpathMap(ConcurrentMap<String, Map<String, OMElement>> map) {
        xpathMap = map;
    }

    public static void setServerName(String name) {
        serverName = name;
    }

    public static String getServerName() {
        return serverName;
    }

    public static String updateServerName(AxisConfiguration axisConfiguration) {
        if (serverName == null) {
            try {
                String carbonHttpsPort = System.getProperty("carbon." + TRANSPORT + ".port");
                if (carbonHttpsPort == null) {
                    carbonHttpsPort = (String) axisConfiguration.getTransportIn(TRANSPORT).getParameter("port").getValue();
                }
                String context = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
                if (context.equals("/")) {
                    context = "";
                }
                serverName = TRANSPORT + "://" + NetworkUtils.getLocalHostname() + ":" + carbonHttpsPort + context;

            } catch (SocketException ignored) {
                serverName = "https://localhost:9943";
            }
        }

        return serverName;
    }

    /**
     * Publishes xpath configuration data to BAM side. No threshold is applicable.
     * Fires data when a user changes a xpath configuration. Event publishing happens synchronously.
     * This is because these event are low volume and more importantly do not occur in the main flow of the mediator.
     * Currently BAM side only supports sending one XPathExpression inside an event though event format is
     * provided for such facility in a future iteration.
     */
    /**
     * Event format for the XPath configuration event:
     * <p/>
     * (01)<activitydata:Event xmlns:activitydata="http://wso2.org/ns/2009/09/bam/service/activity/data">
     * (02)   <activitydata:PropertyFilter>
     * (03)      <activitydata:ServerName>https://192.168.2.2:9444</activitydata:ServerName>
     * (04)      <activitydata:XPathExpressions>
     * (05)          <activitydata:XPathExpression>
     * (06)              <activitydata:ExpressionKey>OrderID</activitydata:ExpressionKey>
     * (07)              <activitydata:Alias>Order Number</activitydata:Alias>
     * (08)              <activitydata :Expression>//ns1:Orders/ns2:orderID</activitydata:Expression>
     * (09)              <activitydata:Namespaces>
     * (10)                 <activitydata:Namespace>ns1@http://www.abc.com</activitydata:Namespace>
     * (11)                 <activitydata:Namespace>ns2@http://www.xyz.com</activitydata:Namespace>
     * (12)              </activitydata:Namespaces>
     * (13)          </activitydata:XPathExpression>
     * (14)      </activitydata:XPatheExpressions>
     * (15) </activitydata:PropertyFilter>
     * (16)</activitydata:Event>
     */
    public static void publishXPathConfigurations(XPathConfigData data) throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace activityNamespace = factory.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);

        OMElement propertyFilterElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_FILTER,
                        activityNamespace);

        OMElement eventElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_EVENT,
                activityNamespace);
        eventElement.addChild(propertyFilterElement);

        OMElement serverNameElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVER_NAME, activityNamespace);
        factory.createOMText(serverNameElement, updateServerName(getConfigurationContext().getAxisConfiguration()));

        OMElement xpathExpressionsElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSIONS, activityNamespace);

        OMElement xpathExpressionElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSION, activityNamespace);


        OMElement expressionKeyElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION_KEY,
                activityNamespace);
        factory.createOMText(expressionKeyElement, data.getKey());

        OMElement aliasElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_ALIAS,
                activityNamespace);
        factory.createOMText(aliasElement, data.getAlias());


        OMElement namespacesElement = factory.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACES,
                activityNamespace);

        if (data.getNameSpaces() != null) {
            for (String ns : data.getNameSpaces()) {
                OMElement namespaceElement = factory.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACE,
                        activityNamespace);
                factory.createOMText(namespaceElement, ns);
                namespacesElement.addChild(namespaceElement);
            }
        }

        OMElement expressionElement = factory.createOMElement(
                ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION, activityNamespace);
        factory.createOMText(expressionElement, data.getXpath());

        xpathExpressionElement.addChild(expressionKeyElement);
        xpathExpressionElement.addChild(aliasElement);
        xpathExpressionElement.addChild(expressionElement);
        xpathExpressionElement.addChild(namespacesElement);
        xpathExpressionsElement.addChild(xpathExpressionElement);

        propertyFilterElement.addChild(serverNameElement);
        propertyFilterElement.addChild(xpathExpressionsElement);

        publishSynchronously(eventElement);

    }

    /**
     * Synchronously publishes an event to the subscribers.
     *
     * @param eventBody Body of event to be published.
     * @throws Exception
     */
    private static synchronized void publishSynchronously(OMElement eventBody) throws Exception {
        if (eventBody != null) {
//            MessageContext eventMsgCtx = new MessageContext();
//            SOAPFactory eventSoapFactory = new SOAP12Factory();
//            SOAPEnvelope envelope = eventSoapFactory.getDefaultEnvelope();
//            envelope.getBody().addChild(eventBody);
//            try {
//                eventMsgCtx.setEnvelope(envelope);
//            } catch (AxisFault e) {
//                log.error("Could not set event envelope" , e);
//                throw e;
//            }
//
//            ActivityEvent<MessageContext> event = null;
//            event = new ActivityThresholdEvent<MessageContext>(eventMsgCtx);
//            ((ActivityThresholdEvent<MessageContext>) event).setResourcePath(ActivityPublisherConstants.BAM_REG_PATH);
//            EventBrokerService<MessageContext> ebs = PublisherUtils.getEventBrokerService();
            LightWeightEventBrokerInterface eb = ServiceHolder.getLWEventBroker();

            try {
                SuperTenantCarbonContext.startTenantFlow();
                int tenantId = SuperTenantCarbonContext.getCurrentContext(PublisherUtils.getConfigurationContext()).getTenantId();
                SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
                SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

                eb.publish(ActivityPublisherConstants.BAM_REG_PATH, eventBody);

            } finally {
                SuperTenantCarbonContext.endTenantFlow();
            }

//            try {
//                if (eb != null) {
//
//                    eb.publish(message, ActivityPublisherConstants.BAM_REG_PATH);
//                    if (log.isDebugEnabled()) {
//                        log.debug("Publishing BAM activity event: " + message.getMessage());
//                    }
//                }
//            } catch (EventBrokerException e) {
//                log.error("Unable to publish event : " + message.getMessage() + "\n with topic" +
//                          ActivityPublisherConstants.BAM_REG_PATH);
//            }
        }
    }

    /**
     * Message format for the activity event:
     *
     *(01)<activitydata:Event xmlns:activitydata="http://wso2.org/ns/2009/09/bam/service/activity/data"> 
     *(02)  <activitydata:ActivityData> 
     *(03)       <activitydata:ServerName>https://192.168.2.2:9444</activitydata:ServerName> 
     *(04)       <activitydata:ServiceName>SampleHelloService</activitydata:ServiceName> 
     *(05)       <activitydata:OperationName>sayHello</activitydata:OperationName> 
     *(06)       <activitydata:ActivityName>hello activity</activitydata:ActivityName>
     *(07)       <activitydata:ActivityDescription>Saying hello</activitydata:ActivityDescription> 
     *(08)       <activitydata:ActivityID>cccc491-a03a-4260-b177-c493ab7a2dbb</activitydata:ActivityID> 
     *(09)       <activitydata:MessageID></activitydata:MessageID> 
     *(10)       <activitydata:RemoteIPAddress>192.168.2.2</activitydata:RemoteIPAddress> 
     *(11)       <activitydata:UserAgent></activitydata:UserAgent>
     *(12)       <activitydata:TimeStamp></activitydata:TimeStamp>
     *(13)       <activitydata:ActivityProperty>Order Id</activitydata:ActivityProperty>
     *(13)       <activitydata:PropertyValue>1001</activitydata:PropertyValue>
     *(14) </activitydata:ActivityData>
     *(15)</activitydata:Event>
     */

    /**
     * Generate Event Payload.
     *
     * @param messageContext
     * @param axisConfiguration
     * @param messageID
     * @param activityID
     * @param serviceName
     * @param operationName
     * @param activityName
     * @param description
     * @param ipAddress
     * @param userAgent
     * @param timeStamp
     * @param properties
     */

    public static void getEventPayload(MessageContext messageContext,
                                       AxisConfiguration axisConfiguration,
                                       String messageID, String activityID, String serviceName,
                                       String operationName, String activityName,
                                       String description, String ipAddress, String userAgent,
                                       String timeStamp, Map<String, String> properties,
                                       String messageDirection) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace activityDataNamespace = factory.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI, ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);

        OMElement serverNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVER_NAME, activityDataNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));

        OMElement serviceNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVICE_NAME,
                activityDataNamespace);
        factory.createOMText(serviceNameElement, serviceName);

        OMElement operationNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_OPERATION_NAME,
                activityDataNamespace);
        factory.createOMText(operationNameElement, operationName);

        OMElement activityNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME,
                activityDataNamespace);
        factory.createOMText(activityNameElement, activityName);

        OMElement activityDescriptionElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_DESCRIPTION,
                activityDataNamespace);
        factory.createOMText(activityDescriptionElement, description);

        OMElement actiivtyIdElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_ID, activityDataNamespace);
        factory.createOMText(actiivtyIdElement, activityID);

        OMElement messageIDElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_ID, activityDataNamespace);
        factory.createOMText(messageIDElement, messageID);

        OMElement messageDirectionElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION,
                activityDataNamespace);
        factory.createOMText(messageDirectionElement, messageDirection);

        OMElement remoteIPElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_REMOTE_IP_ADDRESS,
                activityDataNamespace);
        factory.createOMText(remoteIPElement, ipAddress);

        OMElement userAgentElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_USER_AGENT, activityDataNamespace);
        factory.createOMText(userAgentElement, userAgent);

        OMElement timeStampElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_TIMESTAMP, activityDataNamespace);
        factory.createOMText(timeStampElement, timeStamp);

        OMElement propertiesElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTIES, activityDataNamespace);

        for (String property : properties.keySet()) {
            OMElement propertyElement = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY, activityDataNamespace);
            OMElement propertyChildElement = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_CHILD, activityDataNamespace);
            factory.createOMText(propertyChildElement, property);
            OMElement propertyValue = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_VALUE, activityDataNamespace);
            factory.createOMText(propertyValue, properties.get(property));

            propertyElement.addChild(propertyChildElement);
            propertyElement.addChild(propertyValue);
            propertiesElement.addChild(propertyElement);
        }

        Object value = messageContext.getConfigurationContext().getProperty(ActivityPublisherConstants.BAM_MESSAGE_COUNT);
        if (value != null) {
            if (value instanceof Counter) {
                storeValues(messageID, serverNameElement, serviceNameElement, operationNameElement,
                        activityNameElement, activityDescriptionElement, actiivtyIdElement, messageIDElement,
                        remoteIPElement, userAgentElement, timeStampElement, propertiesElement,messageDirectionElement);
            }
        }

        OMNamespace activityNamespace = factory.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI, ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);
        OMElement eventElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_EVENT, activityNamespace);
        EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin().getEventingConfigData();

        if (((Counter) value).getCount() > eventingConfigData.getMessageThreshold()) {
            synchronized (eventingConfigData) {
                ConfigurationContext configContxt = new ConfigurationContext(axisConfiguration);
                ThreadFactory threadFactory = configContxt.getThreadPool();

                EventGenarator thread = new EventGenarator(messageContext, null, eventingConfigData
                        .getMessageThreshold(), eventElement, true, false, false, getMessageMap(), null, null);
                threadFactory.execute(thread);

                setMessageMap(new ConcurrentHashMap<String, Map<String, OMElement>>());

                Counter messageCounter = new Counter();
                messageCounter.resetMessageCount(messageContext);
            }
        }
        return;
    }

    /**
     * This method to support message lookup of new model of activity 
     * XPath expression = Activity Properties 
     * XPathValue = Message Detail/body
     */
    /**
     * Event format for the XPath activity event:
     *
     *(01)<activitydata:Event xmlns:activitydata="http://wso2.org/ns/2009/09/bam/service/activity/data"> 
     *(02)  <activitydata:ActivityData> 
     *(03)       <activitydata:ServerName>https://192.168.2.2:9444</activitydata:ServerName> 
     *(04)       <activitydata:ServiceName>SampleHelloService</activitydata:ServiceName> 
     *(05)       <activitydata:OperationName>sayHello</activitydata:OperationName> 
     *(06)       <activitydata:ActivityID>cccc491-a03a-4260-b177-c493ab7a2dbb</activitydata:ActivityID> 
     *(07)       <activitydata:MessageID>urn:uuid:24E401C5B278FB9C841285140360076</activitydata:MessageID> 
     *(08)       <activitydata:XPathExpressions>
     *(09)          <activitydata:XPathExpression>
     *(10)              <activitydata:ExpressionKey>OrderID</activitydata:ExpressionKey>
     *(11)              <activitydata:Alias>Order Number</activitydata:Alias>
     *(12)              <activitydata :Expression>//ns1:Orders/ns2:orderID</activitydata:Expression>
     *(13)              <activitydata:Namespaces>
     *(14)                 <activitydata:Namespace>ns1@http://www.abc.com</activitydata:Namespace>
     *(15)                 <activitydata:Namespace>ns2@http://www.xyz.com</activitydata:Namespace>
     *(16)              </activitydata:Namespaces>
     *(17)              <activitydata:XPathValue>344423</activitydata:XPathValue>
     *(18)          </activitydata:XPathExpression>
     *(19)       </activitydata:XPatheExpressions>
     *(20)  </activitydata:ActivityData>
     *(21)</activitydata:Event>
     */

    /**
     * (04)      <activitydata:XPathExpressions>
     * (05)          <activitydata:XPathExpression>
     * (06)              <activitydata:ExpressionKey>OrderID</activitydata:ExpressionKey>
     * (07)              <activitydata:Alias>Order Number</activitydata:Alias>
     * (08)              <activitydata :Expression>//ns1:Orders/ns2:orderID</activitydata:Expression>
     * (09)              <activitydata:Namespaces>
     * (10)                 <activitydata:Namespace>ns1@http://www.abc.com</activitydata:Namespace>
     * (11)                 <activitydata:Namespace>ns2@http://www.xyz.com</activitydata:Namespace>
     * (12)              </activitydata:Namespaces>
     * (13)          </activitydata:XPathExpression>
     * (14)      </activitydata:XPatheExpressions>
     */

    /**
     * @param messageContext
     * @param axisConfiguration
     * @param serviceName
     * @param operationName
     * @param activityID
     * @param messageID
     * @param xpathEvaluations
     * @param activityName
     * @param activityDescription
     */
    public static void getMessageLookupEventPayload(MessageContext messageContext,
                                                    AxisConfiguration axisConfiguration,
                                                    String serviceName, String operationName,
                                                    String activityID,
                                                    String messageID,
                                                    Map<XPathConfigData, String> xpathEvaluations,
                                                    String activityName,
                                                    String activityDescription) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace activityNamespace = factory.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);

        /*OMElement serviceInvocationDataElement = factory
            .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_DATA, activityNamespace);*/

        OMElement eventElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_EVENT,
                activityNamespace);
        //eventElement.addChild(serviceInvocationDataElement);

        OMElement serverNameElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVER_NAME, activityNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));

        OMElement serviceNameElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVICE_NAME, activityNamespace);
        factory.createOMText(serviceNameElement, serviceName);

        OMElement operationNameElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_OPERATION_NAME, activityNamespace);
        factory.createOMText(operationNameElement, operationName);

        OMElement activityIdElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_ID, activityNamespace);
        factory.createOMText(activityIdElement, activityID);

        OMElement messageIDElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_ID, activityNamespace);
        factory.createOMText(messageIDElement, messageID);

        OMElement xpathExpressionsElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSIONS, activityNamespace);

        for (XPathConfigData xpathConfig : xpathEvaluations.keySet()) {
            OMElement xpathExpressionElement = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_XPATH_EXPRESSION, activityNamespace);
            OMElement keyElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION_KEY,
                    activityNamespace);
            factory.createOMText(keyElement, xpathConfig.getKey());

            OMElement expressionElement = factory.createOMElement(
                    ActivityPublisherConstants.ACTIVITY_XPATH_EXPRESSION, activityNamespace);
            factory.createOMText(expressionElement, xpathConfig.getXpath());

            OMElement xpathValue = factory.createOMElement(ActivityPublisherConstants.XPATH_VALUE, activityNamespace);
            factory.createOMText(xpathValue, xpathEvaluations.get(xpathConfig));

            OMElement aliasElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_XPATH_ALIAS,
                    activityNamespace);
            factory.createOMText(aliasElement, xpathConfig.getAlias());


            OMElement namespacesElement = factory.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACES,
                    activityNamespace);

            if (xpathConfig.getNameSpaces() != null) {
                for (String ns : xpathConfig.getNameSpaces()) {
                    OMElement namespaceElement = factory.createOMElement(ActivityPublisherConstants.XPATH_NAMESPACE,
                            activityNamespace);
                    factory.createOMText(namespaceElement, ns);
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

        OMElement activityNameElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME, activityNamespace);
        factory.createOMText(activityNameElement, activityName);

        OMElement activityDescriptionElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_DESCRIPTION, activityNamespace);
        factory.createOMText(activityDescriptionElement, activityDescription);

        Object value = messageContext.getConfigurationContext().getProperty(ActivityPublisherConstants.BAM_XPATH_COUNT);
        if (value != null) {
            if (value instanceof Counter) {
                storeXPathValues(messageID, serverNameElement, serviceNameElement, operationNameElement,
                        activityIdElement, messageIDElement, xpathExpressionsElement, activityNameElement,
                        activityDescriptionElement);

                EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin().getEventingConfigData();

                if (((Counter) value).getCount() > eventingConfigData.getMessageThreshold()) {
                    synchronized (eventingConfigData) {
                        ConfigurationContext configContxt = new ConfigurationContext(axisConfiguration);
                        ThreadFactory threadFactory = configContxt.getThreadPool();

                        EventGenarator thread = new EventGenarator(messageContext, null, eventingConfigData
                                .getMessageThreshold(), eventElement, false, false, true, null, null,
                                getXpathMap());
                        threadFactory.execute(thread);

                        setXpathMap(new ConcurrentHashMap<String, Map<String, OMElement>>());
                        Counter messageCounter = new Counter();
                        messageCounter.resetXPathCount(messageContext);
                    }
                }
            }
        }
        return;
    }

    /**
     * This method is used to generate event for BAM_MESSAGE_DATA table
     *
     * @param messageContext
     * @param axisConfiguration
     * @param serviceName
     * @param operationName
     * @param messageID
     * @param activityID
     * @param timeStamp
     * @param messageDirection
     * @param messageBody
     * @param ipAddress
     * @param activityName
     * @param activityDescription
     */
    public static void getMessageDataEventPayload(MessageContext messageContext,
                                                  AxisConfiguration axisConfiguration,
                                                  String serviceName, String operationName,
                                                  String messageID,
                                                  String activityID, String timeStamp,
                                                  String messageDirection, String messageBody,
                                                  String ipAddress, String activityName,
                                                  String activityDescription, Map<String, String> properties) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMNamespace activityNamespace = factory.createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI, ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);

        OMElement serverNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVER_NAME, activityNamespace);
        factory.createOMText(serverNameElement, updateServerName(axisConfiguration));

        OMElement serviceNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVICE_NAME, activityNamespace);
        factory.createOMText(serviceNameElement, serviceName);

        OMElement operationNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_OPERATION_NAME,
                activityNamespace);
        factory.createOMText(operationNameElement, operationName);

        OMElement actiivtyIdElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_ID, activityNamespace);
        factory.createOMText(actiivtyIdElement, activityID);

        OMElement messageIDElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_ID, activityNamespace);
        factory.createOMText(messageIDElement, messageID);

        OMElement messageBodyElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_BODY, activityNamespace);
        factory.createOMText(messageBodyElement, messageBody);

        OMElement messageDirectionElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION,
                activityNamespace);
        factory.createOMText(messageDirectionElement, messageDirection);

        OMElement timeStampElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_TIMESTAMP, activityNamespace);
        factory.createOMText(timeStampElement, timeStamp);

        OMElement propertiesElement = factory
                .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTIES, activityNamespace);

        for (String property : properties.keySet()) {
            OMElement propertyElement = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY, activityNamespace);
            OMElement propertyChildElement = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_CHILD, activityNamespace);
            factory.createOMText(propertyChildElement, property);
            OMElement propertyValue = factory
                    .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_PROPERTY_VALUE,activityNamespace);
            factory.createOMText(propertyValue, properties.get(property));

            propertyElement.addChild(propertyChildElement);
            propertyElement.addChild(propertyValue);
            propertiesElement.addChild(propertyElement);
        }

        OMElement remoteIPElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_REMOTE_IP_ADDRESS, activityNamespace);
        factory.createOMText(remoteIPElement, ipAddress);

        OMElement activityNameElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME, activityNamespace);
        factory.createOMText(activityNameElement, activityName);

        OMElement activityDescriptionElement = factory.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_DESCRIPTION,
                activityNamespace);
        factory.createOMText(activityDescriptionElement, activityDescription);

        Object value = messageContext.getConfigurationContext()
                .getProperty(ActivityPublisherConstants.BAM_MESSAGE_DATA_COUNT);
        if (value != null) {
            if (value instanceof Counter) {
                storeMessageDataValues(messageID, serverNameElement, serviceNameElement, operationNameElement,
                        activityNameElement, activityDescriptionElement, actiivtyIdElement,
                        messageIDElement, remoteIPElement, timeStampElement, messageDirectionElement,
                        messageBodyElement, propertiesElement);

                OMNamespace actNamespace = factory
                        .createOMNamespace(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                                ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);
                OMElement eventElement = factory
                        .createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_EVENT, actNamespace);
                EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin()
                        .getEventingConfigData();

                if (((Counter) value).getCount() > eventingConfigData.getMessageThreshold()) {
                    synchronized (eventingConfigData) {
                        ConfigurationContext configContxt = new ConfigurationContext(axisConfiguration);
                        ThreadFactory threadFactory = configContxt.getThreadPool();

                        EventGenarator thread = new EventGenarator(messageContext, null, eventingConfigData
                                .getMessageThreshold(), eventElement, false, true, false, null,
                                getMessageDataMap(), null);
                        threadFactory.execute(thread);

                        setMessageDataMap(new ConcurrentHashMap<String, Map<String, OMElement>>());

                        Counter messageCounter = new Counter();
                        messageCounter.resetMessageDataCount(messageContext);
                    }
                }
            }
        }
        return;
    }
    /*
     * store message details into a map
     */

    private static void storeValues(String key, OMElement serverNameElement,
                                    OMElement serviceNameElement,
                                    OMElement operationNameElement, OMElement activityNameElement,
                                    OMElement activityDescriptionElement,
                                    OMElement actiivtyIdElement, OMElement messageIDElement,
                                    OMElement remoteIPElement, OMElement userAgentElement,
                                    OMElement timeStampElement, OMElement propertiesElement ,
                                    OMElement messageDirectionElement) {

        ConcurrentMap<String, OMElement> map = new ConcurrentHashMap<String, OMElement>();
        map.put("ServerElement", serverNameElement);
        map.put("ServiceElement", serviceNameElement);
        map.put("OperationElement", operationNameElement);
        map.put("ActivityNameElement", activityNameElement);
        map.put("ActivityDescriptionElement", activityDescriptionElement);
        map.put("ActivityIdElement", actiivtyIdElement);
        map.put("MessageIdElement", messageIDElement);
        map.put("RemoteIpElement", remoteIPElement);
        map.put("UserAgentElement", userAgentElement);
        map.put("TimeStampElement", timeStampElement);
        map.put("PropertiesElement", propertiesElement);
        map.put("MessageDirElement", messageDirectionElement);

        storeMessage(key, map);
    }

    /*
     * Store the message into a map.
     */

    private static void storeMessage(String key, ConcurrentMap<String, OMElement> actMap) {
        if (!messageMap.containsKey(key)) {
            messageMap.put(key, new ConcurrentHashMap<String, OMElement>());
        }
        messageMap.get(key).putAll(actMap);
    }

    /*
    * store messageData details into a map
    */

    private static void storeMessageDataValues(String key, OMElement serverNameElement,
                                               OMElement serviceNameElement,
                                               OMElement operationNameElement,
                                               OMElement activityNameElement,
                                               OMElement activityDescriptionElement,
                                               OMElement actiivtyIdElement,
                                               OMElement messageIDElement,
                                               OMElement remoteIPElement,
                                               OMElement timeStampElement,
                                               OMElement messageDirectionElement,
                                               OMElement messageBodyElement,
                                               OMElement propertiesElement) {

        ConcurrentMap<String, OMElement> map = new ConcurrentHashMap<String, OMElement>();

        map.put("ServerElement", serverNameElement);
        map.put("ServiceElement", serviceNameElement);
        map.put("OperationElement", operationNameElement);
        map.put("ActivityNameElement", activityNameElement);
        map.put("ActivityDescriptionElement", activityDescriptionElement);
        map.put("ActivityIdElement", actiivtyIdElement);
        map.put("MessageIdElement", messageIDElement);
        map.put("RemoteIpElement", remoteIPElement);
        map.put("TimeStampElement", timeStampElement);
        map.put("PropertiesElement", propertiesElement);
        map.put("MessageDirElement", messageDirectionElement);
        map.put("MessageBodyElement", messageBodyElement);

        storeMessageData(key, map);
    }

    /*
     * Store the message into a map.
     */

    private static void storeMessageData(String key, Map<String, OMElement> actMap) {
        if (!messageDataMap.containsKey(key)) {
            messageDataMap.put(key, new HashMap<String, OMElement>());
        }
        messageDataMap.get(key).putAll(actMap);
    }

    /*
     * store XPathValues into a map
     */

    private static void storeXPathValues(String key, OMElement serverNameElement,
                                         OMElement serviceNameElement,
                                         OMElement operationNameElement,
                                         OMElement activityIdElement, OMElement messageIDElement,
                                         OMElement xpathExpressionsElement,
                                         OMElement activityNameElement,
                                         OMElement activityDescriptionElement) {

        ConcurrentMap<String, OMElement> map = new ConcurrentHashMap<String, OMElement>();

        map.put("ServerElement", serverNameElement);
        map.put("ServiceElement", serviceNameElement);
        map.put("OperationElement", operationNameElement);
        map.put("ActivityNameElement", activityNameElement);
        map.put("ActivityDescriptionElement", activityDescriptionElement);
        map.put("ActivityIdElement", activityIdElement);
        map.put("MessageIdElement", messageIDElement);
        map.put("XPathExpElement", xpathExpressionsElement);

        storeXPathData(key, map);
    }

    /*
     * Store the message into a map.
     */

    private static void storeXPathData(String key, Map<String, OMElement> actMap) {
        if (!xpathMap.containsKey(key)) {
            xpathMap.put(key, new HashMap<String, OMElement>());
        }
        xpathMap.get(key).putAll(actMap);
    }


    public static LightWeightEventBrokerInterface getEventBroker() {
        return lightWeightEventBroker;
    }

    public static void setLWEventBroker(LightWeightEventBroker eventBroker) {
        PublisherUtils.lightWeightEventBroker = eventBroker;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        PublisherUtils.configurationContext = configurationContext;
    }

    public static ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfiguration serverConfiguration) {
        PublisherUtils.serverConfiguration = serverConfiguration;
    }

    public static ActivityPublisherAdmin getActivityPublisherAdmin() {
        return activityPublisherAdmin;
    }

    public static void setActivityPublisherAdmin(ActivityPublisherAdmin activityPublisherAdmin) {
        PublisherUtils.activityPublisherAdmin = activityPublisherAdmin;
    }

}
