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
package org.wso2.carbon.bam.core.persistence;

import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.IndexingTaskConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraIndexingStrategy;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexManager {

    private static IndexManager instance = new IndexManager();

    private List<IndexingTaskProvider> taskProviders;

    private IndexManager() {
        taskProviders = new ArrayList<IndexingTaskProvider>();
    }

    public static IndexManager getInstance() {
        return instance;
    }

    public Map<String, String> getIndexesOfRecord(Record record, String indexName)
            throws IndexingException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        IndexConfiguration configuration;
        try {
            MetaDataManager metaDataManager = MetaDataManager.getInstance();
            configuration = metaDataManager.getIndexMetaData(null, indexName);
        } catch (ConfigurationException e) {
            throw new IndexingException("Unable to fetch index meta data..", e);
        }

        DataSourceType type = configuration.getDataSourceType();

        switch (type) {
            case CASSANDRA:
                IndexingStrategy strategy = new CassandraIndexingStrategy();
                return strategy.getIndexValuesOfRecord(record, configuration);
            default:
                throw new IndexingException("Unknown data source type " + type.getName() + "..");
        }

    }

    public void indexData(IndexConfiguration configuration, Map<String, String> credentials,
                          String cursorName)
            throws IndexingException {

        if (!Utils.credentialsValid(credentials)) {
            return;
        }

        DataSourceType type = configuration.getDataSourceType();

        switch (type) {
            case CASSANDRA:
                indexCassandraData(configuration, credentials, cursorName);
                break;
            case SQL:
                break;
            default:
                throw new IndexingException("Unknown data source type " + type.getName() + "..");

        }
    }

    public void createIndex(IndexConfiguration configuration, Map<String, String> credentials)
            throws IndexingException {

        if (!Utils.credentialsValid(credentials)) {
            return;
        }

        DataSourceType type = configuration.getDataSourceType();

        switch (type) {
            case CASSANDRA:
                IndexingStrategy indexingStrategy = new CassandraIndexingStrategy();
                indexingStrategy.createIndex(configuration, credentials);
                break;
            default:
                throw new IndexingException("Unknown data source type " + type.getName() + "..");
        }
    }
    
    public void editIndex(IndexConfiguration configuration, Map<String, String> credentials)
            throws IndexingException {
        if (!Utils.credentialsValid(credentials)) {
            return;
        }

        DataSourceType type = configuration.getDataSourceType();

        switch (type) {
            case CASSANDRA:
                IndexingStrategy indexingStrategy = new CassandraIndexingStrategy();
                indexingStrategy.editIndex(configuration, credentials);
                break;
            default:
                throw new IndexingException("Unknown data source type " + type.getName() + "..");
        }
    }
    
    public void deleteIndex(String indexName, Map<String, String> credentials)
            throws IndexingException {
        if (!Utils.credentialsValid(credentials)) {
            return;
        }
        
        MetaDataManager metaDataManager = MetaDataManager.getInstance();

        try {
            IndexConfiguration configuration = metaDataManager.
                    getIndexMetaData(credentials, indexName);

            DataSourceType type = configuration.getDataSourceType();
            switch (type) {
                case CASSANDRA:
                    IndexingStrategy strategy = new CassandraIndexingStrategy();
                    strategy.deleteIndex(indexName, credentials);
                    break;
                default:
                    throw new IndexingException("Unknown data source type " + type.getName() +
                                                "..");
            }
        } catch (ConfigurationException e) {
            throw new IndexingException("Unable to fetch index meta data..", e);
        }

    }
    
    public List<String> getIndexValues(String indexName, String indexColumn,
                                       Map<String, String> credentials) throws IndexingException {

        if (!Utils.credentialsValid(credentials)) {
            return null;
        }

        MetaDataManager metaDataManager = MetaDataManager.getInstance();

        try {
            IndexConfiguration configuration = metaDataManager.
                    getIndexMetaData(credentials, indexName);

            DataSourceType type = configuration.getDataSourceType();
            switch (type) {
                case CASSANDRA:
                    IndexingStrategy strategy = new CassandraIndexingStrategy();
                    Map<String, List<String>> indexValues = strategy.
                            getIndexValues(indexName, credentials);
                    return indexValues.get(indexColumn);
                default:
                    throw new IndexingException("Unknown data source type " + type.getName() +
                                                "..");
            }
        } catch (ConfigurationException e) {
            throw new IndexingException("Unable to fetch index meta data..", e);
        }

    }
    
    public String[] getSubIndexValues(String indexName, String subIndex, String subIndexValue,
                                      Map<String, String> credentials) throws IndexingException {

        if (!Utils.credentialsValid(credentials)) {
            return null;
        }

        MetaDataManager metaDataManager = MetaDataManager.getInstance();

        try {
            IndexConfiguration configuration = metaDataManager.
                    getIndexMetaData(credentials, indexName);

            DataSourceType type = configuration.getDataSourceType();
            switch (type) {
                case CASSANDRA:
                    IndexingStrategy strategy = new CassandraIndexingStrategy();
                    String[] subIndexValues = strategy.getNextSubIndexValues(
                            indexName, subIndex, subIndexValue, credentials);
                    return subIndexValues;
                default:
                    throw new IndexingException("Unknown data source type " + type.getName() +
                                                "..");
            }
        } catch (ConfigurationException e) {
            throw new IndexingException("Unable to fetch index meta data..");
        }
        
    }

    public void registerIndexingTaskProvider(IndexingTaskProvider taskProvider) {
        taskProviders.add(taskProvider);
    }

    public void scheduleIndexingTask(IndexConfiguration indexConfiguration,
                                     IndexingTaskConfiguration taskConfiguration)
            throws IndexingException {

        for (IndexingTaskProvider taskProvider : taskProviders) {
            taskProvider.scheduleIndexingTask(indexConfiguration, taskConfiguration);
        }

    }

    public void unScheduleIndexingTask(IndexingTaskConfiguration taskConfiguration)
            throws IndexingException {
        
        for (IndexingTaskProvider taskProvider : taskProviders) {
            taskProvider.unscheduleIndexingTask(taskConfiguration);
        }
    }

    /*            Private helper methods            */

    private void indexCassandraData(IndexConfiguration configuration,
                                    Map<String, String> credentials, String cursorName)
            throws IndexingException {

        Cursor cursor = new Cursor(configuration.getIndexedTable(), cursorName);

        IndexingStrategy indexingStrategy = new CassandraIndexingStrategy();
        indexingStrategy.indexData(configuration, cursor, credentials);

    }

}
