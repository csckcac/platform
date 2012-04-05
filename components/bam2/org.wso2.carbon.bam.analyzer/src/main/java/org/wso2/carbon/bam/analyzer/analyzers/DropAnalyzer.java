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
import org.wso2.carbon.bam.analyzer.analyzers.configs.DropConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.FilterField;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Syntax :
 *
 * <drop type="group|row|column">
 *      [fieldSet] || [groupSet]
 * </drop>
 *
 * [fieldSet] := <fieldSet matchUsing='and|or'>
 *                  +<field name="" (0..1)regex=""/>
 *              </fieldSet>
 *
 * [groupSet] := <groupSet>
 *                  +<group regex=""/>
 *              </groupSet>
 *
 * Drops groups, rows or columns from the data flow fulling the given criteria.
 *
 * Syntax Explanation :
 *
 *   <drop type="group|row|column">              : What type of data to drop is defined by 'type'.
 *
 *   <fieldSet (0..1)matchUsing='and|or'>        : The attribute 'matchUsing' defines the semantics which should
 *                                                 be used to match. e.g.: 'and' will drop a row if all the
 *                                                 criteria defined by the one more fields defined in the
 *                                                 fieldSet is satisfied. Default is 'and'. This attribute
 *                                                 is not applicable for 'column' type.
 *       <field name="" (0..1)regex=""/>         : Defines the criteria to drop rows or columns. Specifying
 *   </fieldSet>                                   multiple fields will match all the fields. ('AND' semantics)
 *                                                    name : Drop row if this column exists or drop the column
 *                                                           from the row.
 *                                                    regex : Additionally match column value with regex. Drop
 *                                                            only regex is matched.
 *
 *   <groupSet>
 *     <group regex=''/>                         : Defines the criteria to drop groups. Multiple groups to be
 *   </groupSet>                                   dropped can be specified.
 *                                                 regex : Match group name with regex. Drop group if group
 *                                                         name matches regex.
 *
 * For 'group' type groupSet specification should be used and for 'row' or 'column' types fieldSet
 * specification should be used.
 *
 * Example : Dropping rows.
 *
 * Input : List of rows as given below.
 *
 *      {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *      {employee2} : {{name : alex}, {age : 35}, {post : admin}, {id : EB233}} }
 *      {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *      {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * Drop Specification : <drop type='row'>
 *                         <fieldSet matchUsing='or'>
 *                            <field name='id'/>
 *                            <field name='post' regex='eng*'/>
 *                         </fieldSet>
 *                      </drop>
 *
 * Output : List of filtered rows as below with row matching filtering criteria dropped.
 *
 *      {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * Example : Dropping columns.
 *
 * Input : List of rows as given below.
 *
 *      {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *      {employee2} : {{name : alex}, {age : 35}, {post : admin}, {id : EB233}} }
 *      {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *      {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * Drop Specification : <drop type='column'>
 *                         <fieldSet>
 *                            <field name='id'/>
 *                            <field name='post' regex='eng*'/>
 *                         </fieldSet>
 *                      </drop>
 *
 * Output : List of rows with filtered columns.
 *
 *      {employee1} : {{name : ben}, {age : 42} }
 *      {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *      {employee3} : {{name : bob}, {age : 48} }
 *      {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * Example : Dropping groups.
 *
 * Input : List of grouped rows.
 *
 *      [30-plus]      : {employee1} : {{name : ben}, {age : 42}, {post : eng} }
 *                       {employee2} : {{name : alex}, {age : 35}, {post : admin} }
 *                       {employee3} : {{name : bob}, {age : 48}, {post : eng} }
 *      [less-than-30] : {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 * Drop Specification : <drop type='group'>
 *                         <groupSet>
 *                            <group regex='30-plus'/>
 *                         </groupSet>
 *                      </drop>
 *
 * Output : List of filtered groups.
 *
 *      [less-than-30] : {employee4} : {{name : sarah}, {age : 26}, {post : admin} }
 *
 *
 * Drops rows from data flow fulfilling given criteria.
 * Configuration : <drop [group='' (o..1)field='' || group='' field='' regex=''] || [field='' (0..1)regex='']
 * 1. At least group or field specifiers should be present.
 * 2. If field specifier is present regex specifier may or may not be present. But regex specifier
 * without field specifier is not allowed.
 * <p/>
 * Specifiers :
 * 1. Group = Removes the rows belonging to named group from the data flow.
 * 2. Field = Removes the rows having named field from data flow.
 * 3. Regex = If this is defined in behaviour of Field specifier is modified as follows. Rows having
 */
public class DropAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(DropAnalyzer.class);

    public DropAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {
        String type = ((DropConfig) getAnalyzerConfig()).getType();

        if (type.equalsIgnoreCase(AnalyzerConfigConstants.ROW)) {
            filterRows(dataContext);
        } else if (type.equalsIgnoreCase(AnalyzerConfigConstants.COLUMN)) {
            filterColumns(dataContext);
        } else if (type.equalsIgnoreCase(AnalyzerConfigConstants.GROUP)) {
            filterGroups(dataContext);
        }
    }

    private void filterRows(DataContext dataContext) {
        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                filterRecordsFromList(list);

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingGroupedRows = (Map<String, List<Record>>)
                        result;

                for (List<Record> list : existingGroupedRows.values()) {
                    filterRecordsFromList(list);
                }
            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " + getAnalyzerSequenceName());
        }
    }

    private void filterRecordsFromList(List<Record> list) {
        String matchUsing = ((DropConfig) getAnalyzerConfig()).getMatchUsing();
        List<FilterField> filters = ((DropConfig) getAnalyzerConfig()).getFieldFilters();
        if (matchUsing.equalsIgnoreCase(AnalyzerConfigConstants.AND_SEMANTIC)) {

            Iterator<Record> listIterator = list.iterator();
            while (listIterator.hasNext()) {

                Record record = listIterator.next();
                boolean shouldDrop = true;
                for (FilterField filter : filters) {
                    String filterField = filter.getName();
                    String regex = filter.getRegex();

                    boolean fieldFound = false;
                    Map<String, String> columns = record.getColumns();
                    for (Map.Entry<String, String> entry : columns.entrySet()) {
                        if (filterField.equals(entry.getKey())) {
                            if (regex != null) {
                                Pattern pattern;
                                if (regex != null) {
                                    pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(entry.getValue());

                                    if (!matcher.matches()) {
                                        shouldDrop = false;
                                    }
                                }
                            }

                            fieldFound = true;
                            break;
                        }
                    }

                    if (!fieldFound) {
                        shouldDrop = false;
                        break;
                    }
                }

                if (shouldDrop) {
                    listIterator.remove();
                }
            }

        } else if (matchUsing.equalsIgnoreCase(AnalyzerConfigConstants.OR_SEMANTIC)) {
            Iterator<Record> listIterator = list.iterator();

            outerLoop:
            while (listIterator.hasNext()) {

                Record record = listIterator.next();
                for (FilterField filter : filters) {
                    String filterField = filter.getName();
                    String regex = filter.getRegex();

                    Map<String, String> colums = record.getColumns();
                    for (Map.Entry<String, String> entry : colums.entrySet()) {
                        if (filterField.equals(entry.getKey())) {
                            if (regex != null) {
                                Pattern pattern;
                                if (regex != null) {
                                    pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(entry.getValue());

                                    if (!matcher.matches()) {
                                        listIterator.remove();
                                        continue outerLoop;
                                    }
                                }
                            } else {
                                listIterator.remove();
                                continue outerLoop;
                            }
                        }
                    }
                }

            }
        }
    }

    private void filterColumns(DataContext dataContext) {
        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                filterColumnsFromList(list);

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingGroupedRows = (Map<String, List<Record>>)
                        result;

                for (List<Record> list : existingGroupedRows.values()) {
                    filterColumnsFromList(list);
                }
            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " +
                     getAnalyzerSequenceName());
        }
    }

    private void filterColumnsFromList(List<Record> list) {
        List<FilterField> filters = ((DropConfig) getAnalyzerConfig()).getFieldFilters();
        for (Record record : list) {

            Map<String, String> columns = record.getColumns();

            for (FilterField field : filters) {
                Iterator<Map.Entry<String, String>> columnIterator = columns.entrySet().iterator();
                while (columnIterator.hasNext()) {
                    Map.Entry<String, String> column = columnIterator.next();

                    String filterField = field.getName();
                    String regex = field.getRegex();
                    if (filterField.equals(column.getKey())) {
                        if (regex != null) {
                            Pattern pattern;
                            if (regex != null) {
                                pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(column.getValue());

                                if (!matcher.matches()) {
                                    columnIterator.remove();
                                }
                            }
                        } else {
                            columnIterator.remove();
                        }

                        break;
                    }
                }
            }
        }
    }

    private void filterGroups(DataContext dataContext) {
        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                log.warn("Mismatch in drop analyzer input. Expected : grouped rows Received :" +
                         " row list. Data flow will not be modified..");
            } else if (result instanceof Map) {
                Map<String, List<Record>> existingRecordMap = (Map<String, List<Record>>)
                        result;
                List<String> groupFilters = ((DropConfig) getAnalyzerConfig()).getGroupFilters();

                for (String groupFilter : groupFilters) {
                    Iterator<Map.Entry<String, List<Record>>> entryIterator =
                            existingRecordMap.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        String groupKey = entryIterator.next().getKey();
                        Pattern pattern;
                        pattern = Pattern.compile(groupFilter);
                        Matcher matcher = pattern.matcher(groupKey);

                        if (matcher.matches()) {
                            existingRecordMap.remove(groupKey);
                        }
                    }
                }
            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " +
                     getAnalyzerSequenceName());
        }
    }

}
