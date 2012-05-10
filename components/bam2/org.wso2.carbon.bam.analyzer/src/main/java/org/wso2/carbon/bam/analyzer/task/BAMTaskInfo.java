/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.bam.analyzer.task;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;

import java.util.Map;

public class BAMTaskInfo {

    private AnalyzerSequence sequence;

    private OMElement analyzerSeqXML;

    private Map<String, String> credentials;

    public BAMTaskInfo () {
    }

    public AnalyzerSequence getAnalyzerSequence() {
        return sequence;
    }

    public OMElement getAnalyzerSeqXML() {
        return analyzerSeqXML;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setAnalyzerSeqXML(OMElement analyzerSeqXML) {
        this.analyzerSeqXML = analyzerSeqXML;
    }

    public void setAnlyzerSequence(AnalyzerSequence sequence) {
        this.sequence = sequence;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

}
