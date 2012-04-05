/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.core.description.query;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;

/**
 * This class is used to get SQL dialect for a given carbon datasource.
 */
public class SQLQueryDialectCarbonDSListener {
	
	private SQLQuery sqlQuery;
	
	private OMElement queryEl;
	
	private String carbonDSId;
	
	private static final Log log = LogFactory.getLog(SQLQueryDialectCarbonDSListener.class);

	public SQLQueryDialectCarbonDSListener(SQLQuery sqlQuery, OMElement queryEl, String carbonDSId) {
		this.sqlQuery = sqlQuery;
		this.queryEl = queryEl;
		this.carbonDSId= carbonDSId;
	}

	public void setCarbonDataSourceService(
			DataSourceInformationRepositoryService carbonDataSourceService) {
		try {
			if (carbonDataSourceService != null
					&& carbonDataSourceService.getDataSourceInformationRepository() != null
					&& carbonDataSourceService.getDataSourceInformationRepository()
							.getDataSourceInformation(getCarbonDSId()) != null) {
				String connectionURL = carbonDataSourceService.getDataSourceInformationRepository()
						.getDataSourceInformation(getCarbonDSId()).getUrl();
				String query = QueryFactory
						.getSQLQueryForConnectionURL(getQueryEl(), connectionURL);
				getSqlQuery().setQuery(query);
				getSqlQuery().init();
			}
		} catch (DataServiceFault e) {
			log.error("Error in getting carbon datasource connection URL.", e);
		}
	}

	public SQLQuery getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(SQLQuery sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public OMElement getQueryEl() {
		return queryEl;
	}

	public void setQueryEl(OMElement queryEl) {
		this.queryEl = queryEl;
	}

	public String getCarbonDSId() {
		return carbonDSId;
	}

	public void setCarbonDSId(String carbonDSId) {
		this.carbonDSId = carbonDSId;
	}
	
}
