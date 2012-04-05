package org.wso2.carbon.bam.data.publisher.activity.service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;

import javax.mail.Service;

/**
 * This thread handles the event generation.
 */
class EventGenarator implements Runnable {
    private Log log = LogFactory.getLog(EventGenarator.class);

    OMFactory factory = OMAbstractFactory.getOMFactory();
    int threshold;
    MessageContext messageContext;
    OMElement eventElement;
    boolean genericEvent = false;
    boolean messageDumpEvent = false;
    boolean xpathEvent = false;
    AxisConfiguration axisConf;
    OMNamespace activityNamespace = factory.createOMNamespace("http://wso2.org/ns/2009/09/bam/service/activity/data",
                                                              "activitydata");

    // Have separate map to deal with reference
    ConcurrentMap<String, Map<String, OMElement>> messageMap = null;
    ConcurrentMap<String, Map<String, OMElement>> messageDataMap = null;
    ConcurrentMap<String, Map<String, OMElement>> xpathMap = null;

    /**
     * @param messageContext
     * @param axisConf
     * @param threshold
     * @param eventElement
     * @param genericEvent
     * @param messageDumpEvent
     * @param xpathEvent
     * @param messageMap
     * @param messageDataMap
     * @param xpathMap
     */
    EventGenarator(MessageContext messageContext, AxisConfiguration axisConf, int threshold,
                   OMElement eventElement,
                   boolean genericEvent, boolean messageDumpEvent, boolean xpathEvent,
                   ConcurrentMap<String, Map<String, OMElement>> messageMap,
                   ConcurrentMap<String, Map<String, OMElement>> messageDataMap,
                   ConcurrentMap<String, Map<String, OMElement>> xpathMap) {
        this.messageContext = messageContext;
        this.threshold = threshold;
        this.eventElement = eventElement;
        this.genericEvent = genericEvent;
        this.messageDumpEvent = messageDumpEvent;
        this.xpathEvent = xpathEvent;
        this.messageMap = messageMap;
        this.messageDataMap = messageDataMap;
        this.xpathMap = xpathMap;
        this.axisConf = axisConf;
    }

    public void run() {

        if (genericEvent) {
            genericEvent();
        }
        if (messageDumpEvent) {
            messageDumpEvent();
        }
        if (xpathEvent) {
            xpathEvent();
        }

    }

    private void genericEvent() {
        Iterator<String> it = messageMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Map<String, OMElement> map = messageMap.get(key);
            OMElement serviceInvocationDataElement = factory.createOMElement("ActivityData", activityNamespace);

            if (map != null && serviceInvocationDataElement != null) {

                serviceInvocationDataElement.addChild(map.get("ServerElement"));
                serviceInvocationDataElement.addChild(map.get("ServiceElement"));
                serviceInvocationDataElement.addChild(map.get("OperationElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityNameElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityDescriptionElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityIdElement"));
                serviceInvocationDataElement.addChild(map.get("MessageIdElement"));
                serviceInvocationDataElement.addChild(map.get("MessageDirElement"));
                serviceInvocationDataElement.addChild(map.get("RemoteIpElement"));
                serviceInvocationDataElement.addChild(map.get("UserAgentElement"));
                serviceInvocationDataElement.addChild(map.get("TimeStampElement"));
                serviceInvocationDataElement.addChild(map.get("PropertiesElement"));

                eventElement.addChild(serviceInvocationDataElement);
            }

        }
        generateEvent(eventElement);
    }


    private void messageDumpEvent() {

        Iterator<String> k = messageDataMap.keySet().iterator();
        while (k.hasNext()) {
            String key = k.next();
            Map<String, OMElement> map = messageDataMap.get(key);

            OMElement serviceInvocationDataElement = factory.createOMElement("ActivityData", activityNamespace);

            if (map != null && serviceInvocationDataElement != null) {

                serviceInvocationDataElement.addChild(map.get("ServerElement"));
                serviceInvocationDataElement.addChild(map.get("ServiceElement"));
                serviceInvocationDataElement.addChild(map.get("OperationElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityNameElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityDescriptionElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityIdElement"));
                serviceInvocationDataElement.addChild(map.get("MessageIdElement"));
                serviceInvocationDataElement.addChild(map.get("RemoteIpElement"));
                serviceInvocationDataElement.addChild(map.get("TimeStampElement"));
                serviceInvocationDataElement.addChild(map.get("PropertiesElement"));
                serviceInvocationDataElement.addChild(map.get("MessageDirElement"));
                serviceInvocationDataElement.addChild(map.get("MessageBodyElement"));

                eventElement.addChild(serviceInvocationDataElement);

            }

        }
        generateEvent(eventElement);
    }


    private void xpathEvent() {
        Iterator<String> k = xpathMap.keySet().iterator();

        while (k.hasNext()) {
            String key = k.next();
            Map<String, OMElement> map = xpathMap.get(key);
            OMElement serviceInvocationDataElement = factory.createOMElement("ActivityData", activityNamespace);

            if (map != null && serviceInvocationDataElement != null) {

                serviceInvocationDataElement.addChild(map.get("ServerElement"));
                serviceInvocationDataElement.addChild(map.get("ServiceElement"));
                serviceInvocationDataElement.addChild(map.get("OperationElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityIdElement"));
                serviceInvocationDataElement.addChild(map.get("MessageIdElement"));
                serviceInvocationDataElement.addChild(map.get("XPathExpElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityNameElement"));
                serviceInvocationDataElement.addChild(map.get("ActivityDescriptionElement"));

                eventElement.addChild(serviceInvocationDataElement);

            }
        }
        generateEvent(eventElement);
    }

    private void generateEvent(OMElement statMessage) {
        if (statMessage != null) {
            Message message = new Message();
            message.setMessage(statMessage);

            try {

                LightWeightEventBrokerInterface broker = ServiceHolder.getLWEventBroker();

                SuperTenantCarbonContext.startTenantFlow();
                int tenantId = SuperTenantCarbonContext.getCurrentContext(PublisherUtils.getConfigurationContext()).getTenantId();
                SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
                SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

                broker.publish(ActivityPublisherConstants.BAM_REG_PATH, statMessage);

            } catch (Exception e) {
                log.error("Can not publish the message ", e);
            } finally {
                SuperTenantCarbonContext.endTenantFlow();
            }
//            try {
//                if (broker != null) {
//                    broker.publish(message, ActivityPublisherConstants.BAM_REG_PATH);
//                    if (log.isDebugEnabled()) {
//                        log.debug("Event is published" + message.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("EventGenerator - Unable to publish event", e);
//            }
        }
    }
}
