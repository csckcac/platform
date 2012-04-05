package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.LookupAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.LookupConfig;
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
public class LookupAnalyzerBuilder extends AnalyzerBuilder {
    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        OMAttribute cfName = analyzerXML.getAttribute(AnalyzerConfigConstants.name);
        OMAttribute defaultCf = analyzerXML.getAttribute(AnalyzerConfigConstants.defaultCf);

        validateAttributes(cfName, defaultCf);

        LookupConfig lookupConfig = null;
        if (cfName != null) {
            lookupConfig = new LookupConfig(cfName.getAttributeValue(), null);
        } else if (defaultCf != null) {
            lookupConfig = new LookupConfig(null, defaultCf.getAttributeValue());
        }

        return lookupConfig;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new LookupAnalyzer(buildConfig(analyzerXML));
    }

    private void validateAttributes(OMAttribute cfName, OMAttribute defaultCf)
            throws AnalyzerException {

        boolean cfNameEmpty = isEmptryAttribute(cfName);
        boolean defaultCfEmpty = isEmptryAttribute(defaultCf);

        if (!cfNameEmpty && !defaultCfEmpty) {
            throw new AnalyzerException("Error at lookup : Either name or default attribute should" +
                                        " be present..");
        } else if (cfNameEmpty && defaultCfEmpty) {
            throw new AnalyzerException("Error at lookup : Either name or default attribute should" +
                                        " be present..");
        }

        if (!defaultCfEmpty) {
            String defaultCfString = defaultCf.getAttributeValue();

            String[] tokens = defaultCfString.split(",");
            for (String token : tokens) {
                if (!(token.equalsIgnoreCase(AnalyzerConfigConstants.EVENT) ||
                      token.equalsIgnoreCase(AnalyzerConfigConstants.META) ||
                    token.equalsIgnoreCase(AnalyzerConfigConstants.CORRELATION))) {
                    throw new AnalyzerException("Error at lookup : Default keys should only" +
                                                " contain Event, Meta or Correlation..");
                }
            }

        }

    }

    private boolean isEmptryAttribute(OMAttribute attr) {
        if (attr == null || attr.getAttributeValue() == null || attr.getAttributeValue().trim().
                equals("")) {
            return true;
        }

        return false;
    }
}
