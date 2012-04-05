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
package org.wso2.carbon.eventing.broker.receivers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.wso2.eventing.exceptions.EventException;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.Event;
import org.wso2.carbon.eventing.broker.builders.utils.BuilderUtils;
import org.wso2.carbon.eventing.broker.builders.exceptions.InvalidExpirationTimeException;
import org.wso2.carbon.eventing.broker.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.eventing.broker.builders.*;
import org.wso2.carbon.eventing.broker.CarbonEventBroker;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;

public class CarbonEventingMessageReceiver extends AbstractMessageReceiver {

    private EventBrokerService brokerService = null;

    private static final Log log = LogFactory.getLog(CarbonEventingMessageReceiver.class);

    private static final String EVENT_BROKER_INSTANCE = "eventBrokerInstance";

    private static final String ENABLE_SUBSCRIBE = "enableSubscribe";

    private static final String ENABLE_UNSUBSCRIBE = "enableUnsubscribe";

    private static final String ENABLE_RENEW = "enableRenew";

    private static final String ENABLE_GET_STATUS = "enableGetStatus";

    private boolean isEnabled(MessageContext mc, String operation) {
        if (mc.getAxisService() != null) {
            String operationValue =
                    (String) mc.getAxisService().getParameterValue(operation);
            return operationValue == null || !operationValue.toLowerCase().equals(
                    Boolean.toString(false));
        }
        return true;
    }

    private void createBrokerService(MessageContext mc) {
        if (this.brokerService != null) {
            return;
        }
        if (mc.getAxisService() != null) {
            String eventBrokerInstance =
                    (String) mc.getAxisService().getParameterValue(EVENT_BROKER_INSTANCE);
            if (eventBrokerInstance != null) {
                this.setBrokerService(CarbonEventBroker.getInstance(eventBrokerInstance));
                return;
            }
        }
        this.setBrokerService(CarbonEventBroker.getInstance());
    }

