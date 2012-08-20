/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.ndatasource.ui.config.DSXMLConfiguration;
import org.wso2.carbon.ndatasource.ui.config.RDBMSDSXMLConfiguration;
import org.wso2.carbon.ndatasource.ui.config.RDBMSDSXMLConfiguration.DataSourceProperty;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.xsd.JNDIConfig;
import org.wso2.carbon.ndatasource.ui.stub.core.xsd.JNDIConfig_EnvEntry;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;

public class NDataSourceHelper {

	private static final Log log = LogFactory.getLog(NDataSourceHelper.class);

	private static ResourceBundle bundle;
	
	public static WSDataSourceMetaInfo createWSDataSourceMetaInfo(HttpServletRequest request) {
		//UI still has no input for data Source type. Assigned RDBMS
		String datasourceType = "RDBMS";
		bundle = ResourceBundle.getBundle("org.wso2.carbon.ndatasource.ui.i18n.Resources",
				request.getLocale());
		String name = request.getParameter("dsName");
		if (name == null || "".equals(name)) {
			name = request.getParameter("name_hidden");
			if (name == null || "".equals(name)) {
				handleException(bundle.getString("ds.name.cannotfound.msg"));
			}
		}

		String description = request.getParameter("description");

		WSDataSourceMetaInfo dataSourceMetaInfo = new WSDataSourceMetaInfo();
		dataSourceMetaInfo.setName(name);
		dataSourceMetaInfo.setSystem(Boolean.parseBoolean(request.getParameter("isSystem")));
		if (description != null && !("".equals(description))) {
			dataSourceMetaInfo.setDescription(description);
		}
		
		if (request.getParameter("jndiname") != null && !request.getParameter("jndiname").equals("")) {
			dataSourceMetaInfo.setJndiConfig(createJNDIConfig(request));
		} else {
			if (request.getParameter("useDataSourceFactory") != null || 
					(request.getParameter("jndiProperties") != null && !request.getParameter("jndiProperties").equals(""))) {
				handleException(bundle.getString("jndi.name.cannotfound.msg"));
			}
		}

		DSXMLConfiguration dsXMLConfig = createDSXMLConfiguration(datasourceType, request);

		WSDataSourceMetaInfo_WSDataSourceDefinition dataSourceDefinition = createWSDataSourceDefinition(
				dsXMLConfig, datasourceType);

		dataSourceMetaInfo.setDefinition(dataSourceDefinition);
		return dataSourceMetaInfo;

	}

	private static DSXMLConfiguration createDSXMLConfiguration(String type,
			HttpServletRequest request) {
		if (type.equals(NDataSourceClientConstants.RDBMS_DTAASOURCE_TYPE)) {
			RDBMSDSXMLConfiguration rdbmsDSXMLConfig = null;
			try {
				rdbmsDSXMLConfig = new RDBMSDSXMLConfiguration();
			} catch (NDataSourceAdminDataSourceException e) {
				handleException(e.getMessage());
			}

			String dsProvider = request.getParameter("dsProviderType");
			if ("External Data Source".equals(dsProvider)) {
				String dsclassname = request.getParameter("dsclassname");
				if (dsclassname == null || "".equals(dsclassname)) {
					handleException(bundle.getString("ds.dsclassname.cannotfound.msg"));
				}

				// retrieve external data source properties
				String dsproviderProperties = request.getParameter("dsproviderProperties");
				if (dsproviderProperties == null || "".equals(dsproviderProperties)) {
					handleException(bundle.getString("ds.external.datasource.property.cannotfound.msg"));
				}
				String[] propsList = dsproviderProperties.split("::");
				String[] property = null;
				List<DataSourceProperty> dataSourceProps = new ArrayList<DataSourceProperty>();

				for (int i = 0; i < propsList.length; i++) {
					RDBMSDSXMLConfiguration.DataSourceProperty dataSourceProperty = new RDBMSDSXMLConfiguration.DataSourceProperty();
					property = propsList[i].split(",");
					dataSourceProperty.setName(property[0]);
					dataSourceProperty.setValue(property[1]);
					dataSourceProps.add(dataSourceProperty);
				}
				rdbmsDSXMLConfig.setDataSourceClassName(dsclassname);
				rdbmsDSXMLConfig.setDataSourceProps(dataSourceProps);
			} else if ("default".equals(dsProvider)) {
				String driver = request.getParameter("driver");
				if (driver == null || "".equals(driver)) {
					handleException(bundle.getString("ds.driver.cannotfound.msg"));
				}
				String url = request.getParameter("url");
				if (url == null || "".equals(url)) {
					handleException(bundle.getString("ds.url.cannotfound.msg"));
				}
				String username = request.getParameter("username");
				String password = request.getParameter("password");

				rdbmsDSXMLConfig.setUrl(url);
				rdbmsDSXMLConfig.setDriverClassName(driver);
				rdbmsDSXMLConfig.setUsername(username);
				RDBMSDSXMLConfiguration.Password passwordOb = new RDBMSDSXMLConfiguration.Password();
				passwordOb.setValue(password);
				rdbmsDSXMLConfig.setPassword(passwordOb);
			} else {
				throw new IllegalArgumentException("Unknown data source provider type");
			}
			// retrieve data source parameteres.
			setDatasourceProperties(rdbmsDSXMLConfig, request);
			return rdbmsDSXMLConfig;
		} else {
			throw new IllegalArgumentException("Provided Data Source type not supported");
		}
	}

