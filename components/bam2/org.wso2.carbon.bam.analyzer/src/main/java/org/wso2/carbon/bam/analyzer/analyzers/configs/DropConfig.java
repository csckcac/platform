package org.wso2.carbon.bam.analyzer.analyzers.configs;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.List;

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
public class DropConfig implements AnalyzerConfig {
    private String type;

    private String matchUsing;

    private List<String> groupFilters;

    private List<FilterField> fieldFilters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMatchUsing() {
        return matchUsing;
    }

    public void setMatchUsing(String matchUsing) {
        this.matchUsing = matchUsing;
    }

    public List<String> getGroupFilters() {
        return groupFilters;
    }

    public void setGroupFilters(List<String> groupFilters) {
        this.groupFilters = groupFilters;
    }

    public List<FilterField> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(List<FilterField> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    @Override
    public String serialize(Analyzer analyzer) throws AnalyzerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Analyzer deserialize(OMElement anaylzerOM) throws AnalyzerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
