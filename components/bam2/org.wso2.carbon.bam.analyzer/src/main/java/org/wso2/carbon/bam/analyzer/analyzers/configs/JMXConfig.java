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
package org.wso2.carbon.bam.analyzer.analyzers.configs;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.ArrayList;
import java.util.List;

public class JMXConfig implements AnalyzerConfig {

    private final String url;
    
    private List<Attribute> attributes = new ArrayList<Attribute>();
    
    private List<Operation> operations = new ArrayList<Operation>();
    
    public JMXConfig(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }
    
    public void addOperation(Operation operation) {
        operations.add(operation);
    }
    
    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    public List<Operation> getOperations() {
        return operations;
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
