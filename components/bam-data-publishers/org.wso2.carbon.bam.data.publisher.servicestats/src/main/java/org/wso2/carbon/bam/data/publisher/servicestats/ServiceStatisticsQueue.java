/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.data.publisher.servicestats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.servicestats.data.StatisticData;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServiceStatisticsQueue {
    private static final Log log = LogFactory.getLog(ServiceStatisticsQueue.class);

    private int previousQueueSize;
    private boolean shutdown = false;
    private Queue<StatisticData> statisticsQueue = new ArrayBlockingQueue<StatisticData>(2000);
    //            new ConcurrentLinkedQueue<StatisticData>();
    private ExecutorService exec = Executors.newFixedThreadPool(100);
    private ServiceStatsProcessor serviceStatsProcessor;

//    private ExecutorService dequePool = Executors.newFixedThreadPool(50);

    public ServiceStatisticsQueue(ServiceStatsProcessor serviceStatisticProcessor) {
        serviceStatsProcessor = serviceStatisticProcessor;
//        for (int i = 0; i < 10; i ++) {

//        }
    }

    public int getSize() {
        return statisticsQueue.size();
    }

    public void enqueue(StatisticData systemStatisticData) {
        if (shutdown) {
            log.warn("BAM service statistics queue is shutting down... Not accepting the new statistics...");
            return;
        }
        boolean queued = statisticsQueue.offer(systemStatisticData);
        if (queued) {
            if (statisticsQueue.size() > 0) {
                exec.submit(new ServiceStatsWorker());
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
        exec.shutdownNow();
        serviceStatsProcessor.destroy();
    }

    private void delay(int previousQueueSize, int idleLoopCount) {
        // let the thread sleep according to load
        try {
            if (idleLoopCount < 20) {
                if (previousQueueSize > 1000) {
                    Thread.sleep(10);
                } else if (1000 >= previousQueueSize && previousQueueSize > 500) {
                    Thread.sleep(50);
                } else if (500 >= previousQueueSize && previousQueueSize > 100) {
                    Thread.sleep(200);
                } else if (100 >= previousQueueSize && previousQueueSize > 10) {
                    Thread.sleep(300);
                } else {
                    Thread.sleep(1000);
                }
            } else {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // Restoring the interrupted status after catching InterruptedException
            // instead of Swallowing
            Thread.currentThread().interrupt();
        }
    }


    private class ServiceStatsWorker implements Runnable {

        public void run() {
            clearStatisticDataInQueue(statisticsQueue.size());
//            if (log.isDebugEnabled()) {
//                log.info("Initializing the ServiceStats processor thread...");
//            }
//            int idleLoopCount=0;
//
//            while (true) {
//                try {
//                    int size = statisticsQueue.size();
//                    if (size > 0) {
//                        //reset idle loop count
//                        idleLoopCount = 0;
//                        previousQueueSize = size;
//                        clearStatisticDataInQueue(size);
//                    } else {
//                        idleLoopCount+=1;
//                        delay(previousQueueSize,idleLoopCount);
//                    }
//                } catch (Throwable t) {
//                    // Catch all the errors here - Just don't let the poor worker die!
//                    log.error("Unexpected runtime error in the service stats processor", t);
//                }
//
//            }
        }
    }
//
//    private class EventProcessor implements Runnable {
//
//        private int size;
//        private EventProcessor(int size) {
//            this.size = size;
//        }
//
//        @Override
//        public void run() {
//            if (log.isDebugEnabled()) {
//                log.debug("Clearing " + size + " statistics data from the service statistics queue...");
//            }
//
//            StatisticData[] sts = new StatisticData[size];
//            for (int i = 0; i < size; i++) {
//                sts[i] = statisticsQueue.poll();
//            }
//            serviceStatsProcessor.process(sts);
//        }
//    }

    private void clearStatisticDataInQueue(int size) {
//        dequePool.submit(new EventProcessor(size));
        if (log.isDebugEnabled()) {
            log.debug("Number of events in queue : " + size );
        }
        StatisticData[] sts = new StatisticData[size];
        for (int i = 0; i < size; i++) {
            sts[i] = statisticsQueue.poll();
        }
        serviceStatsProcessor.process(sts);
    }

}
