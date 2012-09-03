/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.core.custom.datasource;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration.DataSourceProperty;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This class represents the data source reader implementation for 
 * data services custom data sources.
 */
public class CustomDataSourceReader implements DataSourceReader {

	public static String DATA_SOURCE_TYPE = "DS_CUSTOM";
	
	@Override
	public String getType() {
		return DATA_SOURCE_TYPE;
	}
	
	@Override
	public Object createDataSource(String xmlConfiguration, 
			boolean isDataSourceFactoryReference) throws DataSourceException {
		try {
			CustomDataSourceInfo dsInfo = this.loadConfig(xmlConfiguration);
			CustomDataSource customDS = (CustomDataSource) Class.forName(
					dsInfo.getCustomDataSourceClass()).newInstance();
			Map<String, String> dsProps = this.extractProps(dsInfo);
			this.populateStandardProps(dsProps);
			customDS.init(dsProps);
			return customDS;
		} catch (Exception e) {
			throw new DataSourceException("Error creating custom data source: " + 
					e.getMessage(), e);
		}
	}
	
	private void populateStandardProps(Map<String, String> dsProps) {
		String dsInfo = SuperTenantCarbonContext.getCurrentContext().getTenantId() + "###"
				 + DataSourceUtils.getCurrentDataSourceId();
		dsProps.put(CustomDataSource.DATASOURCE_ID, UUID.nameUUIDFromBytes(
				dsInfo.getBytes(Charset.forName(DBConstants.DEFAULT_CHAR_SET_TYPE))).toString());
	}
	
	private Map<String, String> extractProps(CustomDataSourceInfo dsInfo) {
		Map<String, String> props = new HashMap<String, String>();
		for (DataSourceProperty prop : dsInfo.getCustomDataSourceProps()) {
			props.put(prop.getName(), prop.getValue());
		}
		return props;
	}
	
	private CustomDataSourceInfo loadConfig(String xmlConfiguration) throws DataSourceException {
		try {
			xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
			JAXBContext ctx = JAXBContext.newInstance(CustomDataSourceInfo.class);
			return (CustomDataSourceInfo) ctx.createUnmarshaller().unmarshal(
					new ByteArrayInputStream(xmlConfiguration.getBytes()));
		} catch (Exception e) {
			throw new DataSourceException("Error in creating custom data source info: " +
					e.getMessage(), e);
		}
	}

	@Override
	public boolean testDataSourceConnection(String xmlConfiguration) throws DataSourceException {
		return false;
	}

	@XmlRootElement (name = "configuration")
	public static class CustomDataSourceInfo {
		
		private String customDataSourceClass;
		
		private List<DataSourceProperty> customDataSourceProps;

		public String getCustomDataSourceClass() {
			return customDataSourceClass;
		}

		public void setCustomDataSourceClass(String customDataSourceClass) {
			this.customDataSourceClass = customDataSourceClass;
		}

		@XmlElementWrapper (name = "customDataSourceProps")
		@XmlElement (name = "property")
		public List<DataSourceProperty> getCustomDataSourceProps() {
			return customDataSourceProps;
		}

		public void setCustomDataSourceProps(
				List<DataSourceProperty> customDataSourceProps) {
			this.customDataSourceProps = customDataSourceProps;
		}
		
	}
	
}
