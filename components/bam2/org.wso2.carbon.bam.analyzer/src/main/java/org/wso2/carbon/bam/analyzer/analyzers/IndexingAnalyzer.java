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
package org.wso2.carbon.bam.analyzer.analyzers;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.configs.IndexingConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.persistence.IndexManager;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;

import java.util.HashMap;
import java.util.Map;

/*
 * Syntax : <index>
 *              +<ColumnFamily name='' defaultCF=''>
 *                  <granularity>hour</granularity>
 *                  <rowKey>
 *                      +<part name='' storeIndex=''/>
 *                  </rowKey>
 *                  <indexRowKey>allkeys</indexRowKey>
 *              </ColumnFamily>
 *          </index>
 *
 * Creates indexes for received data. For Cassandra this is achieved using creating separate column
 * family for each index.
 *
 * Syntax Explanation :
 *
 *  <index>                                        : Defines an index
 *     +<ColumnFamily name='' defaultCF=''>        : name = The name of index column family
 *                                                   defaultCF = is this the default column family.
 *                                                               should be false. (TODO: Remove this)
 *         <granularity>hour</granularity>         : Defines the granularity to sub-index using time.
 *                                                   If 'none' specified will not be sub-indexed using time.
 *         <rowKey>                                : Defines how the row key should be created of the
 *                                                   column family. 
 *            +<part name='' storeIndex=''/>       : defines the part of the rowkey which defines
 *                                                   a sub index. Set 'storeIndex' to 'true' if
 *                                                   the values of this index needs to be stored.
 *         </rowKey>
 *         <indexRowKey>allkeys</indexRowKey>      : defines the row in which all the row keys will
 *                                                   be stored for query purposes.
 *       </ColumnFamily>
 *  </index>
 *
 * Example :
 *
 * <index>
 *    <ColumnFamily name="customers" defaultCF="false">
 *        <granularity>hour</granularity>
 *        <rowKey>
 *           <part name="customerName" storeIndex="false"/>
 *        </rowKey>
 *        <indexRowKey>allkeys</indexRowKey>
 *    </ColumnFamily>
 * </index>
 *
 * This configuration creates a new column family 'customers' which indexes data in base column
 * family (EVENT) using the customerName. Only EVENT table row keys will be stored in the index
 * column family.
 *
 * Input : The raw event data in EVENT column family.
 *
 *  {2011-12-21 09:35:22---uuid1} : {{orderId : order1}, {customerName : ben}, {quantity : 32} }
 *  {2011-12-21 09:35:22---uuid2} : {{orderId : order2}, {customerName : ben}, {quantity : 53} }
 *  {2011-12-21 10:23:12---uuid3} : {{orderId : order3}, {customerName : bob}, {quantity : 33} }
 *  {2011-12-21 09:34:22---uuid4} : {{orderId : order4}, {customerName : alex}, {quantity : 64} }
 *
 * Output : Indexed data stored in 'customers' column family.
 *
 *  {ben---2011-12-21 09:00:00} : {{2011-12-21 09:35:22---uuid1}, {2011-12-21 09:35:22---uuid2} }
 *  {bob---2011-12-21 10:00:00} : {{2011-12-21 10:23:12---uuid3} }
 *  {alex---2011-12-21 09:00:00} : {{2011-12-21 09:34:22---uuid4} }
 *  {allKeys} : {{alex---2011-12-21 09:00:00}, {ben---2011-12-21 09:00:0},{bob---2011-12-21 10:00:00} }
 * 
 */

public class IndexingAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(IndexingAnalyzer.class);


    public IndexingAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    @Override
    public void analyze(DataContext dataContext) {

        String cursorName = getAnalyzerSequenceName() + getPositionInSequence();
        IndexingConfig config = (IndexingConfig) getAnalyzerConfig();

        try {
            IndexManager.getInstance().indexData(config.getIndexConfiguration(),
                                                 dataContext.getCredentials(), cursorName);
        } catch (IndexingException e) {
            log.error("Error while indexing table " +
                      config.getIndexConfiguration().getIndexedTable() + "..", e);
        }
    }

