package org.wso2.carbon.eventbridge.core.engine;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
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
public interface EventCommunication {

    public EventStreamDefinition getStreamDefinition(ReceiverState state, Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException;

    public EventStreamDefinition getStreamDefinition(ReceiverState state, Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException;


    public Collection<EventStreamDefinition> getAllStreamDefinitions(ReceiverState state, Credentials credentials);

    public String getStreamId(ReceiverState state, Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException;

    public void saveStreamDefinition(Credentials credentials,
                                     EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException;


    /**
     * will get called when Events arrive
     *
     * @param credentials
     * @param eventList Arrived event list
     */
    public void receive(Credentials credentials, List<Event> eventList);
}
