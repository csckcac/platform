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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.*;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * A factory class to create config objects to the given data.
 */
public class ConfigFactory {

	private ConfigFactory() { }
	
	public static Config createConfig(DataService dataService, OMElement configEl) 
			throws DataServiceFault {
		Map<String, String> properties = DBUtils.extractProperties(configEl);
		String configId = getConfigId(configEl);
		String configType = getConfigType(properties);
		
		if (DataSourceTypes.RDBMS.equals(configType)) {
			return getRDBMSConfig(dataService, configId, properties);
		} else if (DataSourceTypes.JNDI.equals(configType)) {
			return getJNDIConfig(dataService, configId, properties);
		} else if (DataSourceTypes.EXCEL.equals(configType)) {
			return getExcelConfig(dataService, configId, properties);
		} else if (DataSourceTypes.RDF.equals(configType)) {
			return getRDFConfig(dataService, configId, properties);
		} else if (DataSourceTypes.SPARQL.equals(configType)) {
            return getSparqlEndpointConfig(dataService, configId, properties);
        } else if (DataSourceTypes.CSV.equals(configType)) {
			return getCSVConfig(dataService, configId, properties);
		} else if (DataSourceTypes.GDATA_SPREADSHEET.equals(configType)) {
			return getGSpreadConfig(dataService, configId, properties);
		} else if (DataSourceTypes.CARBON.equals(configType)) {
			return getCarbonDataSourceConfig(dataService, configId, properties);
		} else if (DataSourceTypes.WEB.equals(configType)) {
            return getWebConfig(dataService, configId, properties);
        }
		
		return null;
	}
	
	private static RDBMSConfig getRDBMSConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		RDBMSConfig config = new RDBMSConfig(dataService, configId, properties);
		return config;
	}
	
	private static JNDIConfig getJNDIConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		JNDIConfig config = new JNDIConfig(dataService, configId, properties);
		return config;
	}
	
	private static ExcelConfig getExcelConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		ExcelConfig config = new ExcelConfig(dataService, configId, properties);
		return config;
	}
	
	//added new method to get RDFConfig
	private static RDFConfig getRDFConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		RDFConfig config = new RDFConfig(dataService, configId, properties);
		return config;
	}

    private static SparqlEndpointConfig getSparqlEndpointConfig(DataService dataService, String configId,
			Map<String, String> properties) throws DataServiceFault {
		SparqlEndpointConfig config = new SparqlEndpointConfig(dataService, configId, properties);
		return config;
	}
	
	private static CSVConfig getCSVConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		CSVConfig config = new CSVConfig(dataService, configId, properties);
		return config;
	}

    private static WebConfig getWebConfig(DataService dataService, String configId,
             Map<String, String> properties) throws DataServiceFault {
        WebConfig config = new WebConfig(dataService, configId, properties);
        return config;
    }

	private static GSpreadConfig getGSpreadConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		GSpreadConfig config = new GSpreadConfig(dataService, configId, properties);
		return config;
	}
	
	private static CarbonDataSourceConfig getCarbonDataSourceConfig(DataService dataService, 
			String configId, Map<String, String> properties) throws DataServiceFault {
		CarbonDataSourceConfig config = new CarbonDataSourceConfig(dataService, configId, 
				properties);
		return config;
	}
	
	private static String getConfigId(OMElement configEl) {
		String configId = configEl.getAttributeValue(new QName(DBSFields.ID));
		if (configId == null) {
			configId = DBConstants.DEFAULT_CONFIG_ID;
		}
		return configId;
	}
		
	private static String getConfigType(Map<String, String> properties) throws DataServiceFault {
		if ((properties.get(RDBMS.DRIVER) != null && 
				properties.get(RDBMS.PROTOCOL) != null) || 
				properties.get(RDBMS.XA_DATASOURCE_CLASS) != null) {
		    return DataSourceTypes.RDBMS;
		} else if (properties.get(Excel.DATASOURCE) != null) {
		    return DataSourceTypes.EXCEL;
		} else if  (properties.get(RDF.DATASOURCE) != null) {
		    return DataSourceTypes.RDF;
		} else if (properties.get(SPARQL.DATASOURCE) != null) {
            return DataSourceTypes.SPARQL;
        } else if (properties.get(CSV.DATASOURCE) != null) {
		    return DataSourceTypes.CSV;
		} else if (properties.get(DBConstants.JNDI.RESOURCE_NAME) != null) {
		    return DataSourceTypes.JNDI;
		} else if (properties.get(GSpread.DATASOURCE) != null) {
		    return DataSourceTypes.GDATA_SPREADSHEET;
		} else if (properties.get(DBConstants.CarbonDatasource.NAME) != null) {
		    return DataSourceTypes.CARBON;
		} else if (properties.get(DBConstants.WebDatasource.WEB_CONFIG) != null) {
            return DataSourceTypes.WEB;
        }
		throw new DataServiceFault("Cannot create config with properties: " + properties);
	}
	
}
