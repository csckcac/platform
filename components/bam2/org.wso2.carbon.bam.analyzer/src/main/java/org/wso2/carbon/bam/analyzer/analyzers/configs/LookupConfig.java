package org.wso2.carbon.bam.analyzer.analyzers.configs;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

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
public class LookupConfig implements AnalyzerConfig {

    private final String columnFamily;
    private final String defaultCf;

    public LookupConfig(String columnFamily, String defaultCf) {
        this.columnFamily = columnFamily;
        this.defaultCf = defaultCf;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public String[] getDefaultCfKeys() {
        if (defaultCf != null) {
            return defaultCf.split(",");
        }

        return null;
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
