/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.events.notification.internal;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.wso2.carbon.appfactory.events.notification.service.EventBean;

public class EventRepository {

    private Buffer eventBuffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer());
    private static EventRepository instance = new EventRepository();

    public static EventRepository getInstance() {
        return instance;
    }
    public Buffer getEventBuffer() {
        return eventBuffer;
    }

    public void addEvent(EventBean event) {
        eventBuffer.add(event);
    }
}