/*    public void analyze(DataContext dataContext, String temp) {

        if (log.isDebugEnabled()) {
            log.debug("Running indexer..");
        }

        List<CFConfigBean> cfConfigs = ((IndexingConfig) getAnalyzerConfig()).getCFConfigurations();

        if (cfConfigs == null || cfConfigs.size() == 0) {
            return; // Nothing to index
        }
        NoSQLDataStore store = null;
        try {
            store = (NoSQLDataStore) NoSQLDataStoreFactory.getInstance().getDataStore(
                    dataContext.getCredentials().get(PersistencyConstants.USER_NAME));
        } catch (DataStoreException e) {
            log.error("Error getting data store..", e);
        }

        if (store == null) {
            try {
                store = (NoSQLDataStore) NoSQLDataStoreFactory.getInstance().
                        initializeDataStore(dataContext.getCredentials(), false);
            } catch (InitializationException e) {
                log.error("Error initializing data store..", e);
                return;
            }
        }

        if (store == null) {
            return;
        }

        QueryManager mgr;
        try {
            mgr = QueryManagerFactory.getInstance().getQueryManager(dataContext.getCredentials().
                    get(PersistencyConstants.USER_NAME));
            if (mgr == null) {

                mgr = QueryManagerFactory.getInstance().initializeQueryManager(
                        dataContext.getCredentials());

            }
        } catch (InitializationException e) {
            log.error("Cannot initialize Query manager", e);
            return;
        }

        for (CFConfigBean cfConfig : cfConfigs) {
            ConfigurationHolder.getInstance().addIndexConfiguration(dataContext.getExecutingTenant(), cfConfig);
        }

        BatchedResult result = mgr.getBatchedBaseColumnFamilyRows(PersistencyConstants.BASE_COLUMN_FAMILY,
                                                                  getAnalyzerSequenceName(),
                                                                  getPositionInSequence(), PersistencyConstants.DEFAULT_BATCH_SIZE);


        store.startBatchCommit();

        List<ResultRow> rows = result.getResults();

        Map<String, Set<String>> keysOfRowsAdded = new HashMap<String, Set<String>>();
        if (rows != null) {
            for (ResultRow row : rows) {
                // Iterate through all the CF configs and decide which one to insert event or index event
                for (CFConfigBean cfConfig : cfConfigs) {
                    Map<String, String> cfData = convertToMap(row.getColumns());

                    String rowKey = null;
                    try {
                        rowKey = createRowKey(cfConfig.getRowKeyParts(), cfConfig.getGranularity(),
                                              cfData, false);
                    } catch (AnalyzerException e) {
                        log.error("Unable to create row key. Not persisting index on row : " +
                                  row.getRowKey(), e);
                        continue;
                    }

                    if (rowKey == null) {
                        // this row key is does not apply to this event, skip it
                        continue;
                    }

                    // We are creating a data map, with the default row key as the column name, i.e. default row key is stored
                    // as a pointer
                    Map<String, String> nonDefaultDataMap = createNonDefaultDataMap(row.getRowKey());

                    store.persistData(cfConfig.getCfName(), rowKey, nonDefaultDataMap);
                    // persist any indexes if they are given
                    store.persistIndexes(cfConfig.getCfName(), cfConfig.getRowKeyParts(),
                                         cfData);

                    if (cfConfig.getIndexRowKey() != null) {
                        // Cassandra does not sort in rows, but Cassandra columns are sorted.
                        // So we store a separate column, i.e. defaults to 'allKeys', that stores pointers to all the row keys
                        // in the same CF
                        Map<String, String> indexRowKeyDataMap = createNonDefaultDataMap(rowKey);
                        store.persistData(cfConfig.getCfName(), cfConfig.getIndexRowKey(),
                                          indexRowKeyDataMap);

                    }

                    // Create an index using timestamp. Events are sorted using in order of arrival
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = formatter.format(new Date());

                    long currentTime = System.nanoTime();
                    String dateTime = date + ":" + Long.toString(currentTime);

                    // Store timeStamp index
                    Map<String, String> timeStampData = new HashMap<String, String>();
                    timeStampData.put(dateTime, rowKey);

                    String rowIndex = mgr.getColumnValue(cfConfig.getCfName(),
                                                         cfConfig.getIndexRowKey(),
                                                         rowKey);
                    // If this row already present we skip adding time stamp index again
                    Set<String> keys = keysOfRowsAdded.get(cfConfig.getCfName());
                    if (keys == null) {
                        keys = new HashSet<String>();
                    }

                    if (rowIndex == null && !keys.contains(rowKey)) {
                        store.persistData(cfConfig.getCfName(), PersistencyConstants.TIMESTAMP_INDEX_ROW,
                                          timeStampData);
                    }

                    keys.add(rowKey);
                    keysOfRowsAdded.put(cfConfig.getCfName(), keys);
                }
            }
        }

        store.endBatchCommit();

        if (result.getCursor() != null) {
            store.setLastCursorForColumnFamily(PersistencyConstants.BASE_COLUMN_FAMILY,
                                               getAnalyzerSequenceName(),
                                               getPositionInSequence(), result.getCursor());
        }

        if (log.isDebugEnabled()) {
            log.debug("Indexing completed ...");
        }
    }

    // Todo: Add clean up hook to analyzer

    public boolean deleteCursor(int tenantId) throws AnalyzerException {
        NoSQLDataStore store = null;
        ConnectionDTO connectionParametersForTenant = Utils.getEngine().getConnectionParametersForTenant(tenantId);
        try {
            store = (NoSQLDataStore) NoSQLDataStoreFactory.getInstance().getDataStore(
                    connectionParametersForTenant.getUsername());
        } catch (DataStoreException e) {
            log.error("Error getting data store..", e);
        }

        if (store == null) {
            try {
                Map<String, String> credentials = new HashMap<String, String>();
                credentials.put(AnalyzerConfigConstants.USERNAME, connectionParametersForTenant.getUsername());
                credentials.put(AnalyzerConfigConstants.PASSWORD, connectionParametersForTenant.getPassword());
                store = (NoSQLDataStore) NoSQLDataStoreFactory.getInstance().
                        initializeDataStore(credentials, false);
            } catch (InitializationException e) {
                log.error("Error initializing data store..", e);
                return false;
            }
        }

        if (store == null) {
            return false;
        }

        store.setLastCursorForColumnFamily(PersistencyConstants.BASE_COLUMN_FAMILY,
                                           getAnalyzerSequenceName(),
                                           getPositionInSequence(), "");
        return true;

    }*/

    /**
     * This will create a map that will be used as a pointer to the entry created in the default CF (i.e. usually the Event CF)
     *
     * @param rowKeyToIndex String that is the row key to index - i.e. usually, the row key in the event CF
     * @return
     */
    private Map<String, String> createNonDefaultDataMap(String rowKeyToIndex) {

        Map<String, String> nonDefaultMap = new HashMap<String, String>();
        nonDefaultMap.put(rowKeyToIndex, "");
        return UnmodifiableMap.decorate(nonDefaultMap);

    }

    /**
     * Creates the row key which is probably the most important entry in an inserted row. This row key allows us to later find this
     * row which will be an event or a pointer to the event
     *
     * @param rowKeyParts
     * @param granularity
     * @param cfData
     * @param appendUUID
     * @return
     * @throws org.wso2.carbon.bam.analyzer.engine.AnalyzerException
     *
     */
