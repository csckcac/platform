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
package org.wso2.carbon.identity.entitlement.proxy;

import java.util.Map;

public class PDPConfig {

    private Map<String, String[]> appToPDPMap;
    private String defaultAppId;
    private boolean enableCaching = false;
    private int maxCacheEntries = 1;

    public PDPConfig(Map<String, String[]> appToPDPMap, String defaultAppId, boolean enableCaching, int maxCacheEntries) {
        this.defaultAppId = defaultAppId;
        this.appToPDPMap = appToPDPMap;
        this.enableCaching = enableCaching;
        if (maxCacheEntries >= 1) {
            this.maxCacheEntries = maxCacheEntries;
        }

    }

    public String getDefaultAppId() {
        return defaultAppId;
    }

    public void setDefaultAppId(String defaultAppId) {
        this.defaultAppId = defaultAppId;
    }

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }

    public Map<String, String[]> getAppToPDPMap() {
        return appToPDPMap;
    }

    public void setAppToPDPMap(Map<String, String[]> appToPDPMap) {
        this.appToPDPMap = appToPDPMap;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    public void setMaxCacheEntries(int maxCacheEntries) {
        this.maxCacheEntries = maxCacheEntries;
    }
}
