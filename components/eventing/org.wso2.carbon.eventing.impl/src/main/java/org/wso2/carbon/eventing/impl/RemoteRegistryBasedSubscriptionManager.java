/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.eventing.impl;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.Event;
import org.wso2.eventing.exceptions.EventException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
public class RemoteRegistryBasedSubscriptionManager
        extends AbstractRegistryBasedSubscriptionManager<MessageContext> {

    private String registryURL;
    private String username;
    private String password;
    private RemoteRegistry registry;    
    private static final Log log = LogFactory.getLog(RemoteRegistryBasedSubscriptionManager.class);

    protected AXIOMXPath topicXPath;
    protected String topicHeaderName;
    protected String topicHeaderNS;

    /**
     * Initlise the subscription manager using secured connection.
     * Need folowing enviornment varibales set before.
     * System.setProperty("javax.net.ssl.trustStore", "../resources/security/client-truststore.jks");
     * System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
     * System.setProperty("javax.net.ssl.trustStoreType", "JKS");
     */

    public void init() {
        try {
            topicHeaderName = getPropertyValue("topicHeaderName");
            if(topicHeaderName==null){
                handleException("Unable to create topic header topic header name is null");
            }
            topicHeaderNS = getPropertyValue("topicHeaderNS");
            if(topicHeaderNS==null){
                handleException("Unable to create topic header topic header namespace is null");
            }
            registryURL = getPropertyValue("registryURL");
            if(registryURL==null){
                handleException("Registry URL is null");
            }
            username = getPropertyValue("username");
            if(username==null){
                handleException("Registry username is null");
            }
            password = getPropertyValue("password");
            if(password==null){
                handleException("Registry password is null");
            }

            log.info("Connecting to the remote registry " + registryURL);
            registry = new RemoteRegistry(new URL(registryURL), username, password);
            super.registry=registry;
            if (registry == null) {
                log.fatal("Unable to connect to the remote registry at : " + registryURL);
            } else {
                resTopicIndex = registry.get(getTopicIndexPath()); // call the topic index
            }
        } catch (ResourceNotFoundException resE) {
            // create the topic index
            try {
                resTopicIndex = registry.newResource();
                registry.put(getTopicIndexPath(), resTopicIndex);
            } catch (RegistryException e) {
                handleException("Unable to add the TopicIndex", e);
            }
        } catch (MalformedURLException e) {
            handleException("Unable to connect to the remote registry at : " + registryURL, e);
        } catch (Exception e) {
            handleException("Unable to connect to the remote registry at : " + registryURL, e);
        }
        log.info("Connection established with the remote " +
                "registry from the passed values " + registryURL + " " + username + " " + password);
        try {
            topicXPath = new AXIOMXPath(
                    "s11:Header/ns:" + topicHeaderName + " | s12:Header/ns:" + topicHeaderName);
            topicXPath.addNamespace("s11", "http://schemas.xmlsoap.org/soap/envelope/");
            topicXPath.addNamespace("s12", "http://www.w3.org/2003/05/soap-envelope");
            topicXPath.addNamespace("ns", topicHeaderNS);
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


    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    private void handleException(String message) {
        log.error(message);
        throw new RuntimeException(message);
    }

}