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
package org.wso2.carbon.rule.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.RuleBackendRuntime;
import org.wso2.carbon.rule.core.RuleBackendRuntimeFactory;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;


/**
 * RuleEngine is to proxy the actual rule engine provider. It manages and control the access to
 * the rule engine provider.
 * <p/>
 * All the arguments to the rule engine provider and all return values from the rule engine provider
 * are validated so that the rule engine provider itself can avoid validation.
 * <p/>
 * Therefore, users must only access the rule engine provider through this class.
 */
public class RuleEngine {

    private static final Log log = LogFactory.getLog(RuleEngine.class);

    /**
     * The value is used for determining the state of this component
     */
    private boolean initialized = false;

    /**
     * The RuleBackendRuntime which provides the rule services
     */
    private RuleBackendRuntime ruleBackendRuntime;

    /**
     * Creates and initializes the underlying rule engine provider.
     * <p/>
     * Using the rule engine provider factory and configuration properties, this creates an instance
     * of the rule engine provider. ClassLoader is to load classes needed by the rule engine provider.
     *
     * @param description           The RuleServerConfiguration giving the information such as
     *                              the rule service provider, adapters.
     * @param ruleEngineClassLoader The classLoader providing all classes needed by the rule
     *                              service provider
     */
    public void init(RuleServerConfiguration description, ClassLoader ruleEngineClassLoader) {
        assertRuleServerConfigurationNull(description);
        assertRuleClassLoaderNull(ruleEngineClassLoader);

        RuleBackendRuntimeFactory factory = description.getRuleBackendRuntimeFactory();
        ruleBackendRuntime = factory.createRuleBackendRuntime(
                description.getProviderPropertiesAsMap(), ruleEngineClassLoader);
        this.initialized = true;
    }

    /**
     * Registers the rule set in the rule service provider.RuleEngine must be initialized before
     * calling this method.
     *
     * @param ruleSetDescription The RuleSetDescription providing information about the rule script
     * @return The bind uri of the given rule set if the rule set was registered successfully.
     *         For any issue during the registration {@link LoggedRuntimeException} is thrown
     */
    public String addRuleSet(RuleSetDescription ruleSetDescription) {
        assertInitialized();
        assertRuleSetNull(ruleSetDescription);

        return ruleBackendRuntime.addRuleSet(ruleSetDescription);
    }

    /**
     * Unregisters a rule set using the binding URI of the rule set.RuleEngine must be initialized before
     * calling this method.
     *
     * @param ruleSetDescription the RuleSetDescription providing information about
     *                           the rule set to be removed
     */
    public void removeRuleSet(RuleSetDescription ruleSetDescription) {
        assertInitialized();
        assertRuleSetNull(ruleSetDescription);

        ruleBackendRuntime.removeRuleSet(ruleSetDescription);
    }

    /**
     * Gets a session to access the working memory of the underlying rule engine. RuleEngine must be
     * initialized before calling this method.
     *
     * @param sessionDescription the description about the type of required session
     * @return <code>Session</code> to access the underlying rule engine's working memory
     */
    public Session createSession(SessionDescription sessionDescription) {
        assertInitialized();
        assertSessionDescriptionNull(sessionDescription);

        return ruleBackendRuntime.createSession(sessionDescription);
    }

    /**
     * Stops the rule engine and does any cleanups.RuleEngine must be initialized before
     * calling this method.
     */
    public void destroy() {
        assertInitialized();

        ruleBackendRuntime.destroy();
    }

    /**
     * Checks the state of the rule engine.
     * It is recommended to check state of the this component prior to access any methods of this
     *
     * @return <code>true<code> if the rule engine has been initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new LoggedRuntimeException("RuleEngine has not been initialized, " +
                    "it requires to be initialized, with the required " +
                    "configurations before starting", log);
        }
    }

    private void assertRuleSetNull(RuleSetDescription ruleSetDescription) {
        if (ruleSetDescription == null) {
            throw new LoggedRuntimeException("Given RuleSetDescription is null", log);
        }
    }

    private void assertSessionDescriptionNull(SessionDescription sessionDescription) {
        if (sessionDescription == null) {
            throw new LoggedRuntimeException("Given SessionDescription is null", log);
        }
    }

    private void assertRuleServerConfigurationNull(RuleServerConfiguration serverConfiguration) {
        if (serverConfiguration == null) {
            throw new LoggedRuntimeException("Given RuleServerConfiguration is null", log);
        }
    }

    private void assertRuleClassLoaderNull(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new LoggedRuntimeException("Given ClassLoader to be used by the rule engine " +
                    "is null", log);
        }
    }
}
