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
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CarbonEventSource implements EventSource {

    private static final Log log = LogFactory.getLog(CarbonEventSource.class);

    private SubscriptionManager subManager = null;

    private NotificationManager notifyManager = null;

    @Deprecated
    public Subscription subscribe(EventSink eventSink, SubscriptionData subscriptionData)
            throws EventBrokerException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String subscribe(Subscription subscription, EventSink eventSink)
            throws EventBrokerException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public Subscription subscribe(Subscription subscription) throws EventBrokerException {
        throw new UnsupportedOperationException();
    }

    public boolean registerSubscriptionManager(SubscriptionManager subManager)
            throws EventBrokerException {
        if (subManager == null) {
            return false;
        } else if (this.subManager != null) {
            throw new EventBrokerException("Subscription Manager already Registered");
        }
        log.debug("Successfully registered Subscription Manager");
        this.subManager = subManager;
        return true;
    }

    public boolean registerNotificationManager(NotificationManager notifyManager)
            throws EventBrokerException {
        if (notifyManager == null) {
            return false;
        } else if (this.notifyManager != null) {
            throw new EventBrokerException("Notification Manager already Registered");
        }
        log.debug("Successfully registered Notification Manager");
        this.notifyManager = notifyManager;
        return true;
    }

    public void init() throws EventBrokerException {
        //No initialization is needed for the default event source
    }
}
