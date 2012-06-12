package org.wso2.carbon.eventbridge.core.internal;

import org.wso2.carbon.eventbridge.core.streamdefn.StreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.subscriber.EventSubscriber;

import java.util.ArrayList;
import java.util.Collections;
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
public class Utils {
    public static StreamDefinitionStore streamDefinitionStore;
    public static List<EventSubscriber> eventSubscribers = Collections.synchronizedList(new ArrayList<EventSubscriber>());

    public static StreamDefinitionStore getStreamDefinitionStore() {
        return streamDefinitionStore;
    }

    public static void setStreamDefinitionStore(StreamDefinitionStore streamDefinitionStore) {
        Utils.streamDefinitionStore = streamDefinitionStore;
    }

    public static List<EventSubscriber> getEventSubscribers() {
        return eventSubscribers;
    }

    public static void addEventSubscriber(EventSubscriber eventSubscriber) {
        eventSubscribers.add(eventSubscriber);
    }

    public static void removeEventSubscriber(EventSubscriber eventSubscriber) {
        eventSubscribers.remove(eventSubscriber);
    }


}
