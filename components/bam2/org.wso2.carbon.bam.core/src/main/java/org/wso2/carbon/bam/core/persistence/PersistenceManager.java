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
import org.wso2.carbon.bam.core.dataobjects.EventData;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraStoreFactory;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.persistence.sql.SQLStoreFactory;
import org.wso2.carbon.bam.core.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PersistenceManager {

    private static final Log log = LogFactory.getLog(PersistenceManager.class);

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void storeEvent(Map<String, String> credentials, EventData eventData)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return;

        }

        DataStore store = getDataStoreForTable(credentials, PersistencyConstants.BASE_TABLES);

        // Store three fold event data (meta, correlation, event) to three initial tables
        String timeStamp = formatter.format(new Date());
        String uuid = UUID.randomUUID().toString();

        String rowKey = timeStamp + PersistencyConstants.INDEX_DELIMITER + System.nanoTime() +
                        PersistencyConstants.INDEX_DELIMITER + uuid;

        // Note: Performance gain by starting a pseudo batch commit for Cassandra,
        // One event, will only have one commit, independent of the number of CFs and Rows being written to
        store.startBatchCommit();

        store.persistBinaryData(PersistencyConstants.META_TABLE, rowKey, eventData.getMetaData());

        store.persistBinaryData(PersistencyConstants.CORRELATION_TABLE, rowKey,
                                eventData.getCorrelationData());

        store.persistBinaryData(PersistencyConstants.EVENT_TABLE, rowKey, eventData.getEventData());

        store.endBatchCommit();

    }

    public void storeRecord(Map<String, String> credentials, String table, Record record)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return;

        }

        if (table != null && record != null) {
            DataStore store = getDataStoreForTable(credentials, table);
            store.persistData(table, record.getKey(), record.getColumns());
        }

    }

    public void storeRecords(Map<String, String> credentials, String table, List<Record> records)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return;

        }

        if (table != null && records != null) {

            DataStore store = getDataStoreForTable(credentials, table);

            store.startBatchCommit();

            for (Record record : records) {
                store.persistData(table, record.getKey(), record.getColumns());
            }

            store.endBatchCommit();
        }

    }

    public void deleteRecord(Map<String, String> credentials, String table, String recordKey)
            throws StoreException {
        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return;

        }

        if (table != null && recordKey != null) {

            DataStore store = getDataStoreForTable(credentials, table);

            store.deleteData(table, recordKey);

        }

    }

    /**
     * Creates the table in the data store and add configuration of table in registry. Needs
     * synchronization since these two actions need to be atomic. And if either of these two steps
     * fails entire transaction is rolled back and 'false' is returned. In a clustered scenario
     * a distributed synchronization lock is needed in place of in VM lock used here. Can be achieved
     * using a Zookeeper distributed lock.
     *
     * @param credentials   credentials to connect to the data store
     * @param configuration table configuration consisting details of the table to be created
     * @return
     */
    public synchronized boolean createTable(Map<String, String> credentials,
                                            TableConfiguration configuration)
            throws StoreException {
        //datastore factory --> datastore --> create table --> storeTableMetaData

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided..");
            }

            return false;

        }

        DataSourceType type = configuration.getDataSourceType();
        DataStore store = getDataStore(type, credentials);

        boolean success = store.createTable(configuration.getTableName(),
                                            configuration.getColumns());
        if (success) {
            try {

                MetaDataManager.getInstance().storeTableMetaData(credentials, configuration);

            } catch (Exception e) {
                //roll back. delete the table in the data store
                if (log.isDebugEnabled()) {
                    log.debug("Adding table meta data failed for table : " +
                              configuration.getTableName() + ".");
                }
                return false;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Table creation failed at database for table : " +
                          configuration.getTableName() + ".");
            }
        }

        return true;
    }

    public boolean deleteTable(Map<String, String> credentials, String tableName)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided..");
            }
        }

        TableConfiguration configuration;
        try {
            configuration = MetaDataManager.getInstance().
                    getTableMetaData(credentials.get(PersistencyConstants.USER_NAME), tableName);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch table meta data..", e);
        }

        if (configuration == null) {
            throw new StoreException("Table meta data not present for table " + tableName + "..");
        }

        DataSourceType dataSourceType = configuration.getDataSourceType();
        DataStore dataStore = getDataStore(dataSourceType, credentials);

        // Delete table on data store
        boolean success = dataStore.deleteTable(tableName);

        // Delete table meta data from memory and registry
        if (success) {
            try {

                MetaDataManager.getInstance().deleteTableMetaData(credentials, tableName);

            } catch (Exception e) {
                log.error("Error while deleting the table..", e);
                return false;
            }
        }

        return true;

    }

    public boolean isTableExists(Map<String, String> credentials, String tableName)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided..");
            }

            return false;

        }

        // Make sure the data stores are initialized before checking table existence
        initializeDataStores(credentials);

        TableConfiguration configuration;
        try {
            configuration = MetaDataManager.getInstance().
                    getTableMetaData(credentials.get(PersistencyConstants.USER_NAME), tableName);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch table meta data..", e);
        }

        if (configuration != null) {
            return true;
        } else {
            // If table meta data doesn't exist in memory or registry, directly check in the data
            // source whether the given table exist in the data source. This shouldn't happen since
            // when ever a table is created it's meta data should be persisted as well. This
            // condition indicates this hasn't happened.
            for (DataSourceType type : DataSourceType.values()) {
                DataStore store = getDataStore(type, credentials);
                boolean tableExists = store.isTableExists(tableName);

                if (tableExists) {
                    log.warn("{{Table meta data is not in synchronization with database for table "
                             + tableName + " }}");
                    return true;
/*                    throw new StoreException("Table meta data is not in synchronization" +
                                             " with database..");*/
                }
            }
        }

        return false;
    }

    public DataStore getDataStoreForTable(Map<String, String> credentials, String table)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return null;

        }

        int tenantId = Utils.getTenantIdFromUserName(credentials.get(
                PersistencyConstants.USER_NAME));

        // Explicitly initialize the cassandra data source before getting meta data in case this
        // is the very first time so that Cassandra key space has not yet been created..

        getDataStore(DataSourceType.CASSANDRA, credentials);

        TableConfiguration configuration;
        try {
            configuration = MetaDataManager.getInstance().
                    getTableMetaData(tenantId, table);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch table meta data..", e);
        }

        if (configuration != null) {
            DataStore store = getDataStore(configuration.getDataSourceType(), credentials);
            return store;
        } else if (table.equals(PersistencyConstants.BASE_TABLES)) {
            return getDefaultDataStore(credentials);
        } else {
            throw new StoreException("Table '" + table + "' is not present in the database..");
        }

    }

    public DataStore getDataStore(DataSourceType type, Map<String, String> credentials)
            throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return null;

        }

        StoreFactory factory;
        DataStore store = null;

        switch (type) {
            case CASSANDRA:
                factory = CassandraStoreFactory.getInstance();
                store = factory.getDataStore(credentials);
                break;
            case SQL:
                factory = SQLStoreFactory.getInstance();
                store = factory.getDataStore(credentials);
                break;
        }

        return store;
    }

    public DataStore getDefaultDataStore(Map<String, String> credentials) throws StoreException {

        if (!Utils.credentialsValid(credentials)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials invalid or not yet provided. Discarding data..");
            }

            return null;

        }

        StoreFactory factory = CassandraStoreFactory.getInstance();
        return factory.getDataStore(credentials);
    }

    public DataSourceType[] getDataSourceTypes() {

        return DataSourceType.values();

    }

    private void initializeDataStores(Map<String, String> credentials) throws StoreException {
        for (DataSourceType type : DataSourceType.values()) {
            getDataStore(type, credentials);
        }
    }


}
