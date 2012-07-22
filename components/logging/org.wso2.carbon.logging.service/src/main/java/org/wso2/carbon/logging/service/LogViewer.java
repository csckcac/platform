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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.appender.CarbonMemoryAppender;
import org.wso2.carbon.logging.appender.LogEventAppender;
import org.wso2.carbon.logging.config.ServiceConfigManager;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.PaginatedLogEvent;
import org.wso2.carbon.logging.service.data.PaginatedLogInfo;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.utils.DataPaginator;

/**
 * This is the Log Viewer service used for obtaining Log messages from locally
 * and from a remote configured syslog server.
 */
public class LogViewer {

	private static final Log log = LogFactory.getLog(LogViewer.class);

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

	public PaginatedLogInfo getLocalLogFiles(int pageNumber) throws LogViewerException {
		LogInfo[] logs = null;
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			logs = LoggingUtil.getRemoteLogFiles();
		} else if (isFileAppenderConfiguredForST()) {
			logs = LoggingUtil.getLocalLogInfo();

		}
		if (logs != null) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			PaginatedLogInfo paginatedLogInfo = new PaginatedLogInfo();
			DataPaginator.doPaging(pageNumber, logInfoList, paginatedLogInfo);
			return paginatedLogInfo;
		} else {
			return null;
		}
	}

	public DataHandler downloadArchivedLogFiles(String logFile) throws Exception {
		return LoggingUtil.downloadArchivedLogFiles(logFile);
	}

	// public boolean isValidTenantDomain(String tenantDomain) {
	// return LoggingUtil.isValidTenantDomain(tenantDomain);
	// }
	//
	public String[] getServiceNames() throws LogViewerException {
		return ServiceConfigManager.getServiceNames();
	}

	//
	// public boolean isManager() {
	// return LoggingUtil.isManager();
	// }

	public int getLineNumbers(String logFile) throws Exception {
		return LoggingUtil.getLineNumbers(logFile);
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end)
			throws LogViewerException {
		return LoggingUtil.getLogLinesFromFile(logFile, maxLogs, start, end);
	}

	public String[] getApplicationNames() throws LogViewerException {
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			return LoggingUtil.getApplicationNamesFromCassandra();
		} else {
			return LoggingUtil.getApplicationNames();
		}
	}

	public boolean isLogEventReciverConfigured() {
		Logger rootLogger = Logger.getRootLogger();
		LogEventAppender logger = (LogEventAppender) rootLogger.getAppender("LOGEVENT");
		if (logger != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isFileAppenderConfiguredForST() {
		Logger rootLogger = Logger.getRootLogger();
		DailyRollingFileAppender logger = (DailyRollingFileAppender) rootLogger
				.getAppender("CARBON_LOGFILE");
		if (logger != null
				&& CarbonContext.getCurrentContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}

	public PaginatedLogEvent getPaginatedLogEvents(int pageNumber, String type, String keyword)
			throws LogViewerException {

		LogEvent list[];
		if (!LoggingUtil.isLogEventAppenderConfigured()) {
			list = getLogs(type, keyword);
		} else {
			list = LoggingUtil.getSortedLogsFromCassandra(type, keyword);
		}
		if (list != null) {
			List<LogEvent> logMsgList = Arrays.asList(list);
			PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
			DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
			return paginatedLogEvent;
		} else {
			return null;
		}

	}

	public int getNoOfLogEvents() throws LogViewerException {
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			return LoggingUtil.getNoOfRows();
		} else {
			return -1;
		}
	}

	public PaginatedLogEvent getPaginatedApplicationLogEvents(int pageNumber, String type,
			String keyword, String applicationName) throws Exception {
		LogEvent list[];
		if (LoggingUtil.isLogEventAppenderConfigured()) {
			list = LoggingUtil.getSortedAppLogsFromCassandra(type, keyword, applicationName);
		} else {
			list = getApplicationLogs(type, keyword, applicationName);
		}
		if (list != null) {
			List<LogEvent> logMsgList = Arrays.asList(list);
			PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
			DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
			return paginatedLogEvent;
		} else {
			return null;
		}
	}

	public LogEvent[] getLogs(String type, String keyword) {

		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return LoggingUtil.getLogs("");
			} else {
				// type is NOT null and NOT equal to ALL Application Name is not
				// needed
				return LoggingUtil.getLogsForType(type, "");
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword, "");
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword, "");
			}
		}
	}

	public LogEvent[] getApplicationLogs(String type, String keyword, String appName) {
		if (keyword == null || keyword.equals("")) {
			// keyword is null
			if (type == null || type.equals("") || type.equalsIgnoreCase("ALL")) {
				return LoggingUtil.getLogs(appName);
			} else {
				// type is NOT null and NOT equal to ALL
				return LoggingUtil.getLogsForType(type, appName);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword, appName);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword, appName);
			}
		}
	}

	public boolean clearLogs() {
		Appender appender = Logger.getRootLogger().getAppender(
				LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
		if (appender instanceof CarbonMemoryAppender) {
			try {
				CarbonMemoryAppender memoryAppender = (CarbonMemoryAppender) appender;
				if (memoryAppender.getCircularQueue() != null) {
					memoryAppender.getCircularQueue().clear();
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
}