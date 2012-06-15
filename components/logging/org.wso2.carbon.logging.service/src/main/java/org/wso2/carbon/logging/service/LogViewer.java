/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.logging.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.logging.appender.CassandraAppender;
import org.wso2.carbon.logging.appenders.MemoryAppender;
import org.wso2.carbon.logging.config.ServiceConfigManager;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LogMessage;
import org.wso2.carbon.logging.service.data.PaginatedLogInfo;
import org.wso2.carbon.logging.service.data.PaginatedLogMessage;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.utils.DataPaginator;

/**
 * This is the Log Viewer service used for obtaining Log messages from locally
 * and from a remote configured syslog server.
 */
public class LogViewer {

	private static final LogMessage[] NO_LOGS_MESSAGE = new LogMessage[] { new LogMessage(
			"NO_LOGS", "INFO") };

	public PaginatedLogInfo getPaginatedLogInfo(int pageNumber, String tenantDomain,
			String serviceName) throws Exception {
		LogInfo[] logs = LoggingUtil.getLogsIndex(tenantDomain, serviceName);
		if (logs != null) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			// Pagination
			PaginatedLogInfo paginatedLogInfo = new PaginatedLogInfo();
			DataPaginator.doPaging(pageNumber, logInfoList, paginatedLogInfo);
			return paginatedLogInfo;
		} else {
			return null;
		}
		
		
	}

	public DataHandler downloadLogFiles(String logFile, String tenantDomain, String serviceName)
			throws Exception {
		return LoggingUtil.downloadLogFiles(logFile, tenantDomain, serviceName);
	}

	public boolean isValidTenantDomain(String tenantDomain) {
		return LoggingUtil.isValidTenantDomain(tenantDomain);
	}

	public boolean isSTSyslogConfig(String tenantDomain) throws Exception {
		return LoggingUtil.isSTSyslogConfig(tenantDomain);
	}

	public LogMessage[] getLogs(String type, String keyword) throws AxisFault {

		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return getLogsMessages();
			} else {
				// type is NOT null and NOT equal to ALL
				return getLogsForType(type);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return getLogsForKey(keyword);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return searchLog(type, keyword);
			}
		}
	}

	public boolean isLogsConfigured(String tenantDomain) throws Exception {
		return LoggingUtil.isLogsConfigured(tenantDomain);
	}

	public boolean isDataFromSysLog(String tenantDomain) throws Exception {
		return LoggingUtil.isSysLogAppender(tenantDomain);
	}

	public String[] getServiceNames() throws LogViewerException {
		return ServiceConfigManager.getServiceNames();
	}

	public boolean isStratosService() throws Exception {
		return LoggingUtil.isStratosService();
	}

	public boolean isManager() {
		return LoggingUtil.isManager();
	}

	public PaginatedLogMessage getPaginatedLogMessage(int pageNumber, String type, String keyword,
			String logFile, String logIndex, int maxLines, int start, int end, String tenantDomain,
			String serviceName) throws Exception {
		int headLogs = Integer.parseInt(logIndex);
		if (headLogs > maxLines) {
			headLogs = maxLines;
		}
		List<LogMessage> logMsgList = Arrays.asList(LoggingUtil.getTenantLogs(type, keyword,
				logFile, logIndex, headLogs, start, end, tenantDomain, serviceName));
		PaginatedLogMessage paginatedLogMessage = new PaginatedLogMessage();
		LoggingUtil.doPaging(pageNumber, logMsgList, headLogs, paginatedLogMessage);
		return paginatedLogMessage;
	}

	public PaginatedLogMessage getPaginatedBottomUpLogMessage(int pageNumber, String type,
			String keyword, String logFile, int maxLines, int start, int end, String tenantDomain,
			String serviceName) throws Exception {
		List<LogMessage> logMsgList = Arrays.asList(LoggingUtil.getBottomUpTenantLogs(type,
				keyword, logFile, maxLines, start, end, tenantDomain, serviceName));
		PaginatedLogMessage paginatedLogMessage = new PaginatedLogMessage();
		LoggingUtil.doPaging(pageNumber, logMsgList, maxLines, paginatedLogMessage);
		return paginatedLogMessage;
	}

	public int getLineNumbers(String logFile, String tenantDomain, String serviceName)
			throws Exception {
		return LoggingUtil.getLineNumbers(logFile, tenantDomain, serviceName);
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end,
			String tenantDomain, String serviceName) throws LogViewerException {
		return LoggingUtil.getLogLinesFromFile(logFile, maxLogs, start, end, tenantDomain,
				serviceName);
	}

	private LogMessage[] getLogsForKey(String keyword) {
		int DEFAULT_NO_OF_LOGS = 100;
		int definedAmount;
		Appender appender = Logger.getRootLogger().getAppender(
				LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
		if (appender instanceof MemoryAppender) {
			MemoryAppender memoryAppender = (MemoryAppender) appender;
			if ((memoryAppender.getCircularQueue() != null)) {
				definedAmount = memoryAppender.getBufferSize();
			} else {
				return NO_LOGS_MESSAGE;
			}
			Object[] objects;
			if (definedAmount < 1) {
				objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
			} else {
				objects = memoryAppender.getCircularQueue().getObjects(definedAmount);
			}
			if ((memoryAppender.getCircularQueue().getObjects(definedAmount) == null)
					|| (memoryAppender.getCircularQueue().getObjects(definedAmount).length == 0)) {
				return NO_LOGS_MESSAGE;
			}
			Layout layout = memoryAppender.getLayout();
			List<LogMessage> resultList = new ArrayList<LogMessage>();
			for (int i = 0; i < objects.length; i++) {
				LoggingEvent logEvt = (LoggingEvent) objects[i];
				if (logEvt != null) {
					String result = layout.format(logEvt);
					if (result != null && result.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
						resultList.add(new LogMessage(result, logEvt.getLevel().toString()));
					}
				}
			}
			if (resultList.isEmpty()) {
				return NO_LOGS_MESSAGE;
			}
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return new LogMessage[] { new LogMessage(
					"The log must be configured to use the "
							+ "org.wso2.carbon.logging.appenders.MemoryAppender to view entries on the admin console",
					"") };
		}
	}

	private LogMessage[] getLogsForType(String type) {
		int DEFAULT_NO_OF_LOGS = 100;
		int definedAmount;
		Appender appender = Logger.getRootLogger().getAppender(
				LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
		if (appender instanceof MemoryAppender) {
			MemoryAppender memoryAppender = (MemoryAppender) appender;
			if ((memoryAppender.getCircularQueue() != null)) {
				definedAmount = memoryAppender.getBufferSize();
			} else {
				return NO_LOGS_MESSAGE;
			}

			Object[] objects;
			if (definedAmount < 1) {
				objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
			} else {
				objects = memoryAppender.getCircularQueue().getObjects(definedAmount);
			}
			if ((memoryAppender.getCircularQueue().getObjects(definedAmount) == null)
					|| (memoryAppender.getCircularQueue().getObjects(definedAmount).length == 0)) {
				return NO_LOGS_MESSAGE;
			}
			Layout layout = memoryAppender.getLayout();
			List<LogMessage> resultList = new ArrayList<LogMessage>();
			for (int i = 0; i < objects.length; i++) {
				LoggingEvent logEvt = (LoggingEvent) objects[i];
				if (logEvt != null) {
					Level level = logEvt.getLevel();
					if (level.toString().equals(type)) {
						resultList.add(new LogMessage(layout.format(logEvt), level.toString()));
					}
				}
			}
			if (resultList.isEmpty()) {
				return NO_LOGS_MESSAGE;
			}
			return resultList.toArray(new LogMessage[resultList.size()]);
		} else {
			return new LogMessage[] { new LogMessage(
					"The log must be configured to use the "
							+ "org.wso2.carbon.logging.appenders.MemoryAppender to view entries through the admin console",
					"") };
		}
	}

	private LogMessage[] searchLog(String type, String keyword) throws AxisFault {
		if ("ALL".equalsIgnoreCase(type)) {
			return getLogsForKey(keyword);

		} else {
			LogMessage[] filerByType = getLogsForType(type);
			List<LogMessage> resultList = new ArrayList<LogMessage>();
			for (int i = 0; i < filerByType.length; i++) {
				String logMessage = filerByType[i].getLogMessage();
				if (logMessage != null
						&& logMessage.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
					resultList.add(filerByType[i]);
				}
			}
			if (resultList.isEmpty()) {
				return NO_LOGS_MESSAGE;
			}
			return resultList.toArray(new LogMessage[resultList.size()]);
		}
	}

	private LogMessage[] getLogsMessages() throws AxisFault {
		int DEFAULT_NO_OF_LOGS = 100;
		int definedamount;
		Appender appender = Logger.getRootLogger().getAppender(
				LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
		if (appender instanceof MemoryAppender) {
			MemoryAppender memoryAppender = (MemoryAppender) appender;
			if ((memoryAppender.getCircularQueue() != null)) {
				definedamount = memoryAppender.getBufferSize();
			} else {
				return NO_LOGS_MESSAGE;
			}
			Object[] objects;
			if (definedamount < 1) {
				objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
			} else {
				objects = memoryAppender.getCircularQueue().getObjects(definedamount);
			}
			if ((memoryAppender.getCircularQueue().getObjects(definedamount) == null)
					|| (memoryAppender.getCircularQueue().getObjects(definedamount).length == 0)) {
				return NO_LOGS_MESSAGE;
			}
			LogMessage[] logMessages = new LogMessage[objects.length];

			Layout layout = memoryAppender.getLayout();

			for (int i = 0; i < objects.length; i++) {
				LoggingEvent logEvt = (LoggingEvent) objects[i];
				if (logEvt != null) {
					Level level = logEvt.getLevel();
					logMessages[i] = new LogMessage(layout.format(logEvt), level.toString());
				}
			}
			return logMessages;
		} else {
			return new LogMessage[] { new LogMessage(
					"The log must be configured to use the org.wso2.carbon."
							+ "logging.appenders.MemoryAppender to view entries on the admin console",
					"") };
		}
	}

	public LogEvent[] getApplicationLogs(String appName, String start, String end, String logger,
			String priority, String keyword, int logIndex) throws LogViewerException {
		return LoggingUtil.getApplicationLogs(appName, start, end, logger, priority, keyword,
				logIndex);
	}

	public String[] getTenantApplicationNames() throws LogViewerException {
		return LoggingUtil.getTenantApplicationNames();
	}

	public LogEvent[] getSystemLogs(String start, String end, String logger, String priority,
			String keyword, String serviceName, String tenantDomain, int logIndex)
			throws LogViewerException {
		LogEvent[] events = LoggingUtil.getSystemLogs(start, end, logger, priority, keyword,
				serviceName, tenantDomain, logIndex);
		return events;
	}

	public boolean isCassandraConfigured() {
		Logger rootLogger = Logger.getRootLogger();
		CassandraAppender logger = (CassandraAppender) rootLogger.getAppender("CASSANDRA");
		if (logger != null) {
			return true;
		} else {
			return false;
		}
	}
}
