package org.wso2.carbon.bam.data.publisher.activity.service.modules;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.service.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.service.BAMCalendar;
import org.wso2.carbon.bam.data.publisher.activity.service.Counter;
import org.wso2.carbon.bam.data.publisher.activity.service.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;

/***
 * Out Handler of ActivityMessagetracing
 * 
 */
public class ActivityOutHandler extends AbstractHandler {

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
  
    
    private static Log log = LogFactory.getLog(ActivityOutHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        String activityID = "";
        String activityName = "";
        String activityDescription = "";
        String userAgent = "";
        String remoteIPAddress = "";
        String activityProperty = "";
        String activityPropertyValue = "";
        String outMessageID = "";
      

        Map<String, String> properties = new HashMap<String, String>();
        ConcurrentMap<String, String> activity ;
        
        try {
            outMessageID = messageContext.getMessageID();
        } catch (Exception e) {
            log.warn("Could not find messageID for the outgoing message.", e);
        }
        UUID uuid = UUID.randomUUID();
        if (outMessageID== null) {
            outMessageID = uuid.toString();
            messageContext.setMessageID(outMessageID);
        }
        if (messageContext.getProperty("REMOTE_ADDR") != null) {
            remoteIPAddress = (String) messageContext.getProperty("REMOTE_ADDR");
        }
 
        //get IN Message Context from OutMessagecontext to track request and response
        MessageContext inMessagecontext= messageContext.getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
       
        activity = processInMessageContext(inMessagecontext);
        if (activity != null) {
            activityID = activity.get("ActivityID");
            activityProperty = activity.get("ActivityProperty");
            activityPropertyValue = activity.get("ActivityPropertyValue");
        }
          if (activityProperty != null && activityPropertyValue != null) {
              if (!activityProperty.equals("") && !activityPropertyValue.equals("")) {
                  properties.put(activityProperty, activityPropertyValue);
              }
          }
       
        genericEvent(messageContext,  outMessageID, activityID, activityName, activityDescription, userAgent,
                          remoteIPAddress, properties);

        // Now set all values to response also
        engageSOAPHeaders(messageContext, activityID, activityProperty, activityPropertyValue);
      
        return InvocationResponse.CONTINUE;
    }

    /*
     * Generate the Event
     */
    private synchronized void genericEvent(MessageContext messageContext, String outMessageID, String activityID,
        String activityName, String activityDescription, String userAgent, String remoteIPAddress,
        Map<String, String> properties) {

        // avoid admin services and hidden services
        if(messageContext.getAxisService() !=null) {
        AxisService service = messageContext.getAxisService();
        Parameter param_admin = service.getParameter("adminService");
        Parameter param_hidden = service.getParameter("hiddenService");
        if (param_admin == null && param_hidden == null) {

            EventingConfigData eventingConfigData = PublisherUtils.getActivityPublisherAdmin().getEventingConfigData();

            if (eventingConfigData != null && eventingConfigData.eventingEnabled()) {
                if (eventingConfigData.messageDumpingEnabled()) {
                    messageDataEvent(messageContext, outMessageID, activityID, remoteIPAddress, activityName,
                            activityDescription,properties);
                }else{
                    Object value = messageContext.getConfigurationContext()
                            .getProperty(ActivityPublisherConstants.BAM_MESSAGE_COUNT);
                    if (value != null) {
                        if (value instanceof Counter) {
                            ((Counter) value).increment();
                        }
                    } else {
                        Counter messageCounter = new Counter();
                        messageCounter.increment();
                        messageContext.getConfigurationContext().setProperty(ActivityPublisherConstants.BAM_MESSAGE_COUNT,
                                messageCounter);
                    }
                    PublisherUtils.getEventPayload(messageContext, messageContext.getConfigurationContext()
                            .getAxisConfiguration(), outMessageID, activityID, messageContext.getAxisService().getName(),
                            messageContext.getAxisOperation().getName().getLocalPart(),
                            activityName, activityDescription, remoteIPAddress, userAgent,
                            BAMCalendar.getInstance(Calendar.getInstance()).getBAMTimestamp(),
                            properties,ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_OUT );
                }
            }
        }
        }
    }
    
    //Message Detail event payload
    private synchronized void messageDataEvent(MessageContext messageContext, String outMessageID, String activityID,
        String remoteIPAddress, String activityName, String activityDescription,Map<String, String> properties) {

        //set a counter property for response
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
                                                  .getName().getLocalPart(), outMessageID, activityID, BAMCalendar.getInstance(Calendar.getInstance())
                                                  .getBAMTimestamp(),ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION_OUT , messageContext.getEnvelope().getBody().toString(), remoteIPAddress,
                                                                                        activityName, activityDescription,properties);
       
    }

    /**
     * We engage activityID,property,value for the response also.
     */
    private synchronized void engageSOAPHeaders(MessageContext messageContext, String actID, String property, String propertyValue) {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
        SOAPFactory soapFactory = null;
        SOAPHeaderBlock soapHeaderBlock =null;
       
        if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            log.error("Not a standard soap message");
        }

        OMElement bampropertyElement = fac.createOMElement(new QName(
            ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "Property", "ns"), null);
        bampropertyElement.addAttribute("name", property, null);
        bampropertyElement.addAttribute("value", propertyValue, null);

        // If header is not null check for BAM headers
        if (soapEnvelope.getHeader() != null) {
            Iterator itr = soapEnvelope.getHeader()
                .getChildrenWithName(
                                     new QName(ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI,
                                         "BAMEvent"));
            if (!itr.hasNext()) {
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                soapHeaderBlock.addAttribute("activityID", actID, null);
                soapHeaderBlock.addChild(bampropertyElement);
            }
        } // If header is null add BAM headers
        if (soapEnvelope.getHeader() == null) {
            if (soapFactory != null) {
                (soapFactory).createSOAPHeader(soapEnvelope);
            }
            if (soapEnvelope.getHeader() != null) {
                soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                soapHeaderBlock.addAttribute("activityID", actID, null);
                soapHeaderBlock.addChild(bampropertyElement);
            }
        }
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
