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

package org.wso2.carbon.bam.common.dataobjects.stats;


/*
 * Common attributes for statistics dataobjects classes
 */
public abstract class StatisticsDO {
    private int serverID;
    private String serverURL;

    public StatisticsDO() {

    }

    public StatisticsDO(String serverURL) {
        this.serverURL = serverURL;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

//    public ServerDO getServer() throws BAMException {
//        return BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getMonitoredServer(getServerID());
//    }
}
