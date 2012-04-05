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
package org.wso2.carbon.rule.service;

import org.wso2.carbon.rule.server.RuleServerManagerService;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.service.RuleCEPDeployerService;

import java.util.Collection;

/**
 * Keeps the runtime objects required by the operation of the rule service
 */
public class RuleServiceManger {

    private static RuleServiceManger ourInstance = new RuleServiceManger();
    /**
     * The access point to allow access major elements in the rule component *
     */
    private RuleServerManagerService ruleServerManagerService;
    /**
     * The access point to WSO2 Registry *
     */
    private RuleCEPDeployerService ruleCEPDeployerService;
    /**
     * The access point to input/output adapters
     */
    private InputOutputAdaptersService inputOutputAdaptersService;

    public static RuleServiceManger getInstance() {
        return ourInstance;
    }

    private RuleServiceManger() {
    }

    public RuleServerManagerService getRuleServerManagerService() {
        return ruleServerManagerService;
    }

    public void setRuleServerManagerService(RuleServerManagerService ruleServerManagerService) {
        this.ruleServerManagerService = ruleServerManagerService;
    }

    public Collection<String> getFactAdapters() {
        return inputOutputAdaptersService.getFactAdapterFactory().getInputAdapters();
    }

    public Collection<String> getResultAdapters() {
        return inputOutputAdaptersService.getResultAdapterFactory().getOutputAdapters();
    }

    public RuleCEPDeployerService getRuleCEPDeployerService() {
        return ruleCEPDeployerService;
    }

    public void setRuleCEPDeployerService(RuleCEPDeployerService ruleCEPDeployerService) {
        this.ruleCEPDeployerService = ruleCEPDeployerService;
    }

    public void setInputOutputAdaptersService(InputOutputAdaptersService inputOutputAdaptersService) {
        this.inputOutputAdaptersService = inputOutputAdaptersService;
    }

    public InputOutputAdaptersService getInputOutputAdaptersService() {
        return inputOutputAdaptersService;
    }
}
