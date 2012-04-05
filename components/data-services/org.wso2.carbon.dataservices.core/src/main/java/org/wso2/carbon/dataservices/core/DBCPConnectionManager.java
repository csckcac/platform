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
package org.wso2.carbon.dataservices.core;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp.managed.ManagedDataSource;
import org.apache.commons.dbcp.managed.XAConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.RDBMSUtils;
import org.wso2.carbon.dataservices.core.description.config.Config;
import org.wso2.carbon.dataservices.core.description.config.RDBMSConfig;
import org.wso2.carbon.dataservices.core.description.xa.XADataSourceInfo;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * Helper for DBCP library based JDBC connection pooling.
 */
@SuppressWarnings("deprecation")
public class DBCPConnectionManager {
	
	private static final Log log = LogFactory.getLog(DBCPConnectionManager.class);
	
	/** Encapsulated data source object */
	private DataSource datasource;
	
	/** Connection pool */
	private GenericObjectPool pool;
	
	/**
	 * Returns the DataSource object created by the pooling manager,
	 * this object is used for retrieving pooled JDBC connections. 
	 * @see DataSourcedatasource
	 */
	public DataSource getDatasource() {
		return datasource;
	}
	
	public void close() {
		try {
			this.getObjectPool().close();
		} catch (Exception e) {
			log.error("Error in closing DBCPConnectionManager: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Creates an instance using the information in the given Config object.
	 */
	public DBCPConnectionManager(Config config) throws DataServiceFault {
		this(config, null);
	}
	
	/**
	 * Creates a DBCPConnectionManager with the given configuration and an external data source,
	 * the external data source will be usually retrieved from JNDI.
	 * @param config The data source configuration
	 * @param externalDS External data source
	 * @throws DataServiceFault
	 */
	public DBCPConnectionManager(Config config, DataSource externalDS) throws DataServiceFault {
		try {
			connectToDB(config, externalDS);
		} catch (Exception e) {
			throw new DataServiceFault(e, 
					"Error occured connecting to database using connection pooling manager");
		}
	}

	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
			log.error("Error occured when finalizing.", e);
		}
	}
	
	/**
	 * Creates the poolable DataSource object with the given parameters.
	 */
	private void connectToDB(Config config, DataSource externalDS) throws DataServiceFault {	
		String driverClass = config.getProperty(RDBMS.DRIVER);
		try {
			if (!DBUtils.isEmptyString(driverClass)) {
				java.lang.Class.forName(driverClass);
			}
		} catch (ClassNotFoundException e) {
			throw new DataServiceFault(e, "Error locating class " + driverClass);
		}
		try {
			this.datasource = setupDataSource(config, externalDS);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error occured while creating datasource."); 
		}
	}

	public ObjectPool getObjectPool() {
		return this.pool;
	}

	/**
	 * Create a generic non-XA connection factory.
	 * 
	 * @param config
	 *            Configuration which contains the connection properties
	 * @return Newly created pool connection
	 * @throws Exception
	 *             Error occurred when parsing properties to create the
	 *             connection factory
	 */
	private GenericObjectPool createGenericConnectionFactory(Config config) throws Exception {
		GenericObjectPool poolConnection = new GenericObjectPool();

		String maxPool = config.getProperty(RDBMS.MAX_POOL_SIZE);
		String maxIdle = config.getProperty(RDBMS.MAX_IDLE);
		String minPool = config.getProperty(RDBMS.MIN_POOL_SIZE);
		String maxWait = config.getProperty(RDBMS.MAX_WAIT);
		String testOnReturn = config.getProperty(RDBMS.TEST_ON_RETURN);
		String testOnBorrow = config.getProperty(RDBMS.TEST_ON_BORROW);
		String testWhileIdle = config.getProperty(RDBMS.TEST_WHILE_IDLE);
		String timeBetweenEvictionRunsMillis = config.getProperty(
				RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLS);
		String numTestsPerEvictionRun = config.getProperty(RDBMS.NUM_TESTS_PER_EVICTION_RUN);
		String minEvictableIdleTimeMillis = config.getProperty(
				RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS);
		poolConnection.setTestOnBorrow(true);
		int minPoolSize = DBConstants.DEFAULT_DBCP_MIN_POOL_SIZE;
		int maxPoolSize = DBConstants.DEFAULT_DBCP_MAX_POOL_SIZE;
		try {
			if (!DBUtils.isEmptyString(maxPool)) {
				maxPoolSize = Integer.valueOf(maxPool).intValue();
				poolConnection.setMaxActive(maxPoolSize);
			}
			if (!DBUtils.isEmptyString(minPool)) {
				minPoolSize = Integer.valueOf(minPool).intValue();
				poolConnection.setMinIdle(minPoolSize);
			}
			int maxPoolIdle = poolConnection.getMaxIdle();
			if (!DBUtils.isEmptyString(maxIdle)) {
				maxPoolIdle = Integer.valueOf(maxIdle).intValue();
				poolConnection.setMaxIdle(maxPoolIdle);
			}
			if (!DBUtils.isEmptyString(maxWait)) {
				int maxPoolWait = Integer.valueOf(maxWait).intValue();
				poolConnection.setMaxWait(maxPoolWait);
			}
			if (!DBUtils.isEmptyString(testOnBorrow)) {
				poolConnection.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
			}
			if (!DBUtils.isEmptyString(testOnReturn)) {
				poolConnection.setTestOnReturn(Boolean.parseBoolean(testOnReturn));
			}
			if (!DBUtils.isEmptyString(testWhileIdle)) {
				poolConnection.setTestWhileIdle(Boolean.parseBoolean(testWhileIdle));
			}
			if (!DBUtils.isEmptyString(timeBetweenEvictionRunsMillis)) {
				long timeBetweenPoolEvictionRunsMillis = Long
						.valueOf(timeBetweenEvictionRunsMillis).longValue();
				poolConnection.setTimeBetweenEvictionRunsMillis(timeBetweenPoolEvictionRunsMillis);
			}
			if (!DBUtils.isEmptyString(numTestsPerEvictionRun)) {
				int numTestsPerPoolEvictionRun = Integer.valueOf(numTestsPerEvictionRun).intValue();
				poolConnection.setNumTestsPerEvictionRun(numTestsPerPoolEvictionRun);
			}
			if (!DBUtils.isEmptyString(minEvictableIdleTimeMillis)) {
				long minPoolEvictableIdleTimeMillis = Long.valueOf(minEvictableIdleTimeMillis)
						.longValue();
				poolConnection.setMinEvictableIdleTimeMillis(minPoolEvictableIdleTimeMillis);
			}
			return poolConnection;
		} catch (NumberFormatException e) {
			log.error("Non-numeric value found for numeric pool configuration property", e);
			throw e;
		}

	}
		
	/**
	 * Creates a DBCP pooled data source with the given parameters and a data source.
	 * @param config Connection parameters
	 * @param externalDS The data source to be used
	 * @return Pooled data source
	 * @throws Exception
	 */
	private DataSource setupDataSource(Config config, DataSource externalDS) throws Exception {
		ConnectionFactory connectionFactory;
		DataService dataService = config.getDataService();
		String jdbcURL = config.getProperty(RDBMS.PROTOCOL);
		String userName = config.getProperty(RDBMS.USER);
		String password = DBUtils.resolvePasswordValue(config.getDataService(),
				config.getProperty(RDBMS.PASSWORD));
		String removeAbandoned = config.getProperty(RDBMS.REMOVE_ABANDONED);
		String removeAbandonedTimeout = config.getProperty(RDBMS.REMOVE_ABONDONED_TIMEOUT);
		String logAbandoned = config.getProperty(RDBMS.LOG_ABANDONED);
		String transactionIsolation = config.getProperty(RDBMS.TRANSACTION_ISOLATION);
		int defaultTransactionIsolation = RDBMSUtils.toIntTransactionIsolation(transactionIsolation);
		
		/* XA data source */
		boolean isXADS = (config instanceof RDBMSConfig) && ((RDBMSConfig) config).hasXADS();

		if (isXADS) {
			if (!dataService.isEnableXA()) {
				throw new DataServiceFault(
						"Internal XADataSource: XA transaction support must be enabled to use XADataSources");
			}
			XADataSourceInfo xaInfo = ((RDBMSConfig) config).getXADataSourceInfo();	
			connectionFactory = new DataSourceXAConnectionFactory(
					dataService.getDSSTxManager().getTransactionManager(), xaInfo.getXADataSource());
		} else if (externalDS != null) {
			if (externalDS instanceof XADataSource) {
				if (!dataService.isEnableXA()) {
					throw new DataServiceFault(
							"External XADataSource: XA transaction support must be enabled to use XADataSources");
				}
				connectionFactory = new DataSourceXAConnectionFactory(
						dataService.getDSSTxManager().getTransactionManager(), (XADataSource) externalDS);
			} else {
				connectionFactory = new DataSourceConnectionFactory(externalDS);
			}
		} else {
			connectionFactory = new DriverManagerConnectionFactory(jdbcURL, userName, password);
			if (dataService.isEnableXA()) {
				connectionFactory = new LocalXAConnectionFactory(
						dataService.getDSSTxManager().getTransactionManager(), connectionFactory);				
			}
		}
		this.pool = createGenericConnectionFactory(config);
		AbandonedConfig abandonedConfig = new AbandonedConfig();
		if (!DBUtils.isEmptyString(logAbandoned)) {
			abandonedConfig.setLogAbandoned(Boolean.parseBoolean(logAbandoned));
		}
		if (!DBUtils.isEmptyString(removeAbandonedTimeout)) {
			abandonedConfig.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimeout));
		}
		if (!DBUtils.isEmptyString(removeAbandoned)) {
			abandonedConfig.setRemoveAbandoned(Boolean.parseBoolean(removeAbandoned));
		}

		String validationQuery = config.getProperty(RDBMS.VALIDATION_QUERY);
		boolean hasValQuery = validationQuery != null && validationQuery.trim().length() > 0;
		PoolableConnectionFactory factory = new PoolableConnectionFactory(connectionFactory,
				this.pool, null, hasValQuery ? validationQuery : null, false, true,
				defaultTransactionIsolation, abandonedConfig);

		this.pool.setFactory(factory);
		PoolingDataSource dataSource;
		if (dataService.isEnableXA()) {
			/* for XA-transactions */
			dataSource = new ManagedDataSource(this.pool,
					((XAConnectionFactory) connectionFactory).getTransactionRegistry());
		} else {
			dataSource = new PoolingDataSource(this.pool);
		}
		dataSource.setAccessToUnderlyingConnectionAllowed(true);
		return dataSource;
	}

}
