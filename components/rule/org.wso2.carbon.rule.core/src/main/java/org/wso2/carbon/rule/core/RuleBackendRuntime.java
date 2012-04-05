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
package org.wso2.carbon.rule.core;

import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;


/**
 * Encapsulates the rule service provider or the engine implements a rule engine. This adapts an
 * existing rule engine implementation.
 * <p/>
 * This class exposes functionality required to create a rule execution set, a session associated
 * with a rule execution set and  remove a registered execution set. Furthermore, this provide a
 * way to destroy the underlying rule engine.
 */
public interface RuleBackendRuntime {

    /**
     * Registers a rule set. The rule set should be given in the <code>RuleSetDescription</code> as the rule source.
     * Within this method , an executable rule set is created and registered with the rule engine
     *
     * @param description information about the rule set
     * @return the registered URI of the rule set
     */
    public String addRuleSet(RuleSetDescription description);

    /**
     * Create a session based on the given SessionDescription. The session can be stateful or stateless
     *
     * @param sessionDescription information about the session to be created
     * @return a valid <code>Session</code> object, either stateful or stateless
     */
    public Session createSession(SessionDescription sessionDescription);

    /**
     * Removed an already registered rule set
     *
     * @param description information about the rule set to be removed
     */
    public void removeRuleSet(RuleSetDescription description);

    /**
     * Cleanup any resources used by the rule engine
     */
    public void destroy();
}
