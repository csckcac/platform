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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.custom.datasource.CustomDataSource;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.sql.driver.TConnectionFactory;
import org.wso2.carbon.dataservices.sql.driver.TCustomConnection;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;

/**
 * This class represents a data services custom data source.
 */
public class CustomConfig extends SQLConfig {

	private static final Log log = LogFactory.getLog(CustomConfig.class);
	
	private CustomSQLDataSource dataSource;
	
	public CustomConfig(DataService dataService, String configId,
			Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.RDBMS, properties);
		String dsClass = properties.get(DBConstants.CustomDatasource.DATA_SOURCE_CLASS);
		try {
			CustomDataSource customDS = (CustomDataSource) Class.forName(dsClass).newInstance();
			String dataSourcePropsString = properties.get(
					DBConstants.CustomDatasource.DATA_SOURCE_PROPS);
			Map<String, String> dsProps;
			if (dataSourcePropsString != null) {
				dsProps = DBUtils.extractProperties(AXIOMUtil.stringToOM(
						dataSourcePropsString));
			} else {
				dsProps = new HashMap<String, String>();
			}
			this.populateStandardProps(dsProps);
			customDS.init(dsProps);
			this.dataSource = new CustomSQLDataSource(customDS);
			if (log.isDebugEnabled()) {
				log.debug("Creating custom data source with info: #" + 
						this.getDataService().getTenantId() + "#" + 
						this.getDataService() + "#" + this.getConfigId());
			}
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating custom data source config: " +
					e.getMessage());
		}
	}
	
	private void populateStandardProps(Map<String, String> dsProps) {
		String dsInfo = this.getDataService().getTenantId() + "#"
				+ this.getDataService().getName() + "#" + this.getConfigId();
		dsProps.put(CustomDataSource.DATASOURCE_ID, UUID.fromString(dsInfo).toString());
	}
	
	@Override
	public DataSource getDataSource() throws DataServiceFault {
		return dataSource;
	}

	@Override
	public boolean isStatsAvailable() throws DataServiceFault {
		return false;
	}

	@Override
	public int getActiveConnectionCount() throws DataServiceFault {
		return -1;
	}

	@Override
	public int getIdleConnectionCount() throws DataServiceFault {
		return -1;
	}

	@Override
	public void close() {
		this.dataSource.close();
	}
	
	/**
	 * Custom SQL data source implementation.
	 */
	public static class CustomSQLDataSource implements DataSource {

		private PrintWriter logWriter;
		
		private int loginTimeout;
		
		private SQLParserCustomDataSourceAdapter dataSource;
		
		private Properties customDSProps;
		
		public CustomSQLDataSource(CustomDataSource dataSource) {
			this.dataSource = new SQLParserCustomDataSourceAdapter(dataSource);
			this.customDSProps = new Properties();
			this.customDSProps.put(TCustomConnection.CUSTOM_DATASOURCE, this.dataSource);
		}
		
		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return logWriter;
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return loginTimeout;
		}

		@Override
		public void setLogWriter(PrintWriter logWriter) throws SQLException {
			this.logWriter = logWriter;
		}

		@Override
		public void setLoginTimeout(int loginTimeout) throws SQLException {
			this.loginTimeout = loginTimeout;
		}

		@Override
		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> arg0) throws SQLException {
			return null;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return TConnectionFactory.createConnection(Constants.CUSTOM, this.customDSProps);
		}

		@Override
		public Connection getConnection(String username, String password)
				throws SQLException {
			return this.getConnection();
		}
		
		public void close() {
			this.dataSource.close();
		}
		
		/**
		 * Adapter class to bridge the functionality between the data services custom data source
		 * and the SQL parser custom data source.
		 */
		public class SQLParserCustomDataSourceAdapter implements
				org.wso2.carbon.dataservices.sql.driver.TCustomConnection.CustomDataSource {
			
			private CustomDataSource customDS;
			
			public SQLParserCustomDataSourceAdapter(CustomDataSource customDS) {
				this.customDS = customDS;
			}

			@Override
			public void createDataTable(String name, Map<String, Integer> columns)
					throws SQLException {
			}

			@Override
			public void dropDataTable(String name) throws SQLException {
				
			}

			@Override
			public DataTable getDataTable(String name) throws SQLException {
				return null;
			}

			@Override
			public Set<String> getDataTableNames() throws SQLException {
				return null;
			}

			@Override
			public void init(Properties props) throws SQLException {
			}
			
			public void close() {
				this.customDS.close();
			}
			
		}
		
	}
	
}
