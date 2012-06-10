package org.wso2.carbon.eventbridge.core.receiver;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.subscriber.EventSubscriber;

import java.util.Collection;

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
public abstract class AbstractEventReceiver implements EventReceiver {

    private EventSubscriber subscriber;

    public void setSubscriber(EventSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    // implement get
    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String name, String version, String username, String password)
            throws StreamDefinitionNotFoundException {
        String streamId = getStreamIdFromStore(domainName, constructNameVersionKey(name, version), username, password);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + constructNameVersionKey(name, version));
        }
        return getStreamDefinition(domainName, streamId, username, password);
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String streamId, String username, String password)
            throws StreamDefinitionNotFoundException {
        EventStreamDefinition eventStreamDefinition = getStreamDefinitionFromStore(domainName, streamId, username, password);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamId);
        }
        return eventStreamDefinition;
    }


    public Collection<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials) {
        return getAllStreamDefinitionsFromStore(credentials.getDomainName(), credentials.getUsername(), credentials.getPassword());
    }

    public String getStreamId(String domainName, String streamName, String streamVersion, String username, String password)
            throws StreamDefinitionNotFoundException {
        String streamId = getStreamIdFromStore(domainName, constructNameVersionKey(streamName, streamVersion),  username, password);
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    protected abstract String getStreamIdFromStore(String domainName, String streamIdKey, String username, String password);

    protected abstract EventStreamDefinition getStreamDefinitionFromStore(String domainName,
                                                                       String streamId, String username, String password);

    protected abstract Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(
            String domainName, String username, String password);
}
