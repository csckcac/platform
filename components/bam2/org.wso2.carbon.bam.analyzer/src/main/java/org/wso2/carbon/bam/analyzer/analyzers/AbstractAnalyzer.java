package org.wso2.carbon.bam.analyzer.analyzers;

import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;
import org.wso2.carbon.bam.analyzer.engine.DataContext;

import java.util.Map;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public abstract class AbstractAnalyzer implements Analyzer {

    private String analyzerSequenceName;
    private int positionInSequence;
    private AnalyzerSequence analyzerSequence;

    private AnalyzerConfig analyzerConfig;

    public AbstractAnalyzer() {

    }

    public AnalyzerSequence getAnalyzerSequence() {
        return analyzerSequence;
    }

    public void setAnalyzerSequence(AnalyzerSequence analyzerSequence) {
        this.analyzerSequence = analyzerSequence;
    }

    public int getExecutingTenantId() {
        return analyzerSequence.getTenantId();
    }

    public AbstractAnalyzer(AnalyzerConfig analyzerConfig) {
        this.analyzerConfig = analyzerConfig;
    }

    public AnalyzerConfig getAnalyzerConfig() {
        return analyzerConfig;
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

    // TODO: Check validity of data when getting and setting and throw an exception if not valid  

    public Object getData(DataContext dataContext) {
        Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
        Object result = properties.get(AnalyzerConfigConstants.RESULT);

        return result;
    }

    public void setData(DataContext dataContext, Object data) {
        Map properties = dataContext.getSequenceProperties(getAnalyzerSequenceName());
        properties.put(AnalyzerConfigConstants.RESULT, data);
    }

}
