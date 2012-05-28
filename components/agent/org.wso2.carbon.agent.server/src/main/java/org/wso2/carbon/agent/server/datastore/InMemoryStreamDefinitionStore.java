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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The in memory implementation of the Event Stream definition Store
 */
public class InMemoryStreamDefinitionStore extends StreamDefinitionStore {

    private Map<String, HashMap<String, EventStreamDefinition>> streamDefinitionStore = new HashMap<String, HashMap<String, EventStreamDefinition>>();
    private Map<String, HashMap<String, String>> streamIdStore = new HashMap<String, HashMap<String, String>>();

    @Override
    protected void saveStreamIdToStore(String domainName, String streamIdKey, String streamId) {
        if (!streamIdStore.containsKey(domainName)) {
            streamIdStore.put(domainName, new HashMap<String, String>());
        }
        streamIdStore.get(domainName).put(streamIdKey, streamId);
    }

    @Override
    protected void saveStreamDefinitionToStore(String domainName,
                                               String streamId,
                                               EventStreamDefinition streamDefinition) {
        if (!streamDefinitionStore.containsKey(domainName)) {
            streamDefinitionStore.put(domainName, new HashMap<String, EventStreamDefinition>());
        }
        streamDefinitionStore.get(domainName).put(streamId, streamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(String domainName, String streamIdKey) {
        if (streamIdStore.get(domainName) != null) {
            return streamIdStore.get(domainName).get(streamIdKey);
        }
        return null;
    }


    public EventStreamDefinition getStreamDefinitionFromStore(String domainName,
                                                              String streamId) {
        if (streamDefinitionStore.get(domainName) != null) {
            return streamDefinitionStore.get(domainName).get(streamId);
        }
        return null;

    }

    public Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(String domainName) {
        HashMap<String, EventStreamDefinition> map = streamDefinitionStore.get(domainName);
        if (map != null) {
            return map.values();
        }
        return null;

    }

}
