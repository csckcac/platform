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

import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;

import java.util.Map;

/**
 * A factory for creating a <code>RuleBackendRuntime</code> instance. This should create a properly initiated
 * <code>RuleBackendRuntime</code>instance. It is recommended to use this class as the only means for creating
 * <code>RuleBackendRuntime</code> instances
 */
public interface RuleBackendRuntimeFactory {

    /**
     * Returns a properly initiated <code>RuleBackendRuntime</code> instance.
     *
     * @param properties  properties to be used when creating the underlying rules service provider
     * @param classLoader class loader to be used by the underlying rules service provider to load
     *                    facts and other required classes
     * @return a properly initiated <code>RuleBackendRuntime</code>  instance if there are on exceptions
     *         during creation process.Otherwise, <code>LoggedRuntimeException</code> should be thrown.
     */
    public RuleBackendRuntime createRuleBackendRuntime(Map<String, PropertyDescription> properties,
                                                       ClassLoader classLoader);
}
