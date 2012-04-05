/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.bam.stub.statquery.Count;
import org.wso2.carbon.bam.stub.statquery.Data;
import org.wso2.carbon.bam.stub.statquery.Endpoint;
import org.wso2.carbon.bam.stub.statquery.Operation;
import org.wso2.carbon.bam.stub.statquery.ProxyService;
import org.wso2.carbon.bam.stub.statquery.Sequence;

import java.rmi.RemoteException;
import java.util.Locale;

public class BAMStatQueryDSClient {
    private static final Log log = LogFactory.getLog(BAMStatQueryDSClient.class);

    BAMStatQueryDSStub stub;

    public BAMStatQueryDSClient(String cookie, String backendServerURL, ConfigurationContext configCtx,
                                Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "BAMStatQueryDS";
        stub = new BAMStatQueryDSStub(configCtx, serviceURL);
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
        } catch (Exception ignore) {
        }

        return getValueRangePair(value);
    }

    public String getLatestMaximumResponseTimeForServer(int serverID) throws RemoteException {
        String value = "0";
        try {
            value = stub.getLatestMaximumResponseTimeForServer(serverID)[0].getTime();
        } catch (Exception ignore) {
        }

        return getValueRangePair(value);
    }

    public String getLatestMinimumResponseTimeForServer(int serverID) throws RemoteException {
        String value = "0";
        try {
            value = stub.getLatestMinimumResponseTimeForServer(serverID)[0].getTime();
        } catch (Exception ignore) {
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

            if (stub.getLatestDataForServer(serverID) == null) {
                return null;
            } else {
                return stub.getLatestDataForServer(serverID)[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Service data

    public String getLatestAverageResponseTimeForService(int serviceID) throws RemoteException {
        String value = "0";
        try {
            value = stub.getLatestAverageResponseTimeForService(serviceID)[0].getTime();
        } catch (Exception ignore) {
        }

        return getValueRangePair(value);

    }

    public String getLatestMaximumResponseTimeForService(int serviceID) throws RemoteException {

        String value = "0";
        try {
            value = stub.getLatestMaximumResponseTimeForService(serviceID)[0].getTime();
        } catch (Exception ignore) {
        }

        return getValueRangePair(value);
    }

    public String getLatestMinimumResponseTimeForService(int serviceID) throws RemoteException {
        String value = "0";
        try {
            value = stub.getLatestMinimumResponseTimeForService(serviceID)[0].getTime();
        } catch (Exception ignore) {
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
            if (stub.getLatestDataForService(serviceID) == null) {
                return null;
            } else {
                return stub.getLatestDataForService(serviceID)[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Operation data

    public Operation[] getOperations(int serviceID) throws RemoteException {
        try {
            return stub.getOperations(serviceID);
        } catch (Exception e) {
            e.printStackTrace();
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
            if (stub.getLatestDataForOperation(operationID) == null) {
                return null;
            } else {
                return stub.getLatestDataForOperation(operationID)[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Endpoint Data

    public Endpoint[] getEndpoints(int serverID) throws RemoteException {
        try {
            return stub.getEndpoints(serverID);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public String getLatestInAverageProcessingTimeForEndpoint(int serverID, String endpointName)throws RemoteException {
        try {
            return stub.getLatestInAverageProcessingTimeForEndpoint(serverID, createMediationKeyString(
                    "Endpoint", "In", "AvgProcessingTime", endpointName))[0].getAverageTime();
        } catch (Exception ignore) {
            return "0";
        }

    }

    public String getLatestInMaximumProcessingTimeForEndpoint(int serverID, String endpointName)throws RemoteException {
        try {
            return stub.getLatestInMaximumProcessingTimeForEndpoint(serverID, createMediationKeyString(
                    "Endpoint", "In", "MaxProcessingTime", endpointName))[0].getMaximumTime();
        } catch (Exception ignore) {
            return "0";
        }
    }

    public String getLatestInMinimumProcessingTimeForEndpoint(int serverID, String endpointName)throws RemoteException {
        try {
            return stub.getLatestInMinimumProcessingTimeForEndpoint(serverID, createMediationKeyString(
                    "Endpoint", "In", "MinProcessingTime", endpointName))[0].getMinimumTime();
        } catch (Exception ignore) {
            return "0";
        }
    }

    public String getLatestInCumulativeCountForEndpoint(int serverID, String endpointName)throws RemoteException {
        try {
            return stub.getLatestInCumulativeCountForEndpoint(serverID, createMediationKeyString("Endpoint",
                    "In", "CumulativeCount", endpointName))[0].getCumulativeCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInFaultCountForEndpoint(int serverID, String endpointName) throws RemoteException {
        try {
            return stub.getLatestInFaultCountForEndpoint(serverID, createMediationKeyString("Endpoint", "In",
                    "FaultCount", endpointName))[0].getFaultCount();
        } catch (Exception e) {
            return "0";
        }
    }

    // Sequence Data

    public Sequence[] getSequences(int serverID) throws RemoteException {
        try {
            return stub.getSequences(serverID);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public String getLatestInAverageProcessingTimeForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInAverageProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "In", "AvgProcessingTime", sequenceName))[0].getAverageTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutAverageProcessingTimeForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInAverageProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "Out", "AvgProcessingTime", sequenceName))[0].getAverageTime();
            // TODO: Rename getLatestInXXXForSequence to getLatestXXXForSequence since the query is common to In and Out direction
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInMaximumProcessingTimeForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInMaximumProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "In", "MaxProcessingTime", sequenceName))[0].getMaximumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutMaximumProcessingTimeForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInMaximumProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "Out", "MaxProcessingTime", sequenceName))[0].getMaximumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInMinimumProcessingTimeForSequence(int serverID, String sequenceName)throws RemoteException {
        try {
            return stub.getLatestInMinimumProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "In", "MinProcessingTime", sequenceName))[0].getMinimumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutMinimumProcessingTimeForSequence(int serverID, String sequenceName)throws RemoteException {
        try {
            return stub.getLatestInMinimumProcessingTimeForSequence(serverID, createMediationKeyString(
                    "Sequence", "Out", "MinProcessingTime", sequenceName))[0].getMinimumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInCumulativeCountForSequence(int serverID, String sequenceName)throws RemoteException {
        try {
            return stub.getLatestInCumulativeCountForSequence(serverID, createMediationKeyString("Sequence",
                    "In", "CumulativeCount", sequenceName))[0].getCumulativeCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutCumulativeCountForSequence(int serverID, String sequenceName)throws RemoteException {
        try {
            return stub.getLatestInCumulativeCountForSequence(serverID, createMediationKeyString("Sequence",
                    "Out", "CumulativeCount", sequenceName))[0].getCumulativeCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInFaultCountForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInFaultCountForSequence(serverID, createMediationKeyString("Sequence", "In",
                    "FaultCount", sequenceName))[0].getFaultCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutFaultCountForSequence(int serverID, String sequenceName) throws RemoteException {
        try {
            return stub.getLatestInFaultCountForSequence(serverID, createMediationKeyString("Sequence", "Out",
                    "FaultCount", sequenceName))[0].getFaultCount();
        } catch (Exception e) {
            return "0";
        }
    }

    // Proxy Data

    public ProxyService[] getProxyServices(int serverID) throws RemoteException {
        try {
            return stub.getProxyServices(serverID);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public String getLatestInAverageProcessingTimeForProxy(int serverID, String proxyName)throws RemoteException {
        try {
            return stub.getLatestInAverageProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "In", "AvgProcessingTime", proxyName))[0].getAverageTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutAverageProcessingTimeForProxy(int serverID, String proxyName)throws RemoteException {
        try {
            return stub.getLatestInAverageProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "Out", "AvgProcessingTime", proxyName))[0].getAverageTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInMaximumProcessingTimeForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInMaximumProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "In", "MaxProcessingTime", proxyName))[0].getMaximumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutMaximumProcessingTimeForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInMaximumProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "Out", "MaxProcessingTime", proxyName))[0].getMaximumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInMinimumProcessingTimeForProxy(int serverID, String proxyName)throws RemoteException {
        try {
            return stub.getLatestInMinimumProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "In", "MinProcessingTime", proxyName))[0].getMinimumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutMinimumProcessingTimeForProxy(int serverID, String proxyName)throws RemoteException {
        try {
            return stub.getLatestInMinimumProcessingTimeForProxy(serverID, createMediationKeyString("Proxy",
                    "Out", "MinProcessingTime", proxyName))[0].getMinimumTime();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInCumulativeCountForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInCumulativeCountForProxy(serverID, createMediationKeyString("Proxy", "In",
                    "CumulativeCount", proxyName))[0].getCumulativeCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutCumulativeCountForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInCumulativeCountForProxy(serverID, createMediationKeyString("Proxy", "Out",
                    "CumulativeCount", proxyName))[0].getCumulativeCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestInFaultCountForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInFaultCountForProxy(serverID, createMediationKeyString("Proxy", "In",
                    "FaultCount", proxyName))[0].getFaultCount();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLatestOutFaultCountForProxy(int serverID, String proxyName) throws RemoteException {
        try {
            return stub.getLatestInFaultCountForProxy(serverID, createMediationKeyString("Proxy", "Out",
                    "FaultCount", proxyName))[0].getFaultCount();
        } catch (Exception e) {
            return "0";
        }
    }

    private String createMediationKeyString(String mediationType, String direction, String dataType, String name) {
        return mediationType + direction + dataType + "-" + name;
    }

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }
}
