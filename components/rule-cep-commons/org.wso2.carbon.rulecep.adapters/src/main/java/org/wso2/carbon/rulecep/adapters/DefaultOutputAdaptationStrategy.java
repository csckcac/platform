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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.impl.POJOResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.List;

/**
 * Default <code>OutputAdaptationStrategy</code> algorithm
 * For all results, checks explicitly whether a result can be adapt and
 * only if it is possible to adapt a result, call adapt method of the adapter with the result
 */
public class DefaultOutputAdaptationStrategy implements OutputAdaptationStrategy {

    private static final Log log = LogFactory.getLog(DefaultOutputAdaptationStrategy.class);

    private final OutputAdapterFactory factory;

    public DefaultOutputAdaptationStrategy(OutputAdapterFactory factory) {
        this.factory = factory;
    }

    /**
     * Default <code>OutputAdaptationStrategy</code> algorithm
     * <ul>
     * <li> step1 - Gets the adapter for the type. If there is no adapter, uses the POJO adapter
     * and type as the class.
     * <li> step2 - if the OutputAdapter implements <code>NestedOutputAdaptable</code>, then adapt children
     * <li> step3 - For all results checks if the output adapter can adapt them. If so , do the adaptation
     * <ul>
     *
     * @param description        output  resource description
     * @param results            results from the rule engine execution
     * @param context            the context object being passed between components in the system
     *                           that the rule component is used
     * @param messageInterceptor a helper class to locate data from given context
     */
    public void adaptOutput(ResourceDescription description,
                            List results,
                            Object context,
                            MessageInterceptor messageInterceptor) {

        OutputAdaptable outputAdaptable = getOutputAdaptable(description);

        if (outputAdaptable instanceof NestedOutputAdaptable) {

            if (log.isDebugEnabled()) {
                log.debug("Adapting results with the children resource descriptions");
            }

            NestedOutputAdaptable nestedOutputAdaptable = (NestedOutputAdaptable) outputAdaptable;
            nestedOutputAdaptable.adaptChildren(description,
                    results, context, messageInterceptor, factory);
        }

        for (Object result : results) {
            if (result == null) {
                if (log.isDebugEnabled()) {
                    log.debug("A null result was found");
                }
                continue;
            }
            if (outputAdaptable.canAdaptOutput(description, result)) {
                outputAdaptable.adaptOutput(
                        description, result, context, messageInterceptor);
            }
        }
    }

    public Object adaptOutput(ResourceDescription description, Object result) {
        OutputAdaptable outputAdaptable = getOutputAdaptable(description);
        if (outputAdaptable.canAdaptOutput(description, result)) {
            return outputAdaptable.adaptOutput(description, result);
        }
        return null;
    }

    private OutputAdaptable getOutputAdaptable(ResourceDescription description) {
        OutputAdaptable outputAdaptable = factory.getOutputAdapter(description.getType());
        if (outputAdaptable == null) {

            if (log.isDebugEnabled()) {
                log.info("There is no registered output adapter for the given type : " +
                        description.getType() + " . using the POJO Adapter");
            }

            outputAdaptable = new POJOResourceAdapter(); // TODO do in a proper way
        }
        return outputAdaptable;
    }
}
