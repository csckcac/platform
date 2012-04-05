package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.PutAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.AggregationMeasure;
import org.wso2.carbon.bam.analyzer.analyzers.configs.PutConfig;
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

/*
 * Syntax :
 *
 * <put name='' indexRow=''>
 *    (0..1)<onExist>
 *             <replace/>
 *             <aggregate>
 *                +<measure name='' aggregationType=''/>
 *             </aggregate>
 *          </onExist>
 * </put>
 *
 */
public class PutAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        OMAttribute cfName = analyzerXML.getAttribute(AnalyzerConfigConstants.name);
        OMAttribute dataSourceType = analyzerXML.
                getAttribute(AnalyzerConfigConstants.dataSourceType);

        if (cfName == null) {
            throw new AnalyzerException("Error at Put : name must be present..");
        }

        PutConfig putConfig = new PutConfig();
        putConfig.setColumnFamily(cfName.getAttributeValue());

        if (dataSourceType != null) {
            putConfig.setDataSource(dataSourceType.getAttributeValue());
        }

        OMElement onExist = analyzerXML.getFirstChildWithName(AnalyzerConfigConstants.onExist);
        if (onExist != null) {
            OMElement replace = onExist.getFirstChildWithName(AnalyzerConfigConstants.replace);
            OMElement aggregate = onExist.getFirstChildWithName(AnalyzerConfigConstants.aggregate);

            if (replace != null && aggregate != null) {
                throw new AnalyzerException("Error at Put : Only replace or aggregate should be " +
                                            "present..");
            }

            if (replace != null) {
                putConfig.setDoReplace(true);
            }

            if (aggregate != null) {
                Iterator<OMElement> measureIterator = aggregate.getChildrenWithName(
                        AnalyzerConfigConstants.measure);
                if (!measureIterator.hasNext()) {
                    throw new AnalyzerException("Error at Put : At least one aggregation measure " +
                                                "should be defined..");
                }

                putConfig.setDoAggregate(true);

                List<AggregationMeasure> aggregationMeasures = new ArrayList<AggregationMeasure>();
                while (measureIterator.hasNext()) {
                    OMElement measure = measureIterator.next();
                    OMAttribute name = measure.getAttribute(AnalyzerConfigConstants.name);
                    OMAttribute aggregationType = measure.getAttribute(
                            AnalyzerConfigConstants.aggregationType);
                    OMAttribute fieldType = measure.getAttribute(AnalyzerConfigConstants.fieldType);

                    if (isEmptryAttribute(name) || isEmptryAttribute(aggregationType)) {
                        throw new AnalyzerException("Error at Put : Both name and aggregationType" +
                                                    "attributes should be present..");
                    }

                    String nameStr = name.getAttributeValue();
                    String aggregationTypeStr = aggregationType.getAttributeValue();
                    String fieldTypeStr;
                    if (fieldType != null) {
                        fieldTypeStr = fieldType.getAttributeValue();
                    } else {
                        fieldTypeStr = "DOUBLE"; // Field type if not specified defaults to Double
                    }

                    AggregationMeasure aggregationMeasure = new AggregationMeasure(
                            nameStr, aggregationTypeStr, fieldTypeStr);
                    aggregationMeasures.add(aggregationMeasure);
                }

                putConfig.setMeasures(aggregationMeasures);
            } else {
                // If 'aggregate' is not specified defaults to 'replace'
                putConfig.setDoReplace(true);
            }
        }

        return putConfig;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new PutAnalyzer(buildConfig(analyzerXML));
    }

    private boolean isEmptryAttribute(OMAttribute attr) {
        if (attr == null || attr.getAttributeValue() == null || attr.getAttributeValue().trim().
                equals("")) {
            return true;
        }

        return false;
    }

}