	private static JNDIConfig createJNDIConfig(HttpServletRequest request) {
		String name = request.getParameter("jndiname");
		JNDIConfig jndiConfig = new JNDIConfig();
		jndiConfig.setName(name);
		//set isUseDataSourceFactory
		String useDataSourceFactory = request.getParameter("useDataSourceFactory");
		useDataSourceFactory = (useDataSourceFactory == null) ? "false" : "true";
		jndiConfig.setUseDataSourceFactory(Boolean.parseBoolean(useDataSourceFactory));
		
		// retrieve environment properties
		String jndiProperties = request.getParameter("jndiProperties");
		if (jndiProperties != null && !("".equals(jndiProperties))) {
			String[] propsList = jndiProperties.split("::");
			String[] property = null;
			JNDIConfig_EnvEntry[] jndiEnvyList = new JNDIConfig_EnvEntry[propsList.length];

			for (int i = 0; i < propsList.length; i++) {
				JNDIConfig_EnvEntry jndiEnvy = new JNDIConfig_EnvEntry();
				property = propsList[i].split(",");
				jndiEnvy.setName(property[0]);
				jndiEnvy.setValue(property[1]);
				jndiEnvyList[i] = jndiEnvy;
			}
			if (propsList.length > 0) {
				jndiConfig.setEnvironment(jndiEnvyList);
			}
		}
		return jndiConfig;
	}

	private static WSDataSourceMetaInfo_WSDataSourceDefinition createWSDataSourceDefinition(
			DSXMLConfiguration dsXMLConfig, String datasourceType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			dsXMLConfig.getDSMarshaller().marshal(dsXMLConfig, out);
		} catch (JAXBException e) {
			handleException("Error in marshelling Data Source Configuration info");
		}
		WSDataSourceMetaInfo_WSDataSourceDefinition wSDataSourceDefinition = new WSDataSourceMetaInfo_WSDataSourceDefinition();
		wSDataSourceDefinition.setDsXMLConfiguration(out.toString());
		wSDataSourceDefinition.setType(datasourceType);

