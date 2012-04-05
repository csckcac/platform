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
import org.wso2.carbon.bam.core.configurations.Granularity;
import org.wso2.carbon.bam.core.configurations.MetaDataPersistor;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.IndexUsageProvider;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.internal.ServiceHolder;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraCFConfiguration;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraIndexConfiguration;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraMetaDataPersistor;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetaDataManager {

    private static final int INITIAL_DELAY = 120 * 1000; //2  minute

    private static final int PERIOD = 60 * 1000; // 1 minute

    private static MetaDataManager instance = new MetaDataManager();

    private final ScheduledExecutorService updaterPool = Executors.
            newSingleThreadScheduledExecutor();

    private Map<Integer, List<IndexConfiguration>> indexConfigurations;

    private Map<Integer, List<TableConfiguration>> tableConfigurations;

    private Map<Integer, IndexUsageProvider> indexUsageProviderMap;

    private List<MetaDataPersistor> metaDataPersistors =
            new ArrayList<MetaDataPersistor>();

    private static final Log log = LogFactory.getLog(MetaDataManager.class);

    private MetaDataManager() {
        indexConfigurations = new ConcurrentHashMap<Integer, List<IndexConfiguration>>();
        tableConfigurations = new ConcurrentHashMap<Integer, List<TableConfiguration>>();
        indexUsageProviderMap = new ConcurrentHashMap<Integer, IndexUsageProvider>();

        metaDataPersistors.add(new CassandraMetaDataPersistor());

/*        updaterPool.scheduleAtFixedRate(new MetaDataSynchronizer(), INITIAL_DELAY, PERIOD,
                                        TimeUnit.MILLISECONDS);*/

    }

    public static MetaDataManager getInstance() {
        return instance;
    }

    /**
     * Get all index meta data for this tenant. If information is not found in memory synchronizes
     * from registry and return meta data.
     *
     * @param credentials
     * @return index meta data or null if no information is present in either memory or registry
     * @throws ConfigurationException
     */
    public List<IndexConfiguration> getAllIndexMetaData(Map<String, String> credentials)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        List<IndexConfiguration> configurations = indexConfigurations.get(tenantId);

        if (configurations == null) {
            synchronizeIndexMetaDataForTenant(tenantId);
            configurations = indexConfigurations.get(tenantId);
        }

        List<IndexConfiguration> clone = new ArrayList<IndexConfiguration>();
        for (IndexConfiguration configuration : configurations) {
            clone.add(cloneIndexMetaData(configuration));
        }

        return clone;

    }

    /**
     * Get meta data details about indexes defined on a table. If information is not found in memory
     * synchronizes from registry and return meta data.
     *
     * @param credentials
     * @param table
     * @return list of index meta data. Empty list if no indexes are defined on this table.
     * @throws ConfigurationException
     */
    public List<IndexConfiguration> getIndexMetaDataOfTable(Map<String, String> credentials,
                                                            String table)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        List<IndexConfiguration> configurations = indexConfigurations.get(tenantId);

        if (configurations == null) {
            synchronizeIndexMetaDataForTenant(tenantId);
            configurations = indexConfigurations.get(tenantId);
        }

        List<IndexConfiguration> indexesOfTable = new ArrayList<IndexConfiguration>();
        if (configurations != null) {
            for (IndexConfiguration configuration : configurations) {
                if (configuration.getIndexedTable().equals(table)) {
                    indexesOfTable.add(configuration);
                }
            }
        }

        List<IndexConfiguration> clone = new ArrayList<IndexConfiguration>();
        for (IndexConfiguration indexConfiguration : indexesOfTable) {
            clone.add(cloneIndexMetaData(indexConfiguration));
        }

        return clone;
    }

    /**
     * Get meta data for the given index. If meta data is not present in the memory synchronizes
     * from registry and return.
     *
     * @param credentials
     * @param indexName
     * @return index meta data or null if no information is present in either memory or registry
     * @throws ConfigurationException
     */
    public IndexConfiguration getIndexMetaData(Map<String, String> credentials, String indexName)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        List<IndexConfiguration> configurations = indexConfigurations.get(tenantId);

        if (configurations == null) {
            synchronizeIndexMetaDataForTenant(tenantId);
            configurations = indexConfigurations.get(tenantId);
        }

        if (configurations != null) {
            for (IndexConfiguration configuration : configurations) {
                if (configuration.getIndexName().equals(indexName)) {
                    return cloneIndexMetaData(configuration);
                }
            }
        }

        return null;
    }

    public int[] getAllTenantsWithDefinedIndexes() throws ConfigurationException {
        if (metaDataPersistors.size() > 0) {
            MetaDataPersistor persistor = metaDataPersistors.get(0);

            return persistor.getAllTenantsWithDefinedIndexes();
        }

        return null;
    }

    /**
     * Get all meta data about tables belonging to the given tenant. If not present in the memory
     * synchronizes with registry and return meta data.
     *
     * @param credentials
     * @return the table meta data or null if there is no information available either in memory
     *         or in registry about this tenant
     * @throws ConfigurationException
     */
    public List<TableConfiguration> getAllTableMetaData(Map<String, String> credentials)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

