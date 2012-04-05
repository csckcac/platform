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
package org.wso2.carbon.dataservices.core.admin;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBDeployer;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.config.CarbonDataSourceConfig;
import org.wso2.carbon.dataservices.core.description.config.GSpreadConfig;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.DataServiceSerializer;
import org.wso2.carbon.dataservices.core.script.DSGenerator;
import org.wso2.carbon.dataservices.core.script.PaginatedTableInfo;
import org.wso2.carbon.dataservices.core.sqlparser.analysers.LexicalAnalyser;
import org.wso2.carbon.dataservices.core.sqlparser.analysers.SyntaxAnalyser;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.Pageable;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Data Services admin service class, for the basic functions.
 */
public class DataServiceAdmin extends AbstractAdmin {

	private static final Log log = LogFactory.getLog(DataServiceAdmin.class);

	public DataServiceAdmin() {
	}

	/**
	 * Returns data service content as a String.
	 * 
	 * @param serviceId
	 *            The data service name
	 * @return The data service configuration data
	 * @throws AxisFault
	 */
	public String getDataServiceContentAsString(String serviceId) throws AxisFault {
		AxisService axisService = getAxisConfig().getServiceForActivation(serviceId);
		StringBuffer fileContents = new StringBuffer();
		String filePath;
		// construct data service configuration file path
		if (axisService != null) {
			filePath = ((DataService) axisService.getParameter(DBConstants.DATA_SERVICE_OBJECT)
					.getValue()).getDsLocation();
		} else {
			// Service could be a fault one. Loading contents directly from
			// repository
			URL repositoryURL = getAxisConfig().getRepository();
			filePath = repositoryURL.getPath() + DBDeployer.DEPLOYMENT_FOLDER_NAME + File.separator
					+ serviceId + "." + DBConstants.DBS_FILE_EXTENSION;
		}

		// load file content into a string buffer
		if (filePath != null) {
			File config = new File(filePath);
			try {
				FileReader fileReader = new FileReader(config);
				BufferedReader in = new BufferedReader(fileReader);
				String str;
				while ((str = in.readLine()) != null) {
					fileContents.append(str + "\n");
				}
				in.close();
			} catch (IOException e) {
				throw new AxisFault(
						"Error while reading the contents from the service config file for service '"
								+ serviceId + "'", e);
			}
		}
		return fileContents.toString();
	}

	private void setServiceFileHashInRegistry(String serviceGroupName, String hash)
			throws Exception {
		Registry registry = this.getConfigSystemRegistry();
		if (registry == null) {
			throw new Exception("WSO2 Registry is not available");
		}
		boolean transactionStarted = Transaction.isStarted();
		try {
			if (transactionStarted) {
				registry.beginTransaction();
			}
			String serviceResourcePath = RegistryResources.SERVICE_GROUPS + serviceGroupName;
			/* the resource should already exist */
			if (registry.resourceExists(serviceResourcePath)) {
				Resource serviceResource = registry.get(serviceResourcePath);
				serviceResource.setProperty(RegistryResources.ServiceGroupProperties.HASH_VALUE,
						hash);
				registry.put(serviceResourcePath, serviceResource);
				serviceResource.discard();
			}
			if (transactionStarted) {
				registry.commitTransaction();
			}
		} catch (Throwable e) {
			if (transactionStarted) {
				registry.rollbackTransaction();
			}
			log.error("Unable to set the hash value property to service group "
					+ serviceGroupName);
			throw new Exception(e);
		}
	}

	protected String getDataServiceFileExtension() {
		ConfigurationContext configCtx = this.getConfigContext();
		String fileExtension = (String) configCtx.getProperty(DBConstants.DB_SERVICE_EXTENSION);
		return fileExtension;
	}

