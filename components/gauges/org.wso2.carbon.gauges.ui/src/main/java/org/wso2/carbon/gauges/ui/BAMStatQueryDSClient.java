/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.gauges.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.*;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.gauges.ui.BAMStatQueryDSStub;
import org.wso2.carbon.bam.ActivityOperation;
import org.wso2.carbon.bam.Count;
import org.wso2.carbon.bam.Data;
import org.wso2.carbon.bam.JmxMetricsInfo;
import org.wso2.carbon.bam.Operation;
import org.wso2.carbon.bam.Message;
import org.wso2.carbon.bam.Endpoint;
import org.wso2.carbon.bam.OperationID;
import org.wso2.carbon.bam.Sequence;
import org.wso2.carbon.bam.ProxyService;

import java.rmi.RemoteException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class BAMStatQueryDSClient {
	private static final Log log = LogFactory.getLog(BAMStatQueryDSClient.class);

	BAMStatQueryDSStub stub;

	public BAMStatQueryDSClient(String cookie, String backendServerURL, ConfigurationContext configCtx,
			Locale locale) throws AxisFault {
		String serviceURL = backendServerURL + "BAMStatQueryDS/getLastMinuteRequestCount";

		stub = new BAMStatQueryDSStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public BAMStatQueryDSClient(ServletConfig config, HttpSession session, HttpServletRequest request)
			throws AxisFault {
		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
				CarbonConstants.CONFIGURATION_CONTEXT);
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

		String serviceURL = backendServerURL + "BAMStatQueryDS/getLastMinuteRequestCount";

		stub = new BAMStatQueryDSStub(configContext, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public Count[] getLastMinuteRequestCount(int serviceID) throws RemoteException {
		return stub.getLastMinuteRequestCount(serviceID);
	}

	public String getAvgResponseTime(int serviceID) throws RemoteException {
		try {
			return stub.getAvgResponseTime(serviceID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getMinResponseTime(int serviceID) throws RemoteException {
		try {
			return stub.getMinResponseTime(serviceID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getMaxResponseTime(int serviceID) throws RemoteException {
		try {
			return stub.getMaxResponseTime(serviceID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	// public String getAvgResponseTime(String serverUrl, String serviceName)
	// throws RemoteException {
	// try {
	// return stub.getAvgResponseTime(serverUrl, serviceName, serverUrl,
	// serviceName)[0]
	// .getTime();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getAvgResponseTimeSystem(String serverUrl)
	// throws RemoteException {
	// try {
	// return stub.getAvgResponseTimeSystem(serverUrl, serverUrl)[0]
	// .getTime();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getMinResponseTimeSystem(String serverUrl)
	// throws RemoteException {
	// try {
	// return stub.getMinResponseTimeSystem(serverUrl, serverUrl)[0]
	// .getTime();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getMaxResponseTimeSystem(String serverUrl)
	// throws RemoteException {
	// try {
	// return stub.getMaxResponseTimeSystem(serverUrl, serverUrl)[0]
	// .getTime();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getNumberOfCallsToday(String serverUrl, String serviceName)
	// throws
	// RemoteException {
	// try {
	// return stub.getNoOfCallsToday(serverUrl, serviceName)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getNumberOfCallsTodaySystem(String serverUrl) throws
	// RemoteException {
	// try {
	// return stub.getNoOfCallsTodaySystem(serverUrl)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getServerLoginAttempts(String serverUrl, int duration)
	// throws
	// RemoteException {
	// try {
	// return stub.getServerLoginAttempts(serverUrl, duration)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getServerFailedLoginAttempts(String serverUrl, int
	// duration) throws
	// RemoteException {
	// try {
	// return stub.getServerFailedLoginAttempts(serverUrl,
	// duration)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getUserLoginAttempts(String serverUrl, String userName, int
	// duration)
	// throws
	// RemoteException {
	// try {
	// return stub.getUserLoginAttempts(serverUrl, userName,
	// duration)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }
	//
	// public String getUserFailedLoginAttempts(String serverUrl, String
	// userName, int duration)
	// throws
	// RemoteException {
	// try {
	// return stub.getUserFailedLoginAttempts(serverUrl, userName,
	// duration)[0].getCount();
	// } catch (Exception e) {
	// return "0";
	// }
	// }

	public String getValueRangePair(String value) {
		int iValue = (int) Double.parseDouble(value);
		int range = (iValue / 10 + 1) * 10; // range in blocks of 10 units
		return "&value=" + value + "&range=" + range + "&";
	}

	// Server data

	public String getLatestAverageResponseTimeForServer(int serverID) throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestAverageResponseTimeForServer(serverID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);
	}

	public String getLatestMaximumResponseTimeForServer(int serverID) throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestMaximumResponseTimeForServer(serverID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);
	}

	public String getLatestMinimumResponseTimeForServer(int serverID) throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestMinimumResponseTimeForServer(serverID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);
	}

	public String getLatestRequestCountForServer(int serverID) throws RemoteException {
		try {
			return stub.getLatestRequestCountForServer(serverID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestResponseCountForServer(int serverID) throws RemoteException {
		try {
			return stub.getLatestResponseCountForServer(serverID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestFaultCountForServer(int serverID) throws RemoteException {
		try {
			return stub.getLatestFaultCountForServer(serverID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public Data getLatestDataForServer(int serverID) throws RemoteException {
		try {
			return stub.getLatestDataForServer(serverID)[0];
		} catch (Exception e) {
			return null;
		}
	}

	// Service data

	public String getLatestAverageResponseTimeForService(int serviceID) throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestAverageResponseTimeForService(serviceID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);

	}

	public String getLatestMaximumResponseTimeForService(int serviceID) throws RemoteException {

		String value = "0";
		try {
			value = stub.getLatestMaximumResponseTimeForService(serviceID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);
	}

	public String getLatestMinimumResponseTimeForService(int serviceID) throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestMinimumResponseTimeForService(serviceID)[0].getTime();
		} catch (RemoteException ignore) {
		}

		return getValueRangePair(value);
	}

	public String getLatestRequestCountForService(int serviceID) throws RemoteException {
		try {
			return stub.getLatestRequestCountForService(serviceID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestResponseCountForService(int serviceID) throws RemoteException {
		try {
			return stub.getLatestResponseCountForService(serviceID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestFaultCountForService(int serviceID) throws RemoteException {
		try {
			return stub.getLatestFaultCountForService(serviceID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public Data getLatestDataForService(int serviceID) throws RemoteException {
		try {
			return stub.getLatestDataForService(serviceID)[0];
		} catch (Exception e) {
			return null;
		}
	}

	// Operation data

	public Operation[] getOperations(int serviceID) throws RemoteException {
		try {
			return stub.getOperations(serviceID);
		} catch (Exception e) {
			return null;
		}
	}

	public String getLatestAverageResponseTimeForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestAverageResponseTimeForOperation(operationID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestMaximumResponseTimeForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestMaximumResponseTimeForOperation(operationID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestMinimumResponseTimeForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestMinimumResponseTimeForOperation(operationID)[0].getTime();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestRequestCountForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestRequestCountForOperation(operationID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestResponseCountForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestResponseCountForOperation(operationID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestFaultCountForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestFaultCountForOperation(operationID)[0].getCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public Data getLatestDataForOperation(int operationID) throws RemoteException {
		try {
			return stub.getLatestDataForOperation(operationID)[0];
		} catch (Exception e) {
			return null;
		}
	}

	// Endpoint Data

	public Endpoint[] getEndpoints(int serverID) throws RemoteException {
		try {
			return stub.getEndpoints(serverID);
		} catch (Exception ignore) {
			return null;
		}
	}

	public String getLatestInAverageProcessingTimeForEndpoint(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getAverageTime();
		} catch (Exception ignore) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMaximumProcessingTimeForEndpoint(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getMaximumTime();
		} catch (Exception ignore) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMinimumProcessingTimeForEndpoint(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getMinimumTime();
		} catch (Exception ignore) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInAverageProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getAverageTime();
		} catch (Exception ignore) {
			return "0";
		}

		return value;
	}

	public String getLatestInMaximumProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getMaximumTime();
		} catch (Exception ignore) {
			return "0";
		}

		return value;
	}

	public String getLatestInMinimumProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName)[0]
					.getMinimumTime();
		} catch (Exception ignore) {
			return "0";
		}

		return value;
	}

	public String getLatestInCumulativeCountForEndpoint(int serverID, String endpointName)
			throws RemoteException {
		try {
			return stub.getLatestInCumulativeCountForEndpoint(serverID, endpointName)[0].getCumulativeCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestInFaultCountForEndpoint(int serverID, String endpointName) throws RemoteException {
		try {
			return stub.getLatestInFaultCountForEndpoint(serverID, endpointName)[0].getFaultCount();
		} catch (Exception e) {
			return "0";
		}
	}

	// Sequence Data

	public Sequence[] getSequences(int serverID) throws RemoteException {
		try {
			return stub.getSequences(serverID);
		} catch (Exception ignore) {
			return null;
		}
	}

	public String getLatestInAverageProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName)[0]
					.getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMaximumProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMinimumProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInAverageProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName)[0]
					.getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInMaximumProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInMinimumProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInCumulativeCountForSequence(int serverID, String sequenceName)
			throws RemoteException {
		try {
			return stub.getLatestInCumulativeCountForSequence(serverID, sequenceName)[0].getCumulativeCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestInFaultCountForSequence(int serverID, String sequenceName) throws RemoteException {
		try {
			return stub.getLatestInFaultCountForSequence(serverID, sequenceName)[0].getFaultCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestOutAverageProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutAverageProcessingTimeForSequence(serverID, sequenceName)[0]
					.getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutMaximumProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutMaximumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutMinimumProcessingTimeForSequence(int serverID, String sequenceName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutMinimumProcessingTimeForSequence(serverID, sequenceName)[0]
					.getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutCumulativeCountForSequence(int serverID, String sequenceName)
			throws RemoteException {
		try {
			return stub.getLatestOutCumulativeCountForSequence(serverID, sequenceName)[0]
					.getCumulativeCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestOutFaultCountForSequence(int serverID, String sequenceName) throws RemoteException {
		try {
			return stub.getLatestOutFaultCountForSequence(serverID, sequenceName)[0].getFaultCount();
		} catch (Exception e) {
			return "0";
		}
	}

	// Proxy Data

	public ProxyService[] getProxyServices(int serverID) throws RemoteException {
		try {
			return stub.getProxyServices(serverID);
		} catch (Exception ignore) {
			return null;
		}
	}

	public String getLatestInAverageProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForProxy(serverID, proxyName)[0].getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMaximumProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName)[0].getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInMinimumProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName)[0].getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestInAverageProcessingTimeForProxyNoWrap(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInAverageProcessingTimeForProxy(serverID, proxyName)[0].getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInMaximumProcessingTimeForProxyNoWrap(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName)[0].getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInMinimumProcessingTimeForProxyNoWrap(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName)[0].getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return value;
	}

	public String getLatestInCumulativeCountForProxy(int serverID, String proxyName) throws RemoteException {
		try {
			return stub.getLatestInCumulativeCountForProxy(serverID, proxyName)[0].getCumulativeCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestInFaultCountForProxy(int serverID, String proxyName) throws RemoteException {
		try {
			return stub.getLatestInFaultCountForProxy(serverID, proxyName)[0].getFaultCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestOutAverageProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutAverageProcessingTimeForProxy(serverID, proxyName)[0].getAverageTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutMaximumProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutMaximumProcessingTimeForProxy(serverID, proxyName)[0].getMaximumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutMinimumProcessingTimeForProxy(int serverID, String proxyName)
			throws RemoteException {
		String value = "0";
		try {
			value = stub.getLatestOutMinimumProcessingTimeForProxy(serverID, proxyName)[0].getMinimumTime();
		} catch (Exception e) {
			return "0";
		}

		return getValueRangePair(value);
	}

	public String getLatestOutCumulativeCountForProxy(int serverID, String proxyName) throws RemoteException {
		try {
			return stub.getLatestOutCumulativeCountForProxy(serverID, proxyName)[0].getCumulativeCount();
		} catch (Exception e) {
			return "0";
		}
	}

	public String getLatestOutFaultCountForProxy(int serverID, String proxyName) throws RemoteException {
		try {
			return stub.getLatestOutFaultCountForProxy(serverID, proxyName)[0].getFaultCount();
		} catch (Exception e) {
			return "0";
		}
	}

	/* Activity */

	public String getLatestMaximumOperationsForAnActivityID(int activityID) throws RemoteException {
		try {

			return stub.getMaximumOperationsForAnActivityID(activityID)[0].getNum();

		} catch (Exception ignore) {
			return null;
		}
	}

	public OperationID[] getOperationsForMessageID(int messageID) throws RemoteException {

		try {
			return stub.getOperationIDForMessageID(messageID);
		} catch (Exception e) {
			return null;
		}
	}

	public String getActivityIDForActivityName(String activityName) throws RemoteException {
		String value = "0";
		try {
			value = stub.getActivityIDForActivityName(activityName)[0].getActivityID();

			return value;
		} catch (Exception ignore) {
			return null;
		}
	}

	public ActivityOperation[] getOperationsForActivityID(int activityID) throws RemoteException {
		try {
			return stub.getOperationForActivityID(activityID);
		} catch (Exception e) {
			return null;
		}
	}

	public Message[] getMessagesForOperationIDAndActivityID(int operationID, int activityID)
			throws RemoteException {
		try {
			return stub.getMessagesForOperationIDAndActivityID(operationID, activityID);
		} catch (Exception e) {
			return null;
		}
	}

	public String getMessagesCountForOperationIDAndActivityID(int operationID, int activityID)
			throws RemoteException {
		try {

			return stub.getMessagesCountForOperationIDAndActivityID(operationID, activityID)[0]
					.getCountMessage();

		} catch (Exception ignore) {
			return null;
		}
	}

	public ActivityInfo[] getActivityInfoForActivityID(int activityID) throws RemoteException {
		try {
			return stub.getActivityInfoForActivityID(activityID);

		} catch (Exception ignore) {
			return null;
		}
	}

	public JmxMetricsInfo[] getJMXMetricsWindow(int serverID) throws RemoteException {
		try {
			return stub.getJMXMetricsWindow(serverID);

		} catch (Exception ignore) {
			return null;
		}
	}
}
