/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.logging.util;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;
import org.wso2.carbon.logging.registry.RegistryManager;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.CassandraConfig;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LogMessage;
import org.wso2.carbon.logging.service.data.SyslogData;
import org.wso2.carbon.logging.appenders.CircularBuffer;
import org.wso2.carbon.logging.appenders.MemoryAppender;
import org.wso2.carbon.utils.Pageable;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.springframework.util.Log4jConfigurer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.FileNotFoundException;

import javax.activation.DataHandler;

public class LoggingUtil {

	private static RegistryManager registryManager = new RegistryManager();
	private static LoggingReader loggingReader = new LoggingReader();
	private static CassandraLogReader cassendraLogReader = new CassandraLogReader();

	public static final String SYSTEM_LOG_PATTERN = "[%d] %5p - %x %m {%c}%n";
	private static final int MAX_LOG_MESSAGES = 200;

	public static void setSystemLoggingParameters(String logLevel, String logPattern)
			throws Exception {
		registryManager.updateConfigurationProperty(LoggingConstants.SYSTEM_LOG_LEVEL, logLevel);
		registryManager
				.updateConfigurationProperty(LoggingConstants.SYSTEM_LOG_PATTERN, logPattern);
	}

	public static LogEvent[] getSystemLogs(String start, String end, String logger,
			String priority, String keyword, String serviceName, String tenantDomain, int logIndex)
			throws LogViewerException {
		return cassendraLogReader.getSystemLogs(start, end, logger, priority, keyword, serviceName,
				tenantDomain, logIndex);
	}

	public static SyslogData getSyslogData() throws Exception {
		return registryManager.getSyslogData();
	}

	public static CassandraConfig getCassandraConfig() throws Exception {
		return registryManager.getCassandraConfigData();
	}

	public static LogInfo[] getLogsIndex(String tenantDomain, String serviceName) throws Exception {
		return loggingReader.getLogsIndex(tenantDomain, serviceName);
	}

	public static LogMessage[] getTenantLogs(String type, String keyword, String logFile,
			String logIndex, int maxLines, int start, int end, String tenantDomain,
			String serviceName) throws Exception {
		return loggingReader.getTenantLogs(type, keyword, logFile, logIndex, maxLines, start, end,
				tenantDomain, serviceName);

	}

	public static LogMessage[] getBottomUpTenantLogs(String type, String keyword, String logFile,
			int maxLines, int start, int end, String tenantDomain, String serviceName)
			throws LogViewerException {
		return loggingReader.getBottomUpTenantLogs(type, keyword, logFile, maxLines, start, end,
				tenantDomain, serviceName);
	}

	public static String getSystemLogLevel() throws Exception {
		String systemLogLevel = registryManager
				.getConfigurationProperty(LoggingConstants.SYSTEM_LOG_LEVEL);
		if (systemLogLevel == null) {
			return Logger.getRootLogger().getLevel().toString();
		}
		return systemLogLevel;
	}

	public static String getSystemLogPattern() throws Exception {
		String systemLogPattern = registryManager
				.getConfigurationProperty(LoggingConstants.SYSTEM_LOG_PATTERN);
		if (systemLogPattern == null) {
			return LoggingUtil.SYSTEM_LOG_PATTERN;
		}
		return systemLogPattern;
	}

	public static boolean isValidTenantDomain(String tenantDomain) {
		return loggingReader.isValidTenantDomain(tenantDomain);
	}

	public static void restoreDefaults() throws Exception {
		registryManager.removeAllRegistryEntries();
		LogManager.resetConfiguration();

		try {
			Log4jConfigurer.initLogging("classpath:log4j.properties");
		} catch (FileNotFoundException e) {
			String msg = "Cannot restore default logging configuration."
					+ " log4j.properties file not found in the classpath";
			throw new Exception(msg, e);
		}
	}

	public static DataHandler downloadLogFiles(String logFile, String tenantDomain,
			String serviceName) throws LogViewerException {
		return loggingReader.downloadLogFiles(logFile, tenantDomain, serviceName);
	}

	public static boolean isManager() {
		return loggingReader.isManager();
	}

