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
package org.wso2.carbon.bam.activity.mediation.data.publisher.queue;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.activity.mediation.data.publisher.data.MessageActivity;
import org.wso2.carbon.bam.activity.mediation.data.publisher.process.ActivityWorker;
import org.wso2.carbon.bam.activity.mediation.data.publisher.publish.ActivityProcessor;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActivityQueue {

    private static final Log log = LogFactory.getLog(ActivityQueue.class);
    private ActivityProcessor activityProcessor;
    private ThreadPoolExecutor threadPool = null;
    private BlockingQueue<Runnable> runnableQueue;
    private Queue<MessageActivity> activityQueue;

    private boolean shutdown = false;

    long keepAliveTime = 20;

    public ActivityQueue(ActivityProcessor activityProcessor, PublisherConfiguration configuration) {
        this.activityProcessor = activityProcessor;
        runnableQueue = new ArrayBlockingQueue<Runnable>(configuration.getTaskQueueSize());
        activityQueue = new ArrayBlockingQueue<MessageActivity>(configuration.getEventQueueSize());
        threadPool = new ThreadPoolExecutor(configuration.getCorePoolSize(),
                                            configuration.getMaxPoolSize(),
                                            keepAliveTime, TimeUnit.SECONDS, runnableQueue);
        threadPool.allowCoreThreadTimeOut(false);
    }

    public void enqueue(MessageActivity activity) {
        if (shutdown) {
            log.warn("BAM activity queue is shutting down... Not accepting the new activity...");
            return;
        }
        boolean queued = activityQueue.offer(activity);
        if (queued) {
            if (activityQueue.size() > 0) {
                try {
                    threadPool.execute(new ActivityWorker(activityQueue, activityProcessor));
                } catch (RejectedExecutionException ignoreRejection) {

                }
            }
        } else {
            log.warn("Queue size exceeded. Event rejected.");
        }
    }


}
