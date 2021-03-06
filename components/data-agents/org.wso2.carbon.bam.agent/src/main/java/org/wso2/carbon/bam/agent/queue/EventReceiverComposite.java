package org.wso2.carbon.bam.agent.queue;

import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;

import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class EventReceiverComposite {

    private final List<Event> event;
    private final EventReceiver eventReceiver;

    public EventReceiverComposite(List<Event> events, EventReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
        this.event = events;
    }

    public List<Event> getEvent() {
        return event;
    }

    public EventReceiver getEventReceiver() {
        return eventReceiver;
    }
}
