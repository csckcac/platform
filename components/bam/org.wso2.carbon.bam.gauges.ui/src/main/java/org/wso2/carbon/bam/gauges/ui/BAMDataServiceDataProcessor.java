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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ActivityDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ClientDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.NamespaceDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.PropertyFilterDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.statquery.Activity;
import org.wso2.carbon.bam.stub.statquery.ActivityForServer;
import org.wso2.carbon.bam.stub.statquery.ActivityForTime;
import org.wso2.carbon.bam.stub.statquery.ActivityGroupForTime;
import org.wso2.carbon.bam.stub.statquery.ActivityInfo;
import org.wso2.carbon.bam.stub.statquery.ActivityOperation;
import org.wso2.carbon.bam.stub.statquery.AllMessagesForActivity;
import org.wso2.carbon.bam.stub.statquery.ClientServiceOperationInfo;
import org.wso2.carbon.bam.stub.statquery.Data;
import org.wso2.carbon.bam.stub.statquery.DirectionForOperation;
import org.wso2.carbon.bam.stub.statquery.Endpoint;
import org.wso2.carbon.bam.stub.statquery.FullActivityData;
import org.wso2.carbon.bam.stub.statquery.JmxMetricsInfo;
import org.wso2.carbon.bam.stub.statquery.Message;
import org.wso2.carbon.bam.stub.statquery.MessageCount;
import org.wso2.carbon.bam.stub.statquery.MessageForMessageID;
import org.wso2.carbon.bam.stub.statquery.MessageForOperation;
import org.wso2.carbon.bam.stub.statquery.MessageId;
import org.wso2.carbon.bam.stub.statquery.Operation;
import org.wso2.carbon.bam.stub.statquery.OperationForService;
import org.wso2.carbon.bam.stub.statquery.OperationInfo;
import org.wso2.carbon.bam.stub.statquery.OperationList;
import org.wso2.carbon.bam.stub.statquery.Property;
import org.wso2.carbon.bam.stub.statquery.PropertyBag;
import org.wso2.carbon.bam.stub.statquery.PropertyChildForActivity;
import org.wso2.carbon.bam.stub.statquery.PropertyKeyForActivity;
import org.wso2.carbon.bam.stub.statquery.ProxyService;
import org.wso2.carbon.bam.stub.statquery.SAPaleaudit;
import org.wso2.carbon.bam.stub.statquery.SAPchild;
import org.wso2.carbon.bam.stub.statquery.SAPcount;
import org.wso2.carbon.bam.stub.statquery.Sequence;
import org.wso2.carbon.bam.stub.statquery.ServerForActivity;
import org.wso2.carbon.bam.stub.statquery.Service;
import org.wso2.carbon.bam.stub.statquery.ServiceForServer;
import org.wso2.carbon.bam.stub.statquery.Status;
import org.wso2.carbon.bam.stub.statquery.TimeStampForOperation;
import org.wso2.carbon.bam.stub.summaryquery.SummaryStat;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.BAMMath;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BAMDataServiceDataProcessor {

    private static final Log log = LogFactory.getLog(BAMDataServiceDataProcessor.class);

    private BAMStatQueryDSClient bamDSClient;
    private BAMListAdminServiceClient bamListAdminClient;
    private BAMSummaryQueryDSClient bamSummaryQueryClient;
    private BAMStatQueryDSClient bamStatQueryDSClient;
    private ServiceInfo serviceInfo;

    public BAMDataServiceDataProcessor(ServletConfig config, HttpSession session,
                                       HttpServletRequest request)
            throws Exception {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        bamDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());
        bamListAdminClient = new BAMListAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
        bamSummaryQueryClient = new BAMSummaryQueryDSClient(cookie, backendServerURL, configContext);
        bamStatQueryDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());
        serviceInfo = new ServiceInfo(config, session, request);
    }

    public String getAdminConsoleUrl(HttpServletRequest request) {
        String data = CarbonUIUtil.getAdminConsoleURL(request);
        // Remove Unnecessary stuff
        data = data.split("/carbon/")[0];
        return data;
    }

    public String getServerList() throws BAMException {
        try {
            MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
            if (serverList != null) {
                return GaugesUtils.serverArrayToString(serverList, ",", "|");
            }
        } catch (Exception e) {
            throw new BAMException("failed to get server list", e);
        }
        return "No Servers Configured";
    }

    public String getServicesList(int serverID) {
        try {

            ServiceDO[] servicesList = bamListAdminClient.getServicesList(serverID);
            if (servicesList != null) {
                return GaugesUtils.serviceArrayToString(servicesList, ",", "|");
            }
        } catch (Exception e) {
            log.debug(e);
        }
        return "No Services Found";
    }

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
        StringBuffer result = new StringBuffer();
        if (activityList != null) {
            if (activityList.length > 0) {

                result.append(activityList[0].getActivityKeyId());
                result.append(separator1);
                result.append(activityList[0].getName());

                for (int i = 1; i < activityList.length; i++) {

                    result.append(separator2);
                    result.append(activityList[i].getActivityKeyId());
                    result.append(separator1);
                    result.append(activityList[i].getName());
                }
            }
        }
        return result.toString();
    }

    public String getServiceResponseTimesOfServer(int serverID, int responseType, boolean demo) {
        try {
            ServiceDO[] servicesList = bamListAdminClient.getServicesList(serverID);
            if (servicesList != null) {
                String header = "";
                StringBuffer values = new StringBuffer();
                if (servicesList.length > 0) {
                    for (ServiceDO service : servicesList) {
                        header += service.getName() + ",";
                        if (responseType == 0) {
                            values.append(bamDSClient.getAvgResponseTime(service.getId()));
                            values.append(",");
                        } else if (responseType == 1) {
                            values.append(bamDSClient.getMinResponseTime(service.getId()));
                            values.append(",");
                        } else if (responseType == 2) {
                            values.append(bamDSClient.getMaxResponseTime(service.getId()));
                            values.append(",");
                        }
                    }
                }
                if (header.equals("") || values.toString().equals("") || values.length() < 2 || header.length() < 2) {
                    return "";
                }
                return header.substring(0, header.length() - 1) + "\n" + values.substring(0, values.length() - 1);
            }
        } catch (Exception e) {
            log.debug(e);
        }

        return "";
    }


    public String getServerWithData(String functionName, BAMMediationDataProcessor processor) throws BAMException {
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

                            result = serviceInfo.getServerInfo(server.getServerId(), false);
                        } else if (functionName.indexOf("getServiceReqResFaultCountsOfServer") > -1) {
                            result = getServiceReqResFaultCountsOfServer(server.getServerId(), false);
                        } else if (functionName.indexOf("getSequenceInAvgProcessingTimesOfServer") > -1) {
                            result = processor.getSequenceInAvgProcessingTimesOfServer(server.getServerId(), false);
                        } else if (functionName.indexOf("getEndpointInAvgProcessingTimesOfServer") > -1) {
                            result = processor.getEndpointInAvgProcessingTimesOfServer(server.getServerId(), false);
                        } else if (functionName.indexOf("getProxyServiceInAvgProcessingTimesOfServer") > -1) {
                            result = processor.getProxyServiceInAvgProcessingTimesOfServer(server.getServerId(), false);
                        } else if (functionName.indexOf("getServerMediationInfo") > -1) {
                            result = processor.getServerMediationInfo(server.getServerId(), false);
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
        } catch (BAMException e) {
            throw new BAMException("error occurred getting server with data for the  " + functionName, e);
        } catch (RemoteException e) {
            throw new BAMException("failed to get server list", e);
        }
        return "-1,http://xxx.xxx.xxx.xxx:xxxx";
    }


    public String getServiceReqResFaultCountsOfServer(int serverID, boolean demo)
            throws BAMException, RemoteException {
        StringBuilder result = new StringBuilder();
        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
        String formatString3 = "</level%d>\n";
        String serverURL = "";
        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
        if (serverList != null) {
            for (MonitoredServerDTO monitoredServerDTO : serverList) {
                if (monitoredServerDTO.getServerId() == serverID) {
                    serverURL = monitoredServerDTO.getServerURL();
                }
            }
        }


        result.append(String.format(formatString1, 0, serverURL)); // <level0>
        Data data;
        ServiceDO[] servicesList = bamListAdminClient.getServicesList(serverID);
        if (servicesList != null) {
            for (ServiceDO service : servicesList) {
                result.append(String.format(formatString1, 1, service.getName())); // <level1>
                data = bamDSClient.getLatestDataForService(service.getId());
                if (data != null) {
                    result.append(String.format(formatString2, 2, "Requests", data.getReqCount())); // <level2/>
                    result.append(String.format(formatString2, 2, "Responses", data.getResCount()));// <level2/>
                    result.append(String.format(formatString2, 2, "Faults", data.getFaultCount())); // <level2/>
                    result.append(String.format(formatString3, 1)); // </level1>
                }
            }
        }
        result.append(String.format(formatString3, 0)); // </level0>
        return result.toString();
        //  return "";
    }

    public String getSequenceReqResFaultCountsOfServer(int serverID, boolean demo)
            throws BAMException, RemoteException {
        /* data is returned in XML format as follows */
        /*
           * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
           * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
           */
        StringBuilder result = new StringBuilder();
        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
        String formatString3 = "</level%d>\n";
        String serverURL = "";
        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
        for (MonitoredServerDTO monitoredServerDTO : serverList) {
            if (monitoredServerDTO.getServerId() == serverID) {
                serverURL = monitoredServerDTO.getServerURL();
                break;
            }
        }

        result.append(String.format(formatString1, 0, serverURL)); // <level0>
        Sequence[] sequenceList = bamDSClient.getSequences(serverID);
        if (sequenceList != null) {
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
        }
        result.append(String.format(formatString3, 0)); // </level0>
        return result.toString();
        //return "";
    }

    public String getProxyServiceReqResFaultCountsOfServer(int serverID, boolean demo)
            throws BAMException, RemoteException {
        /* data is returned in XML format as follows */
        /*
           * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
           * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
           */
        StringBuilder result = new StringBuilder();
        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
        String formatString3 = "</level%d>\n";
        String serverURL = "";
        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
        for (MonitoredServerDTO monitoredServerDTO : serverList) {
            if (monitoredServerDTO.getServerId() == serverID) {
                serverURL = monitoredServerDTO.getServerURL();
            }
        }

        result.append(String.format(formatString1, 0, serverURL)); // <level0>
        ProxyService[] proxies = bamDSClient.getProxyServices(serverID);
        if (proxies != null) {
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
        }
        result.append(String.format(formatString3, 0)); // </level0>
        return result.toString();
        // return "";
    }

    public String getEndpointReqResFaultCountsForAllServers() throws BAMException, RemoteException {

        StringBuilder result = new StringBuilder();
        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d id=\"%d\" name=\"%s\" requests=\"%s\" faults=\"%s\"/>\n";
        String formatString3 = "</level%d>\n";
        result.append(String.format(formatString1, 0, "Servers")); // <level0>
        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();

        if (serverList != null) {
            for (MonitoredServerDTO monitoredServerDTO : serverList) {
                int serverID;
                String serverURL;
                serverID = monitoredServerDTO.getServerId();
                serverURL = monitoredServerDTO.getServerURL();
                Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
                int requests = 0;
                int faults = 0;
                if (endpointList != null) {
                    for (Endpoint endpoint : endpointList) {
                        requests += Integer.parseInt(bamDSClient.getLatestInCumulativeCountForEndpoint(
                                serverID, "EndpointInCumulativeCount-" + endpoint.getEndpoint()));
                        faults += Integer.parseInt(bamDSClient.getLatestInFaultCountForEndpoint(serverID,
                                "EndpointInFaultCount-" + endpoint.getEndpoint()));
                    }
                    result.append(String.format(formatString2, 1, serverID, serverURL, requests, faults)); // <level1>
                }
            }
        }
        result.append(String.format(formatString3, 0)); // <level0>
        return result.toString();
    }

    public String getEndpointReqResFaultCountsOfServer(int serverID, boolean demo)
            throws BAMException, RemoteException {
        /* data is returned in XML format as follows */
        /*
           * <level0 name="Server A"> <level1 name="Service 1"> <level2 name="Requests" count="45"/> <level2
           * name="Responses" count="43"/> <level2 name="Faults" count="2"/>
           */
        StringBuilder result = new StringBuilder();
        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d name=\"%s\" count=\"%s\"/>\n";
        String formatString3 = "</level%d>\n";
        String serverURL = "";
        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
        for (MonitoredServerDTO monitoredServerDTO : serverList) {
            if (monitoredServerDTO.getServerId() == serverID) {
                serverURL = monitoredServerDTO.getServerURL();
            }
        }

        result.append(String.format(formatString1, 0, serverURL)); // <level0>
        Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
        if (endpointList != null) {
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
        }
        result.append(String.format(formatString3, 0)); // </level0>
        return result.toString();
        // return "";
    }

    public String getActivityInfo(int activityKeyId, boolean demo)
            throws BAMException, RemoteException {
        StringBuilder result = new StringBuilder();
        String activityName = "";
        ActivityDTO[] activityList = bamListAdminClient.getActivityList();
        for (ActivityDTO activityDTO : activityList) {
            if (activityDTO.getActivityKeyId() == activityKeyId) {
                activityName = activityDTO.getName();
            }
        }

        if (activityName.length() > 0) {
            result.append("<level0 name=\"").append(activityName).append("\"");
            result.append(">\n");
        }

        ActivityOperation[] operationList = bamDSClient.getOperationsForActivityID(activityKeyId);
        for (ActivityOperation operation : operationList) {

            result.append("<level1 name=\"").append(operation.getActivityOperationName()).append("\"");
            Data operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation.getActivityOperationID()));
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
            Message[] messageList = bamDSClient.getMessagesForOperationIDAndActivityID(Integer.parseInt(operation
                    .getActivityOperationID()), activityKeyId);
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

            result.append("</level1>\n");
        }
        result.append("</level0>\n");
        return result.toString();
        //   return "";
    }

    public String getActivityInfoForActivityID(int activityKeyId) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            ActivityInfo[] activityInfo = bamDSClient.getActivityInfoForActivityID(activityKeyId);
            if (activityInfo != null) {
                String activityName = activityInfo[0].getActivityName();
                result.append("<level0 name=\"").append(activityName).append("\"");
                result.append(">\n");
                for (int index = 0; index < activityInfo.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<ServiceName>");
                    result.append(activityInfo[index].getServiceName());
                    result.append("</ServiceName>\n");
                    result.append("<OperationName>");
                    result.append(activityInfo[index].getOperationName());
                    result.append("</OperationName>\n");
                    result.append("<MessageCount>");
                    result.append(activityInfo[index].getMessageCount());
                    result.append("</MessageCount>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }

                result.append("</level0>\n");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    public String getOperationInfoForActivityID(int activityKeyId) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            OperationInfo[] operationInfo = bamDSClient.getOperationInfoForActivityID(activityKeyId);
            if (operationInfo != null) {
                String activityName = operationInfo[0].getActivityName();
                result.append("<level0 name=\"").append(activityName).append("\"");
                result.append(" meta1=\"").append("1").append("\"");
                result.append(" meta2=\"").append("0").append("\"");
                result.append(" meta3=\"").append("0").append("\"");
                result.append(" meta4=\"").append("0").append("\"");
                result.append(" meta5=\"").append("0").append("\"");
                result.append(" meta6=\"").append("0").append("\"");
                result.append(">\n");
                String currentOperation = "";
                boolean level1Found = false;
                for (int index = 0; index < operationInfo.length; index++) {
                    if (!currentOperation.equals(operationInfo[index].getOperationName())) {
                        if (index != 0) {
                            result.append("</level1>\n");
                        }
                        result.append("<level1 name=\"").append(operationInfo[index].getOperationName()).append("\"");
                        result.append(" meta1=\"").append("1").append("\"");
                        result.append(" meta2=\"").append("0").append("\"");
                        result.append(" meta3=\"").append("0").append("\"");
                        result.append(" meta4=\"").append("0").append("\"");
                        result.append(" meta5=\"").append("0").append("\"");
                        result.append(" meta6=\"").append("0").append("\"");
                        result.append(">\n");
                        level1Found = true;
                    }

                }
                if (level1Found) {
                    result.append("</level1>\n");
                }
                result.append("</level0>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";
    }

    //Get details (operations, services, servers, messages, timestamps) for the given activity

    public String getDetailsForActivity(String activity) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            Activity[] activityInfo = bamDSClient.getDetailsForActivity(activity);
            if (activityInfo != null) {
                result.append("<level0 name=\"").append(activity).append("\"");
                result.append(">\n");
                for (int index = 0; index < activityInfo.length; index++) {
                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<ServerUrl>");
                    result.append(activityInfo[index].getServerUrl());
                    result.append("</ServerUrl>\n");
                    result.append("<ServiceName>");
                    result.append(activityInfo[index].getServiceName());
                    result.append("</ServiceName>\n");
                    result.append("<OperationName>");
                    result.append(activityInfo[index].getName());
                    result.append("</OperationName>\n");
                    result.append("<MessageID>");
                    result.append(activityInfo[index].getMessage_name());
                    result.append("</MessageID>\n");
                    result.append("<TimeStamp>");
                    result.append(activityInfo[index].getTimestamp());
                    result.append("</TimeStamp>\n");
                    result.append("<Direction>");
                    result.append(activityInfo[index].getDirection());
                    result.append("</Direction>\n");
                    result.append("<Status>");
                    result.append(activityInfo[index].getStatus());
                    result.append("</Status>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }

                result.append("</level0>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    //Get details (operations, services, servers, messages, timestamps) for the given time range

    public String getActivityDetailsForTimeRange(String startTime, String endTime,
                                                 String propertyKey1, String propertyValue1,
                                                 String propertyKey2,
                                                 String propertyValue2, String propertyKey3,
                                                 String propertyValue3) throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            ActivityForTime[] activityInfo = bamDSClient.getActivityDetailsForTimeRange(startTime, endTime,
                                                                                        propertyKey1, propertyValue1,
                                                                                        propertyKey2, propertyValue2, propertyKey3, propertyValue3);
            result.append("<level0>");
            if (activityInfo != null) {
                for (int index = 0; index < activityInfo.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<ServerUrl>");
                    result.append(activityInfo[index].getServerUrl());
                    result.append("</ServerUrl>\n");
                    result.append("<ServiceName>");
                    result.append(activityInfo[index].getServiceName());
                    result.append("</ServiceName>\n");
                    result.append("<OperationName>");
                    result.append(activityInfo[index].getName());
                    result.append("</OperationName>\n");
                    result.append("<MessageID>");
                    result.append(activityInfo[index].getMessage_name());
                    result.append("</MessageID>\n");
                    result.append("<TimeStamp>");
                    result.append(activityInfo[index].getTimestamp());
                    result.append("</TimeStamp>\n");
                    result.append("<Direction>");
                    result.append(activityInfo[index].getDirection());
                    result.append("</Direction>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
            }
            result.append("</level0>\n");
            return result.toString();
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
    }

    public String getXpathKeys() throws BAMException {
        StringBuilder result = new StringBuilder();
        List<PropertyFilterDTO> xpaths = new ArrayList<PropertyFilterDTO>();
        try {
            result.append("<level0>");

            MonitoredServerDTO[] servers = bamListAdminClient.getServerList();

            if (servers != null && servers.length > 0) {
                for (MonitoredServerDTO server : servers) {
                    PropertyFilterDTO[] xpathsForServer = bamListAdminClient.
                            getXpathConfigurations(server.getServerId());
                    if (xpathsForServer != null && xpathsForServer.length > 0) {
                        for (PropertyFilterDTO xpathForServer : xpathsForServer) {
                            xpaths.add(xpathForServer);
                            result.append("<xpathKey>");
                            result.append(xpathForServer.getExpressionKey());
                            result.append("</xpathKey>\n");
                        }
                    }
                }
            }

            result.append("</level0>\n");

        } catch (Exception e) {
            throw new BAMException("failed to getXpathKeys", e);
        }

        return result.toString();
    }

    //Get details (operations, services, servers, messages, timestamps) for the given time range grouped by activityId

    public String getActivityDetailsForActivity(String startTime, String endTime)
            throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            ActivityGroupForTime[] activityInfo = bamDSClient.getActivityDetailsForActivity(startTime, endTime);
            if (activityInfo != null) {

                result.append("<level0>");
                for (int index = 0; index < activityInfo.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<ServerUrl>");
                    result.append(activityInfo[index].getServerUrl());
                    result.append("</ServerUrl>\n");
                    result.append("<ServiceName>");
                    result.append(activityInfo[index].getServiceName());
                    result.append("</ServiceName>\n");
                    result.append("<OperationName>");
                    result.append(activityInfo[index].getName());
                    result.append("</OperationName>\n");
                    result.append("<MessageID>");
                    result.append(activityInfo[index].getMessage_name());
                    result.append("</MessageID>\n");
                    result.append("<TimeStamp>");
                    result.append(activityInfo[index].getTimestamp());
                    result.append("</TimeStamp>\n");
                    result.append("<Direction>");
                    result.append(activityInfo[index].getDirection());
                    result.append("</Direction>\n");
                    result.append("<Status>");
                    result.append(activityInfo[index].getStatus());
                    result.append("</Status>\n");
                    result.append("<Activity>");
                    result.append(activityInfo[index].getActivity());
                    result.append("</Activity>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }

                result.append("</level0>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

//Get activity details for a given server

    public String getActivityDetailsForServer(String serverURL) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            ActivityForServer[] activityInfoForServer = bamDSClient.getActivityDetailsForServer(serverURL);

            if (activityInfoForServer != null) {
                result.append("<level0 name=\"").append(serverURL).append("\"");
                result.append(">\n");
                for (int index = 0; index < activityInfoForServer.length; index++) {
                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<TimeStamp>");
                    result.append(activityInfoForServer[index].getTimestamp_activityForServer());
                    result.append("</TimeStamp>\n");
                    result.append("<OperationName>");
                    result.append(activityInfoForServer[index].getOperationName_activityForServer());
                    result.append("</OperationName>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }

                result.append("</level0>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get property bag for the given activity*/

    public String getpropertyBagForActivity(String activity) throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            PropertyBag[] activityInfo = bamDSClient.getpropertyBagForActivity(activity);
            if (activityInfo != null) {

                result.append("<level0 name=\"").append(activity).append("\"");
                result.append(">\n");
                result.append("<Property>");
                result.append(activityInfo[0].getProperties());
                result.append("</Property>\n");
                for (int index = 0; index < activityInfo.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<Message>");
                    result.append(activityInfo[index].getMessageId());
                    result.append("</Message>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }

                result.append("</level0>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get activity property key list*/

    public String getPropertyKeyForActivity(String startTime, String endTime) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            PropertyKeyForActivity[] propertyKey = bamDSClient.getPropertyKeyForActivity(startTime, endTime);
            if (propertyKey != null) {
                result.append("<Parent>\n");
                for (int index = 0; index < propertyKey.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<Property>");
                    result.append(propertyKey[index].getPropertyKey());
                    result.append("</Property>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</Parent>\n");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get activity property key list*/

    public String getPropertyChildrenForActivity(String childParam, int activityId)
            throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            PropertyChildForActivity[] propertyChild = bamDSClient.getPropertyChildrenForActivity(childParam, activityId);
            if (propertyChild != null) {
                result.append("<Child>\n");
                for (int index = 0; index < propertyChild.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<Property>");
                    result.append(propertyChild[index].getPropertyKeyForChild());
                    result.append("</Property>\n");
                    result.append("<MessageId>");
                    result.append(propertyChild[index].getMessageIdForChild());
                    result.append("</MessageId>\n");
                    result.append("<TimeStamp>");
                    result.append(propertyChild[index].getTimestampForChild());
                    result.append("</TimeStamp>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</Child>\n");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get server url list*/

    public String getServerListForActivity() throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            ServerForActivity[] serverDetails = bamDSClient.getServerListForActivity();
            if (serverDetails != null) {

                for (int index = 0; index < serverDetails.length; index++) {
                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<serverUrl>");
                    result.append(serverDetails[index].getServerUrl());
                    result.append("</serverUrl>\n");
                    result.append("<serverId>");
                    result.append(serverDetails[index].getServerId());
                    result.append("</serverId>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get service list for the selected server*/

    public String getServiceListForActivity(int serverID) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            ServiceForServer[] serviceDetails = bamDSClient.getServiceListForActivity(serverID);

            if (serviceDetails != null) {
                result.append("<ActivityServices>");
                for (int index = 0; index < serviceDetails.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<serviceUrl>");
                    result.append(serviceDetails[index].getServiceName());
                    result.append("</serviceUrl>\n");
                    result.append("<serviceId>");
                    result.append(serviceDetails[index].getServiceId());
                    result.append("</serviceId>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</ActivityServices>");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get property values for the given key */

    public String getPropertyList(String key) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            Property[] properties = bamDSClient.getPropertyList(key);

            if (properties != null) {
                result.append("<Properties>");
                for (int index = 0; index < properties.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<value>");
                    result.append(properties[index].getValue());
                    result.append("</value>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</Properties>");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get operation list for the given service */

    public String getOperationNameList(int serviceID) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            OperationList[] operationList = bamDSClient.getOperationNameList(serviceID);

            if (operationList != null) {
                result.append("<Operations>");
                for (int index = 0; index < operationList.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<operationName>");
                    result.append(operationList[index].getOperationName());
                    result.append("</operationName>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</Operations>");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /*Getting status value for given time range and key*/

    public String getPropertyValueForStatus(String statusKey, String startTime, String endTime)
            throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            Status[] status = bamDSClient.getPropertyValueForStatus(statusKey, startTime, endTime);
            if (status != null) {
                result.append("<Status>");
                for (int index = 0; index < status.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<serviceUrl>");
                    result.append(status[index].getStatusValue());
                    result.append("</serviceUrl>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</Status>");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }
        return "";

    }

    public String getMessageCount() throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            MessageCount[] messageId = bamDSClient.getMessageCount();
            if (messageId != null) {
                result.append("<MessageId>");
                for (int index = 0; index < messageId.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<Id>");
                    result.append(messageId[index].getCount());
                    result.append("</Id>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</MessageId>");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }
        return "";


    }

    /**
     * Getting messages for the given status filters.
     *
     * @param startTimeSelection    Optional
     * @param endTimeSelection      Optional
     * @param activityTypeSelection Mandatory
     * @param messageTypeSelection  Mandatory
     * @param expression            Optional - Xpath Expression to be operated on messages.
     * @param value                 Optional - Value to match for xpath expression evaluation. If only
     *                              xpathExpression parameter present fetches messages having any value
     *                              after operating the xpathExpression on them.
     * @param namespaces            Optional - Namespaces and prefixes in the format prefix-1@namespace-1, ... prefix-n@namespace-n
     * @return Non null String containing message details
     * @throws BAMException
     */


    /**
     * Utilty method to process the namespaces.
     *
     * @param namepsaces Should be in the format prefix-1@namespace-1, ... prefix-n@namespace-n. Accepts nulls.
     * @return Zero element Map in case no namespaces are present.
     */
    private static Map<String, String> processNamespaces(String namepsaces) {
        Map<String, String> nsMap = new HashMap<String, String>();

        if (namepsaces != null) {
            String[] nsDefinitions = namepsaces.split(",");

            if (nsDefinitions != null) {
                for (String nsDefinition : nsDefinitions) {
                    String[] tokens = nsDefinition.split("@");

                    if (tokens != null && (tokens.length == 2)) {
                        nsMap.put(tokens[0].trim(), tokens[1].trim());
                    }
                }
            }
        }

        return nsMap;
    }

    /**
     * Utility method to encode the prefixes and uris found in nsArray to
     * prefix_1@uri_1,prefix_2@uri_2,..,prefix_n@uri_n format.
     *
     * @param nsArray Array containing namespace information
     * @return Encoded string in format described above
     */
    private static String encodeNamespaces(NamespaceDTO[] nsArray) {
        if (nsArray != null) {
            StringBuffer namespaces = new StringBuffer();
            for (NamespaceDTO ns : nsArray) {
                namespaces.append(ns.getPrefix() + "@" + ns.getUri());
                namespaces.append(",");
            }

            namespaces.deleteCharAt(namespaces.length() - 1); // Deleting extra ',' at the end of the string

            return namespaces.toString();
        }

        return null;
    }

    /* Get operation list for the selected service*/

    public String getOperationListForActivity(int serviceId) throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            OperationForService[] operationDetails = bamDSClient.getOperationListForActivity(serviceId);
            if (operationDetails != null) {
                result.append("<ActivityOperations>");
                for (int index = 0; index < operationDetails.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<operationName>");
                    result.append(operationDetails[index].getOperationName());
                    result.append("</operationName>\n");
                    result.append("<operationId>");
                    result.append(operationDetails[index].getOperationId());
                    result.append("</operationId>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</ActivityOperations>");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get message timestamp list for the selected operation*/

    public String gettimestampForOperation(int operationId) throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            TimeStampForOperation[] timestampDetails = bamDSClient.gettimestampForOperation(operationId);
            if (timestampDetails != null) {
                result.append("<MessageTimeStamp>");
                for (int index = 0; index < timestampDetails.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<timeStamp>");
                    result.append(timestampDetails[index].getTimestamp());
                    result.append("</timeStamp>\n");
                    result.append("<messageId>");
                    result.append(timestampDetails[index].getMessageId());
                    result.append("</messageId>\n");
                    result.append("</dataSet").append(index).append(">\n");
                }
                result.append("</MessageTimeStamp>");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get message timestamp list for the selected operation*/

    public String getDirectionForOperation(int operationId) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            DirectionForOperation[] directionDetails = bamDSClient.getDirectionForOperation(operationId);
            if (directionDetails != null) {
                result.append("<MessageDirection>");
                for (int index = 0; index < directionDetails.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<direction>");
                    result.append(directionDetails[index].getDirection());
                    result.append("</direction>\n");
                    result.append("<messageId>");
                    result.append(directionDetails[index].getMessageId());
                    result.append("</messageId>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</MessageDirection>");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get message list for the selected operation*/

    public String getMessagesForOperation(int operationId, String direction, String startTime,
                                          String endTime) throws BAMException {


        try {
            StringBuilder result = new StringBuilder();
            MessageForOperation[] messagesForOperation = bamDSClient.getMessagesForOperation(operationId, direction, startTime, endTime);
            if (messagesForOperation != null) {
                result.append("<Messages>");
                for (int index = 0; index < messagesForOperation.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<messageId>");
                    result.append(messagesForOperation[index].getMessageId());
                    result.append("</messageId>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</Messages>");

                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }

    /* Get message for message Id*/

    public String getMessageForMessageID(int messageId) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            MessageForMessageID[] messages = bamDSClient.getMessageForMessageID(messageId);
            if (messages != null) {
                result.append("<Message>");
                for (int index = 0; index < messages.length; index++) {

                    result.append("<dataSet").append(index).append(">\n");
                    result.append("<message>");
                    result.append(messages[index].getMessage());
                    result.append("</message>\n");
                    result.append("<timestamp>");
                    result.append(messages[index].getTimestamp());
                    result.append("</timestamp>\n");
                    result.append("</dataSet").append(index).append(">\n");

                }
                result.append("</Message>");
                return result.toString();
            }
        } catch (Exception e) {
            throw new BAMException("failed to getActivityInfoForActivityID ", e);
        }
        return "";

    }


    /**
     * Returns Operations for a given service
     *
     * @param serverID  - Server ID
     * @param serviceID - Service ID
     * @param demo      - Flag to indicate Demo data
     * @return XML contains Operations data
     */
    public String getOperationsOfService(int serverID, int serviceID, boolean demo)
            throws RemoteException, BAMException {
        /* data is returned in XML format as follows */
        /*
           * <level0 name="Server A" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level1
           * name="Service 1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22"> <level2
           * name="Operation 1" count="1" meta1="25" meta2="12" meta3="13" meta4="22" meta 5="12" meta6="22">
           * meta1 = Request Count meta2 = Response Count meta3 = Fault Count meta4 = Avg Response Time meta5 =
           * Min Response Time meta6 = Max Response Time
           */
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

        ServiceDO[] servicesList = bamListAdminClient.getServicesList(serverID);
        for (ServiceDO service : servicesList) {
            if (service.getId() == serviceID) {

                result.append("<level1 name=\"").append(service.getName()).append("\"");
                Data serviceData = bamDSClient.getLatestDataForService(service.getId());

                if (serviceData != null) {
                    result.append(" meta1=\"").append(serviceData.getReqCount()).append("\"");
                    result.append(" meta2=\"").append(serviceData.getResCount()).append("\"");
                    result.append(" meta3=\"").append(serviceData.getFaultCount()).append("\"");
                    result.append(" meta4=\"").append(serviceData.getAvgTime()).append("\"");
                    result.append(" meta5=\"").append(serviceData.getMinTime()).append("\"");
                    result.append(" meta6=\"").append(serviceData.getMaxTime()).append("\"");
                }
                result.append(">\n");
                Operation[] operationsList = bamDSClient.getOperations(service.getId());

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
    }


    public String getJMXMetricsWindow(int serverID) {
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

    public String getClientList(int serverID) {
        try {
            ClientDTO[] clientList = bamListAdminClient.getClientList(serverID);
            if (clientList != null) {
                return clientArrayToString(clientList, ",", "|");
            }
        } catch (Exception e) {
            log.debug(e);
        }

        return "No Servers Configured";
    }

    private static String clientArrayToString(ClientDTO[] a, String separator1, String separator2) {
        StringBuffer result = new StringBuffer();
        if (a != null) {
            if (a.length > 0) {
                result.append(a[0].getUUID());
                result.append(separator1);
                result.append(a[0].getName());
                // the first element
                for (int i = 1; i < a.length; i++) {
                    result.append(a[0].getUUID());
                    result.append(separator1);
                    result.append(a[0].getName());
                }
            }
        }
        return result.toString();
    }

    public String getClientServiceOperation(int serverID) throws BAMException, RemoteException {

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

        ClientServiceOperationInfo[] clientServiceOperationInfoList = bamDSClient.getClientServiceOperation(serverID);
        for (ClientServiceOperationInfo clientServiceOperationInfo : clientServiceOperationInfoList) {
            result.append(" <level1 name=\"").append(clientServiceOperationInfo.getClientService()).append("\"");
            Service[] serviceList = bamDSClient.getServiceForServer(serverID, clientServiceOperationInfo.getClientService());

            for (Service service : serviceList) {
                Data serviceData = bamDSClient.getLatestDataForService(Integer.parseInt(service.getServiceID()));
                if (serviceData != null) {
                    result.append(" meta1=\"").append(serviceData.getReqCount()).append("\"");
                    result.append(" meta2=\"").append(serviceData.getResCount()).append("\"");
                    result.append(" meta3=\"").append(serviceData.getFaultCount()).append("\"");
                    result.append(" meta4=\"").append(serviceData.getAvgTime()).append("\"");
                    result.append(" meta5=\"").append(serviceData.getMinTime()).append("\"");
                    result.append(" meta6=\"").append(serviceData.getMaxTime()).append("\"");
                    result.append(">\n");
                }

                Operation[] operationsList = bamDSClient.getOperations(Integer.parseInt(service.getServiceID()));
                for (Operation operation : operationsList) {
                    result.append(" <level2 name=\"").append(clientServiceOperationInfo.getClientOperation()).append("\"");
                    Data operationData = bamDSClient.getLatestDataForOperation(Integer.parseInt(operation.getId()));
                    if (operationData != null) {
                        result.append(" count=\"").append(operationData.getReqCount()).append("\"");
                        result.append(" meta1=\"").append(operationData.getReqCount()).append("\"");
                        result.append(" meta2=\"").append(operationData.getResCount()).append("\"");
                        result.append(" meta3=\"").append(operationData.getFaultCount()).append("\"");
                        result.append(" meta4=\"").append(operationData.getAvgTime()).append("\"");
                        result.append(" meta5=\"").append(operationData.getMinTime()).append("\"");
                        result.append(" meta6=\"").append(operationData.getMaxTime()).append("\"");
                        // result.append("/>\n");
                    }
                    result.append("/>\n");
                }
            }
            result.append("</level1>\n");
        }
        result.append("</level0>\n");
        return result.toString();
        //   return "";
    }

    // types : all, service, mediation, jmx, generic

    public String getServersHeirarchy(String type) throws BAMException, RemoteException {

        String nodeOpenFormatString = "<node>";
        String nodeCloseFormatString = "</node>";
        String labelFormatString = "<label>%s</label>";
        String valueFormatString = "<value>%d</value>";
        String childrenOpenFormatString = "<children>";
        String childrenCloseFormatString = "</children>";

        StringBuilder result = new StringBuilder();
        result.append(nodeOpenFormatString);
        result.append(String.format(labelFormatString, "Servers"));
        result.append(String.format(valueFormatString, 0)); // Artificial ID : TODO : FIXME
        result.append(childrenOpenFormatString);

        MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
        if (serverList != null && serverList.length > 0) {

            for (MonitoredServerDTO monitoredServerDTO : serverList) {
                result.append(nodeOpenFormatString);
                result.append(String.format(labelFormatString, monitoredServerDTO.getServerURL()));
                result.append(String.format(valueFormatString, monitoredServerDTO.getServerId()));
                int serverID = monitoredServerDTO.getServerId();
                result.append(childrenOpenFormatString);

                if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("mediation")) {
                    Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
                    result.append(nodeOpenFormatString);
                    result.append(String.format(labelFormatString, "Endpoints"));
                    result.append(String.format(valueFormatString, -2)); // Artificial ID : TODO : FIXME
                    if (endpointList != null && endpointList.length > 0) {
                        result.append(childrenOpenFormatString);
                        int artifialIDtillThisSupported = -2; // Artificial ID : TODO : FIXME
                        for (Endpoint endpoint : endpointList) {
                            result.append(nodeOpenFormatString);
                            result.append(String.format(labelFormatString, endpoint.getEndpoint()));
                            result.append(String.format(valueFormatString, artifialIDtillThisSupported)); // Artificial
                            result.append(nodeCloseFormatString);
                            artifialIDtillThisSupported--; // Artificial ID : TODO : FIXME
                        }
                        result.append(childrenCloseFormatString);
                    }
                    result.append(nodeCloseFormatString);

                    ProxyService[] proxyList = bamDSClient.getProxyServices(serverID);
                    result.append(nodeOpenFormatString);
                    result.append(String.format(labelFormatString, "Proxy Services"));
                    result.append(String.format(valueFormatString, -3)); // Artificial ID : TODO : FIXME
                    if (proxyList != null && proxyList.length > 0) {
                        result.append(childrenOpenFormatString);
                        int artifialIDtillThisSupported = -2; // Artificial ID : TODO : FIXME
                        for (ProxyService proxy : proxyList) {
                            result.append(nodeOpenFormatString);
                            result.append(String.format(labelFormatString, proxy.getProxyService()));
                            result.append(String.format(valueFormatString, artifialIDtillThisSupported)); // Artificial
                            result.append(nodeCloseFormatString);
                            artifialIDtillThisSupported--; // Artificial ID : TODO : FIXME
                        }
                        result.append(childrenCloseFormatString);
                    }
                    result.append(nodeCloseFormatString);

                    Sequence[] sequenceList = bamDSClient.getSequences(serverID);
                    result.append(nodeOpenFormatString);
                    result.append(String.format(labelFormatString, "Sequences"));
                    result.append(String.format(valueFormatString, -4)); // Artificial ID : TODO : FIXME
                    if (sequenceList != null && sequenceList.length > 0) {
                        result.append(childrenOpenFormatString);
                        int artifialIDtillThisSupported = -2; // Artificial ID : TODO : FIXME
                        for (Sequence sequence : sequenceList) {
                            result.append(nodeOpenFormatString);
                            result.append(String.format(labelFormatString, sequence.getSequence()));
                            result.append(String.format(valueFormatString, artifialIDtillThisSupported)); // Artificial
                            result.append(nodeCloseFormatString);
                            artifialIDtillThisSupported--; // Artificial ID : TODO : FIXME
                        }
                        result.append(childrenCloseFormatString);
                    }
                    result.append(nodeCloseFormatString);
                }

                if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("service")) {
                    ServiceDO[] servicesList = bamListAdminClient.getServicesList(serverID);
                    if (servicesList != null && servicesList.length > 0) {
                        for (ServiceDO serviceDTO : servicesList) {
                            result.append(nodeOpenFormatString);
                            result.append(String.format(labelFormatString, serviceDTO.getName()));
                            result.append(String.format(valueFormatString, serviceDTO.getId()));

                            int serviceID = serviceDTO.getId();
                            if (serviceID > -1) {
                                result.append(childrenOpenFormatString);
                                OperationDO[] operationsList = bamListAdminClient.getOperationList(serviceID);
                                for (OperationDO operationDTO : operationsList) {
                                    result.append(nodeOpenFormatString);
                                    result.append(String.format(labelFormatString, operationDTO.getName()));
                                    result.append(String.format(valueFormatString, operationDTO.getOperationID()));
                                    result.append(nodeCloseFormatString);
                                }
                                result.append(childrenCloseFormatString);
                            }
                            result.append(nodeCloseFormatString);
                        }
                    }
                }

                result.append(childrenCloseFormatString);
                result.append(nodeCloseFormatString);
            }
        }

        result.append(childrenCloseFormatString);
        result.append(nodeCloseFormatString);
        return result.toString();

    }

    public String getMediationSummaries(String categoryType, String summaryType, int categoryID,
                                        String timeStart, String timeEnd) throws Exception {
        String rootOpenFormatString = "<level0>";
        String rootCloseFormatString = "</level0>";
        String xFormatString = "<x count=\"%s\" name=\"%s\"/>";
        String yFormatString = "<y count=\"%s\" />";
        String setOpenFormatString = "<set>";
        String setCloseFormatString = "</set>";
        String dataOpenFormatString = "<data name=\"%s\">";
        String dataCloseFormatString = "</data>";
        StringBuilder result = new StringBuilder();

        if (categoryType.equalsIgnoreCase("AllServers")) {

        } else if (categoryType.equalsIgnoreCase("Server")) {
            MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();

            if (serverList != null) {
                for (MonitoredServerDTO server : serverList) {
                    if (server != null) {
                        if (server.getServerId() == categoryID) {
                            SummaryStat[] summaryStats = null;
                            if (summaryType.equalsIgnoreCase("hour")) {
                                summaryStats = bamSummaryQueryClient.getServerStatHourlySummaries(server
                                        .getServerId(), BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                            } else if (summaryType.equalsIgnoreCase("day")) {
                                summaryStats = bamSummaryQueryClient.getServerStatDailySummaries(server
                                        .getServerId(), BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                            } else if (summaryType.equalsIgnoreCase("month")) {
                                summaryStats = bamSummaryQueryClient.getServerStatMonthlySummaries(server
                                        .getServerId(), BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                            } else if (summaryType.equalsIgnoreCase("quater")) {
                                summaryStats = bamSummaryQueryClient.getServerStatQuarterlySummaries(server
                                        .getServerId(), BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                            } else if (summaryType.equalsIgnoreCase("year")) {
                                summaryStats = bamSummaryQueryClient.getServerStatYearlySummaries(server
                                        .getServerId(), BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                            }

                            result.append(rootOpenFormatString);
                            if (summaryStats != null) {
                                result.append(String.format(dataOpenFormatString, "Requests"));
                                int i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatReqCount = summaryStat.getReqCount();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatReqCount));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);

                                result.append(String.format(dataOpenFormatString, "Responses"));
                                i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatResTime = summaryStat.getReqCount();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatResTime));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);
                                result.append(String.format(dataOpenFormatString, "Faults"));

                                i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatFault = summaryStat.getFaultCount();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatFault));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);
                                result.append(String.format(dataOpenFormatString, "Average Processing Time"));
                                i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatAvgTime = summaryStat.getAvgResTime();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatAvgTime));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);
                                result.append(String.format(dataOpenFormatString, "Minimum Processing Time"));
                                i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatMinTime = summaryStat.getMinResTime();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatMinTime));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);
                                result.append(String.format(dataOpenFormatString, "Maximum Processing Time"));
                                i = 0;
                                for (SummaryStat summaryStat : summaryStats) {
                                    if (summaryStat != null) {
                                        result.append(setOpenFormatString);
                                        String summaryStatsTimeStamp = summaryStat.getTimestamp();
                                        String summaryStatMaxTime = summaryStat.getMaxResTime();
                                        result.append(String.format(xFormatString, Integer.toString(i), summaryStatsTimeStamp));
                                        result.append(String.format(yFormatString, summaryStatMaxTime));
                                        result.append(setCloseFormatString);
                                        i++;
                                    }
                                }
                                result.append(dataCloseFormatString);
                            }
                            result.append(rootCloseFormatString);
                            return result.toString();
                        }
                    }
                }
            }
        } else if (categoryType.equalsIgnoreCase("AllEndpoints")) {
            if (summaryType.equalsIgnoreCase("hour")) {
            } else if (summaryType.equalsIgnoreCase("day")) {
            } else if (summaryType.equalsIgnoreCase("month")) {
            } else if (summaryType.equalsIgnoreCase("quoter")) {
            } else if (summaryType.equalsIgnoreCase("year")) {

            }
        }
        return "";
    }

    public String getMediationRealTimeFaultStat_temp(String categoryType, int serverId,
                                                     String mediationName, String cacheId)
            throws BAMException {
/*        String formatString1 = "<level%d name=\"%s\">\n";
        String formatString2 = "<level%d name=\"%s\">\n";
        String formatString3 = "<stat name=\"%s\" count=\"%d\" />\n";
        String formatString4 = "</level%d>\n";
        StringBuilder result = new StringBuilder();

        try {
            String serverURL = "";
            MonitoredServerDTO[] serverList = bamListAdminClient.getServerList();
            if (serverList != null) {
                for (MonitoredServerDTO monitoredServerDTO : serverList) {
                    if (monitoredServerDTO.getServerId() == serverId) {
                        serverURL = monitoredServerDTO.getServerURL();
                    }
                }
                result.append(String.format(formatString1, 0, serverURL)); // <level0>
                MediationFaultStatList stat = bamStatQueryAdminServiceClient.getMediationRealTimeFaultStat_temp(categoryType, serverId, mediationName, cacheId);
                if (stat != null) {
                    MediationFaultStat[] statList = stat.getFaultList();
                    if (statList != null) {
                        for (MediationFaultStat faultStat : statList) {
                            if (faultStat != null) {
                                int faultCount = faultStat.getFaultCount();
                                String faultName = faultStat.getName();

                                if ((faultName == null) || (faultName.equals("")) || (faultName.equals("null"))) {
                                    faultName = "Other";
                                }
                                String statName = "Faults";
                                result.append(String.format(formatString2, 1, faultName)); // <level1>
                                result.append(String.format(formatString3, statName, faultCount)); // <stat />
                                result.append(String.format(formatString4, 1)); // </level1>
                            }
                        }
                    }

                }

                result.append(String.format(formatString4, 0)); // </level0>
            }
            return result.toString();

        } catch (Exception e) {
            throw new BAMException("failed to get server list ", e);
        }*/
        return "";
    }

    public String getAllActivityDataForTimeRange(String startTime, String endTime)
            throws BAMException {
        FullActivityData[] fullActivityDatas = bamDSClient.getAllActivityDataForTimeRange(startTime, endTime);
        StringBuffer result = new StringBuffer();
        if (fullActivityDatas != null) {
            for (FullActivityData activityData : fullActivityDatas) {
                result.append(activityData.getActivityDataId());
                result.append(",");
                result.append(activityData.getActivityDataName());
                result.append("|");
            }
        }
        return result.toString();
    }

    public String getAllMessagesForTimeRangeAndActivity(String startTime, String endTime,
                                                        int activityID) throws BAMException {
        AllMessagesForActivity[] messagesForActivities = bamDSClient.getAllMessagesForTimeRangeAndActivity(startTime, endTime, activityID);
        StringBuffer result = new StringBuffer();
        if (messagesForActivities != null) {
            for (AllMessagesForActivity messagesForActivity : messagesForActivities) {
                result.append(messagesForActivity.getActivityMessage());
                result.append("|");
            }
        }
        return result.toString();
    }

    public String getMessagesForStatus(String startTimeSelection, String endTimeSelection,
                                       String server, String service,
                                       String operation, String direction, String status,
                                       String activityTypeSelection, String messageTypeSelection,
                                       String messageFormatSelection,
                                       String applicationStatusSelection,
                                       String technicalStatusSelection, String messageGUID,
                                       String expression, String value, String namespaces,
                                       int startDataset, int endDataset) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            // messageGUID is the arcKey
            MessageId[] messageIds = bamDSClient.getMessagesForStatus(
                    startTimeSelection, endTimeSelection, server, service,
                    operation, direction, status, activityTypeSelection,
                    messageTypeSelection, messageFormatSelection,
                    applicationStatusSelection, technicalStatusSelection,
                    messageGUID, startDataset, endDataset);
            List<MessageId> idsForXPathList = new ArrayList<MessageId>();

            if (expression != null && !expression.equals("")) {

                MessageId[] idsForXPath;
                AXIOMXPath xpath = new AXIOMXPath(expression);
                Map<String, String> nsMap = processNamespaces(namespaces);

                for (String prefix : nsMap.keySet()) {
                    xpath.addNamespace(prefix, nsMap.get(prefix));
                }

                for (int index = 0; index < messageIds.length; index++) {
                    MessageForMessageID[] messages = bamDSClient
                            .getMessageForMessageID(Integer
                                    .parseInt(messageIds[index].getMessageId()));

                    if (messages != null && messages.length > 0
                            && messages[0].getMessage() != "") {
                        StAXBuilder builder = new StAXOMBuilder(
                                new ByteArrayInputStream(messages[0]
                                        .getMessage().getBytes()));
                        OMElement root = builder.getDocumentElement();

                        List nodes;

                        if (root != null) {
                            nodes = xpath.selectNodes(root);

                            if (nodes != null && nodes.size() > 0) {
                                for (Object node : nodes) {

                                    OMElement element = null;
                                    OMAttribute attribute = null;
                                    if (node instanceof OMDocument) {
                                        element = ((OMDocument) node)
                                                .getOMDocumentElement();
                                    } else if (node instanceof OMElement) {
                                        element = (OMElement) node;
                                    } else if (node instanceof OMAttribute) {
                                        attribute = (OMAttribute) node;
                                    }

                                    if (value != null && !value.equals("")) {
                                        String evaluation = null;
                                        String lineBreak = System.getProperty("line.separator");

                                        if (element != null) {
                                            if (element.getChildElements()
                                                    .hasNext()) {
                                                // This is a top level element
                                                evaluation = element.toString().replace(lineBreak, "");
                                            } else {
                                                // This is a element with a text value
                                                evaluation = element.getText().replace(lineBreak, "");
                                            }
                                        } else if (attribute != null) {
                                            evaluation = attribute.getLocalName().replace(lineBreak, "");
                                        }

                                        evaluation = evaluation.trim();
                                        value = value.replace(lineBreak, "");
                                        if (evaluation.equals(value.trim())) {
                                            idsForXPathList.add(messageIds[index]);
                                            break;
                                        }
                                    } else {
                                        idsForXPathList.add(messageIds[index]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                idsForXPath = idsForXPathList
                        .toArray(new MessageId[idsForXPathList.size()]);
                messageIds = idsForXPath;
            }

            if (messageIds != null && messageIds.length > 0) {
                result.append("<Messages>");

                String messageId = "";
                String messageDirection = "";
                String messageUUID = "";
                String messageStatus = "";
                String messageTimestamp = "";
                String activityType = "";
                String applicationFailure = "";
                String technicalFailure = "";
                String messageFormat = "";
                String messageType = "";
                String serviceName = "";
                String arcKey = "";
                String activityId = "";
                boolean parentRecord = false;
                MessageId message = null;
                for (int index = 0; index < messageIds.length; index++) {
                    // for a given messageId, there are multiple rows having
                    // different property keys & values.
                    // we are trying to accumulate all the keys/values belonging
                    // to a single message into a
                    // row model here. (for additional explanation on the
                    // problem
                    // see - http://markmail.org/thread/i24sllyzksw2cfho)
                    message = messageIds[index];
                    if (!messageId.equals(message.getMessageId())) {
                        if (!("".equals(messageId))) {
                            // dump the collected set of variables into XML
                            // format
                            dumpXML(result, messageDirection, messageUUID,
                                    messageStatus, messageTimestamp,
                                    activityType, applicationFailure,
                                    technicalFailure, messageFormat,
                                    messageType, serviceName, arcKey,
                                    parentRecord, index, messageIds[index - 1]);
                        }
                        activityType = "";
                        applicationFailure = "";
                        technicalFailure = "";
                        messageFormat = "";
                        messageType = "";
                        arcKey = "";
                        parentRecord = false;

                        // repeated with same value for all the rows. So getting
                        // assigned from the values in the
                        // first row is sufficient
                        messageId = message.getMessageId();
                        messageDirection = message.getMessageDirection();
                        messageUUID = message.getMessageUUID();
                        messageStatus = message.getStatus();
                        messageTimestamp = message.getTimestamp();
                        serviceName = message.getServiceName();
                        activityId = message.getActivityId();
                    }

                    if (("activity_type").equals(message.getKey())) {
                        activityType = message.getValue();
                    } else if ("application_failure".equals(message.getKey())) {
                        applicationFailure = message.getValue();
                    } else if ("technical_failure".equals(message.getKey())) {
                        technicalFailure = message.getValue();
                    } else if ("message_format".equals(message.getKey())) {
                        messageFormat = message.getValue();
                    } else if ("message_type".equals(message.getKey())) {
                        messageType = message.getValue();
                    } else if ("arc_key".equals(message.getKey())) {
                        arcKey = message.getValue();
                    } else if (message.getKey().contains("parent")) {
                        parentRecord = true;
                    }
                    // MessageForMessageID[] messages = bamDSClient
                    // .getMessageForMessageID(Integer
                    // .parseInt(messageId[index].getMessageId()));
                    // System.out.println(messages[0].getMessage());
                }
                // Not forgetting the last message block
                dumpXML(result, messageDirection, messageUUID, messageStatus,
                        messageTimestamp, activityType, applicationFailure,
                        technicalFailure, messageFormat, messageType,
                        serviceName, arcKey, parentRecord,
                        (messageIds.length - 1), message);

                if (activityResult != null) {
                    result.append(activityResult);
                }

                result.append("</Messages>");

            }

            if (log.isDebugEnabled()) {
                log.debug("List of filtered messages are :" + result.toString());
            }
            return result.toString();
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }

    }

    /**
     * Utility method to dump collected statistics to a XML format to be
     * consumed at the gadget level.
     *
     * @param result
     * @param messageDirection
     * @param messageUUID
     * @param messageStatus
     * @param messageTimestamp
     * @param activityType
     * @param applicationFailure
     * @param messageFormat
     * @param messageType
     * @param index
     * @param message
     */
    String lastArcKey = "";
    StringBuilder activityResult = new StringBuilder();
    StringBuilder activityParentIdArr = new StringBuilder();
    StringBuilder activityChildIdArr = new StringBuilder();

    private void dumpXML(StringBuilder result, String messageDirection,
                         String messageUUID, String messageStatus, String messageTimestamp,
                         String activityType, String applicationFailure,
                         String technicalFailure, String messageFormat, String messageType,
                         String serviceName, String arcKey, boolean parentRecord, int index,
                         MessageId message) throws BAMException {

        int failMessageCount;
        String msgStatus = "";

        int activityID = Integer.parseInt(message.getActivityId());

        try {

            failMessageCount = getCountofChildrenFailedMessages(activityID);
            if (failMessageCount > 0) {
                int originalFail = bamDSClient.getOriginalFailCount(activityID);
                int replayedResponseFailed = bamDSClient.getReplayedFailReponseCount(activityID);
                int replayedRequestFailed = bamDSClient.getReplayedFailRequestCount(activityID);

                int error = originalFail + replayedResponseFailed - replayedRequestFailed;
                if (error != 0) {
                    int aleauditFailed = bamDSClient.getAleauditFailCount(activityID);
                    if (aleauditFailed != 0) {
                        bamDSClient.setParentStatus(activityID);
                        messageStatus = "Fail";
                    }
                }

            }

        } catch (Exception e) {
            throw new BAMException("failed to getCountofChildrenFailedMessages ", e);

        } finally {


        }

        /*
         * To sort the messages having the parent message on the top and having
         * the corresponding children messages below that.
         */
        if (!arcKey.equals(lastArcKey)) {

            result.append(activityResult);
            activityResult = new StringBuilder();
        }

        if (parentRecord) {
            result.append("<dataSet" + index + ">\n");
            result.append("<MessageId>");
            result.append(message.getMessageId());
            result.append("</MessageId>\n");

            result.append("<ActivityId>");
            result.append(message.getActivityId());
            result.append("</ActivityId>\n");

            result.append("<ActivityType>");
            result.append(activityType);
            result.append("</ActivityType>\n");

            result.append("<ApplicationFailure>");
            result.append(applicationFailure);
            result.append("</ApplicationFailure>\n");

            result.append("<TechnicalFailure>");
            result.append(technicalFailure);
            result.append("</TechnicalFailure>\n");

            result.append("<MessageFormat>");
            result.append(messageFormat);
            result.append("</MessageFormat>\n");

            result.append("<MessageType>");
            result.append(messageType);
            result.append("</MessageType>\n");

            result.append("<MessageDirection>");
            result.append(messageDirection);
            result.append("</MessageDirection>\n");

            result.append("<MessageUUID>");
            result.append(messageUUID);
            result.append("</MessageUUID>\n");

            result.append("<Status>");
            result.append(messageStatus);
            result.append("</Status>\n");

            result.append("<TimeStamp>");
            result.append(messageTimestamp);
            result.append("</TimeStamp>\n");

            result.append("<ServiceName>");
            result.append(serviceName);
            result.append("</ServiceName>\n");

            result.append("<ARCKey>");
            result.append(arcKey);
            result.append("</ARCKey>\n");

            result.append("<ParentRecord>");
            result.append(parentRecord);
            result.append("</ParentRecord>\n");

            result.append("<IpAddress>");
            result.append(message.getIpAddress());
            result.append("</IpAddress>\n");

            result.append("<UserAgent>");
            result.append(message.getUserAgent());
            result.append("</UserAgent>\n");

            result.append("</dataSet").append(index).append(">\n");

        } else {
            activityResult.append("<dataSet" + index + ">\n");
            activityResult.append("<MessageId>");
            activityResult.append(message.getMessageId());
            activityResult.append("</MessageId>\n");

            activityResult.append("<ActivityId>");
            activityResult.append(message.getActivityId());
            activityResult.append("</ActivityId>\n");

            activityResult.append("<ActivityType>");
            activityResult.append(activityType);
            activityResult.append("</ActivityType>\n");

            activityResult.append("<ApplicationFailure>");
            activityResult.append(applicationFailure);
            activityResult.append("</ApplicationFailure>\n");

            activityResult.append("<TechnicalFailure>");
            activityResult.append(technicalFailure);
            activityResult.append("</TechnicalFailure>\n");

            activityResult.append("<MessageFormat>");
            activityResult.append(messageFormat);
            activityResult.append("</MessageFormat>\n");

            activityResult.append("<MessageType>");
            activityResult.append(messageType);
            activityResult.append("</MessageType>\n");

            activityResult.append("<MessageDirection>");
            activityResult.append(messageDirection);
            activityResult.append("</MessageDirection>\n");

            activityResult.append("<MessageUUID>");
            activityResult.append(messageUUID);
            activityResult.append("</MessageUUID>\n");

            activityResult.append("<Status>");
            activityResult.append(messageStatus);
            activityResult.append("</Status>\n");

            activityResult.append("<TimeStamp>");
            activityResult.append(messageTimestamp);
            activityResult.append("</TimeStamp>\n");

            activityResult.append("<ServiceName>");
            activityResult.append(serviceName);
            activityResult.append("</ServiceName>\n");

            activityResult.append("<ARCKey>");
            activityResult.append(arcKey);
            activityResult.append("</ARCKey>\n");

            activityResult.append("<ParentRecord>");
            activityResult.append(parentRecord);
            activityResult.append("</ParentRecord>\n");

            activityResult.append("<IpAddress>");
            activityResult.append(message.getIpAddress());
            activityResult.append("</IpAddress>\n");

            activityResult.append("<UserAgent>");
            activityResult.append(message.getUserAgent());
            activityResult.append("</UserAgent>\n");

            activityResult.append("</dataSet").append(index).append(">\n");
        }

        lastArcKey = arcKey;
    }

    /**
     * Getting messages for the given status filters. Overloaded method for
     * accepting xpath configuration key instead of xpath expressions and
     * namespaces directly.
     *
     * @param startTimeSelection    Optional
     * @param endTimeSelection      Optional
     * @param activityTypeSelection Mandatory
     * @param messageTypeSelection  Mandatory
     * @param xpathKey              Optional - Xpath configuration name for xpath evaluation.
     *                              Xpath expressions and namespace details relating to this
     *                              configuration is fetched from database if present and used for
     *                              xpath evaluation.
     * @param value                 Optional - Value to match for xpath expression evaluation. If
     *                              only xpathKey parameter is present fetches messages having any
     *                              value after operating the xpath expression related to xpathKey
     *                              on them.
     * @return Non null String containing message details
     * @throws BAMException
     */
    public String getMessagesForStatus(String startTimeSelection,
                                       String endTimeSelection, String server, String service,
                                       String operation, String direction, String status,
                                       String activityTypeSelection, String messageTypeSelection,
                                       String messageFormatSelection,
                                       String applicationStatusSelection,
                                       String technicalStatusSelection, String messageGUID,
                                       String xpathKey, String value, int startDataset,
                                       int endDataset)
            throws BAMException {

        int serverId;
        String xpathExpression = null;
        String namespaces = null;

        if (xpathKey != null) {
            try {
                serverId = bamStatQueryDSClient.getServerIdForServer(server);
            } catch (BAMException e) {
                log.error("Error fetching server data for server " + server);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }

            try {
                PropertyFilterDTO[] xpathConfigs = bamListAdminClient
                        .getXpathConfigurations(serverId);
                for (PropertyFilterDTO xpathConfig : xpathConfigs) {
                    if (xpathConfig.getExpressionKey().equals(xpathKey)) {
                        xpathExpression = xpathConfig.getExpression();
                    }

                    try {
                        NamespaceDTO[] nsArray = bamListAdminClient
                                .getNamespaces(xpathConfig.getId());
                        namespaces = encodeNamespaces(nsArray);
                    } catch (BAMException e) {
                        log.error("Error fetching namespace for xpath id "
                                + xpathConfig.getId()
                                + ". No namespaces will be available..", e);
                    }
                }
            } catch (BAMException e) {
                log.error("Error fetching xpath configurations for server id "
                        + serverId);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }
        }

        return getMessagesForStatus(startTimeSelection, endTimeSelection,
                server, service, operation, direction, status,
                activityTypeSelection, messageTypeSelection,
                messageFormatSelection, applicationStatusSelection,
                technicalStatusSelection, messageGUID, xpathExpression, value,
                namespaces, startDataset, endDataset);

    }

    /**
     * Getting the SAP message Count
     */

    public String getMessagesCountForSAP(String startTimeSelection, String endTimeSelection,
                                         String server, String service,
                                         String operation, String direction, String status,
                                         String activityTypeSelection, String messageTypeSelection,
                                         String messageFormatSelection,
                                         String applicationStatusSelection,
                                         String technicalStatusSelection, String messageGUID,
                                         String expression, String value, String namespaces)
            throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            // messageGUID is the arcKey
            SAPcount[] messageIds = bamDSClient.getMessagesCountForSAP(
                    startTimeSelection, endTimeSelection, server, service,
                    operation, direction, status, activityTypeSelection,
                    messageTypeSelection, messageFormatSelection,
                    applicationStatusSelection, technicalStatusSelection,
                    messageGUID);

            if (messageIds != null && messageIds.length > 0) {
                result.append("<MessageCount>");


                for (int index = 0; index < messageIds.length; index++) {

                    result.append(messageIds[index].getSAPmessageCount());

                }


                result.append("</MessageCount>");

            }

            if (log.isDebugEnabled()) {
                log.debug("Filtered messages count :" + result.toString());
            }

            return result.toString();
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }

    }

    /**
     * Get the children for the seleted parent
     */
    public String getChildrenMessagesForSAP(String startTimeSelection, String endTimeSelection,
                                            String server, String service,
                                            String operation, String direction, String status,
                                            String activityTypeSelection,
                                            String messageTypeSelection,
                                            String messageFormatSelection,
                                            String applicationStatusSelection,
                                            String technicalStatusSelection, String messageGUID,
                                            String expression, String value, String namespaces,
                                            int activityId,
                                            int startDataset, int endDataset) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            // messageGUID is the arcKey
            SAPchild[] messageIds = bamDSClient.getChildrenMessagesForSAP(
                    startTimeSelection, endTimeSelection, server, service,
                    operation, direction, status, activityTypeSelection,
                    messageTypeSelection, messageFormatSelection,
                    applicationStatusSelection, technicalStatusSelection,
                    messageGUID, activityId, startDataset, endDataset);
            List<SAPchild> idsForXPathList = new ArrayList<SAPchild>();

            if (expression != null && !expression.equals("")) {

                SAPchild[] idsForXPath;
                AXIOMXPath xpath = new AXIOMXPath(expression);
                Map<String, String> nsMap = processNamespaces(namespaces);

                for (String prefix : nsMap.keySet()) {
                    xpath.addNamespace(prefix, nsMap.get(prefix));
                }

                for (int index = 0; index < messageIds.length; index++) {
                    MessageForMessageID[] messages = bamDSClient
                            .getMessageForMessageID(Integer
                                    .parseInt(messageIds[index].getMessageId()));

                    if (messages != null && messages.length > 0
                            && messages[0].getMessage() != "") {
                        StAXBuilder builder = new StAXOMBuilder(
                                new ByteArrayInputStream(messages[0]
                                        .getMessage().getBytes()));
                        OMElement root = builder.getDocumentElement();

                        List nodes;

                        if (root != null) {
                            nodes = xpath.selectNodes(root);

                            if (nodes != null && nodes.size() > 0) {
                                for (Object node : nodes) {

                                    OMElement element = null;
                                    OMAttribute attribute = null;
                                    if (node instanceof OMDocument) {
                                        element = ((OMDocument) node)
                                                .getOMDocumentElement();
                                    } else if (node instanceof OMElement) {
                                        element = (OMElement) node;
                                    } else if (node instanceof OMAttribute) {
                                        attribute = (OMAttribute) node;
                                    }

                                    if (value != null && !value.equals("")) {
                                        String evaluation = null;
                                        String lineBreak = System
                                                .getProperty("line.separator");

                                        if (element != null) {
                                            if (element.getChildElements()
                                                    .hasNext()) {
                                                // This is a top level element
                                                evaluation = element.toString()
                                                        .replace(lineBreak, "");
                                            } else {
                                                // This is a element with a text
                                                // value
                                                evaluation = element.getText()
                                                        .replace(lineBreak, "");
                                            }
                                        } else if (attribute != null) {
                                            evaluation = attribute
                                                    .getLocalName().replace(
                                                    lineBreak, "");
                                        }

                                        evaluation = evaluation.trim();
                                        value = value.replace(lineBreak, "");
                                        if (evaluation.equals(value.trim())) {
                                            idsForXPathList
                                                    .add(messageIds[index]);
                                            break;
                                        }
                                    } else {
                                        idsForXPathList.add(messageIds[index]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                idsForXPath = idsForXPathList
                        .toArray(new SAPchild[idsForXPathList.size()]);
                messageIds = idsForXPath;
            }

            if (messageIds != null && messageIds.length > 0) {
                result.append("<ChildMessages>");

                String messageId = "";
                String messageDirection = "";
                String messageUUID = "";
                String messageStatus = "";
                String messageTimestamp = "";
                String activityType = "";
                String applicationFailure = "";
                String technicalFailure = "";
                String messageFormat = "";
                String messageType = "";
                String serviceName = "";
                String arcKey = "";
                boolean parentRecord = false;
                SAPchild message = null;
                for (int index = 0; index < messageIds.length; index++) {
                    // for a given messageId, there are multiple rows having
                    // different property keys & values.
                    // we are trying to accumulate all the keys/values belonging
                    // to a single message into a
                    // row model here. (for additional explanation on the
                    // problem
                    // see - http://markmail.org/thread/i24sllyzksw2cfho)
                    message = messageIds[index];
                    if (!messageId.equals(message.getMessageId())) {
                        if (!("".equals(messageId))) {
                            // dump the collected set of variables into XML
                            // format
                            dumpXML(result, messageDirection, messageUUID,
                                    messageStatus, messageTimestamp,
                                    activityType, applicationFailure,
                                    technicalFailure, messageFormat,
                                    messageType, serviceName, arcKey,
                                    parentRecord, index, messageIds[index - 1]);
                        }
                        activityType = "";
                        applicationFailure = "";
                        technicalFailure = "";
                        messageFormat = "";
                        messageType = "";
                        arcKey = "";
                        parentRecord = false;

                        // repeated with same value for all the rows. So getting
                        // assigned from the values in the
                        // first row is sufficient
                        messageId = message.getMessageId();
                        messageDirection = message.getMessageDirection();
                        messageUUID = message.getMessageUUID();
                        messageStatus = message.getStatus();
                        messageTimestamp = message.getTimestamp();
                        serviceName = message.getServiceName();
                    }

                    if (("activity_type").equals(message.getKey())) {
                        activityType = message.getValue();
                    } else if ("application_failure".equals(message.getKey())) {
                        applicationFailure = message.getValue();
                    } else if ("technical_failure".equals(message.getKey())) {
                        technicalFailure = message.getValue();
                    } else if ("message_format".equals(message.getKey())) {
                        messageFormat = message.getValue();
                    } else if ("message_type".equals(message.getKey())) {
                        messageType = message.getValue();
                    } else if ("arc_key".equals(message.getKey())) {
                        arcKey = message.getValue();
                    } else if (message.getKey().contains("parent")) {
                        parentRecord = true;
                    }
                    // MessageForMessageID[] messages = bamDSClient
                    // .getMessageForMessageID(Integer
                    // .parseInt(messageId[index].getMessageId()));
                    // System.out.println(messages[0].getMessage());
                }
                // Not forgetting the last message block
                dumpXML(result, messageDirection, messageUUID, messageStatus,
                        messageTimestamp, activityType, applicationFailure,
                        technicalFailure, messageFormat, messageType,
                        serviceName, arcKey, parentRecord,
                        (messageIds.length - 1), message);

                if (activityResult != null) {
                    result.append(activityResult);
                }

                result.append("</ChildMessages>");

            }

            if (log.isDebugEnabled()) {
                log.debug("List of filtered messages are :" + result.toString());
            }

            return result.toString();
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }

    }

    /**
     * Utility method to dump collected statistics to a XML format to be
     * consumed at the gadget level.
     *
     * @param result
     * @param messageDirection
     * @param messageUUID
     * @param messageStatus
     * @param messageTimestamp
     * @param activityType
     * @param applicationFailure
     * @param messageFormat
     * @param messageType
     * @param index
     * @param message
     */
    String lastArcKeyC = "";
    StringBuilder activityResultC = new StringBuilder();
    StringBuilder activityParentIdArrC = new StringBuilder();
    StringBuilder activityChildIdArrC = new StringBuilder();

    private void dumpXML(StringBuilder result, String messageDirection,
                         String messageUUID, String messageStatus, String messageTimestamp,
                         String activityType, String applicationFailure,
                         String technicalFailure, String messageFormat, String messageType,
                         String serviceName, String arcKey, boolean parentRecord, int index,
                         SAPchild message) {

        /*
           * To sort the messages having the parent message on the top and having
           * the corresponding children messages below that.
           */

        if (!parentRecord) {
            result.append("<dataSet" + index + ">\n");
            result.append("<MessageId>");
            result.append(message.getMessageId());
            result.append("</MessageId>\n");

            result.append("<ActivityId>");
            result.append(message.getActivityId());
            result.append("</ActivityId>\n");

            result.append("<ActivityType>");
            result.append(activityType);
            result.append("</ActivityType>\n");

            result.append("<ApplicationFailure>");
            result.append(applicationFailure);
            result.append("</ApplicationFailure>\n");

            result.append("<TechnicalFailure>");
            result.append(technicalFailure);
            result.append("</TechnicalFailure>\n");

            result.append("<MessageFormat>");
            result.append(messageFormat);
            result.append("</MessageFormat>\n");

            result.append("<MessageType>");
            result.append(messageType);
            result.append("</MessageType>\n");

            result.append("<MessageDirection>");
            result.append(messageDirection);
            result.append("</MessageDirection>\n");

            result.append("<MessageUUID>");
            result.append(messageUUID);
            result.append("</MessageUUID>\n");

            result.append("<Status>");
            result.append(messageStatus);
            result.append("</Status>\n");

            result.append("<TimeStamp>");
            result.append(messageTimestamp);
            result.append("</TimeStamp>\n");

            result.append("<ServiceName>");
            result.append(serviceName);
            result.append("</ServiceName>\n");

            result.append("<ARCKey>");
            result.append(arcKey);
            result.append("</ARCKey>\n");

            result.append("<ParentRecord>");
            result.append(parentRecord);
            result.append("</ParentRecord>\n");

            result.append("<IpAddress>");
            result.append(message.getIpAddress());
            result.append("</IpAddress>\n");

            result.append("<UserAgent>");
            result.append(message.getUserAgent());
            result.append("</UserAgent>\n");

            result.append("</dataSet").append(index).append(">\n");
        }

    }

    /**
     * Getting messages for the given status filters. Overloaded method for
     * accepting xpath configuration key instead of xpath expressions and
     * namespaces directly.
     *
     * @param startTimeSelection    Optional
     * @param endTimeSelection      Optional
     * @param activityTypeSelection Mandatory
     * @param messageTypeSelection  Mandatory
     * @param xpathKey              Optional - Xpath configuration name for xpath evaluation.
     *                              Xpath expressions and namespace details relating to this
     *                              configuration is fetched from database if present and used for
     *                              xpath evaluation.
     * @param value                 Optional - Value to match for xpath expression evaluation. If
     *                              only xpathKey parameter is present fetches messages having any
     *                              value after operating the xpath expression related to xpathKey
     *                              on them.
     * @return Non null String containing message details
     * @throws BAMException
     */
    public String getChildrenMessagesForSAP(String startTimeSelection,
                                            String endTimeSelection, String server, String service,
                                            String operation, String direction, String status,
                                            String activityTypeSelection,
                                            String messageTypeSelection,
                                            String messageFormatSelection,
                                            String applicationStatusSelection,
                                            String technicalStatusSelection, String messageGUID,
                                            String xpathKey, String value, int activityId,
                                            int startDataset, int endDataset)
            throws BAMException {

        int serverId;
        String xpathExpression = null;
        String namespaces = null;

        if (xpathKey != null) {
            try {
                serverId = bamStatQueryDSClient.getServerIdForServer(server);
            } catch (BAMException e) {
                log.error("Error fetching server data for server " + server);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }

            try {
                PropertyFilterDTO[] xpathConfigs = bamListAdminClient
                        .getXpathConfigurations(serverId);
                for (PropertyFilterDTO xpathConfig : xpathConfigs) {
                    if (xpathConfig.getExpressionKey().equals(xpathKey)) {
                        xpathExpression = xpathConfig.getExpression();
                    }

                    try {
                        NamespaceDTO[] nsArray = bamListAdminClient
                                .getNamespaces(xpathConfig.getId());
                        namespaces = encodeNamespaces(nsArray);
                    } catch (BAMException e) {
                        log.error("Error fetching namespace for xpath id "
                                + xpathConfig.getId()
                                + ". No namespaces will be available..", e);
                    }
                }
            } catch (BAMException e) {
                log.error("Error fetching xpath configurations for server id "
                        + serverId);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }
        }

        return getChildrenMessagesForSAP(startTimeSelection, endTimeSelection,
                server, service, operation, direction, status,
                activityTypeSelection, messageTypeSelection,
                messageFormatSelection, applicationStatusSelection,
                technicalStatusSelection, messageGUID, xpathExpression, value,
                namespaces, activityId, startDataset, endDataset);

    }

    /**
     * Get the aleaudit messages for the selected parent
     */
    public String getAleauditMessagesForSAP(String startTimeSelection, String endTimeSelection,
                                            String server, String service,
                                            String operation, String direction, String status,
                                            String messageTypeSelection,
                                            String messageFormatSelection,
                                            String applicationStatusSelection,
                                            String technicalStatusSelection, String messageGUID,
                                            String expression, String value, String namespaces,
                                            int startDataset, int endDataset) throws BAMException {
        try {
            StringBuilder result = new StringBuilder();
            // messageGUID is the arcKey
            SAPaleaudit[] messageIds = bamDSClient.getAleauditMessagesForSAP(
                    startTimeSelection, endTimeSelection, server, service,
                    operation, direction, status,
                    messageTypeSelection, messageFormatSelection,
                    applicationStatusSelection, technicalStatusSelection,
                    messageGUID, startDataset, endDataset);
            List<SAPaleaudit> idsForXPathList = new ArrayList<SAPaleaudit>();

            if (expression != null && !expression.equals("")) {

                SAPaleaudit[] idsForXPath;
                AXIOMXPath xpath = new AXIOMXPath(expression);
                Map<String, String> nsMap = processNamespaces(namespaces);

                for (String prefix : nsMap.keySet()) {
                    xpath.addNamespace(prefix, nsMap.get(prefix));
                }

                for (int index = 0; index < messageIds.length; index++) {
                    MessageForMessageID[] messages = bamDSClient
                            .getMessageForMessageID(Integer
                                    .parseInt(messageIds[index].getMessageId()));

                    if (messages != null && messages.length > 0
                            && messages[0].getMessage() != "") {
                        StAXBuilder builder = new StAXOMBuilder(
                                new ByteArrayInputStream(messages[0]
                                        .getMessage().getBytes()));
                        OMElement root = builder.getDocumentElement();

                        List nodes;

                        if (root != null) {
                            nodes = xpath.selectNodes(root);

                            if (nodes != null && nodes.size() > 0) {
                                for (Object node : nodes) {

                                    OMElement element = null;
                                    OMAttribute attribute = null;
                                    if (node instanceof OMDocument) {
                                        element = ((OMDocument) node)
                                                .getOMDocumentElement();
                                    } else if (node instanceof OMElement) {
                                        element = (OMElement) node;
                                    } else if (node instanceof OMAttribute) {
                                        attribute = (OMAttribute) node;
                                    }

                                    if (value != null && !value.equals("")) {
                                        String evaluation = null;
                                        String lineBreak = System
                                                .getProperty("line.separator");

                                        if (element != null) {
                                            if (element.getChildElements()
                                                    .hasNext()) {
                                                // This is a top level element
                                                evaluation = element.toString()
                                                        .replace(lineBreak, "");
                                            } else {
                                                // This is a element with a text
                                                // value
                                                evaluation = element.getText()
                                                        .replace(lineBreak, "");
                                            }
                                        } else if (attribute != null) {
                                            evaluation = attribute
                                                    .getLocalName().replace(
                                                    lineBreak, "");
                                        }

                                        evaluation = evaluation.trim();
                                        value = value.replace(lineBreak, "");
                                        if (evaluation.equals(value.trim())) {
                                            idsForXPathList
                                                    .add(messageIds[index]);
                                            break;
                                        }
                                    } else {
                                        idsForXPathList.add(messageIds[index]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                idsForXPath = idsForXPathList
                        .toArray(new SAPaleaudit[idsForXPathList.size()]);
                messageIds = idsForXPath;
            }

            if (messageIds != null && messageIds.length > 0) {
                result.append("<AleauditMessages>");

                String messageId = "";
                String messageDirection = "";
                String messageUUID = "";
                String messageStatus = "";
                String messageTimestamp = "";
                String activityType = "";
                String applicationFailure = "";
                String technicalFailure = "";
                String messageFormat = "";
                String messageType = "";
                String serviceName = "";
                String arcKey = "";
                boolean parentRecord = false;
                SAPaleaudit message = null;
                for (int index = 0; index < messageIds.length; index++) {
                    // for a given messageId, there are multiple rows having
                    // different property keys & values.
                    // we are trying to accumulate all the keys/values belonging
                    // to a single message into a
                    // row model here. (for additional explanation on the
                    // problem
                    // see - http://markmail.org/thread/i24sllyzksw2cfho)
                    message = messageIds[index];
                    if (!messageId.equals(message.getMessageId())) {
                        if (!("".equals(messageId))) {
                            // dump the collected set of variables into XML
                            // format
                            dumpXML(result, messageDirection, messageUUID,
                                    messageStatus, messageTimestamp,
                                    activityType, applicationFailure,
                                    technicalFailure, messageFormat,
                                    messageType, serviceName, arcKey,
                                    parentRecord, index, messageIds[index - 1]);
                        }
                        activityType = "";
                        applicationFailure = "";
                        technicalFailure = "";
                        messageFormat = "";
                        messageType = "";
                        arcKey = "";
                        parentRecord = false;

                        // repeated with same value for all the rows. So getting
                        // assigned from the values in the
                        // first row is sufficient
                        messageId = message.getMessageId();
                        messageDirection = message.getMessageDirection();
                        messageUUID = message.getMessageUUID();
                        messageStatus = message.getStatus();
                        messageTimestamp = message.getTimestamp();
                        serviceName = message.getServiceName();
                    }

                    if (("activity_type").equals(message.getKey())) {
                        activityType = message.getValue();
                    } else if ("application_failure".equals(message.getKey())) {
                        applicationFailure = message.getValue();
                    } else if ("technical_failure".equals(message.getKey())) {
                        technicalFailure = message.getValue();
                    } else if ("message_format".equals(message.getKey())) {
                        messageFormat = message.getValue();
                    } else if ("message_type".equals(message.getKey())) {
                        messageType = message.getValue();
                    } else if ("arc_key".equals(message.getKey())) {
                        arcKey = message.getValue();
                    } else if (message.getKey().contains("parent")) {
                        parentRecord = true;
                    }
                    // MessageForMessageID[] messages = bamDSClient
                    // .getMessageForMessageID(Integer
                    // .parseInt(messageId[index].getMessageId()));
                    // System.out.println(messages[0].getMessage());
                }
                // Not forgetting the last message block
                dumpXML(result, messageDirection, messageUUID, messageStatus,
                        messageTimestamp, activityType, applicationFailure,
                        technicalFailure, messageFormat, messageType,
                        serviceName, arcKey, parentRecord,
                        (messageIds.length - 1), message);

                if (activityResult != null) {
                    result.append(activityResult);
                }

                result.append("</AleauditMessages>");

            }

            if (log.isDebugEnabled()) {
                log.debug("List of filtered messages are :" + result.toString());
            }

            return result.toString();
        } catch (Exception e) {
            throw new BAMException("failed to getPropertyValueForStatus ", e);
        }

    }

    /**
     * Utility method to dump collected statistics to a XML format to be
     * consumed at the gadget level.
     *
     * @param result
     * @param messageDirection
     * @param messageUUID
     * @param messageStatus
     * @param messageTimestamp
     * @param activityType
     * @param applicationFailure
     * @param messageFormat
     * @param messageType
     * @param index
     * @param message
     */
    String lastArcKeyA = "";
    StringBuilder activityResultA = new StringBuilder();
    StringBuilder activityParentIdArrA = new StringBuilder();
    StringBuilder activityChildIdArrA = new StringBuilder();

    private void dumpXML(StringBuilder result, String messageDirection,
                         String messageUUID, String messageStatus, String messageTimestamp,
                         String activityType, String applicationFailure,
                         String technicalFailure, String messageFormat, String messageType,
                         String serviceName, String arcKey, boolean parentRecord, int index,
                         SAPaleaudit message) {

        /*
           * To sort the messages having the parent message on the top and having
           * the corresponding children messages below that.
           */

        result.append("<AleauditMessage_dataSet" + index + ">\n");
        result.append("<MessageId>");
        result.append(message.getMessageId());
        result.append("</MessageId>\n");

        result.append("<ActivityId>");
        result.append(message.getActivityId());
        result.append("</ActivityId>\n");

        result.append("<ActivityType>");
        result.append(activityType);
        result.append("</ActivityType>\n");

        result.append("<ApplicationFailure>");
        result.append(applicationFailure);
        result.append("</ApplicationFailure>\n");

        result.append("<TechnicalFailure>");
        result.append(technicalFailure);
        result.append("</TechnicalFailure>\n");

        result.append("<MessageFormat>");
        result.append(messageFormat);
        result.append("</MessageFormat>\n");

        result.append("<MessageType>");
        result.append(messageType);
        result.append("</MessageType>\n");

        result.append("<MessageDirection>");
        result.append(messageDirection);
        result.append("</MessageDirection>\n");

        result.append("<MessageUUID>");
        result.append(messageUUID);
        result.append("</MessageUUID>\n");

        result.append("<Status>");
        result.append(messageStatus);
        result.append("</Status>\n");

        result.append("<TimeStamp>");
        result.append(messageTimestamp);
        result.append("</TimeStamp>\n");

        result.append("<ServiceName>");
        result.append(serviceName);
        result.append("</ServiceName>\n");

        result.append("<ARCKey>");
        result.append(arcKey);
        result.append("</ARCKey>\n");

        result.append("<ParentRecord>");
        result.append(parentRecord);
        result.append("</ParentRecord>\n");

        result.append("<IpAddress>");
        result.append(message.getIpAddress());
        result.append("</IpAddress>\n");

        result.append("<UserAgent>");
        result.append(message.getUserAgent());
        result.append("</UserAgent>\n");

        result.append("</AleauditMessage_dataSet").append(index).append(">\n");
    }

    /**
     * Getting messages for the given status filters. Overloaded method for
     * accepting xpath configuration key instead of xpath expressions and
     * namespaces directly.
     *
     * @param startTimeSelection    Optional
     * @param endTimeSelection      Optional
     * @param messageTypeSelection  Mandatory
     * @param xpathKey              Optional - Xpath configuration name for xpath evaluation.
     *                              Xpath expressions and namespace details relating to this
     *                              configuration is fetched from database if present and used for
     *                              xpath evaluation.
     * @param value                 Optional - Value to match for xpath expression evaluation. If
     *                              only xpathKey parameter is present fetches messages having any
     *                              value after operating the xpath expression related to xpathKey
     *                              on them.
     * @return Non null String containing message details
     * @throws BAMException
     */
    public String getAleauditMessagesForSAP(String startTimeSelection,
                                            String endTimeSelection, String server, String service,
                                            String operation, String direction, String status,
                                            String messageTypeSelection,
                                            String messageFormatSelection,
                                            String applicationStatusSelection,
                                            String technicalStatusSelection, String messageGUID,
                                            String xpathKey, String value,
                                            int startDataset, int endDataset)
            throws BAMException {

        int serverId;
        String xpathExpression = null;
        String namespaces = null;

        if (xpathKey != null) {
            try {
                serverId = bamStatQueryDSClient.getServerIdForServer(server);
            } catch (BAMException e) {
                log.error("Error fetching server data for server " + server);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }

            try {
                PropertyFilterDTO[] xpathConfigs = bamListAdminClient
                        .getXpathConfigurations(serverId);
                for (PropertyFilterDTO xpathConfig : xpathConfigs) {
                    if (xpathConfig.getExpressionKey().equals(xpathKey)) {
                        xpathExpression = xpathConfig.getExpression();
                    }

                    try {
                        NamespaceDTO[] nsArray = bamListAdminClient
                                .getNamespaces(xpathConfig.getId());
                        namespaces = encodeNamespaces(nsArray);
                    } catch (BAMException e) {
                        log.error("Error fetching namespace for xpath id "
                                + xpathConfig.getId()
                                + ". No namespaces will be available..", e);
                    }
                }
            } catch (BAMException e) {
                log.error("Error fetching xpath configurations for server id "
                        + serverId);
                throw new BAMException(
                        "Filter processing failed at xpath filters..", e);
            }
        }

        return getAleauditMessagesForSAP(startTimeSelection, endTimeSelection,
                server, service, operation, direction, status,
                messageTypeSelection,
                messageFormatSelection, applicationStatusSelection,
                technicalStatusSelection, messageGUID, xpathExpression, value,
                namespaces, startDataset, endDataset);

    }

    public int getCountofChildrenFailedMessages(int activityKeyID) throws BAMException {

        try {
            StringBuilder result = new StringBuilder();
            int count = bamDSClient.getCountofChildrenFailedMessages(activityKeyID);
            if (count > 0) {
                return count;
            }


        } catch (Exception e) {
            throw new BAMException("failed to getCountofChildrenFailedMessages ", e);
        }
        return 0;

    }

    public String getCountofChildrenFailedMessagesString(int activityKeyID) throws BAMException {
        StringBuilder result = new StringBuilder();
        try {

            int count = bamDSClient.getCountofChildrenFailedMessages(activityKeyID);
            result.append("<FailedMessageCount>");
            result.append(count);
            result.append("</FailedMessageCount>\n");

        } catch (Exception e) {
            throw new BAMException("failed to getCountofChildrenFailedMessages ", e);
        }
        return result.toString();

    }

}


