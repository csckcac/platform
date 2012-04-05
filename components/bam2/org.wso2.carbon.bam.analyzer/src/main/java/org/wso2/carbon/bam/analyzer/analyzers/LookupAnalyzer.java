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
import org.wso2.carbon.bam.analyzer.engine.DataContext;

/*
 * Syntax :
 *
 * <lookup [name='' || default='Event,Meta,Correlation']/>
 *
 * Does look up and fetch rows from the column family specified using 'name' or default
 * column families taking column keys of received rows. In case of default column families comma
 * separated list can be given where columns of each default column family will be combined and
 * fetched per row.
 *
 * Syntax Explanation :
 *
 * Either name or default should be present. Otherwise error will be thrown. The name
 * attribute defines which column family the lookup should happen. The default attribute is a comma
 * separated list of default column families Event, Meta or Correlation.
 *
 * Example :
 *
 * Input : List of rows as given below. Column of each row is a row key of lookup column family.
 *
 *   {key1} : {{uuid1 : ''}, {uuid2 : ''}, {uuid3 : ''} }
 *   {key2} : {{uuid4 : ''}, {uuid5 : ''}, {uuid6 : ''} }
 *   {key3} : {{uuid7 : ''}, {uuid8 : ''}, {uuid9 : ''} }
 *   {key4} : {{uuid10 : ''}, {uuid11 : ''}, {uuid12 : ''} }
 *
 * Output : List of rows from lookup column family.
 *
 *   {uuid1} : {{key1 : value1}, {key2 : value2} }
 *   {uuid2} : {{key3 : value3}, {key4 : value4} }
 *   {uuid3} : {{key5 : value5}, {key6 : value6} }
 *   ....
 *   {uuid12} : {{key23: value23}, {key24 : value24} }
 *
 * When the input is grouped the output will maintain the same grouping structure and each group will
 * contain rows from the lookup column family.
 *
 * Example :
 *
 * Input : List of grouped rows as given below. Column of each row is a row key of lookup column family
 *
 *   [group1]      : {key1} : {{uuid1 : ''}, {uuid2 : ''}, {uuid3 : ''} }
 *                   {key2} : {{uuid4 : ''}, {uuid5 : ''}, {uuid6 : ''} }
 *   [group2]      : {key3} : {{uuid7 : ''}, {uuid8 : ''}, {uuid9 : ''} }
 *                   {key4} : {{uuid10 : ''}, {uuid11 : ''}, {uuid12 : ''} }
 *
 * Output : List of grouped lookup column family rows.
 *
 *   [group1]      : {uuid1} : {{k1 : v1}, {k2 : v2} }
 *                   {uuid2} : {{k3 : v3}, {k4 : v4} }
 *                   ..
 *                   {uuid6} : {{k11 : v11}, {k12 : v12} }
 *   [group2]      : {uuid1} : {{k13 : v13}, {k14 : v14} }
 *                   {uuid2} : {{k15 : v15}, {k16 : v16} }
 *                   ..
 *                   {uuid6} : {{k23 : v23}, {k24 : v24} }
 *
 */

// TODO : Move this into get analyzer. It is more intuitive since this is always used after get.
public class LookupAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(LookupAnalyzer.class);

    public LookupAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

/*
        QueryManager mgr;
        try {
            mgr = QueryManagerFactory.getInstance().getQueryManager(dataContext.getCredentials().get(PersistencyConstants.USER_NAME));
            if (mgr == null) {

                mgr = QueryManagerFactory.getInstance().initializeQueryManager(
                        dataContext.getCredentials());

            }
        } catch (InitializationException e) {
            log.error("Cannot initialize Query manager", e);
            return;
        }

        if (mgr == null) {
            return;
        }

        Object result = getData(dataContext);
        String columnFamily = ((LookupConfig) getAnalyzerConfig()).getColumnFamily();
        String[] defaultCfKeys = ((LookupConfig) getAnalyzerConfig()).getDefaultCfKeys();

        if (result != null) {
            if (result instanceof List) {
                List<ResultRow> list = (List<ResultRow>) result;

                List<ResultRow> resultRows = new ArrayList<ResultRow>();
                for (ResultRow row : list) {
                    List<ResultColumn> columns = row.getColumns();

                    for (ResultColumn column : columns) {
                        String rowKey = column.getKey();

                        ResultRow resultRow = getResultRow(mgr, columnFamily, defaultCfKeys, rowKey);

                        if (resultRow != null) {
                            resultRows.add(resultRow);
                        }
                    }
                }

                setData(dataContext, resultRows);

            } else if (result instanceof Map) {
                Map<String, List<ResultRow>> groupedRows = (Map<String, List<ResultRow>>)
                        result;

                Map<String, List> resultGroupedRows = new HashMap<String, List>();
                for (Map.Entry<String, List<ResultRow>> group : groupedRows.entrySet()) {
                    List<ResultRow> rows = group.getValue();

                    List<ResultRow> resultRows = new ArrayList<ResultRow>();
                    for (ResultRow row : rows) {
                        List<ResultColumn> columns = row.getColumns();

                        for (ResultColumn column : columns) {
                            String rowKey = column.getKey();

                            ResultRow resultRow = getResultRow(mgr, columnFamily, defaultCfKeys,
                                                               rowKey);

                            if (resultRow != null) {
                                resultRows.add(resultRow);
                            }
                        }
                    }

                    resultGroupedRows.put(group.getKey(), resultRows);

                    setData(dataContext, resultGroupedRows);

                }

            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " + getAnalyzerSequenceName());
        }
*/

    }

    /**
     * Fetch from either the named column family or one or more default column families. In case of
     * multiple default column families defined combine the key value pairs and return in single row.
     *
     * @param mgr           QueryManager instance
     * @param cfName        Column family to lookup
     * @param defaultCfKeys Array containing default column family names (Event, Meta, Correlation)
     * @param rowKey        Row to fetch having this key
     * @return
     */
/*    private ResultRow getResultRow(QueryManager mgr, String cfName, String[] defaultCfKeys,
                                   String rowKey) {

        ResultRow resultRow = null;
        if (cfName != null) {
            resultRow = mgr.getColumnFamilyRow(cfName, rowKey, "", "");
        } else if (defaultCfKeys != null && defaultCfKeys.length > 0) {
            List<ResultColumn> combinedColumns = new ArrayList<ResultColumn>();
            for (String defaultCf : defaultCfKeys) {
                resultRow = mgr.getColumnFamilyRow(defaultCf, rowKey, "", "");
                combinedColumns.addAll(resultRow.getColumns());
            }

            resultRow = new ResultRow(rowKey, combinedColumns);
        }

        return resultRow;

    }*/

}

