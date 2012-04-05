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
import org.wso2.carbon.bam.core.dataobjects.Record;

import java.util.List;
import java.util.Map;

public class LogAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(IndexingAnalyzer.class);

    public LogAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof List) {
                List<Record> records = (List<Record>) result;

                for (Record record : records) {
                    Map<String, String> columns = record.getColumns();
                    String key = record.getKey();

                    log.info("Record key : " + key + "\n");

                    for (Map.Entry<String, String> entry : columns.entrySet()) {
                        String columnKey = entry.getKey();
                        String columnValue = entry.getValue();
                        log.info("\t Column : { Key : " + columnKey + " Value : " +
                                           columnValue + "} \n");
                    }
                }

            } else if (result instanceof Map) {
                Map<String, List> recordMap = (Map<String, List>) result;

                for (Map.Entry<String, List> group : recordMap.entrySet()) {
                    String groupKey = group.getKey();

                    log.info("Group key : " + groupKey + "\n");
                    List<Record> records = group.getValue();

                    for (Record record : records) {
                        String key = record.getKey();

                        log.info("\t Record key : " + key + "\n");

                        Map<String, String> columns = record.getColumns();

                        for (Map.Entry<String, String> entry : columns.entrySet()) {
                            String columnKey = entry.getKey();
                            String columnValue = entry.getValue();

                            log.info("\t\t Column : { Key : " + columnKey + " Value : " +
                                               columnValue + "} \n");
                        }
                    }

                }
            }
        }
    }

}