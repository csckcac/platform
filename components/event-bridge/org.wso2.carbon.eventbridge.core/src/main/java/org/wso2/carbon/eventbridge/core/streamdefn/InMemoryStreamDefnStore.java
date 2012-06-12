package org.wso2.carbon.eventbridge.core.streamdefn;

import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class InMemoryStreamDefnStore extends AbstractStreamDefnStore {

    private Map<String, HashMap<String, EventStreamDefinition>> streamDefinitionStore = new ConcurrentHashMap<String, HashMap<String, EventStreamDefinition>>();

    private Map<String, HashMap<String, String>> streamIdStore = new ConcurrentHashMap<String, HashMap<String, String>>();

    @Override
    protected void saveStreamIdToStore(Credentials credentials, String streamIdKey, String streamId) throws StreamDefinitionException {

        if (!streamIdStore.containsKey(getTenantDomain(credentials))) {
            streamIdStore.put(getTenantDomain(credentials), new HashMap<String, String>());
        }
        streamIdStore.get(getTenantDomain(credentials)).put(streamIdKey, streamId);
    }

    private String getTenantDomain(Credentials credentials) {
       return MultitenantUtils.getTenantDomain(credentials.getUsername());
    }

    @Override
    protected void saveStreamDefinitionToStore(Credentials credentials, String streamId, EventStreamDefinition streamDefinition) throws StreamDefinitionException {
        if (!streamDefinitionStore.containsKey(getTenantDomain(credentials))) {
            streamDefinitionStore.put(getTenantDomain(credentials), new HashMap<String, EventStreamDefinition>());
        }
        streamDefinitionStore.get(getTenantDomain(credentials)).put(streamId, streamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(Credentials credentials, String streamIdKey) throws StreamDefinitionException {
        if (streamIdStore.get(getTenantDomain(credentials)) != null) {
            return streamIdStore.get(getTenantDomain(credentials)).get(streamIdKey);
        }
        return null;
    }

    @Override
    public EventStreamDefinition getStreamDefinitionFromStore(Credentials credentials, String streamId) throws StreamDefinitionException {
        if (streamDefinitionStore.get(getTenantDomain(credentials)) != null) {
            return streamDefinitionStore.get(getTenantDomain(credentials)).get(streamId);
        }
        return null;
    }

    @Override
    protected Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(Credentials credentials) throws StreamDefinitionException {
        HashMap<String, EventStreamDefinition> map = streamDefinitionStore.get(getTenantDomain(credentials));
        if (map != null) {
            return map.values();
        }
        return null;
    }
}
