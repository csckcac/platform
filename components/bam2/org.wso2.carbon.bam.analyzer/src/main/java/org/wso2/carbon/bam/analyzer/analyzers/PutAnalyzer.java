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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.configs.PutConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.TableConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.PersistenceManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Syntax :
 *
 * <put name='' dataSourceType=''>
 *    [0..1]<onExist>
 *             <replace/>
 *             <aggregate>
 *                [1..*]<measure name='' aggregationType=''/>
 *             </aggregate>
 *          </onExist>
 * </put>
 *
 * This analyzer stores data to the a given column family. Currently storing grouped rows is not
 * supported. Only list of rows can be stored as of now. Additionally it can be defined what to be
 * done in case the row with given key already exists. The behaviour is either to replace the entire
 * row or aggregate specified fields of existing row with the new row being stored.
 *
 * Syntax Explanation :
 *
 * <put name='' dataSource=''>                              : name = the column family to store
 *                                                            dataSource = the data source this
 *                                                            table belongs to.
 *    (0..1)<onExist>                                       : specifies the behaviour if row with the
 *                                                            given key already exists. If not present
 *                                                            the default behaviour is to replace the
 *                                                            row.
 *             <replace/>                                   : replace the row. This is the default behaviour.
 *             <aggregate>                                  : aggregate given fields of existing and new row and store.
 *                +<measure name='' aggregationType=''/>    : the field to be aggregated along with the type of
 *                                                            aggregation. (SUM, MIN, MAX, AVG, CUM). (The other
 *                                                            fields will get replaced if existing or newly added
 *                                                            to the stored row.) - TODO
 *             </aggregate>
 *          </onExist>
 * </put>
 *
 * Example :
 *
 * Input : List of rows as given below.
 *
 *   {ESB} : {{requestCount : 32}, {responseCount : 32}, {responseTime : 3.0} }
 *   {AS} : {{requestCount : 23}, {responseCount : 23}, {responseTime : 23.0} }
 *   {BAM} : {{requestCount : 31}, {responseCount : 31}, {responseTime : 12.3} }
 *   {BRS} : {{requestCount : 43}, {responseCount : 43}, {responseTime : 53.3} }
 *
 * Existing data in column family 'result':
 *
 *   {ESB} : {{requestCount : 65}, {responseCount : 65}, {responseTime : 7.0} }
 *   {AS} : {{requestCount : 4}, {responseCount : 4}, {responseTime : 23.0} }
 *   {BAM} : {{requestCount : 56}, {responseCount : 56}, {responseTime : 35} }
 *   {allKeys} : {{AS}, {BAM}, {ESB}} // This is the index row
 *
 * Put specification :
 *
 *      <put name='result' dataSourceType='CASSANDRA'>
 *          <onExist>
 *             <aggregate>
 *                <measure name='requestCount' aggregationType='SUM'/>
 *                <measure name='responseCount' aggregationType='SUM'/>
 *                <measure name='responseTime' aggregationType='AVG'/>
 *             </aggregate>
 *          </onExist>
 *      </put>
 *
 * Output : Modified data in the column family 'result'.
 *
 *   {ESB} : {{requestCount : 97}, {responseCount : 97}, {responseTime : 5.0} }
 *   {AS} : {{requestCount : 27}, {responseCount : 27}, {responseTime : 23.0} }
 *   {BAM} : {{requestCount :87}, {responseCount : 87}, {responseTime : 23.65} }
 *   {BRS} : {{requestCount : 43}, {responseCount : 43}, {responseTime : 53.3} }
 *   {allKeys} : {{AS}, {BAM}, {BRS}, {ESB}} // This is the index row
 */
