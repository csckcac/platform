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
import org.wso2.carbon.bam.analyzer.analyzers.configs.GroupByConfig;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.configurations.DataSourceType;
import org.wso2.carbon.bam.core.configurations.Granularity;
import org.wso2.carbon.bam.core.configurations.IndexType;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraIndexConfiguration;
import org.wso2.carbon.bam.core.utils.TimeStampFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Syntax : <groupBy>
 *             +<field name=''/>
 *             (0..1)<time name='' granularity='minute,hour,day,month,year'/>
 *          </groupBy>
 *
 * This analyzer groups the rows according the values of the columns specified by 'field' or 'time'.
 * The result is a map with each unique concatenated value of 'field' columns as key and the list of
 * rows having this unique values in their respective columns, as map value. 'time' is a special
 * field containing a date time whose value can be rounded up to given granularity (e.g. : hour)
 * during the grouping.
 *
 * Example :
 *
 * Input : List of rows as given below.
 *
 *   {employee1} : {{name : ben}, {age : 42}, {post : eng}, {dept : E1}, {joinDate : 2011-01-21} }
 *   {employee2} : {{name : alex}, {age : 35}, {post : admin}, {dept : A1}, {joinDate : 2011-03-24} }
 *   {employee3} : {{name : bob}, {age : 48}, {post : eng}, {dept : E1}, {joinDate : 2011-01-22} }
 *   {employee4} : {{name : sarah}, {age : 26}, {post : admin}, {dept : A2 }, {joinDate : 2008-02-28} }
 *   {employee5} : {{name : ben}, {age : 42}, {post : eng} , {dept : E2}, {joinDate : 2010-07-23} }
 *
 * GroupBy Specification : <groupBy>
 *                            <field name='dept'/>
 *                            <field name='post'/>
 *                            <time name='joinDate' granularity='month'/>
 *                         </groupBy>
 *
 * Output : Rows grouped by each unique concatenated value of columns 'dept', 'post' and 'joinDate'.
 *          Values of 'joinDate' column has been rounded to months as defined by 'granularity'.
 *
 *   [E1---eng---2011-01]   : {employee1} : {{name : ben}, {age : 42}, {post : eng}, {dept : E1}, {joinDate : 2011-01-21} }
 *                            {employee3} : {{name : bob}, {age : 48}, {post : eng}, {dept : E1}, {joinDate : 2011-01-22} }
 *   [E2---eng---2010-07]   : {employee5} : {{name : ben}, {age : 42}, {post : eng} , {dept : E2}, {joinDate : 2010-07-23} }
 *   [A1---admin---2011-03] : {employee2} : {{name : alex}, {age : 35}, {post : admin}, {dept : A1}, {joinDate : 2011-03-24} }
 *   [A2---admin---2008-02] : {employee4} : {{name : sarah}, {age : 26}, {post : admin}, {dept : A2 }, {joinDate : 2008-02-28} }
 *
 * When the input it self is grouped rows new groupings will be formed using each
 * unique value of the 'field' in each of rows found in the groups.
 *
 * Example :
 *
 * Input : List of grouped rows as given below.
 *
 *   [30-plus]      : {employee1} : {{name : ben}, {age : 42}, {post : eng}, {dept : E1}, {joinDate : 2011-01-21} }
 *                    {employee2} : {{name : alex}, {age : 35}, {post : admin}, {dept : A1}, {joinDate : 2011-03-24} }
 *                    {employee3} : {{name : bob}, {age : 48}, {post : eng}, {dept : E1}, {joinDate : 2011-01-22} }
 *                    {employee5} : {{name : ben}, {age : 42}, {post : eng} , {dept : E2}, {joinDate : 2010-07-23} }
 *   [less-than-30] : {employee4} : {{name : sarah}, {age : 26}, {post : admin}, {dept : A2 }, {joinDate : 2008-02-28} }
 *
 * GroupBy Specification : <groupBy>
 *                            <field name='dept'/>
 *                            <field name='post'/>
 *                            <time name='joinDate' granularity='month'/>
 *                         </groupBy>
 *
 * Output : Rows grouped by each unique concatenated value of columns 'dept', 'post' and 'joinDate'.
 *          Values of 'joinDate' column has been rounded to months as defined by 'granularity'.
 *          Old grouping not present.
 *
 *   [E1---eng---2011-01]   : {employee1} : {{name : ben}, {age : 42}, {post : eng}, {dept : E1}, {joinDate : 2011-01-21} }
 *                            {employee3} : {{name : bob}, {age : 48}, {post : eng}, {dept : E1}, {joinDate : 2011-01-22} }
 *   [E2---eng---2010-07]   : {employee5} : {{name : ben}, {age : 42}, {post : eng} , {dept : E2}, {joinDate : 2010-07-23} }
 *   [A1---admin---2011-03] : {employee2} : {{name : alex}, {age : 35}, {post : admin}, {dept : A1}, {joinDate : 2011-03-24} }
 *   [A2---admin---2008-02] : {employee4} : {{name : sarah}, {age : 26}, {post : admin}, {dept : A2 }, {joinDate : 2008-02-28} }
 *
 */
