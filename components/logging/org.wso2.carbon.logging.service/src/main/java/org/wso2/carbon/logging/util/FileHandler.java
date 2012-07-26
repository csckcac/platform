package org.wso2.carbon.logging.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.logging.config.LoggingConfigManager;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

public class FileHandler {

	private String getFileLocation(String serverURL, String logFile) {
		String fileLocation = "";
		String lastChar = String.valueOf(serverURL.charAt(serverURL.length() - 1));
		if (lastChar.equals(LoggingConstants.URL_SEPARATOR)) { // http://my.log.server/logs/stratos/
			serverURL = serverURL.substring(0, serverURL.length() - 1);
		}
		fileLocation = serverURL.replaceAll("\\s", "%20");
		logFile = logFile.replaceAll("\\s", "%20");
		String tenantId = String.valueOf(CarbonContextHolder.getCurrentCarbonContextHolder()
				.getTenantId());
	
		if (tenantId.equals(String.valueOf(MultitenantConstants.INVALID_TENANT_ID))
				|| tenantId.equals(String.valueOf(MultitenantConstants.SUPER_TENANT_ID))) {
			tenantId = "0";
		}
		String ServiceName = getCurrentServerName();
		if (logFile != null && !logFile.equals("")) {
			return fileLocation + LoggingConstants.URL_SEPARATOR + tenantId
					+ LoggingConstants.URL_SEPARATOR + ServiceName + LoggingConstants.URL_SEPARATOR
					+ logFile;
		} else {
			return fileLocation + LoggingConstants.URL_SEPARATOR + tenantId
			+ LoggingConstants.URL_SEPARATOR + ServiceName;
		}

	}
	
	private String getCurrentServerName() {
		String serverName = ServerConfiguration.getInstance().getFirstProperty("Name");
		serverName = serverName.replace("WSO2", "");
		return serverName.replace(" ", "_");
	}

	private InputStream getLogDataStream(String fileName) throws Exception {
		LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
		String url = "";
		// TODO this will change depending on the hive impl
		String hostUrl = config.getArchivedHost();
		url = getFileLocation(hostUrl, fileName);
		String password = config.getArchivedUser();
		String userName = config.getArchivedPassword();
		int port = Integer.parseInt(config.getArchivedPort());
		String realm = config.getArchivedRealm();
		URI uri = new URI(url);
		String host = uri.getHost();
		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(host, port, realm),
				new UsernamePasswordCredentials(userName, password));
		GetMethod get = new GetMethod(url);
		get.setDoAuthentication(true);
		client.executeMethod(get);
		return get.getResponseBodyAsStream();
	}

	public LogInfo[] getRemoteLogFiles() throws LogViewerException {
		InputStream logStream;
		try {
			logStream = getLogDataStream("");
		} catch (HttpException e) {
			throw new LogViewerException("Cannot establish the connection to the syslog server", e);
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		String line;
		ArrayList<LogInfo> logs = new ArrayList<LogInfo>();
		Pattern pattern = Pattern.compile(LoggingConstants.RegexPatterns.SYS_LOG_FILE_NAME_PATTERN);
		try {
			while ((line = dataInput.readLine()) != null) {
				String fileNameLinks[] = line
						.split(LoggingConstants.RegexPatterns.LINK_SEPARATOR_PATTERN);
				String fileDates[] = line
						.split((LoggingConstants.RegexPatterns.SYSLOG_DATE_SEPARATOR_PATTERN));
				String dates[] = null;
				String sizes[] = null;
				if (fileDates.length == 3) {
					dates = fileDates[1]
							.split(LoggingConstants.RegexPatterns.COLUMN_SEPARATOR_PATTERN);
					sizes = fileDates[2]
							.split(LoggingConstants.RegexPatterns.COLUMN_SEPARATOR_PATTERN);
				}
				if (fileNameLinks.length == 2) {
					String logFileName[] = fileNameLinks[1]
							.split(LoggingConstants.RegexPatterns.GT_PATTARN);
					Matcher matcher = pattern.matcher(logFileName[0]);
					if (matcher.find()) {
						if (logFileName != null && dates != null && sizes != null) {
							String logName = logFileName[0].replace(
									LoggingConstants.RegexPatterns.BACK_SLASH_PATTERN, "");
							logName = logName.replaceAll("%20", " ");
							LogInfo log = new LogInfo(logName, dates[0], sizes[0]);
							logs.add(log);
						}
					}
				}
			}
			dataInput.close();
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		return getSortedLogInfo(logs.toArray(new LogInfo[logs.size()]));
	}

	private LogInfo[] getSortedLogInfo(LogInfo logs[]) {
		int maxLen = logs.length;
		if (maxLen > 0) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			Collections.sort(logInfoList, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					LogInfo log1 = (LogInfo) o1;
					LogInfo log2 = (LogInfo) o2;
					return log1.getLogName().compareToIgnoreCase(log2.getLogName());
				}

			});
			return (LogInfo[]) logInfoList.toArray(new LogInfo[logInfoList.size()]);
		} else {
			return null;
		}
	}

	public int getLineNumbers(String logFile) throws Exception {
		InputStream logStream;

		try {
			logStream = getLocalInputStream(logFile);
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = logStream.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return count;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				logStream.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}

	public DataHandler downloadArchivedLogFiles(String logFile) throws LogViewerException {
		InputStream logStream;
		try {
			logStream = getLogDataStream(logFile);
		} catch (HttpException e) {
			throw new LogViewerException("Cannot establish the connection to the apache server", e);
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		try {
			ByteArrayDataSource bytArrayDS = new ByteArrayDataSource(logStream, "application/zip");
			DataHandler dataHandler = new DataHandler(bytArrayDS);
			return dataHandler;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				logStream.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}

	private InputStream getLocalInputStream(String logFile) throws FileNotFoundException {
		String fileName = CarbonUtils.getCarbonLogsPath() + LoggingConstants.URL_SEPARATOR
				+ logFile;
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		return is;
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end)
			throws LogViewerException {
		ArrayList<String> logsList = new ArrayList<String>();
		InputStream logStream;
		if (end > maxLogs) {
			end = maxLogs;
		}
		try {
			logStream = getLocalInputStream(logFile);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		int index = 1;
		String line;
		try {
			while ((line = dataInput.readLine()) != null) {
				if (index <= end && index > start) {
					logsList.add(line);
				}
				index++;
			}
			dataInput.close();
		} catch (IOException e) {
			throw new LogViewerException("Cannot read the log file", e);
		}
		return logsList.toArray(new String[logsList.size()]);
	}

}
