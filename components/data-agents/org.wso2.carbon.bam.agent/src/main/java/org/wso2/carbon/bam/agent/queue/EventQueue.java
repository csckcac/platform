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
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.publish.EventPublisher;

import java.util.Queue;
import java.util.concurrent.*;

public class EventQueue {

    private static final Log log = LogFactory.getLog(EventQueue.class);
    private EventPublisher eventPublisher;
    private ThreadPoolExecutor threadPool = null;
    private BlockingQueue<Runnable> runnableQueue;
    private Queue<EventReceiverComposite> eventQueue;

    private boolean shutdown = false;

    long keepAliveTime = 20;

    public EventQueue(EventPublisher eventPublisher, AgentConfiguration configuration) {
        this.eventPublisher = eventPublisher;
        init(configuration);
    }

    private void init(AgentConfiguration configuration) {
        runnableQueue = new ArrayBlockingQueue<Runnable>(configuration.getTaskQueueSize());
        eventQueue = new ArrayBlockingQueue<EventReceiverComposite>(configuration.getEventQueueSize());
        threadPool = new ThreadPoolExecutor(configuration.getCorePoolSize(),
                                            configuration.getMaxPoolSize(),
                                            keepAliveTime, TimeUnit.SECONDS, runnableQueue);
        threadPool.allowCoreThreadTimeOut(false);
    }

    public void enqueue(EventReceiverComposite eventReceiverComposite) {
        if (shutdown) {
            log.warn("BAM activity queue is shutting down... Not accepting the new activity...");
            return;
        }
        boolean queued = eventQueue.offer(eventReceiverComposite);
        if (queued) {
            if (eventQueue.size() > 0) {
                try {
                    threadPool.execute(new EventWorker(eventQueue, eventPublisher));
                } catch (RejectedExecutionException ignoreRejection) {

                }
            }
        } else {
            log.warn("Queue size exceeded. Event rejected.");
        }
    }

    public void shutdown() {
        runnableQueue = null;
        eventQueue = null;
        threadPool.shutdown();

    }


}
