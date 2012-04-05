/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.throttling.manager.rules;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rule.server.RuleEngine;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.throttling.manager.exception.ThrottlingException;
import org.wso2.carbon.throttling.manager.utils.Util;

public class RuleInvoker {
    private static final Log log = LogFactory.getLog(RuleInvoker.class);
    RuleEngine ruleEngine;
    Session session;
    boolean continueExecute = true;

    public RuleInvoker() throws ThrottlingException {
        updateRules();
    }

    public synchronized void invoke(List<Object> knowledgeBase) throws ThrottlingException {
        session.execute(knowledgeBase);
    }

    public synchronized void updateRules() throws ThrottlingException {
        ruleEngine =
                Util.getRuleServerManagerService().createRuleEngine(
                        Thread.currentThread().getContextClassLoader());
        RuleSetDescription ruleSetDescription = new RuleSetDescription();
        Resource ruleContentResource;
        // getting the resource content.
        try {
            UserRegistry systemRegistry = Util.getSuperTenantGovernanceSystemRegistry();
            ruleContentResource =
                    systemRegistry.get(StratosConstants.THROTTLING_RULES_PATH);
        } catch (RegistryException e) {
            String msg =
                    "Error in reading the rule resource content. resource path: " +
                            StratosConstants.THROTTLING_RULES_PATH + ".";
            log.error(msg, e);
            throw new ThrottlingException(msg, e);
        } catch (Exception e) {
            String msg = "Error in loading the rules.";
            log.error(msg, e);
            throw new ThrottlingException(msg, e);
        }

        try {
            ruleSetDescription.setRuleSource(ruleContentResource.getContentStream());
        } catch (RegistryException e) {
            String msg = "Error in loading the rules from the content stream.";
            log.error(msg, e);
            throw new ThrottlingException(msg, e);
        }
        // ruleSetDescription.setBindURI("file:" + ruleFile);
        String uri;
        try {
            uri = ruleEngine.addRuleSet(ruleSetDescription);
        } catch (Exception e) {
            String msg = "Error in compiling the rules.";
            log.error(msg, e);
            throw new ThrottlingException(msg, e);
        }
        SessionDescription sessionDescription = new SessionDescription();
        sessionDescription.setSessionType(SessionDescription.STATELESS_SESSION);
        sessionDescription.setRuleSetURI(uri);

        session = ruleEngine.createSession(sessionDescription);
    }
}
