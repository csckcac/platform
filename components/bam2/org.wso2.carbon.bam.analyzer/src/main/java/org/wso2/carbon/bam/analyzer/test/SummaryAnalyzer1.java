/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.analyzer.test;

import me.prettyprint.cassandra.serializers.StringSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;
import org.wso2.carbon.bam.analyzer.engine.DataContext;

public class SummaryAnalyzer1 implements Analyzer {

    //    private static final String KEYSPACE_NAME = ReceiverConstants.BAM_KEYSPACE;
    private static final String CF_NAME = "SUMMARY";
    private static final StringSerializer stringSerializer = StringSerializer.get();
    private static Log log = LogFactory.getLog(SummaryAnalyzer1.class);

    private String analyzerSequenceName;

    private int positionInSequence;

    @Override
    public void analyze(DataContext dataContext) {
        log.info("Summary Analyzer 1 started");

//
/*        RangeSlicesQuery<String, String, String> rangeQuery = HFactory.createRangeSlicesQuery(dataContext.getBamKeySpace(), stringSerializer, stringSerializer, stringSerializer);

        for (String cfNames : dataContext.getColumnFamilyNames()) {
        
        rangeQuery.setColumnFamily(cfNames);
        rangeQuery.setRange("", "", false, 10000);
        rangeQuery.setKeys("", "");
        rangeQuery.setRowCount(10000);



        QueryResult<OrderedRows<String,String,String>> result = rangeQuery.execute();

        log.info("Column Family name : " + cfNames);
        for (Row<String, String, String> superRow : result.get().getList())  {
            log.info(superRow + "\n Number of columns in row : " + superRow.getColumnSlice().getColumns().size());
//            for (HColumn<String, String> column : superRow.getColumnSlice().getColumns()) {
//                log.info("Column key : " + column.getName() + " | Column value : " + column.getValue());
//            }
        }

        log.info("Number of row entries in " + cfNames + " : " + result.get().getCount() );
        }*/
    }

    public void setAnalyzerSeqeunceName(String analyzerSequence) {
        this.analyzerSequenceName = analyzerSequence;
    }

    public String getAnalyzerSequenceName() {
        return this.analyzerSequenceName;
    }

    public void setPositionInSequence(int positionInSequence) {
        this.positionInSequence = positionInSequence;
    }

    public int getPositionInSequence() {
        return this.positionInSequence;
    }

    public AnalyzerSequence getAnalyzerSequence() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAnalyzerSequence(AnalyzerSequence analyzerSequence) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
