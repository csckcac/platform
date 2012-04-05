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
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Formulate inputs (facts) to be injected to the rule engine based on a set of input resource descriptions
 */
@SuppressWarnings("unused")
public class InputManager {

    private static final Log log = LogFactory.getLog(InputManager.class);

    /**
     * Strategy for encapsulating an algorithm of the input adaptation
     */
    private final InputAdaptationStrategy inputAdaptationStrategy;
    /**
     * Interceptor to access message being passed between components in the system that the rule
     * component is used
     */
    private MessageInterceptor messageInterceptor;

    /**
     * The list of input descriptions to be used by this manager when formulating inputs
     */
    private List<ResourceDescription> inputs;

    /**
     * The criteria for filtering input descriptions
     */
    private InputsFilter inputsFilter;

    /**
     * Holder of the all registered input adapters of the rule server
     */
    private InputAdapterFactory adapterFactory;

    /**
     * Creates a new instance of the InputManager
     *
     * @param adapterFactory     the holder of the all registered input adapters
     * @param inputs             a list of input descriptions to be used by this manager when
     *                           formulating inputs
     * @param messageInterceptor interceptor to access message being passed between components in
     *                           the system that the rule component is used
     */
    public InputManager(InputAdapterFactory adapterFactory,
                        List<ResourceDescription> inputs,
                        MessageInterceptor messageInterceptor) {
        this.adapterFactory = adapterFactory;
        this.inputs = inputs;
        this.messageInterceptor = messageInterceptor;
        this.inputAdaptationStrategy =
                new DefaultInputAdaptationStrategy(adapterFactory);
    }

    /**
     * Formulates facts to be injected into the rule engine based on the provided message
     * context object and input descriptions
     *
     * @param message the message passed between components in the system that the rule
     *                component is used.
     * @return a list of objects representing inputs to the rule engine
     */
    @SuppressWarnings("unchecked")
    public List<Object> processInputs(Object message) {

        List<ResourceDescription> tobeUsed;
        if (inputsFilter != null) {

            if (log.isDebugEnabled()) {
                log.debug("Filtering  inputs using InputsFilter : " + inputsFilter);
            }

            tobeUsed = inputsFilter.filter(inputs, message, messageInterceptor);

            if (log.isDebugEnabled()) {
                log.debug("Filtered inputs :  " + tobeUsed);
            }

        } else {
            tobeUsed = inputs;
        }

        final List<Object> results = new ArrayList<Object>();

        for (ResourceDescription description : tobeUsed) {
            if (description != null) {

                Object value = inputAdaptationStrategy.adapt(description, message, messageInterceptor);
                if (value == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Value result formulated based on the fact : " + description +
                                " is null.");
                    }
                    continue;
                }

                if (value instanceof Collection) {
                    results.addAll((Collection<? extends Object>) value);
                } else {
                    results.add(value);
                }
            }
        }
        return results;
    }

    public void setInputsFilter(InputsFilter inputsFilter) {
        this.inputsFilter = inputsFilter;
    }


    public InputAdapterFactory getFactAdapterFactory() {
        return adapterFactory;
    }
}
