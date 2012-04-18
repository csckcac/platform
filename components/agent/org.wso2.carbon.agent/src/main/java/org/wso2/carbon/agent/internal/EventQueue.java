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
package org.wso2.carbon.agent.internal;

import org.wso2.carbon.agent.commons.Event;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue containing the incoming events of a DataPublisher.
 * @param <E>
 */
public class EventQueue<E> {
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    private volatile boolean isEventDispatching = false;

    public synchronized Event poll() {
        Event event = eventQueue.poll();
        if (null == event) {
            isEventDispatching = false;
        }
        return event;
    }

    public synchronized boolean put(Event event) throws InterruptedException {
        eventQueue.put(event);
        if (!isEventDispatching) {
            isEventDispatching = true;
        }
        return isEventDispatching;
    }
}
