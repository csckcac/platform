/*
 * Copyright  The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.logging.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.config.LoggingConfigManager;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.logging.session.LoggingSessionManager;
import org.wso2.carbon.logging.sort.LogEventSorter;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class CassandraLogReader {

	private static Log log = LogFactory.getLog(CassandraLogReader.class);
	private final static StringSerializer stringSerializer = StringSerializer.get();
	private static final String CASSANDRA_CLUSTER_CONNECTION = "cluster";
	private static final int MAX_NO_OF_EVENTS = 40000;
	private ExecutorService executorService = Executors.newFixedThreadPool(1);

	private int getCurrentTenantId(String tenantDomain) throws LogViewerException {
		int tenantId = getTenantIdForDomain(tenantDomain);
		if (isSuperTenantUser()) {// ST can view tenant specific log files.
			return tenantId;
		} else {
			return CarbonContext.getCurrentContext().getTenantId();
		}
	}

	private String getCurrentServerName(String serverName) {
		if (isManager() && serverName != null && serverName.length() > 0) {
			return serverName;
		} else {
			return ServerConfiguration.getInstance().getFirstProperty("Name");
		}

	}

	public boolean isLogEventAppenderConfigured() {
		LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
		return config.isCassandraServerAvailable();
	}

	private boolean isManager() {
		if (LoggingConstants.WSO2_STRATOS_MANAGER.equals(ServerConfiguration.getInstance()
				.getFirstProperty("Name"))) {
			return true;
		} else {
			return false;
		}
	}

	private Cluster retrieveCassandraCluster(String clusterName, String connectionUrl,
			Map<String, String> credentials) {

		CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
		hostConfigurator.setRetryDownedHosts(false);
		Cluster cluster = HFactory.createCluster(clusterName, hostConfigurator, credentials);
		LoggingSessionManager.setSessionObject(CASSANDRA_CLUSTER_CONNECTION, cluster);
		return cluster;
	}

	private Cluster getCluster(String clusterName, String connectionUrl,
			Map<String, String> credentials) {
		Cluster cluster = (Cluster) LoggingSessionManager.getSessionObject(CASSANDRA_CLUSTER_CONNECTION);
		if (cluster != null) {
			return cluster;
		} else {
			return retrieveCassandraCluster(clusterName, connectionUrl, credentials);
		}
	}

	private Keyspace getCurrentCassandraKeyspace() throws LogViewerException {
		LoggingConfig config;
		try {
			config = LoggingConfigManager.loadLoggingConfiguration();
		} catch (Exception e) {
			throw new LogViewerException("Cannot read the log config file", e);
		}
		String connectionUrl = config.getUrl();
		String userName = config.getUser();
		String password = config.getPassword();
		String keyspaceName = config.getKeyspace();
		String clusterName = config.getCluster();
		Cluster cluster;
		Map<String, String> credentials = new HashMap<String, String>();
		credentials.put(LoggingConstants.USERNAME_KEY, userName);
		credentials.put(LoggingConstants.PASSWORD_KEY, password);
		cluster = getCluster(clusterName, connectionUrl, credentials);
		return HFactory.createKeyspace(keyspaceName, cluster);
	}

	private boolean isSuperTenantUser() {
		CarbonContext carbonContext = CarbonContext.getCurrentContext();
		int tenantId = carbonContext.getTenantId();
		if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}

	private byte[] longToByteArray(long data) {
		return new byte[] { (byte) ((data >> 56) & 0xff), (byte) ((data >> 48) & 0xff),
				(byte) ((data >> 40) & 0xff), (byte) ((data >> 32) & 0xff),
				(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
				(byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff), };

	}

	private String convertByteToString(byte[] array) {
		return new String(array);
	}

	private String convertLongToString(Long longval) {
		Date date = new Date(longval);
		DateFormat formatter = new SimpleDateFormat(LoggingConstants.DATE_TIME_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		String formattedDate = formatter.format(date);
		return formattedDate;
	}

	private long convertByteToLong(byte[] array, int offset) {
		return ((long) (array[offset] & 0xff) << 56) | ((long) (array[offset + 1] & 0xff) << 48)
				| ((long) (array[offset + 2] & 0xff) << 40)
				| ((long) (array[offset + 3] & 0xff) << 32)
				| ((long) (array[offset + 4] & 0xff) << 24)
				| ((long) (array[offset + 5] & 0xff) << 16)
				| ((long) (array[offset + 6] & 0xff) << 8) | ((long) (array[offset + 7] & 0xff));

	}

	private List<LogEvent> getLoggingResultList(
			RangeSlicesQuery<String, String, byte[]> rangeSlicesQuery) {
		List<LogEvent> resultList = new ArrayList<LogEvent>();
		QueryResult<OrderedRows<String, String, byte[]>> result = rangeSlicesQuery.execute();
		for (Row<String, String, byte[]> row : result.get().getList()) {
			LogEvent event = new LogEvent();
			event.setKey(row.getKey());
			for (HColumn<String, byte[]> hc : row.getColumnSlice().getColumns()) {
				if (hc.getName().equals(LoggingConstants.HColumn.TENANT_ID)) {
					event.setTenantId(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.SERVER_NAME)) {
					event.setServerName(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.APP_NAME)) {
					event.setAppName(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.LOG_TIME)) {
					event.setLogTime(convertLongToString(convertByteToLong(hc.getValue(), 0)));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.LOGGER)) {
					event.setLogger(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.PRIORITY)) {
					event.setPriority(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.MESSAGE)) {
					event.setMessage(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.IP)) {
					event.setIp(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.STACKTRACE)) {
					event.setStacktrace(convertByteToString(hc.getValue()));
					continue;
				}
				if (hc.getName().equals(LoggingConstants.HColumn.INSTANCE)) {
					event.setIp(convertByteToString(hc.getValue()));
					continue;
				}

			}
			resultList.add(event);
		}
		return resultList;
	}

	private String getCurrentServerName() {
		String serverName = ServerConfiguration.getInstance().getFirstProperty("Name");
		serverName = serverName.replace("WSO2", "");
		return serverName.replace(" ", "_");
	}

	private String getCurrentDate() {
		Date cirrDate = new Date();
		DateFormat formatter = new SimpleDateFormat(LoggingConstants.DATE_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		String formattedDate = formatter.format(cirrDate);
		return formattedDate.replace("-", "_");
	}

	private String getCFName(LoggingConfig config) {
		int tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
		String currTenantId = "";
		if (tenantId == MultitenantConstants.INVALID_TENANT_ID
				|| tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			currTenantId = "0";
		} else {
			currTenantId = String.valueOf(tenantId);
		}
		String applicationName = getCurrentServerName();
		String currDateStr = getCurrentDate();
		String colFamily = config.getColFamily() + "_" + currTenantId + "_" + applicationName + "_"
				+ currDateStr;
		return colFamily;
	}

	private LogEvent[] getLogsForType(LogEvent[] events, String type) {
		List<LogEvent> resultList = new ArrayList<LogEvent>();
		for (LogEvent event : events) {
			if (event.getPriority().equals(type)) {
				resultList.add(event);
			}
		}
		return resultList.toArray(new LogEvent[resultList.size()]);
	}

	private LogEvent[] getLogsForKey(LogEvent[] events, String keyword) {
		List<LogEvent> resultList = new ArrayList<LogEvent>();
		for (LogEvent event : events) {
			boolean isInLogMessage = event.getMessage() != null
					&& (event.getMessage().toLowerCase().indexOf(keyword.toLowerCase()) > -1);
			boolean isInLogger = event.getLogger() != null
					&& (event.getLogger().toLowerCase().indexOf(keyword.toLowerCase()) > -1);
			if (isInLogger || isInLogMessage) {
				resultList.add(event);
			}
		}
		return resultList.toArray(new LogEvent[resultList.size()]);
	}

	private LogEvent[] getSortedLogsFromCassandra(String applicationName) throws LogViewerException {
		Future<LogEvent[]> task = this.getExecutorService().submit(
				new LogEventSorter(this.getSystemLogs(), ""));
		List<LogEvent> resultList = new ArrayList<LogEvent>();
		try {
			if (applicationName.equals("")) {
				return task.get();
			} else {
				LogEvent events[] = task.get();
				for (LogEvent e : events) {
					if (applicationName.equals(e.getAppName())) {
						resultList.add(e);
					}
				}
				return resultList.toArray(new LogEvent[resultList.size()]);
			}

		} catch (InterruptedException e) {
			log.error("Error occurred while retrieving the sorted log event list", e);
			throw new LogViewerException(
					"Error occurred while retrieving the sorted log event list");
		} catch (ExecutionException e) {
			log.error("Error occurred while retrieving the sorted log event list", e);
			throw new LogViewerException(
					"Error occurred while retrieving the sorted log event list");
		}

	}

	private LogEvent[] searchLog(LogEvent[] sortedLogs, String type, String keyword)
			throws LogViewerException {
		if ("ALL".equalsIgnoreCase(type)) {
			return getLogsForKey(sortedLogs, keyword);
		} else {
			LogEvent[] filerByType = getLogsForType(sortedLogs, type);
			List<LogEvent> resultList = new ArrayList<LogEvent>();
			if (filerByType != null) {
				for (int i = 0; i < filerByType.length; i++) {
					String logMessage = filerByType[i].getMessage();
					String logger = filerByType[i].getLogger();
					if (logMessage != null
							&& logMessage.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
						resultList.add(filerByType[i]);
					} else if (logger != null
							&& logger.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
						resultList.add(filerByType[i]);
					}
				}
			}
			if (resultList.isEmpty()) {
				return null;
			}
			return resultList.toArray(new LogEvent[resultList.size()]);
		}

	}

	public int getNoOfRows() throws LogViewerException {
		Keyspace currKeyspace = getCurrentCassandraKeyspace();
		LoggingConfig config;
		try {
			config = LoggingConfigManager.loadLoggingConfiguration();
		} catch (Exception e) {
			throw new LogViewerException("Cannot load cassandra configuration", e);
		}
		String colFamily = getCFName(config);

		RangeSlicesQuery<String, String, String> rangeSlicesQuery = HFactory
				.createRangeSlicesQuery(currKeyspace, stringSerializer, stringSerializer,
						stringSerializer);
		rangeSlicesQuery.setColumnFamily(colFamily);
		rangeSlicesQuery.setKeys("", "");
		rangeSlicesQuery.setRowCount(Integer.MAX_VALUE);
		rangeSlicesQuery.setReturnKeysOnly();
		QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
		return result.get().getCount();
	}

	public LogEvent[] getLogs(String type, String keyword) throws LogViewerException {
		LogEvent[] events = getSortedLogsFromCassandra("");
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return events;
			} else {
				// type is NOT null and NOT equal to ALL Application Name is not
				// needed
				return getLogsForType(events, type);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return getLogsForKey(events, keyword);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return searchLog(events, type, keyword);
			}
		}
	}

	public LogEvent[] getApplicationLogs(String type, String keyword, String appName)
			throws LogViewerException {
		LogEvent[] events = getSortedLogsFromCassandra(appName);
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return events;
			} else {
				// type is NOT null and NOT equal to ALL Application Name is not
				// needed
				return getLogsForType(events, type);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return getLogsForKey(events, keyword);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return searchLog(events, type, keyword);
			}
		}
	}

	private ExecutorService getExecutorService() {
		return executorService;
	}

	private LogEvent[] getSearchedAppLogsFromCassandra(String type, String keyword, String appName)
			throws LogViewerException {
		LogEvent sortedLogs[] = getSortedLogsFromCassandra(appName);
		if ("ALL".equalsIgnoreCase(type)) {
			return getLogsForKey(sortedLogs, keyword);
		} else {
			LogEvent[] filerByType = getLogsForType(sortedLogs, type);
			List<LogEvent> resultList = new ArrayList<LogEvent>();
			if (filerByType != null) {
				for (int i = 0; i < filerByType.length; i++) {
					String logMessage = filerByType[i].getMessage();
					String logger = filerByType[i].getLogger();
					if (logMessage != null
							&& logMessage.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
						resultList.add(filerByType[i]);
					} else if (logger != null
							&& logger.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
						resultList.add(filerByType[i]);
					}
				}
			}
			if (resultList.isEmpty()) {
				return null;
			}
			return resultList.toArray(new LogEvent[resultList.size()]);
		}

	}

	public LogEvent[] getSystemLogs() throws LogViewerException {

		// int tenantId = getCurrentTenantId(tenantDomain);
		// serviceName = getCurrentServerName(serviceName);
		Keyspace currKeyspace = getCurrentCassandraKeyspace();
		LoggingConfig config;
		try {
			config = LoggingConfigManager.loadLoggingConfiguration();
		} catch (Exception e) {
			throw new LogViewerException("Cannot load cassandra configuration", e);
		}
		String colFamily = getCFName(config);
		RangeSlicesQuery<String, String, byte[]> rangeSlicesQuery = HFactory
				.createRangeSlicesQuery(currKeyspace, stringSerializer, stringSerializer,
						BytesArraySerializer.get());
		rangeSlicesQuery.setColumnFamily(colFamily);
		rangeSlicesQuery.setRowCount(MAX_NO_OF_EVENTS);

		rangeSlicesQuery.setColumnNames(LoggingConstants.HColumn.TENANT_ID,
				LoggingConstants.HColumn.SERVER_NAME, LoggingConstants.HColumn.APP_NAME,
				LoggingConstants.HColumn.LOG_TIME, LoggingConstants.HColumn.LOGGER,
				LoggingConstants.HColumn.PRIORITY, LoggingConstants.HColumn.MESSAGE,
				LoggingConstants.HColumn.IP, LoggingConstants.HColumn.STACKTRACE,
				LoggingConstants.HColumn.INSTANCE);
		rangeSlicesQuery.setRange("", "", false, 30);

		List<LogEvent> resultList = getLoggingResultList(rangeSlicesQuery);
		return resultList.toArray(new LogEvent[resultList.size()]);
	}

	public String[] getApplicationNamesFromCassandra() throws LogViewerException {
		List<String> appList = new ArrayList<String>();
		LogEvent allLogs[];
		try {
			allLogs = getSystemLogs();
		} catch (LogViewerException e) {
			log.error("Error retrieving application logs", e);
			throw new LogViewerException("Error retrieving application logs", e);
		}
		for (LogEvent event : allLogs) {
			if (event.getAppName() != null && !event.getAppName().equals("")  && !event.getAppName().equals("NA")
					&& !LoggingUtil.isAdmingService(event.getAppName()) && !appList.contains(event.getAppName())) {
				appList.add(event.getAppName());
			}
		}
		return appList.toArray(new String[appList.size()]);
	}

	// convert date to the given fomat.
	public Date getDateForCurrFormat(String dateString) throws LogViewerException {
		// 2012-05-23 09:16:46,114
		DateFormat formatter;
		formatter = new SimpleDateFormat(LoggingConstants.DATE_TIME_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		dateString = (dateString.length() == 16) ? dateString + ":00,000" : dateString;
		dateString = (dateString.length() == 19) ? dateString + ",000" : dateString;
		Date date;
		try {
			date = (Date) formatter.parse(dateString);
		} catch (ParseException e) {
			log.error("Illegal Date Format", e);
			throw new LogViewerException("Illegal Date Format", e);
		}
		return date;

	}

	private LogEvent[] getSortedLogInfo(LogEvent logs[]) {
		int maxLen = logs.length;
		final SimpleDateFormat formatter = new SimpleDateFormat(
				LoggingConstants.DATE_TIME_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		if (maxLen > 0) {
			List<LogEvent> logInfoList = Arrays.asList(logs);
			Collections.sort(logInfoList, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					LogEvent log1 = (LogEvent) o1;
					LogEvent log2 = (LogEvent) o2;
					Date d1 = null, d2 = null;
					try {
						d1 = (Date) formatter.parse(log1.getLogTime());
						d2 = (Date) formatter.parse(log2.getLogTime());

					} catch (ParseException e1) {
						log.error(e1.getStackTrace());

					}
					return -d1.compareTo(d2);

				}

			});
			return (LogEvent[]) logInfoList.toArray(new LogEvent[logInfoList.size()]);
		}
		return null;
	}

	public int getTenantIdForDomain(String tenantDomain) throws LogViewerException {
		int tenantId;
		TenantManager tenantManager = LoggingServiceComponent.getTenantManager();
		if (tenantDomain == null || tenantDomain.equals("")) {
			tenantId = MultitenantConstants.SUPER_TENANT_ID;
		} else {

			try {
				tenantId = tenantManager.getTenantId(tenantDomain);
			} catch (UserStoreException e) {
				throw new LogViewerException("Cannot find tenant id for the given tenant domain.");
			}
		}
		return tenantId;
	}
}