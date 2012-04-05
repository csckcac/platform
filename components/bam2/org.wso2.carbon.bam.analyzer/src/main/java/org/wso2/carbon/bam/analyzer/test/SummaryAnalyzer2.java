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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;
import org.wso2.carbon.bam.analyzer.engine.DataContext;

import java.util.ArrayList;
import java.util.List;

public class SummaryAnalyzer2 implements Analyzer {

    private static Log log = LogFactory.getLog(SummaryAnalyzer2.class);

    private String analyzerSequenceName;

    private int positionInSequence;

    @Override
    public void analyze(DataContext dataContext) {
        //To change body of implemented methods use File | Settings | File Templates.
/*        log.info("Summary Analyzer 2 executed");

        List<QueryIndex> indexes = new ArrayList<QueryIndex>();

        QueryIndex timeStamp = new QueryIndex("timeStamp", "Tue Aug 02 14:00:00 IOT 2011",
                                              "Tue Aug 02 14:00:00 IOT 2011");
        QueryIndex wf = new QueryIndex("workFlowId", "ORDER", "ORDER");
        QueryIndex node = new QueryIndex("nodeId", "AS", "AS");

        indexes.add(timeStamp);
        indexes.add(wf);
        indexes.add(node);

        List<ResultRow> events = QueryUtils.querySecondaryColumnFamily(
                "WorkFlowId_NodeId", indexes);
        for (ResultRow event : events) {
            System.out.println("Event key : " + event.getRowKey() + "\n");

            for (ResultColumn column : event.getColumns()) {
                System.out.println("\tKey : " + column.getKey() + " Value : " + column.getValue() + "\n");
            }
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
