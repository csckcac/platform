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
package org.wso2.carbon.eventing.broker.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.eventing.broker.CarbonEventBroker;
import org.wso2.carbon.eventing.broker.CarbonNotificationManager;
import org.wso2.carbon.eventing.broker.CarbonEventDispatcher;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;
import org.wso2.carbon.eventing.broker.exceptions.ActivationException;
import org.wso2.carbon.eventing.impl.internal.EventingServiceComponent;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.NotificationManager;
import org.wso2.eventing.EventDispatcher;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

/**
 * @scr.component name="org.wso2.carbon.eventing.broker" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventing.component.service"
 * interface="org.wso2.carbon.eventing.impl.internal.EventingServiceComponent"
 * cardinality="1..1" policy="dynamic" bind="setEventingServiceComponent"
 * unbind="unsetEventingServiceComponent"
 */
public class EventBrokerServiceComponent {

    private static Log log = LogFactory.getLog(EventBrokerServiceComponent.class);

    private boolean configurationDone = false;

    private ConfigurationContextService configurationContextService = null;

    private ConfigurationContext serverConfigurationContext = null;

    private Dictionary dictionary = null;

    private Stack<ServiceRegistration> eventingServiceRegistrations = null;

    private BundleContext bundleContext = null;

    private static final String BROKER_CONFIG_FILE = "broker-config.xml";

    private static final String LOCAL_NAME_EVENT_STREAM_CONFIG = "eventStream";

    private static final String LOCAL_NAME_SUBSCRIPTION_MANAGER_CONFIG = "subscriptionManager";

    private static final String LOCAL_NAME_NOTIFICATION_MANAGER_CONFIG = "notificationManager";

    private static final String LOCAL_NAME_EVENT_DISPATCHER_CONFIG = "eventDispatcher";

    private static final String LOCAL_NAME_PARAMETER = "parameter";

    private static final String ATTR_NAME = "name";

    private static final String ATTR_CLASS = "class";

    private static final String DEFAULT_EVENT_SOURCE_NAME = "default";

    private static final String BROKER_CONFIG_NAMESPACE =
            "http://wso2.org/ns/2009/09/eventing";

    protected void activate(ComponentContext context) {
        serverConfigurationContext = configurationContextService.getServerConfigContext();
        bundleContext = context.getBundleContext();
        if (!configurationDone && serverConfigurationContext != null) {
            registerEventBrokerServices();
            dictionary = new Hashtable();
            processConfiguration();
            configurationDone = true;
        }
        log.debug("Event Broker bundle is activated ");
    }

    private void registerEventBrokerServices() {
        if (eventingServiceRegistrations == null && bundleContext != null) {
            eventingServiceRegistrations = new Stack<ServiceRegistration>();
            log.debug("Successfully setup the Event Broker OGSi Service");
        }
    }

    private void unregisterEventingService() {
        if (eventingServiceRegistrations != null) {
            while (!eventingServiceRegistrations.empty()) {
                eventingServiceRegistrations.pop().unregister();
            }
            eventingServiceRegistrations = null;
            log.debug("Successfully unregistered the Event Broker OGSi Service");
        }
        configurationDone = false;
    }

    @SuppressWarnings("unchecked")
    private void processConfiguration() {
        try {
            OMElement configElement = getConfigElement();
            Iterator<OMElement> configIterator =
                    configElement.getChildrenWithName(new QName(
                            BROKER_CONFIG_NAMESPACE, LOCAL_NAME_EVENT_STREAM_CONFIG));
            while(configIterator.hasNext()) {
                OMElement element = configIterator.next();
                EventBrokerService service = null;
                try {
                    service = buildEventSource(element);
                } catch (ClassNotFoundException ignore) {
                    log.error("Unable to build Event Source.");
                    service = null;
                }
                if (service != null) {
                    eventingServiceRegistrations.push(
                            bundleContext.registerService(EventBrokerService.class.getName(),
                                    service, dictionary));
                }
            }
        } catch (Exception e) {
            String message = "Error while building broker configuration from file";
            log.error(message, e);
            throw new ActivationException(message, e);
        }
    }

    private Map<String, String> getParameters(Iterator<OMElement> parameterElements) {
        Map<String, String> parameters = new HashMap<String, String>();
        while(parameterElements.hasNext()) {
            OMElement parameterElement = parameterElements.next();
            String name = parameterElement.getAttribute(
                    new QName(ATTR_NAME)).getAttributeValue();
            if (name != null) {
                parameters.put(name, parameterElement.getText().trim());
            }
        }
        return parameters;
    }

