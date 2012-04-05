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

import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregationMeasure;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregationType;
import org.wso2.carbon.bam.core.dataobjects.Record;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalyzerUtils {

    public static Record aggregateRecords(List<Record> records, List<AggregationMeasure> measures,
                                          String key) {
        Map<String, String> counts = new HashMap<String, String>();
        Map<String, String> otherColumns = new HashMap<String, String>();

        long rowCount = 0;
        for (Record record : records) {
            Map<String, String> columns = record.getColumns();
            rowCount++;

            for (AggregationMeasure measure : measures) {

                Iterator<Map.Entry<String, String>> iterator = columns.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();

                    if (measure.getName().equals(entry.getKey())) {
                        String runningCountStr = counts.get(measure.getName());

                        Double runningCount;
                        if (runningCountStr != null) {
                            runningCount = Double.parseDouble(runningCountStr); // TODO: Add type check
                        } else {
                            runningCount = 0.0;
                        }

                        Double current = Double.parseDouble(entry.getValue());

                        Double newRunningCount = getNewRunningCount(runningCount, current, rowCount,
                                                                    measure.getAggregationType());

                        counts.put(measure.getName(), newRunningCount.toString());

                        iterator.remove();

                        break;

                    }
                }
            }

            // Process remaining columns of this record for equal columns of all the records
            for (Map.Entry<String, String> entry : columns.entrySet()) {

                if (otherColumns.containsKey(entry.getKey())) {
                    String columnValue = otherColumns.get(entry.getKey());

                    if (columnValue != null) {
                        if (!columnValue.equals(entry.getValue())) {
                            otherColumns.put(entry.getKey(), null);
                        }
                    }
                    
                } else {
                    otherColumns.put(entry.getKey(), entry.getValue());    
                }
            }
        }

        // Remove non unique columns
        Iterator<Map.Entry<String, String>> iterator = otherColumns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            if (entry.getValue() == null) {
                iterator.remove();
            }
        }

        // Add columns having similar values in all the records of this group
        counts.putAll(otherColumns);

        Record record = new Record(key, counts);
        return record;
    }

    public boolean isResultEmpty(Object result) {
        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                if (list.size() == 0) {
                    return true;
                } else {
                    return false;
                }

            } else if (result instanceof Map) {
                Map map = (Map) result;

                if (map.size() == 0) {
                    return true;
                } else {
                    return false;
                }

            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static double getNewRunningCount(double runningCount, double current, double entries,
                                             AggregationType aggregationType) {
        double newRunningCount = runningCount;
        if (aggregationType.equals(AggregationType.MIN)) {
            if (runningCount > current) {
                newRunningCount = current;
            }
        } else if (aggregationType.equals(AggregationType.MAX)) {
            if (runningCount < current) {
                newRunningCount = current;
            }
        } else if (aggregationType.equals(AggregationType.CUMULATIVE)) {
            newRunningCount = current;
        } else if (aggregationType.equals(AggregationType.SUM)) {
            newRunningCount = runningCount + current;
        } else if (aggregationType.equals(AggregationType.AVG)) {
            double total = (runningCount * (entries - 1)) + current;
            newRunningCount = total / entries;
        }

        return newRunningCount;
    }

}
