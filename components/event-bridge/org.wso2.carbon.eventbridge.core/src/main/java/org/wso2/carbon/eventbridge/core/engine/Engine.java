package org.wso2.carbon.eventbridge.core.engine;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.core.exceptions.EventProcessingException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.internal.Utils;
import org.wso2.carbon.eventbridge.core.streamdefn.StreamDefinitionStore;
import org.wso2.carbon.eventbridge.core.subscriber.EventSubscriber;

import java.util.Collection;
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
public class Engine implements EventSubscriber, StreamDefinitionStore, EventBridgeEngine {
    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionException {
        synchronized (Engine.class) {
            return Utils.getStreamDefinitionStore().getStreamDefinition(credentials, streamName, streamVersion);
        }
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId) throws StreamDefinitionNotFoundException, StreamDefinitionException {
        synchronized (Engine.class) {
            return Utils.getStreamDefinitionStore().getStreamDefinition(credentials, streamId);
        }
    }

    @Override
    public Collection<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials) throws StreamDefinitionException {
        synchronized (Engine.class) {
            return Utils.getStreamDefinitionStore().getAllStreamDefinitions(credentials);
        }
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion) throws StreamDefinitionNotFoundException, StreamDefinitionException {
        synchronized (Engine.class) {
            return Utils.getStreamDefinitionStore().getStreamId(credentials, streamName, streamVersion);
        }
    }


    @Override
    public void saveStreamDefinition(Credentials credentials, EventStreamDefinition eventStreamDefinition) throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException {
        synchronized (Engine.class) {
            Utils.getStreamDefinitionStore().saveStreamDefinition(credentials, eventStreamDefinition);
        }
    }

    @Override
    public void receive(Credentials credentials, List<Event> eventList) throws EventProcessingException {
        synchronized (Engine.class) {
            for (EventSubscriber eventSubscriber : Utils.getEventSubscribers()) {
                eventSubscriber.receive(credentials, eventList);
            }
        }
    }

    @Override
    public void addEventSubscriber(EventSubscriber eventSubscriber) {
        synchronized (Engine.class) {
            Utils.addEventSubscriber(eventSubscriber);
        }
    }

    @Override
    public void removeEventSubscriber(EventSubscriber eventSubscriber) {
        synchronized (Engine.class) {
            Utils.removeEventSubscriber(eventSubscriber);
        }
    }
}
