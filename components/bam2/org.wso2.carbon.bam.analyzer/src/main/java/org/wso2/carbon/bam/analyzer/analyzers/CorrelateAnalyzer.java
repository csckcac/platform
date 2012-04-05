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

import org.wso2.carbon.bam.analyzer.engine.DataContext;

public class CorrelateAnalyzer extends AbstractAnalyzer {

    public CorrelateAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    @Override
    public void analyze(DataContext dataContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

/*    private static final Log log = LogFactory.getLog(CorrelateAnalyzer.class);

    public static final String INDEX_COLUMN_FAMILY_NAME = "CFIndexes";

    private String outputLocation = INDEX_COLUMN_FAMILY_NAME;


    public CorrelateAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {
        Map properties = dataContext.
                getSequenceProperties(getAnalyzerSequenceName());
        Boolean isGrouped = (Boolean) properties.get(AnalyzerConfigConstants.IS_GROUPED);

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

        if (mgr == null) {
            return;
        }

        if (isGrouped == null || !isGrouped) {
            List<ResultRow> rows = (List<ResultRow>) properties.get(AnalyzerConfigConstants.RESULT);

            String orderBy = ((CorrelateConfig) getAnalyzerConfig()).getOrderBy();
            String nodeIdentifier = ((CorrelateConfig) getAnalyzerConfig()).getNodeIdentifier();


            for (ResultRow row : rows) {
                List<ResultColumn> columns = row.getColumns();

                Collection<ResultRow> events = lookupEvents(columns, mgr);

                if (orderBy != null) {
                    events = reorderEvents(events, mgr);
                }

                List<String> nodeSequence = new ArrayList<String>();
                for (ResultRow event : events) {
                    List<ResultColumn> fields = event.getColumns();

                    for (ResultColumn field : fields) {
                        if (field.getKey().equals(nodeIdentifier)) {
                            nodeSequence.add(field.getValue());
                            break;
                        }
                    }
                }

                StringBuffer sb = new StringBuffer();
                for (String node : nodeSequence) {
                    sb.append(node);
                    sb.append(">");
                }

                sb.deleteCharAt(sb.lastIndexOf(">"));

                String nodes = sb.toString();

                ResultRow resultRow = mgr.getColumnFamilyRow(outputLocation, "paths", nodes,
                        nodes);
                List<ResultColumn> resultColumns = resultRow.getColumns();

                long count = 0;
                if (resultColumns != null) {

                    for (ResultColumn resultColumn : resultColumns) {
                        if (resultColumn.getKey().equals(nodes)) {
                            String countStr = resultColumn.getValue();
                            count = Long.parseLong(countStr);
                            break;
                        }
                    }
                }

                Map<String, String> data = new HashMap<String, String>();
                data.put(nodes, Long.toString(++count));

                NoSQLDataStore store = null;
                try {
                    store = (NoSQLDataStore)NoSQLDataStoreFactory.getInstance().getDataStore(
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

                store.persistData(outputLocation, "paths", data);

            }

            ResultRow resultRow = mgr.getColumnFamilyRow(outputLocation, "paths", "", "");
            List<ResultColumn> resultColumns = resultRow.getColumns();

            for (ResultColumn resultColumn : resultColumns) {
                System.out.println("Path : " + resultColumn.getKey() + " Count : " + resultColumn.getValue());
            }

        }
    }

    private List<ResultRow> lookupEvents(List<ResultColumn> columns, QueryManager mgr) {
        List<ResultRow> events = new ArrayList<ResultRow>();
        String lookup = ((CorrelateConfig) getAnalyzerConfig()).getLookup();

        for (ResultColumn column : columns) {
            ResultRow event = mgr.getColumnFamilyRow(lookup, column.getKey(), "", "");
            events.add(event);
        }

        return events;
    }

    private Collection<ResultRow> reorderEvents(Collection<ResultRow> events, QueryManager mgr) {
        Map<String, ResultRow> orderedMap = new TreeMap<String, ResultRow>();

        String orderBy = ((CorrelateConfig) getAnalyzerConfig()).getOrderBy();

        for (ResultRow event : events) {
            List<ResultColumn> fields = event.getColumns();

            String orderKeyValue = null;
            for (ResultColumn field : fields) {
                if (field.getKey().equals(orderBy)) {
                    orderKeyValue = field.getValue();
                    break;
                }
            }

            // Handles duplicate order keys (duplicate timestamps etc.) by incrementing later
            // duplicated keys
            ResultRow duplicate = orderedMap.get(orderKeyValue);
            if (duplicate != null) {
                String incrementedKey = mgr.getNextStringInLexicalOrder(orderKeyValue);

                while (orderedMap.get(incrementedKey) != null) {
                    incrementedKey = mgr.getNextStringInLexicalOrder(incrementedKey);
                }

                orderKeyValue = incrementedKey;
            }

            orderedMap.put(orderKeyValue, event);
        }

        return orderedMap.values();
    }*/

}
