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
package org.wso2.carbon.rssmanager.core.internal.dao.entity;

public class DatabaseMetaData {

    private String name;

    private String url;

    private String rssInstanceName;

    private String rssTenantDomain;

    public DatabaseMetaData(String name, String url, String rssInstanceName,
                            String rssTenantDomain) {
        this.name = name;
        this.url = url;
        this.rssInstanceName = rssInstanceName;
        this.rssTenantDomain = rssTenantDomain;
    }

    public DatabaseMetaData() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRssInstanceName() {
        return rssInstanceName;
    }

    public void setRssInstanceName(String rssInstanceName) {
        this.rssInstanceName = rssInstanceName;
    }

    public String getRssTenantDomain() {
        return rssTenantDomain;
    }

    public void setRssTenantDomain(String rssTenantDomain) {
        this.rssTenantDomain = rssTenantDomain;
    }

}
