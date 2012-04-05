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
package org.wso2.carbon.logging.view.ui;

import java.io.InputStream;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.view.stub.LogViewerException;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.carbon.logging.view.stub.types.carbon.PaginatedLogInfo;
import org.wso2.carbon.logging.view.stub.types.carbon.PaginatedLogMessage;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

public class LogViewerClient {
	private static final Log log = LogFactory.getLog(LogViewerClient.class);
	public LogViewerStub stub;

	public LogViewerClient(String cookie, String backendServerURL, ConfigurationContext configCtx)
			throws AxisFault {
		String serviceURL = backendServerURL + "LogViewer";
		stub = new LogViewerStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
		option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
	}

	public boolean isLogsConfigured(String domainName) throws Exception {
		try {
			return stub.isLogsConfigured(domainName);
		} catch (Exception e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}

	}
	
	public boolean isDataFromSysLog(String domainName) throws Exception {
		try {
			return stub.isDataFromSysLog(domainName);
		} catch (Exception e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}

	}
	
	public boolean isValidTenantDomain(String tenantDomain) throws Exception {
		try {
			return stub.isValidTenantDomain(tenantDomain);
		} catch (Exception e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public void downloadLogFiles(String logFile, String tenantDomain,
			String serviceName, HttpServletResponse response) throws Exception {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment;filename="
					+ logFile.replaceAll("\\s", "_"));
			
			DataHandler data = stub.downloadLogFiles(logFile, tenantDomain,
					serviceName);
			InputStream fileToDownload = data.getInputStream();
			int c;
			while ((c = fileToDownload.read()) != -1) {
				outputStream.write(c);
			}
			outputStream.flush();
			outputStream.flush();
		} catch (Exception e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	
	public boolean isSTSyslogConfig(String domainName) throws Exception {
		try {
			return stub.isSTSyslogConfig(domainName);
		} catch (Exception e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public int getLineNumbers(String logFile,String domainName, String serviceName) throws Exception {
		try {
			return stub.getLineNumbers(logFile,domainName,serviceName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public PaginatedLogInfo getPaginatedLogInfo(int pageNumber, String tenantDomain, String serviceName) throws Exception {
		try {
			return stub.getPaginatedLogInfo(pageNumber,tenantDomain, serviceName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	
	public LogMessage[] getLogs(String type, String keyword) throws Exception {
		if (type == null || type.equals("")) {
			type = "ALL";
		}
		try {
			return stub.getLogs(type, keyword);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end, String domainName, String serviceName)
			throws Exception {
		try {
			return stub.getLogLinesFromFile(logFile, maxLogs, start, end, domainName, serviceName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public  PaginatedLogMessage getPaginatedBottomUpLogMessage(int pageNumber, String type,
			String keyword, String logFile, int maxLines, int start, int end, String domainName, String serviceName) throws Exception {
		if (type == null || type.equals("")) {
			type = "ALL";
		}
		try {
			return stub.getPaginatedBottomUpLogMessage(pageNumber, type, keyword, logFile, maxLines, start, end, domainName, serviceName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public PaginatedLogMessage getPaginatedLogMessage(int pageNumber, String type, String keyword,
			String logFile, String logIndex, int maxLines, int start, int end, String domainName, String serviceName) throws Exception {
		try {
			if (type == null || type.equals("")) {
				type = "ALL";
			}
			return stub.getPaginatedLogMessage(pageNumber, type, keyword, logFile, logIndex, maxLines, start, end, domainName, serviceName);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public boolean isManager () throws RemoteException {
		try {
			return stub.isManager();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	
	public boolean isStratosService () throws RemoteException, LogViewerException {
		try {
			return stub.isStratosService();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public String[] getServiceNames() throws RemoteException, LogViewerLogViewerException {
		try {
			return stub.getServiceNames();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}

	public  LogEvent[]  getSystemLogs(String start, String end, String logger,
			String priority, String keyword, String serviceName, String tenantDomain, int logIndex) throws LogViewerLogViewerException, RemoteException {
		try {
			return stub.getSystemLogs(start, end, logger, priority, keyword, serviceName, tenantDomain, logIndex);
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	
	public boolean isCassandraConfigured () throws RemoteException {
		try {
			return stub.isCassandraConfigured();
		} catch (RemoteException e) {
			String msg = "Error occurred while getting logger data. Backend service may be unavailable";
			log.error(msg, e);
			throw e;
		}
	}
	public String getImageName(String type) {
		if (type.equals("INFO")) {
			return "images/information.gif";
		} else if (type.equals("ERROR")) {
			return "images/error.png";
		} else if (type.equals("WARN")) {
			return "images/warn.png";
		} else if (type.equals("DEBUG")) {
			return "images/debug.png";
		} else if (type.equals("TRACE")) {
			return "images/trace.png";
		} else if (type.equals("FATAL")) {
			return "images/fatal.png";
		}
		return "";
	}
	
	public String[] getLogLevels() {
		return new String[] { "ALL", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" };
	}

}
