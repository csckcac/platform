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
package org.wso2.carbon.eventing.broker;

import org.wso2.eventing.EventDispatcher;
import org.wso2.eventing.Event;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.exceptions.EventException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.Policy;

public class CarbonEventDispatcher extends CarbonEventBrokerConstants implements EventDispatcher {

    private ConfigurationContext configContext = null;
    private Policy policy = null;
    protected CarbonNotificationManager notificationManager = null;

    private static Log log = LogFactory.getLog(CarbonEventDispatcher.class);

    public boolean send(Event event, Subscription subscription) throws EventException {
        String endpoint = subscription.getEndpointUrl();
        if (endpoint == null || !(event.getMessage() instanceof MessageContext)) {
            return false;
        }
        String topic = event.getTopic();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace topicNs = factory.createOMNamespace(
                NOTIFICATION_NS_URI,
                NOTIFICATION_NS_PREFIX);
        OMElement topicEle = factory.createOMElement(EventingConstants.WSE_EN_TOPIC, topicNs);
        topicEle.setText(topic);
        MessageContext mc = (MessageContext)event.getMessage();
        SOAPEnvelope envelope = mc.getEnvelope();
        if (envelope.getBody() == null) {
            return false;
        }
        OMElement payload = envelope.getBody().getFirstElement();
        try {
            sendNotification(topicEle, payload, endpoint, null);
            return true;
        } catch (Exception e) {
            log.error("Unable to send message", e);
            return false;
        }
    }

    public void setNotificationManager(CarbonNotificationManager notificationManager) {
        this.notificationManager = notificationManager;    
    }

    public void init(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    protected void sendNotification(OMElement topic, OMElement payload, String endpoint,
                                  Object args) throws AxisFault {
        if (configContext == null) {
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            if (messageContext != null) {
                configContext = messageContext.getConfigurationContext();
            }
        }

        ServiceClient serviceClient;
        if (configContext != null) {
            serviceClient = new ServiceClient(configContext, null);
        } else {
            serviceClient = new ServiceClient();
        }
        Options options = new Options();
        serviceClient.engageModule("addressing");
        options.setTo(new EndpointReference(endpoint));
        // Try obtaining the policy if we don't have it.
        if (policy == null) {
            if (this.notificationManager != null) {
                String policyPath = this.notificationManager.getPropertyValue("securityPolicy");
                if (policyPath != null) {
                    try {
                        StAXOMBuilder builder = new StAXOMBuilder(policyPath);
                        policy =  PolicyEngine.getPolicy(builder.getDocumentElement());
                    } catch (Exception e) {
                        log.error("Unable to attach security policy.", e);
                    }
                }
            }
        }
        // If we have the policy, use it.
        if (policy != null) {
            options.setProperty("rampartPolicy", policy);
            serviceClient.engageModule("rampart");
        }
        options.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, Boolean.TRUE);
        options.setAction(EventingConstants.WSE_PUBLISH);
        serviceClient.setOptions(options);
        serviceClient.addHeader(topic);
        serviceClient.fireAndForget(payload);
    }
}
