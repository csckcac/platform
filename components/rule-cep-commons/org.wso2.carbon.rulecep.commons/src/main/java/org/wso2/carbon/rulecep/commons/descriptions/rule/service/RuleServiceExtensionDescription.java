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
package org.wso2.carbon.rulecep.commons.descriptions.rule.service;

import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceExtensionDescription;

/**
 * Rule specific extension for ServiceDescription. This contains information about rule set and  session 
 */
public class RuleServiceExtensionDescription extends ServiceExtensionDescription {

    /**
     * Information  about the rule set
     */
    private RuleSetDescription ruleSetDescription;

    /**
     * Information  about the session
     */
    private SessionDescription sessionDescription;


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
