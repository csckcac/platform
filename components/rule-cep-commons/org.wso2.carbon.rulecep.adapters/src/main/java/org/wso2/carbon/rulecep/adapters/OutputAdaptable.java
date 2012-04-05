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
 * Adapts a result based on the output description. Before calling for adapt ,
 * it is recommended to explicitly checks whether the result can be adapted or not by this adapter
 */

public interface OutputAdaptable {

    /**
     * Adapts the result according to the corresponding output description. The result of the adaptation
     * is put into the given context based on the information in the output description.
     *
     * @param description        output resource description
     * @param output             a result from the rule engine
     * @param context            the context object being passed between components in the system
     *                           that the rule component is used
     * @param messageInterceptor a helper class to locate data from given context
     * @return <code>true</code> if the adaptation was successful
     */
    boolean adaptOutput(ResourceDescription description,
                        Object output,
                        Object context,
                        MessageInterceptor messageInterceptor);

    /**
     * Explicitly checks  whether the result can be adapted or not by this adapter
     *
     * @param description output resource description
     * @param output      a result from the rule engine
     * @return True if it is possible to successfully adapt
     */
    boolean canAdaptOutput(ResourceDescription description,
                           Object output);

    /**
     * Adapts the result according to the corresponding output description.
     *
     * @param description information about target object
     * @param output      the object to be adapted
     * @return Adapted Object
     */
    public Object adaptOutput(ResourceDescription description, Object output);
}
