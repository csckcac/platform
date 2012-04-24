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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.commons.datasource.DataSourceRepositoryManager;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents a Carbon Data Source based data source configuration.
 */
public class CarbonDataSourceConfig extends SQLConfig {

	private static final Log log = LogFactory.getLog(CarbonDataSourceConfig.class);
	
	private DataSource dataSource;
	
	private String dataSourceName;
		
	public CarbonDataSourceConfig(DataService dataService, String configId,
			Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CARBON, properties);
		this.dataSourceName = properties.get(DBConstants.CarbonDatasource.NAME);
        this.dataSource = initDataSource();
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String getDataSourceName() {
		return dataSourceName;
	}

	private DataSource initDataSource() {
        DataSourceInformationRepositoryService dataSourceService =
                DataServicesDSComponent.getCarbonDataSourceService();
        if (dataSourceService == null) {
            log.error("Carbon DataSource Service is not initialized properly");
            return null;
        }
		DataSourceInformationRepository datasourceRepo =
                dataSourceService.getDataSourceInformationRepository();
		return ((DataSourceRepositoryManager) (
				datasourceRepo.getRepositoryListener())).getDataSource(this.getDataSourceName());
	}
	
	public static List<String> getCarbonDataSourceNames() {
		DataSourceInformationRepositoryService dataSourceService = DataServicesDSComponent.
				getCarbonDataSourceService();
		if (dataSourceService == null) {
			log.error("CarbonDataSourceConfig.getCarbonDataSourceNames(): " +
                    "Carbon data source service is not available, returning empty list");
			return new ArrayList<String>();
		}
		List<String> namesList = new ArrayList<String>();
		Iterator<DataSourceInformation> dataItr = dataSourceService.
				getDataSourceInformationRepository().getAllDataSourceInformation();
		DataSourceInformation dataInfo = null;
		while (dataItr.hasNext()) {
			dataInfo = dataItr.next();
			namesList.add(dataInfo.getDatasourceName());
		}
		return namesList;
	}

	@Override
	public int getActiveConnectionCount() {
		return -1;
	}
	
	@Override
	public int getIdleConnectionCount() {
		return -1;
	}

	@Override
	public boolean isStatsAvailable() {
		return false;
	}

	public void close() {
		/* nothing to close */
	}
    
}

