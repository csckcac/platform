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
package org.wso2.carbon.rssmanager.core.entity;

import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.common.RSSManagerHelper;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;

import javax.sql.DataSource;

/**
 * Class to represent an RSS Server Instance.
 */
public class RSSInstance {

	private String name;
	
	private String serverURL;
	
	private String dbmsType;
	
	private String instanceType;

    private String serverCategory;
	
	private String adminUsername;
	
	private String adminPassword;
	
	private int tenantId;

    private DataSource dataSource;

	public RSSInstance(String name, String serverURL,
			String dbmsType, String instanceType, String serverCategory, String adminUsername,
			String adminPassword, int tenantId) {
		this.name = name;
		this.serverURL = serverURL;
		this.dbmsType = dbmsType;
		this.instanceType = instanceType;
        this.serverCategory = serverCategory;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.tenantId = tenantId;
        this.dataSource = initDataSource();
	}

    public RSSInstance(String name, String serverURL,
			String dbmsType, String instanceType, String serverCategory, String adminUsername,
			String adminPassword, int tenantId, DataSource dataSource) {
		this.name = name;
		this.serverURL = serverURL;
		this.dbmsType = dbmsType;
		this.instanceType = instanceType;
        this.serverCategory = serverCategory;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.tenantId = tenantId;
        this.dataSource = dataSource;
	}

    public RSSInstance() {
        
    }

    private DataSource initDataSource() {
        RDBMSConfiguration config = new RDBMSConfiguration();
        config.setUrl(this.getServerURL());
        config.setUsername(this.getAdminUsername());
        config.setPassword(this.getAdminPassword());
        config.setDriverClassName(RSSManagerHelper.getDatabaseDriver(this.getServerURL()));
        config.setTestOnBorrow(true);
        config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        return RSSManagerUtil.createDataSource(config);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getDbmsType() {
		return dbmsType;
	}

	public void setDbmsType(String dbmsType) {
		this.dbmsType = dbmsType;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

    public String getServerCategory() {
        return serverCategory;
    }

    public void setServerCategory(String serverCategory) {
        this.serverCategory = serverCategory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