public class GroupByAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(GroupByAnalyzer.class);

    public GroupByAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        Object result = getData(dataContext);

        Map<String, List<Record>> recordMap = new HashMap<String, List<Record>>();
        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                groupRecords(list, recordMap);

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingGroupedRows = (Map<String, List<Record>>)
                        result;

                for (List<Record> list : existingGroupedRows.values()) {
                    groupRecords(list, recordMap);
                }
            } else {
                log.error("Unknown data format in received data for groupBy analyzer..");
            }
        } else {
            log.warn("Data flow empty at groupBy analyzer in sequence : " + getAnalyzerSequenceName());
        }

        setData(dataContext, recordMap);

        // Create and store indexes in data context so that this new cf can be queried later
/*        GroupByConfig config = (GroupByConfig) getAnalyzerConfig();
        List<String> indexedColumns = config.getFields();

        if (indexedColumns == null) {
            indexedColumns = new ArrayList<String>();
        }

        if (config.getGranularity() != null) {
            indexedColumns.add(config.getTimeField());
        }

        // Create temporary index configuration for new grouping. Index defaults to a Cassandra
        // index. Will be re-assigned to the correct index configuration type at the first
        // subsequent put analyzer according to the data source defined there
        CassandraIndexConfiguration indexConfiguration = new CassandraIndexConfiguration(
                AnalyzerConfigConstants.TEMPORARY_INDEX, null, indexedColumns.
                toArray(new String[]{}),
                DataSourceType.CASSANDRA);
        indexConfiguration.setAutoGenerated(Boolean.TRUE);

        Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
        properties.put(AnalyzerConfigConstants.INDEX, indexConfiguration);*/

    }

    private void groupRecords(List<Record> records, Map<String, List<Record>> recordMap) {

        GroupByConfig config = (GroupByConfig) getAnalyzerConfig();
        List<String> fields = config.getFields();

        outer : for (Record record : records) {

            Map<String, String> columns = record.getColumns();

            StringBuffer sb = new StringBuffer();
            for (String field : fields) {
                boolean fieldFound = false;
                String timeStamp = null;
                for (Map.Entry<String, String> entry : columns.entrySet()) {
                    if (entry.getKey().equals(field)) {
                        if (entry.getKey().equals(config.getTimeField())) {
                            try {
                                timeStamp = TimeStampFactory.getFactory().getTimeStamp(
                                        entry.getValue(), Granularity.valueOf(
                                        config.getGranularity()));
                                sb.append(timeStamp);
                                sb.append("---");
                            } catch (ParseException e) {
                                log.error("Invalid timeStamp. TimeStamp will not be " +
                                          "included in the group key..");
                            }

                            fieldFound = true;
                            break;
                        } else {
                            sb.append(entry.getValue());
                            sb.append("---");

                            fieldFound = true;
                            break;
                        }
                    }
                }

                // If a field specified is not found in the columns of this record skip adding this
                // record to any group
                if (!fieldFound) {
                    continue outer;
                }

                // If group by time is specified add back rounded timeStamp to the record instead of
                // original timeStamp
                if (timeStamp != null) {
                    columns.put(config.getTimeField(), timeStamp);
                }
            }

            boolean isMultipleGroupBy = sb.lastIndexOf("---") == -1 ? Boolean.FALSE : Boolean.TRUE;

            String groupKey = sb.toString();
            if (isMultipleGroupBy) {
                groupKey = groupKey.substring(0, (sb.lastIndexOf("---"))); // Remove trailing '---'
            }

            List<Record> groupMembers = recordMap.get(groupKey);

            if (groupMembers == null) {
                groupMembers = new ArrayList<Record>();
            }

            groupMembers.add(record);
            recordMap.put(groupKey, groupMembers);
        }
    }

}
