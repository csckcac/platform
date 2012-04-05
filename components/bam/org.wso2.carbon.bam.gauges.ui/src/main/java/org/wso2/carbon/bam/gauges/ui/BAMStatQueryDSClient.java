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
package org.wso2.carbon.bam.gauges.ui;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.stub.statquery.*;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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

    public BAMStatQueryDSClient(ServletConfig config, HttpSession session, HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "BAMStatQueryDS";
        stub = new BAMStatQueryDSStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public Count[] getLastMinuteRequestCount(int serviceID) throws RemoteException{
        return stub.getLastMinuteRequestCount(serviceID);
    }

    public String getAvgResponseTime(int serviceID) throws BAMException {
        try {
            Time[] times = stub.getAvgResponseTime(serviceID);
            String avgTime = null;
            if (times != null) {
                avgTime = times[0].getTime();
            }
            return avgTime;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getAvgResponseTime -bam.gauges ", e);
        }
    }

    public String getMinResponseTime(int serviceID) throws BAMException {
        try {
            Time[] times = stub.getMinResponseTime(serviceID);
            String minTime = null;
            if (times != null) {
                minTime = times[0].getTime();
            }
            return minTime;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMinResponseTime -bam.gauges ", e);
        }
    }

    public String getMaxResponseTime(int serviceID) throws BAMException {
        try {
            String maxTime = null;
            Time[] times = stub.getMaxResponseTime(serviceID);
            if (times != null) {
                maxTime = times[0].getTime();
            }
            return maxTime;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMaxResponseTime -bam.gauges ", e);
        }
    }


    public String getValueRangePair(String value) {
        int iValue = (int) Double.parseDouble(value);
        int range = (iValue / 10 + 1) * 10; // range in blocks of 10 units
        return "&value=" + value + "&range=" + range + "&";
    }

    // Server data

    public String getLatestAverageResponseTimeForServer(int serverID) throws BAMException {
        String value = "0";
        try {
            Time[] times = stub.getLatestAverageResponseTimeForServer(serverID);
            if (times != null) {
                value = times[0].getTime();
            }
            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestAverageResponseTimeForServer -bam.gauges ", e);
        }
    }

    public String getLatestMaximumResponseTimeForServer(int serverID) throws BAMException {
        String value = "0";
        try {
            Time[] times = stub.getLatestMaximumResponseTimeForServer(serverID);
            if (times != null) {
                value = times[0].getTime();
            }
            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMaximumResponseTimeForServer -bam.gauges ", e);
        }
    }

    public String getLatestMinimumResponseTimeForServer(int serverID) throws BAMException {
        String value = "0";
        try {
            Time[] times = stub.getLatestMinimumResponseTimeForServer(serverID);
            if (times != null) {
                value = times[0].getTime();
            }
            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMinimumResponseTimeForServer -bam.gauges ", e);
        }
    }

    public String getLatestRequestCountForServer(int serverID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestRequestCountForServer(serverID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestRequestCountForServer -bam.gauges ", e);
        }
    }

    public String getLatestResponseCountForServer(int serverID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestResponseCountForServer(serverID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestResponseCountForServer -bam.gauges ", e);
        }
    }

    public String getLatestFaultCountForServer(int serverID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestFaultCountForServer(serverID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestFaultCountForServer -bam.gauges ", e);
        }
    }

    public Data getLatestDataForServer(int serverID) throws BAMException {
        try {
            Data data = null;
            Data[] datas = stub.getLatestDataForServer(serverID);
            if (datas != null) {
                data = datas[0];
            }
            return data;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestDataForServer -bam.gauges ", e);
        }
    }

    // Service data

    public String getLatestAverageResponseTimeForService(int serviceID) throws BAMException {
        String value = "0";
        try {
            Time[] times = stub.getLatestAverageResponseTimeForService(serviceID);
            if (times != null) {
                value = times[0].getTime();
            }

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestAverageResponseTimeForService -bam.gauges ", e);
        }


        return getValueRangePair(value);

    }

    public String getLatestMaximumResponseTimeForService(int serviceID) throws BAMException {

        String value = "0";
        try {
            Time[] times = stub.getLatestMaximumResponseTimeForService(serviceID);
            if (times != null) {
                value = times[0].getTime();
            }
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMaximumResponseTimeForService -bam.gauges ", e);
        }


        return getValueRangePair(value);
    }

    public String getLatestMinimumResponseTimeForService(int serviceID) throws BAMException {
        String value = "0";
        try {
            Time[] times = stub.getLatestMinimumResponseTimeForService(serviceID);
            if (times != null) {
                value = times[0].getTime();
            }

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMinimumResponseTimeForService -bam.gauges ", e);
        }


        return getValueRangePair(value);
    }

    public String getLatestRequestCountForService(int serviceID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestRequestCountForService(serviceID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestRequestCountForService -bam.gauges ", e);
        }
    }

    public String getLatestResponseCountForService(int serviceID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestResponseCountForService(serviceID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestResponseCountForService -bam.gauges ", e);
        }
    }

    public String getLatestFaultCountForService(int serviceID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestFaultCountForService(serviceID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestFaultCountForService -bam.gauges ", e);
        }
    }

    public Data getLatestDataForService(int serviceID) throws BAMException {
        try {
            Data[] datas = stub.getLatestDataForService(serviceID);
            Data data = null;
            if (datas != null) {
                data = datas[0];
            }
            return data;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestDataForService -bam.gauges ", e);
        }
    }

    // Operation data

    public Operation[] getOperations(int serviceID) throws BAMException {
        try {
            return stub.getOperations(serviceID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getOperations -bam.gauges ", e);
        } 
    }

    public String getLatestAverageResponseTimeForOperation(int operationID) throws BAMException {
        try {
            String time = null;
            Time[] times = stub.getLatestAverageResponseTimeForOperation(operationID);
            if (times != null) {
                time = times[0].getTime();
            }
            return time;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestAverageResponseTimeForOperation -bam.gauges ", e);
        }
    }

    public String getLatestMaximumResponseTimeForOperation(int operationID) throws BAMException {
        try {
            String time = null;
            Time[] times = stub.getLatestMaximumResponseTimeForOperation(operationID);
            if (times != null) {
                time = times[0].getTime();
            }
            return time;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMaximumResponseTimeForOperation -bam.gauges ", e);
        }
    }

    public String getLatestMinimumResponseTimeForOperation(int operationID) throws BAMException {
        try {
            String time = null;
            Time[] times = stub.getLatestMinimumResponseTimeForOperation(operationID);
            if (times != null) {
                time = times[0].getTime();
            }
            return time;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestMinimumResponseTimeForOperation -bam.gauges ", e);
        }
    }

    public String getLatestRequestCountForOperation(int operationID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestRequestCountForOperation(operationID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestRequestCountForOperation -bam.gauges ", e);
        }
    }

    public String getLatestResponseCountForOperation(int operationID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestResponseCountForOperation(operationID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestResponseCountForOperation -bam.gauges ", e);
        }
    }

    public String getLatestFaultCountForOperation(int operationID) throws BAMException {
        try {
            String count = null;
            Count[] counts = stub.getLatestFaultCountForOperation(operationID);
            if (counts != null) {
                count = counts[0].getCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestFaultCountForOperation -bam.gauges ", e);
        }
    }

    public Data getLatestDataForOperation(int operationID) throws BAMException {
        try {
            Data data = null;
            Data[] datas = stub.getLatestDataForOperation(operationID);
            if (datas != null) {
                data = datas[0];
            }
            return data;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestDataForOperation -bam.gauges ", e);
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
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInAverageProcessingTimeForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInMaximumProcessingTimeForEndpoint(int serverID, String endpointName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForEndpoint -bam.gauges ", e);
        }


        return getValueRangePair(value);
    }

    public String getLatestInMinimumProcessingTimeForEndpoint(int serverID, String endpointName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }

            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInAverageProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return value;

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInAverageProcessingTimeForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInMaximumProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return value;

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInMinimumProcessingTimeForEndpointNoWrap(int serverID, String endpointName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return value;

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInCumulativeCountForEndpoint(int serverID, String endpointName)
            throws BAMException {
        try {
            String count = null;
            CumulativeCount[] cumulativeCount = stub.getLatestInCumulativeCountForEndpoint(serverID, endpointName);
            if (cumulativeCount != null) {
                count = cumulativeCount[0].getCumulativeCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInCumulativeCountForEndpoint -bam.gauges ", e);
        }
    }

    public String getLatestInFaultCountForEndpoint(int serverID, String endpointName) throws BAMException {
        try {
            String count = null;
            FaultCount[] faultCount = stub.getLatestInFaultCountForEndpoint(serverID, endpointName);
            if (faultCount != null) {
                count = faultCount[0].getFaultCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInFaultCountForEndpoint -bam.gauges ", e);
        }
    }


    // Sequence Data

    public Sequence[] getSequences(int serverID) throws BAMException {
        try {
            return stub.getSequences(serverID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getSequences -bam.gauges ", e);
        }
    }

    public String getLatestInAverageProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInAverageProcessingTimeForSequence -bam.gauges ", e);
        }

    }

    public String getLatestInMaximumProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return getValueRangePair(value);


        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForSequence -bam.gauges ", e);
        }

    }

    public String getLatestInMinimumProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return getValueRangePair(value);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForSequence -bam.gauges ", e);
        }

    }

    public String getLatestInAverageProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInAverageProcessingTimeForSequence -bam.gauges ", e);
        }
    }


    public String getLatestInMaximumProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForSequence -bam.gauges ", e);
        }
    }

    public String getLatestInMinimumProcessingTimeForSequenceNoWrap(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();

            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForSequence -bam.gauges ", e);
        }
    }

    public String getLatestInCumulativeCountForSequence(int serverID, String sequenceName)
            throws BAMException {
        try {
            String count = null;
            CumulativeCount[] cumulativeCount = stub.getLatestInCumulativeCountForSequence(serverID, sequenceName);
            if (cumulativeCount != null) {
               count = cumulativeCount[0].getCumulativeCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInCumulativeCountForSequence -bam.gauges ", e);
        }
    }

    public String getLatestInFaultCountForSequence(int serverID, String sequenceName) throws BAMException {
        try {
            String count = null;
            FaultCount[] faultCounts = stub.getLatestInFaultCountForSequence(serverID, sequenceName);
            if (faultCounts != null) {
                count = faultCounts[0].getFaultCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInFaultCountForSequence -bam.gauges ", e);
        }
    }

    public String getLatestOutAverageProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestOutAverageProcessingTimeForSequence(serverID, sequenceName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutAverageProcessingTimeForSequence -bam.gauges ", e);
        }


    }

    public String getLatestOutMaximumProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestOutMaximumProcessingTimeForSequence(serverID, sequenceName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutMaximumProcessingTimeForSequence -bam.gauges ", e);
        }
    }

    public String getLatestOutMinimumProcessingTimeForSequence(int serverID, String sequenceName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestOutMinimumProcessingTimeForSequence(serverID, sequenceName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutMinimumProcessingTimeForSequence -bam.gauges ", e);
        }

    }

    public String getLatestOutCumulativeCountForSequence(int serverID, String sequenceName)
            throws BAMException {
        try {
            String count = null;
            CumulativeCount[] cumulativeCount = stub.getLatestOutCumulativeCountForSequence(serverID, sequenceName);
            if (cumulativeCount != null) {
                count = cumulativeCount[0].getCumulativeCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutCumulativeCountForSequence -bam.gauges ", e);
        }
    }

    public String getLatestOutFaultCountForSequence(int serverID, String sequenceName) throws BAMException {
        try {
            String count = null;
            FaultCount[] faultCount = stub.getLatestOutFaultCountForSequence(serverID, sequenceName);
            if (faultCount != null) {
                count = faultCount[0].getFaultCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutFaultCountForSequence -bam.gauges ", e);
        }
    }

    // Proxy Data

    public ProxyService[] getProxyServices(int serverID) throws BAMException {
        try {
            return stub.getProxyServices(serverID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getProxyServices -bam.gauges ", e);
        }
    }

    public String getLatestInAverageProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForProxy(serverID, proxyName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getProxyServices -bam.gauges ", e);
        }
    }

    public String getLatestInMaximumProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForProxy -bam.gauges ", e);
        }
    }

    public String getLatestInMinimumProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForProxy -bam.gauges ", e);
        }

    }

    public String getLatestInAverageProcessingTimeForProxyNoWrap(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestInAverageProcessingTimeForProxy(serverID, proxyName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInAverageProcessingTimeForProxy -bam.gauges ", e);
        }
    }

    public String getLatestInMaximumProcessingTimeForProxyNoWrap(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMaximumProcessingTimeForProxy -bam.gauges ", e);
        }
    }

    public String getLatestInMinimumProcessingTimeForProxyNoWrap(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return value;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForProxy -bam.gauges ", e);
        }
    }


    public String getLatestInCumulativeCountForProxy(int serverID, String proxyName) throws BAMException {
        try {
            String count = null;
            CumulativeCount[] cumulativeCount = stub.getLatestInCumulativeCountForProxy(serverID, proxyName);
            if (cumulativeCount != null) {
                count = cumulativeCount[0].getCumulativeCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInMinimumProcessingTimeForProxy -bam.gauges ", e);
        }
    }

    public String getLatestInFaultCountForProxy(int serverID, String proxyName) throws BAMException {
        try {
            String count = null;
            FaultCount[] faultCounts = stub.getLatestInFaultCountForProxy(serverID, proxyName);
            if (faultCounts != null) {
                count = faultCounts[0].getFaultCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInFaultCountForProxy -bam.gauges ", e);
        }
    }

    public String getLatestOutAverageProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            AverageTime[] averageTime = stub.getLatestOutAverageProcessingTimeForProxy(serverID, proxyName);
            if (averageTime != null) {
                value = averageTime[0].getAverageTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestInFaultCountForProxy -bam.gauges ", e);
        }

    }

    public String getLatestOutMaximumProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MaximumTime[] maximumTime = stub.getLatestOutMaximumProcessingTimeForProxy(serverID, proxyName);
            if (maximumTime != null) {
                value = maximumTime[0].getMaximumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutMaximumProcessingTimeForProxy -bam.gauges ", e);
        }

    }

    public String getLatestOutMinimumProcessingTimeForProxy(int serverID, String proxyName)
            throws BAMException {
        String value = "0";
        try {
            MinimumTime[] minimumTime = stub.getLatestOutMinimumProcessingTimeForProxy(serverID, proxyName);
            if (minimumTime != null) {
                value = minimumTime[0].getMinimumTime();
            }
            return getValueRangePair(value);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutMinimumProcessingTimeForProxy -bam.gauges ", e);
        }
    }

    public String getLatestOutCumulativeCountForProxy(int serverID, String proxyName) throws BAMException {
        try {
            String count = null;
            CumulativeCount[] cumulativeCount = stub.getLatestOutCumulativeCountForProxy(serverID, proxyName);
            if (cumulativeCount != null) {
                count = cumulativeCount[0].getCumulativeCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutCumulativeCountForProxy -bam.gauges ", e);
        }
    }

    public String getLatestOutFaultCountForProxy(int serverID, String proxyName) throws BAMException {
        try {
            FaultCount[] faultCount = stub.getLatestOutFaultCountForProxy(serverID, proxyName);
            String count = null;
            if (faultCount != null) {
                count = faultCount[0].getFaultCount();
            }
            return count;
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getLatestOutFaultCountForProxy -bam.gauges ", e);
        }
    }

    /* Activity */

    public String getLatestMaximumOperationsForAnActivityID(int activityID) throws BAMException {
        try {
            String count = null;
            Num[] nums = stub.getMaximumOperationsForAnActivityID(activityID);
            if (nums != null) {
                count = nums[0].getNum();
            }
            return count;

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMaximumOperationsForAnActivityID -bam.gauges ", e);
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
            throws BAMException {
        try {
            return stub.getMessagesForOperationIDAndActivityID(operationID, activityID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMessagesForOperationIDAndActivityID -bam.gauges ", e);
        }
    }


    public ActivityInfo[] getActivityInfoForActivityID(int activityKeyId) throws BAMException {
        try {
            return stub.getActivityInfoForActivityID(activityKeyId);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getActivityInfoForActivityID -bam.gauges ", e);
        }
    }

    public OperationInfo[] getOperationInfoForActivityID(int activityKeyId) throws BAMException {
        try {
            return stub.getOperationInfoForActivityID(activityKeyId);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getOperationInfoForActivityID -bam.gauges ", e);
        }
    }

    public JmxMetricsInfo[] getJMXMetricsWindow(int serverID) throws BAMException {
        try {
            return stub.getJMXMetricsWindow(serverID);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getJMXMetricsWindow -bam.gauges ", e);
        }
    }

    public ClientServiceOperationInfo[] getClientServiceOperation(int serverID) throws BAMException {
        try {
            return stub.getClientServiceOperation(serverID);

        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getClientServiceOperation -bam.gauges ", e);
        }
    }

    public Service[] getServiceForServer(int serverID, String serviceName) throws BAMException {
        try {
            return stub.getServiceForServer(serverID, serviceName);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getServiceForServer -bam.gauges ", e);
        }
    }

    public Activity[] getDetailsForActivity(String activityName) throws BAMException {
        try {

            return stub.getDetailsForActivity(activityName);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getDetailsForActivity -bam.gauges ", e);
        }
    }
    
	public ActivityForTime[] getActivityDetailsForTimeRange(String startTime,
			String endTime, String propertyKey1, String propertyValue1, String propertyKey2, 
			String propertyValue2, String propertyKey3, String propertyValue3)
            throws BAMException {
		try {
			// TODO : pass the key & value parameters
			return stub.getActivityDetailsForTimeRange(startTime, endTime, propertyKey1, propertyValue1, 
					propertyKey2, propertyValue2, propertyKey3, propertyValue3);
		} catch (RemoteException e) {
			throw new BAMException(
					"Error occurred executing getDetailsForActivity -bam.gauges ",
					e);
		}
	}
    
    public ActivityGroupForTime[] getActivityDetailsForActivity(String startTime, String endTime)
            throws BAMException {
		try {
		
			return stub.getActivityDetailsForActivity(startTime, endTime);
		} catch (RemoteException e) {
			throw new BAMException("Error occurred executing getDetailsForActivity -bam.gauges ", e);
		}
	}
    
    public ActivityForServer[] getActivityDetailsForServer(String serverUrl) throws BAMException {
        try {
            return stub.getActivityDetailsForServer(serverUrl);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getActivityDetailsForServer -bam.gauges ", e);
        }
    }

    public PropertyBag[] getpropertyBagForActivity(String activityName) throws BAMException {
        try {
            return stub.getpropertyBagForActivity(activityName);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getpropertyBagForActivity -bam.gauges ", e);
        }
    }

    public FullActivityData[] getAllActivityDataForTimeRange(String satrtTime, String endTime) throws BAMException {
        try {
            return stub.getAllActivityDataForTimeRange(satrtTime, endTime);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getAllActivityDataForTimeRange -bam.gauges ", e);
        }
    }

    public AllMessagesForActivity[] getAllMessagesForTimeRangeAndActivity(String satrtTime, String endTime, int activityId)
            throws BAMException {
        try {
            return stub.getAllMessagesForTimeRangeAndActivity(satrtTime, endTime, activityId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getAllMessagesForTimeRangeAndActivity -bam.gauges ", e);
        }
    }

    public ServerForActivity[] getServerListForActivity() throws BAMException {
        try {
            return stub.getServerListForActivity();
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getServerListForActivity -bam.gauges ", e);
        }
    }
    
    public Property[] getPropertyList(String key) throws BAMException {
        try {
            return stub.getPropertyList(key);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getPropertyList -bam.gauges ", e);
        }
    }
    
    public OperationList[] getOperationNameList(int serviceID) throws BAMException {
        try {
            return stub.getOperationNameList(serviceID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getPropertyList -bam.gauges ", e);
        }
    }
    
    public PropertyKeyForActivity[] getPropertyKeyForActivity(String startTime, String endTime) throws BAMException {
    	try {
            return stub.getPropertyKeyForActivity(startTime, endTime);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getPropertyKeyForActivity -bam.gauges ", e);
        }
    }
    
    public PropertyChildForActivity[] getPropertyChildrenForActivity(String childParam, int activityId) throws BAMException {
        try {
            return stub.getPropertyChildrenForActivity(childParam, activityId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getPropertyChildrenForActivity -bam.gauges ", e);
        }
    }

    public ServiceForServer[] getServiceListForActivity(int serverID) throws BAMException {
        try {

            return stub.getServiceListForActivity(serverID);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getServiceListForActivity -bam.gauges ", e);
        }
    }

    public OperationForService[] getOperationListForActivity(int serviceId) throws BAMException {
        try {
            return stub.getOperationListForActivity(serviceId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getOperationListForActivity -bam.gauges ", e);
        }
    }

    public TimeStampForOperation[] gettimestampForOperation(int operationId) throws BAMException {
        try {
            return stub.gettimestampForOperation(operationId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing gettimestampForOperation -bam.gauges ", e);
        }
    }

    public DirectionForOperation[] getDirectionForOperation(int operationId) throws BAMException {
        try {
            return stub.getDirectionForOperation(operationId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getDirectionForOperation -bam.gauges ", e);
        }
    }

    public MessageForOperation[] getMessagesForOperation(int operationId, String direction, String startTime, String endTime)
            throws BAMException {
        try {
            return stub.getMessagesForOperation(operationId, direction, startTime, endTime);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMessagesForOperation -bam.gauges ", e);
        }
    }

    public MessageForMessageID[] getMessageForMessageID(int messageId) throws BAMException {
        try {
            return stub.getMessageForMessageID(messageId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred executing getMessageForMessageID -bam.gauges ", e);
        }
    }
    
    public Status[] getPropertyValueForStatus(String statusKey, String startTime, String endTime)
            throws BAMException {
		try {
		    return stub.getPropertyValueForStatus(statusKey, startTime, endTime);
		} catch (RemoteException e) {
		    throw new BAMException("Error occurred executing getPropertyValueForStatus -bam.gauges ", e);
		}
	}
    
   public MessageCount[] getMessageCount() throws BAMException {
        try {
            return stub.getMessageCount();
        } catch (RemoteException e) {
            throw new BAMException(
                    "Error occurred executing getMessagesForStatus -bam.gauges ", e);
        }
    	
    }
    

    /**
     * 
     * @param startTime
     * @param endTime
     * @param activityType
     * @param messageType
     * @param messageFormat
     * @param applicationStatus
     * @param technicalStatus
     * @param arcKey
     * @return
     * @throws BAMException
     */
    public MessageId[] getMessagesForStatus(String startTime, String endTime,
    		String server, String service, String operation, String direction, String status,
            String activityType, String messageType, String messageFormat,
            String applicationStatus, String technicalStatus, String arcKey, int startDataset, int endDataset)
            throws BAMException {
        try {
            return stub.getMessagesForStatus(startTime, endTime, server, service, operation, direction, status, activityType,
                    messageType, messageFormat, applicationStatus,
                    technicalStatus, arcKey, startDataset, endDataset);
        } catch (RemoteException e) {
            throw new BAMException(
                    "Error occurred executing getMessagesForStatus -bam.gauges ",
                    e);
        }
    }

    public SAPcount[] getMessagesCountForSAP(String startTime, String endTime,
    		String server, String service, String operation, String direction, String status,
            String activityType, String messageType, String messageFormat,
            String applicationStatus, String technicalStatus, String arcKey)
            throws BAMException {
        try {
            return stub.getMessagesCountForSAP(startTime, endTime, server, service, operation, direction, status, activityType,
                    messageType, messageFormat, applicationStatus,
                    technicalStatus, arcKey);
        } catch (RemoteException e) {
            throw new BAMException(
                    "Error occurred executing getMessagesForStatus -bam.gauges ",
                    e);
        }
    }
    
    public SAPchild[] getChildrenMessagesForSAP(String startTime, String endTime,
    		String server, String service, String operation, String direction, String status,
            String activityType, String messageType, String messageFormat,
            String applicationStatus, String technicalStatus, String arcKey, int activityId,
			int startDataset, int endDataset)
            throws BAMException {
        try {
            return stub.getChildrenMessagesForSAP(startTime, endTime, server, service, operation, direction, status, activityType,
                    messageType, messageFormat, applicationStatus,
                    technicalStatus, arcKey, activityId, startDataset, endDataset);
        } catch (RemoteException e) {
            throw new BAMException(
                    "Error occurred executing getMessagesForStatus -bam.gauges ",
                    e);
        }
    }
    
    public SAPaleaudit[] getAleauditMessagesForSAP(String startTime, String endTime,
    		String server, String service, String operation, String direction, String status,
            String messageType, String messageFormat,
            String applicationStatus, String technicalStatus, String arcKey,
			int startDataset, int endDataset)
            throws BAMException {
        try {
            return stub.getAleauditMessagesForSAP(startTime, endTime, server, service, operation, direction, status,
                    messageType, messageFormat, applicationStatus,
                    technicalStatus, arcKey, startDataset, endDataset);
        } catch (RemoteException e) {
            throw new BAMException(
                    "Error occurred executing getMessagesForStatus -bam.gauges ",
                    e);
        }
    }
    
    
    
       public MessageData[] getMessagesWithXPathValue(String xpathExpression, String value)
               throws org.wso2.carbon.bam.util.BAMException {

        List<MessageData> list = new ArrayList<MessageData>();

        try {

            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            MessageData[] messages = stub.getAllMessages();

            if (messages != null && messages.length > 0) {
                for (MessageData message : messages) {
                    StAXBuilder builder = new StAXOMBuilder(
                            new ByteArrayInputStream(message.getMessageBody().getBytes()));
                    OMElement root = builder.getDocumentElement();

                    List<OMContainer> nodes;

                    if(root != null) {
                        nodes = xpath.selectNodes(root);

                        if(nodes != null && nodes.size() > 0){
                            for (OMContainer node : nodes) {
                                if(node.toString().equals(value)){
                                    list.add(message);
                                    break;
                                }
                            }
                        }
                    }
                }

            }
        } catch (RemoteException e) {
            throw new org.wso2.carbon.bam.util.BAMException("Unable to fetch message list", e);
        } catch (XMLStreamException e) {
            throw new org.wso2.carbon.bam.util.BAMException("Unable to construct message body", e);
        } catch (JaxenException e) {
            throw new org.wso2.carbon.bam.util.BAMException("Unable to construct xpath", e);
        }

        return list.toArray(new MessageData[list.size()]);
    }

    /**
     * Gets server id of BAM_SERVER given the server name. If not present returns a negative value (-1).
     * @param serverName Name of the server to which the server id should be returned.
     * @return
     */
    public int getServerIdForServer(String serverName) throws BAMException {
        try {
            Server[] id = stub.getServerIdForServer(serverName);

            if (id != null && id.length > 0) {
                return id[0].getServerID().intValue();
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve server id for server " + serverName);
        }

        return -1;
    }
    
    public int getCountofChildrenFailedMessages(int activityKeyID) throws BAMException {
        try {
            CountofChildrenFailedMessage[] count = stub.getCountofChildrenFailedMessages(activityKeyID);

            if (count != null && count.length > 0) {
                return Integer.parseInt(count[0].getCount());
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve Count of Children Failed Messages" ,e);
        }

        return -1;
    }

    public int getOriginalFailCount(int activityKeyID) throws BAMException {
        try {
            OriginalFailCount[] count = stub.getOriginalFailCount(activityKeyID);

            if (count != null && count.length > 0) {
                return Integer.parseInt(count[0].getCount());
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve Count of Children Failed Messages" ,e);
        }

        return -1;
    }

    public int getReplayedFailReponseCount(int activityKeyID) throws BAMException {
        try {
            ReplayedFailResponseCount[] count = stub.getReplayedFailReponseCount(activityKeyID);

            if (count != null && count.length > 0) {
                return Integer.parseInt(count[0].getCount());
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve Count of Children Failed Messages" ,e);
        }

        return -1;
    }

    public int getReplayedFailRequestCount(int activityKeyID) throws BAMException {
        try {
            ReplayedFailRequestCount[] count = stub.getReplayedFailRequestCount(activityKeyID);

            if (count != null && count.length > 0) {
                return Integer.parseInt(count[0].getCount());
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve Count of Children Failed Messages" ,e);
        }

        return -1;
    }

    public int getAleauditFailCount(int activityKeyID) throws BAMException {
        try {
               AleauditFailCount[] count = stub.getAleauditFailCount(activityKeyID);
            if (count != null && count.length > 0) {
                return Integer.parseInt(count[0].getCount());
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve Count of Children Failed Messages" ,e);
        }
        
        return -1;
    }

    public void setParentStatus(int activityKeyID) throws BAMException {
       try {
               stub.setParentStatus(activityKeyID);
        } catch (RemoteException e) {
            throw new BAMException("Unable to set parent status" ,e);
        }
    }

}
