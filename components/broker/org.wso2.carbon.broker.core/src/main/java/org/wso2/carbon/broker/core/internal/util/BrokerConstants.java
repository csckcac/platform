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

package org.wso2.carbon.broker.core.internal.util;

public interface BrokerConstants {

    String BROKER_CONF = "broker.xml";

    String BROKER_CONF_NS = "http://wso2.org/carbon/broker";
    String BROKER_CONF_ELE_ROOT = "brokerTypes";

    String BROKER_CONF_ELE_BROKER_TYPE = "brokerType";
    String BROKER_CONF_ELE_PROPERTY = "property";
    String BROKER_CONF_ELE_BROKER = "broker";

    String BROKER_CONF_ATTR_NAME = "name";
    String BROKER_CONF_ATTR_CLASS = "class";
    String BROKER_CONF_ATTR_TYPE = "type";

    String BROKER_WS_SERVICE_NAME = "WSBrokerService";

    String BROKER_CONF_WS_PROP_URI = "uri";
    String BROKER_CONF_WS_PROP_USERNAME = "username";
    String BROKER_CONF_WS_PROP_PASSWORD = "password";
    String BROKER_CONF_WS_PROP_AUTO_SUBSCRIBE = "autoSubscribe";

    String BROKER_CONF_JMS_PROP_JNDI_NAME = "jndiName";
    String BROKER_CONF_JMS_PROP_SERVER_NAME = "serverName";
    String BROKER_CONF_JMS_PROP_VIRTURAL_HOST_NAME = "virtualHostName";
    String BROKER_CONF_JMS_PROP_IP_ADDRESS = "ipAddress";
    String BROKER_CONF_JMS_PROP_PORT = "port";
    String BROKER_CONF_JMS_PROP_USER_NAME = "username";
    String BROKER_CONF_JMS_PROP_PASSWORD = "password";
    String BROKER_CONF_JMS_PROP_PROVIDER_URL="providerUrl";

    String BROKER_CONF_AGENT_PROP_RECEIVER_URL = "receiverURL";
    String BROKER_CONF_AGENT_PROP_AUTHENTICATOR_URL = "authenticatorURL";
    String BROKER_CONF_AGENT_PROP_USER_NAME = "username";
    String BROKER_CONF_AGENT_PROP_PASSWORD = "password";

    String BROKER_CONF_JMS_PROP_CONNECTION_FACTORY_LOOK_UP_NAME = "ConnectionFactory";
    String BROKER_CONF_JMS_PROP_JNDI_OBJECT_NAME_PREfIX = "connectionfactory.";

    String BROKER_TYPE_LOCAL = "local";
    String BROKER_TYPE_WS_EVENT = "ws-event";
    String BROKER_TYPE_JMS_QPID = "jms-qpid";
    String BROKER_TYPE_JMS_GENERIC = "jms";
    String BROKER_TYPE_AGENT = "agent";
}
