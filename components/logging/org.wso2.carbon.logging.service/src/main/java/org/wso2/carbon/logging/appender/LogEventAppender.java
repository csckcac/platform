/*
 * Copyright The Apache Software Foundation.
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
package org.wso2.carbon.logging.appender;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.logging.service.LoggingAdminException;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.logging.TenantAwarePatternLayout;
import org.wso2.carbon.utils.multitenancy.CarbonApplicationContextHolder;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

public class LogEventAppender extends AppenderSkeleton implements Appender {
	private String url;
	private String password;
	private String userName;
	private String columnList;
	private DataPublisher dataPublisher;

	private Agent agent;
	private static final Log log = LogFactory.getLog(LogEventAppender.class);

	public LogEventAppender() {
		startLogger();
	}

	private static final BlockingQueue<TenantAwareLoggingEvent> loggingEventQueue = new LinkedBlockingDeque<TenantAwareLoggingEvent>();

	public void init() {
		if (agent == null) {
			// creating the agent
			// TODO authentication is not finalized need to provide a mechanism
			// to server to server authentication
			AgentConfiguration agentConfiguration = new AgentConfiguration();
			String truststorePath = CarbonUtils.getCarbonHome()
					+ "/repository/resources/security/client-truststore.jks";
			System.setProperty("javax.net.ssl.trustStore", truststorePath);
			System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
			agent = new Agent(agentConfiguration);

		}
	}

	private String getCurrentServerName() {
		String serverName = ServerConfiguration.getInstance().getFirstProperty("Name");
		serverName = serverName.replace("WSO2", "");
		serverName = serverName.trim();
		return serverName.replace(" ", ".");
	}

	private String getCurrentDate() {
		Date cirrDate = new Date();
		DateFormat formatter = new SimpleDateFormat(LoggingConstants.DATE_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		String formattedDate = formatter.format(cirrDate);
		return formattedDate.replace("-", ".");
	}

	private void insertLogEvent(TenantAwareLoggingEvent event) throws LoggingAdminException {

		String streamId = "";
		String tenantId = event.getTenantId();

		if (tenantId.equals(String.valueOf(MultitenantConstants.INVALID_TENANT_ID))
				|| tenantId.equals(String.valueOf(MultitenantConstants.SUPER_TENANT_ID))) {
			tenantId = "0";
		}
		String applicationName = getCurrentServerName();
		String currDateStr = getCurrentDate();
		init();
		try {
			dataPublisher = new DataPublisher(url, userName, password, agent);
			streamId = dataPublisher.defineStream("{" + "'name':'log" + "." + tenantId + "."
					+ applicationName + "." + currDateStr + "'," + "  'version':'1.0.0',"
					+ "  'nickName': 'Logs'," + "  'description': 'Logging Event',"
					+ "  'metaData':[" + "          {'name':'clientType','type':'STRING'}" + "  ],"
					+ "  'payloadData':[" + "          {'name':'tenantID','type':'STRING'},"
					+ "          {'name':'serverName','type':'STRING'},"
					+ "          {'name':'appName','type':'STRING'},"
					+ "          {'name':'logTime','type':'LONG'},"
					+ "          {'name':'priority','type':'STRING'},"
					+ "          {'name':'message','type':'STRING'},"
					+ "          {'name':'logger','type':'STRING'},"
					+ "          {'name':'ip','type':'STRING'},"
					+ "          {'name':'instance','type':'STRING'},"
					+ "          {'name':'stacktrace','type':'STRING'}" + "  ]" + "}");

		} catch (Exception e) {
			log.error("Error connecting to bam event data publisher", e);

		}
		List<String> patterns = Arrays.asList(columnList.split(","));
		String tenantID = "";
		String serverName = "";
		String appName = "";
		String logTime = "";
		String logger = "";
		String priority = "";
		String message = "";
		String stacktrace = "";
		String ip = "";
		String instance = "";
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
				if (appName == null || appName.equals("")) {
					appName = "NA";
				}
				continue;
			}
			if (currEle.equals("d")) {

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
					stacktrace = "NA";
				}
				continue;
			}
			if (currEle.equals("H")) {
				ip = patternLayout.format(event);
				continue;
			}
		}
		Date date = null;
		DateFormat formatter;
		formatter = new SimpleDateFormat(LoggingConstants.DATE_TIME_FORMATTER);
		formatter.setTimeZone(TimeZone.getTimeZone(LoggingConstants.GMT));
		try {
			date = (Date) formatter.parse(logTime);
		} catch (ParseException e) {
			log.error("parsing error in date from String");
		}

		if (tenantID != null && serverName != null && logTime != null) {
			// String key = tenantID + "_" + serverName + "_" + logTime + "_"
			// + UUID.randomUUID().toString();
			if (!streamId.isEmpty()) {
				Event logEvent = new Event(streamId, System.currentTimeMillis(),
						new Object[] { "external" }, null, new Object[] { tenantID, serverName,
								appName, date.getTime(), priority, message, logger, ip, instance,
								stacktrace });
				try {
					dataPublisher.publish(logEvent);
				} catch (Exception e) {
					log.error("Error publishing the logging event", e);
					throw new LoggingAdminException("Error publishing the logging event ", e);
				}

			}
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

	private void processLoggingEventQueue() {
		while (true) {
			try {
				TenantAwareLoggingEvent event = loggingEventQueue.poll(1L, TimeUnit.SECONDS);
				if (event != null) {
					insertLogEvent(event);
				}
			} catch (InterruptedException e) {
				LogLog.error(e.toString());
			} catch (LoggingAdminException e) {
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

	public void close() {

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

		tenantEvent.setTenantId(Integer.toString(CarbonContextHolder
				.getCurrentCarbonContextHolder().getTenantId()));
		if (CarbonApplicationContextHolder.getThreadLocalCarbonApplicationContextHolder()
				.getApplicationName() != null) {
			tenantEvent.setServiceName(CarbonApplicationContextHolder
					.getThreadLocalCarbonApplicationContextHolder().getApplicationName());
		} else {
			tenantEvent.setServiceName("");
			;
		}
		loggingEventQueue.add(tenantEvent);
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getColumnList() {
		return columnList;
	}

	public void setColumnList(String columnList) {
		this.columnList = columnList;
	}

	public static BlockingQueue<TenantAwareLoggingEvent> getLoggingeventqueue() {
		return loggingEventQueue;
	}

}