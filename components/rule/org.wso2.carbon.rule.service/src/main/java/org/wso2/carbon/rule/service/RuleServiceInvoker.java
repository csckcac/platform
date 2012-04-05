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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rule.server.RuleEngine;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Invokes the rule engine with facts and returns results
 */
public class RuleServiceInvoker {

    private static final Log log = LogFactory.getLog(RuleServiceInvoker.class);

    /**
     * Access facade to rule service provider
     */
    private RuleEngine ruleEngine;

    /**
     * To formulate the facts from the Axis2 Message Context
     */
    private InputManager inputManager;
    /**
     * Information about the session - the default is a stateless session
     */
    private SessionDescription sessionDescription;

    /**
     * The previous expired session
     */
//    private Session lastSession;  TODO add proper session  clean up

    /* Lock used to ensure thread-safe creation of the rule session*/
    private final Object resourceLock = new Object();

    public RuleServiceInvoker(RuleEngine ruleEngine,
                              InputManager inputManager,
                              SessionDescription sessionDescription) {
        this.ruleEngine = ruleEngine;
        this.inputManager = inputManager;
        this.sessionDescription = sessionDescription;
    }

    /**
     * Extracts the facts from the request message and inserts into the rule engine and then executes
     * the rule engine.
     * Returns if there any results
     *
     * @param requestMessageContext request message
     * @return A list containing results of rule engine execution
     */
    public List invoke(MessageContext requestMessageContext) {

        Session session;
        if (!sessionDescription.isStateful()) {
            session = ruleEngine.createSession(sessionDescription);
        } else {
            ServiceContext serviceContext =
                    requestMessageContext.getServiceContext();
            session = (Session) serviceContext.getProperty(RuleConstants.RULE_SESSION);
            if (session == null) {

                synchronized (resourceLock) {
                    session = (Session) serviceContext.getProperty(RuleConstants.RULE_SESSION);
                    if (session == null) {  // double check
                        session = ruleEngine.createSession(sessionDescription);
                        serviceContext.setProperty(RuleConstants.RULE_SESSION, session);
                    }
                }
            }
        }

        List<Object> facts = inputManager.processInputs(requestMessageContext);

        if (!facts.isEmpty()) {
            List results = session.execute(facts);
            if (!sessionDescription.isStateful()) {
                session.release();
            }
            return results;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There is no facts to be injected into the rule engine");
            }
            return new ArrayList();
        }
    }
}
