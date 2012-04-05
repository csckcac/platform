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
package org.wso2.carbon.rule.service;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.service.MessageReceiverFactory;
import org.wso2.carbon.rulecep.service.ServiceEngine;

/**
 * Create rule service message receivers 
 */
public class RuleServiceMessageReceiverFactory implements MessageReceiverFactory {

    public AbstractInMessageReceiver createInOnlyMessageReceiver(ServiceEngine serviceEngine,
                                                                 InputManager inputManager,
                                                                 OperationDescription operationDescription) {

        RuleServiceEngine ruleServiceEngine = (RuleServiceEngine) serviceEngine;
        return new RuleServiceInOnlyMessageReceiver(ruleServiceEngine.getRuleEngine(),
                inputManager,
                createSessionDescription(ruleServiceEngine));
    }

    public AbstractInOutMessageReceiver createInOutMessageReceiver(ServiceEngine serviceEngine,
                                                                   InputManager inputManager,
                                                                   OutputManager outputManager,
                                                                   OperationDescription operationDescription) {
        RuleServiceEngine ruleServiceEngine = (RuleServiceEngine) serviceEngine;
        return new RuleServiceInOutMessageReceiver(ruleServiceEngine.getRuleEngine(),
                inputManager,
                outputManager,
                createSessionDescription(ruleServiceEngine));
    }

    private SessionDescription createSessionDescription(RuleServiceEngine ruleServiceEngine) {

        SessionDescription sessionDescription = new SessionDescription();
        AxisService axisService = ruleServiceEngine.getAxisService();
        if (isStateless(axisService.getScope())) {
            sessionDescription.setSessionType(SessionDescription.STATELESS_SESSION);
        }
        sessionDescription.setRuleSetURI(ruleServiceEngine.getRulesetURI());
        return sessionDescription;
    }

    private boolean isStateless(String scope) {
        return scope == null || "".equals(scope) || Constants.SCOPE_REQUEST.equals(scope);
    }


}
