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

import junit.framework.TestCase;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;
import org.wso2.carbon.eventing.broker.exceptions.NotificationException;
import org.wso2.carbon.eventing.broker.exceptions.EventBrokerException;
import org.wso2.eventing.Event;
import org.wso2.eventing.EventDispatcher;
import org.wso2.eventing.NotificationManager;

public class CarbonEventBrokerTest extends TestCase {

    public void testGetDefaultInstance() throws Exception {
        EventBrokerService broker1 = CarbonEventBroker.getInstance();
        EventBrokerService broker2 = CarbonEventBroker.getInstance();

        assertSame("The instance is not unique", broker1,  broker2);
    }

    public void testGetNamedInstance() throws Exception {
        EventBrokerService broker1 = CarbonEventBroker.getInstance("instance1");
        EventBrokerService broker2 = CarbonEventBroker.getInstance("instance1");

        assertSame("The instance is not unique", broker1,  broker2);

        EventBrokerService broker3 = CarbonEventBroker.getInstance();
        EventBrokerService broker4 = CarbonEventBroker.getInstance("instance2");

        assertNotSame("The instance is not unique", broker1,  broker3);
        assertNotSame("The instance is not unique", broker1,  broker4);
    }

    public void testRegisterNotificationDispatcher() throws Exception {
        EventBrokerService broker = CarbonEventBroker.getInstance("instance3");
        NotificationManager notificationManager = new TestNotificationManager();
        ((CarbonEventBroker)broker).registerNotificationManager(notificationManager);

        assertNotNull("Notification Manager is not set", broker.getNotificationManager());
        assertSame("Invalid notification manager", notificationManager,
                broker.getNotificationManager());
        broker.registerEventDispatcher(new CarbonEventDispatcher());

        assertTrue("Invalid dispatcher registration",
                ((TestNotificationManager)notificationManager).isEventDispatcherRegistered());
    }

    public void testEventPublish() throws Exception {
        EventBrokerService broker = CarbonEventBroker.getInstance("instance4");
        NotificationManager notificationManager = new TestNotificationManager();
        ((CarbonEventBroker)broker).registerNotificationManager(notificationManager);

        assertNotNull("Notification Manager is not set", broker.getNotificationManager());
        assertSame("Invalid notification manager", notificationManager,
                broker.getNotificationManager());
        broker.publishEvent(new Event());

        assertTrue("Invalid event published status", 
                ((TestNotificationManager)notificationManager).isEventPublished());
    }

    public class TestNotificationManager extends CarbonNotificationManager {

        private boolean eventPublished = false;
        private boolean eventDispatcherRegistered = false;

        public boolean isEventPublished() {
            return eventPublished;
        }

        public boolean isEventDispatcherRegistered() {
            return eventDispatcherRegistered;
        }

        @Override
        public void publishEvent(Event event) throws NotificationException {
            if (event != null) {
                eventPublished = true;
            } else {
                throw new NotificationException("no event found");
            }
        }

        @Override
        public void registerEventDispatcher(EventDispatcher eventDispatcher) throws NotificationException {
            if (eventDispatcher != null) {
                eventDispatcherRegistered = true;
            } else {
                throw new NotificationException("no event found");
            }
        }
    }
}
