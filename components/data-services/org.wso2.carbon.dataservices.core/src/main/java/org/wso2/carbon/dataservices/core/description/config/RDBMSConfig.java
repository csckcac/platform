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

import java.sql.SQLException;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.core.DBCPConnectionManager;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.xa.XADataSourceInfo;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * This class represents a RDBMS based data source configuration.
 */
public class RDBMSConfig extends DBCPSQLConfig {
	
	protected DBCPConnectionManager dbcpConnectionManager;
	
	private XADataSourceInfo xaDataSourceInfo;

	public RDBMSConfig(DataService dataService, String configId, Map<String, String> properties) 
			throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.RDBMS, properties);
		
		if (!DBUtils.isEmptyString(properties.get(DBConstants.RDBMS.XA_DATASOURCE_CLASS))) {
			this.xaDataSourceInfo = this.createXADataSourceInfo(properties);
		}
		
		if (!dataService.isServiceInactive()) {
			validateRDBMSConfig();
		    this.setDBCPConnectionManager(new DBCPConnectionManager(this));
		    try {
			    this.initSQLDataSource();
		    } catch (SQLException e) {
			    throw new DataServiceFault(e, DBConstants.FaultCodes.CONNECTION_UNAVAILABLE_ERROR,
			    		e.getMessage());
		    }
		}
	}
	
	public boolean hasXADS() {
		return this.getXADataSourceInfo() != null;
	}
	
	private XADataSourceInfo createXADataSourceInfo(Map<String, String> properties)
			throws DataServiceFault {
		String className = properties.get(DBConstants.RDBMS.XA_DATASOURCE_CLASS);
		if (DBUtils.isEmptyString(className)) {
			throw new DataServiceFault(
					"The class cannot be empty in XADataSource entry with props: \n" + properties);
		}
		String propsString = properties.get(DBConstants.RDBMS.XA_DATASOURCE_PROPS);
		OMElement xaPropsEl = null;
		try {
			xaPropsEl = AXIOMUtil.stringToOM(propsString);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in passing XA data source properties: " + propsString);
		}
		Map<String, String> xaProps = DBUtils.extractProperties(xaPropsEl);
		return new XADataSourceInfo(this.getDataService(), className, xaProps);
	}
	
	public XADataSourceInfo getXADataSourceInfo() {
		return xaDataSourceInfo;
	}
	
	private void validateRDBMSConfig () throws DataServiceFault  {
		String driverClass = this.getProperty(RDBMS.DRIVER);
		String jdbcURL = this.getProperty(RDBMS.PROTOCOL);	
		String xaDataSourceClass = this.getProperty(RDBMS.XA_DATASOURCE_CLASS);		
		if (DBUtils.isEmptyString(xaDataSourceClass)) {
			if (DBUtils.isEmptyString(driverClass)) {
				throw new DataServiceFault("Driver class cannot be null in config '" + 
						this.getConfigId() + "'");
			}
			if (DBUtils.isEmptyString(jdbcURL)) {
				throw new DataServiceFault("JDBC URL cannot be null in config '" + 
						this.getConfigId() + "'");
			}
		}
		this.validateDBCPSQLConfig();
	}
		
}
