/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.rule.backend.jsr94;

import org.wso2.carbon.rule.kernel.backend.Session;
import org.wso2.carbon.rule.common.exception.RuleRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.rules.StatefulRuleSession;
import javax.rules.Handle;
import javax.rules.InvalidHandleException;
import javax.rules.InvalidRuleSessionException;
import java.util.List;
import java.rmi.RemoteException;

public class JSR94StatefulSession implements Session {

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

    @Override
    public List execute(List facts) throws RuleRuntimeException {
         try {

            synchronized (resourceLock) {  // TODO optimize this

                statefulRuleSession.addObjects(facts);
                statefulRuleSession.executeRules();

                List results = statefulRuleSession.getObjects();

                return results;
            }
        } catch (InvalidRuleSessionException e) {
            throw new RuleRuntimeException("Error was occurred when executing stateful session", e);
        } catch (RemoteException e) {
            throw new RuleRuntimeException("Error was occurred when executing stateful session", e);
        }
    }

    @Override
    public void destroy() throws RuleRuntimeException {
         try {
            synchronized (resourceLock) { //TODO
                statefulRuleSession.release();
            }
        } catch (RemoteException e) {
            throw new RuleRuntimeException("Error was occurred when executing stateful session", e);
        } catch (InvalidRuleSessionException e) {
            throw new RuleRuntimeException("Error was occurred when executing stateful session",e);
        }
    }
}
