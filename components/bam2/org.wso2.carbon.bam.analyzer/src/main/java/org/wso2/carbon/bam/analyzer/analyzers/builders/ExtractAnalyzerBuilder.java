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
import org.wso2.carbon.bam.analyzer.analyzers.ExtractAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractField;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Syntax :
 * <p/>
 * <extract>
 * +<field from='' name='' xpath='' >
 * +<namespace prefix='' uri=''/>
 * </field>
 * </extract>
 */
public class ExtractAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        Iterator<OMElement> fieldIterator = (Iterator<OMElement>) analyzerXML.getChildrenWithName(
                AnalyzerConfigConstants.field);

        List<ExtractField> extractFields = new ArrayList<ExtractField>();
        while (fieldIterator.hasNext()) {
            OMElement field = fieldIterator.next();

            OMAttribute from = field.getAttribute(AnalyzerConfigConstants.from);
            OMAttribute name = field.getAttribute(AnalyzerConfigConstants.name);
            OMAttribute xpath = field.getAttribute(AnalyzerConfigConstants.xpath);

            validateAttribute(from, AnalyzerConfigConstants.from.getLocalPart());
            validateAttribute(name, AnalyzerConfigConstants.name.getLocalPart());
            validateAttribute(xpath, AnalyzerConfigConstants.xpath.getLocalPart());

            Iterator<OMElement> nsIterator = (Iterator<OMElement>) field.getChildrenWithName(
                    AnalyzerConfigConstants.namespace);

            Map<String, String> namespaces = new HashMap<String, String>();
            while (nsIterator.hasNext()) {
                OMElement namespace = nsIterator.next();

                OMAttribute prefix = namespace.getAttribute(AnalyzerConfigConstants.prefix);
                OMAttribute uri = namespace.getAttribute(AnalyzerConfigConstants.uri);

                validateAttribute(prefix, AnalyzerConfigConstants.prefix.getLocalPart());
                validateAttribute(uri, AnalyzerConfigConstants.uri.getLocalPart());

                namespaces.put(prefix.getAttributeValue(), uri.getAttributeValue());
            }

            String fromStr = from.getAttributeValue();
            String nameStr = name.getAttributeValue();
            String xpathStr = xpath.getAttributeValue();

            ExtractField extractField = new ExtractField();
            extractField.setFrom(fromStr);
            extractField.setName(nameStr);
            extractField.setXpath(xpathStr);
            extractField.setNamespaces(namespaces);

            extractFields.add(extractField);

        }

        ExtractConfig config = new ExtractConfig();
        config.setFields(extractFields);

        return config;

    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new ExtractAnalyzer(buildConfig(analyzerXML));
    }

    private void validateAttribute(OMAttribute attr, String attrName) throws AnalyzerException {
        if (attr == null || attr.getAttributeValue() == null || attr.getAttributeValue().trim().
                equals("")) {
            throw new AnalyzerException("Error at extract : " + attrName +
                                        " attribute must be present..");
        }
    }
}
