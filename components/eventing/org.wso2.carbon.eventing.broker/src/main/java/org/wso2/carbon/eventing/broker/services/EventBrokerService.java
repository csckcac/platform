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
package org.wso2.carbon.eventing.broker.services;

import org.wso2.eventing.NotificationManager;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.exceptions.EventException;
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;

public interface EventBrokerService extends NotificationManager {
    SubscriptionManager getSubscriptionManager();

    NotificationManager getNotificationManager();

    void init() throws EventBrokerException;

    String subscribe(Subscription subscription) throws EventBrokerException;

    boolean unsubscribe(String subscriptionID) throws EventException;

    Subscription getStatus(String subscriptionID) throws EventException;

    boolean renew(Subscription subscription) throws EventException;

    String getSubscriptionManagerUrl();

    void setSubscriptionManagerUrl(String subscriptionManagerUrl);
}
