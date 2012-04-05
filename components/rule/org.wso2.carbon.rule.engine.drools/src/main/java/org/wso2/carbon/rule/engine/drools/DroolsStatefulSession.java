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
package org.wso2.carbon.rule.engine.drools;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.wso2.carbon.rule.core.Expirable;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rulecep.commons.NameValuePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * StatefulSession implementation for Drools API
 */

public class DroolsStatefulSession implements Session, Expirable {

    private StatefulKnowledgeSession statefulKnowledgeSession;
    private long nextTime;
    private final static long DEFAULT_INTERVAL = 1000 * 60 * 10;
    private final Map<String, Object> properties;

    public DroolsStatefulSession(StatefulKnowledgeSession statefulKnowledgeSession,
                                 Map<String, Object> properties) {
        this.statefulKnowledgeSession = statefulKnowledgeSession;
        this.properties = properties;
        this.nextTime = System.currentTimeMillis() + DEFAULT_INTERVAL;
    }


    /**
     * Executes the drools stateful session with the given facts and returns the results of execution
     *
     * @param facts facts as a list of objects
     * @return A list of objects representing results of rule execution
     */
    public List execute(List<Object> facts) {

        for (Object fact : facts) {
            if (fact == null) {
                continue;
            }
            if (fact instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) fact;
                Object value = pair.getValue();
                String name = pair.getName();
                String entryPointName = (String) properties.get(name);
                if (entryPointName != null && !"".equals(entryPointName)) {
                    WorkingMemoryEntryPoint entryPoint =
                            statefulKnowledgeSession.getWorkingMemoryEntryPoint(entryPointName);
                    if (entryPoint != null) {
                        entryPoint.insert(value);
                        continue;
                    }
                }
                if (value != null) {
                    statefulKnowledgeSession.insert(value);
                }
            } else {
                statefulKnowledgeSession.insert(fact);
            }
        }
        statefulKnowledgeSession.fireAllRules();
        Iterator results = statefulKnowledgeSession.getObjects().iterator();
        final List tobeReturn = new ArrayList();
        while (results.hasNext()) {
            Object result = results.next();
            if (result != null) {
                tobeReturn.add(result);
            }
        }
        return tobeReturn;
    }

    /**
     * Release the Drool stateful session
     */
    public void release() {
        statefulKnowledgeSession.dispose();
    }

    public boolean isExpired() {
        return nextTime < System.currentTimeMillis();
    }
}