    @SuppressWarnings("unchecked")
    private EventBrokerService buildEventSource(OMElement configElement)
            throws ClassNotFoundException {
        SubscriptionManager subscriptionManager;
        NotificationManager notificationManager;
        EventDispatcher eventDispatcher;

        String eventSourceName = null;
        OMAttribute eventSourceNameAttribute = configElement.getAttribute(
                new QName(ATTR_NAME)) ;
        if (eventSourceNameAttribute != null) {
            eventSourceName = eventSourceNameAttribute.getAttributeValue();
        }

        OMElement subscriptionManagerElement = configElement.getFirstChildWithName(new QName(
                BROKER_CONFIG_NAMESPACE, LOCAL_NAME_SUBSCRIPTION_MANAGER_CONFIG));

        String subscriptionManagerClass = subscriptionManagerElement.getAttribute(
                new QName(ATTR_CLASS)).getAttributeValue();

        try {
            subscriptionManager =
                    (SubscriptionManager) Class.forName(subscriptionManagerClass).newInstance();
        } catch(Exception e) {
            String message = "Error while creating Subscription Manager";
            log.error(message, e);
            if (e instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)e;
            }
            throw new ActivationException(message, e);
        }

        Map<String, String> subscriptionManagerParameters = getParameters(
                subscriptionManagerElement.getChildrenWithName(new QName(
                        BROKER_CONFIG_NAMESPACE, LOCAL_NAME_PARAMETER)));
        Set<Map.Entry<String, String>> parameters = subscriptionManagerParameters.entrySet();
        for(Map.Entry<String, String> e : parameters) {
            subscriptionManager.addProperty(e.getKey(), e.getValue());
        }

        OMElement notificationManagerElement = configElement.getFirstChildWithName(new QName(
                BROKER_CONFIG_NAMESPACE, LOCAL_NAME_NOTIFICATION_MANAGER_CONFIG));

        String notificationManagerClass = notificationManagerElement.getAttribute(
                new QName(ATTR_CLASS)).getAttributeValue();

        try {
            notificationManager =
                    (NotificationManager) Class.forName(notificationManagerClass).newInstance();
            if (notificationManager instanceof CarbonNotificationManager) {
                Map<String, String> notificationManagerParameters = getParameters(
                        notificationManagerElement.getChildrenWithName(new QName(
                                BROKER_CONFIG_NAMESPACE, LOCAL_NAME_PARAMETER)));
                ((CarbonNotificationManager) notificationManager).init(
                        notificationManagerParameters);
            }
        } catch(Exception e) {
            String message = "Error while creating Notification Manager";
            log.error(message, e);
            if (e instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)e;
            }
            throw new ActivationException(message, e);
        }

        String eventDispatcherName = configElement.getFirstChildWithName(new QName(
                BROKER_CONFIG_NAMESPACE, LOCAL_NAME_EVENT_DISPATCHER_CONFIG)).getText().trim();

        try {
            eventDispatcher = (EventDispatcher) Class.forName(eventDispatcherName).newInstance();
            if (eventDispatcher instanceof CarbonEventDispatcher) {
                ((CarbonEventDispatcher)eventDispatcher).init(serverConfigurationContext);
            }
        } catch(Exception e) {
            String message = "Error while creating Event Dispatcher";
            log.error(message, e);
            if (e instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)e;
            }
            throw new ActivationException(message, e);
        }

        try {
            notificationManager.registerEventDispatcher(eventDispatcher);
            CarbonEventBroker broker;
            if (eventSourceName != null) {
                broker = (CarbonEventBroker)CarbonEventBroker.getInstance(eventSourceName);
            } else {
                broker = (CarbonEventBroker)CarbonEventBroker.getInstance();
            }
            broker.registerSubscriptionManager(subscriptionManager);
            broker.registerNotificationManager(notificationManager);
            subscriptionManager.init();
            if (eventSourceName != null) {
                dictionary.put(ATTR_NAME, eventSourceName);
            } else {
                dictionary.put(ATTR_NAME, DEFAULT_EVENT_SOURCE_NAME);
            }
            return broker;
        } catch(Exception e) {
            String message = "Error while initializing Event Source";
            log.error(message, e);
            throw new ActivationException(message, e);
        }
    }

    private OMElement getConfigElement() throws XMLStreamException, IOException {
        InputStream inStream = null;

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);

        if (carbonHome != null) {
            File profileConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(),
                    BROKER_CONFIG_FILE);
            if (profileConfigXml.exists()) {
                inStream = new FileInputStream(profileConfigXml);
            }
        } else {
            inStream = EventBrokerServiceComponent.class.getResourceAsStream(BROKER_CONFIG_FILE);
        }

        if (inStream == null) {
            String message = "Broker configuration not found.";
            log.error(message);
            throw new FileNotFoundException(message);
        }

        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        inStream.close();
        return documentElement;
    }

    protected void deactivate(ComponentContext context) {
        unregisterEventingService();
        log.debug("Event Broker bundle is deactivated ");
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        this.configurationContextService  = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
    }

    protected void setRegistryService(RegistryService registryService) {
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    protected void setEventingServiceComponent(EventingServiceComponent eventingServiceComponent) {
    }

    protected void unsetEventingServiceComponent(EventingServiceComponent eventingServiceComponent) {
    }
}
