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
package org.wso2.carbon.rssmanager.ui.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a Database Instance created by an RSS Server.
 */
public class DatabaseInstance {

	private int databaseInstanceId;

	private String name;

	private int rssInstanceId;

	private Map<String, String> properties;

    private int tenantId;

	public DatabaseInstance(int databaseInstanceId, String name, int rssInstanceId, int tenantId) {
		this.databaseInstanceId = databaseInstanceId;
		this.name = name;
		this.rssInstanceId = rssInstanceId;
		this.properties = new HashMap<String, String>();
        this.tenantId = tenantId;
	}

    public DatabaseInstance() {

    }

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public int getDatabaseInstanceId() {
		return databaseInstanceId;
	}

	public void setDatabaseInstanceId(int databaseInstanceId) {
		this.databaseInstanceId = databaseInstanceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRssInstanceId() {
		return rssInstanceId;
	}

	public void setRssInstanceId(int rssInstanceId) {
		this.rssInstanceId = rssInstanceId;
	}

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
