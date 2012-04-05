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

/**
 * RuleServerManagerService is to provide the functionality to create main components belong to the
 * rule server, and information belong to it. There are three main components
 * <p/>
 * <ul>
 * <li> Rule Engine
 * <li> Input Manager
 * <li> OutPut Manager
 * </ul>
 * <p/>
 * Rule Engine encapsulates the underlying rule service provider. Input Manager is to adapt
 * given objects into target facts and OutPut Manager is to adapt results from the rule engine
 * into target objects
 */
public interface RuleServerManagerService {

    /**
     * Factory method to create a RuleEngine instance. This method returns a new RuleEngine instance.
     * The RuleEngine is created based on the RuleEngineProvider defined in the rule-component.conf
     *
     * @param ruleEngineClassLoader The class loader to be used by the RuleEngine instance
     * @return a valid <code>RuleEngine<code> instance
     */
    RuleEngine createRuleEngine(ClassLoader ruleEngineClassLoader);
}
