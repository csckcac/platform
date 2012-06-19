package org.wso2.carbon.eventbridge.streamdefn.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Test;

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
public class StreamIdCacheTest extends BaseCassandraSDSTest {


    @Test
    public void checkBasicCacheOperations() {

        String actualStreamId1 = streamDefinition1.getStreamId();
        System.out.println("Actual Stream Id 1 : " + actualStreamId1);
        String actualStreamId2 = streamDefinition2.getStreamId();
        System.out.println("Actual Stream Id 2 : " + actualStreamId2);

        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition1);
        cassandraConnector.saveStreamIdToStore(getCluster(), streamDefinition2);

        // get it once, now the ids should be cached
        String streamIdFromStore1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);
        String streamIdFromStore2 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition2);

        //First compare before these ids are equal before checking with cache
        // If these are not equal, there is no point checking with the cache
        assertEquals(streamIdFromStore1, actualStreamId1);
        assertEquals(streamIdFromStore2, actualStreamId2);

        //Shutdown Cassandra
        EmbeddedCassandraServerHelper.stopEmbeddedCassandra();

        // Get from the Cache
        String cachedStreamId1 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition1);
        String cachedStreamId2 = cassandraConnector.getStreamIdFromStore(getCluster(), streamDefinition2);

        assertEquals(cachedStreamId1, actualStreamId1);
        assertEquals(cachedStreamId2, actualStreamId2);
    }




}
