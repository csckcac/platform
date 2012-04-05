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
package org.wso2.carbon.rulecep.adapters.service;


import org.wso2.carbon.rulecep.adapters.*;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.List;

/**
 * The default <code>InputOutputAdaptersService</code> implementation
 */
public class InputOutputAdaptersServiceImpl implements InputOutputAdaptersService {

    /**
     * Facts adapter factory
     */
    private final InputAdapterFactory factAdapterFactory = new InputAdapterFactory();

    /**
     * Results adapter factory
     */
    private final OutputAdapterFactory resultAdapterFactory = new OutputAdapterFactory();

    public InputOutputAdaptersServiceImpl(InputOutputAdaptersConfiguration adaptersConfiguration) {
        List<ResourceDescription> adapterDescriptions =
                adaptersConfiguration.getFactAdapterDescriptionAsList();
        for (ResourceDescription adapterDescription : adapterDescriptions) {
            factAdapterFactory.addInputAdapter(adapterDescription);
        }
        List<ResourceDescription> resultAdapterDescriptions =
                adaptersConfiguration.getResultAdapterDescriptionAsList();
        for (ResourceDescription adapterDescription : resultAdapterDescriptions) {
            resultAdapterFactory.addOutputAdapter(adapterDescription);
        }
    }

    /**
     * Create a new InputManager instance and returns it
     *
     * @param inputs             A list of ResourceDescriptions presenting information abouts facts
     * @param messageInterceptor MessageInterceptor to be used for accessing the data in the message
     *                           being transit
     * @return A new InputManager instance
     */
    public InputManager createInputManager(List<ResourceDescription> inputs,
                                           MessageInterceptor messageInterceptor) {

        return new InputManager(factAdapterFactory, inputs, messageInterceptor);
    }

    /**
     * Create a new OutputManager instance and returns it
     *
     * @param outputs            A list of ResourceDescriptions presenting information abouts results
     * @param messageInterceptor MessageInterceptor to be used for accessing the data in
     *                           the message being transit
     * @return A new OutputManager instance
     */
    public OutputManager createOutputManager(List<ResourceDescription> outputs,
                                             MessageInterceptor messageInterceptor) {

        return new OutputManager(resultAdapterFactory, outputs, messageInterceptor);
    }

    /**
     * @return the InputAdapterFactory of the rule server
     */
    public InputAdapterFactory getFactAdapterFactory() {

        return factAdapterFactory;
    }

    /**
     * @return the OutputAdapterFactory of the rule server
     */
    public OutputAdapterFactory getResultAdapterFactory() {

        return resultAdapterFactory;
    }
}
