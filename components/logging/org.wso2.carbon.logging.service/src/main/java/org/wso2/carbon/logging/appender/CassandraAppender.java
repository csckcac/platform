/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.logging.appender;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import me.prettyprint.cassandra.serializers.DateSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.logging.TenantAwarePatternLayout;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

public class CassandraAppender extends AppenderSkeleton implements Appender {

	private Cluster cluster;
	private String user;
	private String password;
	private String url;
	private String keyspace;
	private String colFamily;
	private String columnList;

	private static final StringSerializer stringSerializer = StringSerializer.get();
	private static final BlockingQueue<TenantAwareLoggingEvent> loggingEventQueue = new LinkedBlockingDeque<TenantAwareLoggingEvent>();

	public CassandraAppender() {
		startLogger();
	}

	@Override
	protected void append(LoggingEvent event) {
		Logger logger = Logger.getLogger(event.getLoggerName());
		TenantAwareLoggingEvent tenantEvent;
		if (event.getThrowableInformation() != null) {
			tenantEvent = new TenantAwareLoggingEvent(event.fqnOfCategoryClass, logger,
					event.timeStamp, event.getLevel(), event.getMessage(), event
							.getThrowableInformation().getThrowable());
		} else {
			tenantEvent = new TenantAwareLoggingEvent(event.fqnOfCategoryClass, logger,
					event.timeStamp, event.getLevel(), event.getMessage(), null);
		}
		if (CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId() > -1) {

			tenantEvent.setTenantId(Integer.toString(CarbonContextHolder
					.getCurrentCarbonContextHolder().getTenantId()));
		}
		loggingEventQueue.add(tenantEvent);
	}

	private Cluster getConnection() {
		if (cluster == null) {
			cluster = newConnection();
		}
		return cluster;
	}

	private Cluster newConnection() {
		if (cluster == null) {
			Map<String, String> credentials = new HashMap<String, String>();
			if (user != null) {
				credentials.put("username", user);
			}
			if (password != null) {
				credentials.put("password", password);
			}
			cluster = HFactory.createCluster("Cluster", new CassandraHostConfigurator(url),
					credentials);
		}
		return cluster;
	}

	private void insertLogEvent(TenantAwareLoggingEvent event) throws ParseException {
		Keyspace ks = HFactory.createKeyspace(keyspace, getConnection());
		Mutator<String> mutator = HFactory.createMutator(ks, stringSerializer);
		List<String> patterns = Arrays.asList(columnList.split(","));
		String tenantID = null;
		String serverName = null;
		String appName = null;
		String logTime = null;
		String logger = null;
		String priority = null;
		String message = null;
		String stacktrace = null;
		String ip = null;
		String instance=null;
		for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
			String currEle = ((String) i.next()).replace("%", "");
			TenantAwarePatternLayout patternLayout = new TenantAwarePatternLayout("%" + currEle);
			if (currEle.equals("T")) {
				tenantID = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("S")) {
				serverName = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("A")) {
				appName = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("d")) {
				//String timeSplit[] = patternLayout.format(event).split(",");
				//logTime = timeSplit[0];// 2012-01-26 15:14:52
				logTime = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("c")) {
				logger = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("p")) {
				priority = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("m")) {
				message = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("I")) {
				instance = patternLayout.format(event);
				continue;
			}
			if (currEle.equals("Stacktrace")) {
				if (event.getThrowableInformation() != null) {
					stacktrace = getStacktrace(event.getThrowableInformation().getThrowable());
				} else {
					stacktrace = "";
				}
				continue;
			}
			if (currEle.equals("H")) {
				ip = patternLayout.format(event);
				continue;
			}
		}
		if (tenantID != null && serverName != null && logTime != null) {
			String key = tenantID + "_" + serverName + "_" + logTime + "_"
					+ UUID.randomUUID().toString();
			Serializer<String> stringSerializer = StringSerializer.get();
			Serializer<Date> dateSerializer = DateSerializer.get();
			Serializer<Long> longSerializer = LongSerializer.get();
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = (Date) formatter.parse(logTime);
			
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.TENANT_ID, tenantID, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily, HFactory.createColumn("serverName", serverName,
					stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.APP_NAME, appName, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.LOG_TIME, date.getTime(), stringSerializer, longSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.LOGGER, logger, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.PRIORITY, priority, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.MESSAGE, message, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily, HFactory.createColumn(LoggingConstants.HColumn.STACKTRACE, stacktrace,
					stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.IP, ip, stringSerializer, stringSerializer));
			mutator.insert(key, colFamily,
					HFactory.createColumn(LoggingConstants.HColumn.INSTANCE, instance, stringSerializer, stringSerializer));

			String currDate[] = logTime.split(" ");
			String keycf = tenantID + "_" + serverName.trim() + "_" + currDate[0];
			mutator.insert(keycf, "TimeCF",HFactory.createColumn(LoggingConstants.HColumn.LOG_TIME, date.getTime(), stringSerializer, longSerializer));
		}
	}

	private String getStacktrace(Throwable e) {
		StringBuilder stackTrace = new StringBuilder();
		StackTraceElement[] stackTraceElements = e.getStackTrace();
		for (StackTraceElement ele : stackTraceElements) {
			stackTrace.append(ele.toString()).append("\n");
		}
		return stackTrace.toString();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setColumnList(String columnList) {
		this.columnList = columnList;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public void setColFamily(String colFamily) {
		this.colFamily = colFamily;
	}

	public void finalize() {
		close();
		super.finalize();
	}

	private void processLoggingEventQueue() {
		while (true) {
			try {
				TenantAwareLoggingEvent event = loggingEventQueue.poll(1L, TimeUnit.SECONDS);
				if (event != null) {
					insertLogEvent(event);
				}
			} catch (InterruptedException e) {
				LogLog.error(e.toString());
			} catch (ParseException e) {
				LogLog.error(e.toString());
			}
		}
	}

	private void startLogger() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				processLoggingEventQueue();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void close() {

	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public String getColFamily() {
		return colFamily;
	}

	public String getColumnList() {
		return columnList;
	}
	
	
}
