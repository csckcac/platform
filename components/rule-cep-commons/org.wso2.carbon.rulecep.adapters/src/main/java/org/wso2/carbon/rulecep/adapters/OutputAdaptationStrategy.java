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
package org.wso2.carbon.rulecep.adapters;


import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.List;

/**
 * Encapsulates an Output Adaptation algorithm or logic
 */
public interface OutputAdaptationStrategy {

    /**
     * Adapts the results based on the given output resource description
     *
     * @param description        output  resource description
     * @param results            results from the rule engine execution
     * @param context            the context object being passed between components in the system that the rule
     *                           component is used
     * @param messageInterceptor a helper class to locate resources from given context
     */
    void adaptOutput(ResourceDescription description,
                     List results,
                     Object context,
                     MessageInterceptor messageInterceptor);

    Object adaptOutput(ResourceDescription description,
                       Object result);

}
