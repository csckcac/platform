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
package org.wso2.carbon.rule.engine.jsr94;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.Session;

import javax.rules.InvalidRuleSessionException;
import javax.rules.StatelessRuleSession;
import java.rmi.RemoteException;
import java.util.List;

/**
 * StatelessSession implementation for JSR94 API
 */
public class JSR94StatelessSession implements Session {

    private static Log log = LogFactory.getLog(JSR94StatelessSession.class);

    /**
     * JSR94 StatelessSession
     */
    private StatelessRuleSession statelessRuleSession;

    public JSR94StatelessSession(StatelessRuleSession statelessRuleSession) {
        this.statelessRuleSession = statelessRuleSession;
    }

    /**
     * Execute the JSR94 StatelessSession with the given facts and returns the results
     *
     * @param facts facts as a list of objects
     * @return a list of results from the rule execution
     */
    public List execute(List<Object> facts) {
        try {
            return statelessRuleSession.executeRules(facts);
        } catch (InvalidRuleSessionException e) {
            throw new LoggedRuntimeException("Error was occurred when executing StateLess Session",
                    e, log);
        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when executing StateLess Session",
                    e, log);
        }
    }

    /**
     * Release the stateless rule session to enable to dispose any resources being used by the session
     */
    public void release() {
        try {
            statelessRuleSession.release();
        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when executing StateLess Session",
                    e, log);
        } catch (InvalidRuleSessionException e) {
            throw new LoggedRuntimeException("Error was occurred when executing StateLess Session",
                    e, log);
        }
    }

    /**
     * Checks the expiration of the session
     *
     * @return <code>true</code> always
     */
    public boolean isExpired() {
        return true;
    }
}