		return wSDataSourceDefinition;
	}

	public static Map<String, String> getAllDataSources(WSDataSourceInfo[] allDataSourcesInfo) {
		Map<String, String> allDataSources = new HashMap<String, String>();
		if (allDataSourcesInfo != null) {
			for (WSDataSourceInfo dataSourceInfo : allDataSourcesInfo) {
				WSDataSourceMetaInfo dataSourceMetaInfo = dataSourceInfo.getDsMetaInfo();
				allDataSources.put(dataSourceMetaInfo.getName(), dataSourceInfo.getDsStatus().getMode());
			}
		}
		return allDataSources;
	}
	
	public static DSXMLConfiguration unMarshal(String datasourceType, String configuration) throws NDataSourceAdminDataSourceException {
		if (datasourceType.equals(NDataSourceClientConstants.RDBMS_DTAASOURCE_TYPE)) {
			JAXBContext ctx = null;
			RDBMSDSXMLConfiguration rdbmsConfiguration = null;
			try {
				ctx = JAXBContext.newInstance(RDBMSDSXMLConfiguration.class);
				rdbmsConfiguration = (RDBMSDSXMLConfiguration)ctx.createUnmarshaller().unmarshal((Element)(stringToElement(configuration)));
			} catch (JAXBException e) {
				throw new NDataSourceAdminDataSourceException ("Error creating rdbms data source configuration info unmarshaller: "+ e.getMessage(), e);
			}
			return rdbmsConfiguration;
		} else {
			throw new IllegalArgumentException("Provided Dta Source type not supported");
		}
	}
	
	public static Element stringToElement(String xml) {
		if (xml == null) {
			return null;
		}
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(false);
		    DocumentBuilder db = docFactory.newDocumentBuilder();
		    return db.parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
		} catch (Exception e) {
			log.error("Error while convering string to element: " + e.getMessage(), e);
			return null;
		}
	}
	
	private static void setDatasourceProperties(RDBMSDSXMLConfiguration rdbmsDSXMLConfig,
			HttpServletRequest request) {
		Boolean defaultAutoCommit = Boolean.parseBoolean(request.getParameter("defaultAutoCommit"));
		if (defaultAutoCommit != false) {
			rdbmsDSXMLConfig.setDefaultAutoCommit(defaultAutoCommit);
		}
		Boolean defaultReadOnly = Boolean.parseBoolean(request.getParameter("defaultReadOnly"));
		if (defaultReadOnly != false) {
			rdbmsDSXMLConfig.setDefaultReadOnly(defaultReadOnly);
		}
		String defaultTransactionIsolation = request.getParameter("defaultTransactionIsolation");
		if (defaultTransactionIsolation != null && !"NONE".equals(defaultTransactionIsolation)) {
			rdbmsDSXMLConfig.setDefaultTransactionIsolation(defaultTransactionIsolation.trim());
		}
		String defaultCatalog = request.getParameter("defaultCatalog");
		if (defaultCatalog != null && !"".equals(defaultCatalog)) {
			rdbmsDSXMLConfig.setDefaultCatalog(defaultCatalog.trim());
		}
		
		String maxActive = request.getParameter("maxActive");
		if (maxActive != null && !maxActive.contains("int") && !maxActive.equals("")){
			try {
				rdbmsDSXMLConfig.setMaxActive(Integer.parseInt(maxActive.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.maxActive"));
			}
		}
		String maxIdle = request.getParameter("maxIdle");
		if (maxIdle != null && !maxIdle.contains("int") && !maxIdle.equals("")) {
			try {
				rdbmsDSXMLConfig.setMaxIdle(Integer.parseInt(maxIdle.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.maxidle"));
			}
		}
		String minIdle = request.getParameter("minIdle");
		if (minIdle != null && !minIdle.contains("int") && !minIdle.equals("")) {
			try {
				rdbmsDSXMLConfig.setMinIdle(Integer.parseInt(minIdle.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.MinIdle"));
			}
		}
		String initialSize = request.getParameter("initialSize");
		if (initialSize != null && !initialSize.contains("int") && !initialSize.equals("")) {
			try {
				rdbmsDSXMLConfig.setInitialSize(Integer.parseInt(initialSize.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.Initialsize"));
			}
		}
		String maxWait = request.getParameter("maxWait");
		if (maxWait != null && !maxWait.contains("int") && !maxWait.equals("")) {
			try {
				rdbmsDSXMLConfig.setMaxWait(Integer.parseInt(maxWait.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.maxWait"));
			}
		}
		Boolean testOnBorrow = Boolean.parseBoolean(request.getParameter("testOnBorrow"));
		if (testOnBorrow != false) {
			rdbmsDSXMLConfig.setTestOnBorrow(testOnBorrow);
		}
		Boolean testOnReturn = Boolean.parseBoolean(request.getParameter("testOnReturn"));
		if (testOnReturn != false && !"".equals(testOnReturn)) {
			rdbmsDSXMLConfig.setTestOnReturn(testOnReturn);
		}
		Boolean testWhileIdle = Boolean.parseBoolean(request.getParameter("testWhileIdle"));
		if (testWhileIdle != false) {
			rdbmsDSXMLConfig.setTestWhileIdle(testWhileIdle);
		}
		String validationQuery = request.getParameter("validationquery");
		if (validationQuery != null && !"".equals(validationQuery)) {
			rdbmsDSXMLConfig.setValidationQuery(validationQuery.trim());
		}
		String validatorClassName = request.getParameter("validatorClassName");
		if (validatorClassName != null && !"".equals(validatorClassName)) {
			rdbmsDSXMLConfig.setValidatorClassName(validatorClassName.trim());
		}
		String timeBetweenEvictionRunsMillis = request.getParameter("timeBetweenEvictionRunsMillis");
		if (timeBetweenEvictionRunsMillis != null && !timeBetweenEvictionRunsMillis.contains("int") 
				&& !timeBetweenEvictionRunsMillis.equals("")) {
			try {
				rdbmsDSXMLConfig.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.timeBetweenEvictionRunsMillis"));
			}
		}
		String numTestsPerEvictionRun = request.getParameter("numTestsPerEvictionRun");
		if (numTestsPerEvictionRun != null && !numTestsPerEvictionRun.contains("int") 
				&& !numTestsPerEvictionRun.equals("")) {
			try {
				rdbmsDSXMLConfig.setNumTestsPerEvictionRun(Integer.parseInt(numTestsPerEvictionRun.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.numTestsPerEvictionRun"));
			}
		}
		String minEvictableIdleTimeMillis = request.getParameter("minEvictableIdleTimeMillis");
		if (minEvictableIdleTimeMillis != null && !minEvictableIdleTimeMillis.contains("int") 
				&& !minEvictableIdleTimeMillis.equals("")) {
			try {
				rdbmsDSXMLConfig.setMinEvictableIdleTimeMillis(Integer.parseInt(minEvictableIdleTimeMillis.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.minEvictableIdleTimeMillis"));
			}
		}
		Boolean accessToUnderlyingConnectionAllowed = Boolean.parseBoolean(request.getParameter("accessToUnderlyingConnectionAllowed"));
		if (accessToUnderlyingConnectionAllowed != false) {
			rdbmsDSXMLConfig.setAccessToUnderlyingConnectionAllowed(accessToUnderlyingConnectionAllowed);
		}
		Boolean removeAbandoned = Boolean.parseBoolean(request.getParameter("removeAbandoned"));
		if (removeAbandoned != false) {
			rdbmsDSXMLConfig.setRemoveAbandoned(removeAbandoned);
		}
		String removeAbandonedTimeout = request.getParameter("removeAbandonedTimeout");
		if (removeAbandonedTimeout != null && !removeAbandonedTimeout.contains("int") 
				&& !removeAbandonedTimeout.equals("")) {
			try {
				rdbmsDSXMLConfig.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimeout.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.removeAbandonedTimeout"));
			}
		}
		Boolean logAbandoned = Boolean.parseBoolean(request.getParameter("logAbandoned"));
		if (logAbandoned != false) {
			rdbmsDSXMLConfig.setLogAbandoned(logAbandoned);
		}
		String connectionProperties = request.getParameter("connectionProperties");
		if (connectionProperties != null && !"".equals(connectionProperties)) {
			rdbmsDSXMLConfig.setConnectionProperties(connectionProperties.trim());
		}
		String initSQL = request.getParameter("initSQL");
		if (initSQL != null && !"".equals(initSQL)) {
			rdbmsDSXMLConfig.setInitSQL(initSQL.trim());
		}
		String jdbcInterceptors = request.getParameter("jdbcInterceptors");
		if (jdbcInterceptors != null && !"".equals(jdbcInterceptors)) {
			rdbmsDSXMLConfig.setJdbcInterceptors(jdbcInterceptors.trim());
		}
		String validationInterval = request.getParameter("validationInterval");
		if (validationInterval != null && !validationInterval.contains("long") && !validationInterval.equals("")) {
			try {
				rdbmsDSXMLConfig.setValidationInterval(Long.parseLong(validationInterval.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.validationInterval"));
			}
		}
		Boolean jmxEnabled = Boolean.parseBoolean(request.getParameter("jmxEnabled"));
		if (jmxEnabled != false) {
			rdbmsDSXMLConfig.setJmxEnabled(jmxEnabled);
		}
		Boolean fairQueue = Boolean.parseBoolean(request.getParameter("fairQueue"));
		if (fairQueue != false) {
			rdbmsDSXMLConfig.setFairQueue(fairQueue);
		}
		String abandonWhenPercentageFull = request.getParameter("abandonWhenPercentageFull");
		if (abandonWhenPercentageFull != null && !abandonWhenPercentageFull.contains("int") 
				&& !abandonWhenPercentageFull.equals("")) {
			try {
				rdbmsDSXMLConfig.setAbandonWhenPercentageFull(Integer.parseInt(abandonWhenPercentageFull.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.abandonWhenPercentageFull"));
			}
		}
		Boolean useEquals = Boolean.parseBoolean(request.getParameter("useEquals"));
		if (useEquals != false) {
			rdbmsDSXMLConfig.setUseEquals(useEquals);
		}
		String maxAge = request.getParameter("maxAge");
		if (maxAge != null && !maxAge.contains("long") && !maxAge.equals("")) {
			try {
				rdbmsDSXMLConfig.setMaxAge(Long.parseLong(maxAge.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.maxAge"));
			}
		}
		String suspectTimeout = request.getParameter("suspectTimeout");
		if (suspectTimeout != null && !suspectTimeout.contains("int") && !suspectTimeout.equals("")) {
			try {
				rdbmsDSXMLConfig.setSuspectTimeout(Integer.parseInt(suspectTimeout.trim()));
			} catch (NumberFormatException e) {
				handleException(bundle.getString("invalid.suspectTimeout"));
			}
		}
		Boolean alternateUsernameAllowed = Boolean.parseBoolean(request.getParameter("alternateUsernameAllowed"));
		if (alternateUsernameAllowed != false) {
			rdbmsDSXMLConfig.setAlternateUsernameAllowed(alternateUsernameAllowed);
		}
	}
	
	private static void handleException(String msg) {
		log.error(msg);
		throw new IllegalArgumentException(msg);
	}
	
}
