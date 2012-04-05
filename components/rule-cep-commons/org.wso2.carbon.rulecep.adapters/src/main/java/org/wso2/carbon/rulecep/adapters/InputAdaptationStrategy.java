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


/**
 * Encapsulates an input adaptation algorithm or logic.
 * Formulates the input based on provided input descriptions , adapters and context object
 */
public interface InputAdaptationStrategy {

    /**
     * Formulates an input based on the given input resource description
     * Message context object is used to locate data to be used to construct the input
     *
     * @param description        input  resource description
     * @param context            the context object being passed between components in the system
     *                           that the rule component is used
     * @param messageInterceptor a helper class to locate data from given context
     * @return the formulated input
     */
    Object adapt(ResourceDescription description,
                 Object context,
                 MessageInterceptor messageInterceptor);
}