/*        List<TableConfiguration> configurations = tableConfigurations.get(tenantId);
        // Synchronize from registry and try again
        if (configurations == null) {
            synchronizeTableMetaDataForTenant(tenantId);
            configurations = tableConfigurations.get(tenantId);
        }*/

        List<TableConfiguration> configurations = null;
        List<TableConfiguration> allCfConfigurations = new ArrayList<TableConfiguration>();
        for (MetaDataPersistor persistor : metaDataPersistors) {

            configurations = persistor.getAllTableMetaData(Utils.getConnectionParameters(tenantId));
            allCfConfigurations.addAll(configurations);
        }

        List<TableConfiguration> clone = new ArrayList<TableConfiguration>();
        for (TableConfiguration configuration : allCfConfigurations) {
            clone.add(cloneTableMetaData(configuration));
        }

        return clone;

    }

    /**
     * Get meta data about given table belonging to the tenant.
     *
     * @param tenantId
     * @param tableName
     * @return the table meta data or null if no information is present in either memory or registry
     * @throws ConfigurationException
     */
    public TableConfiguration getTableMetaData(int tenantId, String tableName)
            throws ConfigurationException {

        List<TableConfiguration> configurations = tableConfigurations.get(tenantId);

        // Meta data for this tenant is not in the memory. Synchronize it from database and try
        // again.
/*        log.info("Table configurations for tenant " + tenantId);
        if (configurations == null) {

            // Create and add TABLE_INFO meta data explicitly at start up which holds the meta data
            // for tables itself avoiding an infinite recursion scenario..
            configurations = new ArrayList<TableConfiguration>();
            TableConfiguration tableInfoCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.TABLE_INFO_TABLE, null, DataSourceType.CASSANDRA);
            tableInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);
            configurations.add(tableInfoCfConfiguration);

            tableConfigurations.put(tenantId, configurations);

            synchronizeTableMetaDataForTenant(tenantId);
            configurations = tableConfigurations.get(tenantId);

            log.info("###############Synchronized table configurations for tenant " + tenantId +
            "####################");

        }

        if (configurations != null) {

            log.info("############Table configurations size : " + configurations.size() + "########");

            for (TableConfiguration configuration : configurations) {
                log.info("Table configuration : " + configuration.getTableName());
                if (configuration.getTableName().equals(tableName)) {
                    return cloneTableMetaData(configuration);
                }
            }
        }*/

        if (tableName.trim().equalsIgnoreCase(PersistencyConstants.TABLE_INFO_TABLE)) {
            TableConfiguration tableInfoCfConfiguration = new CassandraCFConfiguration(
                    PersistencyConstants.TABLE_INFO_TABLE, null, DataSourceType.CASSANDRA);
            tableInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);
            configurations.add(tableInfoCfConfiguration);

            return tableInfoCfConfiguration;
        }

        List<TableConfiguration> allCfConfigurations = new ArrayList<TableConfiguration>();
        for (MetaDataPersistor persistor : metaDataPersistors) {

            configurations = persistor.getAllTableMetaData(Utils.getConnectionParameters(tenantId));
            allCfConfigurations.addAll(configurations);
        }

        if (allCfConfigurations != null) {

            for (TableConfiguration configuration : allCfConfigurations) {

                if (log.isDebugEnabled()) {
                    log.info("Table configuration : " + configuration.getTableName());
                }

                if (configuration.getTableName().equals(tableName)) {
                    return cloneTableMetaData(configuration);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Returning null meta data for table : " + tableName + " for tenant : " +
                     tenantId);
        }

        return null;
    }

    /**
     * Get meta data about given table belonging to the tenant to which the given user belongs to.
     *
     * @param userName  the user name including tenant domain
     * @param tableName
     * @return the table meta data or null if no information is present in either memory or registry
     * @throws ConfigurationException
     */
    public TableConfiguration getTableMetaData(String userName, String tableName)
            throws ConfigurationException {
        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new ConfigurationException("Unable to get tenant information from user name '" +
                                             userName + "'..", e);
        }

        return getTableMetaData(tenantId, tableName);
    }

    public void registerIndexUsageProvider(int tenantId, IndexUsageProvider provider) {
        indexUsageProviderMap.put(tenantId, provider);
    }

    /**
     * Fetches meta data for particular cursor for given tenant. Cursor meta data needs to be fetched
     * without any delay for proper functioning of dependent components. So cursor meta data are
     * fetched from registry every time. It is expected this API call is not very frequent. Ideally
     * this shouldn't hurt even so if registry caching is working properly.
     *
     * @param credentials
     * @param table       the table which the cursor belongs to
     * @param cursorName  name of the cursor
     * @return
     * @throws ConfigurationException
     */
    public Cursor getCursorMetaData(Map<String, String> credentials, String table,
                                    String cursorName)
            throws ConfigurationException {

        if (metaDataPersistors.size() > 0) {
            MetaDataPersistor persistor = metaDataPersistors.get(0);

            Cursor cursor = persistor.getCursorMetaData(credentials, table, cursorName);
            return cursor;
        }

        return null;
    }

    /**
     * Fetches all cursor related meta data for this tenant. Cursor meta data needs to be fetched
     * without any delay for proper functioning of dependent components. So cursor meta data are
     * fetched from registry every time. It is expected this API call is not very frequent. Ideally
     * this shouldn't hurt even so if registry caching is working properly.
     *
     * @param credentials
     * @return
     * @throws ConfigurationException
     */
    public List<Cursor> getAllCursorMetaData(Map<String, String> credentials)
            throws ConfigurationException {
        if (metaDataPersistors.size() > 0) {
            MetaDataPersistor persistor = metaDataPersistors.get(0);

            return persistor.getAllCursorMetaData(credentials);
        }

        return null;
    }

    public void storeCursorMetaData(Map<String, String> credentials, Cursor cursor)
            throws ConfigurationException {
        if (metaDataPersistors.size() > 0) {
            MetaDataPersistor persistor = metaDataPersistors.get(0);
            persistor.persistCursorMetaData(credentials, cursor);

        }

    }

    public void deleteCursorMetaData(Map<String, String> credentials, Cursor cursor)
            throws ConfigurationException {
        if (metaDataPersistors.size() > 0) {
            MetaDataPersistor persistor = metaDataPersistors.get(0);
            persistor.deleteCursorMetaData(credentials, cursor);
        }
    }

    public void storeTableMetaData(Map<String, String> credentials,
                                   TableConfiguration configuration)
            throws ConfigurationException {

        MetaDataPersistor persistor = getMetaDataPersistor(configuration.getDataSourceType());
        persistor.persistTableMetaData(credentials, configuration);

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        List<TableConfiguration> tenantTables = tableConfigurations.get(tenantId);
        if (tenantTables == null) {
            tenantTables = new ArrayList<TableConfiguration>();
        }

        tenantTables.add(configuration);
        tableConfigurations.put(tenantId, tenantTables);

    }

    // This method is specific for adding meta data for TABLE_INFO table in which meta data for
    // tables themselves are stored
    public void addTableMetaDataForTenant(int tenantId, TableConfiguration configuration) {

        List<TableConfiguration> tenantTables = tableConfigurations.get(tenantId);
        if (tenantTables == null) {
            tenantTables = new ArrayList<TableConfiguration>();
        }

        tenantTables.add(configuration);
        tableConfigurations.put(tenantId, tenantTables);
        tenantTables = tableConfigurations.get(tenantId);

    }

    public void deleteTableMetaData(Map<String, String> credentials, String tableName)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        TableConfiguration tableConfiguration = getTableMetaData(tenantId, tableName);

        if (tableConfiguration != null) {
            MetaDataPersistor persistor = getMetaDataPersistor(
                    tableConfiguration.getDataSourceType());
            persistor.deleteTableMetaData(credentials, tableName);

            synchronizeTableMetaDataForTenant(tenantId);
        }

    }

    public void storeIndexMetaData(Map<String, String> credentials,
                                   IndexConfiguration configuration) throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        String indexedTable = configuration.getIndexedTable();
        TableConfiguration tableConfiguration = getTableMetaData(tenantId, indexedTable);

        if (tableConfiguration != null) {
            MetaDataPersistor persistor = getMetaDataPersistor(
                    tableConfiguration.getDataSourceType());
            persistor.persistIndexMetaData(credentials, configuration);

            List<IndexConfiguration> tenantIndexes = indexConfigurations.get(tenantId);

            if (tenantIndexes == null) {
                tenantIndexes = new ArrayList<IndexConfiguration>();
            }

            tenantIndexes.add(configuration);
            indexConfigurations.put(tenantId, tenantIndexes);
        } else {
            throw new ConfigurationException("Table " + indexedTable + " for which the index " +
                                             configuration.getIndexName() +
                                             " is defined is not found in the data source..");
        }

    }

    public void deleteIndexMetaData(Map<String, String> credentials, String indexName)
            throws ConfigurationException {

        IndexConfiguration indexConfiguration = getIndexMetaData(credentials, indexName);

        if (indexConfiguration != null) {

            int tenantId;
            try {
                tenantId = Utils.getTenantIdFromUserName(
                        credentials.get(PersistencyConstants.USER_NAME));
            } catch (StoreException e) {
                throw new ConfigurationException("Unable to obtain tenant information", e);
            }

            MetaDataPersistor persistor = getMetaDataPersistor(
                    indexConfiguration.getDataSourceType());
            persistor.deleteIndexMetaData(credentials, indexName);

            synchronizeIndexMetaDataForTenant(tenantId);
        }

    }

    public MetaDataPersistor getMetaDataPersistor(DataSourceType type)
            throws ConfigurationException {

        MetaDataPersistor persistor;
        switch (type) {
            case CASSANDRA:
                persistor = new CassandraMetaDataPersistor();
                break;
            case SQL:
                persistor = null;
                break;
            default:
                throw new ConfigurationException("Unknown data source type " + type.name() + "..");
        }

        return persistor;
    }

    public TableConfiguration createTableMetaData(String table, List<String> columns,
                                                  DataSourceType dataSourceType,
                                                  boolean autoGenerated)
            throws ConfigurationException {

        if (dataSourceType != null) {
            switch (dataSourceType) {
                case CASSANDRA:
                    TableConfiguration configuration = new CassandraCFConfiguration(table, columns,
                                                                                    dataSourceType);
                    configuration.setAutoGenerated(autoGenerated);

                    return configuration;
                default:
                    throw new ConfigurationException("Unknown data source type " +
                                                     dataSourceType.getName() + "..");
            }
        } else {
            throw new ConfigurationException("Data source type must not be empty..");
        }

    }

    public TableConfiguration createTableMetaData(String table, Record record,
                                                  DataSourceType dataSourceType,
                                                  boolean autoGenerated)
            throws ConfigurationException {

        List<String> columns = null;

        if (record != null) {
            Map<String, String> columnMap = record.getColumns();

            if (columnMap != null) {

                columns = new ArrayList<String>();
                Set<String> columnNames = columnMap.keySet();

                for (String columnName : columnNames) {
                    columns.add(columnName);
                }
            }
        }

        return createTableMetaData(table, columns, dataSourceType, autoGenerated);

    }

    public TableConfiguration cloneTableMetaData(TableConfiguration configuration)
            throws ConfigurationException {

        DataSourceType dataSourceType = configuration.getDataSourceType();

        switch (dataSourceType) {
            case CASSANDRA:
                CassandraCFConfiguration config = (CassandraCFConfiguration) configuration;

                List<String> clonedColumns = null;
                if (config.getColumns() != null) {
                    clonedColumns = new ArrayList<String>();
                    for (String column : configuration.getColumns()) {
                        clonedColumns.add(column);
                    }
                }

                List<String> clonedSecondaryTables = null;
                if (config.getSecondaryTables() != null) {
                    clonedSecondaryTables = new ArrayList<String>();
                    for (String secondaryTable : config.getSecondaryTables()) {
                        clonedSecondaryTables.add(secondaryTable);
                    }
                }

                CassandraCFConfiguration clone = new CassandraCFConfiguration(
                        config.getTableName(), clonedColumns, dataSourceType);
                clone.setPrimaryTable(config.isPrimaryTable());
                clone.setSecondaryTables(clonedSecondaryTables);
                clone.setAutoGenerated(config.getAutoGenerated());

                return clone;
            default:
                throw new ConfigurationException("Unknown data source type " +
                                                 dataSourceType.getName() + "..");
        }

    }

    public IndexConfiguration createIndexMetaData(String indexName, String indexedTable,
                                                  String[] indexedColumns,
                                                  DataSourceType dataSourceType,
                                                  Map<String, String> configurations)
            throws ConfigurationException {

        switch (dataSourceType) {
            case CASSANDRA:
                CassandraIndexConfiguration indexConfiguration = null;
                boolean timeStampIndexFound = false;

                if (indexedColumns != null) {
                    indexConfiguration = new CassandraIndexConfiguration(
                            indexName, indexedTable, indexedColumns.clone(), dataSourceType);
                    for (String indexedColumn : indexedColumns) {
                        if (indexedColumn.equals(PersistencyConstants.TIMESTAMP_KEY_NAME)) {
                            timeStampIndexFound = true;
                        }
                    }
                } else {
                    indexConfiguration = new CassandraIndexConfiguration(
                            indexName, indexedTable, null, dataSourceType);
                }

                indexConfiguration.setIndexingColumnFamily(configurations.
                        get(PersistencyConstants.INDEXING_TABLE));
                indexConfiguration.setCron(configurations.get(PersistencyConstants.CRON));
                indexConfiguration.setManuallyIndexed(Boolean.TRUE);

                String granularity = configurations.get(PersistencyConstants.GRANULARITY);

                if (granularity != null) {
                    indexConfiguration.setGranularity(Granularity.valueOf(
                            granularity));

                    // Add timestamp column to indexed columns
                    indexedColumns = indexConfiguration.getIndexedColumns();

                    if (indexedColumns != null) {
                        List<String> columnList = new ArrayList<String>();

                        for (String indexedColumn : indexedColumns) {
                            columnList.add(indexedColumn);
                        }

                        columnList.add(PersistencyConstants.TIMESTAMP_KEY_NAME);
                        indexConfiguration.setIndexedColumns(columnList.toArray(new String[]{}));
                    } else {
                        String[] columnArray = {PersistencyConstants.TIMESTAMP_KEY_NAME};
                        indexConfiguration.setIndexedColumns(columnArray);
                    }
                } else {
                    if (timeStampIndexFound) {
                        // Set default granularity if timeStamp is selected as an indexed column
                        indexConfiguration.setGranularity(Granularity.MINUTE);
                    }
                }

                return indexConfiguration;
            default:
                throw new ConfigurationException("Unknown data source type " +
                                                 dataSourceType.getName() + "..");
        }

    }

    public IndexConfiguration cloneIndexMetaData(IndexConfiguration configuration)
            throws ConfigurationException {

        DataSourceType dataSourceType = configuration.getDataSourceType();

        switch (dataSourceType) {
            case CASSANDRA:
                CassandraIndexConfiguration config = (CassandraIndexConfiguration) configuration;
                CassandraIndexConfiguration clone = new CassandraIndexConfiguration(
                        config.getIndexName(), config.getIndexedTable(), config.getIndexedColumns().
                        clone(),
                        dataSourceType);
                clone.setIndexingColumnFamily(config.getIndexingColumnFamily());
                clone.setManuallyIndexed(config.isManuallyIndexed());
                clone.setAutoGenerated(config.isAutoGenerated());
                clone.setGranularity(config.getGranularity());

                return clone;
            default:
                throw new ConfigurationException("Unknown data source type " +
                                                 dataSourceType.getName() + "..");
        }
    }


    /*             Private helper methods            */

    /**
     * Synchronize in memory index meta data cache with persisted meta data for all tenants.
     *
     * @throws ConfigurationException
     */
    private void synchronizeIndexMetaData() throws ConfigurationException {

        for (Map.Entry<Integer, List<IndexConfiguration>> entry :
                indexConfigurations.entrySet()) {

            List<IndexConfiguration> allIndexConfigurations = new ArrayList<IndexConfiguration>();

            int tenantId = entry.getKey();
            for (MetaDataPersistor persistor : metaDataPersistors) {
                List<IndexConfiguration> configurations = persistor.
                        getAllIndexMetaData(Utils.getConnectionParameters(tenantId));
                allIndexConfigurations.addAll(configurations);
            }

            indexConfigurations.put(tenantId, allIndexConfigurations);

        }
    }

    /**
     * Synchronize in memory column family meta data cache with persisted meta data for this tenant.
     *
     * @param tenantId
     * @throws ConfigurationException
     */
    private void synchronizeIndexMetaDataForTenant(int tenantId)
            throws ConfigurationException {

        List<IndexConfiguration> allIndexConfigurations = new ArrayList<IndexConfiguration>();
        for (MetaDataPersistor persistor : metaDataPersistors) {

            List<IndexConfiguration> configurations = persistor.
                    getAllIndexMetaData(Utils.getConnectionParameters(tenantId));
            allIndexConfigurations.addAll(configurations);
        }

        indexConfigurations.put(tenantId, allIndexConfigurations);

    }

    /**
     * Synchronize in memory column family meta data cache with persisted meta data for all tenants.
     *
     * @throws ConfigurationException
     */
    private void synchronizeTableMetaData() throws ConfigurationException {

        for (Map.Entry<Integer, List<TableConfiguration>> entry :
                tableConfigurations.entrySet()) {

            List<TableConfiguration> allCfConfigurations = new ArrayList<TableConfiguration>();

            int tenantId = entry.getKey();
            for (MetaDataPersistor persistor : metaDataPersistors) {
                List<TableConfiguration> configurations = persistor.
                        getAllTableMetaData(Utils.getConnectionParameters(tenantId));
                allCfConfigurations.addAll(configurations);
            }

            tableConfigurations.put(tenantId, allCfConfigurations);

        }

    }

    /**
     * Synchronize in memory column family meta data cache with persisted meta data for the this tenant
     *
     * @param tenantId
     * @throws ConfigurationException
     */
    private void synchronizeTableMetaDataForTenant(int tenantId) throws ConfigurationException {

        List<TableConfiguration> allCfConfigurations = new ArrayList<TableConfiguration>();
        for (MetaDataPersistor persistor : metaDataPersistors) {

            List<TableConfiguration> configurations = persistor.
                    getAllTableMetaData(Utils.getConnectionParameters(tenantId));
            allCfConfigurations.addAll(configurations);
        }

        //System.out.println("Table size : " + allCfConfigurations.size() + " ***************");
        tableConfigurations.put(tenantId, allCfConfigurations);
    }

    private class MetaDataSynchronizer implements Runnable {

        private final Log log = LogFactory.getLog(MetaDataSynchronizer.class);

        @Override
        public void run() {

            //if (log.isDebugEnabled()) {
            log.info("Running meta data synchronizer for meta data at " + new Date());
            //}

            try {
                try {
                    synchronizeIndexMetaData();
                } catch (ConfigurationException e) {
                    // Not letting this thread die due to the exception. Log and continue..
                    log.error("Error while fetching index meta data during synchronization..",
                              e);
                }
            } catch (Throwable e) {
                log.error("ERROR WHILE SYNCHRONIZING INDEX META DATA.", e);
            }

            try {
                try {
                    synchronizeTableMetaData();
                } catch (ConfigurationException e) {
                    // Not letting this thread die due to the exception. Log and continue..
                    log.error("Error while fetching column family meta data during " +
                              "synchronization..", e);
                }
            } catch (Throwable e) {
                log.error("ERROR WHILE SYNCHRONIZING TABLE META DATA.", e);
            }

        }

    }

}
