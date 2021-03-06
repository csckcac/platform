/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.agent.server.internal.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.agent.server.internal.utils.AgentServerConstants;
import org.wso2.carbon.agent.server.internal.utils.EventComposite;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Event Queue class wraps a thread safe queue to,
 * queue and deque events in a scalable manner
 */
public class EventQueue {

    private static final Log log = LogFactory.getLog(EventQueue.class);

    private BlockingQueue<EventComposite> eventQueue;

    private ExecutorService executorService;
    private List<AgentCallback> subscribers;

    public EventQueue(List<AgentCallback> subscribers) {
        this.subscribers = subscribers;
        // Note : Using a fixed worker thread pool and a bounded queue to prevent the server dying if load is too high
        executorService = Executors.newFixedThreadPool(AgentServerConstants.NO_OF_WORKER_THREADS);
        eventQueue = new ArrayBlockingQueue<EventComposite>(AgentServerConstants.EVENT_CAPACITY);
    }

    public void publish(EventComposite eventComposite) {
        try {
            eventQueue.put(eventComposite);
        } catch (InterruptedException e) {
            String logMessage = "Failure to insert event into queue";
            log.warn(logMessage);
        }
        executorService.submit(new QueueWorker(eventQueue, subscribers));
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }
}