	public static void loadCustomConfiguration() throws Exception {
		// set the appender details

		// we have not provided a facility to add or remove appenders so all the
		// initial appender set should present in the system.
		// and all the initall logger should present in the system
		Set<Appender> appenderSet = new HashSet<Appender>();
		Logger rootLogger = LogManager.getRootLogger();

		// set the root logger level, if the system log level is changed.
		String persistedSystemLoggerLevel = registryManager
				.getConfigurationProperty(LoggingConstants.SYSTEM_LOG_LEVEL);
		boolean systemLogLevelChanged = (persistedSystemLoggerLevel != null);
		if (systemLogLevelChanged) {
			rootLogger.setLevel(Level.toLevel(persistedSystemLoggerLevel));
		}

		String persistedSystemLogPattern = registryManager
				.getConfigurationProperty(LoggingConstants.SYSTEM_LOG_PATTERN);
		boolean systemLogPatternChanged = (persistedSystemLogPattern != null);
		setSystemLoggingParameters(persistedSystemLoggerLevel,
				(systemLogPatternChanged) ? persistedSystemLogPattern : SYSTEM_LOG_PATTERN);

		addAppendersToSet(rootLogger.getAllAppenders(), appenderSet);

		// System log level has been changed, need to update all the loggers and
		// appenders
		if (systemLogLevelChanged) {
			Logger logger;
			Enumeration loggersEnum = LogManager.getCurrentLoggers();
			Level systemLevel = Level.toLevel(persistedSystemLoggerLevel);
			while (loggersEnum.hasMoreElements()) {
				logger = (Logger) loggersEnum.nextElement();
				// we ignore all class level defined loggers
				addAppendersToSet(logger.getAllAppenders(), appenderSet);
				logger.setLevel(systemLevel);
			}

			for (Appender appender : appenderSet) {
				if (appender instanceof AppenderSkeleton) {
					AppenderSkeleton appenderSkeleton = (AppenderSkeleton) appender;
					appenderSkeleton.setThreshold(systemLevel);
					appenderSkeleton.activateOptions();
				}
			}
		}

		// Update the logger data according to the data stored in the registry.
		Collection loggerCollection = registryManager.getLoggers();
		if (loggerCollection != null) {
			String[] loggerResourcePaths = loggerCollection.getChildren();
			for (String loggerResourcePath : loggerResourcePaths) {
				String loggerName = loggerResourcePath.substring(LoggingConstants.LOGGERS.length());
				Logger logger = LogManager.getLogger(loggerName);
				Resource loggerResource = registryManager.getLogger(loggerName);
				if (loggerResource != null && logger != null) {
					logger.setLevel(Level.toLevel(loggerResource
							.getProperty(LoggingConstants.LoggerProperties.LOG_LEVEL)));
					logger.setAdditivity(Boolean.parseBoolean(loggerResource
							.getProperty(LoggingConstants.LoggerProperties.ADDITIVITY)));
				}
			}
		}

		// update the appender data according to data stored in database
		Collection appenderCollection = registryManager.getAppenders();
		if (appenderCollection != null) {
			String[] appenderResourcePaths = appenderCollection.getChildren();
			for (String appenderResourcePath : appenderResourcePaths) {
				String appenderName = appenderResourcePath.substring(LoggingConstants.APPENDERS
						.length());
				Appender appender = getAppenderFromSet(appenderSet, appenderName);
				Resource appenderResource = registryManager.getAppender(appenderName);
				if (appenderResource != null && appender != null) {
					if ((appender.getLayout() != null)
							&& (appender.getLayout() instanceof PatternLayout)) {
						((PatternLayout) appender.getLayout())
								.setConversionPattern(appenderResource
										.getProperty(LoggingConstants.AppenderProperties.PATTERN));
					}
					if (appender instanceof FileAppender) {
						FileAppender fileAppender = ((FileAppender) appender);
						fileAppender.setFile(appenderResource
								.getProperty(LoggingConstants.AppenderProperties.LOG_FILE_NAME));
						fileAppender.activateOptions();
					}

					if (appender instanceof MemoryAppender) {
						MemoryAppender memoryAppender = (MemoryAppender) appender;
						memoryAppender.setCircularBuffer(new CircularBuffer(200));
						memoryAppender.activateOptions();
					}

					if (appender instanceof SyslogAppender) {
						SyslogAppender syslogAppender = (SyslogAppender) appender;
						syslogAppender.setSyslogHost(appenderResource
								.getProperty(LoggingConstants.AppenderProperties.SYS_LOG_HOST));
						syslogAppender.setFacility(appenderResource
								.getProperty(LoggingConstants.AppenderProperties.FACILITY));
					}

					if (appender instanceof AppenderSkeleton) {
						AppenderSkeleton appenderSkeleton = (AppenderSkeleton) appender;
						appenderSkeleton.setThreshold(Level.toLevel(appenderResource
								.getProperty(LoggingConstants.AppenderProperties.THRESHOLD)));
						appenderSkeleton.activateOptions();
					}
				}
			}
		}
	}

