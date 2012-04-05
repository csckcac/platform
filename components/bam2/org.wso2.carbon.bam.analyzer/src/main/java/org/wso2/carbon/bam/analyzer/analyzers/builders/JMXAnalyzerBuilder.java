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
import org.wso2.carbon.bam.analyzer.analyzers.JMXAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Attribute;
import org.wso2.carbon.bam.analyzer.analyzers.configs.JMXConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Operation;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Parameter;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.Iterator;

/*
<jmx url="">
	<attributes>
		<attribute name='' mbean=''/>
	</attributes>
	<operations>
		<operation name='' mbean=''>
			<parameter type='' value=''/>
		</operation>
	</operations>
</jmx>
 */
public class JMXAnalyzerBuilder extends AnalyzerBuilder {
    
    private static final String DEFAULT_JMX_URL = "service:jmx:rmi:///jndi/rmi://:9999/jmxrmi";
    
    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerEl) throws AnalyzerException {

        OMAttribute url = analyzerEl.getAttribute(AnalyzerConfigConstants.url);

        String jmxUrl;
        if (url != null) {
            jmxUrl = url.getAttributeValue();
        } else {
            jmxUrl = DEFAULT_JMX_URL;
        }

        JMXConfig config = new JMXConfig(jmxUrl);

        OMElement attributes = analyzerEl.getFirstChildWithName(AnalyzerConfigConstants.attributes);
        if (attributes != null) {
            Iterator<OMElement> attributeIterator = attributes.getChildrenWithLocalName(
                    AnalyzerConfigConstants.ATTRIBUTE);

            while (attributeIterator.hasNext()) {
                OMElement attributeEl = attributeIterator.next();

                OMAttribute mbeanAttr = attributeEl.getAttribute(AnalyzerConfigConstants.mbean);

                if (isAttributeEmpty(mbeanAttr)) {
                    throw new AnalyzerException("Attribute mbean should not be empty..");
                }

                OMAttribute nameAttr = attributeEl.getAttribute(AnalyzerConfigConstants.name);

                if (isAttributeEmpty(nameAttr)) {
                    throw new AnalyzerException("Attribute name should not be empty..");
                }

                Attribute attribute = new Attribute(mbeanAttr.getAttributeValue(),
                                                    nameAttr.getAttributeValue());
                config.addAttribute(attribute);
            }

        }

        OMElement operations = analyzerEl.getFirstChildWithName(AnalyzerConfigConstants.operations);
        if (operations != null) {
            Iterator<OMElement> operationIterator = operations.getChildrenWithLocalName(
                    AnalyzerConfigConstants.OPERATION);

            while (operationIterator.hasNext()) {
                OMElement operationEl = operationIterator.next();

                OMAttribute mbeanAttr = operationEl.getAttribute(AnalyzerConfigConstants.mbean);

                if (isAttributeEmpty(mbeanAttr)) {
                    throw new AnalyzerException("Operation mbean should not be empty..");
                }

                OMAttribute nameAttr = operationEl.getAttribute(AnalyzerConfigConstants.name);

                if (isAttributeEmpty(nameAttr)) {
                    throw new AnalyzerException("Operation name should not be empty..");
                }

                Operation operation = new Operation(mbeanAttr.getAttributeValue(),
                                                    nameAttr.getAttributeValue());

                Iterator<OMElement> parameterIterator = operationEl.getChildrenWithLocalName(
                        AnalyzerConfigConstants.PARAMETER);
                while (parameterIterator.hasNext()) {
                    OMElement parameterEl = parameterIterator.next();
                    
                    OMAttribute typeAttr = parameterEl.getAttribute(AnalyzerConfigConstants.type);
                    
                    if (isAttributeEmpty(typeAttr)) {
                        throw new AnalyzerException("Parameter type should not be empty..");
                    }
                    
                    OMAttribute valueAttr = parameterEl.getAttribute(AnalyzerConfigConstants.value);
                    
                    Parameter parameter = new Parameter(valueAttr.getAttributeValue(),
                                                        typeAttr.getAttributeValue());
                    
                    operation.addParameter(parameter);
                }

                config.addOperation(operation);

            }
        }

        return config;

    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new JMXAnalyzer(buildConfig(analyzerXML));
    }
    
    private boolean isAttributeEmpty(OMAttribute attribute) {
        if (attribute == null || attribute.getAttributeValue() == null || 
                attribute.getAttributeValue().trim().equals("")) {
            return true;
        }
        
        return false;
    }
    
}
