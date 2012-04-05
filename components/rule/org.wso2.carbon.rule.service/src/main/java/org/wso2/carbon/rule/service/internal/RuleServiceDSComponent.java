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
package org.wso2.carbon.rule.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.rule.server.RuleServerManagerService;
import org.wso2.carbon.rule.service.RuleServiceManger;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.service.RuleCEPDeployerService;
import org.wso2.carbon.utils.Utils;

/**
 * @scr.component name="ruleservices.component" immediate="true"
 * @scr.reference name="ruleservermanager.component"
 * interface="org.wso2.carbon.rule.server.RuleServerManagerService" cardinality="1..1"
 * policy="dynamic" bind="setRuleServerManagerService" unbind="unSetRuleServerManagerService"
 * @scr.reference name="rulecep.service.component"
 * interface="org.wso2.carbon.rulecep.service.RuleCEPDeployerService" cardinality="1..1"
 * policy="dynamic" bind="setRuleCEPDeployerService" unbind="unSetRuleCEPDeployerService"
 * @scr.reference name="inputoutputadapters.component"
 * interface="org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService" cardinality="1..1"
 * policy="dynamic" bind="setInputOutputAdaptersService" unbind="unsetInputOutputAdaptersService"
 */
public class RuleServiceDSComponent {
    private static Log log = LogFactory.getLog(RuleServiceDSComponent.class);

    private RuleServerManagerService ruleServerManagerService;
    private RuleServiceManger ruleServiceManger = RuleServiceManger.getInstance();
    private RuleCEPDeployerService registryService;
    private InputOutputAdaptersService adaptersService;

    protected void activate(ComponentContext componentContext) {
        log.debug("Rule Services bundle is activated ");
        try {
            ruleServiceManger.setRuleServerManagerService(ruleServerManagerService);
            ruleServiceManger.setRuleCEPDeployerService(registryService);
            ruleServiceManger.setInputOutputAdaptersService(adaptersService);
            Utils.registerDeployerServices(componentContext.getBundleContext());
        } catch (Exception e) {
            String msg = "Failed to register RuleServiceDeployer as an OSGi service.";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        log.debug("Rule Services bundle is deactivated ");
    }

    protected void setRuleServerManagerService(RuleServerManagerService ruleServerManagerService) {
        this.ruleServerManagerService = ruleServerManagerService;
    }

    protected void unSetRuleServerManagerService(RuleServerManagerService ruleServerManagerService) {
        this.ruleServerManagerService = null;
    }

    protected void setRuleCEPDeployerService(RuleCEPDeployerService registryService) {
        this.registryService = registryService;
    }

    protected void unSetRuleCEPDeployerService(RuleCEPDeployerService registryService) {
        this.registryService = null;
    }

    protected void setInputOutputAdaptersService(InputOutputAdaptersService adaptersService) {
        this.adaptersService = adaptersService;
    }

    protected void unsetInputOutputAdaptersService(InputOutputAdaptersService adaptersService) {
        this.adaptersService = null;
    }
}
