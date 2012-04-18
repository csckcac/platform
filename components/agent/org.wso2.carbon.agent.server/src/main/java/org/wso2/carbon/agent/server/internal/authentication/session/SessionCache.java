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

package org.wso2.carbon.agent.server.internal.authentication.session;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Cache that contains all sessions
 */
public class SessionCache {

    private ConcurrentMap<String, AgentSession> sessionCache;

    public SessionCache(int expirationTimeInMinutes) {
        sessionCache = new MapMaker()
                .expiration(expirationTimeInMinutes, TimeUnit.MINUTES)
                .makeComputingMap(new SessionFunction());
    }

    static class SessionFunction implements Function<String, AgentSession> {
        @Override
        public AgentSession apply(@Nullable String sessionId) {
            return new AgentSession(sessionId);
        }
    }

    public AgentSession getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionCache.remove(sessionId);
    }
}
