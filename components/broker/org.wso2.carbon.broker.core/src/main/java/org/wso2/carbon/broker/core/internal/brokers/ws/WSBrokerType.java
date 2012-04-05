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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.internal.BrokerType;
import org.wso2.carbon.broker.core.internal.ds.BrokerServiceValueHolder;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;
import org.wso2.carbon.broker.core.internal.util.Axis2Util;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.RemoteException;

public class WSBrokerType implements BrokerType {

    private static final Log log = LogFactory.getLog(WSBrokerType.class);

    private static WSBrokerType instance = new WSBrokerType();

    private BrokerTypeDto brokerTypeDto = null;

    private Map<String, Map<String, String>> brokerSubscriptionsMap;

    private WSBrokerType() {

        this.brokerTypeDto = new BrokerTypeDto();
        this.brokerTypeDto.setName(BrokerConstants.BROKER_TYPE_WS_EVENT);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.broker.core.i18n.Resources", Locale.getDefault());

        Property uriProperty = new Property(BrokerConstants.BROKER_CONF_WS_PROP_URI);
        uriProperty.setRequired(true);
        uriProperty.setDisplayName(resourceBundle.getString(BrokerConstants.BROKER_CONF_WS_PROP_URI));
        this.brokerTypeDto.addProperty(uriProperty);

        Property userNameProperty = new Property(BrokerConstants.BROKER_CONF_WS_PROP_USERNAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(resourceBundle.getString(BrokerConstants.BROKER_CONF_WS_PROP_USERNAME));
        this.brokerTypeDto.addProperty(userNameProperty);

        Property passwordProperty = new Property(BrokerConstants.BROKER_CONF_WS_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(resourceBundle.getString(BrokerConstants.BROKER_CONF_WS_PROP_PASSWORD));
        this.brokerTypeDto.addProperty(passwordProperty);

        this.brokerSubscriptionsMap = new ConcurrentHashMap<String, Map<String, String>>();

    }

    public static WSBrokerType getInstance() {
        return instance;
    }

    public BrokerTypeDto getBrokerTypeDto() {
        return this.brokerTypeDto;
    }

    public void subscribe(String topicName,
                          BrokerListener brokerListener,
                          BrokerConfiguration brokerConfiguration,
                          AxisConfiguration axisConfiguration)
            throws BrokerEventProcessingException {


        try {

            AxisService axisService =
                    Axis2Util.registerAxis2Service(topicName, brokerListener, brokerConfiguration, axisConfiguration);

            String httpEpr = null;
            for (String epr : axisService.getEPRs()) {
                if (epr.startsWith("http")) {
                    httpEpr = epr;
                    break;
                }
            }

            if (!httpEpr.endsWith("/")) {
                httpEpr += "/";
            }

            httpEpr += topicName.replaceAll("/","");

            Map<String, String> properties = brokerConfiguration.getProperties();
            BrokerClient brokerClient =
                    new BrokerClient(properties.get(BrokerConstants.BROKER_CONF_WS_PROP_URI),
                            properties.get(BrokerConstants.BROKER_CONF_WS_PROP_USERNAME),
                            properties.get(BrokerConstants.BROKER_CONF_WS_PROP_PASSWORD));
            brokerClient.subscribe(topicName, httpEpr);

            String subscriptionID = brokerClient.subscribe(topicName, httpEpr);

            // keep the subscription id to unsubscribe
            Map<String, String> topicSubscriptionsMap =
                    this.brokerSubscriptionsMap.get(brokerConfiguration.getName());
            if (topicSubscriptionsMap == null) {
                topicSubscriptionsMap = new ConcurrentHashMap<String, String>();
                this.brokerSubscriptionsMap.put(brokerConfiguration.getName(), topicSubscriptionsMap);
            }

            topicSubscriptionsMap.put(topicName, subscriptionID);
            
        } catch (BrokerClientException e) {
            throw new BrokerEventProcessingException("Can not create the broker client", e);
        } catch (AuthenticationExceptionException e) {
            throw new BrokerEventProcessingException("Can not authenticate the broker client", e);
        } catch (AxisFault axisFault) {
            throw new BrokerEventProcessingException("Can not subscribe", axisFault);
        }
    }

    public void publish(String topicName,
                        Object message,
                        BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {

        try {
            Map<String, String> properties = brokerConfiguration.getProperties();
            ConfigurationContextService configurationContextService =
                BrokerServiceValueHolder.getConfigurationContextService();
            BrokerClient brokerClient =
                    new BrokerClient(configurationContextService.getClientConfigContext(),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_URI),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_USERNAME),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_PASSWORD));
            brokerClient.publish(topicName, ((OMElement)message));
        } catch (AuthenticationExceptionException e) {
            throw new BrokerEventProcessingException("Can not authenticate the broker client", e);
        } catch (AxisFault axisFault) {
            throw new BrokerEventProcessingException("Can not subscribe", axisFault);
        }

    }

    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration) throws BrokerEventProcessingException {
        try {
            Axis2Util.removeOperation(topicName, brokerConfiguration, axisConfiguration);
        } catch (AxisFault axisFault) {
            throw new BrokerEventProcessingException("Can not unsubscribe from the broker", axisFault);
        }

        Map<String, String> topicSubscriptionsMap =
                    this.brokerSubscriptionsMap.get(brokerConfiguration.getName());
        if (topicSubscriptionsMap == null){
            throw new BrokerEventProcessingException("There is no subscription for broker "
                    + brokerConfiguration.getName());
        }

        String subscriptionID = topicSubscriptionsMap.remove(topicName);
        if (subscriptionID == null){
            throw new BrokerEventProcessingException("There is no subscriptions for this topic" + topicName);
        }

        try {
            Map<String, String> properties = brokerConfiguration.getProperties();
            ConfigurationContextService configurationContextService =
                BrokerServiceValueHolder.getConfigurationContextService();
            BrokerClient brokerClient =
                    new BrokerClient(configurationContextService.getClientConfigContext(),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_URI),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_USERNAME),
                                     properties.get(BrokerConstants.BROKER_CONF_WS_PROP_PASSWORD));
            brokerClient.unsubscribe(subscriptionID);
        } catch (AuthenticationExceptionException e) {
            throw new BrokerEventProcessingException("Can not authenticate the broker client", e);
        } catch (RemoteException e) {
            throw new BrokerEventProcessingException("Can not connect to the server", e);
        }

    }
}
