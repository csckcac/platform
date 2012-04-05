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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraStoreFactory;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.persistence.sql.SQLStoreFactory;
import org.wso2.carbon.bam.core.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryManager {

    private static final Log log = LogFactory.getLog(QueryManager.class);

    public List<String> getIndexValues(Map<String, String> credentials, String indexName) {
        return null;
    }

    public List<String> getTableColumns(Map<String, String> credentials, String tableName) {
        return null;
    }

    public List<Record> getRecords(Map<String, String> credentials, String table, String primaryKey,
                                   List<String> filterByColumns)
            throws StoreException {
        if (table != null && primaryKey != null && Utils.credentialsValid(credentials)) {
            PersistenceManager persistenceManager = new PersistenceManager();

            int tenantId = Utils.getTenantIdFromUserName(credentials.get(
                    PersistencyConstants.USER_NAME));

            if (persistenceManager.isTableExists(credentials, table)) {
                TableConfiguration tableConfiguration;
                try {
                    tableConfiguration = MetaDataManager.getInstance().
                            getTableMetaData(tenantId, table);
                } catch (ConfigurationException e) {
                    throw new StoreException("Unable to fetch table meta data..", e);
                }

                DataSourceType type = tableConfiguration.getDataSourceType();

                StoreFetcher fetcher = getStoreFetcher(type, credentials);

                if (PersistencyConstants.BASE_TABLES.equalsIgnoreCase(table.trim())) {
                    List<Record> eventRecords = fetcher.fetchRecords(
                            PersistencyConstants.EVENT_TABLE, primaryKey, filterByColumns);

                    for (Record eventRecord : eventRecords) {
                        String key = eventRecord.getKey();

                        Map<String, String> columns = eventRecord.getColumns();

                        List<Record> metaRecords = fetcher.fetchRecords(
                                PersistencyConstants.META_TABLE, key, filterByColumns);
                        if (metaRecords != null && metaRecords.size() > 0) {
                            // Only one meta record is associated with the event record
                            Record metaRecord = metaRecords.get(0);
                            columns.putAll(metaRecord.getColumns());
                        }

                        List<Record> correlationRecords = fetcher.fetchRecords(
                                PersistencyConstants.CORRELATION_TABLE, key, filterByColumns);
                        if (correlationRecords != null && correlationRecords.size() > 0) {
                            // Only one correlation record is associated with the event record
                            Record correlationRecord = correlationRecords.get(0);
                            columns.putAll(correlationRecord.getColumns());
                        }
                    }

                    return eventRecords;

                } else {

                    List<Record> result = fetcher.fetchRecords(table, primaryKey, filterByColumns);

                    return result;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Either table name, primary key or credentials invalid..");
            }
        }

        return null;
    }

    public List<Record> getRecords(Map<String, String> credentials, String table,
                                   QueryIndex index, List<String> filterByColumns)
            throws StoreException {
        if (table != null && Utils.credentialsValid(credentials)) {
            PersistenceManager persistenceManager = new PersistenceManager();

            int tenantId = Utils.getTenantIdFromUserName(credentials.get(
                    PersistencyConstants.USER_NAME));

            if (persistenceManager.isTableExists(credentials, table)) {

                TableConfiguration tableConfiguration;
                try {
                    tableConfiguration = MetaDataManager.getInstance().
                            getTableMetaData(tenantId, table);
                } catch (ConfigurationException e) {
                    throw new StoreException("Unable to fetch table meta data..", e);
                }

                DataSourceType type = tableConfiguration.getDataSourceType();

                StoreFetcher fetcher = getStoreFetcher(type, credentials);

                if (PersistencyConstants.BASE_TABLES.equalsIgnoreCase(table.trim())) {
                    List<Record> eventRecords = fetcher.fetchRecords(
                            PersistencyConstants.EVENT_TABLE, index, filterByColumns);

                    for (Record eventRecord : eventRecords) {
                        String key = eventRecord.getKey();

                        Map<String, String> columns = eventRecord.getColumns();

                        List<Record> metaRecords = fetcher.fetchRecords(
                                PersistencyConstants.META_TABLE, key, filterByColumns);
                        if (metaRecords != null && metaRecords.size() > 0) {
                            // Only one meta record is associated with the event record
                            Record metaRecord = metaRecords.get(0);
                            columns.putAll(metaRecord.getColumns());
                        }

                        List<Record> correlationRecords = fetcher.fetchRecords(
                                PersistencyConstants.CORRELATION_TABLE, key, filterByColumns);
                        if (correlationRecords != null && correlationRecords.size() > 0) {
                            // Only one correlation record is associated with the event record
                            Record correlationRecord = correlationRecords.get(0);
                            columns.putAll(correlationRecord.getColumns());
                        }
                    }

                    return eventRecords;

                } else {
                    List<Record> result = fetcher.fetchRecords(table, index, filterByColumns);

                    return result;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Either table name or credentials invalid at query manager.");
            }
        }

        return null;
    }

    public List<Record> getRecords(Map<String, String> credentials, String table,
                                   QueryIndex index, List<String> filterByColumns,
                                   int batchSize,
                                   Cursor cursor) throws StoreException {
        if (table != null && Utils.credentialsValid(credentials)) {
            PersistenceManager persistenceManager = new PersistenceManager();

            int tenantId = Utils.getTenantIdFromUserName(credentials.get(
                    PersistencyConstants.USER_NAME));

            if (persistenceManager.isTableExists(credentials, table)) {

                TableConfiguration tableConfiguration;
                try {
                    tableConfiguration = MetaDataManager.getInstance().
                            getTableMetaData(tenantId, table);
                } catch (ConfigurationException e) {
                    throw new StoreException("Unable to fetch table meta data..", e);
                }

                DataSourceType type = tableConfiguration.getDataSourceType();

                StoreFetcher fetcher = getStoreFetcher(type, credentials);

                if (PersistencyConstants.BASE_TABLES.equalsIgnoreCase(table.trim())) {
                    List<Record> eventRecords = fetcher.fetchRecords(
                            PersistencyConstants.EVENT_TABLE, index, filterByColumns,
                            batchSize, cursor);

                    for (Record eventRecord : eventRecords) {
                        String key = eventRecord.getKey();

                        Map<String, String> columns = eventRecord.getColumns();

                        List<Record> metaRecords = fetcher.fetchRecords(
                                PersistencyConstants.META_TABLE, key, filterByColumns);
                        if (metaRecords != null && metaRecords.size() > 0) {
                            // Only one meta record is associated with the event record
                            Record metaRecord = metaRecords.get(0);
                            columns.putAll(metaRecord.getColumns());
                        }

                        List<Record> correlationRecords = fetcher.fetchRecords(
                                PersistencyConstants.CORRELATION_TABLE, key, filterByColumns);
                        if (correlationRecords != null && correlationRecords.size() > 0) {
                            // Only one correlation record is associated with the event record
                            Record correlationRecord = correlationRecords.get(0);
                            columns.putAll(correlationRecord.getColumns());
                        }
                    }

                    return eventRecords;

                } else {

                    List<Record> result = fetcher.fetchRecords(table, index, filterByColumns,
                                                               batchSize, cursor);

                    return result;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Table " + table + " does not exist..");
                }
            }
        }

        return null;
    }

    public StoreFetcher getStoreFetcher(DataSourceType type, Map<String, String> credentials)
            throws StoreException {

        StoreFactory factory;
        StoreFetcher store;

        switch (type) {
            case CASSANDRA:
                factory = CassandraStoreFactory.getInstance();
                store = factory.getStoreFetcher(credentials);
                break;
            case SQL:
                factory = SQLStoreFactory.getInstance();
                store = factory.getStoreFetcher(credentials);
                break;
            default:
                throw new StoreException("Unknown data source type..");
        }

        return store;
    }

}
