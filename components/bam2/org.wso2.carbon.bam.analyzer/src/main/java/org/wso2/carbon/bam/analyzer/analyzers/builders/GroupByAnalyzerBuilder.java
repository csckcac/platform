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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.GroupByAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.GroupByConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupByAnalyzerBuilder extends AnalyzerBuilder {

    private static final Log log = LogFactory.getLog(GroupByAnalyzerBuilder.class);

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {

        GroupByConfig config = new GroupByConfig();

        Iterator<OMElement> fieldIterator = analyzerXML.getChildrenWithName(
                AnalyzerConfigConstants.field);
        OMElement timeField = analyzerXML.getFirstChildWithName(AnalyzerConfigConstants.time);

        if (!fieldIterator.hasNext() && timeField == null) {
            handleException("Error at groupBy : At least one field or time field should be" +
                            " specified..");
        }

        Iterator<OMElement> iterator = analyzerXML.getChildElements();
        List<String> fields = new ArrayList<String>();
        while (iterator.hasNext()) {
            OMElement field = iterator.next();

            if (field.getLocalName().equals(AnalyzerConfigConstants.field.getLocalPart())) {
                OMAttribute name = field.getAttribute(AnalyzerConfigConstants.name);
                validateAttribute(name, AnalyzerConfigConstants.name.getLocalPart());
                fields.add(name.getAttributeValue());
            } else if (field.getLocalName().equals(AnalyzerConfigConstants.time.getLocalPart())) {
                OMAttribute name = timeField.getAttribute(AnalyzerConfigConstants.name);
                validateAttribute(name, AnalyzerConfigConstants.name.getLocalPart());

                fields.add(name.getAttributeValue());
                config.setTimeField(name.getAttributeValue());

                OMAttribute granularity = timeField.getAttribute(AnalyzerConfigConstants.granularity);
                validateAttribute(granularity, AnalyzerConfigConstants.granularity.getLocalPart());

                config.setGranularity(granularity.getAttributeValue());
            } else {
                handleException("Error at groupBy : Unrecognized element in groupBy. Either " +
                                "'field' or 'time' expected..");
            }
        }

        config.setFields(fields);

        return config;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new GroupByAnalyzer(buildConfig(analyzerXML));
    }

    private void handleException(String message) throws AnalyzerException {
        log.error(message);
        throw new AnalyzerException(message);
    }

    private void validateAttribute(OMAttribute attribute, String attributeName)
            throws AnalyzerException {
        if (attribute == null || attribute.getAttributeValue() == null || attribute.
                getAttributeValue().trim().equals("")) {
            handleException("Error at groupBy : Missing or empty required attribute " +
                            attributeName);
        }
    }

    private boolean isEmptyAttribute(OMAttribute attribute) {
        if (attribute == null || attribute.getAttributeValue() == null || attribute.
                getAttributeValue().trim().equals("")) {
            return true;
        }

        return false;
    }

}
