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

import org.jaxen.XPath;
import org.wso2.carbon.rulecep.commons.ReturnValue;

/**
 * Provides an abstraction for intercepting the context object being passed between the components of
 * the system the rule component is used
 * <p/>
 * Provides required functionality to extract data from the context and enrich the
 * context with some data.
 * <p/>
 * This provide a data access API to be used in the software elements of the rule component.
 * This enables to decouple the context message from the implementation of the rule component,
 * which in turns enable any system to use rule component 'as-is'
 */
public interface MessageInterceptor {

    /**
     * Looking up for a resource with the given key. The look up processes is coupled to
     * the implementation
     *
     * @param key          an identifier to locate the resource
     * @param message      the context object being passed between the components of
     *                     the system the rule component is used
     * @param defaultValue default value in the case of there is no resource for the given key
     * @return resorce corresponded to the given key if there is a resource for the give key.
     *         Otherwise, the default value should be returned
     */
    public ReturnValue extract(String key, Object message, Object defaultValue);

    /**
     * Looking up for a resource with the given XPath expression. The look up processes is coupled
     * to the implementation
     *
     * @param xPath        an expression to locate the resource
     * @param message      the context object being passed between the components of
     *                     the system the rule component is used
     * @param defaultValue default value in the case of there is no resource for the given expression
     * @return resorce corresponded to the given key if there is a resource for the give expression.
     *         Otherwise, the default value should be returned
     */
    public ReturnValue extract(XPath xPath, Object message, Object defaultValue);

    /**
     * Looking up for the default resource. The value of the default resource is coupled to
     * the implementation
     *
     * @param message the context object being passed between the components of
     *                the system that the rule component is used
     * @return The default resource
     */
    public ReturnValue extractPayload(Object message);

    /**
     * Extracts the message envelope of the message
     *
     * @param message the context object being passed between components of
     *                the system the rule component is used
     * @return the envelope of the message
     */
    public ReturnValue extractEnvelope(Object message);

    /**
     * Enrich the context message with the given object based on the given key.
     * The enrichment process is coupled to the implementation
     *
     * @param key     an identifier for the resource
     * @param message the context object being passed between components of
     *                the system the rule component is used
     * @param value   data to be used for enriching context message
     */
    public void enrich(String key, Object message, Object value);
}