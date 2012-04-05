package org.wso2.carbon.dataservices.core.description.config;

import java.util.Map;

import javax.sql.DataSource;

import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.core.DBCPConnectionManager;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * This class represents a DBCP based SQL data source configuration.
 */
public abstract class DBCPSQLConfig extends SQLConfig {

	protected DBCPConnectionManager dbcpConnectionManager;
	
	public DBCPSQLConfig(DataService dataService, String configId, String type,
			Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, type, properties);
	}

	protected void validateDBCPSQLConfig() throws DataServiceFault {
		String minPool = this.getProperty(RDBMS.MIN_POOL_SIZE);
		String maxPool = this.getProperty(RDBMS.MAX_POOL_SIZE);
		int minPoolSize = DBConstants.DEFAULT_DBCP_MIN_POOL_SIZE;
		int maxPoolSize = DBConstants.DEFAULT_DBCP_MAX_POOL_SIZE;
		try {
			if (!DBUtils.isEmptyString(minPool)) {
				minPoolSize = Integer.valueOf(minPool).intValue();
				if (minPoolSize < 0) {
					throw new DataServiceFault("Minimum pool size '" + minPoolSize +
							"' should be a positive value in config '" + this.getConfigId() + "'");
				}
			}
		} catch (NumberFormatException e) {
			throw new DataServiceFault(e, "Invalid minimum pool size '" + minPool + "' in config '" + 
					this.getConfigId() + "'");
		}
		try {
			if (!DBUtils.isEmptyString(maxPool)) {
				maxPoolSize = Integer.valueOf(maxPool).intValue();
			}
		} catch (NumberFormatException e) {
			throw new DataServiceFault(e, "Invalid maximum pool size '" + maxPool + "' in config '" + 
						this.getConfigId() + "'");
		}
	    if (!DBUtils.isEmptyString(minPool) && !DBUtils.isEmptyString(maxPool)) {
		    if (minPoolSize > maxPoolSize) {
		 		throw new DataServiceFault("Minimum pool size is greater than maximum pool size in config '" + 
						this.getConfigId() + "'");
		    }
	    }
	}
	
	@Override
	public boolean isStatsAvailable() {
		return true;
	}
	
	@Override
	public int getActiveConnectionCount() {
		return this.getDBCPConnectionManager().getObjectPool().getNumActive();
	}
	
	@Override
	public int getIdleConnectionCount() {
		return this.getDBCPConnectionManager().getObjectPool().getNumIdle();
	}
	
	protected void setDBCPConnectionManager(DBCPConnectionManager dbcpConnectionManager) {
		this.dbcpConnectionManager = dbcpConnectionManager;
	}
	
	protected DBCPConnectionManager getDBCPConnectionManager() {
		return dbcpConnectionManager;
	}
	
	public DataSource getDataSource() {
		DBCPConnectionManager manager = this.getDBCPConnectionManager();
		if (manager != null) {
			return manager.getDatasource();
		} else {
			return null;
		}
	}
	
	public void close() {
		DBCPConnectionManager cm = this.getDBCPConnectionManager();
		if (cm != null) {
		    cm.close();
		}
	}

}
