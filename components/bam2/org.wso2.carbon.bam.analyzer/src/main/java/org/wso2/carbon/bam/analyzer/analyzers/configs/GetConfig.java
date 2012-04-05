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
package org.wso2.carbon.bam.analyzer.analyzers.configs;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;
import org.wso2.carbon.bam.core.persistence.QueryIndex;

import java.util.List;

public class GetConfig implements AnalyzerConfig {

    private String table;

    private QueryIndex index;

    private boolean fetchDirty;

    private int batchSize;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public QueryIndex getIndex() {
        return index;
    }

    public void setIndex(QueryIndex index) {
        this.index = index;
    }

    public boolean isFetchDirty() {
        return fetchDirty;
    }

    public void setFetchDirty(boolean fetchDirty) {
        this.fetchDirty = fetchDirty;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public String serialize(Analyzer analyzer) throws AnalyzerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Analyzer deserialize(OMElement anaylzerOM) throws AnalyzerException {
        return null;
    }
}
