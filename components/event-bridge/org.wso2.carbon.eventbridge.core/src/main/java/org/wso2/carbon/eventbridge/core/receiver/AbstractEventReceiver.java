package org.wso2.carbon.eventbridge.core.receiver;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.engine.Engine;
import org.wso2.carbon.eventbridge.core.engine.EventCommunication;
import org.wso2.carbon.eventbridge.core.exceptions.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.state.ReceiverState;

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
public abstract class AbstractEventReceiver implements EventCommunication {


    // implement get
    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }

    public EventStreamDefinition getStreamDefinition(ReceiverState state, Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException {
        String streamId = getEngine().getStreamId(state, credentials, streamName, streamVersion);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + credentials.getUsername() + " for " + constructNameVersionKey(streamName, streamVersion));
        }
        return getEngine().getStreamDefinition(state, credentials, streamId);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(ReceiverState state, Credentials credentials, String streamId) throws StreamDefinitionNotFoundException {
        EventStreamDefinition eventStreamDefinition = getEngine().getStreamDefinition(state, credentials, streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + credentials.getUsername() + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    @Override
    public Collection<EventStreamDefinition> getAllStreamDefinitions(ReceiverState state, Credentials credentials) {
        return getEngine().getAllStreamDefinitions(state, credentials);
    }

    @Override
    public String getStreamId(ReceiverState state, Credentials credentials, String streamName, String streamVersion) throws StreamDefinitionNotFoundException {
        String streamId = getEngine().getStreamId(state, credentials, streamName, streamVersion);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    @Override
    public void saveStreamDefinition(Credentials credentials, EventStreamDefinition eventStreamDefinition) throws DifferentStreamDefinitionAlreadyDefinedException {
        getEngine().saveStreamDefinition(credentials, eventStreamDefinition);
    }

    @Override
    public void receive(Credentials credentials, List<Event> eventList) {
        getEngine().receive(credentials, eventList);
    }

    protected abstract Engine getEngine();
}
