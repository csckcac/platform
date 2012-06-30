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
package org.wso2.carbon.databridge.core.definitionstore;

import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The in memory implementation of the Event Stream definition Store
 */
public class InMemoryStreamDefinitionStore extends
        AbstractStreamDefinitionStore {

    private Map<String, HashMap<String, StreamDefinition>> streamDefinitionStore = new HashMap<String, HashMap<String, StreamDefinition>>();
    private Map<String, HashMap<String, String>> streamIdStore = new HashMap<String, HashMap<String, String>>();

    @Override
    protected void saveStreamIdToStore(Credentials credentials, String streamIdKey, String streamId)
            throws StreamDefinitionStoreException {
        if (!streamIdStore.containsKey(credentials.getDomainName())) {
            streamIdStore.put(credentials.getDomainName(), new HashMap<String, String>());
        }
        streamIdStore.get(credentials.getDomainName()).put(streamIdKey, streamId);
    }

    @Override
    protected void saveStreamDefinitionToStore(Credentials credentials,
                                               String streamId,
                                               StreamDefinition streamDefinition)
            throws StreamDefinitionStoreException {
        if (!streamDefinitionStore.containsKey(credentials.getDomainName())) {
            streamDefinitionStore.put(credentials.getDomainName(), new HashMap<String, StreamDefinition>());
        }
        streamDefinitionStore.get(credentials.getDomainName()).put(streamId, streamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(Credentials credentials, String streamIdKey)
            throws StreamDefinitionStoreException {
        if (streamIdStore.get(credentials.getDomainName()) != null) {
            return streamIdStore.get(credentials.getDomainName()).get(streamIdKey);
        }
        return null;
    }


    public StreamDefinition getStreamDefinitionFromStore(Credentials credentials,
                                                              String streamId) throws StreamDefinitionStoreException {
        if (streamDefinitionStore.get(credentials.getDomainName()) != null) {
            return streamDefinitionStore.get(credentials.getDomainName()).get(streamId);
        }
        return null;

    }

    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore(
            Credentials credentials) {
        HashMap<String, StreamDefinition> map = streamDefinitionStore.get(credentials.getDomainName());
        if (map != null) {
            return map.values();
        }
        return null;

    }

}
