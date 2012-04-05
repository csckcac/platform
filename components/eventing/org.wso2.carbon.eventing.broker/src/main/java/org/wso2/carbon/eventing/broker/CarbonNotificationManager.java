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
import org.wso2.carbon.eventing.broker.exceptions.NotificationException;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

public class CarbonNotificationManager implements NotificationManager {

    private static final Log log = LogFactory.getLog(CarbonNotificationManager.class);
    private List<EventDispatcher> dispatchers = Collections.synchronizedList(
            new LinkedList<EventDispatcher>());
    private EventBrokerService broker = null;
    private ExecutorService executor = null;
    private Map<String, String> parameters = null;

    public void setEventBroker(EventBrokerService broker) {
        this.broker = broker;
    }

    public String getPropertyValue(String key) {
        if (this.parameters != null) {
            return this.parameters.get(key);
        }
        return null;
    }

    public void init(Map<String, String> parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
        String temp = null;
        int minSpareThreads = 25;
        if (parameters != null) {
            temp = parameters.get("minSpareThreads");
            if (temp != null) {
                minSpareThreads = Integer.parseInt(temp);
            }
        }
        int maxThreads = 150;
        if (parameters != null) {
            temp = parameters.get("maxThreads");
            if (temp != null) {
                maxThreads = Integer.parseInt(temp);
            }
        }
        int maxQueuedRequests = 100;
        if (parameters != null) {
            temp = parameters.get("maxQueuedRequests");
            if (temp != null) {
                maxQueuedRequests = Integer.parseInt(temp);
            }
        }
        long keepAliveTime = 1000;
        if (parameters != null) {
            temp = parameters.get("keepAliveTime");
            if (temp != null) {
                keepAliveTime = Long.parseLong(temp);
            }
        }
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueuedRequests);
        executor = new ThreadPoolExecutor(minSpareThreads, maxThreads,
                keepAliveTime, TimeUnit.NANOSECONDS, queue);
    }

    @SuppressWarnings("unchecked")
    public void publishEvent(Event event) throws NotificationException {
        if (getExecutor() == null) {
            String msg = "Notification Dispatcher not initialized";
            log.error(msg);
            throw new NotificationException(msg);
        }
        for (EventDispatcher dispatcher : dispatchers) {
            try {
                SubscriptionManager subManager = broker.getSubscriptionManager();
                List<Subscription> subscriptions = subManager.getMatchingSubscriptions(event);
                for (Subscription subscription : subscriptions) {
                    getExecutor().submit(new Worker(dispatcher,  event, subscription));
                }
            } catch (Exception e) {
                log.warn("Unable to dispatch event of type " + event.getClass().getName() +
                        " using the dispatcher of type " + dispatcher.getClass().getName(), e);
            }
        }
    }

    public void registerEventDispatcher(EventDispatcher eventDispatcher)
            throws NotificationException {
        if (eventDispatcher == null) {
            log.warn("No event dispatcher was provided.");
            return;
        } else if (eventDispatcher instanceof CarbonEventDispatcher) {
            ((CarbonEventDispatcher)eventDispatcher).setNotificationManager(this);
        }
        dispatchers.add(eventDispatcher);
    }

    public ExecutorService getExecutor() {
        return executor;
    }


    private class Worker implements Runnable {

        EventDispatcher dispatcher;
        Event event;
        Subscription subscription;

        public Worker(EventDispatcher dispatcher, Event event, Subscription subscription) {
            this.dispatcher = dispatcher;
            this.event = event;
            this.subscription = subscription;
        }

        @SuppressWarnings("unchecked")
        public void run() {
            try {
                dispatcher.send(event, subscription);
            } catch (Exception e) {
                log.warn("Unable to dispatch event of type " + event.getClass().getName() +
                        " using the dispatcher of type " + dispatcher.getClass().getName(), e);
            }
        }
    }
}
