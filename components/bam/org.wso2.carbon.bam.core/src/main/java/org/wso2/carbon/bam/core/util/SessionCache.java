/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.core.util;

import java.util.HashMap;
import java.util.Map;

// TODO: Server URL is no longer unique. Use server.getId() as the key instead
public class SessionCache {
    private Map<String, String> sessoinMap;

    public SessionCache() {
        sessoinMap = new HashMap<String, String>();
    }

    public String getSessionString(String serverURL) {
        return sessoinMap.get(serverURL);
    }

    public void addSessionString(String serverURL, String session) {
        sessoinMap.put(serverURL, session);
    }
}
