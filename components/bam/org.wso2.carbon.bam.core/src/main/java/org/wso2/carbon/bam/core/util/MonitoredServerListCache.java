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


import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;

import java.util.*;

/**
 * Cache to hold server details from the database.
 */
public class MonitoredServerListCache {
    private Map<Integer, ServerDO> serverList;

    public MonitoredServerListCache() {
        serverList = new HashMap<Integer, ServerDO>();
    }

    public List<ServerDO> getServers() {
        List<ServerDO> retList = new ArrayList<ServerDO>();

        Collection c = serverList.values();
        if (c != null) {
            for (Object o : c) {
                retList.add((ServerDO) o);
            }
        }
        return retList;
    }

    public void addServer(ServerDO server) {
        serverList.put(server.getId(), server);
    }

    public void removeServer(int serverID) {
        serverList.remove(serverID);
    }

    public ServerDO getServer(int serverID) {
        return serverList.get(serverID);
    }
}
