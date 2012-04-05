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
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.process.ServiceStatsWorker;
import org.wso2.carbon.bam.service.data.publisher.publish.StatsProcessor;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServiceStatisticsQueue {

    private static final Log log = LogFactory.getLog(ServiceStatisticsQueue.class);

    private BlockingQueue<Runnable> runnableQueue;
    private Queue<PublishData> statisticsQueue;
    private ThreadPoolExecutor threadPool = null;

    long keepAliveTime = 20;

    private StatsProcessor serviceStatsProcessor;
    private boolean shutdown = false;


    public ServiceStatisticsQueue(StatsProcessor serviceStatisticProcessor,
                                  PublisherConfiguration configuration) {
        runnableQueue = new ArrayBlockingQueue<Runnable>(configuration.getTaskQueueSize());
        statisticsQueue = new ArrayBlockingQueue<PublishData>(configuration.getEventQueueSize());
        this.serviceStatsProcessor = serviceStatisticProcessor;
        threadPool = new ThreadPoolExecutor(configuration.getCorePoolSize(), configuration.getMaxPoolSize(),
                                            keepAliveTime, TimeUnit.SECONDS, runnableQueue);
        threadPool.allowCoreThreadTimeOut(false);
    }


    public void enqueue(PublishData publishData) {
        if (shutdown) {
            log.warn("BAM service statistics queue is shutting down... Not accepting the new statistics...");
            return;
        }
        boolean queued = statisticsQueue.offer(publishData);
        if (queued) {
            if (statisticsQueue.size() > 0) {
                try {
                    threadPool.execute(new ServiceStatsWorker(statisticsQueue,serviceStatsProcessor));
                } catch (RejectedExecutionException ignoreRejection) {

                }
            }
        } else {
            log.warn("Queue size exceeded. Event rejected.");
        }
    }

    public void cleanup() {
        shutdown = true; // This will stop accepting new statistics into the queue
        while (statisticsQueue.size() > 0) {
            // Wait for the worker to purge already accepted statistics from the queue
            if (log.isDebugEnabled()) {
                log.debug("Waiting for the service statistics queue to become empty");
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // Restoring the interrupted status after catching InterruptedException
                // instead of Swallowing
                Thread.currentThread().interrupt();
            }
        }
        threadPool.shutdownNow();
        serviceStatsProcessor.destroy();
    }
}
