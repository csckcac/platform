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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.config.CassandraConfigManager;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.CassandraConfig;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class CassandraLogReader {

	private static Log log = LogFactory.getLog(CassandraLogReader.class);
	private final static StringSerializer stringSerializer = StringSerializer.get();

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

	private boolean isManager() {
		if (LoggingConstants.WSO2_STRATOS_MANAGER.equals(ServerConfiguration.getInstance()
				.getFirstProperty("Name"))) {
			return true;
		} else {
			return false;
		}
	}

	public LogEvent getLogs(int tenantId, String serverName, String startdate, String enddate,
			String logger, String priority, String keyword, int index) {

		return null;
	}

	public Keyspace getCurrentCassandraKeyspace() throws LogViewerException {

		CassandraConfig config;
		try {
			config = LoggingUtil.getCassandraConfig();
		} catch (Exception e) {
			throw new LogViewerException("Cannot read the log config file", e);
		}
		String connectionURL = config.getUrl();
		String userName = config.getUser();
		String password = config.getPassword();
		String keyspaceName = config.getKeyspace();
		Cluster cluster;
		Map<String, String> credentials = new HashMap<String, String>();
		credentials.put(LoggingConstants.USERNAME_KEY, userName);
		credentials.put(LoggingConstants.PASSWORD_KEY, password);
		cluster = HFactory.createCluster(LoggingConstants.DEFUALT_CLUSTER_NAME,
				new CassandraHostConfigurator(connectionURL), credentials);

		return HFactory.createKeyspace(keyspaceName, cluster);
	}

	public boolean isSuperTenantUser() {
		CarbonContext carbonContext = CarbonContext.getCurrentContext();
		int tenantId = carbonContext.getTenantId();
		if (tenantId == LoggingConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isCassandraConfigured() {
		CassandraConfig config = CassandraConfigManager.loadCassandraConfiguration();
		return config.isCassandraServerAvailable();
	}

	public byte[] longToByteArray(long data) {
		return new byte[] { (byte) ((data >> 56) & 0xff), (byte) ((data >> 48) & 0xff),
				(byte) ((data >> 40) & 0xff), (byte) ((data >> 32) & 0xff),
				(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
				(byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff), };

	}

	public String convertByteToString(byte[] array) {
		return new String(array);
	}

	public String convertLongToString(Long longval) {
		Date date = new Date(longval);
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
		String formattedDate = formatter.format(date);
		return formattedDate;
	}

	public long convertByteToLong(byte[] array, int offset) {
		return ((long) (array[offset] & 0xff) << 56) | ((long) (array[offset + 1] & 0xff) << 48)
				| ((long) (array[offset + 2] & 0xff) << 40)
				| ((long) (array[offset + 3] & 0xff) << 32)
				| ((long) (array[offset + 4] & 0xff) << 24)
				| ((long) (array[offset + 5] & 0xff) << 16)
				| ((long) (array[offset + 6] & 0xff) << 8) | ((long) (array[offset + 7] & 0xff));

	}


	public LogEvent[] getSystemLogs(String start, String end, String logger, String priority,
			String keyword, String serviceName, String tenantDomain, int logIndex)
			throws LogViewerException {
		List<LogEvent> resultList = new ArrayList<LogEvent>();
		DateFormat formatter;
		Date startDate, endDate;
		formatter = new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");

		int tenantId = getCurrentTenantId(tenantDomain);
		serviceName = getCurrentServerName(serviceName);
		Keyspace currKeyspace = getCurrentCassandraKeyspace();
		CassandraConfig config;
		try {
			config = LoggingUtil.getCassandraConfig();
		} catch (Exception e) {
			throw new LogViewerException("Cannot read the log config file", e);
		}
		String colFamily = config.getColFamily();
		IndexedSlicesQuery<String, String, byte[]> indexedSlicesQuery = HFactory
				.createIndexedSlicesQuery(currKeyspace, stringSerializer, stringSerializer,
						BytesArraySerializer.get());
		indexedSlicesQuery.setColumnNames(LoggingConstants.HColumn.TENANT_ID,
				LoggingConstants.HColumn.SERVER_NAME, LoggingConstants.HColumn.APP_NAME,
				LoggingConstants.HColumn.LOG_TIME, LoggingConstants.HColumn.LOGGER,
				LoggingConstants.HColumn.PRIORITY, LoggingConstants.HColumn.MESSAGE,
				LoggingConstants.HColumn.IP, LoggingConstants.HColumn.STACKTRACE);

		indexedSlicesQuery.addEqualsExpression(LoggingConstants.HColumn.TENANT_ID,
				String.valueOf(tenantId).getBytes());
		indexedSlicesQuery.addEqualsExpression(LoggingConstants.HColumn.SERVER_NAME,
				serviceName.getBytes());
		if (!logger.equals("")) {
			indexedSlicesQuery.addEqualsExpression(LoggingConstants.HColumn.LOGGER,
					logger.getBytes());
		}
		if (!priority.equals("ALL")) {
			indexedSlicesQuery.addEqualsExpression(LoggingConstants.HColumn.PRIORITY,
					priority.getBytes());
		}
		try {

			if ( start.equals("now") || start.equals("")) {
				Calendar newTime = Calendar.getInstance();
				newTime.set(Calendar.HOUR_OF_DAY, newTime.getTime().getHours() - 1);
				indexedSlicesQuery.addGteExpression(LoggingConstants.HColumn.LOG_TIME,
						longToByteArray(newTime.getTime().getTime()));
			} else {
				startDate = (Date) formatter.parse(start);
				endDate = (Date) formatter.parse(end);
				indexedSlicesQuery.addGteExpression(LoggingConstants.HColumn.LOG_TIME,
						longToByteArray(endDate.getTime()));
				indexedSlicesQuery.addLtExpression(LoggingConstants.HColumn.LOG_TIME,
						longToByteArray(startDate.getTime()));
			}

		} catch (ParseException e1) {
			throw new LogViewerException("Illegal Date Format", e1);
		}

		indexedSlicesQuery.setColumnFamily(colFamily);
		indexedSlicesQuery.setStartKey("");
		// TODO limit the return rows
		indexedSlicesQuery.setRowCount(logIndex);

		QueryResult<OrderedRows<String, String, byte[]>> result = indexedSlicesQuery.execute();
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
			}
			resultList.add(event);
		}
		return getSortedLogInfo(resultList.toArray(new LogEvent[resultList.size()]));
	}

	private LogEvent[] getSortedLogInfo(LogEvent logs[]) {
		int maxLen = logs.length;
		final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
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
					return  -d1.compareTo(d2);

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
			tenantId = LoggingConstants.SUPER_TENANT_ID;
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
