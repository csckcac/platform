package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AggregateAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregationMeasure;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregateConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.ArrayList;
import java.util.Iterator;
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
public class AggregateAnalyzerBuilder extends AnalyzerBuilder {
    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        Iterator measureIterator = analyzerXML.getChildrenWithLocalName(AnalyzerConfigConstants.MEASURE);
        List<AggregationMeasure> measures = new ArrayList<AggregationMeasure>();

        while (measureIterator.hasNext()) {
            Object measureElementObject = measureIterator.next();
            if (!(measureElementObject instanceof OMElement)) {
                return null;
            }

            OMElement measureEl = (OMElement) measureElementObject;
            OMAttribute measureName = measureEl.getAttribute(AnalyzerConfigConstants.name);
            OMAttribute aggregationType = measureEl.getAttribute(
                    AnalyzerConfigConstants.aggregationType);
            OMAttribute fieldType = measureEl.getAttribute(AnalyzerConfigConstants.fieldType);

            if (measureName == null || aggregationType == null) {
                throw new AnalyzerException("measureName and aggregationType are required..");
            }

            String fieldTypeStr;
            if (fieldType != null) {
                fieldTypeStr = fieldType.getAttributeValue();
            } else {
                fieldTypeStr = "DOUBLE"; // Field type if not specified defaults to Double
            }

            AggregationMeasure measure = new AggregationMeasure(measureName.getAttributeValue(),
                                                                aggregationType.getAttributeValue(),
                                                                fieldTypeStr);
            measures.add(measure);

        }

        AggregateConfig aggregateConfig = new AggregateConfig();
        aggregateConfig.setMeasures(measures);
        return aggregateConfig;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new AggregateAnalyzer(buildConfig(analyzerXML));
    }
}
