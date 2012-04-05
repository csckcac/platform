/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.data.publisher.activity.service.modules;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.bam.data.publisher.activity.service.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.service.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.service.XPathStore;
import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.Counter;
import org.wso2.carbon.bam.data.publisher.activity.service.config.XPathConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.data.MessageData;

/*
 * This handler is used to extract Message lookup details.
 */
public class XPathLookupHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(XPathLookupHandler.class);
    private static Map<Integer, String> xpathValues = new HashMap<Integer, String>();
    private static MessageData messageData = new MessageData();

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        ConcurrentMap<String, String> activity;
        String activityID = "";
        String activityName = "";
        String activityDescription = "";
        String xmlStream = messageContext.getEnvelope().getBody().toString();
        List listOfNodes = null;
        int flow = messageContext.getFLOW();
        if (messageContext.getAxisService() != null) {
            AxisService service = messageContext.getAxisService();
            Parameter param_admin = service.getParameter("adminService");
            Parameter param_hidden = service.getParameter("hiddenService");
            if (param_admin == null && param_hidden == null) {
                EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin()
                        .getEventingConfigData();
                if (eventingConfigData != null && eventingConfigData.eventingEnabled()) {
                    if (eventingConfigData.messageLookupEnabled()) {
                        // set a counter property to support messagelookup
                        Object value = messageContext.getConfigurationContext()
                                .getProperty(ActivityPublisherConstants.BAM_XPATH_COUNT);
                        if (value != null) {
                            if (value instanceof org.wso2.carbon.bam.data.publisher.activity.service.Counter) {
                                ((Counter) value).increment();
                            }
                        } else {
                            Counter xpathCounter = new Counter();
                            xpathCounter.increment();
                            messageContext.getConfigurationContext()
                                    .setProperty(ActivityPublisherConstants.BAM_XPATH_COUNT, xpathCounter);
                        }

                        XPathConfigData[] xpathExps = null;
                        try {
                            xpathExps = PublisherUtils.getActivityPublisherAdmin().getXPathData();
                        } catch (Exception e) {
                            log.error("Error while fetching xpath properties from registry..", e);
                        }

                        if (xpathExps != null && xpathExps.length > 0) {
                            UUID uuid = UUID.randomUUID();
                            String messageID = messageContext.getMessageID();
                            if (messageID == null) {
                                messageID = uuid.toString();
                                messageContext.setMessageID(messageID);
                            }
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlStream.getBytes());
                            try {
                                //Get from MessageContext (Flow is 'IN')
                                if (flow == 1) {
                                    activity = processInMessageContext(messageContext);
                                    if (activity != null) {
                                        activityID = activity.get("ActivityID");
                                    }
                                }
                                //get from IN Message Context using OutMessagecontext to track request and response
                                else {
                                    MessageContext inMessagecontext = messageContext.getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);

                                    activity = processInMessageContext(inMessagecontext);
                                    if (activity != null) {
                                        activityID = activity.get("ActivityID");
                                    }
                                }

                                StAXBuilder builder = new StAXOMBuilder(byteArrayInputStream);
                                OMElement root = builder.getDocumentElement();
                                Map<XPathConfigData, String> xpathEvaluations = new HashMap<XPathConfigData, String>();

                                for (XPathConfigData xpathExp : xpathExps) {
                                    AXIOMXPath xpath = new AXIOMXPath(xpathExp.getXpath());
                                    String[] namespaces = xpathExp.getNameSpaces();

                                    if (namespaces != null && namespaces.length > 0) {
                                        for (String ns : namespaces) {
                                            String tokens[] = ns.split("@");

                                            if (tokens != null && tokens.length >= 2) {
                                                xpath.addNamespace(tokens[0], tokens[1]);
                                            }
                                        }
                                    }

                                    listOfNodes = xpath.selectNodes(root);
                                    if (listOfNodes != null && listOfNodes.size() > 0) {
                                        for (int i = 0; i < listOfNodes.size(); i++) {
                                            OMContainer omContainer = (OMContainer) listOfNodes.get(i);

                                            if (omContainer instanceof OMDocument) {
                                                omContainer = ((OMDocument) omContainer).getOMDocumentElement();
                                            }

                                            String xpathValue = omContainer.toString();
                                            xpathValues.put(i + 1, xpathValue);
                                        }
                                        messageData.addValuesForXPath(xpathExp.getKey(), xpathValues);
                                        XPathStore.storeMessageXPathData(messageID, messageData);
                                    }

                                    if (XPathStore.getMessageXPathData(messageID) != null) {
                                        MessageData msgData = XPathStore.getMessageXPathData(messageID);
                                        Map<Integer, String> xpathDet = msgData.getValuesForXPath(xpathExp.getKey());
                                        String xpathEvaluation = "";
                                        for (int i = 0; i < xpathDet.size(); i++) {
                                            xpathEvaluation += xpathDet.get(i + 1);
                                        }
                                        xpathEvaluations.put(xpathExp, xpathEvaluation);
                                    }
                                }
                                AxisConfiguration axisConfig = messageContext.getConfigurationContext()
                                        .getAxisConfiguration();

                                PublisherUtils.getMessageLookupEventPayload(messageContext, axisConfig, messageContext
                                        .getAxisService().getName(), messageContext.getAxisOperation().getName()
                                        .getLocalPart(), activityID, messageID, xpathEvaluations, activityName,
                                                                            activityDescription);

                            } catch (JaxenException e) {
                                log.error("Error in creating XPath", e);
                            } catch (XMLStreamException e) {
                                log.error("Could not create OMBuilder", e);
                            } catch (Exception e) {
                                log.error("XPathe handler error", e);
                            }
                        }
                    }
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

    private ConcurrentMap<String, String> processInMessageContext(MessageContext inMsgContext) {
        // process AID, property
        ConcurrentMap<String, String> activity = new ConcurrentHashMap<String, String>();
        String activityID = "";
        String activityProperty = "";
        String activityPropertyValue = "";

        Iterator itr = inMsgContext
                .getEnvelope()
                .getHeader()
                .getChildrenWithName(new QName(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "BAMEvent"));

        if (itr.hasNext()) {
            OMElement element = (OMElement) itr.next();
            activityID = element.getAttributeValue(new QName("activityID"));
            Iterator childItr = element.getChildElements();
            if (childItr.hasNext()) {
                OMElement childElement = (OMElement) childItr.next();
                activityProperty = childElement.getAttributeValue(new QName("name"));
                activityPropertyValue = childElement.getAttributeValue(new QName("value"));
            }
        }
        activity.put("ActivityID", activityID);
        activity.put("ActivityProperty", activityProperty);
        activity.put("ActivityPropertyValue", activityPropertyValue);

        return activity;

    }
}
