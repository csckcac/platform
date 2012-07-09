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

import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.appender.LogEventAppender;
import org.wso2.carbon.logging.config.ServiceConfigManager;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LogMessage;
import org.wso2.carbon.logging.service.data.PaginatedLogEvent;
import org.wso2.carbon.logging.service.data.PaginatedLogInfo;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.utils.DataPaginator;
import org.wso2.carbon.utils.logging.CarbonMemoryAppender;

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
	
	public PaginatedLogInfo getLocalLogFiles (int pageNumber) {
		LogInfo [] logs = LoggingUtil.getLocalLogInfo();
		if (logs != null) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
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

	public String[] getServiceNames() throws LogViewerException {
		return ServiceConfigManager.getServiceNames();
	}

	public boolean isManager() {
		return LoggingUtil.isManager();
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
	
	public String[] getApplicationNames()  {
		return LoggingUtil.getApplicationNames();
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
		DailyRollingFileAppender logger = (DailyRollingFileAppender) rootLogger.getAppender("CARBON_LOGFILE");
		if (logger != null && CarbonContext.getCurrentContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
			return true;
		} else {
			return false;
		}
	}
	
	public PaginatedLogEvent getPaginatedLogEvents(int pageNumber,String type, String keyword) {
		LogEvent list[] = getLogs(type, keyword);
		if (list != null) {
			List<LogEvent> logMsgList = Arrays.asList(list);
			PaginatedLogEvent paginatedLogEvent = new PaginatedLogEvent();
			DataPaginator.doPaging(pageNumber, logMsgList, paginatedLogEvent);
			return paginatedLogEvent;
		} else {
			return null;
		}	
	}
	
	public PaginatedLogEvent getPaginatedApplicationLogEvents(int pageNumber,String type, String keyword, String applicationName) throws Exception {
		LogEvent list[] =getApplicationLogs(type, keyword,applicationName);
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
				// type is NOT null and NOT equal to ALL Application Name is not needed
				return LoggingUtil.getLogsForType(type,"");
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword,"");
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword,"");
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
				return LoggingUtil.getLogsForType(type,appName);
			}
		} else {
			// keyword is NOT null
			if (type == null || type.equals("")) {
				// type is null
				return LoggingUtil.getLogsForKey(keyword,appName);
			} else {
				// type is NOT null and keyword is NOT null, but type can be
				// equal to ALL
				return LoggingUtil.searchLog(type, keyword,appName);
			}
		}
	}
  
    public boolean clearLogs() {
        Appender appender = Logger.getRootLogger().getAppender(LoggingConstants.WSO2CARBON_MEMORY_APPENDER);
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
