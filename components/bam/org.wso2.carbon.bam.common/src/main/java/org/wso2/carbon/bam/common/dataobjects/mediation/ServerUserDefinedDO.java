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


package org.wso2.carbon.bam.common.dataobjects.mediation;

import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;

import java.util.Calendar;

/**
 * Data class used for storing user defined key/value pairs for server data.
 */
public class ServerUserDefinedDO extends StatisticsDO {
    private Calendar timestamp;
    private String key;
    private String value;

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    private int serverID;

    public ServerUserDefinedDO() {

    }

    public ServerUserDefinedDO(int  serverID, String serverURL, Calendar timestamp, String key, String value) {
        super(serverURL);
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
        this.serverID = serverID;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
