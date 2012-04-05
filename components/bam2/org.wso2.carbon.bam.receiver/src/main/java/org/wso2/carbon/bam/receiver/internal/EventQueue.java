/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.dataobjects.EventData;
import org.wso2.carbon.bam.receiver.ReceiverConstants;
import org.wso2.carbon.bam.receiver.event.RawEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Event Queue class wraps a thread safe queue to
 * queue and deque events in a scalable manner
 */
public class EventQueue {

    private static final Log log = LogFactory.getLog(EventQueue.class);

    private BlockingQueue<RawEvent> queue;

    private BlockingQueue<EventData> eventQueue;

    private ExecutorService executorService;

    public EventQueue() {
        // Note : Using a fixed worker thread pool and a bounded queue to prevent the server dying if load is too high
        executorService = Executors.newFixedThreadPool(ReceiverConstants.NO_OF_WORKER_THREADS);
        queue = new ArrayBlockingQueue<RawEvent>(ReceiverConstants.EVENT_CAPACITY);
        eventQueue = new ArrayBlockingQueue<EventData>(ReceiverConstants.EVENT_CAPACITY);
    }
    
/*    public void queue(RawEvent rawEvent) {
        boolean success = queue.offer(rawEvent);
        if (success) {
            if (log.isDebugEnabled()) {
                log.debug("Message enqueued : " + rawEvent.getSOAPBody().toString());
            }
            // Note: As an event is enqueued, we submit a worker to dequeue it and process it.
            // This is done so that we don't have to use another thread that observes and polls the queue and dequeues events.
            // During a high load, when a large number of threads are in action, such an observer thread can starve,
            // thus, becoming a bottle neck and causing the queue to overflow.
            executorService.submit(new QueueWorker(queue));
        } else {
            // if queuing fails log the failure and return
            String logMessage = "Failure to insert event into queue";
            if (log.isDebugEnabled()) {
                logMessage += "\n Event Content : " + rawEvent.getSOAPBody().toString();
            }
            log.info(logMessage);
        }
    }*/

    public void queue(EventData event) {
        boolean success = eventQueue.offer(event);
        if (success) {
            if (log.isDebugEnabled()) {
                log.debug("Message enqueued : " + event.toString());
            }
            // Note: As an event is enqueued, we submit a worker to dequeue it and process it.
            // This is done so that we don't have to use another thread that observes and polls the queue and dequeues events.
            // During a high load, when a large number of threads are in action, such an observer thread can starve,
            // thus, becoming a bottle neck and causing the queue to overflow.
            executorService.submit(new QueueWorker(eventQueue));
        } else {
            // if queuing fails log the failure and return
            String logMessage = "Failure to insert event into queue";
            if (log.isDebugEnabled()) {
                logMessage += "\n Event Content : " + event.toString();
            }
            log.warn(logMessage);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }
}
