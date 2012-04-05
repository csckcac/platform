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
 * OSGI service for exposing both input and output adapters
 * Input Manager is to adapt given objects into target facts and OutPut Manager is to adapt results
 * from the rule/cep engine into target objects
 */
public interface InputOutputAdaptersService {

    /**
     * Create an input manager instance based on the given input descriptions.
     *
     * @param inputs             A list of ResourceDescriptions presenting information abouts facts
     * @param messageInterceptor MessageInterceptor to be used for accessing the data in the message
     *                           being transit
     * @return a valid <code>InputManager </code>  instance
     */
    InputManager createInputManager(List<ResourceDescription> inputs,
                                    MessageInterceptor messageInterceptor);

    /**
     * Create an output manager instance based on the given input descriptions.
     *
     * @param outputs            A list of ResourceDescriptions presenting information abouts results
     * @param messageInterceptor MessageInterceptor to be used for accessing the data in
     *                           the message being transit
     * @return a valid <code>OutputManager </code>  instance
     */
    OutputManager createOutputManager(List<ResourceDescription> outputs,
                                      MessageInterceptor messageInterceptor);

    /**
     * Returns the InputAdapterFactory in the rule server. This is useful when it
     * is needed to access available input adapters and to add a new input adapter
     *
     * @return a valid <code>InputAdapterFactory</code> instance
     */
    public InputAdapterFactory getFactAdapterFactory();

    /**
     * Returns the OutputAdapterFactory in the rule server. This is useful when it
     * is needed to access available result adapters and to add a new result adapter
     *
     * @return a valid <code>OutputAdapterFactory</code> instance
     */
    public OutputAdapterFactory getResultAdapterFactory();
}
