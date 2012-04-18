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
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryStreamDefinitionStore implements StreamDefinitionStore {

    private Map<String, HashMap<String, String>> streamIdMap = new HashMap<String, HashMap<String, String>>();
    private Map<String, HashMap<String, EventStreamDefinition>> streamDefinitionMap = new HashMap<String, HashMap<String, EventStreamDefinition>>();

    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }

    @Override
    public boolean containsStreamDefinition(String domainName, String name,
                                            String version) {
        return streamIdMap.containsKey(domainName) && streamIdMap.get(domainName).containsKey(constructNameVersionKey(name, version));
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String name,
                                                     String version)
            throws StreamDefinitionNotFoundException {
        String streamId = getStreamId(domainName, name, version);
        return getStreamDefinition(domainName, streamId);
    }

    @Override
    public void saveStreamDefinition(String domainName,
                                     EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException {
        if (!streamIdMap.containsKey(domainName)) {
            streamIdMap.put(domainName, new HashMap<String, String>());
            streamDefinitionMap.put(domainName, new HashMap<String, EventStreamDefinition>());
        }
        String key = constructNameVersionKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
        EventStreamDefinition existingDefinition = streamDefinitionMap.get(domainName).get(key);
        if (existingDefinition != null) {
            if (!existingDefinition.equals(eventStreamDefinition)) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Another Stream with same name and version exist :" + EventConverter.convertToJson(existingDefinition));
            }
        } else {
            streamIdMap.get(domainName).put(key, eventStreamDefinition.getStreamId());
        }
        streamDefinitionMap.get(domainName).put(eventStreamDefinition.getStreamId(), eventStreamDefinition);
    }

    public EventStreamDefinition getStreamDefinition(String domainName, String streamId)
            throws StreamDefinitionNotFoundException {
        EventStreamDefinition eventStreamDefinition = streamDefinitionMap.get(domainName).get(streamId);
        if (eventStreamDefinition == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    public Collection<EventStreamDefinition> getAllStreamDefinitions(String domainName)
            throws StreamDefinitionNotFoundException {
        HashMap<String, EventStreamDefinition> map = streamDefinitionMap.get(domainName);
        if (map == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + domainName);
        }
        return map.values();
    }

    @Override
    public String getStreamId(String domainName, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException {
        HashMap<String, String> map = streamIdMap.get(domainName);
        if (map == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + domainName);
        }
        String streamId = map.get(constructNameVersionKey(streamName, streamVersion));
        if (streamId == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamName + " " + streamVersion);
        }
        return streamId;

    }

    @Override
    public List<EventStreamDefinition> getStreamDefinition(String domainName)
            throws StreamDefinitionNotFoundException {
        HashMap<String, EventStreamDefinition> map = streamDefinitionMap.get(domainName);
        if (map == null) {
            throw new StreamDefinitionNotFoundException("No definitions exist for " + domainName);
        }
        return new ArrayList<EventStreamDefinition>(map.values());
    }

}
