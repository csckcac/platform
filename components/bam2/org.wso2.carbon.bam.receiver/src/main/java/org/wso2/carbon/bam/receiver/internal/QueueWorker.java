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
import org.wso2.carbon.bam.core.persistence.PersistenceManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class QueueWorker implements Runnable {

    private static final Log log = LogFactory.getLog(QueueWorker.class);

    private BlockingQueue<EventData> eventQueue;


    public QueueWorker(BlockingQueue<EventData> queue) {
        this.eventQueue = queue;
    }

    @Override
    public void run() {
        try {
            if (log.isDebugEnabled()) {
                // Useful log to determine if the server can handle the load
                // If the numbers go above 1000+, then it probably will.
                // Typically, for c = 300, n = 1000, the number stays < 100
                log.debug(eventQueue.size() + " messages in queue before " +
                          Thread.currentThread().getName() + " worker has polled queue");
            }
            EventData event = eventQueue.poll();

            // if the event was built, this cannot be null
            // Being defensive, just in case the event is not built
            if (event == null) {
                log.warn("Message deque failed: Event is null");
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Message dequeued : " + event.toString());
            }

            PersistenceManager manager = new PersistenceManager();
            manager.storeEvent(event.getCredentials(), event);

            if (log.isDebugEnabled()) {
                log.info(eventQueue.size() + " messages in queue after " +
                         Thread.currentThread().getName() + " worker has finished work");
            }
        } catch (Throwable e) {
            log.error("Error in processing message and storing", e);
        }
    }

//    private
//    private EventData constructEventData(DataType dataType, Iterator elementIterator, Date timeStamp, String xPathResult) {
//        Map<String, String> data = new HashMap<String, String>();
//        while (elementIterator.hasNext()) {
//            Object object = elementIterator.next();
//            if (object instanceof OMElement) {
//                OMElement elem = (OMElement) object;
//                data.put(elem.getLocalName(), elem.getText());   // column name
//            }
//        }
//        return new EventData(dataType, data, timeStamp, xPathResult);
//    }


}
