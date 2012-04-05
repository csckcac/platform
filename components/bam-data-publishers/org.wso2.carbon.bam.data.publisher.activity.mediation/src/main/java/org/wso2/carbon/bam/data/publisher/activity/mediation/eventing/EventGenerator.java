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

package org.wso2.carbon.bam.data.publisher.activity.mediation.eventing;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityProcessor;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.mediation.MessageActivity;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;

public class EventGenerator implements ActivityProcessor {

    private static final Log log = LogFactory.getLog(EventGenerator.class);

    private OMFactory fac = OMAbstractFactory.getOMFactory();

    public void process(MessageActivity[] activities) {
        if (activities == null || activities.length == 0) {
            return;
        }

        // Create the top level Event element (parent)
        OMNamespace actNamespace = fac.createOMNamespace(
                ActivityPublisherConstants.ACTIVITY_DATA_NS_URI, ActivityPublisherConstants.ACTIVITY_DATA_NS_PREFIX);
        OMElement eventElement = fac.createOMElement(ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_EVENT, actNamespace);

        // Add activity elements to the event
        for (MessageActivity activity : activities) {
            eventElement.addChild(ActivityPublisherUtils.serialize(activity));
        }

//        // Construct the event MessageContext
//        MessageContext eventMsgCtx = new MessageContext();
//        SOAPFactory eventSoapFactory = OMAbstractFactory.getSOAP12Factory();
//        SOAPEnvelope envelope = eventSoapFactory.getDefaultEnvelope();
//        envelope.getBody().addChild(eventElement);
//        try {
//            eventMsgCtx.setEnvelope(envelope);
//        } catch (AxisFault e) {
//            log.error("Error while setting the event payload to the SOAP envelope" , e);
//            return;
//        }
//
//        // Create a new event and publish to the event broker service
//        ActivityThresholdEvent<MessageContext> event = new ActivityThresholdEvent<MessageContext>(eventMsgCtx);
//        event.setResourcePath(ActivityPublisherConstants.BAM_REG_PATH);

        try {
            LightWeightEventBroker eb = ActivityPublisherUtils.getEventBroker().getInstance();

            SuperTenantCarbonContext.startTenantFlow();
            int tenantId = SuperTenantCarbonContext.getCurrentContext(ActivityPublisherUtils.getConfigurationContextService().getServerConfigContext()).getTenantId();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
            SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

            eb.publish(ActivityPublisherConstants.BAM_REG_PATH, eventElement);

        } catch (Exception e) {
            log.error("Can not publish the message ", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
//            try {
//				if (eb != null) {
//
//				    eb.publish(message, ActivityPublisherConstants.BAM_REG_PATH);
//				    if (log.isDebugEnabled()) {
//				        log.debug("Publishing BAM activity event: " + message.getMessage());
//				    }
//				}
//			} catch (EventBrokerException e) {
//				log.error("Unable to publish event : " + message.getMessage() + "\n with topic" +
//						ActivityPublisherConstants.BAM_REG_PATH);
//			}
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Terminating BAM EventGenerator");
        }
    }
}