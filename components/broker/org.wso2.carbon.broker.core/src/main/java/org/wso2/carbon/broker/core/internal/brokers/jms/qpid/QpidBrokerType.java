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
package org.wso2.carbon.broker.core.internal.brokers.jms.qpid;

import org.wso2.carbon.CarbonConstants;
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

@Deprecated
/**
 *   Because Qpid is not shipped with CEP by default
 */
public class QpidBrokerType extends JMSBrokerType {

    private static QpidBrokerType instance = new QpidBrokerType();

    public QpidBrokerType() {
        setBrokerTypeDto(new BrokerTypeDto());
        getBrokerTypeDto().setName(BrokerConstants.BROKER_TYPE_JMS_QPID);

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
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_USER_NAME));
        getBrokerTypeDto().addProperty(userNameProperty);

        // set connection password as property
        Property passwordProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_PASSWORD));
        getBrokerTypeDto().addProperty(passwordProperty);

        // set ip of broker
        Property ipProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_IP_ADDRESS);
        ipProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_IP_ADDRESS));
        ipProperty.setRequired(true);
        getBrokerTypeDto().addProperty(ipProperty);

        // set broker port listening
        Property portProperty = new Property(BrokerConstants.BROKER_CONF_JMS_PROP_PORT);
        portProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_PORT));
        portProperty.setRequired(true);
        getBrokerTypeDto().addProperty(portProperty);

        // set virtual host name as property
        Property virtualHostNameProperty =
                new Property(BrokerConstants.BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME);
        virtualHostNameProperty.setRequired(true);
        virtualHostNameProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME));
        getBrokerTypeDto().addProperty(virtualHostNameProperty);

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
            String factoryInitial = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_JNDI_NAME);
            String ipAddress = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_IP_ADDRESS);
            String port = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_PORT);
            String virtualHostName = properties.get(BrokerConstants.BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME);

            // create qpid connection factory lookup name
            String qpidConnectionFactoryName = BrokerConstants.BROKER_CONF_JMS_PROP_JNDI_OBJECT_NAME_PREfIX +
                                               BrokerConstants.BROKER_CONF_JMS_PROP_CONNECTION_FACTORY_LOOK_UP_NAME;

            Properties property = new Properties();
            property.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryInitial);

            // create connection url
            property.put(qpidConnectionFactoryName, createQpidConnectionUrl(virtualHostName, ipAddress, port));

            property.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");

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

    /**
     * Get the amqp provider url with given parameters
     *
     * @param virtualHostName - qpid configuration details as in config.xml in qpid, eg: test, development
     * @param ipAddress       - broker running machines ip address
     * @param port            - qpid port listening
     * @return connection url as string
     */
    private String createQpidConnectionUrl(String virtualHostName,
                                           String ipAddress,
                                           String port) {
        StringBuffer buffer = new StringBuffer("amqp://");
        buffer.append(":")
                .append("@")
                .append("clientId")
                .append("/")
                .append(virtualHostName)
                .append("?brokerlist='tcp://")
                .append(ipAddress)
                .append(":")
                .append(port)
                .append("'");
        return buffer.toString();
    }

}
