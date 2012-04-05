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

package org.wso2.carbon.broker.core.internal.brokers.ws;

import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.BrokerListener;

import java.util.List;
import java.util.ArrayList;

public class SubscriptionMessageReceiver extends AbstractInMessageReceiver {

    private static final Log log = LogFactory.getLog(SubscriptionMessageReceiver.class);

    private List<BrokerListener> brokerListeners;

    public SubscriptionMessageReceiver() {
        this.brokerListeners = new ArrayList<BrokerListener>();
    }

    public void addBrokerListener(BrokerListener brokerListener){
        this.brokerListeners.add(brokerListener);
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

        // notify the BrokerProxies
        try {
            for (BrokerListener brokerListener : this.brokerListeners){
                brokerListener.onEvent(bodyElement);
            }
        } catch (BrokerEventProcessingException e) {
            log.error("Can not process the received event ", e);
        }
    }
}