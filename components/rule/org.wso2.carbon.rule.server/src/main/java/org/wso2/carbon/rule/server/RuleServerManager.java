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

/**
 * The default implementation of the {@link RuleServerManagerService }
 */
public class RuleServerManager implements RuleServerManagerService {

    private static final Log log = LogFactory.getLog(RuleServerManager.class);
    /**
     * The value is used for determining the state of this component
     */
    private boolean initialized = false;

    /**
     * Contains the configuration information of the rule server
     */
    private RuleServerConfiguration ruleServerConfiguration;

    /**
     * Creates and initializes RuleServerManager based on the given RuleServerConfiguration
     *
     * @param ruleServerConfiguration Contains the configuration information of the rule server
     */
    public void init(RuleServerConfiguration ruleServerConfiguration) {
        assertRuleServerConfigurationNull(ruleServerConfiguration);

        this.ruleServerConfiguration = ruleServerConfiguration;
        this.initialized = true;
    }

    /**
     * Creates and initializes a new rule engine instance and returns it
     *
     * @param ruleEngineClassLoader The class loader to be used by the RuleEngine instance
     * @return A properly initiated <code>RuleEngine </code> instance
     */
    public RuleEngine createRuleEngine(ClassLoader ruleEngineClassLoader) {
        assertInitialized();

        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.init(ruleServerConfiguration, ruleEngineClassLoader);
        return ruleEngine;
    }

    /**
     * Checks the state of the rule engine.
     * It is recommended to check state of the this component prior to access any methods of this
     *
     * @return <code>true<code> if this component has been initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new LoggedRuntimeException("RuleServerManager has not been initialized, " +
                    "it requires to be initialized, with the required " +
                    "configurations before starting", log);
        }
    }

    private void assertRuleServerConfigurationNull(RuleServerConfiguration configuration) {
        if (configuration == null) {
            throw new LoggedRuntimeException("Given RuleServerConfiguration is null", log);
        }
    }
}
