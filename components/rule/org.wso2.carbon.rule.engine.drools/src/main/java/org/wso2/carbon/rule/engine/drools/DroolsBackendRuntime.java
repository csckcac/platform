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
package org.wso2.carbon.rule.engine.drools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.RuleBackendRuntime;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rulecep.adapters.utils.PropertyDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.utils.ObjectToStreamConverter;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * The <code>RuleBackendRuntime</code> implemetaion for the Drools native API.
 * Implementation is not completed as we uses JSR 94 as the default one
 */
public class DroolsBackendRuntime implements RuleBackendRuntime {

    private static Log log = LogFactory.getLog(DroolsBackendRuntime.class);
    private KnowledgeBase knowledgeBase;
    private KnowledgeBuilder knowledgeBuilder;

    public DroolsBackendRuntime(KnowledgeBase knowledgeBase, KnowledgeBuilder knowledgeBuilder) {
        this.knowledgeBase = knowledgeBase;
        this.knowledgeBuilder = knowledgeBuilder;
    }

    public String addRuleSet(RuleSetDescription description) {
        if (log.isDebugEnabled()) {
            log.debug("Creating a Rule Execution Set");
        }

        Object ruleScript = description.getRuleSource();
        Map<String, Object> properties = PropertyDescriptionEvaluator.evaluate(
                description.getCreationProperties());
        InputStream in = ObjectToStreamConverter.toInputStream(ruleScript, properties);
        if (in == null) {
            throw new LoggedRuntimeException("The input stream form rule script is null.",
                    log);
        }

        ResourceTypeDetectionStrategy strategy = new DefaultResourceTypeDetectionStrategy();
        ResourceType resourceType = strategy.getResourceType(properties);

        knowledgeBuilder.add(ResourceFactory.newInputStreamResource(in), resourceType);
        if (knowledgeBuilder.hasErrors()) {
            throw new LoggedRuntimeException("Error during creating rule set: " +
                    knowledgeBuilder.getErrors(), log);
        }

        Collection<KnowledgePackage> pkgs = knowledgeBuilder.getKnowledgePackages();
        knowledgeBase.addKnowledgePackages(pkgs);
        return description.getBindURI();
    }

    public Session createSession(SessionDescription description) {
        boolean stateful = description.isStateful();
        String name = description.getRuleSetURI();
        final Map<String, Object> properties = PropertyDescriptionEvaluator.evaluate(
                description.getSessionProperties());
        final KnowledgeSessionConfiguration configuration =
                KnowledgeBaseFactory.newKnowledgeSessionConfiguration();

        if (stateful) {
            if (log.isDebugEnabled()) {
                log.debug("Using stateful rule session ");
            }
            StatefulKnowledgeSession ruleSession = knowledgeBase.newStatefulKnowledgeSession(
                    configuration, null);

            if (ruleSession == null) {
                throw new LoggedRuntimeException("The created stateful rule session is null", log);
            }
            return new DroolsStatefulSession(ruleSession, properties);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using stateless rule session");
            }
            // create stateless rule session
            StatelessKnowledgeSession ruleSession =
                    knowledgeBase.newStatelessKnowledgeSession(configuration);

            if (ruleSession == null) {
                throw new LoggedRuntimeException("The created stateless rule session is null", log);
            }
            return new DroolsStatelessSession(ruleSession);
        }
    }

    public void removeRuleSet(RuleSetDescription description) {
        String bindUri = description.getBindURI();
        if (log.isDebugEnabled()) {
            log.debug("Removing the rule execution set" +
                    " that has been bind to Uri " + bindUri);
        }
        knowledgeBase.removeKnowledgePackage(bindUri);
    }

    public void destroy() {
        //todo
    }
}
