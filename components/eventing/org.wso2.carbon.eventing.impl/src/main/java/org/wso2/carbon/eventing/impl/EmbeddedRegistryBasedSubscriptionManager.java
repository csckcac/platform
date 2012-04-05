/*
*  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
package org.wso2.carbon.eventing.impl;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.jaxen.JaxenException;
import org.wso2.carbon.eventing.impl.internal.EventingServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.Event;
import org.wso2.eventing.exceptions.EventException;

import java.util.List;

public class EmbeddedRegistryBasedSubscriptionManager
        extends AbstractRegistryBasedSubscriptionManager<MessageContext> {

    private static final Log log =
            LogFactory.getLog(EmbeddedRegistryBasedSubscriptionManager.class);

    protected AXIOMXPath topicXPath;
    protected String topicHeaderName;
    protected String topicHeaderNS;

    /**
     * {@inheritDoc}
     */
    public void init() {
        try {
            registry = EventingServiceComponent.getSystemRegistry();
            super.registry = registry;
        } catch (RegistryException e) {
            handleException("Cannot connect to the embedded registry", e);
        }

        try {
            if (registry == null) {
                log.fatal("Unable to create the registry instance ");
            } else {
                resTopicIndex = registry.get(getTopicIndexPath()); // call the topic index
            }
        } catch (ResourceNotFoundException resE) {
            try {
                resTopicIndex = registry.newResource();
                registry.put(getTopicIndexPath(), resTopicIndex);
            } catch (RegistryException e) {
                handleException("Unable to add the TopicIndex", e);
            }
        } catch (RegistryException regE) {
            handleException("Registry error", regE);
        }
        log.info("Connection established with the registry");
        try {
            log.debug("Creating XPath with Topic Header Name: '" + getTopicHeaderName() +
                      "' and Topic Header NS: '" + getTopicHeaderNS() + "'");
            topicXPath = new AXIOMXPath(
                    "s11:Header/ns:" + getTopicHeaderName() + " | s12:Header/ns:" + getTopicHeaderName());
            topicXPath.addNamespace("s11", "http://schemas.xmlsoap.org/soap/envelope/");
            topicXPath.addNamespace("s12", "http://www.w3.org/2003/05/soap-envelope");
            topicXPath.addNamespace("ns", getTopicHeaderNS());
        } catch (JaxenException e) {
            handleException("Unable to create the topic header XPath", e);
        }
    }

    public List<Subscription> getMatchingSubscriptions(Event<MessageContext> event)
            throws EventException {
        MessageContext mc = event.getMessage();
        log.debug("Got SOAP Envelope: " + mc.getEnvelope());
        String topic = null;
        try {
            OMElement topicNode = (OMElement) topicXPath.selectSingleNode(mc.getEnvelope());
            if (topicNode != null) {
                topic = topicNode.getText();
            }
        } catch (JaxenException e) {
            log.error("Topic not found");
            throw new EventException("Topic not found", e);
        }
        event.setTopic(topic);
        return super.getMatchingSubscriptions(event);
    }

    public String getTopicHeaderName() {
        if (topicHeaderName == null) {
            topicHeaderName = getPropertyValue("topicHeaderName");
        }
        return topicHeaderName;
    }

    public void setTopicHeaderName(String topicHeaderName) {
        this.topicHeaderName = topicHeaderName;
    }

    public String getTopicHeaderNS() {
        if (topicHeaderNS == null) {
            topicHeaderNS = getPropertyValue("topicHeaderNS");    
        }
        return topicHeaderNS;
    }

    public void setTopicHeaderNS(String topicHeaderNS) {
        this.topicHeaderNS = topicHeaderNS;
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }
}
