package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.LogAnalyzer;
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
public class LogAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        // no config needed
        return null;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new LogAnalyzer(buildConfig(analyzerXML));
    }
}
