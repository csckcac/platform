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
package org.wso2.carbon.eventbridge.core.definitionstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.Utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Event Stream Definition Store interface
 * Used to persist Event Stream Definitions at the Agent Server
 */
public abstract class AbstractStreamDefinitionStore implements StreamDefinitionStore {

    private Log log = LogFactory.getLog(AbstractStreamDefinitionStore.class);

    private String constructNameVersionKey(String name, String version) {
        return EventBridgeUtils.constructStreamKey(name, version);
    }

    public EventStreamDefinition getStreamDefinition(Credentials credentials, String name, String version)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        String streamId = getStreamIdFromStore(credentials, constructNameVersionKey(name, version));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + credentials.getUsername() + " for " + constructNameVersionKey(name, version));
        }
        return getStreamDefinition(credentials, streamId);
    }

    public EventStreamDefinition getStreamDefinition(Credentials credentials, String streamId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        EventStreamDefinition eventStreamDefinition = getStreamDefinitionFromStore(credentials, streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + credentials.getUsername() + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    public void saveStreamDefinition(Credentials credentials,
                                     EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        EventStreamDefinition existingDefinition;
        try {
            existingDefinition = getStreamDefinition(credentials, eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
        } catch (StreamDefinitionNotFoundException e) {
            saveStreamIdToStore(credentials, constructNameVersionKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion()), eventStreamDefinition.getStreamId());
            saveStreamDefinitionToStore(credentials, eventStreamDefinition.getStreamId(), eventStreamDefinition);
            return;
        }
        if (!existingDefinition.equals(eventStreamDefinition)) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Another Stream with same name and version exi" +
                    "st :" + EventDefinitionConverterUtils
                    .convertToJson(existingDefinition));
        }
    }

    public Collection<EventStreamDefinition> getAllStreamDefinitions(Credentials credentials)
    {
        try {
            return getAllStreamDefinitionsFromStore(credentials);
        } catch (StreamDefinitionStoreException e) {
            log.error("Error occured when trying to retrieve definitions. Returning empty list.");
            return new ArrayList<EventStreamDefinition>();
        }
    }

    public String getStreamId(Credentials credentials, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        String streamId = getStreamIdFromStore(credentials, constructNameVersionKey(streamName, streamVersion));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    protected abstract void saveStreamIdToStore(Credentials credentials, String streamIdKey,
                                                String streamId) throws StreamDefinitionStoreException;

    protected abstract void saveStreamDefinitionToStore(Credentials credentials, String streamId,
                                                        EventStreamDefinition streamDefinition)
            throws StreamDefinitionStoreException;


    protected abstract String getStreamIdFromStore(Credentials credentials, String streamIdKey)
            throws StreamDefinitionStoreException;

    public abstract EventStreamDefinition getStreamDefinitionFromStore(Credentials credentials,
                                                                       String streamId)
            throws StreamDefinitionStoreException;

    protected abstract Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(
            Credentials credentials) throws StreamDefinitionStoreException;
}