/*
    private String createRowKey(List<KeyPart> rowKeyParts, String granularity,
                                Map<String, String> cfData, boolean appendUUID)
            throws AnalyzerException {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < rowKeyParts.size(); i++) {
            KeyPart rowKeyPart = rowKeyParts.get(i);
            String rowKeyPartName = rowKeyPart.getName();
            if (cfData.containsKey(rowKeyPartName)) {
                String rowKeyPartValue = cfData.get(rowKeyPartName);

                // handle timestamp case according to granularity
                if (rowKeyPartName.equals(PersistencyConstants.TIMESTAMP_KEY_NAME)) {
                    try {
                        // we use the time stamp factory to generate the time stamp according to granularity
                        rowKeyPartValue = TimeStampFactory.getFactory().getTimeStamp(
                                cfData.get(rowKeyPartName), granularity);
                    } catch (ParseException e) {
                        throw new AnalyzerException("Cannot parse time stamp : " +
                                                    cfData.get(rowKeyPartName));
                    }

                }
                buffer.append(rowKeyPartValue);

                // Skip appending row key delimiter for the last row key part
                if ((i + 1) != rowKeyParts.size()) {
                    buffer.append("---");
                }


            } else {
                // if there is no column name that corresponds to the row key parts, that means this event should not be inserted,
                // we return null, in that case
                return null;
            }
        }

        // Add an optional uuid
        if (appendUUID) {
            buffer.append("---");
            buffer.append(UUID.randomUUID());
        }

        buffer.trimToSize();
        return buffer.toString();
    }

    private Map<String, String> convertToMap(List<ResultColumn> columns) {
        Map<String, String> result = new HashMap<String, String>();

        if (columns != null) {
            for (ResultColumn column : columns) {
                result.put(column.getKey(), column.getValue());
            }
        }

        return result;
    }
*/

}
