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
package org.wso2.carbon.rulecep.service;

import org.apache.axis2.description.AxisService;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;

/**
 * Encapsulates an service provider such as rule engine , CEP engine, etc
 */
public abstract class ServiceEngine {
    /** **/
    private AxisService axisService;

    protected ServiceEngine(AxisService axisService) {
        this.axisService = axisService;
    }

    public AxisService getAxisService() {
        return axisService;
    }

    /**
     * Checks whether the results of service invocation can be received with an EDA approach
     *
     * @return <code>true</code>, if the the results of service invocation can be received with an EDA approach
     */
    public abstract boolean canListenForResult();

    /**
     * Add a Listener for the the results of service invocation
     *
     * @param manager              used to covert results into XML
     * @param operationDescription information about operation
     */
    public abstract void registerResultListener(OutputManager manager,
                                                OperationDescription operationDescription);
}
