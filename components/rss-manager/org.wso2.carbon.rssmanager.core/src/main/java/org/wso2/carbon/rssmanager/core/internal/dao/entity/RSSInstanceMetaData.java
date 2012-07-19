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

public class RSSInstanceMetaData {

    private String name;

    private String serverUrl;

    private String instanceType;

    private String serverCategory;

    private String tenantDomainName;

    public RSSInstanceMetaData(String name, String serverUrl, String instanceType,
                            String serverCategory, String tenantDomainName) {
        this.name = name;
        this.serverUrl = serverUrl;
        this.instanceType = instanceType;
        this.serverCategory = serverCategory;
        this.tenantDomainName = tenantDomainName;
    }

    public RSSInstanceMetaData() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getServerCategory() {
        return serverCategory;
    }

    public void setServerCategory(String serverCategory) {
        this.serverCategory = serverCategory;
    }

    public String getTenantDomainName() {
        return tenantDomainName;
    }

    public void setTenantDomainName(String tenantDomainName) {
        this.tenantDomainName = tenantDomainName;
    }
}
