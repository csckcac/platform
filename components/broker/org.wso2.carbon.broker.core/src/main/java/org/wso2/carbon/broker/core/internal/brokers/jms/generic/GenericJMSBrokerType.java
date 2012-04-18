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
package org.wso2.carbon.broker.core.internal.brokers.jms.generic;

import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.internal.brokers.jms.JMSBrokerType;
import org.wso2.carbon.broker.core.internal.brokers.jms.SubscriptionDetails;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class GenericJMSBrokerType extends JMSBrokerType {

    private static GenericJMSBrokerType instance = new GenericJMSBrokerType();

    public GenericJMSBrokerType() {
        setBrokerTypeDto(new BrokerTypeDto());
        getBrokerTypeDto().setName(BrokerConstants.BROKER_TYPE_JMS_GENERIC);

        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.broker.core.i18n.Resources", Locale.getDefault());

        // set initial factory as a property
        Property factoryInitialProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_JNDI_NAME);
        factoryInitialProperty.setRequired(true);
        factoryInitialProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_JNDI_NAME));
        getBrokerTypeDto().addProperty(factoryInitialProperty);

        // set connection user name as property
        Property userNameProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_USER_NAME);
        userNameProperty.setRequired(false);
        userNameProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_USER_NAME));
        getBrokerTypeDto().addProperty(userNameProperty);

        // set connection password as property
        Property passwordProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_PASSWORD);
        passwordProperty.setRequired(false);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_PASSWORD));
        getBrokerTypeDto().addProperty(passwordProperty);

        // set provider url of broker
        Property ipProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_PROVIDER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_PROVIDER_URL));
        ipProperty.setRequired(true);
        getBrokerTypeDto().addProperty(ipProperty);

//        // set virtual host name as property
//        Property virtualHostNameProperty =
//                new Property(BrokerConstants.BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME);
//        virtualHostNameProperty.setRequired(true);
//        virtualHostNameProperty.setDisplayName(
//                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME));
//        this.brokerTypeDto.addProperty(virtualHostNameProperty);

        setBrokerSubscriptionsMap(new ConcurrentHashMap<String, Map<String, SubscriptionDetails>>());
    }

    public static JMSBrokerType getInstance() {
        return instance;
    }

    /**
     * Create Connection factory with initial context
     *
     * @param brokerConfiguration broker - configuration details to create a broker
     * @return Topic connection
     * @throws org.wso2.carbon.broker.core.exception.BrokerEventProcessingException
     *          - jndi look up failed
     */
    protected TopicConnection getTopicConnection(BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {
        try {

            // get configuration details from brokerConfiguration provided
            Map<String, String> properties = brokerConfiguration.getProperties();
            String providerUrl = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_PROVIDER_URL);
            String factoryInitial = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_JNDI_NAME);

            Properties property = new Properties();
            property.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryInitial);
            property.setProperty(Context.PROVIDER_URL, providerUrl);
            Context context = new InitialContext(property);

            TopicConnectionFactory topicConnectionFactory
                    = (TopicConnectionFactory) context.lookup(BrokerConstants.BROKER_CONF_JMS_PROP_CONNECTION_FACTORY_LOOK_UP_NAME);

            String userName = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_USER_NAME);
            String password = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_PASSWORD);
            return topicConnectionFactory.createTopicConnection(userName, password);
        } catch (NamingException e) {
            throw new BrokerEventProcessingException("Can not create initial context with given parameters.", e);

        } catch (JMSException e) {
            throw new BrokerEventProcessingException("Can not create topic connection .", e);
        }
    }

}
