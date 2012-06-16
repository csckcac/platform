package org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.hector.api.Cluster;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.eventbridge.commons.Credentials;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.internal.util.Utils;

import java.util.concurrent.TimeUnit;

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
public class ClusterFactory {

    private static LoadingCache<Credentials, Cluster> clusterLoadingCache;

    private ClusterFactory() {

    }

    private static void init() {
        synchronized (ClusterFactory.class) {
            if (clusterLoadingCache != null) {
                return;
            }
            clusterLoadingCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<Credentials, Cluster>() {

                        @Override
                        public Cluster load(Credentials credentials) throws Exception {
                            ClusterInformation clusterInformation = new ClusterInformation(credentials.getUsername(),
                                    credentials.getPassword());
                            Cluster cluster = Utils.getDataAccessService().getCluster(clusterInformation);
                            initCassandraKeySpaces(cluster);
                            return cluster;
                        }
                    });
        }
    }

    public static void initCassandraKeySpaces(Cluster cluster) {
        CassandraConnector connector = Utils.getCassandraConnector();
        connector.createKeySpaceIfNotExisting(cluster, CassandraConnector.BAM_META_KEYSPACE);

        connector.createKeySpaceIfNotExisting(cluster, CassandraConnector.BAM_EVENT_DATA_KEYSPACE);


        // Create BAM meta column families if not existing
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_ID_CF);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_ID_KEY_CF);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAMID_TO_STREAM_ID_KEY);
        connector.createColumnFamily(cluster, CassandraConnector.BAM_META_KEYSPACE,
                CassandraConnector.BAM_META_STREAM_DEF_CF);
    }



    public static Cluster getCluster(Credentials credentials) {
        init();
        return clusterLoadingCache.getUnchecked(credentials);
    }
}
