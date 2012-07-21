/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cep.core.backend;

import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.listener.CEPEventListener;

/**
 * this class represents the interface between the real cep engine and wso2 cep
 * engine. Real cep engines like esper and fusion should implement this interface
 * in order to process the cep messags.
 */
public interface CEPBackEndRuntime {

    /**
     * adds an event object as defined in the event description. This causs some of the
     * rules to be fired
     *
     * @param event   - object representing the event data
     * @param inputMapping - Mapping to the topic which publish data
     *                details of the input stream to add events. eg. entry points in drools fusion.
     * @throws CEPEventProcessingException - if cep engine finds any problem with the
     *                                     event.
     */
    public void insertEvent(Object event, InputMapping inputMapping)
            throws CEPEventProcessingException;

    /**
     * adding a query to the cep engine. this source can either contains the source
     * query or a .drl file in the case of fusion
     *
     * @param queryName       - Name of the Query to be added
     * @param expression      - cep rule source
     * @param cepEventListener - wso2 cep engine pass this object to receive the events
     *                        back from the cep engine.
     * @return StatementName from the back end runtime
     * @throws CEPConfigurationException - if there is a problem with the query statement
     */
    public void addQuery(String queryName, Expression expression, CEPEventListener cepEventListener)
            throws CEPConfigurationException;

    /**
     * Removing a specific query from back end runtime
     *
     * @param queryName - name of the query to be removed
     * @throws CEPConfigurationException - If there is problem with removing the query
     */
    public void removeQuery(String queryName) throws CEPConfigurationException;

    /**
     * Removing all available queries of a bucket from back end runtime
     *
     * @throws CEPConfigurationException - If there is a error in removing al the queries
     */
    public void removeAllQueries() throws CEPConfigurationException;

    /**
     * Adding additional Inputs
     * @param input
     * @throws CEPConfigurationException
     */
    void addInput(Input input) throws CEPConfigurationException;

    /**
     * removing inputs
     * @param input
     */
    void removeInput(Input input) throws CEPConfigurationException;

    /**
     * Called after adding all inputs and queries
     */
    void init();
}

