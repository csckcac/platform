/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service.data.publisher.queue;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.service.data.publisher.data.ActivityData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.process.ActivityWorker;
import org.wso2.carbon.bam.service.data.publisher.publish.StatsProcessor;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActivityQueue {

    private static final Log log = LogFactory.getLog(ActivityQueue.class);

    private BlockingQueue<Runnable> runnableQueue = new ArrayBlockingQueue<Runnable>(100);
    private Queue<PublishData> activityInQueue = new ArrayBlockingQueue<PublishData>(6000);
    private ThreadPoolExecutor threadPool = null;

    int poolSize = 30;
    int maxPoolSize = 150;
    long keepAliveTime = 10;

    private StatsProcessor activityStatsProcessor;
    private boolean shutdown = false;


    public ActivityQueue(StatsProcessor activityStatsProcessor) {
        this.activityStatsProcessor = activityStatsProcessor;
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
                                            keepAliveTime, TimeUnit.SECONDS, runnableQueue);
        threadPool.allowCoreThreadTimeOut(true);
    }

    public void enqueue(PublishData publishData) {
        if (shutdown) {
            log.warn("BAM service Activity queue is shutting down... Not accepting the new statistics...");
            return;
        }
        boolean queued = activityInQueue.offer(publishData);
        if (queued) {
            if (activityInQueue.size() > 0) {
                try {
                    threadPool.execute(new ActivityWorker(activityInQueue, activityStatsProcessor));
                } catch (RejectedExecutionException ignoreRejection) {

                }
            }
        } else {
            log.warn("Queue size exceeded. Event rejected.");
        }
    }
}
