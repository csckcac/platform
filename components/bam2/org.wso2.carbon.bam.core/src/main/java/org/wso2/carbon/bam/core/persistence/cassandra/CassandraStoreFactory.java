/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.core.persistence.cassandra;

import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.persistence.DataStore;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.StoreFactory;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.StoreFetcher;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraStoreFactory implements StoreFactory {

    private static CassandraStoreFactory instance = new CassandraStoreFactory();

    private Map<Integer, List<DataStore>> dataStorePool;
    
    private Map<Integer, List<StoreFetcher>> storeFetcherPool;

    private CassandraStoreFactory() {
        dataStorePool = new HashMap<Integer, List<DataStore>>();
        storeFetcherPool = new HashMap<Integer, List<StoreFetcher>>();
    }

    public static StoreFactory getInstance() {
        return instance;
    }

    @Override
    public DataStore getDataStore(Map<String, String> credentials)
            throws StoreException {
        int tenantId = Utils.getTenantIdFromUserName(
                credentials.get(PersistencyConstants.USER_NAME));
        List<DataStore> tenantStores = dataStorePool.get(tenantId);

        if (tenantStores != null) {
            for (DataStore store : tenantStores) {
                if (store.getDataSourceType().equals(DataSourceType.CASSANDRA)) {
                    return store;
                }
            }
        }

        DataStore store;
        synchronized (this) {

            // Again check if the store has been initialized by another thread. If not proceed to
            // create a new data store. This is a form of double checked locking. But usual pitfalls
            // of DCL doesn't apply since it's guaranteed the store is fully initialized before
            // being added to the dataStorePool structure and dataStorePool structure being thread
            // safe so that no partially created store would be visible to any thread. This is also
            // used since concurrent schema modifications are not allowed in Cassandra.
            tenantStores = dataStorePool.get(tenantId);

            if (tenantStores != null) {
                for (DataStore tenantStore : tenantStores) {
                    if (tenantStore.getDataSourceType().equals(DataSourceType.CASSANDRA)) {
                        return tenantStore;
                    }
                }
            } else {
                tenantStores = new ArrayList<DataStore>();
            }

            store = new CassandraDataStore();
            store.initialize(credentials);

/*            MetaDataManager metaDataManager = MetaDataManager.getInstance();

            TableConfiguration eventCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.EVENT_TABLE, null, DataSourceType.CASSANDRA);
            TableConfiguration metaCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.META_TABLE, null, DataSourceType.CASSANDRA);
            TableConfiguration correlationCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.CORRELATION_TABLE, null, DataSourceType.CASSANDRA);
            TableConfiguration baseCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.BASE_TABLES, null, DataSourceType.CASSANDRA);

            try {
                metaDataManager.storeTableMetaData(tenantId, eventCfConfiguration);
                metaDataManager.storeTableMetaData(tenantId, metaCfConfiguration);
                metaDataManager.storeTableMetaData(tenantId, correlationCfConfiguration);
                metaDataManager.storeTableMetaData(tenantId, baseCfConfiguration);
            } catch (ConfigurationException e) {
                // roll back. delete meta data delete tables in the database
                throw new StoreException("Unable to initialize store..", e);
            }*/

            tenantStores.add(store);
            dataStorePool.put(tenantId, tenantStores);
        }

        return store;
    }

    @Override
    public StoreFetcher getStoreFetcher(Map<String, String> credentials) throws StoreException {
        int tenantId = Utils.getTenantIdFromUserName(
                credentials.get(PersistencyConstants.USER_NAME));
        List<StoreFetcher> storeFetchers = storeFetcherPool.get(tenantId);

        if (storeFetchers != null) {
            for (StoreFetcher storeFetcher : storeFetchers) {
                if (storeFetcher.getDataSourceType().equals(DataSourceType.CASSANDRA)) {
                    return storeFetcher;
                }
            }
        } 

        StoreFetcher fetcher = null;
        synchronized (this) {
            storeFetchers = storeFetcherPool.get(tenantId);

            if (storeFetchers != null) {
                for (StoreFetcher storeFetcher : storeFetchers) {
                    if (storeFetcher.getDataSourceType().equals(DataSourceType.CASSANDRA)) {
                        return storeFetcher;
                    }
                }
            } else {
                storeFetchers = new ArrayList<StoreFetcher>();

                fetcher = new CassandraStoreFetcher();
                fetcher.initialize(credentials);
                
                storeFetchers.add(fetcher);
                
                storeFetcherPool.put(tenantId, storeFetchers);

            }
        }
        
        return fetcher;
        
    }

}
