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
package org.wso2.carbon.dataservices.core.description.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants.AutoCommit;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * This class is the base class used for all SQL based (RDBMS) data source configurations.
 */
public abstract class SQLConfig extends Config {
	
	private static final Log log = LogFactory.getLog(SQLConfig.class);

	private String validationQuery;
	
	private boolean jdbcBatchUpdateSupport;
	
	private AutoCommit autoCommit;
	
	public SQLConfig(DataService dataService, String configId, 
			String type, Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, type, properties);
		/* set validation query, if exists */
		this.validationQuery = this.getProperty(RDBMS.VALIDATION_QUERY);
		this.processAutoCommitValue();		
	}
	
	private void processAutoCommitValue() throws DataServiceFault {
		String autoCommitProp = this.getProperty(RDBMS.AUTO_COMMIT);
		if (!DBUtils.isEmptyString(autoCommitProp)) {
			autoCommitProp = autoCommitProp.trim();
			try {
				boolean acBool = Boolean.parseBoolean(autoCommitProp);
				if (acBool) {
					this.autoCommit = AutoCommit.AUTO_COMMIT_ON;
				} else {
					this.autoCommit = AutoCommit.AUTO_COMMIT_OFF;
				}
			} catch (Exception e) {
				throw new DataServiceFault(e, "Invalid autocommit value in config: " + 
						autoCommitProp + ", autocommit should be a boolean value");
			}		
		} else {
			this.autoCommit = AutoCommit.DEFAULT;			
		}
	}
	
	public boolean hasJDBCBatchUpdateSupport() {
		return jdbcBatchUpdateSupport;
	}
	
	public AutoCommit getAutoCommit() {
		return autoCommit;
	}
		
	protected void initSQLDataSource() throws SQLException, DataServiceFault {
		Connection conn = this.createConnection();
		/* check if we have JDBC batch update support */
		this.jdbcBatchUpdateSupport = conn.getMetaData().supportsBatchUpdates();
		conn.close();
	}
		
	public abstract DataSource getDataSource();
	
	public abstract boolean isStatsAvailable();
	
	public abstract int getActiveConnectionCount();
	
	public abstract int getIdleConnectionCount();
		
	public String getValidationQuery() {
		return validationQuery;
	}
	
	public Connection createConnection() throws SQLException, DataServiceFault {
		if (log.isDebugEnabled()){
			log.debug("Creating data source connection");
		}
		DataSource ds = this.getDataSource();
		if (ds != null) {
			Connection conn = ds.getConnection();
			return conn;
		} else {
			throw new DataServiceFault("The data source is nonexistent");
		}
	}
	
	@Override
	public boolean isActive() {
		try {
			Connection conn = this.getDataSource().getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			log.error("Error in checking SQL config availability", e);
			return false;
		}
	}
	
}
