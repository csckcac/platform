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
package org.wso2.carbon.rulecep.adapters.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.OutputAdaptable;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

/**
 * Sets the result into the underlying message context
 */
public class ContextPropertyOutputAdapter implements OutputAdaptable {

    private static final Log log = LogFactory.getLog(ContextPropertyOutputAdapter.class);

    /**
     * Puts the provided result into the message context if there is a key attribute in
     * the resource description
     *
     * @param description        Output ResourceDescription
     * @param value              object to be set to the context
     * @param context            the context to be used for looking up resources
     * @param messageInterceptor a helper class to locate resources from given context
     * @return <code>true</code> if there is a key in a key attribute in
     *         the resource description. Otherwise <code>false</code>
     */
    public boolean adaptOutput(ResourceDescription description,
                               Object value,
                               Object context,
                               MessageInterceptor messageInterceptor) {

        if (description == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find the Resource description.");

            }
            return false;
        }
        // if the key attribute is present , then whatever value to the target
        String key = description.getKey();
        if (key != null) {
            messageInterceptor.enrich(key, context, value);
            return true;
        }
        return false;
    }

    public boolean canAdaptOutput(ResourceDescription description, Object ouptput) {
        String key = description.getKey();
        return key != null && !"".equals(key);
    }

    public Object adaptOutput(ResourceDescription description, Object result) {
        throw new UnsupportedOperationException("Operation adaptOutput(ResourceDescription description, " +
                "Object result) is not supported.");
    }
}
