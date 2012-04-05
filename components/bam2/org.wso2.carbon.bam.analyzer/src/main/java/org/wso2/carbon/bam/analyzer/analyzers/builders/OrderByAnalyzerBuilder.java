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
package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.OrderByAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.OrderByConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

public class OrderByAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        OMAttribute name = analyzerXML.getAttribute(AnalyzerConfigConstants.field);
        
        if (name == null || name.getAttributeValue() == null || name.getAttributeValue().trim().
                equals("")) {
            throw new AnalyzerException("Error at orderBy : Field attribute must be present..");
        }

        OrderByConfig config = new OrderByConfig();
        config.setField(name.getAttributeValue());

        return config;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new OrderByAnalyzer(buildConfig(analyzerXML));
    }

}
