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
import org.wso2.carbon.bam.analyzer.analyzers.configs.OrderByConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/*
 * Syntax : <orderBy field=''>
 *
 * This analyzer orders the rows according the values of the column specified by 'field'. Ordering
 * is done lexically.
 *
 * Example :
 *
 * Input : List of rows as given below.
 *
 *   {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *   {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *   {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *   {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * OrderBy Specification : <orderBy field='name'/>
 *
 * Output : Rows ordered by each unique value of 'name'.
 *
 *   {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *   {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *   {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *   {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * When the input is grouped rows ordering will happen within groups.
 *
 * Example :
 *
 * Input : List of grouped rows as given below.
 *
 *   [30-plus]      : {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *                    {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *                    {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *   [less-than-30] : {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * OrderBy Specification : <orderBy field='name'/>
 *
 * Output : Rows ordered by name within each group.
 *
 *   [30-plus]      : {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *                    {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *                    {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *   [less-than-30] : {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 */
public class OrderByAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(OrderByAnalyzer.class);

    public OrderByAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                orderRecords(list);

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingGroupedRows = (Map<String, List<Record>>)
                        result;

                for (List<Record> list : existingGroupedRows.values()) {
                    orderRecords(list);
                }
            } else {
                log.error("Unknown data format in received data for orderBy analyzer..");
            }
        } else {
            log.warn("Data flow empty at orderBy analyzer in sequence : " + getAnalyzerSequenceName());
        }

        setData(dataContext, result);

    }

    private void orderRecords(List<Record> list) {

        OrderByConfig config = (OrderByConfig) getAnalyzerConfig();

        Comparator<Record> rowComparator = new RecordComparator(config.getField());
        Collections.sort(list, rowComparator);
    }

    private class RecordComparator implements Comparator<Record> {

        private String field;

        public RecordComparator(String field) {
            this.field = field;
        }

        public int compare(Record record, Record nextRecord) {

            String fieldValue =findFieldValue(record);
            String nextFieldValue = findFieldValue(nextRecord);

            if (fieldValue != null) {
                return fieldValue.compareTo(nextFieldValue);
            } else {
                return -1; // If field or it's value null we consider this row to be lesser than a row
                          // having the field
            }
        }

        private String findFieldValue(Record record) {
            Map<String, String> columns = record.getColumns();
            return columns.get(field);
        }

    }

}
