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

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.MetaDataManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.QueryIndex;
import org.wso2.carbon.bam.core.persistence.StoreFetcher;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraStoreFetcher implements StoreFetcher {

    private static final int RANGE_FIRST_INDEX = 0;

    private static final int RANGE_LAST_INDEX = 1;

    private Keyspace keyspace;

    private int tenantId;

    private Map<String, String> credentials;

    private static StringSerializer stringSerializer = StringSerializer.get();

    CassandraDataStore store = null;

    @Override
    public void initialize(Map<String, String> credentials) throws StoreException {
        this.credentials = credentials;
        String userName = credentials.get(PersistencyConstants.USER_NAME);
        String password = credentials.get(PersistencyConstants.PASSWORD);

        ClusterInformation clusterInfo = new ClusterInformation(userName, password);
        clusterInfo.setClusterName(userName);

        Cluster cluster = CassandraUtils.createCluster(clusterInfo);

        // Initialize and create meta column families along with column family meta data if not existing
        store = (CassandraDataStore)CassandraStoreFactory.getInstance().getDataStore(credentials);

        keyspace = store.getKeySpace();

/*        KeyspaceDefinition keySpaceDef = cluster.describeKeyspace(
                PersistencyConstants.BAM_KEY_SPACE);

        if (keySpaceDef == null) {
            throw new StoreException("Data store has not been properly initialized..");
        }*/

        //keyspace = HFactory.createKeyspace(PersistencyConstants.BAM_KEY_SPACE, cluster);
        tenantId = Utils.getTenantIdFromUserName(PersistencyConstants.USER_NAME);
    }

    @Override
    public List<String> fetchIndexValues(String indexName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> fetchTableColumns(String cfName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Record> fetchRecords(String cfName, String rowKey, List<String> filterByColumns)
            throws StoreException {
        CassandraCFConfiguration configuration;
        try {
            configuration = (CassandraCFConfiguration) MetaDataManager.
                    getInstance().getTableMetaData(tenantId, cfName);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch column family meta data..", e);
        }

        List<Record> result = new ArrayList<Record>();

        List<HColumn<String, String>> columns = getColumnsOfRow(cfName, rowKey, "", "",
                                                                Integer.MAX_VALUE);
        if (configuration.isPrimaryTable() &&
            configuration.getSecondaryTables() != null &&
            configuration.getSecondaryTables().size() > 0) {

            for (HColumn<String, String> column : columns) {
                String secondaryCfRecordKey = column.getName();
                List<String> secondaryCfs = configuration.getSecondaryTables();

                List<String> clonedSecondaryCfs = new ArrayList<String>();
                for (String secondaryCf : secondaryCfs) {
                    clonedSecondaryCfs.add(secondaryCf);
                }

                String firstSecondaryCf = clonedSecondaryCfs.get(0);

                List<Record> secondaryCfRecords = fetchRecords(firstSecondaryCf,
                                                               secondaryCfRecordKey,
                                                               filterByColumns);

                // Only one level of foreign key relations are assumed. So only one record should be
                // returned when querying a secondary column family.
                List<Record> secondaryRecords = new ArrayList<Record>();
                if (secondaryCfRecords != null && secondaryCfRecords.size() > 0) {
                    Record secondaryRecord = secondaryCfRecords.get(0);

                    Map<String, String> secondaryCfColumns = secondaryRecord.getColumns();

                    clonedSecondaryCfs.remove(0); // Remove first cf from cf list

                    // Add other secondary column family record columns to this record.
                    for (String secondaryCf : clonedSecondaryCfs) {
                        List<Record> records = fetchRecords(secondaryCf, secondaryCfRecordKey,
                                                            filterByColumns);

                        // Only one level of foreign key relations are assumed.
                        // So only one record should be returned when querying a secondary column
                        // family.
                        if (records != null && records.size() > 0) {
                            secondaryCfColumns.putAll(records.get(0).getColumns());
                        }
                    }

                    secondaryRecords.add(secondaryRecord);

                }

                result.addAll(secondaryRecords);
            }
        } else {
            Map<String, String> columnsMap = new HashMap<String, String>();
            for (HColumn<String, String> column : columns) {
                if (filterByColumns != null && filterByColumns.contains(column.getName())) {
                    columnsMap.put(column.getName(), column.getValue());
                } else if (filterByColumns == null) {
                    columnsMap.put(column.getName(), column.getValue());
                }
            }

            result.add(new Record(rowKey, columnsMap));
        }

        return result;
    }

    @Override
    public List<Record> fetchRecords(String cfName, QueryIndex index,
                                     List<String> filterByColumns) throws StoreException {

        List<Record> result = new ArrayList<Record>();

        if (index == null) {
            List<HColumn<String, String>> rowIndexColumns = getColumnsOfRow(
                    cfName, PersistencyConstants.ROW_INDEX, "", "", Integer.MAX_VALUE);

            for (HColumn<String, String> rowIndexColumn : rowIndexColumns) {
                String rowKey = rowIndexColumn.getName();
                List<Record> rows = fetchRecords(cfName, rowKey, filterByColumns);
                result.addAll(rows);
            }

            return result;

        }

        String indexName = index.getIndexName();

        IndexConfiguration indexConfiguration;
        try {
            indexConfiguration = MetaDataManager.getInstance().getIndexMetaData(
                    credentials, indexName);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch index meta data..", e);
        }

        if (indexConfiguration == null) {
            throw new StoreException("Index not found for index name " + indexName + "..");
        }

        if (!(indexConfiguration instanceof CassandraIndexConfiguration)) {
            throw new StoreException("Invalid index type. Expected Cassandra index..");
        }

        CassandraIndexConfiguration cassandraIndexConfiguration = (CassandraIndexConfiguration)
                indexConfiguration;
        String indexingCf = cassandraIndexConfiguration.getIndexingColumnFamily();

        List<String> ranges = getIndexRanges(index, indexConfiguration);

        List<HColumn<String, String>> rowIndexColumns = getColumnsOfRow(
                indexingCf, PersistencyConstants.ROW_INDEX, ranges.get(RANGE_FIRST_INDEX),
                ranges.get(RANGE_LAST_INDEX), Integer.MAX_VALUE);

        for (HColumn<String, String> rowIndexColumn : rowIndexColumns) {
            String rowKey = rowIndexColumn.getName();
            List<Record> rows = fetchRecords(indexingCf, rowKey, filterByColumns);
            result.addAll(rows);
        }

        return result;

    }

    @Override
    public List<Record> fetchRecords(String cfName, QueryIndex index, List<String> filterByColumns,
                                     int batchSize, Cursor cursor) throws StoreException {

        List<Record> result = new ArrayList<Record>();

        if (index == null) {

            MetaDataManager manager = MetaDataManager.getInstance();
            Cursor lastCursor;
            try {
                lastCursor = manager.getCursorMetaData(credentials, cfName, cursor.getCursorName());
            } catch (ConfigurationException e) {
                throw new StoreException("Unable to fetch cursor meta data..", e);
            }

            String resumePoint = null;

            if (lastCursor != null) {
                resumePoint = lastCursor.getResumePoint();
            }

            List<HColumn<String, String>> timeStampIndexColumns = getColumnsOfRow(
                    cfName, PersistencyConstants.TIMESTAMP_INDEX, resumePoint, "", batchSize);

            for (HColumn<String, String> timeStampIndexColumn : timeStampIndexColumns) {

                String rowKey = timeStampIndexColumn.getValue();
                List<Record> rows = fetchRecords(cfName, rowKey, filterByColumns);
                result.addAll(rows);

            }

            if (timeStampIndexColumns.size() > 0) {
                HColumn<String, String> lastTimeStampIndex = timeStampIndexColumns.get(
                        timeStampIndexColumns.size() - 1);
                String nextResumePoint = lastTimeStampIndex.getName();
                cursor.setTable(cfName);
                cursor.setResumePoint(getNextStringInLexicalOrder(nextResumePoint));

                /*try {
                    manager.storeCursorMetaData(credentials, cursor);
                } catch (ConfigurationException e) {
                    throw new StoreException("Unable to persist cursor meta data..", e);
                }*/
            }

            return result;

        }

        String indexName = index.getIndexName();

        IndexConfiguration indexConfiguration;
        try {
            indexConfiguration = MetaDataManager.getInstance().getIndexMetaData(
                    credentials, indexName);
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch index meta data..", e);
        }

        if (indexConfiguration == null) {
            throw new StoreException("Index not found for index name " + indexName + "..");
        }

        if (!(indexConfiguration instanceof CassandraIndexConfiguration)) {
            throw new StoreException("Invalid index type. Expected Cassandra index..");
        }

        CassandraIndexConfiguration cassandraIndexConfiguration = (CassandraIndexConfiguration)
                indexConfiguration;
        String indexingCf = cassandraIndexConfiguration.getIndexingColumnFamily();

        List<String> ranges = getIndexRanges(index, indexConfiguration);

        MetaDataManager manager = MetaDataManager.getInstance();
        Cursor lastCursor;
        try {
            lastCursor = manager.getCursorMetaData(credentials, indexingCf, cursor.getCursorName());
        } catch (ConfigurationException e) {
            throw new StoreException("Unable to fetch cursor meta data..", e);
        }

        String resumePoint = null;

        if (lastCursor != null) {
            resumePoint = lastCursor.getResumePoint();
        }

        if (resumePoint == null) {
            resumePoint = "";
        }

        List<HColumn<String, String>> timeStampIndexColumns = getColumnsOfRow(
                indexingCf, PersistencyConstants.TIMESTAMP_INDEX, resumePoint, "", batchSize);

        String rangeFirst = ranges.get(RANGE_FIRST_INDEX);
        String rangeLast = ranges.get(RANGE_LAST_INDEX);

        for (HColumn<String, String> timeStampIndexColumn : timeStampIndexColumns) {
            String rowKey = timeStampIndexColumn.getValue();

            if (rowKey.compareTo(rangeFirst) >= 0 && rowKey.compareTo(rangeLast) < 0) {
                List<Record> rows = fetchRecords(indexingCf, rowKey, filterByColumns);
                result.addAll(rows);
            } else if ("".equals(rangeFirst) && "".equals(rangeLast)) {  // If ranges are both empty we want to fetch all the data without any filtering
                List<Record> rows = fetchRecords(indexingCf, rowKey, filterByColumns);
                result.addAll(rows);
            }

        }

        if (timeStampIndexColumns.size() > 0) {
            HColumn<String, String> lastTimeStampIndex = timeStampIndexColumns.get(
                    timeStampIndexColumns.size() - 1);
            String nextResumePoint = lastTimeStampIndex.getName();
            cursor.setTable(indexingCf);
            cursor.setResumePoint(getNextStringInLexicalOrder(nextResumePoint));

            try {
                manager.storeCursorMetaData(credentials, cursor);
            } catch (ConfigurationException e) {
                throw new StoreException("Unable to persist cursor meta data..", e);
            }
        }

        return result;

    }

    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.CASSANDRA;
    }

    /*        Private helper methods        */

    private List<HColumn<String, String>> getColumnsOfRow(String cfName, String rowKey,
                                                          String rangeFirst, String rangeLast,
                                                          int batchSize) {


        if (store.isTableExists(cfName)) {
            MultigetSliceQuery<String, String, String> multigetSliceQuery =
                    HFactory.createMultigetSliceQuery(keyspace, stringSerializer,
                                                      stringSerializer, stringSerializer);
            multigetSliceQuery.setColumnFamily(cfName);
            multigetSliceQuery.setKeys(rowKey);
            multigetSliceQuery.setRange(rangeFirst, rangeLast, false, batchSize);
            QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();

            Row<String, String, String> indexRow = result.get().getByKey(rowKey);
            List<HColumn<String, String>> list = indexRow.getColumnSlice().getColumns();

            return list;
        }

        return null;

    }

    private List<String> getIndexRanges(QueryIndex index, IndexConfiguration configuration)
            throws StoreException {

        if (index == null) {
            return getDefaultRanges();
        }

        String[] indexedColumns = configuration.getIndexedColumns();
        Map<String, List<String>> indexRanges = index.getCompositeRanges();

        int compositeColumnsInQuery;
        if (indexRanges != null) {
            compositeColumnsInQuery = indexRanges.size();
        } else {
            return getDefaultRanges();
        }

        StringBuilder rangeFirst = new StringBuilder("");
        StringBuilder rangeLast = new StringBuilder("");
        for (String indexedColumn : indexedColumns) {
            if (compositeColumnsInQuery > 0) {
                List<String> rangesForIndex = indexRanges.get(indexedColumn);

                if (rangesForIndex == null && compositeColumnsInQuery > 0) {
                    throw new StoreException("Unable to find column " + indexedColumn +
                                             " in defined composite index");
                } else if (rangesForIndex == null) {
                    return null;
                }

                if (rangesForIndex.size() == 2 || rangesForIndex.get(0) != null ||
                    rangesForIndex.get(1) != null) {
                    rangeFirst.append(rangesForIndex.get(RANGE_FIRST_INDEX));
                    rangeFirst.append(PersistencyConstants.INDEX_DELIMITER);

                    rangeLast.append(rangesForIndex.get(RANGE_LAST_INDEX));
                    rangeLast.append(PersistencyConstants.INDEX_DELIMITER);
                }

                compositeColumnsInQuery--;
            } else {
                break;
            }

        }

        String rangeFirstStr = rangeFirst.toString();
        if (!"".equals(rangeFirstStr)) {
            rangeFirstStr = rangeFirstStr.substring(0, (rangeFirstStr.lastIndexOf(
                    PersistencyConstants.INDEX_DELIMITER)));
        }

        String rangeLastStr = rangeLast.toString();
        if (!"".equals(rangeLastStr)) {
            rangeLastStr = rangeLastStr.substring(0, (rangeLastStr.lastIndexOf(
                    PersistencyConstants.INDEX_DELIMITER)));
        }

        List<String> ranges = new ArrayList<String>();
        ranges.add(rangeFirstStr);
        ranges.add(rangeLastStr);

        return ranges;

    }

    private List<String> getDefaultRanges() {
        List<String> ranges = new ArrayList<String>();
        ranges.add("");
        ranges.add("");

        return ranges;
    }


    public String getNextStringInLexicalOrder(String str) {

        if ((str == null) || (str.equals(""))) {
            return str;
        }

        byte[] bytes = str.getBytes();

        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle
        //  overflows.

        bytes[bytes.length - 1] = last;

        return new String(bytes);
    }

}
