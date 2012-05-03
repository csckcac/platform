/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.cassandra.mgt;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * KeySpace Explorer for Cassandra
 */
public class CassandraExplorerAdmin extends AbstractAdmin {

    private static final StringSerializer stringSerializer = new StringSerializer();

    private static final Log log = LogFactory.getLog(CassandraKeyspaceAdmin.class);

    /**
     * @param keyspaceName Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @return Rows of the given column family
     * @throws CassandraServerManagementException
     *
     */
    private String[] queryRowsforColumnFamily(String keyspaceName, String columnFamily,
                                              String start, String finish, int limit)
            throws CassandraServerManagementException {
        ClusterInformation clusterInformation = new ClusterInformation("admin", "admin");
        clusterInformation.setClusterName("ClusterOne");
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        ClusterAuthenticationUtil clusterAuthenticationUtil =
                new ClusterAuthenticationUtil(super.getHttpSession(),
                        super.getTenantDomain());
        Cluster cluster = clusterAuthenticationUtil.getCluster(clusterInformation);
        Keyspace keyspace = dataAccessService.getKeySpace(cluster, keyspaceName);

        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer,
                        stringSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys(start, finish);
        rangeSlicesQuery.setReturnKeysOnly();
        rangeSlicesQuery.setRowCount(limit);

        ArrayList<String> rowNameslist = new ArrayList<String>();

        QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
        List<Row<String, String, String>> resultList = result.get().getList();
        for (Row<String, String, String> row : resultList) {
            rowNameslist.add(row.getKey());
        }
        String[] rowKeyArray = new String[rowNameslist.size()];
        return rowNameslist.toArray(rowKeyArray);
    }

