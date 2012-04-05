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
import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregateConfig;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;

import java.util.*;

public class AggregateAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(AggregateAnalyzer.class);

    public AggregateAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        Object result = getData(dataContext);

        List<Record> results = new ArrayList<Record>();
        if (result != null) {
            if (result instanceof List) {
                List<Record> records = (List<Record>) result;

                String uuid = UUID.randomUUID().toString();
                Record record = AnalyzerUtils.aggregateRecords(
                        records,((AggregateConfig) getAnalyzerConfig()).getMeasures(), uuid);

                results.add(record);

            } else if (result instanceof Map) {
                Map<String, List> recordMap = (Map<String, List>) result;

                for (Map.Entry<String, List> group : recordMap.entrySet()) {
                    List<Record> rows = group.getValue();
                    Record record = AnalyzerUtils.aggregateRecords(rows, ((AggregateConfig) getAnalyzerConfig()).
                            getMeasures(), group.getKey()); // Record key would be the group key

                    results.add(record);
                }
            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " +
                     getAnalyzerSequenceName());
        }

        setData(dataContext, results);

    }

}
