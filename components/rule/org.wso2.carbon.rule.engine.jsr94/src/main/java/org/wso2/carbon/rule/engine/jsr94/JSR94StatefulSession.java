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
import org.wso2.carbon.rule.core.Expirable;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rulecep.adapters.TransientObject;

import javax.rules.Handle;
import javax.rules.InvalidHandleException;
import javax.rules.InvalidRuleSessionException;
import javax.rules.StatefulRuleSession;
import java.rmi.RemoteException;
import java.util.List;

/**
 * StatefulSession implementation for JSR94 API
 */
public class JSR94StatefulSession implements Session, Expirable {

    private static Log log = LogFactory.getLog(JSR94StatefulSession.class);
    private StatefulRuleSession statefulRuleSession;
    private long nextTime;
    private final static long DEFAULT_INTERVAL = 1000 * 60 * 10;
    /* Lock used to ensure thread-safe execution of the rule engine */
    private final Object resourceLock = new Object();

    public JSR94StatefulSession(StatefulRuleSession statefulRuleSession) {
        this.statefulRuleSession = statefulRuleSession;
        this.nextTime = System.currentTimeMillis() + DEFAULT_INTERVAL;
    }

    /**
     * Execute the JSR94 StatefulSession with the given facts and returns the results
     *
     * @param facts facts as a list of objects
     * @return a list of results from the rule execution
     */
    public List execute(List<Object> facts) {

        try {

            synchronized (resourceLock) {  // TODO optimize this

                statefulRuleSession.addObjects(facts);
                statefulRuleSession.executeRules();

                List results = statefulRuleSession.getObjects();
                List handles = statefulRuleSession.getHandles();

                for (Object handleObject : handles) {
                    if (handleObject instanceof Handle) {
                        Handle handle = (Handle) handleObject;
                        try {
                            Object object = statefulRuleSession.getObject(handle);
                            if (object == null) {
                                continue;
                            }
                            TransientObject transientObject =
                                    object.getClass().getAnnotation(TransientObject.class);
                            if (transientObject != null) {
                                statefulRuleSession.removeObject(handle);
                            }
                        } catch (InvalidHandleException e) {
                            throw new LoggedRuntimeException("Error was occurred when accessing " +
                                    "an object from handle : " + handle, e, log);
                        }
                    }
                }

                return results;
            }
        } catch (InvalidRuleSessionException e) {
            throw new LoggedRuntimeException("Error was occurred when executing stateful session",
                    e, log);
        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when executing stateful session",
                    e, log);
        }
    }

    /**
     * Release the rule session to enable to dispose any resources being used by the session
     */
    public void release() {
        try {
            synchronized (resourceLock) { //TODO
                statefulRuleSession.release();
            }
        } catch (RemoteException e) {
            throw new LoggedRuntimeException("Error was occurred when executing stateful session",
                    e, log);
        } catch (InvalidRuleSessionException e) {
            throw new LoggedRuntimeException("Error was occurred when executing stateful session",
                    e, log);
        }
    }

    /**
     * Checks the expiration of the session
     *
     * @return <code>true</code> if the session has been expired
     */
    public boolean isExpired() {
        return nextTime < System.currentTimeMillis();
    }
}