	private static void addAppendersToSet(Enumeration appenders, Set<Appender> appenderSet) {
		while (appenders.hasMoreElements()) {
			Appender appender = (Appender) appenders.nextElement();
			appenderSet.add(appender);
		}
	}

	public static Appender getAppenderFromSet(Set<Appender> appenderSet, String name) {
		for (Appender appender : appenderSet) {
			if (appender.getName().equals(name)) {
				return appender;
			}
		}
		return null;
	}

	public static void updateConfigurationProperty(String key, String value)
			throws RegistryException {
		registryManager.updateConfigurationProperty(key, value);
	}

	public static String getConfigurationProperty(String key) throws RegistryException {
		return registryManager.getConfigurationProperty(key);
	}

	public static void removeAllLoggersAndAppenders() throws Exception {
		registryManager.removeAllRegistryEntries();
	}

	public static boolean isSysLogAppender(String tenantDomain) throws Exception {
		int tenantId = loggingReader.getTenantIdForDomain(tenantDomain);
		return loggingReader.isSysLogAppender(tenantId);
	}

	public static boolean isSyslogConfigured() throws Exception {
		if (registryManager.getSyslogConfig() == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean isCassandraConfigured () {
		return cassendraLogReader.isCassandraConfigured();
	}

	public static boolean isSTSyslogConfig(String tenantDomain) throws Exception {
		return loggingReader.isSuperTenantUser();
	}

	public static boolean isStratosService() throws Exception {
		return loggingReader.isStratosService();
	}

	public static int getLineNumbers(String logFile, String tenantDomain, String serviceName)
			throws Exception {
		return loggingReader.getLineNumbers(logFile, tenantDomain, serviceName);
	}

	public static boolean isLogsConfigured(String tenantDomain) throws Exception {
		if (loggingReader.isSuperTenantUser()) {
			return true;
		} else {
			return isSysLogAppender(tenantDomain);
		}
	}

	public static String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end,
			String tenantId, String serviceName) throws LogViewerException {
		return loggingReader.getLogLinesFromFile(logFile, maxLogs, start, end, tenantId,
				serviceName);
	}

	/**
	 * This method stream log messages and retrieve 100 log messages per page
	 * 
	 * @param pageNumber
	 *            The page required. Page number starts with 0.
	 * @param sourceList
	 *            The original list of items
	 * @param pageable
	 *            The type of Pageable item
	 * @return Returned page
	 */
	public static <C> List<C> doPaging(int pageNumber, List<C> sourceList, int maxLines,
			Pageable pageable) {
		if (pageNumber < 0 || pageNumber == Integer.MAX_VALUE) {
			pageNumber = 0;
		}
		if (sourceList.size() == 0) {
			return sourceList;
		}
		if (pageNumber < 0) {
			throw new RuntimeException("Page number should be a positive integer. "
					+ "Page numbers begin at 0.");
		}
		int itemsPerPageInt = MAX_LOG_MESSAGES; // the default number of item
												// per page
		int numberOfPages = (int) Math.ceil((double) maxLines / itemsPerPageInt);
		if (pageNumber > numberOfPages - 1) {
			pageNumber = numberOfPages - 1;
		}
		List<C> returnList = new ArrayList<C>();
		for (int i = 0; i < sourceList.size(); i++) {
			returnList.add(sourceList.get(i));
		}
		int pages = calculatePageLevel(pageNumber + 1);
		if (pages > numberOfPages) {
			pages = numberOfPages;
		}
		pageable.setNumberOfPages(pages);
		pageable.set(returnList);
		return returnList;
	}

	/*
	 * This is an equation to retrieve the visible number of pages ie p1-p5 -> 5
	 * p6-p10 -> 10 p11-p15 -> 15
	 */
	private static int calculatePageLevel(int x) {
		int p = x / 5;
		int q = x % 5;
		int t = (p + 1) * 5;
		int s = (p * 5) + 1;
		int y = q > 0 ? t : s;
		return y;
	}
}
