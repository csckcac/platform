/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.internal.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a Database Instance created by an RSS Server.
 */
public class Database {

	private String name;

    private String url;
	
	private String rssInstanceName;

    private int tenantId = -1;
	
	private Map<String, String> properties;

	public Database(String name, String rssInstanceName) {
		this.name = name;
		this.rssInstanceName = rssInstanceName;
		this.properties = new HashMap<String, String>();
        this.tenantId = tenantId;
	}

    public Database(String name, String rssInstanceName, String url, int tenantId) {
		this.name = name;
		this.rssInstanceName = rssInstanceName;
        this.url = url;
        this.tenantId = tenantId;
		this.properties = new HashMap<String, String>();
	}

    public Database() {

    }
	
	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

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

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

}
