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
 *//**
 *
 */
package org.wso2.carbon.rulecep.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.List;

/**
 * Process outputs from the rule engine based on output resources descriptions
 * OutputManager instance should be created per a list of related output descriptions
 */
public class OutputManager {

    private static final Log log = LogFactory.getLog(OutputManager.class);

    /**
     * Strategy to encapsulates the algorithmic logic of the output adaptation
     */
    private final OutputAdaptationStrategy strategy;

    /**
     * Implements a criteria for filtering outputs
     */
    private OutputsFilter outputsFilter;
    /**
     * a list of related output descriptions
     */
    private List<ResourceDescription> outputs;
    /**
     * Interceptor to access message being passed between components in the system that the rule
     * component is used
     */
    private MessageInterceptor messageInterceptor;

    /**
     * Factory to access and create all output adapters registered in the rule server
     */
    private OutputAdapterFactory outputAdapterFactory;

    /**
     * Create an output manager instance
     *
     * @param outputAdapterFactory Strategy encapsulating the algorithmic logic of the output adaptation
     * @param outputs              A list of descriptions about outputs this manager should adapt
     * @param messageInterceptor   Interceptor to access message being passed between components in
     *                             the system that the rule component is used
     */
    public OutputManager(OutputAdapterFactory outputAdapterFactory,
                         List<ResourceDescription> outputs,
                         MessageInterceptor messageInterceptor) {
        this.outputAdapterFactory = outputAdapterFactory;
        this.outputs = outputs;
        this.messageInterceptor = messageInterceptor;
        this.strategy = new DefaultOutputAdaptationStrategy(outputAdapterFactory);
    }

    /**
     * Process the given a list of results with the already given descriptions about results.
     *
     * @param results a list of results from the rule engine execution
     * @param source  the message being passed between components in the system that the rule
     *                component is used.
     */
    public void processOutputs(List results, Object source) {

        if (results.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no results to be processed");
            }
            return;
        }

        List<ResourceDescription> tobeUsed;
        if (outputsFilter != null) {

            if (log.isDebugEnabled()) {
                log.debug("Filtering outputs using OutputsFilter : " + outputsFilter);
            }

            tobeUsed = outputsFilter.filter(outputs, results, source, messageInterceptor);

            if (log.isDebugEnabled()) {
                log.debug("Filtered outputs :  " + tobeUsed);
            }

        } else {
            tobeUsed = outputs;
        }

        for (ResourceDescription description : tobeUsed) {
            if (description != null) {
                strategy.adaptOutput(description, results, source, messageInterceptor);
            }
        }
    }

    public Object processOutput(Object result) {
        for (ResourceDescription description : outputs) {
            if (description != null) {
                Object o = strategy.adaptOutput(description, result);
                if (o != null) {
                    return o;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void setOutputsFilter(OutputsFilter outputsFilter) {
        this.outputsFilter = outputsFilter;
    }

    @SuppressWarnings("unused")
    public OutputAdapterFactory getResultAdapterFactory() {
        return outputAdapterFactory;
    }
}