    public String[] getRowNamesForColumnFamily(String keyspaceName, String columnFamily,
                                               String start, String finish, int limit)
            throws CassandraServerManagementException {
        if ("".equals(start) & "".equals(finish)) { // Query forward
            return this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                    start, finish, limit);
        } else if ("".equals(start) & !("".equals(finish))) { // Query backwards
            String[] window1 = new String[limit];
            String[] window2 = new String[limit];

            boolean justStarted = true;

            while ((window2.length > 1) || justStarted) {
                if (justStarted) {
                    window1 = this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                            "", "", limit);
                    for (int i = 0; i < window1.length; i++) {
                        if (finish.equals(window1[i])) {
                            return this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                                    "", finish, limit);
                        }
                    }
                } else {
                    window1 = this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                            window2[window2.length - 1], "", limit);
                    for (int i = 0; i < window1.length; i++) {
                        if (finish.equals(window1[i])) {
                            return this.queryRowsforColumnFamily(keyspaceName,
                                    columnFamily, window2[i], "", limit);
                        }
                    }
                }
                window2 = this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                        window1[window1.length - 1], "", limit);

                for (int i = 0; i < window2.length; i++) {

                    if (finish.equals(window2[i])) {
                        return this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                                window1[i], "", limit);
                    }
                }
                justStarted = false;
            }
            return new String[0]; // Queried start/finish key is not existing
        } else { // Not valid. Just use results ofqueryRowNamesForColumnFamily
            return this.queryRowsforColumnFamily(keyspaceName, columnFamily,
                    start, finish, limit);
        }
    }

    /**
     * @param keyspaceName Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @param rowName      Row name to get columns
     * @param startKey     Starting key of columns
     * @param lastKey      End key of Column
     * @param isReversed   are the results in reverse order
     * @return Columns
     * @throws CassandraServerManagementException
     *
     */
    private Column[] queryColumnForRow(String keyspaceName, String columnFamily,
                                       String rowName, String startKey, String lastKey,
                                       int limit, boolean isReversed)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        ClusterAuthenticationUtil clusterAuthenticationUtil =
                new ClusterAuthenticationUtil(super.getHttpSession(),
                        super.getTenantDomain());
        Cluster cluster = clusterAuthenticationUtil.getCluster(null);
        Keyspace keyspace = dataAccessService.getKeySpace(cluster, keyspaceName);

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer,
                        stringSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        sliceQuery.setKey(rowName);
        sliceQuery.setRange(startKey, lastKey, isReversed, limit);

        QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
        List<HColumn<String, String>> hColumnsList;
        ArrayList<Column> columnsList = new ArrayList<Column>();

        hColumnsList = result.get().getColumns();
        for (HColumn hColumn : hColumnsList) {
            Column column = new Column();
            column.setName(hColumn.getName().toString());
            column.setValue(hColumn.getValue().toString());
            column.setTimeStamp(hColumn.getClock());
            columnsList.add(column);
        }
        Column[] columnArray = new Column[columnsList.size()];
        return columnsList.toArray(columnArray);
    }

    /**
     * @param keyspaceName
     * @param columnFamily
     * @param rowName
     * @param startKey
     * @param lastKey
     * @param limit
     * @param isReversed
     * @return
     * @throws CassandraServerManagementException
     *
     */
    public Column[] getColumnsForRow(String keyspaceName, String columnFamily, String rowName,
                                     String startKey, String lastKey, int limit, boolean isReversed)
            throws CassandraServerManagementException {
        if ("".equals(startKey) & "".equals(lastKey)) { // Query forward
            return this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                    startKey, lastKey, limit, isReversed);
        } else if ("".equals(startKey) & !("".equals(lastKey))) { // Query backwards
            Column[] window1 = new Column[limit];
            Column[] window2 = new Column[limit];

            boolean justStarted = true;

            while (window2.length > 1 | justStarted) {
                if (justStarted) {
                    window1 = this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                            "", "", limit, isReversed);
                    for (int i = 0; i < window1.length; i++) {
                        if (lastKey.equals(window1[i].getName())) {
                            return this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                                    "", lastKey, limit, isReversed);
                        }
                    }
                } else {
                    window1 = this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                            window2[window2.length - 1].getName(), "", limit, isReversed);
                    for (int i = 0; i < window1.length; i++) {
                        if (lastKey.equals(window1[i].getName())) {
                            return this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                                    window2[i].getName(), "", limit, isReversed);
                        }
                    }
                }
                window2 = this.queryColumnForRow(keyspaceName, columnFamily, rowName,
                        window1[window1.length - 1].getName(), "", limit, isReversed);

                for (int i = 0; i < window2.length; i++) {

                    if (lastKey.equals(window2[i].getName())) {
                        return this.queryColumnForRow(keyspaceName, columnFamily,
                                rowName, window1[i].getName(), "", limit, isReversed);
                    }
                }
                justStarted = false;
            }
            return new Column[0]; // Queried start/finish key is not existing
        } else { // Not valid. Just use results of getColumnsForRowx
            return this.queryColumnForRow(keyspaceName, columnFamily, rowName, startKey,
                    lastKey, limit, isReversed);
        }

    }

    /**
     * @param keySpace     Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @param rowID        Row id to get Columns
     * @param columnKey    Key of Column to retrieve
     * @return Column
     * @throws CassandraServerManagementException
     *
     */
    public Column getColumn(String keySpace, String columnFamily, String rowID, String columnKey)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        ClusterAuthenticationUtil clusterAuthenticationUtil =
                new ClusterAuthenticationUtil(super.getHttpSession(), super.getTenantDomain());
        Cluster cluster = clusterAuthenticationUtil.getCluster(null);
        Keyspace keyspace = dataAccessService.getKeySpace(cluster, keySpace);

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer,
                        stringSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        sliceQuery.setKey(rowID);
        sliceQuery.setColumnNames(columnKey);

        QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
        HColumn hColumn = result.get().getColumnByName(columnKey);

        if (hColumn != null) {
            Column column = new Column();
            column.setName((String) hColumn.getName());
            column.setValue((String) hColumn.getValue());
            column.setTimeStamp(hColumn.getClock());
            return column;
        } else {
            return null;
        }
    }

    /**
     * @param keyspaceName Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @return no Of Rows
     * @throws CassandraServerManagementException
     *
     */
    public int getNoOfRows(String keyspaceName, String columnFamily)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        ClusterAuthenticationUtil clusterAuthenticationUtil =
                new ClusterAuthenticationUtil(super.getHttpSession(),
                        super.getTenantDomain());
        Cluster cluster = clusterAuthenticationUtil.getCluster(null);
        Keyspace keyspace = dataAccessService.getKeySpace(cluster, keyspaceName);

        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer,
                        stringSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys("", "");
        rangeSlicesQuery.setReturnKeysOnly();
        QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
        return result.get().getCount();
    }

    /**
     * Returns columns in last updated first order
     *
     * @param keyspaceName Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @param rowName      Row name to get columns
     * @param startKey     Starting key of columns
     * @param lastKey      End key of Column
     * @param isReversed   are the results in reverse order
     * @return Columns
     * @throws CassandraServerManagementException
     *
     */
    public Column[] getColumnsInUpdateOrder(String keyspaceName, String columnFamily,
                                            String rowName, String startKey, String lastKey,
                                            int limit, boolean isReversed)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        ClusterAuthenticationUtil clusterAuthenticationUtil =
                new ClusterAuthenticationUtil(super.getHttpSession(),
                        super.getTenantDomain());
        Cluster cluster = clusterAuthenticationUtil.getCluster(null);
        System.out.println(cluster.describeClusterName());
        Keyspace keyspace = dataAccessService.getKeySpace(cluster, keyspaceName);

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer,
                        stringSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        sliceQuery.setKey(rowName);
        sliceQuery.setRange(startKey, lastKey, isReversed, limit);
        QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();

        List<HColumn<String, String>> hColumnsList;
        ArrayList<Column> columnsList = new ArrayList<Column>();
        hColumnsList = result.get().getColumns();

        for (HColumn hColumn : hColumnsList) {
            Column column = new Column();
            column.setName(hColumn.getName().toString());
            column.setValue(hColumn.getValue().toString());
            column.setTimeStamp(hColumn.getClock());
            columnsList.add(column);
        }
        Collections.sort(columnsList);
        Column[] columnArray = new Column[columnsList.size()];
        return columnsList.toArray(columnArray);
    }
}
