/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.core.internal.authentication.session;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Cache that contains all agent sessions
 */
public class SessionCache {

    private ConcurrentMap<SessionBean, AgentSession> sessionCache;

    public SessionCache(int expirationTimeInMinutes) {
        sessionCache = new MapMaker()
                .expiration(expirationTimeInMinutes, TimeUnit.MINUTES)
                .makeComputingMap(new SessionFunction());
    }

    static class SessionFunction implements Function<SessionBean, AgentSession> {
        @Override
        public AgentSession apply( SessionBean sessionBean) {
            return new AgentSession(sessionBean.getSessionId(), sessionBean.getCredentials());
        }
    }

    public AgentSession getSession(SessionBean sessionBean) {
        return sessionCache.get(sessionBean);
    }

    public void removeSession(String sessionId) {
        sessionCache.remove(sessionId);
    }
}