    public final void invokeBusinessLogic(MessageContext mc) throws AxisFault {
        try {
            createBrokerService(mc);
            processMessage(mc);
        } catch (EventException e) {
            log.error("An exception occured. Unable to Process Request", e);
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "An exception occured. Unable to Process Request ", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleSubscribe(MessageContext mc) throws AxisFault, EventException {
        if (!isEnabled(mc, ENABLE_SUBSCRIBE)) {
            log.warn("Subscribe operation is disabled");
            return;
        }
        Subscription subscription = null;
        SubscribeCommandBuilder builder = new SubscribeCommandBuilder(mc);
        try {
            subscription = builder.toSubscription(mc.getEnvelope());
            if (mc.getTo() != null) {
                subscription.setAddressUrl(mc.getTo().getAddress());
            }
        } catch (InvalidExpirationTimeException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "InvalidExpirationTime",
                    e.getMessage(), "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        } catch (InvalidMessageException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "InvalidMessage",
                    e.getMessage(), "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
        if (subscription != null && subscription.getId() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Subscription request recieved  : " + subscription.getId());
            }
            String subID = getBrokerService().getSubscriptionManager().subscribe(subscription);
            if (subID != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending subscription response for Subscription ID : " +
                            subscription.getId());
                }
                SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
                dispatchResponse(soapEnvelope, CommandBuilderConstants.WSE_SUBSCRIBE_RESPONSE, mc, false);
            } else {
                log.debug("Subscription Failed, sending fault response");
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                        "Unable to subscribe ", "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
            }
        } else {
            log.debug("Subscription Failed, sending fault response");
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Unable to subscribe ", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleUnsubscribe(MessageContext mc) throws AxisFault, EventException {
        if (!isEnabled(mc, ENABLE_UNSUBSCRIBE)) {
            log.warn("Unsubscribe operation is disabled");
            return;
        }
        UnSubscribeCommandBuilder builder = new UnSubscribeCommandBuilder(mc);
        Subscription subscription = builder.toSubscription(mc.getEnvelope());
        if (mc.getTo() != null) {
            subscription.setAddressUrl(mc.getTo().getAddress());
        }
        if (log.isDebugEnabled()) {
            log.debug("UnSubscribe response recived for Subscription ID : " +
                    subscription.getId());
        }
        if (getBrokerService().getSubscriptionManager().unsubscribe(subscription.getId())) {
            if (log.isDebugEnabled()) {
                log.debug("Sending UnSubscribe responce for Subscription ID : " +
                        subscription.getId());
            }
            SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSE_UNSUBSCRIBE_RESPONSE, mc, false);
        } else {
            log.debug("UnSubscription failed, sending fault repsponse");
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Unable to Unsubscribe", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleGetStatus(MessageContext mc) throws AxisFault, EventException {
        if (!isEnabled(mc, ENABLE_GET_STATUS)) {
            log.warn("Get Status operation is disabled");
            return;
        }
        GetStatusCommandBuilder builder = new GetStatusCommandBuilder(mc);
        Subscription subscription = builder.toSubscription(mc.getEnvelope());
        if (mc.getTo() != null) {
            subscription.setAddressUrl(mc.getTo().getAddress());
        }
        if (log.isDebugEnabled()) {
            log.debug("GetStatus request recived for Subscription ID : " +
                    subscription.getId());
        }
        subscription = getBrokerService().getSubscriptionManager().getSubscription(subscription.getId());
        if (subscription != null) {
            if (log.isDebugEnabled()) {
                log.debug("Sending GetStatus responce for Subscription ID : " +
                        subscription.getId());
            }
            SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSE_GET_STATUS_RESPONSE, mc, false);
        } else {
            log.debug("GetStatus failed, sending fault response");
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Subscription Not Found", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleRenew(MessageContext mc) throws AxisFault, EventException {
        if (!isEnabled(mc, ENABLE_RENEW)) {
            log.warn("Renew operation is disabled");
            return;
        }
        RenewCommandBuilder builder = new RenewCommandBuilder(mc);
        Subscription subscription = null;
        try {
            subscription = builder.toSubscription(mc.getEnvelope());
            if (mc.getTo() != null) {
                subscription.setAddressUrl(mc.getTo().getAddress());
            }
        } catch (InvalidExpirationTimeException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "InvalidExpirationTime",
                    e.getMessage(), "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }

        if (subscription != null && subscription.getId() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Renew request recived for Subscription ID : " +
                        subscription.getId());
            }
            if (getBrokerService().getSubscriptionManager().renew(subscription)) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending Renew response for Subscription ID : " +
                            subscription.getId());
                }
                SOAPEnvelope soapEnvelope =
                        builder.fromSubscription(subscription);
                dispatchResponse(soapEnvelope, CommandBuilderConstants.WSE_RENEW_RESPONSE, mc, false);
            } else {
                log.debug("Renew failed, sending fault response");
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "UnableToRenew",
                        "Subscription Not Found", "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
            }
        } else {
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    CommandBuilderConstants.WSE_FAULT_CODE_RECEIVER, "UnableToRenew",
                    "Subscription Not Found", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, CommandBuilderConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleEvent(MessageContext mc) throws AxisFault, EventException {
        log.debug("Received Event");
        Event<MessageContext> emc = new Event<MessageContext>();
        emc.setMessage(mc);
        getBrokerService().getNotificationManager().publishEvent(emc);
    }

    public final void processMessage(MessageContext mc) throws AxisFault, EventException {
        if (CommandBuilderConstants.WSE_SUBSCRIBE.equals(mc.getWSAAction())) {
            handleSubscribe(mc);
        } else if (CommandBuilderConstants.WSE_UNSUBSCRIBE.equals(mc.getWSAAction())) {
            handleUnsubscribe(mc);
        } else if (CommandBuilderConstants.WSE_GET_STATUS.equals(mc.getWSAAction())) {
            handleGetStatus(mc);
        } else if (CommandBuilderConstants.WSE_RENEW.equals(mc.getWSAAction())) {
            handleRenew(mc);
        } else {
            handleEvent(mc);    
        }
    }

    /**
     * Dispatch the message to the target endpoint
     *
     * @param soapEnvelope   Soap Enevlop with message
     * @param responseAction WSE action for the response
     * @param mc             Message Context
     * @param isFault        Whether a Fault message must be sent
     * @throws AxisFault Thrown by the axis2 engine.
     */
    private void dispatchResponse(SOAPEnvelope soapEnvelope, String responseAction,
                                  MessageContext mc, boolean isFault) throws AxisFault {
        MessageContext rmc = MessageContextBuilder.createOutMessageContext(mc);
        rmc.getOperationContext().addMessageContext(rmc);
        replicateState(mc);
        rmc.setEnvelope(soapEnvelope);
        rmc.setWSAAction(responseAction);
        rmc.setSoapAction(responseAction);
        if (isFault) {
            AxisEngine.sendFault(rmc);
        } else {
            AxisEngine.send(rmc);
        }
    }

    private EventBrokerService getBrokerService() {
        if (brokerService == null) {
            brokerService = CarbonEventBroker.getInstance();
        }
        return brokerService;
    }

    public void setBrokerService(EventBrokerService brokerService) {
        this.brokerService = brokerService;
    }
}
