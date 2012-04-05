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
package org.wso2.carbon.eventing.broker;

import org.wso2.eventing.*;
import org.wso2.eventing.exceptions.EventException;
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class CarbonEventBroker implements EventBrokerService {

    protected SubscriptionManager subManager = null;

    protected NotificationManager notifyManager = null;

    private CarbonEventSource eventSource = null;

    private static EventBrokerService instance = null;

    protected String subscriptionManagerUrl = null;

    private static Map<String, EventBrokerService> namedInstances =
            new HashMap<String, EventBrokerService>();

    private static final Log log = LogFactory.getLog(CarbonEventBroker.class);

    protected CarbonEventBroker() { }

    public boolean registerSubscriptionManager(SubscriptionManager subManager)
            throws EventBrokerException {
        this.subManager = subManager;
        return eventSource.registerSubscriptionManager(subManager);
    }

    public SubscriptionManager getSubscriptionManager() {
        return subManager;
    }

    public boolean registerNotificationManager(NotificationManager notifyManager)
            throws EventBrokerException {
        this.notifyManager = notifyManager;
        if (notifyManager != null && notifyManager instanceof CarbonNotificationManager) {
            ((CarbonNotificationManager)notifyManager).setEventBroker(this);
        }
        return eventSource.registerNotificationManager(notifyManager);
    }

    public NotificationManager getNotificationManager() {
        return notifyManager;
    }

    public void init() throws EventBrokerException {
        this.eventSource = new CarbonEventSource();
        eventSource.init();
    }

    public void publishEvent(Event event) throws EventBrokerException {
        try {
            notifyManager.publishEvent(event);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Unable to publish event.", e);
                throw new EventBrokerException("Unable to publish event.", e);
            }
        }
    }

    public void registerEventDispatcher(EventDispatcher eventDispatcher)
            throws EventBrokerException {
        try {
            notifyManager.registerEventDispatcher(eventDispatcher);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Unable to register event dispatcher.", e);
                throw new EventBrokerException("Unable to register event dispatcher.", e);
            }
        }
    }

    public static EventBrokerService getInstance() {
        if (instance == null) {
            instance = new CarbonEventBroker();
            try {
                instance.init();
            } catch (EventException e) {
                log.error("Event Broker Initialization Failed", e);
            }
        }
        return instance;
    }

    /**
     * Associates a named event broker service with this event broker.
     * @param name The named of the instance
     * @param service The service object
     */
    public static void setInstance(String name, EventBrokerService service) {
        if (name != null && service != null) {
            namedInstances.put(name, service);
        } else {
            log.error("Unable to register Event Broker Service");
        }
    }

    /**
     * Obtains named instance or creates one if the instance doesn't exist.
     * @param name The named of the instance
     * @return The named event broker
     */
    public static EventBrokerService getInstance(String name) {
        if (name == null) {
            return getInstance();
        }
        EventBrokerService namedInstance = namedInstances.get(name);
        if (namedInstance != null) {
            return namedInstance;
        }
        namedInstance = new CarbonEventBroker();
        try {
            namedInstance.init();
        } catch (EventException e) {
            log.error("Event Broker Initialization Failed", e);
        }
        setInstance(name, namedInstance);
        return namedInstance;
    }

    /**
     * Method to add subscription to the store
     * @param subscription to be added to the store
     * @return subscription ID
     * @throws EventBrokerException if the operation fails
     */
    public String subscribe(Subscription subscription) throws EventBrokerException {
        if (subManager == null) {
            throw new EventBrokerException("Subscription Manager not Registered");
        }
        try {
            return subManager.subscribe(subscription);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Subscribe operation failed.", e);
                throw new EventBrokerException("Subscribe operation failed.", e);
            }
        }
    }

    /**
     * Method to remove a subscription from the store
     * @param subscriptionID the subscription identifier
     * @return true|false based on the transaction
     * @throws EventBrokerException if the operation fails
     */
    public boolean unsubscribe(String subscriptionID) throws EventException {
        if (subManager == null) {
            throw new EventBrokerException("Subscription Manager not Registered");
        }
        try {
            return subManager.unsubscribe(subscriptionID);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Unsubscribe operation failed.", e);
                throw new EventBrokerException("Unsubscribe operation failed.", e);
            }
        }
    }

    /**
     * Method to get status of a subscription from the store
     * @param subscriptionID the subscription identifier
     * @return subscription if found.
     * @throws EventBrokerException if the operation fails
     */
    public Subscription getStatus(String subscriptionID) throws EventException {
        if (subManager == null) {
            throw new EventBrokerException("Subscription Manager not Registered");
        }
        try {
            return subManager.getStatus(subscriptionID);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Get Status operation failed.", e);
                throw new EventBrokerException("Get Status operation failed.", e);
            }
        }
    }

    /**
     * Renew an existing subscription
     * @param subscription subscription to renew
     * @return subscription ID
     * @throws EventBrokerException if the operation fails
     */
    public boolean renew(Subscription subscription) throws EventException {
        if (subManager == null) {
            throw new EventBrokerException("Subscription Manager not Registered");
        }
        try {
            return subManager.renew(subscription);
        } catch (EventException e) {
            if (e instanceof EventBrokerException) {
                throw (EventBrokerException)e;
            } else {
                log.error("Renew operation failed.", e);
                throw new EventBrokerException("Renew operation failed.", e);
            }
        }
    }

    public String getSubscriptionManagerUrl() {
        return subscriptionManagerUrl;
    }

    public void setSubscriptionManagerUrl(String subscriptionManagerUrl) {
        this.subscriptionManagerUrl = subscriptionManagerUrl;
    }
}
