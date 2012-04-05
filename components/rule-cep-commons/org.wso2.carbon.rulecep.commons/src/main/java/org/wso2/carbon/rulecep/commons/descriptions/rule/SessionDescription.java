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
package org.wso2.carbon.rulecep.commons.descriptions.rule;


import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Session's Meta-data
 * The default session type is stateful
 */
@SuppressWarnings("unused")
public class SessionDescription {

    public final static String STATEFUL_SESSION = "stateful";
    public final static String STATELESS_SESSION = "stateless";

    /**
     * The type of the session - either stateful or stateless
     */
    private String sessionType = STATEFUL_SESSION;

    /**
     * URI of the rule set received from the rule set registration with the rule engine.
     * This is required to create a session associated with that rule set
     */
    private String ruleSetURI;

    private final List<PropertyDescription> sessionProperties =
            new ArrayList<PropertyDescription>();

    public void addSessionPropertyDescription(PropertyDescription propertyDescription) {
        sessionProperties.add(propertyDescription);
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public boolean isStateful() {
        return STATEFUL_SESSION.equals(sessionType);
    }

    public List<PropertyDescription> getSessionProperties() {
        List<PropertyDescription> view = new ArrayList<PropertyDescription>();
        view.addAll(sessionProperties);
        return view;
    }

    public String getRuleSetURI() {
        return ruleSetURI;
    }

    public void setRuleSetURI(String ruleSetURI) {
        this.ruleSetURI = ruleSetURI;
    }
}
