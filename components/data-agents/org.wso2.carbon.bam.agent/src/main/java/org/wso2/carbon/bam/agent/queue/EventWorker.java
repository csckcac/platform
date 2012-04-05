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
package org.wso2.carbon.bam.agent.queue;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.agent.publish.EventPublisher;

import java.util.ArrayList;
import java.util.Queue;

public class EventWorker implements Runnable {

    private static final Log log = LogFactory.getLog(EventWorker.class);

    private Queue<EventReceiverComposite> eventQueue;
    EventPublisher eventPublisher;

    public EventWorker(Queue<EventReceiverComposite> eventQueue,
                       EventPublisher eventPublisher) {
        this.eventQueue = eventQueue;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void run() {
        clearActivityDataQueue(eventQueue.size());
    }

    private void clearActivityDataQueue(int size) {
        if (log.isDebugEnabled()) {
            log.debug("Clearing " + size + " activities from the activity queue...");
        }
        ArrayList<EventReceiverComposite> eventList = new ArrayList<EventReceiverComposite>();
        int tenantId = -1;
        for (int i = 0; i < size; i++) {

            EventReceiverComposite eventReceiverComposite = eventQueue.poll();
            //Sometimes other thread may get the last queue object
            if (eventReceiverComposite != null) {

                eventList.add(eventReceiverComposite);
            }
        }
        if (eventList.size() > 0) {
            eventPublisher.publish(eventList);
        }

    }

}
