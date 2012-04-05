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
 * Adapts the child resources defined in a resource description.
 * <p/>
 * Adapts the child resources defined in a resource description. The Implementation of this class
 * should adapts the provided results based on the child resource descriptions defined in the
 * provided parent resource description
 */
public interface NestedOutputAdaptable {

    /**
     * @param description        Parent resource description
     * @param results            a list of results from the rule engine
     * @param context            context object being passed between the components in the system that
     *                           the rule component is used
     * @param messageInterceptor intercepts the context message
     * @param adapterFactory     place holder for the output adapters
     * @return <code>true</code> if the adaptation process is completed successfully.
     */
    boolean adaptChildren(ResourceDescription description,
                          List results,
                          Object context,
                          MessageInterceptor messageInterceptor,
                          OutputAdapterFactory adapterFactory);
}
