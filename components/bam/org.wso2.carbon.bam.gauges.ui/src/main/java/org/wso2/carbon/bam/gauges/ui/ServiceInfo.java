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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.statquery.Data;
import org.wso2.carbon.bam.stub.statquery.Operation;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.SocketException;
import java.rmi.RemoteException;

/**
 * used to get service info for gauges
 */
public class ServiceInfo {

    Log log = LogFactory.getLog(ServiceInfo.class);
    private BAMStatQueryDSClient bamDSClient;
    private BAMListAdminServiceClient bamListAdminClient;

    public ServiceInfo(ServletConfig config, HttpSession session, HttpServletRequest request)
            throws AxisFault, SocketException {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            bamDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());
            bamListAdminClient = new BAMListAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
        }

    }

    public String getServerInfo(int serverID, boolean demo) throws BAMException, RemoteException {
        /* data is returned in XML format as follows */
        /*
        * <level0 name="Server A" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level1
        * name="Service 1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level2
        * name="Operation 1" count="1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22">
        * meta1 = Request Count meta2 = Response Count meta3 = Fault Count meta4 = Avg Response Time meta5 =
        * Min Response Time meta6 = Max Response Time
        */

        if (demo) {
            GaugesUtils gaugesUtils = new GaugesUtils();
            StringBuilder result = new StringBuilder();
            int[] maxOps = {8, 2, 4, 5, 1, 6};
            String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
            Data data = gaugesUtils.generateRandomData(1000);
            result.append(String.format(formatString, 0, "http://127.0.0.1:RND", data.getReqCount(), data
                    .getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
            result.append(">\n");
            for (int i = 0; i < 6; i++) {
                data = gaugesUtils.generateRandomData(250);
                result.append(String.format(formatString, 1, String.format("Service %d", i), data
                        .getReqCount(), data.getResCount(), data.getFaultCount(), data.getAvgTime(), data
                        .getMinTime(), data.getMaxTime()));
                result.append(">\n");
                for (int j = 0; j < maxOps[i]; j++) {
                    data = gaugesUtils.generateRandomData(50);
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


        StringBuilder result = new StringBuilder();

        String serverURL = "";
        if (bamListAdminClient != null) {
            MonitoredServerDTO[] serverList;
            try {
                serverList = bamListAdminClient.getServerList();
            } catch (BAMException e) {
                return "";
            }

            for (MonitoredServerDTO monitoredServerDTO : serverList) {
                if (monitoredServerDTO.getServerId() == serverID) {
                    serverURL = monitoredServerDTO.getServerURL();
                }
            }
        }
        if (bamDSClient != null) {
            Data serverData = null;
            try {
                serverData = bamDSClient.getLatestDataForServer(serverID);
            } catch (BAMException e) {
                throw new BAMException("Unable to get data for server with id" + serverID, e);
            }
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
        }
        if (bamListAdminClient != null) {
            ServiceDO[] servicesList = new ServiceDO[0];
            try {
                servicesList = bamListAdminClient.getServicesList(serverID);
            } catch (BAMException e) {
                return "";
            }
            if (servicesList != null) {
                for (ServiceDO service : servicesList) {
                    result.append("<level1 name=\"").append(service.getName()).append("\"");

                    Data serviceData = null;
                    try {
                        serviceData = bamDSClient.getLatestDataForService(service.getId());
                    } catch (BAMException e) {
                        throw new BAMException("Unable to get data for server with id" + serverID, e);
                    }

                    if (serviceData != null) {
                        result.append(" meta1=\"").append(serviceData.getReqCount()).append("\"");
                        result.append(" meta2=\"").append(serviceData.getResCount()).append("\"");
                        result.append(" meta3=\"").append(serviceData.getFaultCount()).append("\"");
                        result.append(" meta4=\"").append(serviceData.getAvgTime()).append("\"");
                        result.append(" meta5=\"").append(serviceData.getMinTime()).append("\"");
                        result.append(" meta6=\"").append(serviceData.getMaxTime()).append("\"");
                    }
                    result.append(">\n");

                    Operation[] operationsList;
                    try {
                        operationsList = bamDSClient.getOperations(service.getId());
                    } catch (BAMException e) {
                        throw new BAMException("Unable to get operation list for service" + service.getName(), e);
                    }

                    if (operationsList != null) {
                        for (Operation operation : operationsList) {
                            result.append(" <level2 name=\"").append(operation.getName()).append("\"");

                            Data operationData = null;
                            try {
                                operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation
                                        .getId()));
                            } catch (BAMException e) {
                                throw new BAMException("Unable to get operation data for operation with ID: " + operation.getId(), e);
                            }

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
                    }
                    result.append("</level1>\n");
                }
            }
        }
        result.append("</level0>\n");
        return result.toString();

        // return "";
    }

}
