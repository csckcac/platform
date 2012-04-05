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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

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
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.service.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.service.BAMCalendar;
import org.wso2.carbon.bam.data.publisher.activity.service.Counter;

/***
 * 
 * In Handler of ActivityMessagetracing
 * 
 */
public class ActivityInHandler extends AbstractHandler {

    /*
     * Go through soap header and search for AID . If AID present just pass the message else
     * generate one and add to the header.
     */

    /*
     * SOAP HEADER FORMAT
     */

    /**
     *(1)<soapenv:Header> 
     *(2)    <ns:BAMEvent xmlns:ns="http://wso2.org/ns/2010/10/bam" activityID="asiasd-sdswodi-2329"> 
     *(3)        <ns:Property name="order details" value="Order Id 100" /> 
     *(4)  </ns:BAMEvent> 
     *(5)</soapenv:Header>
     */

    private static Log log = LogFactory.getLog(ActivityInHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        String messageID = "";
        String activityID = "";
        String activityName = "";
        String activityDescription = "";
        String userAgent = "";
        String remoteIPAddress = "";
        String activityProperty = "";
        String activityPropertyValue = "";
        // no of properties we try to store, so pass it as a map 
        Map<String, String> properties = new HashMap<String, String>();
        
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
     
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
        SOAPFactory soapFactory = null;
        SOAPHeaderBlock soapHeaderBlock = null;
        UUID uuid_random = UUID.randomUUID();
        String uuid = uuid_random.toString();
        
        //Go through the header and see whether the AID is present or not. If not add.
        if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Not a standard soap message");
        }
        if (soapEnvelope.getHeader() != null) {
            Iterator itr = soapEnvelope.getHeader()
                .getChildrenWithName(
                                     new QName(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI,
                                         "BAMEvent"));
            if (itr.hasNext() == false) {
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                soapHeaderBlock.addAttribute("activityID", uuid, null);
            } else {
                OMElement element = (OMElement) itr.next();
                String aid = element.getAttributeValue(new QName("activityID"));
                if (aid != null) {
                    if (aid.equals("")) {
                        element.addAttribute("activityID", uuid, null);
                    }
                }
                else {
                    element.addAttribute("activityID", uuid, null);
                }
            }
        }
        if (soapEnvelope.getHeader() == null) {           
            if (soapFactory != null) {
                (soapFactory).createSOAPHeader(soapEnvelope);
            }
            if (soapEnvelope.getHeader() != null) { 
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                soapHeaderBlock.addAttribute("activityID", uuid, null);            
            }
       }
        
        if (messageContext.getProperty("REMOTE_ADDR") != null) {
            remoteIPAddress = (String) messageContext.getProperty("REMOTE_ADDR");
        }
        
        try {
            messageID = messageContext.getMessageID();
        } catch (Exception e) {
            log.warn("Could not find messageID for the incoming message.", e);
        }
        if (messageID== null) {
            messageID = uuid.toString();
            messageContext.setMessageID(messageID);
        }
        // process AID, property

        Iterator itr = messageContext.getEnvelope().getHeader()
        .getChildrenWithName(new QName(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI,"BAMEvent"));
        
        if(itr.hasNext()) {
            OMElement element = (OMElement)itr.next();
            activityID = element.getAttributeValue(new QName("activityID"));
            Iterator childItr = element.getChildElements();
            if (childItr.hasNext()) {
                OMElement childElement = (OMElement) childItr.next();
                activityProperty = childElement.getAttributeValue(new QName("name"));
                activityPropertyValue = childElement.getAttributeValue(new QName("value"));
            }
        }
        if (activityProperty != null && activityPropertyValue != null) {
            if (!activityProperty.equals("") && !activityPropertyValue.equals("")) {
                properties.put(activityProperty, activityPropertyValue);
            }
        }
        genericEvent(messageContext, messageID, activityID, activityName, activityDescription, userAgent,
                      remoteIPAddress, properties);

        return InvocationResponse.CONTINUE;
    }

    /*
     * Generate the Event
     */
    private synchronized void genericEvent(MessageContext messageContext, String messageID, String activityID, String activityName,
        String activityDescription, String userAgent, String remoteIPAddress,  Map<String, String> properties) {
       
   
        // avoid admin services and hidden services
        if(messageContext.getAxisService() !=null) {
        AxisService service = messageContext.getAxisService();
        Parameter param_admin = service.getParameter("adminService");
        Parameter param_hidden = service.getParameter("hiddenService");
        if (param_admin == null && param_hidden == null) {          
            
            EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin()
                    .getEventingConfigData();

                if (eventingConfigData != null && eventingConfigData.eventingEnabled()) {
                    if (eventingConfigData.messageDumpingEnabled()) {
                        messageDataEvent(messageContext, messageID, activityID, remoteIPAddress, activityName,
                                            activityDescription,properties);
                    }
                    else{
                        //set a counter, to support batch mode of event
                        Object value = messageContext.getConfigurationContext()
                                .getProperty(ActivityPublisherConstants.BAM_MESSAGE_COUNT);
                        if (value != null) {
                            if (value instanceof Counter) {
                                ((Counter) value).increment();
                            }
                        } else {
                            Counter messageCounter = new Counter();
                            messageCounter.increment();
                            messageContext.getConfigurationContext()
                                    .setProperty(ActivityPublisherConstants.BAM_MESSAGE_COUNT, messageCounter);
                        }
                        PublisherUtils.getEventPayload(messageContext, messageContext.getConfigurationContext()
                                .getAxisConfiguration(), messageID, activityID, messageContext.getAxisService().getName(),
                                messageContext.getAxisOperation().getName().getLocalPart(),
                                activityName, activityDescription, remoteIPAddress, userAgent,
                                BAMCalendar.getInstance(Calendar.getInstance())
                                        .getBAMTimestamp(), properties,ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_IN);
                    }
                }
            }
        }
    }
    /**
     * Event for messageBody
     * 
     * @param messageContext
     * @param messageID
     * @param activityID
     * @param remoteIPAddress
     */
    private synchronized void messageDataEvent(MessageContext messageContext, String messageID, String activityID,
        String remoteIPAddress, String activityName, String activityDescription,Map<String, String> properties) {

        //set a counter property
        Object value = messageContext.getConfigurationContext()
            .getProperty(ActivityPublisherConstants.BAM_MESSAGE_DATA_COUNT);
        if (value != null) {
            if (value instanceof Counter) {
                ((Counter) value).increment();
            }
        } else {
            Counter messageCounter = new Counter();
            messageCounter.increment();
            messageContext.getConfigurationContext().setProperty(ActivityPublisherConstants.BAM_MESSAGE_DATA_COUNT,
                                                                 messageCounter);
        }

       PublisherUtils.getMessageDataEventPayload(messageContext, messageContext.getConfigurationContext()
            .getAxisConfiguration(), messageContext.getAxisService().getName(), messageContext.getAxisOperation()
            .getName().getLocalPart(), messageID, activityID, BAMCalendar.getInstance(Calendar.getInstance())
            .getBAMTimestamp(),ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_IN, messageContext.getEnvelope().getBody().toString(), remoteIPAddress,
                                                  activityName, activityDescription,properties);
       
    }

  
}
