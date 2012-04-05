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

import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;

/**
 * Factory for creating MessageReceivers
 */
public interface MessageReceiverFactory {

    /**
     * Creates an in only message receiver
     *
     * @param serviceEngine        <code>ServiceEngine</code>
     * @param inputManager         <code>InputManager<code> for creating input for service engine
     * @param operationDescription information about the current operation
     * @return an instance of <code>AbstractInMessageReceiver</code>
     */
    public AbstractInMessageReceiver createInOnlyMessageReceiver(ServiceEngine serviceEngine,
                                                                 InputManager inputManager,
                                                                 OperationDescription operationDescription);

    /**
     * Creates an in out message receiver
     *
     * @param serviceEngine        <code>ServiceEngine</code>
     * @param inputManager         <code>InputManager<code> for creating input for service engine
     * @param outputManager        <code>OutputManager<code> for creating XML messages from results of
     *                             the service engine execution
     * @param operationDescription information about the current operation
     * @return an instance of <code>AbstractInOutMessageReceiver</code>
     */
    public AbstractInOutMessageReceiver createInOutMessageReceiver(ServiceEngine serviceEngine,
                                                                   InputManager inputManager,
                                                                   OutputManager outputManager,
                                                                   OperationDescription operationDescription);
}