	/**
	 * Saves the data service in service repository. 
	 * @param serviceName The name of the data service to be saved
	 * @param serviceHierarchy The hierarchical path of the service
	 * @param serviceContents The content of the service
	 * @throws AxisFault
	 */
	public void saveDataService(String serviceName, String serviceHierarchy, 
			String serviceContents) throws AxisFault {
		String dataServiceFilePath;
		ConfigurationContext configCtx = this.getConfigContext();
		AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
		AxisService axisService = axisConfig.getServiceForActivation(serviceName);
		AxisServiceGroup axisServiceGroup;
		boolean hotUpdateOrFaulty = false;
		
		if (serviceHierarchy == null) {
			serviceHierarchy = "";
		}

		if (axisService == null) {
			/* new service */
			String axis2RepoDirectory = axisConfig.getRepository().getPath();
			String repoDirectory = (String) configCtx.getProperty(DBConstants.DB_SERVICE_REPO);
			String fileExtension = this.getDataServiceFileExtension();

			String dataServiceDirectory = axis2RepoDirectory + File.separator + repoDirectory
					+ File.separator + serviceHierarchy;
			dataServiceFilePath = dataServiceDirectory + File.separator + serviceName + "."
					+ fileExtension;

			/* create the directory, if it does not exist */
			File directory = new File(dataServiceDirectory);
			if (!directory.exists() && !directory.mkdirs()) {
				throw new AxisFault("Cannot create directory: " + directory.getAbsolutePath());
			}

			/* check if this is a faulty service */
			if (CarbonUtils.getFaultyService(serviceName, configCtx) != null) {
				hotUpdateOrFaulty = true;
			}
		} else {
			dataServiceFilePath = ((DataService) axisService.getParameter(
					DBConstants.DATA_SERVICE_OBJECT).getValue()).getDsLocation();
			axisServiceGroup = axisService.getAxisServiceGroup();
			axisServiceGroup.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
			axisServiceGroup.addParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true");
			axisService.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
			axisService.addParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true");
			hotUpdateOrFaulty = true;
		}

		serviceContents = DBUtils.prettifyXML(serviceContents);
		/*
		 * If this is hot-update or a faulty service that is getting deployed
		 * again, the hash is recalculated and set in the registry, so it will
		 * think the service did not change, thus the service settings will not
		 * be cleared. The hash is computed by our own here, also considering
		 * faulty services, i.e. cannot get an AxisServiceGroup for a faulty
		 * service.
		 */
		if (hotUpdateOrFaulty) {
			try {
				this.setServiceFileHashInRegistry(serviceName,
						this.getMD5HashOfString(serviceContents));
			} catch (Exception e) {
				log.error("Error while saving " + serviceName, e);
				throw new AxisFault(
						"Error occurred while saving the last access time in the registry for the existing service "
								+ serviceName, e);
			}
		}

		/* save contents to .dbs file */
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dataServiceFilePath));
			out.write(serviceContents);
			out.close();
		} catch (IOException e) {
			log.error("Error while saving " + serviceName, e);
			throw new AxisFault(
					"Error occurred while writing the contents for the service config file for the new service "
							+ serviceName, e);
		}
	}

	private String getMD5HashOfString(String contents) {
		return CarbonUtils.getMD5(contents.getBytes());
	}

	/**
	 * This will test a connection to a given database. If connection can be
	 * made this method will return the status as String, if not, faliour String
	 * will be return.
	 * 
	 * @param driverClass
	 *            Driver class
	 * @param jdbcURL
	 *            JDBC URL
	 * @param username
	 *            User name
	 * @param password
	 *            Pass word
	 * @return String; state
	 */
	public String testJDBCConnection(String driverClass, String jdbcURL, String username,
			String password,String protectedTokens,String passwordProvider) {
		int tenantId =
                SuperTenantCarbonContext.getCurrentContext(this.getConfigContext()).getTenantId();
		Connection connection = null;
		try {
			SuperTenantCarbonContext.startTenantFlow();
            CarbonContextHolder.getCurrentCarbonContextHolder().setTenantId(tenantId);

			String resolvePwd = "";
			if (driverClass == null || driverClass.length() == 0) {
				String message = "Driver class is missing";
				log.debug(message);
				return message;
			}
			if (jdbcURL == null || jdbcURL.length() == 0) {
				String message = "Driver connection URL is missing";
				log.debug(message);
				return message;
			}
			if (protectedTokens.equals("null")) {
				protectedTokens = "";
			}
			if (passwordProvider.equals("null")) {
				passwordProvider = "";
			}
			if (!(DBUtils.isEmptyString(protectedTokens) && DBUtils
					.isEmptyString(passwordProvider))) {
				OMFactory fac = OMAbstractFactory.getOMFactory();
				OMElement dataEl = fac.createOMElement("data", null);
				OMElement passwordManagerEl = fac.createOMElement(
						"passwordManager", null);
				OMElement protectedTokensEl = fac.createOMElement(
						"protectedTokens", null);
				protectedTokensEl.setText(protectedTokens);
				OMElement passwordProviderEl = fac.createOMElement(
						"passwordProvider", null);
				passwordProviderEl.setText(passwordProvider);
				passwordManagerEl.addChild(protectedTokensEl);
				passwordManagerEl.addChild(passwordProviderEl);
				dataEl.addChild(passwordManagerEl);
				SecretResolver secretResolver = SecretResolverFactory.create(
						dataEl, false);
				if (secretResolver != null
						&& secretResolver.isTokenProtected(password)) {
					resolvePwd = secretResolver.resolve(password);
				} else {
					resolvePwd = password;
				}
			} else {
				resolvePwd = password;
			}

			Class.forName(driverClass.trim());
			connection = DriverManager.getConnection(jdbcURL, username, resolvePwd);
			String message = "Database connection is successfull with driver class " + driverClass
					+ " , jdbc url " + jdbcURL + " and user name " + username;
			log.debug(message);
			return message;
		} catch (SQLException e) {
			String message = "Could not connect to database " + jdbcURL + " with username "
					+ username;
			log.error(message, e);
			return message;
		} catch (ClassNotFoundException e) {
			String message = "Driver class " + driverClass + " can not be loaded";
			log.error(message, e);
			return message;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
			SuperTenantCarbonContext.endTenantFlow();
		}
	}


	/**
	 * This will test the connection(retrieve CallFeed) of a Google spreadsheet
	 * document. If connection can be made this method will return the status as
	 * String, if not, failure String will be return.
	 * 
	 * @param user
	 *            - user name
	 * @param password
	 *            - password
	 * @param visibility
	 *            - Whether its private or public
	 * @param documentURL
	 *            - Google spreadsheet URL
	 * @return string State
	 */
	public String testGSpreadConnection(String user, String password, String visibility,
			String documentURL, String protectedTokens, String passwordProvider) {
		String resolvePwd;
		if (DBUtils.isEmptyString(documentURL)) {
			String message = "Document URL is empty";
			log.debug(message);
			return message;
		}
		String key;
		SpreadsheetService service = new SpreadsheetService("GSpread Connection Service");
		try {
			key = GSpreadConfig.extractKey(documentURL);
		} catch (DataServiceFault e) {
			String message = "Invalid documentURL:" + documentURL;
			log.warn(message);
			return message;
		}
		if (protectedTokens == null) {
			protectedTokens = "";
		}
		if (passwordProvider == null) {
			passwordProvider = "";
		}
		if (!(DBUtils.isEmptyString(protectedTokens) && DBUtils.isEmptyString(passwordProvider))) {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMElement dataEl = fac.createOMElement("data", null);
			OMElement passwordManagerEl = fac.createOMElement("passwordManager", null);
    		OMElement protectedTokensEl = fac.createOMElement("protectedTokens", null);
    		protectedTokensEl.setText(protectedTokens);
    		OMElement passwordProviderEl = fac.createOMElement("passwordProvider", null);
    		passwordProviderEl.setText(passwordProvider);
    		passwordManagerEl.addChild(protectedTokensEl);
    		passwordManagerEl.addChild(passwordProviderEl);		
            dataEl.addChild(passwordManagerEl);
            SecretResolver secretResolver = SecretResolverFactory.create(dataEl, false);
            if (secretResolver.isInitialized() && secretResolver.isTokenProtected(password)) {
                resolvePwd = secretResolver.resolve(password);
            } else {
                resolvePwd = password;
            }
		} else {
			resolvePwd = password;
		}
		if (!visibility.equals("public")) {
			if (DBUtils.isEmptyString(user)) {
				String message = "User name field is empty";
				log.error(message);
				return message;
			}
			if (DBUtils.isEmptyString(resolvePwd)) {
				String message = "Password field is empty";
				log.error(message);
				return message;
			}
			try {
				service.setUserCredentials(user, resolvePwd);
			} catch (AuthenticationException e) {
				String message = "Invalid User Credentials";
				log.error(message,e);
				return message;
			}
		}
		String worksheetFeedURL = GSpreadConfig.BASE_WORKSHEET_URL + key + "/" + visibility
				+ "/basic";
	    try {
			URL url = new URL(worksheetFeedURL);
			try {
				service.getFeed(url,  CellFeed.class);
				String message = "Google spreadsheet connection is successfull ";
				log.debug(message);
		return message;
			} catch (IOException e) {
				String message = "URL Not found:" + documentURL;
				log.error(message,e);
				return message;
			} catch (ServiceException e) {
				String message = "URL Not found:" + documentURL;
				log.error(message,e);
				return message;
			}
		} catch (MalformedURLException e) {
			String message = "Invalid documentURL:" + documentURL;
			log.error(message,e);
			return message;
		}

	}

	/**
	 * Return data services
	 * 
	 * @return names of the data services
	 * @throws AxisFault
	 *             AxisFault
	 */
	public String[] getAvailableDS() throws AxisFault {
		List<String> serviceList = new ArrayList<String>();
		Map<String, AxisService> map = getAxisConfig().getServices();
		Set<String> set = map.keySet();
		for (String serviceName : set) {
			AxisService axisService = getAxisConfig().getService(serviceName);
			Parameter parameter = axisService.getParameter(DBConstants.AXIS2_SERVICE_TYPE);
			if (parameter != null) {
				if (DBConstants.DB_SERVICE_TYPE.equals(parameter.getValue().toString())) {
					serviceList.add(serviceName);
				}
			}
		}
		return serviceList.toArray(new String[serviceList.size()]);
	}

	public String[] getCarbonDataSourceNames() {
		List<String> list = CarbonDataSourceConfig.getCarbonDataSourceNames();
		return list.toArray(new String[list.size()]);
	}

	public static String[] getColumnNames(String query) throws Exception {
		try {
			Queue<String> tokenQueue = new LexicalAnalyser(query).getTokens();
			if (tokenQueue.size() > 0) {
				SyntaxAnalyser sa = new SyntaxAnalyser(tokenQueue);
				List<String> columns = sa.processSelectStatement();
				return columns.toArray(new String[columns.size()]);
			} else {
				return new String[0];
			}
		} catch (Exception e) {
			throw new Exception("Error generating the response for the query " + query + ".", e);
		}
	}

	
	public String[] getdbSchemaList(String datasourceId) throws Exception {
		return DSGenerator.getSchemas(datasourceId);
	}

    public PaginatedTableInfo getPaginatedSchemaInfo(int pageNumber, String datasourceId)
            throws Exception {
        List<String> temp = new ArrayList<String>();
        Collections.addAll(temp, getdbSchemaList(datasourceId));
        // Pagination
        PaginatedTableInfo paginatedTableInfo = new PaginatedTableInfo();
        doPaging(pageNumber, temp, paginatedTableInfo);
        return paginatedTableInfo;
    }

	public String[] getTableList(String datasourceId, String dbName, String[] schemas) throws AxisFault {
		try {
		    return DSGenerator.getTableList(datasourceId, dbName, schemas);
		} catch (Exception e) {
			throw new AxisFault("Error in retrieving table list: " + e.getMessage(), e);
		}
	}

    public PaginatedTableInfo getPaginatedTableInfo(int pageNumber, String datasourceId,
                                                    String dbName, String[] schemas) throws Exception {
        List<String> tableInfoList = Arrays.asList(getTableList(datasourceId, dbName, schemas));

        // Pagination
        PaginatedTableInfo paginatedTableInfo = new PaginatedTableInfo();
        doPaging(pageNumber, tableInfoList, paginatedTableInfo);
        return paginatedTableInfo;
    }

    

	/** 
	 * Return the generated services name list 
	 */
	public String[] getDSServiceList(String dataSourceId, String dbName, String[] schemas,
			String[] tableNames, boolean singleService,String serviceNamespace) throws AxisFault  {
		DSGenerator generator = new DSGenerator(dataSourceId, dbName, schemas, tableNames, false,serviceNamespace,"");
		List<String> serviceNames = new ArrayList<String>();
		List<DataService> dsList = generator.getGeneratedServiceList();
		for (DataService ds : dsList) {
			OMElement element = DataServiceSerializer.serializeDataService(ds);
			this.saveDataService(ds.getName(), null, element.toString());
			serviceNames.add(ds.getName());
		}
		return serviceNames.toArray(new String[serviceNames.size()]);
	}

	/** 
	 * Return the generated service name 
	 */
	public String getDSService(String dataSourceId, String dbName, String[] schemas,
			String[] tableNames, boolean singleService,String serviceName,String serviceNamespace) throws AxisFault {
		DSGenerator generator = new DSGenerator(dataSourceId, dbName, schemas, tableNames, true,serviceNamespace,serviceName);
		DataService dataservice = generator.getGeneratedService();
		OMElement element = DataServiceSerializer.serializeDataService(dataservice);
		this.saveDataService(dataservice.getName(),	null, element.toString());
		return generator.getGeneratedService().getName();
	}

     /**
     * A reusable generic method for doing item paging
     *
     * @param pageNumber The page required. Page number starts with 0.
     * @param sourceList The original list of items
     * @param pageable          The type of Pageable item
     * @return Returned page
     */
    private static <C> List<C> doPaging(int pageNumber, List<C> sourceList, Pageable pageable) {
        if (pageNumber < 0 || pageNumber == Integer.MAX_VALUE) {
            pageNumber = 0;
        }
        if (sourceList.size() == 0) {
            return sourceList;
        }
        if (pageNumber < 0){
            throw new RuntimeException("Page number should be a positive integer. " +
                                       "Page numbers begin at 0.");
        }
        int itemsPerPageInt = 60; // the default number of item per page
        int numberOfPages = (int) Math.ceil((double) sourceList.size() / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        List<C> returnList = new ArrayList<C>();
        for (int i = startIndex; i < endIndex && i < sourceList.size(); i++) {
            returnList.add(sourceList.get(i));
        }
        pageable.setNumberOfPages(numberOfPages);
        pageable.set(returnList);
        return returnList;
    }
    
}
