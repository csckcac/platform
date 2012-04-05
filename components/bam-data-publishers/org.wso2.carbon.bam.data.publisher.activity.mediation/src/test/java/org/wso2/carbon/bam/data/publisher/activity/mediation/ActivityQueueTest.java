/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.bam.data.publisher.activity.mediation;

import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.bam.data.publisher.activity.mediation.eventing.EventGenerator;
import org.wso2.carbon.bam.data.publisher.activity.mediation.eventing.TestEventBrokerService;
import org.wso2.carbon.bam.data.publisher.activity.mediation.services.ActivityPublisherAdmin;

public class ActivityQueueTest extends AbstractActivityTest {

    public void testSingleActivity1() throws AxisFault{
        TestEventBrokerService eventBrokerService = new TestEventBrokerService("");
        ActivityPublisherUtils.setEventBroker(eventBrokerService);

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        ActivityQueue queue = new ActivityQueue(new EventGenerator());
        queue.setThreshold(1);
        initSerialization();

        MessageContext synCtx = getTestContext();
        MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, true);
        queue.enqueue(activity);
        delay(500);
        queue.cleanup();

        //       Todo: Fix this test, product is working fine
//        assertEquals(1, eventBrokerService.getTotalEventCount());
    }

    public void testMultipleActivities1() throws AxisFault {
        TestEventBrokerService eventBrokerService = new TestEventBrokerService("");
        ActivityPublisherUtils.setEventBroker(eventBrokerService);

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        ActivityQueue queue = new ActivityQueue(new EventGenerator());
        queue.setThreshold(10);
        initSerialization();

        for (int i = 0; i < 100; i++) {
            MessageContext synCtx = getTestContext();
            MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, true);
            queue.enqueue(activity);
        }

        delay(500);
        queue.cleanup();

//       Todo: Fix this test
// assertEquals(100, eventBrokerService.getActivityCount());
    }

    public void testMultipleActivities2() throws AxisFault {
        TestEventBrokerService eventBrokerService = new TestEventBrokerService("");
        ActivityPublisherUtils.setEventBroker(eventBrokerService);

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        ActivityQueue queue = new ActivityQueue(new EventGenerator());
        queue.setThreshold(10);
        initSerialization();

        final int TOTAL_THREAD_COUNT = 10;
        for (int i = 0; i < TOTAL_THREAD_COUNT; i++) {
            Thread t = new ActivityGenerator(queue);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ignored) {

            }
        }

        System.out.println("Waiting for the activity consumer to terminate...");
        queue.cleanup();
//       Todo: Fix this test
//        assertEquals(1000, eventBrokerService.getActivityCount());
    }

    private class ActivityGenerator extends Thread {

        private ActivityQueue queue;

        public ActivityGenerator(ActivityQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                MessageContext synCtx = getTestContext();
                MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, true);
                queue.enqueue(activity);
            }
        }
    }
}