/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.eventing.broker.utils;

import org.wso2.eventing.Event;
import org.wso2.eventing.EventingConstants;
import org.wso2.carbon.eventing.broker.receivers.CarbonEventingMessageReceiver;
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;
import org.wso2.carbon.eventing.broker.CarbonEventDispatcher;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.context.MessageContext;

public class EventBrokerUtils {

    public static boolean generateEvent(OMElement payload, OMElement topic,
                                        EventBrokerService brokerService, int tenantId)
            throws EventBrokerException {
        if (payload == null) {
            throw new EventBrokerException("Unable to generate event. No payload was given.");
        }
        CarbonEventingMessageReceiver dummyReceiver = new CarbonEventingMessageReceiver();
        if (brokerService != null) {
            dummyReceiver.setBrokerService(brokerService);
        }
        MessageContext mc = new MessageContext();
        SuperTenantCarbonContext.getCurrentContext(mc).setTenantId(tenantId);
        SOAPFactory soapFactory = new SOAP12Factory();
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        envelope.getBody().addChild(payload);
        if (topic != null) {
            envelope.getHeader().addChild(topic);
        }
        try {
            mc.setEnvelope(envelope);
            dummyReceiver.processMessage(mc);
        } catch (Exception e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            }
            throw new EventBrokerException("Unable to generate event.", e);
        }
        return true;
    }

    public static boolean generateEvent(OMElement payload, OMElement topic,
                                        EventBrokerService brokerService)
            throws EventBrokerException {
        return generateEvent(payload, topic, brokerService, 0);
    }

    public static OMElement buildTopic(OMFactory factory, Event event) {
        OMNamespace topicNs = factory.createOMNamespace(
                CarbonEventDispatcher.NOTIFICATION_NS_URI,
                CarbonEventDispatcher.NOTIFICATION_NS_PREFIX);
        OMElement topicEle = factory.createOMElement(EventingConstants.WSE_EN_TOPIC, topicNs);
        topicEle.setText(event.getTopic());
        return topicEle;
    }
}
