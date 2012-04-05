/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.description;

public class DatabaseInstanceEntry {

    private int dbInstanceId;

    private String dbName;

    private String dbUrl;

    private int rssInstanceId;

    private String rssName;

    private String rssTenantDomain;

    public DatabaseInstanceEntry(int dbInstanceId, String dbName, String dbUrl, int rssInstanceId,
                                 String rssName, String rssTenantDomain) {
        this.dbInstanceId = dbInstanceId;
        this.dbName = dbName;
        this.dbUrl = dbUrl;
        this.rssInstanceId = rssInstanceId;
        this.rssName = rssName;
        this.rssTenantDomain = rssTenantDomain;
    }

    public DatabaseInstanceEntry() {}

    public int getDbInstanceId() {
        return dbInstanceId;
    }

    public void setDbInstanceId(int dbInstanceId) {
        this.dbInstanceId = dbInstanceId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getRssName() {
        return rssName;
    }

    public void setRssName(String rssName) {
        this.rssName = rssName;
    }

    public String getRssTenantDomain() {
        return rssTenantDomain;
    }

    public void setRssTenantDomain(String rssTenantDomain) {
        this.rssTenantDomain = rssTenantDomain;
    }

    public int getRssInstanceId() {
        return rssInstanceId;
    }

    public void setRssInstanceId(int rssInstanceId) {
        this.rssInstanceId = rssInstanceId;
    }
}
