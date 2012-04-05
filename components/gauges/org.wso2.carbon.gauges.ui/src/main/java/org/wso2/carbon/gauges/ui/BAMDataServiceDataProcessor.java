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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.*;
import org.wso2.carbon.gauges.ui.types.carbon.ActivityDTO;
import org.wso2.carbon.gauges.ui.types.carbon.MessageDTO;
import org.wso2.carbon.gauges.ui.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.gauges.ui.types.carbon.ServiceDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.bouncycastle.crypto.prng.RandomGenerator;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.Random;
import java.text.DecimalFormat;

public class BAMDataServiceDataProcessor {

	private static final Log log = LogFactory.getLog(BAMDataServiceDataProcessor.class);

	BAMStatQueryDSClient bamDSClient;
	BAMListAdminServiceClient bamListAdminClient;

	public static String serverArrayToString(MonitoredServerDTO[] a, String separator1, String separator2) {
		String result = "";
		if (a != null && a.length > 0) {
			result += a[0].getServerId() + separator1 + a[0].getServerURL(); // start
			// with
			// the
			// first
			// element
			for (int i = 1; i < a.length; i++) {
				result += separator2 + a[i].getServerId() + separator1 + a[i].getServerURL();
			}
		}
		return result;
	}

	private static String serviceArrayToString(ServiceDTO[] a, String separator1, String separator2) {
		String result = "";
		if (a != null & a.length > 0) {
			result += a[0].getID() + separator1 + a[0].getName(); // start with
			// the first
			// element
			for (int i = 1; i < a.length; i++) {
				result += separator2 + a[i].getID() + separator1 + a[i].getName();
			}
		}
		return result;
	}

	public BAMDataServiceDataProcessor(ServletConfig config, HttpSession session, HttpServletRequest request)
			throws AxisFault {
		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
				CarbonConstants.CONFIGURATION_CONTEXT);
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

		bamDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());

		bamListAdminClient = new BAMListAdminServiceClient(cookie, backendServerURL, configContext, request
				.getLocale());

	}

	public String getAdminConsoleUrl(HttpServletRequest request) {
		String data = CarbonUIUtil.getAdminConsoleURL(request);

		// Remove Unnecessary stuff
		data = data.split("/carbon/")[0];

		return data;
	}

	/**
	 * GetLastMinuteRequestCount for the Service
	 */
	public String getLastMinuteRequestCount(int serviceID) {
		String resp = "";

		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));

			Count[] response = bamDSClient.getLastMinuteRequestCount(serviceID);
			if (response != null) {
				return response[0].getCount();
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "0";
	}

	// public String getLastMinuteRequestCountSystem() {
	// String resp = "";
	//
	// try {
	// Random randomGenerator = new Random();
	// int randomInt = randomGenerator.nextInt(100);
	//
	// resp = "&value=" + randomInt + "&range=100";
	// return resp;
	// } catch (Exception e) {
	// log.debug(e);
	// }
	// return "0";
	// }

	public String getServerList() {
		try {
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			if (serverList != null) {
				return serverArrayToString(serverList, ",", "|");
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "No Servers Configured";
	}

	private int[] getServerIDList() {
		try {
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			if (serverList != null) {
				if (serverList.length > 0) {
					int[] serverIDList = new int[serverList.length];
					for (int i = 0; i < serverList.length; i++) {
						serverIDList[i] = serverList[i].getServerId();
					}
					return serverIDList;
				}
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return null;
	}

	public String getServicesList(int serverID) {
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			// String[] servicesList =
			// bamListAdminClient.getServicesList(serverUrl);
			ServiceDTO[] servicesList = bamListAdminClient.getServicesList(serverID);

			if (servicesList != null) {
				return serviceArrayToString(servicesList, ",", "|");
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "No Services Found";
	}

	public String getAvgResponseTime(int serviceID) {

		String value = "0";
		String range = "1000";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			value = bamDSClient.getAvgResponseTime(serviceID);

			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal < 10) {
					range = "10";
				} else if (dVal < 100) {
					range = "100";
				} else if (dVal < 1000) {
					range = "1000";
				} else if (dVal < 0) {
					dVal = 0;
				} else {
					dVal = 1000;
					range = "1000";
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}

		return "&value=" + value + "&range=" + range;
	}

	public String getMinResponseTimeSystem(int serverID) {

		String value = "0";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));

			// value = bamDSClient.getMinResponseTimeSystem(serverID);
			value = "0";
			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal > 1000) {
					dVal = 1000;
				} else if (dVal < 0) {
					dVal = 0;
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}

		return value;
	}

	public String getMaxResponseTimeSystem(String serverUrl) {

		String value = "0";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			// value = bamDSClient.getMaxResponseTimeSystem(serverUrl);
			value = "0";
			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal > 1000) {
					dVal = 1000;
				} else if (dVal < 0) {
					dVal = 0;
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}

		return value;
	}

	public String getAvgResponseTimeSystem(String serverUrl) {

		String value = "0";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			// value = bamDSClient.getAvgResponseTimeSystem(serverUrl);
			value = "0";
			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal > 1000) {
					dVal = 1000;
				} else if (dVal < 0) {
					dVal = 0;
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}

		return value;
	}

	public String getAvgResponseTimeSystem() {
		String resp = "100";
		return resp;
	}

	public String getMaxResponseTime(int serviceID) {
		String value = "0";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			value = bamDSClient.getMaxResponseTime(serviceID);

			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal > 1000) {
					dVal = 1000;
				} else if (dVal < 0) {
					dVal = 0;
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}
		return value;
	}

	public String getMinResponseTime(int serviceID) {
		String value = "0";
		try {
			// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
			value = bamDSClient.getMinResponseTime(serviceID);

			if (value == null) {
				value = "0";
			} else {
				int dVal = (int) Double.parseDouble(value);

				// Correcting extreme values.
				if (dVal > 1000) {
					dVal = 1000;
				} else if (dVal < 0) {
					dVal = 0;
				}

				value = "" + dVal;
			}
		} catch (Exception e) {
			log.debug(e);
			value = "0";
		}
		return value;
	}

	// public String getMaxResponseTimeSystem() {
	// String resp = "100";
	// return resp;
	// }
	//
	// public String getMinResponseTime(String serverUrl, String serviceName) {
	// String resp = "200";
	// return resp;
	// }
	//
	// public String getMinResponseTimeSystem() {
	// String resp = "100";
	// return resp;
	// }

	/*
	 * public String getMinMaxAverageRespTimesSystem(int serverID) { String minVal = "0"; String maxVal = "0";
	 * String avgVal = "0"; try { //serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
	 * 
	 * minVal = bamDSClient.getMinResponseTimeSystem(serverID); maxVal =
	 * bamDSClient.getMaxResponseTimeSystem(serverID); avgVal =
	 * bamDSClient.getAvgResponseTimeSystem(serverID); } catch (Exception e) { minVal = "0"; maxVal = "0";
	 * avgVal = "0"; }
	 * 
	 * return minVal + "&" + maxVal + "&" + avgVal; }
	 */

	public String getMinMaxAverageRespTimesService(int serviceID) {
		String minVal = "0";
		String maxVal = "0";
		String avgVal = "0";

		try {
			minVal = bamDSClient.getMinResponseTime(serviceID);
			maxVal = bamDSClient.getMaxResponseTime(serviceID);
			avgVal = bamDSClient.getAvgResponseTime(serviceID);
		} catch (RemoteException e) {
			minVal = "0";
			maxVal = "0";
			avgVal = "0";
		}

		return minVal + "&" + maxVal + "&" + avgVal;
	}

	public String getEndpoints(int serverID) {

		Endpoint endpoints[] = null;
		try {
			endpoints = bamDSClient.getEndpoints(serverID);
		} catch (RemoteException e) {
		}

		String epString = "";
		if (endpoints != null && endpoints.length > 0) {
			epString = endpoints[0].getEndpoint();
			for (int i = 1; i < endpoints.length; i++) {
				epString += "&" + endpoints[i].getEndpoint();
			}
		}
		return epString;
	}

	public String getSequences(int serverID) {

		Sequence sequences[] = null;
		try {
			sequences = bamDSClient.getSequences(serverID);
		} catch (RemoteException e) {
		}

		String sequencesString = "";
		if (sequences != null && sequences.length > 0) {
			sequencesString = sequences[0].getSequence();
			for (int i = 1; i < sequences.length; i++) {
				sequencesString += "&" + sequences[i].getSequence();
			}
		}
		return sequencesString;
	}

	public String getProxyServices(int serverID) {

		ProxyService proxyServices[] = null;
		try {
			proxyServices = bamDSClient.getProxyServices(serverID);
		} catch (RemoteException e) {
		}

		String proxyServicesString = "";
		if (proxyServices != null && proxyServices.length > 0) {
			proxyServicesString = proxyServices[0].getProxyService();
			for (int i = 1; i < proxyServices.length; i++) {
				proxyServicesString += "&" + proxyServices[i].getProxyService();
			}
		}
		return proxyServicesString;
	}

	/*
	 * public String getEndpointInvokeCount(int serverID, String endpoint) { try { String count =
	 * bamDSClient.getEndpointInvokeCount(serverID, endpoint); //return "&value_a=" + count + "&value_b=0";
	 * return count; } catch (Exception e) { return "0"; } }
	 */
	// public String getLoginsAndFailures(String serverUrl) {
	// int duration = 60 * 60 * 24;
	// String loginAttempts = "0";
	// String loginFailures = "0";
	//
	// try {
	// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
	//
	// loginAttempts = bamDSClient.getServerLoginAttempts(serverUrl, duration);
	// if ((loginAttempts == null) || ("".equals(loginAttempts))) {
	// loginAttempts = "0";
	// }
	//
	// loginFailures = bamDSClient.getServerFailedLoginAttempts(serverUrl,
	// duration);
	// if ((loginFailures == null) || "".equals(loginFailures)) {
	// loginFailures = "0";
	// }
	//
	// } catch (Exception e) {
	// loginAttempts = "0";
	// loginFailures = "0";
	// }
	//
	// return "&value_a=" + loginAttempts + "&value_b=" + loginFailures;
	// }
	//
	// public String getSuccessFailureLoginsByUser(String serverUrl, String
	// userName) {
	// int duration = 60 * 60 * 24;
	// String loginAttempts = "0";
	// String loginFailures = "0";
	//
	// try {
	// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
	//
	// loginAttempts = bamDSClient.getUserLoginAttempts(serverUrl, userName,
	// duration);
	// if ((loginAttempts == null) || "".equals(loginAttempts)) {
	// loginAttempts = "0";
	// }
	//
	// loginFailures = bamDSClient.getUserFailedLoginAttempts(serverUrl,
	// userName, duration);
	// if ((loginFailures == null) || "".equals(loginFailures)) {
	// loginFailures = "0";
	// }
	//
	// } catch (Exception e) {
	// loginAttempts = "0";
	// loginFailures = "0";
	// }
	//
	// return "&value_a=" + loginAttempts + "&value_b=" + loginFailures;
	// }
	//
	// public String getNoOfCalls(String serverUrl, String serviceName) {
	//
	// String callsSystem = "0";
	// String callsService = "0";
	//
	// try {
	// serverUrl = new String(Hex.decodeHex(serverUrl.toCharArray()));
	// callsSystem = bamDSClient.getNumberOfCallsTodaySystem(serverUrl);
	// callsService = bamDSClient.getNumberOfCallsToday(serverUrl, serviceName);
	// } catch (Exception e) {
	// log.debug(e);
	// }
	//
	// if (callsSystem == null) {
	// callsSystem = "0";
	// }
	//
	// if (callsService == null) {
	// callsService = "0";
	// }
	//
	// return "&value_a=" + callsService + "&value_b=" + callsSystem;
	// }
	//
	// getting activity list
	public String getActivityList() {
		try {
			ActivityDTO[] activityList = bamListAdminClient.getActivityList();
			if (activityList != null) {
				return activityArrayToString(activityList, ",", "|");
			}
		} catch (Exception e) {
			log.debug(e);
		}
		return "No Activities Configured";
	}

	private static String activityArrayToString(ActivityDTO[] activityList, String separator1,
			String separator2) {
		String result = "";
		if (activityList != null & activityList.length > 0) {
			result += activityList[0].getActivityID() + separator1 + activityList[0].getName();
			for (int i = 1; i < activityList.length; i++) {

				result += separator2 + activityList[i].getActivityID() + separator1
						+ activityList[i].getName();
			}
		}
		return result;
	}

	public String getServiceResponseTimesOfServer(int serverID, int responseType, boolean demo) {
		/* Data is returned in CSV format */
		/*
		 * service1,service2,service3,service4,service5.... 34,23,22,223,32....
		 */

		if (demo) {
			Random generator = new Random();
			String demoString = "myService1, myService2, myService3, myService4, myService5, myService6\n";
			for (int i = 0; i < 6; i++) {
				double val = generator.nextDouble() * 10.0;
				DecimalFormat df = new DecimalFormat("#.##");
				demoString += df.format(val) + ",";
			}
			return demoString.substring(0, demoString.length() - 1);
		}

		try {
			ServiceDTO[] servicesList = bamListAdminClient.getServicesList(serverID);
			if (servicesList != null) {
				String header = "";
				String values = "";
				if (servicesList.length > 0) {
					for (ServiceDTO service : servicesList) {
						header += service.getName() + ",";
						if (responseType == 0) {
							values += bamDSClient.getAvgResponseTime(service.getID()) + ",";
						} else if (responseType == 1) {
							values += bamDSClient.getMinResponseTime(service.getID()) + ",";
						} else if (responseType == 2) {
							values += bamDSClient.getMaxResponseTime(service.getID()) + ",";
						}
					}
				}
				if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2)
					return "";
				return header.substring(0, header.length() - 1) + "\n"
						+ values.substring(0, values.length() - 1);
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "";
	}

	public String getSequenceInAvgProcessingTimesOfServer(int serverID, boolean demo) {
		/* Data is returned in CSV format */
		/*
		 * service1,service2,service3,service4,service5.... 34,23,22,223,32....
		 */

		if (demo) {
			Random generator = new Random();
			String demoString = "mySequence1, mySequence2, mySequence3, mySequence4, mySequence5, mySequence6\n";
			for (int i = 0; i < 6; i++) {
				double val = generator.nextDouble() * 10.0;
				DecimalFormat df = new DecimalFormat("#.##");
				demoString += df.format(val) + ",";
			}
			return demoString.substring(0, demoString.length() - 1);
		}

		try {
			Sequence[] sequenceList = bamDSClient.getSequences(serverID);
			if (sequenceList != null) {
				String header = "";
				String values = "";
				if (sequenceList.length > 0) {
					for (Sequence sequence : sequenceList) {
						header += sequence.getSequence() + ",";
						values += bamDSClient.getLatestInAverageProcessingTimeForSequenceNoWrap(serverID,
								"SequenceInAvgProcessingTime-" + sequence.getSequence())
								+ ",";
					}
				}
				if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2)
					return "";
				return header.substring(0, header.length() - 1) + "\n"
						+ values.substring(0, values.length() - 1);
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "";
	}

	public String getProxyServiceInAvgProcessingTimesOfServer(int serverID, boolean demo) {
		/* Data is returned in CSV format */
		/*
		 * service1,service2,service3,service4,service5.... 34,23,22,223,32....
		 */

		if (demo) {
			Random generator = new Random();
			String demoString = "myProxyService1, myProxyService2, myProxyService3, myProxyService4, myProxyService5, myProxyService6\n";
			for (int i = 0; i < 6; i++) {
				double val = generator.nextDouble() * 10.0;
				DecimalFormat df = new DecimalFormat("#.##");
				demoString += df.format(val) + ",";
			}
			return demoString.substring(0, demoString.length() - 1);
		}

		try {
			ProxyService[] proxyServiceList = bamDSClient.getProxyServices(serverID);
			if (proxyServiceList != null) {
				String header = "";
				String values = "";
				if (proxyServiceList.length > 0) {
					for (ProxyService proxyService : proxyServiceList) {
						header += proxyService.getProxyService() + ",";
						values += bamDSClient.getLatestInAverageProcessingTimeForProxyNoWrap(serverID,
								"ProxyInAvgProcessingTime-" + proxyService.getProxyService())
								+ ",";
					}
				}
				if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2)
					return "";
				return header.substring(0, header.length() - 1) + "\n"
						+ values.substring(0, values.length() - 1);
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "";
	}

	public String getEndpointInAvgProcessingTimesOfServer(int serverID, boolean demo) {
		/* Data is returned in CSV format */
		/*
		 * service1,service2,service3,service4,service5.... 34,23,22,223,32....
		 */

		if (demo) {
			Random generator = new Random();
			String demoString = "myEndpoint1, myEndpoint2, myEndpoint3, myEndpoint4, myEndpoint5, myEndpoint6\n";
			for (int i = 0; i < 6; i++) {
				double val = generator.nextDouble() * 10.0;
				DecimalFormat df = new DecimalFormat("#.##");
				demoString += df.format(val) + ",";
			}
			return demoString.substring(0, demoString.length() - 1);
		}

		try {
			Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
			if (endpointList != null) {
				String header = "";
				String values = "";
				if (endpointList.length > 0) {
					for (Endpoint endpoint : endpointList) {
						header += endpoint.getEndpoint() + ",";
						values += bamDSClient.getLatestInAverageProcessingTimeForEndpointNoWrap(serverID,
								"EndpointInAvgProcessingTime-" + endpoint.getEndpoint())
								+ ",";
					}
				}
				if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2)
					return "";
				return header.substring(0, header.length() - 1) + "\n"
						+ values.substring(0, values.length() - 1);
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "";
	}

	public String getServerInfo(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level1
		 * name="Service 1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level2
		 * name="Operation 1" count="1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22">
		 * meta1 = Request Count meta2 = Response Count meta3 = Fault Count meta4 = Avg Response Time meta5 =
		 * Min Response Time meta6 = Max Response Time
		 */

		if (demo) {
			StringBuilder result = new StringBuilder();
			int[] maxOps = { 8, 2, 4, 5, 1, 6 };
			String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
			Data data = generateRandomData(1000);
			result.append(String.format(formatString, 0, "http://127.0.0.1:RND", data.getReqCount(), data
					.getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data
					.getMaxTime()));
			result.append(">\n");
			for (int i = 0; i < 6; i++) {
				data = generateRandomData(250);
				result.append(String.format(formatString, 1, String.format("Service %d", i), data
						.getReqCount(), data.getResCount(), data.getFaultCount(), data.getAvgTime(), data
						.getMinTime(), data.getMaxTime()));
				result.append(">\n");
				for (int j = 0; j < maxOps[i]; j++) {
					data = generateRandomData(50);
					result.append(String.format(formatString, 2, String.format("Operation %d", j),
							(j % 2 == 0) ? "0" : data.getReqCount(), data.getResCount(),
							data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
					result
							.append(String.format(" count=\"%s\"/>\n", (j % 2 == 0) ? "0" : data
									.getReqCount()));
				}
				result.append("</level1>\n");
			}
			result.append("</level0>\n");

			return result.toString();
		}

		try {
			StringBuilder result = new StringBuilder();

			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			Data serverData = bamDSClient.getLatestDataForServer(serverID);
			if (serverData != null) {
				result.append("<level0 name=\"").append(serverURL).append("\"");
				result.append(" meta1=\"").append(serverData.getReqCount()).append("\"");
				result.append(" meta2=\"").append(serverData.getResCount()).append("\"");
				result.append(" meta3=\"").append(serverData.getFaultCount()).append("\"");
				result.append(" meta4=\"").append(serverData.getAvgTime()).append("\"");
				result.append(" meta5=\"").append(serverData.getMinTime()).append("\"");
				result.append(" meta6=\"").append(serverData.getMaxTime()).append("\"");
				result.append(">\n");
			}

			ServiceDTO[] servicesList = bamListAdminClient.getServicesList(serverID);
			for (ServiceDTO service : servicesList) {
				result.append("<level1 name=\"").append(service.getName()).append("\"");
				Data serviceData = bamDSClient.getLatestDataForService(service.getID());

				if (serviceData != null) {
					result.append(" meta1=\"").append(serviceData.getReqCount()).append("\"");
					result.append(" meta2=\"").append(serviceData.getResCount()).append("\"");
					result.append(" meta3=\"").append(serviceData.getFaultCount()).append("\"");
					result.append(" meta4=\"").append(serviceData.getAvgTime()).append("\"");
					result.append(" meta5=\"").append(serviceData.getMinTime()).append("\"");
					result.append(" meta6=\"").append(serviceData.getMaxTime()).append("\"");
				}
				result.append(">\n");
				Operation[] operationsList = bamDSClient.getOperations(service.getID());
				for (Operation operation : operationsList) {
					result.append(" <level2 name=\"").append(operation.getName()).append("\"");
					Data operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation
							.getId()));
					if (operationData != null) {
						result.append(" count=\"").append(operationData.getReqCount()).append("\"");
						result.append(" meta1=\"").append(operationData.getReqCount()).append("\"");
						result.append(" meta2=\"").append(operationData.getResCount()).append("\"");
						result.append(" meta3=\"").append(operationData.getFaultCount()).append("\"");
						result.append(" meta4=\"").append(operationData.getAvgTime()).append("\"");
						result.append(" meta5=\"").append(operationData.getMinTime()).append("\"");
						result.append(" meta6=\"").append(operationData.getMaxTime()).append("\"");
					}
					result.append("/>\n");
				}
				result.append("</level1>\n");
			}
			result.append("</level0>\n");
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getServerMediationInfo(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level1
		 * name="Service 1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level2
		 * name="Operation 1" count="1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22">
		 * meta1 = Request Count meta2 = Response Count meta3 = Fault Count meta4 = Avg Response Time meta5 =
		 * Min Response Time meta6 = Max Response Time
		 */

		if (demo) {
			StringBuilder result = new StringBuilder();
			int[] maxOps = { 8, 2, 4, 5, 3, 6 };
			String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
			Data data = generateRandomData(1000);
			result.append(String.format(formatString, 0, "http://127.0.0.1:RND", data.getReqCount(), data
					.getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data
					.getMaxTime()));
			result.append(">\n");
			String[] mediation = { "Endpoint", "Proxy Service", "Sequence" };
			for (int i = 0; i < 3; i++) {
				data = generateRandomData(250);
				result.append(String.format(formatString, 1, mediation[i] + "s", data.getReqCount(), data
						.getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data
						.getMaxTime()));
				result.append(">\n");
				for (int j = 0; j < maxOps[i]; j++) {
					data = generateRandomData(50);
					result.append(String.format(formatString, 2, mediation[i] + String.format(" %d", j), data
							.getReqCount(), data.getResCount(), data.getFaultCount(), data.getAvgTime(), data
							.getMinTime(), data.getMaxTime()));
					result.append(" count=\"1\"/>\n");
				}
				result.append("</level1>\n");
			}
			result.append("</level0>\n");

			return result.toString();
		}

		try {
			StringBuilder result = new StringBuilder();

			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			result.append("<level0 name=\"").append(serverURL).append("\">\n");

			Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
			if (endpointList != null && endpointList.length > 0) {
				result.append("<level1 name=\"").append("Endpoints").append("\">\n");
				for (Endpoint endpoint : endpointList) {
					result.append(" <level2 name=\"").append(endpoint.getEndpoint()).append("\"");
					result.append(" count=\"").append(
							bamDSClient.getLatestInCumulativeCountForEndpoint(serverID,
									"EndpointInCumulativeCount-" + endpoint.getEndpoint())).append("\"");
					result.append(" meta1=\"").append(
							bamDSClient.getLatestInCumulativeCountForEndpoint(serverID,
									"EndpointInCumulativeCount-" + endpoint.getEndpoint())).append("\"");
					result.append(" meta3=\"").append(
							bamDSClient.getLatestInFaultCountForEndpoint(serverID, "EndpointInFaultCount-"
									+ endpoint.getEndpoint())).append("\"");
					result.append(" meta4=\"").append(
							bamDSClient.getLatestInAverageProcessingTimeForEndpointNoWrap(serverID,
									"EndpointInAvgProcessingTime-" + endpoint.getEndpoint())).append("\"");
					result.append(" meta5=\"").append(
							bamDSClient.getLatestInMinimumProcessingTimeForEndpointNoWrap(serverID,
									"EndpointInMinProcessingTime-" + endpoint.getEndpoint())).append("\"");
					result.append(" meta6=\"").append(
							bamDSClient.getLatestInMaximumProcessingTimeForEndpointNoWrap(serverID,
									"EndpointInMaxProcessingTime-" + endpoint.getEndpoint())).append("\"");
					result.append("/>\n");
				}
				result.append("</level1>\n");
			}

			ProxyService[] proxyServiceList = bamDSClient.getProxyServices(serverID);
			if (proxyServiceList != null && proxyServiceList.length > 0) {
				result.append("<level1 name=\"").append("Proxy Services").append("\">\n");
				for (ProxyService proxyService : proxyServiceList) {
					result.append(" <level2 name=\"").append(proxyService.getProxyService()).append("\"");
					result.append(" count=\"").append(
							bamDSClient.getLatestInCumulativeCountForProxy(serverID,
									"ProxyInCumulativeCount-" + proxyService.getProxyService())).append("\"");
					result.append(" meta1=\"").append(
							bamDSClient.getLatestInCumulativeCountForProxy(serverID,
									"ProxyInCumulativeCount-" + proxyService.getProxyService())).append("\"");
					result.append(" meta3=\"").append(
							bamDSClient.getLatestInFaultCountForProxy(serverID, "ProxyInFaultCount-"
									+ proxyService.getProxyService())).append("\"");
					result.append(" meta4=\"").append(
							bamDSClient.getLatestInAverageProcessingTimeForProxyNoWrap(serverID,
									"ProxyInAvgProcessingTime-" + proxyService.getProxyService())).append(
							"\"");
					result.append(" meta5=\"").append(
							bamDSClient.getLatestInMinimumProcessingTimeForProxyNoWrap(serverID,
									"ProxyInMinProcessingTime-" + proxyService.getProxyService())).append(
							"\"");
					result.append(" meta6=\"").append(
							bamDSClient.getLatestInMaximumProcessingTimeForProxyNoWrap(serverID,
									"ProxyInMaxProcessingTime-" + proxyService.getProxyService())).append(
							"\"");
					result.append("/>\n");
				}
				result.append("</level1>\n");
			}

			Sequence[] sequenceList = bamDSClient.getSequences(serverID);
			if (sequenceList != null && sequenceList.length > 0) {
				result.append("<level1 name=\"").append("Sequences").append("\">\n");
				for (Sequence sequence : sequenceList) {
					result.append(" <level2 name=\"").append(sequence.getSequence()).append("\"");
					result.append(" count=\"").append(
							bamDSClient.getLatestInCumulativeCountForSequence(serverID,
									"SequenceInCumulativeCount-" + sequence.getSequence())).append("\"");
					result.append(" meta1=\"").append(
							bamDSClient.getLatestInCumulativeCountForSequence(serverID,
									"SequenceInCumulativeCount-" + sequence.getSequence())).append("\"");
					result.append(" meta3=\"").append(
							bamDSClient.getLatestInFaultCountForSequence(serverID, "SequenceInFaultCount-"
									+ sequence.getSequence())).append("\"");
					result.append(" meta4=\"").append(
							bamDSClient.getLatestInAverageProcessingTimeForSequenceNoWrap(serverID,
									"SequenceInAvgProcessingTime-" + sequence.getSequence())).append("\"");
					result.append(" meta5=\"").append(
							bamDSClient.getLatestInMinimumProcessingTimeForSequenceNoWrap(serverID,
									"SequenceInMinProcessingTime-" + sequence.getSequence())).append("\"");
					result.append(" meta6=\"").append(
							bamDSClient.getLatestInMaximumProcessingTimeForSequenceNoWrap(serverID,
									"SequenceInMaxProcessingTime-" + sequence.getSequence())).append("\"");
					result.append("/>\n");
				}
				result.append("</level1>\n");
			}
			result.append("</level0>\n");

			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	private Data generateRandomData(int num) {
		Random generator = new Random();
		DecimalFormat df1 = new DecimalFormat("##.##");
		DecimalFormat df2 = new DecimalFormat("###");

		Data data = new Data();

		double min = generator.nextDouble() * 10.0;
		double max = generator.nextDouble() * (10.0 - min) + min;
		double avg = generator.nextDouble() * (max - min) + min;

		int fault = generator.nextInt(num);
		int response = generator.nextInt(num) + fault + 1;
		int request = fault + response;

		data.setAvgTime(df1.format(avg));
		data.setMinTime(df1.format(min));
		data.setMaxTime(df1.format(max));

		data.setReqCount(df2.format(request));
		data.setResCount(df2.format(response));
		data.setFaultCount(df2.format(fault));

		return data;
	}

	public String getServiceReqResFaultCountsOfServer(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
		 * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
		 */
		StringBuilder result = new StringBuilder();
		String formatString1 = "<level%d name=\"%s\">\n";
		String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
		String formatString3 = "</level%d>\n";
		if (demo) {
			String[] serviceNames = { "Service 1", "Service 2", "Service 3", "Service 4", "Service 5",
					"Service 6" };
			result.append(String.format(formatString1, 0, "http://127.0.0.1:RND"));
			Data data;
			for (String service : serviceNames) {
				result.append(String.format(formatString1, 1, service));
				data = generateRandomData(250);
				result.append(String.format(formatString2, 2, "Requests", data.getReqCount()));
				result.append(String.format(formatString2, 2, "Responses", data.getResCount()));
				result.append(String.format(formatString2, 2, "Faults", data.getFaultCount()));
				result.append(String.format(formatString3, 1));
			}
			result.append(String.format(formatString3, 0));

			return result.toString();
		}

		try {
			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			result.append(String.format(formatString1, 0, serverURL)); // <level0>
			Data data;
			ServiceDTO[] servicesList = bamListAdminClient.getServicesList(serverID);
			for (ServiceDTO service : servicesList) {
				result.append(String.format(formatString1, 1, service.getName())); // <level1>
				data = bamDSClient.getLatestDataForService(service.getID());
				result.append(String.format(formatString2, 2, "Requests", data.getReqCount())); // <level2/>
				result.append(String.format(formatString2, 2, "Responses", data.getResCount()));// <level2/>
				result.append(String.format(formatString2, 2, "Faults", data.getFaultCount())); // <level2/>
				result.append(String.format(formatString3, 1)); // </level1>
			}
			result.append(String.format(formatString3, 0)); // </level0>
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getSequenceReqResFaultCountsOfServer(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
		 * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
		 */
		StringBuilder result = new StringBuilder();
		String formatString1 = "<level%d name=\"%s\">\n";
		String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
		String formatString3 = "</level%d>\n";
		if (demo) {
			String[] serviceNames = { "Sequence 1", "Sequence 2", "Sequence 3", "Sequence 4", "Sequence 5",
					"Sequence 6" };
			result.append(String.format(formatString1, 0, "http://127.0.0.1:RND"));
			Data data;
			for (String service : serviceNames) {
				result.append(String.format(formatString1, 1, service));
				data = generateRandomData(250);
				result.append(String.format(formatString2, 2, "Requests", data.getReqCount()));
				result.append(String.format(formatString2, 2, "Faults", data.getFaultCount()));
				result.append(String.format(formatString3, 1));
			}
			result.append(String.format(formatString3, 0));

			return result.toString();
		}

		try {
			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			result.append(String.format(formatString1, 0, serverURL)); // <level0>
			Sequence[] sequenceList = bamDSClient.getSequences(serverID);
			for (Sequence sequence : sequenceList) {
				result.append(String.format(formatString1, 1, sequence.getSequence())); // <level1>
				result.append(String.format(formatString2, 2, "Requests", bamDSClient
						.getLatestInCumulativeCountForSequence(serverID, "SequenceInCumulativeCount-"
								+ sequence.getSequence()))); // <level2/>
				result.append(String.format(formatString2, 2, "Faults", bamDSClient
						.getLatestInFaultCountForSequence(serverID, "SequenceInFaultCount-"
								+ sequence.getSequence()))); // <level2/>
				result.append(String.format(formatString3, 1)); // </level1>
			}
			result.append(String.format(formatString3, 0)); // </level0>
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getProxyServiceReqResFaultCountsOfServer(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
		 * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
		 */
		StringBuilder result = new StringBuilder();
		String formatString1 = "<level%d name=\"%s\">\n";
		String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
		String formatString3 = "</level%d>\n";
		if (demo) {
			String[] serviceNames = { "Proxy Service 1", "Proxy Service 2", "Proxy Service 3",
					"Proxy Service 4", "Proxy Service 5", "Proxy Service 6" };
			result.append(String.format(formatString1, 0, "http://127.0.0.1:RND"));
			Data data;
			for (String service : serviceNames) {
				result.append(String.format(formatString1, 1, service));
				data = generateRandomData(250);
				result.append(String.format(formatString2, 2, "Requests", data.getReqCount()));
				result.append(String.format(formatString2, 2, "Faults", data.getFaultCount()));
				result.append(String.format(formatString3, 1));
			}
			result.append(String.format(formatString3, 0));

			return result.toString();
		}

		try {
			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			result.append(String.format(formatString1, 0, serverURL)); // <level0>
			ProxyService[] proxies = bamDSClient.getProxyServices(serverID);
			for (ProxyService proxy : proxies) {
				result.append(String.format(formatString1, 1, proxy.getProxyService())); // <level1>
				result.append(String.format(formatString2, 2, "Requests", bamDSClient
						.getLatestInCumulativeCountForProxy(serverID, "ProxyInCumulativeCount-"
								+ proxy.getProxyService()))); // <level2/>
				result.append(String.format(formatString2, 2, "Faults", bamDSClient
						.getLatestInFaultCountForProxy(serverID, "ProxyInFaultCount-"
								+ proxy.getProxyService()))); // <level2/>
				result.append(String.format(formatString3, 1)); // </level1>
			}
			result.append(String.format(formatString3, 0)); // </level0>
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getEndpointReqResFaultCountsOfServer(int serverID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
		 * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
		 */
		StringBuilder result = new StringBuilder();
		String formatString1 = "<level%d name=\"%s\">\n";
		String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
		String formatString3 = "</level%d>\n";
		if (demo) {
			String[] serviceNames = { "Endpoint 1", "Endpoint 2", "Endpoint 3", "Endpoint 4", "Endpoint 5",
					"Endpoint 6" };
			result.append(String.format(formatString1, 0, "http://127.0.0.1:RND"));
			Data data;
			for (String service : serviceNames) {
				result.append(String.format(formatString1, 1, service));
				data = generateRandomData(250);
				result.append(String.format(formatString2, 2, "Requests", data.getReqCount()));
				result.append(String.format(formatString2, 2, "Faults", data.getFaultCount()));
				result.append(String.format(formatString3, 1));
			}
			result.append(String.format(formatString3, 0));

			return result.toString();
		}

		try {
			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			result.append(String.format(formatString1, 0, serverURL)); // <level0>
			Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
			for (Endpoint endpoint : endpointList) {
				result.append(String.format(formatString1, 1, endpoint.getEndpoint())); // <level1>
				result.append(String.format(formatString2, 2, "Requests", bamDSClient
						.getLatestInCumulativeCountForEndpoint(serverID, "EndpointInCumulativeCount-"
								+ endpoint.getEndpoint()))); // <level2/>
				result.append(String.format(formatString2, 2, "Faults", bamDSClient
						.getLatestInFaultCountForEndpoint(serverID, "EndpointInFaultCount-"
								+ endpoint.getEndpoint()))); // <level2/>
				result.append(String.format(formatString3, 1)); // </level1>
			}
			result.append(String.format(formatString3, 0)); // </level0>
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getActivityInfo(int activityID, boolean demo) {

		if (demo) {
			StringBuilder result = new StringBuilder();
			int[] maxOps = { 8, 2, 4, 5, 3, 6 };
			String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
			Data data = generateRandomData(1000);
			result.append(String.format(formatString, 0, "Activity", data.getReqCount(), data.getResCount(),
					data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
			result.append(">\n");
			for (int i = 0; i < 6; i++) {
				data = generateRandomData(250);
				result.append(String.format(formatString, 1, String.format("Operation %d", i), data
						.getReqCount(), data.getResCount(), data.getFaultCount(), data.getAvgTime(), data
						.getMinTime(), data.getMaxTime()));
				result.append(">\n");
				for (int j = 0; j < maxOps[i]; j++) {
					data = generateRandomData(50);
					result.append(String.format(formatString, 2, String.format("Message %d", j), 0, 0, 0, 0,
							0, 0
					// data.getReqCount(),
							// data.getResCount(),
							// data.getFaultCount(),
							// data.getAvgTime(),
							// data.getMinTime(),
							// data.getMaxTime()
							));
					result.append(" count=\"1\"/>\n");
				}
				result.append("</level1>\n");
			}
			result.append("</level0>\n");

			return result.toString();
		}
		try {
			StringBuilder result = new StringBuilder();

			String activityName = "";
			ActivityDTO[] activityList = bamListAdminClient.getActivityList();
			for (ActivityDTO activityDTO : activityList) {
				if (activityDTO.getActivityID() == activityID) {
					activityName = activityDTO.getName();
				}
			}

			if (activityName.length() > 0) {
				result.append("<level0 name=\"").append(activityName).append("\"");
				result.append(">\n");
			}

			ActivityOperation[] operationList = bamDSClient.getOperationsForActivityID(activityID);
			for (ActivityOperation operation : operationList) {

				result.append("<level1 name=\"").append(operation.getActivityOperationName()).append("\"");
				Data operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation
						.getActivityOperationID()));
				if (operationData != null) {
					result.append(" meta1=\"").append(operationData.getReqCount()).append("\"");
					result.append(" meta2=\"").append(operationData.getResCount()).append("\"");
					result.append(" meta3=\"").append(operationData.getFaultCount()).append("\"");
					result.append(" meta4=\"").append(operationData.getAvgTime()).append("\"");
					result.append(" meta5=\"").append(operationData.getMinTime()).append("\"");
					result.append(" meta6=\"").append(operationData.getMaxTime()).append("\"");
				} else {
					result.append(" meta1=\"").append("0").append("\"");
					result.append(" meta2=\"").append("0").append("\"");
					result.append(" meta3=\"").append("0").append("\"");
					result.append(" meta4=\"").append("0").append("\"");
					result.append(" meta5=\"").append("0").append("\"");
					result.append(" meta6=\"").append("0").append("\"");
				}
				result.append(">\n");
				// getting msgid of each msgs..would be ugly
				Message[] messageList = bamDSClient.getMessagesForOperationIDAndActivityID(Integer
						.parseInt(operation.getActivityOperationID()), activityID);
				for (Message message : messageList) {
					result.append(" <level2 name=\"").append(message.getId()).append("\" count=\"1\"");
					result.append(" meta1=\"").append("Not defined for messages").append("\"");
					result.append(" meta2=\"").append("Not defined for messages").append("\"");
					result.append(" meta3=\"").append("Not defined for messages").append("\"");
					result.append(" meta4=\"").append("Not defined for messages").append("\"");
					result.append(" meta5=\"").append("Not defined for messages").append("\"");
					result.append(" meta6=\"").append("Not defined for messages").append("\"");
					result.append("/>\n");
				}

				/*
				 * //getting msgs counts.. String messageCounts =
				 * bamDSClient.getMessagesCountForOperationIDAndActivityID (Integer
				 * .parseInt(operation.getActivityOperationID()),activityID); result
				 * .append(" <level2 name=\"").append(messageCounts).append ("\" count=\"1\"");
				 * result.append("/>\n");
				 */

				result.append("</level1>\n");
			}
			result.append("</level0>\n");
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	public String getActivityInfoForActivityID(int activityID, boolean demo) {
		try {
			StringBuilder result = new StringBuilder();
			ActivityInfo[] activityInfo = bamDSClient.getActivityInfoForActivityID(activityID);
			if (activityInfo.length > 0) {
				String activityName = activityInfo[0].getActivityName();
				result.append("<level0 name=\"").append(activityName).append("\"");
				result.append(" meta1=\"").append("1").append("\"");
				result.append(" meta2=\"").append("0").append("\"");
				result.append(" meta3=\"").append("0").append("\"");
				result.append(" meta4=\"").append("0").append("\"");
				result.append(" meta5=\"").append("0").append("\"");
				result.append(" meta6=\"").append("0").append("\"");
				result.append(">\n");
				String currentService = "";
				boolean level1Found = false;
				for (int index = 0; index < activityInfo.length; index++) {
					if (!currentService.equals(activityInfo[index].getServiceName())) {
						if (index != 0) {
							result.append("</level1>\n");
						}
						result.append("<level1 name=\"").append(activityInfo[index].getServiceName()).append(
								"\"");
						result.append(" meta1=\"").append("1").append("\"");
						result.append(" meta2=\"").append("0").append("\"");
						result.append(" meta3=\"").append("0").append("\"");
						result.append(" meta4=\"").append("0").append("\"");
						result.append(" meta5=\"").append("0").append("\"");
						result.append(" meta6=\"").append("0").append("\"");
						result.append(">\n");
						level1Found = true;
					}

					currentService = activityInfo[index].getServiceName();
					result.append("<level2 name=\"").append(activityInfo[index].getOperationName()).append(
							"\"");
					result.append(" count=\"").append(activityInfo[index].getMessageCount()).append("\"");
					result.append(" meta1=\"").append("1").append("\"");
					result.append(" meta2=\"").append("0").append("\"");
					result.append(" meta3=\"").append("0").append("\"");
					result.append(" meta4=\"").append("0").append("\"");
					result.append(" meta5=\"").append("0").append("\"");
					result.append(" meta6=\"").append("0").append("\"");
					result.append("/>\n");
				}
				if (level1Found) {
					result.append("</level1>\n");
				}
				result.append("</level0>\n");
				return result.toString();
			}
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}

	/**
	 * Returns Operations for a given service
	 * 
	 * @param serverID
	 *            - Server ID
	 * @param serviceID
	 *            - Service ID
	 * @param demo
	 *            - Flag to indicate Demo data
	 * @return XML contains Operations data
	 */
	public String getOperationsOfService(int serverID, int serviceID, boolean demo) {
		/* data is returned in XML format as follows */
		/*
		 * <level0 name="Server A" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level1
		 * name="Service 1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level2
		 * name="Operation 1" count="1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22">
		 * meta1 = Request Count meta2 = Response Count meta3 = Fault Count meta4 = Avg Response Time meta5 =
		 * Min Response Time meta6 = Max Response Time
		 */

		if (demo) {
			StringBuilder result = new StringBuilder();
			int[] maxOps = { 8 };
			int i = 0;
			String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
			Data data = generateRandomData(1000);
			result.append(String.format(formatString, 0, "http://127.0.0.1:RND", data.getReqCount(), data
					.getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data
					.getMaxTime()));
			result.append(">\n");

			data = generateRandomData(250);
			result.append(String.format(formatString, 1, String.format("Service %d", i), data.getReqCount(),
					data.getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data
							.getMaxTime()));
			result.append(">\n");

			for (int j = 0; j < maxOps[i]; j++) {
				data = generateRandomData(50);
				result.append(String.format(formatString, 2, String.format("Operation %d", j),
						(j % 2 == 0) ? "0" : data.getReqCount(), data.getResCount(), data.getFaultCount(),
						data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
				result.append(String.format(" count=\"%s\"/>\n", (j % 2 == 0) ? "0" : data.getReqCount()));
			}

			result.append("</level1>\n");

			result.append("</level0>\n");

			return result.toString();
		}

		try {
			StringBuilder result = new StringBuilder();

			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}

			Data serverData = bamDSClient.getLatestDataForServer(serverID);
			if (serverData != null) {
				result.append("<level0 name=\"").append(serverURL).append("\"");
				result.append(" meta1=\"").append(serverData.getReqCount()).append("\"");
				result.append(" meta2=\"").append(serverData.getResCount()).append("\"");
				result.append(" meta3=\"").append(serverData.getFaultCount()).append("\"");
				result.append(" meta4=\"").append(serverData.getAvgTime()).append("\"");
				result.append(" meta5=\"").append(serverData.getMinTime()).append("\"");
				result.append(" meta6=\"").append(serverData.getMaxTime()).append("\"");
				result.append(">\n");
			}

			ServiceDTO[] servicesList = bamListAdminClient.getServicesList(serverID);
			for (ServiceDTO service : servicesList) {

				if (service.getID() == serviceID) {

					result.append("<level1 name=\"").append(service.getName()).append("\"");
					Data serviceData = bamDSClient.getLatestDataForService(service.getID());

					if (serviceData != null) {
						result.append(" meta1=\"").append(serviceData.getReqCount()).append("\"");
						result.append(" meta2=\"").append(serviceData.getResCount()).append("\"");
						result.append(" meta3=\"").append(serviceData.getFaultCount()).append("\"");
						result.append(" meta4=\"").append(serviceData.getAvgTime()).append("\"");
						result.append(" meta5=\"").append(serviceData.getMinTime()).append("\"");
						result.append(" meta6=\"").append(serviceData.getMaxTime()).append("\"");
					}
					result.append(">\n");

					Operation[] operationsList = bamDSClient.getOperations(service.getID());

					for (Operation operation : operationsList) {
						result.append(" <level2 name=\"").append(operation.getName()).append("\"");
						Data operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation
								.getId()));
						if (operationData != null) {
							result.append(" count=\"").append(operationData.getReqCount()).append("\"");
							result.append(" meta1=\"").append(operationData.getReqCount()).append("\"");
							result.append(" meta2=\"").append(operationData.getResCount()).append("\"");
							result.append(" meta3=\"").append(operationData.getFaultCount()).append("\"");
							result.append(" meta4=\"").append(operationData.getAvgTime()).append("\"");
							result.append(" meta5=\"").append(operationData.getMinTime()).append("\"");
							result.append(" meta6=\"").append(operationData.getMaxTime()).append("\"");
						}
						result.append("/>\n");
					}

					result.append("</level1>\n");

					// break;
				}
			}

			result.append("</level0>\n");

			return result.toString();

		} catch (Exception e) {
			log.debug(e);
		}

		return "";
	}

	public String getServerWithData(String functionName) {
		try {
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			if (serverList != null) {
				if (serverList.length > 0) {
					String result = "";
					for (MonitoredServerDTO server : serverList) {
						if (functionName.indexOf("getServiceAvgResponseTimesOfServer") > -1) {
							result = getServiceResponseTimesOfServer(server.getServerId(), 0, false);
						} else if (functionName.indexOf("getServiceMaxResponseTimesOfServer") > -1) {
							result = getServiceResponseTimesOfServer(server.getServerId(), 1, false);
						} else if (functionName.indexOf("getServiceMinResponseTimesOfServer") > -1) {
							result = getServiceResponseTimesOfServer(server.getServerId(), 2, false);
						} else if (functionName.indexOf("getServerInfo") > -1) {
							result = getServerInfo(server.getServerId(), false);
						} else if (functionName.indexOf("getServiceReqResFaultCountsOfServer") > -1) {
							result = getServiceReqResFaultCountsOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getSequenceInAvgProcessingTimesOfServer") > -1) {
							result = getSequenceInAvgProcessingTimesOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getEndpointInAvgProcessingTimesOfServer") > -1) {
							result = getEndpointInAvgProcessingTimesOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getProxyServiceInAvgProcessingTimesOfServer") > -1) {
							result = getProxyServiceInAvgProcessingTimesOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getServerMediationInfo") > -1) {
							result = getServerMediationInfo(server.getServerId(), false);
						} else if (functionName.indexOf("getSequenceReqResFaultCountsOfServer") > -1) {
							result = getSequenceReqResFaultCountsOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getProxyServiceReqResFaultCountsOfServer") > -1) {
							result = getProxyServiceReqResFaultCountsOfServer(server.getServerId(), false);
						} else if (functionName.indexOf("getEndpointReqResFaultCountsOfServer") > -1) {
							result = getEndpointReqResFaultCountsOfServer(server.getServerId(), false);
						}

						if (!result.equals("")) {
							return server.getServerId() + "," + server.getServerURL();
						}
					}
				}
			}
		} catch (Exception e) {
			log.debug(e);
		}

		return "-1,http://xxx.xxx.xxx.xxx:xxxx";
	}

	public String getJMXMetricsWindow(int serverID) {
		String resp = "";
		StringBuilder result = new StringBuilder();
		String formatString1 = "<level%d name=\"%s\">\n";
		String formatString2 = "<level%d name=\"%s\" count=\"%s\" key=\"%s\" value=\"%s\">\n";
		String formatString3 = "</level%d>\n";
		try {
			String serverURL = "";
			MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
			for (MonitoredServerDTO monitoredServerDTO : serverList) {
				if (monitoredServerDTO.getServerId() == serverID) {
					serverURL = monitoredServerDTO.getServerURL();
				}
			}
			result.append(String.format(formatString1, 0, serverURL)); // <level0>
			JmxMetricsInfo[] response = bamDSClient.getJMXMetricsWindow(serverID);
			for (int i = 0; i < response.length; i++) {
				result.append(String.format(formatString2, 1, "Attributes", i, response[i].getMxMetricsKey(),
						response[i].getJmxMetricsValue()));
				result.append(String.format(formatString3, 1)); // </level1>

			}
			result.append(String.format(formatString3, 0)); // </level0>
			return result.toString();
		} catch (Exception e) {
			log.debug(e);
		}
		return "";
	}
}
