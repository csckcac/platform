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

import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rulecep.commons.NameValuePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * implementation for Drools API
 */
public class DroolsStatelessSession implements Session {

    private StatelessKnowledgeSession statelessKnowledgeSession;

    public DroolsStatelessSession(StatelessKnowledgeSession statelessKnowledgeSession) {
        this.statelessKnowledgeSession = statelessKnowledgeSession;
    }

    /**
     * Executes the drools stateless session with the given facts and returns the results of execution
     *
     * @param facts facts as a list of objects
     * @return A list of objects representing results of rule execution
     */
    public List execute(List<Object> facts) {

        final List<Command> cmds = new ArrayList<Command>();
        for (Object fact : facts) {
            if (fact instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) fact;
                Object value = pair.getValue();
                String name = pair.getName();
                if (value != null) {
                    cmds.add(CommandFactory.newInsert(value, name));
                }
            } else {
                cmds.add(CommandFactory.newInsert(fact));
            }
        }

        final ExecutionResults executionResults = statelessKnowledgeSession.execute(
                CommandFactory.newBatchExecution(cmds));
        final Collection<String> results = executionResults.getIdentifiers();
        final List tobeReturn = new ArrayList();
        for (String name : results) {
            if (name != null) {
                tobeReturn.add(executionResults.getValue(name));
            }
        }
        return tobeReturn;
    }

    public void release() {

    }

    public boolean isExpired() {
        return true;
    }
}