public class PutAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(PutAnalyzer.class);

    public PutAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    @Override
    public void analyze(DataContext dataContext) {
        Object result = getData(dataContext);

        if (result != null) {

            PutConfig config = (PutConfig) getAnalyzerConfig();

            String table = config.getColumnFamily();

            String dataSource = config.getDataSource();

            DataSourceType dataSourceType = DataSourceType.valueOf(dataSource);

            boolean isDoAggregate = config.isDoAggregate();

            PersistenceManager persistenceManager = new PersistenceManager();
            MetaDataManager metaDataManager = MetaDataManager.getInstance();

            if (result instanceof List) {
                List<Record> records = (List<Record>) result;

                if (records.size() > 0) {

                    try {
                        if (!persistenceManager.isTableExists(dataContext.getCredentials(),
                                                              table)) {

                            Record firstRecord = records.get(0);

                            TableConfiguration configuration = metaDataManager.
                                    createTableMetaData(table, firstRecord, dataSourceType, false);
                            persistenceManager.createTable(dataContext.getCredentials(),
                                                           configuration);
                        }

                        putRecords(table, records, isDoAggregate,
                                   dataContext);

                    } catch (StoreException e) {
                        log.error("Error storing records to the table " + table + "..", e);
                    } catch (ConfigurationException e) {
                        log.error("Error creating meta data for table " + table + "..", e);
                    }

                    setData(dataContext, null);

                    // Create indexes if not existing. Not generic..
/*                    Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
                    CassandraIndexConfiguration index = (CassandraIndexConfiguration) properties.
                            get(AnalyzerConfigConstants.INDEX);

                    String indexName = table + "_auto_index";
                    try {
                        if (metaDataManager.getIndexMetaData(getExecutingTenantId(),
                                                             indexName) == null) {
                            IndexConfiguration indexConfiguration = metaDataManager.
                                    createIndexMetaData(indexName, table, table,
                                                        index.getIndexedColumns(), dataSourceType);
                            indexConfiguration.setAutoGenerated(Boolean.TRUE);
                            IndexManager.getInstance().createIndex(indexConfiguration,
                                                                   dataContext.getCredentials());
                        }
                    } catch (ConfigurationException e) {
                        log.error("Unable to store index meta data for index " + indexName + "..",
                                  e);
                    } catch (IndexingException e) {
                        log.error("Unable to store index meta data for index " + indexName + "..",
                                  e);
                    }*/

                }

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingRecordMap = (Map<String, List<Record>>) result;

                if (existingRecordMap.size() > 0) {

                    String secondaryTable = table + "_Secondary_" +
                                            PersistencyConstants.SECONDARY_COLUMN_FAMILIES;

                    // Create primary table if not existing
                    try {

                        if (!persistenceManager.isTableExists(dataContext.getCredentials(),
                                                              table)) {

                            TableConfiguration configuration = metaDataManager.
                                    createTableMetaData(table, new ArrayList<String>()
                                            , dataSourceType, false);
                            configuration.setPrimaryTable(Boolean.TRUE);

                            List<String> secondaryTables = new ArrayList<String>();
                            secondaryTables.add(secondaryTable);

                            configuration.setSecondaryTables(secondaryTables);

                            persistenceManager.createTable(dataContext.getCredentials(),
                                                           configuration);
                        }

                    } catch (StoreException e) {
                        log.error("Error creating primary table " + table + "..", e);
                        return;
                    } catch (ConfigurationException e) {
                        log.error("Error creating meta data for primary table " + table + "..", e);
                        return;
                    }

                    // Create secondary table if not existing
                    try {

                        if (!persistenceManager.isTableExists(dataContext.getCredentials(),
                                                              secondaryTable)) {

                            List<Record> recordGroup = existingRecordMap.values().iterator().next();

                            Record sampleRecord = null;
                            if (recordGroup != null && recordGroup.size() > 0) {
                                sampleRecord = recordGroup.get(0);
                            }

                            TableConfiguration configuration = metaDataManager.
                                    createTableMetaData(secondaryTable, sampleRecord,
                                                        dataSourceType, true);

                            persistenceManager.createTable(dataContext.getCredentials(),
                                                           configuration);
                        }

                    } catch (StoreException e) {
                        log.error("Error creating secondary table " + secondaryTable + "..", e);
                        return;
                    } catch (ConfigurationException e) {
                        log.error("Error creating meta data for secondary table " + secondaryTable +
                                  "..", e);
                        return;
                    }

                    // Store data to primary and secondary tables
                    try {
                        List<Record> primaryTableRecords = new ArrayList<Record>();
                        List<Record> secondaryTableRecords = new ArrayList<Record>();
                        for (Map.Entry<String, List<Record>> entry : existingRecordMap.entrySet()) {
                            String groupKey = entry.getKey();
                            List<Record> groupRecords = entry.getValue();

                            Map<String, String> columns = new HashMap<String, String>();

                            for (Record record : groupRecords) {
                                String key = record.getKey();
                                columns.put(key, "");
                            }

                            Record record = new Record(groupKey, columns);
                            primaryTableRecords.add(record);

                            secondaryTableRecords.addAll(groupRecords);

                        }

                        try {
                            // Store primary table records
                            putRecords(table, primaryTableRecords, false, dataContext);
                        } catch (StoreException e) {
                            log.error("Error storing records to the primary table " + table + "..",
                                      e);
                            // roll back. remove secondary table records
                        }

                        // Store secondary table records
                        putRecords(secondaryTable, secondaryTableRecords, isDoAggregate,
                                   dataContext);

                    } catch (StoreException e) {
                        log.error("Error storing records to the secondary table " + secondaryTable +
                                  "..", e);
                        return;
                    }

                    setData(dataContext, null);

                    // Create indexes if not existing. Not generic.. :(
/*                    Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
                    CassandraIndexConfiguration index = (CassandraIndexConfiguration) properties.
                            get(AnalyzerConfigConstants.INDEX);


                    String indexName = table + "_auto_index";
                    try {
                        if (metaDataManager.getIndexMetaData(getExecutingTenantId(),
                                                             indexName) == null) {
                            IndexConfiguration indexConfiguration = metaDataManager.
                                    createIndexMetaData(indexName, table, table,
                                                        index.getIndexedColumns(), dataSourceType);
                            indexConfiguration.setAutoGenerated(Boolean.TRUE);
                            IndexManager.getInstance().createIndex(indexConfiguration,
                                                                   dataContext.getCredentials());
                        }
                    } catch (ConfigurationException e) {
                        log.error("Unable to store index meta data for index " + indexName + "..",
                                  e);
                    } catch (IndexingException e) {
                        log.error("Unable to store index meta data for index " + indexName + "..",
                                  e);
                    }*/

                }

            }

            Cursor cursor = getAnalyzerSequence().getCursor();

            if (cursor != null) {
                try {
                    metaDataManager.storeCursorMetaData(dataContext.getCredentials(), cursor);
                } catch (ConfigurationException e) {
                    log.error("Error while persisting cursor meta data. This may cause " +
                              "inconsistencies in summarized data..");
                }
            }
        }
    }

    private List<Record> putRecords(String table, List<Record> records,
                                    boolean isDoAggregate, DataContext context)
            throws StoreException {

        if (records == null || records.size() == 0) {
            return null;
        }

        PutConfig config = (PutConfig) getAnalyzerConfig();

        QueryManager queryManager = new QueryManager();

        List<Record> result = new ArrayList<Record>();

        //If same task run concurrently
        synchronized (this) {
            if (records != null) {
                // Persist records to table
                for (Record record : records) {

                    // If the record/s with given key is already existing aggregate with current record
                    if (isDoAggregate) {
                        List<Record> existingRecords = queryManager.getRecords(context.getCredentials(),
                                                                               table, record.getKey(),
                                                                               null);
                        if (existingRecords != null && existingRecords.size() > 0) {
                            List<Record> recordsToAggregate = new ArrayList<Record>();
                            recordsToAggregate.add(record);
                            recordsToAggregate.addAll(existingRecords);

                            record = AnalyzerUtils.aggregateRecords(recordsToAggregate,
                                                                    config.getMeasures(),
                                                                    record.getKey());
                        }

                    }

                    result.add(record);

                }
            }

            PersistenceManager persistenceManager = new PersistenceManager();
            persistenceManager.storeRecords(context.getCredentials(), table, result);
        }

        return result;

    }

}
