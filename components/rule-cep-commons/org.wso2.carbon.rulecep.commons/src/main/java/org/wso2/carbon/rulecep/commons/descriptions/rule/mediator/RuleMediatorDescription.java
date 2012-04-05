/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.commons.descriptions.rule.mediator;


import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Required information to use the rule engine by the <code>RuleMediator</code>
 */
public class RuleMediatorDescription {

    /**
     * Information  about Facts
     */
    private final List<ResourceDescription> inputs = new ArrayList<ResourceDescription>();

    /**
     * Information  about Results
     */
    private final List<ResourceDescription> outputs = new ArrayList<ResourceDescription>();

    /**
     * Information  about the rule set
     */
    private RuleSetDescription ruleSetDescription;

    /**
     * Information  about the session
     */
    private SessionDescription sessionDescription;

    public void addFactDescription(ResourceDescription resourceDescription) {
        inputs.add(resourceDescription);
    }

    public void addResultDescription(ResourceDescription resourceDescription) {
        outputs.add(resourceDescription);
    }

    public List<ResourceDescription> getFacts() {
        return inputs;
    }

    public List<ResourceDescription> getResults() {
        return outputs;
    }

    public RuleSetDescription getRuleSetDescription() {
        return ruleSetDescription;
    }

    public void setRuleSetDescription(RuleSetDescription ruleSetDescription) {
        this.ruleSetDescription = ruleSetDescription;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }
}
