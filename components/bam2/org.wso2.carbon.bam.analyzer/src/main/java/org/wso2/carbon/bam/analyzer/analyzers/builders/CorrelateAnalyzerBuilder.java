package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.CorrelateAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.CorrelateConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
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
public class CorrelateAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        CorrelateConfig correlateConfig = null;

        OMAttribute orderBy = analyzerXML.getAttribute(AnalyzerConfigConstants.orderBy);
        OMAttribute nodeIdentifier = analyzerXML.getAttribute(AnalyzerConfigConstants.nodeIdentifier);
        OMAttribute lookup = analyzerXML.getAttribute(AnalyzerConfigConstants.lookup);

        if (lookup == null) {
            throw new AnalyzerException(" nodeIdentifier and lookup must be present..");
        }

        if (orderBy != null) {
            correlateConfig = new CorrelateConfig(lookup.getAttributeValue(),
                    orderBy.getAttributeValue(), nodeIdentifier.getAttributeValue());
        } else {
            correlateConfig = new CorrelateConfig(lookup.getAttributeValue(), null,
                                                  nodeIdentifier.getAttributeValue());
        }

        return correlateConfig;
    }


    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new CorrelateAnalyzer(buildConfig(analyzerXML));
    }
}
