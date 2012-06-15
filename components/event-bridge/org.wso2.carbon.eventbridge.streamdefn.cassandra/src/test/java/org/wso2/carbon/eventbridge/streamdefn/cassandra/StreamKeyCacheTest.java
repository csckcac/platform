package org.wso2.carbon.eventbridge.streamdefn.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Test;
import org.wso2.carbon.eventbridge.core.utils.EventBridgeUtils;

import static junit.framework.Assert.assertEquals;

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
public class StreamKeyCacheTest extends BaseCassandraSDSTest {

    @Test
    public void checkBasicOperations() {

        String actualStreamKey1 = EventBridgeUtils.constructStreamKey(streamDefinition1.getName(), streamDefinition1.getVersion());
        String actualStreamKey2 = EventBridgeUtils.constructStreamKey(streamDefinition2.getName(),
                streamDefinition2.getVersion());

        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition2);

        // get it once, now the ids should be cached
        String streamKeyFromStore1 = cassandraConnector.getStreamKeyFromStreamId(getCluster(), streamDefinition1);
        String streamKeyFromStore2 = cassandraConnector.getStreamKeyFromStreamId(getCluster(), streamDefinition2);

        //First compare before these ids are equal before checking with cache
        // If these are not equal, there is no point checking with the cache
        assertEquals(streamKeyFromStore1, actualStreamKey1);
        assertEquals(streamKeyFromStore2, actualStreamKey2);

        //Shutdown Cassandra
        EmbeddedCassandraServerHelper.stopEmbeddedCassandra();

        // Get from the Cache
        String cachedStreamKey1 = cassandraConnector.getStreamKeyFromStreamId(getCluster(), streamDefinition1);
        String cachedStreamKey2 = cassandraConnector.getStreamKeyFromStreamId(getCluster(), streamDefinition2);

        assertEquals(cachedStreamKey1, actualStreamKey1);
        assertEquals(cachedStreamKey2, actualStreamKey2);

    }
}
