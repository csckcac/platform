package org.wso2.carbon.eventbridge.core.streamdefn;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.utils.StreamDefnConverterUtils;

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
public abstract class AbstractStreamDefnStore implements StreamDefinitionStore {
    
@Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamName, String streamVersion)
        throws StreamDefinitionNotFoundException, StreamDefinitionException {
        String streamId = getStreamIdFromStore(credentials, EventBridgeUtils.constructStreamKey(streamName, streamVersion));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + credentials.getUsername()
                    + " for " + EventBridgeUtils.constructStreamKey(streamName, streamVersion));
        }
        return getStreamDefinition(credentials, streamId);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId) throws StreamDefinitionNotFoundException, StreamDefinitionException {
        EventStreamDefinition eventStreamDefinition = getStreamDefinitionFromStore(credentials, streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + credentials.getUsername() + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    @Override
    public Collection<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials) throws StreamDefinitionException {
        return getAllStreamDefinitionsFromStore(credentials);
    }

    @Override
    public String getStreamId(Credentials credentials, String streamName, String streamVersion) throws StreamDefinitionNotFoundException, StreamDefinitionException {
        String streamId = getStreamIdFromStore(credentials, EventBridgeUtils.constructStreamKey(streamName, streamVersion));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    @Override
    public void saveStreamDefinition(Credentials credentials,
                                     EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException {
        EventStreamDefinition existingDefinition = null;
        try {
            existingDefinition = getStreamDefinition(credentials, eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
        } catch (StreamDefinitionNotFoundException e) {
            saveStreamIdToStore(credentials,
                    EventBridgeUtils.constructStreamKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion()),
                    eventStreamDefinition.getStreamId());
            saveStreamDefinitionToStore(credentials, eventStreamDefinition.getStreamId(), eventStreamDefinition);
            return;
        } catch (StreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (!existingDefinition.equals(eventStreamDefinition)) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Another Stream with same name and version exist :" + StreamDefnConverterUtils.convertToJson(existingDefinition));
        }
    }


    /**
     * Store the unique stream Id key and the machine readable stream Id
     * @param credentials username and password
     * @param streamIdKey key consisting stream name and version
     * @param streamId machine generated unique id to be used as stream id.
     * This is done so that this id can be used regardless of the changes take place
     * and will be unique forever for this stream name and version.
     * @throws StreamDefinitionException exception to be thrown for an error by the implementer
     */
    protected abstract void saveStreamIdToStore(Credentials credentials, String streamIdKey,
                                                String streamId) throws StreamDefinitionException;

    protected abstract void saveStreamDefinitionToStore(Credentials credentials, String streamId,
                                                        EventStreamDefinition streamDefinition) throws StreamDefinitionException;


    protected abstract String getStreamIdFromStore(Credentials credentials, String streamIdKey) throws StreamDefinitionException;

    public abstract EventStreamDefinition getStreamDefinitionFromStore(Credentials credentials,
                                                                       String streamId) throws StreamDefinitionException;

    protected abstract Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(
            Credentials credentials) throws StreamDefinitionException;
    
}
