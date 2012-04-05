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

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;

import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.persistence.AbstractDataStore;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

public class CassandraDataStore extends AbstractDataStore {

    private static final Log log = LogFactory.getLog(CassandraDataStore.class);

    private static final int SUPER_TENANT = 0;

    private static StringSerializer stringSerializer = StringSerializer.get();
    private static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Cluster cluster = null;
    private Keyspace keySpace = null;
    private KeyspaceDefinition keySpaceDef = null;

    private Map<String, String> credentials;

    private ThreadLocal<Boolean> startBatchCommit = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    private ThreadLocal<Mutator<String>> mutatorThreadLocal = new ThreadLocal<Mutator<String>>();

    public static void main(String[] args) {

TSocket socket = new TSocket("192.168.0.100", 9160);
       TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(socket, true, true);

       TTransport transport = socket;

       try {
           transport.open();

           Cassandra.Client client = new Cassandra.Client(tBinaryProtocol);

           Map<String, List<String>> schemaVersions= client.describe_schema_versions();


           transport.close();

       } catch (TTransportException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       } catch (TException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       } catch (InvalidRequestException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }
    }

    @Override
    public void initialize(Map<String, String> credentials) throws StoreException {

        this.credentials = credentials;
        String userName = credentials.get(PersistencyConstants.USER_NAME);
        String password = credentials.get(PersistencyConstants.PASSWORD);

        ClusterInformation clusterInfo = new ClusterInformation(userName, password);
        clusterInfo.setClusterName(userName);

        cluster = CassandraUtils.createCluster(clusterInfo);
        keySpaceDef = cluster.describeKeyspace(
                PersistencyConstants.BAM_KEY_SPACE);

        TableConfiguration tableInfoCfConfiguration;
        TableConfiguration cursorInfoCfConfiguration;
        TableConfiguration indexInfoCfConfiguration;
        TableConfiguration metaInfoCfConfiguration;
        TableConfiguration metaCfConfiguration;
        TableConfiguration eventCfConfiguration;
        TableConfiguration baseCfConfiguration;
        TableConfiguration correlationCfConfiguration;

        tableInfoCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.TABLE_INFO_TABLE, null, DataSourceType.CASSANDRA);
        tableInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);

        cursorInfoCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.CURSOR_INFO_TABLE, null, DataSourceType.CASSANDRA);
        cursorInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);

        indexInfoCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.INDEX_INFO_TABLE, null, DataSourceType.CASSANDRA);
        indexInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);

        metaInfoCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.META_INFO_TABLE, null, DataSourceType.CASSANDRA);
        metaInfoCfConfiguration.setAutoGenerated(Boolean.TRUE);

        eventCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.EVENT_TABLE, null, DataSourceType.CASSANDRA);

        metaCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.META_TABLE, null, DataSourceType.CASSANDRA);

        baseCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.BASE_TABLES, null, DataSourceType.CASSANDRA);

        correlationCfConfiguration = new CassandraCFConfiguration(
                PersistencyConstants.CORRELATION_TABLE, null, DataSourceType.CASSANDRA);


        keySpace = HFactory.createKeyspace(PersistencyConstants.BAM_KEY_SPACE, cluster);

        if (keySpaceDef == null) {

            List<ColumnFamilyDefinition> columnFamilyDefinitionList = getAllColumnFamiliesAsList();


            cluster.addKeyspace(HFactory.createKeyspaceDefinition(
                    PersistencyConstants.BAM_KEY_SPACE, ThriftKsDef.DEF_STRATEGY_CLASS, 1, columnFamilyDefinitionList));

            keySpaceDef = cluster.describeKeyspace(PersistencyConstants.BAM_KEY_SPACE);
            //Sometimes it takes some time to make keySpaceDef!=null
            int retryCount = 0;
            while (keySpaceDef == null && retryCount < 100) {
                try {
                    Thread.sleep(100);
                    keySpaceDef = cluster.describeKeyspace(PersistencyConstants.BAM_KEY_SPACE);
                    if (keySpaceDef != null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Do not use the MetaDataManager method. Results in a chicken egg situation :).
            // metaDataManager.storeTableMetaData(credentials, tableInfoCfConfiguration);

            Map<String, String> tableConfigurationData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(tableInfoCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.TABLE_INFO_TABLE, tableConfigurationData);

            // Do not use the MetaDataManager method. Results in a chicken egg situation :).
            // metaDataManager.storeTableMetaData(credentials, tableInfoCfConfiguration);

            Map<String, String> cursorInfoTableConfigurationData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(cursorInfoCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.CURSOR_INFO_TABLE, cursorInfoTableConfigurationData);

            Map<String, String> tableConfigurationIndexInfoData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(indexInfoCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.INDEX_INFO_TABLE, tableConfigurationIndexInfoData);


            Map<String, String> tableConfigurationMetaInfoData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(metaInfoCfConfiguration);
            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.META_INFO_TABLE, tableConfigurationMetaInfoData);

            Map<String, String> tableConfigurationEventData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(eventCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.EVENT_TABLE, tableConfigurationEventData);


            // Store the meta data of pseudo table 'BASE'
            Map<String, String> tableConfigurationBaseData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(baseCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.BASE_TABLES, tableConfigurationBaseData);

            Map<String, String> tableConfigurationMetaData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(metaCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.META_TABLE, tableConfigurationMetaData);


            Map<String, String> tableConfigurationCorrelationData = CassandraMetaDataPersistor.
                    getTableMetaDataAsMap(correlationCfConfiguration);

            persistData(PersistencyConstants.TABLE_INFO_TABLE,
                        PersistencyConstants.CORRELATION_TABLE, tableConfigurationCorrelationData);

        }

        MetaDataManager metaDataManager = MetaDataManager.getInstance();

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            tenantId = SUPER_TENANT;
            log.error("Unable to obtain tenant information. Assuming tenant super tenant..");
        }

        metaDataManager.addTableMetaDataForTenant(tenantId, tableInfoCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, cursorInfoCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, indexInfoCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, metaInfoCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, eventCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, baseCfConfiguration);

        metaDataManager.addTableMetaDataForTenant(tenantId, metaCfConfiguration);


        metaDataManager.addTableMetaDataForTenant(tenantId, correlationCfConfiguration);


    }

    public Keyspace getKeySpace() {
        return keySpace;
    }

    private List<ColumnFamilyDefinition> getAllColumnFamiliesAsList() {
        return Arrays.asList(HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.TABLE_INFO_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.CURSOR_INFO_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.INDEX_INFO_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.META_INFO_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.EVENT_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.META_TABLE),
                             HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE, PersistencyConstants.CORRELATION_TABLE));

    }

    @Override
    public void persistData(String cfName, String rowKey, Map<String, String> columns)
            throws StoreException {
        if (!isTableExists(cfName)) {
            throw new StoreException("The column family '" + cfName + "' does not exist..");
        }

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {

            mutator = HFactory.createMutator(keySpace, stringSerializer);
        }

        for (Map.Entry<String, String> column : columns.entrySet()) {
            mutator.addInsertion(rowKey, cfName, HFactory.createStringColumn(column.getKey(),
                                                                             column.getValue()));
        }

        persistMetaInformation(cfName, rowKey);

        if (!startBatchCommit.get()) {
            mutator.execute();
        }
    }

    @Override
    public void deleteData(String cfName, String rowKey) throws StoreException {
        if (!isTableExists(cfName)) {
            throw new StoreException("The column family '" + cfName + "' does not exist..");
        }

        startBatchCommit();

        Mutator<String> mutator = HFactory.createMutator(keySpace, stringSerializer);

        List<HColumn<String, String>> columns = getColumnsOfRow(
                cfName, PersistencyConstants.ROW_INDEX,
                rowKey, rowKey + "1", Integer.MAX_VALUE); // Append "1" since rangeLast is exclusive in Cassandra range query

        String timeStampIndex = null;
        if (columns != null) {
            for (HColumn<String, String> column : columns) {
                if (column.getName().equals(rowKey)) {
                    timeStampIndex = column.getValue();
                }
            }
        }

        // Delete row and timestamp index related to this row as well if this row is not row or timestamp index itself
        if (!rowKey.equals(PersistencyConstants.ROW_INDEX) && !rowKey.
                equals(PersistencyConstants.TIMESTAMP_INDEX)) {
            deleteRowColumn(cfName, PersistencyConstants.ROW_INDEX, rowKey);

            if (timeStampIndex != null) {
                deleteRowColumn(cfName, PersistencyConstants.TIMESTAMP_INDEX, timeStampIndex);
            }
        }

        // Delete indexed data on this row
        MetaDataManager metaDataManager = MetaDataManager.getInstance();
        try {
            List<IndexConfiguration> indexConfigurations = metaDataManager.
                    getIndexMetaDataOfTable(credentials, cfName);

            if (indexConfigurations != null) {
                for (IndexConfiguration indexConfiguration : indexConfigurations) {
                    if (indexConfiguration instanceof CassandraIndexConfiguration) {
                        CassandraIndexConfiguration cassandraIndexConfiguration =
                                (CassandraIndexConfiguration) indexConfiguration;
                        String indexingColumnFamily = cassandraIndexConfiguration.
                                getIndexingColumnFamily();

                        columns = getColumnsOfRow(cfName, rowKey, "", "", Integer.MAX_VALUE);

                        Map<String, String> columnMap = new HashMap<String, String>();
                        for (HColumn<String, String> column : columns) {
                            columnMap.put(column.getName(), column.getValue());
                        }

                        CassandraIndexingStrategy indexingStrategy =
                                new CassandraIndexingStrategy();
                        String indexRowKey = indexingStrategy.createRowKey(
                                cassandraIndexConfiguration.getIndexedColumns(), null, columnMap);

                        if (indexRowKey != null) {
                            deleteRowColumn(indexingColumnFamily, indexRowKey, rowKey);
                        }
                    }
                }
            }
        } catch (ConfigurationException e) {
            log.error("Unable to fetch table meta data. Will not be deleting indexed data for " +
                      "the row '" + rowKey + "' of table '" + cfName +
                      "'. May cause in inconsistencies on indexed data..", e);
        } catch (IndexingException e) {
            log.error("Unable to fetch indexed data for deletion for the row '" + rowKey +
                      "' of the table '" + cfName + "'..");
        }

        // Finally delete the row
        mutator.addDeletion(rowKey, cfName, null, stringSerializer);

        mutator.execute();

    }

    @Override
    public void updateData(String table, String key, Map<String, String> columns)
            throws StoreException {
        //TODO : Add updateData logic
    }

    @Override
    public void persistBinaryData(String cfName, String rowKey, Map<String, ByteBuffer> data)
            throws StoreException {

        if (!isTableExists(cfName)) {
            throw new StoreException("The column family '" + cfName + "' does not exist..");
        }

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {
            mutator = HFactory.createMutator(keySpace, stringSerializer);
        }

        for (Map.Entry<String, ByteBuffer> column : data.entrySet()) {
            mutator.addInsertion(rowKey, cfName, HFactory.createColumn(column.getKey(),
                                                                       column.getValue(),
                                                                       stringSerializer,
                                                                       byteBufferSerializer));
        }

        persistMetaInformation(cfName, rowKey);

        if (!startBatchCommit.get()) {
            mutator.execute();
        }


    }

    @Override
    public void startBatchCommit() {
        if (!startBatchCommit.get()) {
            startBatchCommit.set(true);
            mutatorThreadLocal.set(HFactory.createMutator(keySpace, stringSerializer));
        }
    }

    @Override
    public void endBatchCommit() {
        if (startBatchCommit.get()) {
            mutatorThreadLocal.get().execute();
            mutatorThreadLocal.set(null);
            startBatchCommit.set(false);
        }
    }

    // This is a hacky way to detect whether the column family is present in Cassandra.
    // (Method using KeyspaceDef doesn't work reliably. Gives false negatives).
    // This should be replaced with a proper way to detect a column family if such method is found
    @Override
    public boolean isTableExists(String table) {

        int retryCount = 0;
        while (!isCfExists(table)) {
            if (retryCount < 100) {
                try {
                    Thread.sleep(100);
                    retryCount++;
                } catch (InterruptedException ignored) {
                    break;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isCfExists(String table) {
/*        KeyspaceDefinition keyspaceDefinition =   cluster.describeKeyspace(PersistencyConstants.BAM_KEY_SPACE);

        if (keyspaceDefinition != null) {
            for (ColumnFamilyDefinition cf : keyspaceDefinition.getCfDefs()) {
                if (cf.getName().equals(table)) {
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }*/
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(keySpace, stringSerializer, stringSerializer,
                                                  stringSerializer);
        multigetSliceQuery.setColumnFamily(table);
        multigetSliceQuery.setKeys("test");
        multigetSliceQuery.setRange("", "", false, 1);


        try {
            multigetSliceQuery.execute();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean createTable(String table, List<String> fields) throws StoreException {

        ColumnFamilyDefinition columnFamilyDefinition = HFactory.createColumnFamilyDefinition(PersistencyConstants.BAM_KEY_SPACE,
                table);

        synchronized (CassandraDataStore.class) {
            boolean cfDefFound = false;

            keySpaceDef = cluster.describeKeyspace(PersistencyConstants.BAM_KEY_SPACE);

            if (keySpaceDef != null) {
                for (ColumnFamilyDefinition cfDef : keySpaceDef.getCfDefs()) {
                    if (cfDef.getName().equals(table)) {
                        cfDefFound = true;
                        break;
                    }
                }
                // Column Family not found, so create it . Here isTableExists also used to really
                // sure that the column family is not present in the keyspace.
                if (!cfDefFound) {
                    cluster.addColumnFamily(columnFamilyDefinition);
                    KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(PersistencyConstants.BAM_KEY_SPACE);


                } else {
                    return false; // Column family not added since it's already existing.
                }
            } else {
                return false;
            }


            // This is due to asynchronous nature of hector API when it comes to creating column
            // families. We need to poll until column family is really created in Cassandra.
            int retryCount = 0;
            int size = 0;
            try {

                Set<CassandraHost> knownPoolHosts = cluster.getKnownPoolHosts(true);

                Iterator<CassandraHost> hostIterator = knownPoolHosts.iterator();
                CassandraHost firstCassandraHost = null;
                if (hostIterator.hasNext()) {
                    firstCassandraHost = hostIterator.next();
                }
                TSocket socket = null;
                if (firstCassandraHost != null) {
                    socket = new TSocket(firstCassandraHost.getHost(), firstCassandraHost.getPort());
                } else {
                    throw new StoreException("Cassandra hosts are null. Why: Cassandra cluster has gone down");
                }
                TTransport transport = new TFramedTransport(socket);
                TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(transport, true, true);
                transport.open();

                Cassandra.Client client = new Cassandra.Client(tBinaryProtocol);
                while ((size != 1) && retryCount < 100) {
                    Map<String, List<String>> describeSV = client.describe_schema_versions();
                    try {
                        size = describeSV.size();
                        Thread.sleep(1000);
                        retryCount++;
                        if (log.isDebugEnabled()) {
                            log.debug("No. of schema versions in cluster : " + size );
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                if (size != 1) {
                    String message = "Fatal Error: Cassandra Schema not in agreement";
                    RuntimeException runtimeException = new RuntimeException(message);
                    log.error(message, runtimeException);
                    throw runtimeException;
                }
                transport.close();
            } catch (InvalidRequestException e) {
                String message = "Invalid request when connecting to Cassandra cluster";
                log.error(message, e);
                throw new StoreException(message, e);
            } catch (TException e) {
                String message = "Transport issue when connecting to Cassandra cluster";
                log.error(message, e);
                throw new StoreException(message, e);
            }
        }

        return true;

    }

    @Override
    public boolean deleteTable(String columnFamily) throws StoreException {

        if (isTableExists(columnFamily)) {
            cluster.dropColumnFamily(PersistencyConstants.BAM_KEY_SPACE, columnFamily);
        }

        while (isTableExists(columnFamily)) { // Wait while the column family is actually deleted
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                //Ignore
            }
        }

        return true;

    }

    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.CASSANDRA;
    }


    /*         Private helper methods           */

    /*
     * Persists meta information about the row inserted. Updates meta rows as follows.
     * 1. ROW_INDEX : Adds row key to facilitate range queries on row keys.
     * 2. TIMESTAMP_INDEX : Adds insertion timestamp along with row key to facilitate batch queries
     *                      which operate by fetching rows in order of arrival
     *
     * @param table the table row is being added
     * @param key   the row key
     */
    // TODO: Move these meta information to a separate table since storing these information in same
    // table constrains the data stored to not contain the meta row keys.
    private void persistMetaInformation(String table, String key) {

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {
            mutator = HFactory.createMutator(keySpace, stringSerializer);
        }

        String date = formatter.format(new Date());

        long currentTime = System.nanoTime();
        String dateTime = date + ":" + Long.toString(currentTime);

        mutator.addInsertion(PersistencyConstants.ROW_INDEX, table,
                             HFactory.createStringColumn(key, dateTime));

        mutator.addInsertion(PersistencyConstants.TIMESTAMP_INDEX, table,
                             HFactory.createStringColumn(dateTime, key));

        if (!startBatchCommit.get()) {
            mutator.execute();
        }

    }

    private void deleteRowColumn(String cfName, String rowKey, String column)
            throws StoreException {
        if (!isTableExists(cfName)) {
            throw new StoreException("The column family '" + cfName + "' does not exist..");
        }

        Mutator<String> mutator;
        if (startBatchCommit.get()) {
            mutator = mutatorThreadLocal.get();
        } else {
            mutator = HFactory.createMutator(keySpace, stringSerializer);
        }

        mutator.addDeletion(rowKey, cfName, column, stringSerializer);

        if (!startBatchCommit.get()) {
            mutator.execute();
        }
    }

    private List<HColumn<String, String>> getColumnsOfRow(String cfName, String rowKey,
                                                          String rangeFirst, String rangeLast,
                                                          int batchSize) {
        MultigetSliceQuery<String, String, String> multigetSliceQuery =
                HFactory.createMultigetSliceQuery(keySpace, stringSerializer,
                                                  stringSerializer, stringSerializer);
        multigetSliceQuery.setColumnFamily(cfName);
        multigetSliceQuery.setKeys(rowKey);
        multigetSliceQuery.setRange(rangeFirst, rangeLast, false, batchSize);
        QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();

        Row<String, String, String> indexRow = result.get().getByKey(rowKey);
        List<HColumn<String, String>> list = indexRow.getColumnSlice().getColumns();

        return list;

    }

}
