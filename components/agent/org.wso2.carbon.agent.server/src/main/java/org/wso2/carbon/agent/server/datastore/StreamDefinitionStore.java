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
package org.wso2.carbon.agent.server.datastore;

import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.server.EventConverter;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;

import java.util.Collection;

/**
 * The Event Stream Definition Store interface
 * Used to persist Event Stream Definitions at the Agent Server
 */
public abstract class StreamDefinitionStore {

    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String name, String version)
            throws StreamDefinitionNotFoundException {
        String streamId = getStreamIdFromStore(domainName, constructNameVersionKey(name, version));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + constructNameVersionKey(name, version));
        }
        return getStreamDefinition(domainName, streamId);
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String streamId)
            throws StreamDefinitionNotFoundException {
        EventStreamDefinition eventStreamDefinition = getStreamDefinitionFromStore(domainName, streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    public void saveStreamDefinition(String domainName,
                                     EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException {
        EventStreamDefinition existingDefinition = null;
        try {
            existingDefinition = getStreamDefinition(domainName, eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
        } catch (StreamDefinitionNotFoundException e) {
            saveStreamIdToStore(domainName, constructNameVersionKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion()), eventStreamDefinition.getStreamId());
            saveStreamDefinitionToStore(domainName, eventStreamDefinition.getStreamId(), eventStreamDefinition);
            return;
        }
        if (!existingDefinition.equals(eventStreamDefinition)) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Another Stream with same name and version exist :" + EventConverter.convertToJson(existingDefinition));
        }
    }

    public Collection<EventStreamDefinition> getAllStreamDefinitions(String domainName) {
        return getAllStreamDefinitionsFromStore(domainName);
    }

    public String getStreamId(String domainName, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException {
        String streamId = getStreamIdFromStore(domainName, constructNameVersionKey(streamName, streamVersion));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No stream id found for " + streamId + " " + streamVersion);
        }
        return streamId;
    }

    protected abstract void saveStreamIdToStore(String domainName, String streamIdKey,
                                                String streamId);

    protected abstract void saveStreamDefinitionToStore(String domainName, String streamId,
                                                        EventStreamDefinition streamDefinition);


    protected abstract String getStreamIdFromStore(String domainName, String streamIdKey);

    public abstract EventStreamDefinition getStreamDefinitionFromStore(String domainName,
                                                                       String streamId);

    protected abstract Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(
            String domainName);
}
