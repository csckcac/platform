/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception;

/**
 * This will be thrown if the EPR of the Agent, to be registered or unregistered
 * is null.
 */
public class NullAgentException extends Exception {

    /**
     * For serializing requirement
     */
    private static final long serialVersionUID = 1766719509942670304L;

    public NullAgentException(String msg) {
        super(msg);
    }

}
