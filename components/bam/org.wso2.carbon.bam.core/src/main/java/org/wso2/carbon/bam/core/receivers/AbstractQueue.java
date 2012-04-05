/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.core.receivers;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractQueue {

    private Queue<MessageContext> serverQueue = new ArrayBlockingQueue<MessageContext>(5000);
    private ExecutorService exec;

    private boolean shutdown = false;
    private static final Log log = LogFactory.getLog(AbstractQueue.class);


    public AbstractQueue(int threadPoolSize) {
        exec = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void enqueue(MessageContext messageContext) {
        if (shutdown) {
            log.warn("The queue is shutting down... Not accepting the new events...");
            return;
        }
        messageContext.getEnvelope().build();
        boolean queued = serverQueue.offer(messageContext);
        if (queued) {
            if (serverQueue.size() > 0) {
                exec.submit(new ServerWorker());
            }
        } else {
            log.warn("Queue filled up. Event rejected");
            if (log.isDebugEnabled()) {
                log.debug("Event rejected : " + messageContext.getEnvelope().toString());
            }
        }
    }

    public void cleanup() {
        shutdown = true; // This will stop accepting new statistics into the queue
        while (serverQueue.size() > 0) {
            // Wait for the worker to purge already accepted statistics from the queue
            if (log.isDebugEnabled()) {
                log.debug("Waiting for the queue to become empty");
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
    }


    private class ServerWorker implements Runnable {

        public void run() {
            clearEventsInQueue(serverQueue.size());
        }
    }

    private void clearEventsInQueue(int size) {
        if (log.isDebugEnabled()) {
            log.debug("No of messages in queue : " + size);
        }
        if (size == 0) {
            return;
        }
        MessageContext[] messageContexts = new MessageContext[size];
        for (int i = 0; i < size; i++) {
            messageContexts[i] = serverQueue.poll();
        }
        processEvents(messageContexts);

    }

    protected abstract void processEvents(MessageContext[] messageContexts);

}
