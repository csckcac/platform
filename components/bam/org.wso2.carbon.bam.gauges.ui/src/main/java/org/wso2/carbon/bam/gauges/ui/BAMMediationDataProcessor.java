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
import org.wso2.carbon.bam.stub.statquery.Data;
import org.wso2.carbon.bam.stub.statquery.Endpoint;
import org.wso2.carbon.bam.stub.statquery.ProxyService;
import org.wso2.carbon.bam.stub.statquery.Sequence;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * used this class to process  mediation data only for gauges
 */
public class BAMMediationDataProcessor {
    Log log = LogFactory.getLog(BAMMediationDataProcessor.class);
    private BAMStatQueryDSClient bamDSClient;
    private BAMListAdminServiceClient bamListAdminClient;

    public BAMMediationDataProcessor(ServletConfig config, HttpSession session,
                                     HttpServletRequest request)
            throws AxisFault, SocketException {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            bamDSClient = new BAMStatQueryDSClient(cookie, backendServerURL, configContext, request.getLocale());
            bamListAdminClient = new BAMListAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
        }

    }

    public String getEndpoints(int serverID) {

        Endpoint endpoints[] = null;
        try {
            if (bamDSClient != null) {
                endpoints = bamDSClient.getEndpoints(serverID);
            }
        } catch (RemoteException e) {
        }

        StringBuffer epString = new StringBuffer("");
        if (endpoints != null && endpoints.length > 0) {
            epString.append(endpoints[0].getEndpoint());
            for (int i = 1; i < endpoints.length; i++) {
//                epString.append(epString);
                epString.append("&");
                epString.append(endpoints[i].getEndpoint());
            }
        }
        return epString.toString();
    }

    public String getSequences(int serverID)
            throws BAMException {

        Sequence sequences[] = null;

        if (bamDSClient != null) {
            sequences = bamDSClient.getSequences(serverID);
        }


        StringBuffer sequencesString = new StringBuffer("");
        if (sequences != null && sequences.length > 0) {
            sequencesString.append(sequences[0].getSequence());
            for (int i = 1; i < sequences.length; i++) {
                sequencesString.append("&").append(sequences[i].getSequence());
            }
        }
        return sequencesString.toString();
    }

    public String getProxyServices(int serverID)
            throws BAMException {

        ProxyService proxyServices[] = null;

        if (bamDSClient != null) {
            proxyServices = bamDSClient.getProxyServices(serverID);
        }

        StringBuffer proxyServicesString = new StringBuffer("");
        if (proxyServices != null && proxyServices.length > 0) {
            proxyServicesString.append(proxyServices[0].getProxyService());
            for (int i = 1; i < proxyServices.length; i++) {
//                proxyServicesString.append(proxyServicesString);
                proxyServicesString.append("&");
                proxyServicesString.append(proxyServices[i].getProxyService());
            }
        }
        return proxyServicesString.toString();
    }

    public String getSequenceInAvgProcessingTimesOfServer(int serverID, boolean demo) {
        /* Data is returned in CSV format */
        /*
           * service1,service2,service3,service4,service5.... 34,23,22,223,32....
           */

        if (demo) {
            Random generator = new Random();
            StringBuffer demoString = new StringBuffer("mySequence1, mySequence2, mySequence3, mySequence4, mySequence5, mySequence6\n");
            for (int i = 0; i < 6; i++) {
                double val = generator.nextDouble() * 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                demoString.append(df.format(val)).append(",");
            }
            return demoString.substring(0, demoString.length() - 1);
        }

        try {
            if (bamDSClient != null) {
                Sequence[] sequenceList = bamDSClient.getSequences(serverID);
                if (sequenceList != null) {
                    String header = "";
                    String values = "";
                    if (sequenceList.length > 0) {
                        for (Sequence sequence : sequenceList) {
                            header += sequence.getSequence() + ",";
                            values += bamDSClient.getLatestInAverageProcessingTimeForSequenceNoWrap(serverID,
                                                                                                    "SequenceInAvgProcessingTime-" + sequence.getSequence()) + ",";
                        }
                    }
                    if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2) {
                        return "";
                    }
                    return header.substring(0, header.length() - 1) + "\n" + values.substring(0, values.length() - 1);
                }
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
            StringBuffer demoString = new StringBuffer("myProxyService1, myProxyService2, myProxyService3, myProxyService4, myProxyService5, myProxyService6\n");
            for (int i = 0; i < 6; i++) {
                double val = generator.nextDouble() * 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                demoString.append(demoString);
                demoString.append(df.format(val));
                demoString.append(",");
            }
            return demoString.substring(0, demoString.length() - 1);
        }

        try {
            if (bamDSClient != null) {
                ProxyService[] proxyServiceList = bamDSClient.getProxyServices(serverID);
                if (proxyServiceList != null) {
                    String header = "";
                    String values = "";
                    if (proxyServiceList.length > 0) {
                        for (ProxyService proxyService : proxyServiceList) {
                            header += proxyService.getProxyService() + ",";
                            values += bamDSClient.getLatestInAverageProcessingTimeForProxyNoWrap(serverID,
                                                                                                 "ProxyInAvgProcessingTime-" + proxyService.getProxyService()) + ",";
                        }
                    }
                    if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2) {
                        return "";
                    }
                    return header.substring(0, header.length() - 1) + "\n"
                           + values.substring(0, values.length() - 1);
                }
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
            StringBuffer demoString = new StringBuffer("myEndpoint1, myEndpoint2, myEndpoint3, myEndpoint4, myEndpoint5, myEndpoint6\n");
            for (int i = 0; i < 6; i++) {
                double val = generator.nextDouble() * 10.0;
                DecimalFormat df = new DecimalFormat("#.##");
                demoString.append(demoString);
                demoString.append(df.format(val));
                demoString.append(",");
            }
            return demoString.substring(0, demoString.length() - 1);
        }

        try {
            if (bamDSClient != null) {
                Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
                if (endpointList != null) {
                    String header = "";
                    String values = "";
                    if (endpointList.length > 0) {
                        for (Endpoint endpoint : endpointList) {
                            header += endpoint.getEndpoint() + ",";
                            values += bamDSClient.getLatestInAverageProcessingTimeForEndpointNoWrap(serverID,
                                                                                                    "EndpointInAvgProcessingTime-" + endpoint.getEndpoint()) + ",";
                        }
                    }
                    if (header.equals("") || values.equals("") || values.length() < 2 || header.length() < 2) {
                        return "";
                    }
                    return header.substring(0, header.length() - 1) + "\n"
                           + values.substring(0, values.length() - 1);
                }
            }
        } catch (Exception e) {
            log.debug(e);
        }

        return "";
    }

    public String getServerMediationInfo(int serverID, boolean demo)
            throws BAMException {
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
            int[] maxOps = {8, 2, 4, 5, 3, 6};
            String formatString = "<level%d name=\"%s\" meta1=\"%s\" meta2=\"%s\" meta3=\"%s\" meta4=\"%s\" meta5=\"%s\" meta6=\"%s\"";
            Data data = gaugesUtils.generateRandomData(1000);
            result.append(String.format(formatString, 0, "http://127.0.0.1:RND", data.getReqCount(), data
                    .getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
            result.append(">\n");
            String[] mediation = {"Endpoint", "Proxy Service", "Sequence"};
            for (int i = 0; i < 3; i++) {
                data = gaugesUtils.generateRandomData(250);
                result.append(String.format(formatString, 1, mediation[i] + "s", data.getReqCount(), data
                        .getResCount(), data.getFaultCount(), data.getAvgTime(), data.getMinTime(), data.getMaxTime()));
                result.append(">\n");
                for (int j = 0; j < maxOps[i]; j++) {
                    data = gaugesUtils.generateRandomData(50);
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
        StringBuilder result = new StringBuilder();
        String serverURL = "";
        MonitoredServerDTO[] serverList = null;
        try {
            if (bamListAdminClient != null) {
                serverList = bamListAdminClient.getServerList();
            }
        } catch (BAMException e) {
            throw new BAMException("failed to get server list", e);
        }
        if (serverList != null) {
            for (MonitoredServerDTO monitoredServerDTO : serverList) {
                if (monitoredServerDTO.getServerId() == serverID) {
                    serverURL = monitoredServerDTO.getServerURL();
                }
            }
        }

        result.append("<level0 name=\"").append(serverURL).append("\">\n");
        try {
            if (bamDSClient != null) {
                Endpoint[] endpointList = bamDSClient.getEndpoints(serverID);
                if (endpointList != null && endpointList.length > 0) {
                    result.append("<level1 name=\"").append("Endpoints").append("\">\n");
                    for (Endpoint endpoint : endpointList) {
                        result.append(" <level2 name=\"").append(endpoint.getEndpoint()).append("\"");
                        result.append(" count=\"").append(bamDSClient.getLatestInCumulativeCountForEndpoint(serverID,
                                                                                                            "EndpointInCumulativeCount-" + endpoint.getEndpoint())).append("\"");
                        result.append(" meta1=\"").append(bamDSClient.getLatestInCumulativeCountForEndpoint(serverID,
                                                                                                            "EndpointInCumulativeCount-" + endpoint.getEndpoint())).append("\"");
                        result.append(" meta3=\"").append(bamDSClient.getLatestInFaultCountForEndpoint(serverID, "EndpointInFaultCount-"
                                                                                                                 + endpoint.getEndpoint())).append("\"");
                        result.append(" meta4=\"").append(bamDSClient.getLatestInAverageProcessingTimeForEndpointNoWrap(serverID,
                                                                                                                        "EndpointInAvgProcessingTime-" + endpoint.getEndpoint())).append("\"");
                        result.append(" meta5=\"").append(bamDSClient.getLatestInMinimumProcessingTimeForEndpointNoWrap(serverID,
                                                                                                                        "EndpointInMinProcessingTime-" + endpoint.getEndpoint())).append("\"");
                        result.append(" meta6=\"").append(bamDSClient.getLatestInMaximumProcessingTimeForEndpointNoWrap(serverID,
                                                                                                                        "EndpointInMaxProcessingTime-" + endpoint.getEndpoint())).append("\"");
                        result.append("/>\n");
                    }
                    result.append("</level1>\n");
                }
            }
        } catch (RemoteException e) {
            throw new BAMException("error occurred getting end points from server id =" + serverID, e);
        }

        if (bamDSClient != null) {
            ProxyService[] proxyServiceList = bamDSClient.getProxyServices(serverID);
            if (proxyServiceList != null && proxyServiceList.length > 0) {
                result.append("<level1 name=\"").append("Proxy Services").append("\">\n");
                for (ProxyService proxyService : proxyServiceList) {
                    result.append(" <level2 name=\"").append(proxyService.getProxyService()).append("\"");
                    result.append(" count=\"").append(bamDSClient.getLatestInCumulativeCountForProxy(serverID, "ProxyInCumulativeCount-" + proxyService.getProxyService())).append("\"");
                    result.append(" meta1=\"").append(bamDSClient.getLatestInCumulativeCountForProxy(serverID, "ProxyInCumulativeCount-" + proxyService.getProxyService())).append("\"");
                    result.append(" meta3=\"").append(bamDSClient.getLatestInFaultCountForProxy(serverID, "ProxyInFaultCount-" + proxyService.getProxyService())).append("\"");
                    result.append(" meta4=\"").append(bamDSClient.getLatestInAverageProcessingTimeForProxyNoWrap(serverID, "ProxyInAvgProcessingTime-" + proxyService.getProxyService())).append("\"");
                    result.append(" meta5=\"").append(bamDSClient.getLatestInMinimumProcessingTimeForProxyNoWrap(serverID, "ProxyInMinProcessingTime-" + proxyService.getProxyService())).append("\"");
                    result.append(" meta6=\"").append(bamDSClient.getLatestInMaximumProcessingTimeForProxyNoWrap(serverID, "ProxyInMaxProcessingTime-" + proxyService.getProxyService())).append("\"");
                    result.append("/>\n");
                }
                result.append("</level1>\n");
            }
        }

        if (bamDSClient != null) {
            Sequence[] sequenceList = bamDSClient.getSequences(serverID);
            if (sequenceList != null && sequenceList.length > 0) {
                result.append("<level1 name=\"").append("Sequences").append("\">\n");
                for (Sequence sequence : sequenceList) {
                    result.append(" <level2 name=\"").append(sequence.getSequence()).append("\"");
                    result.append(" count=\"").append(bamDSClient.getLatestInCumulativeCountForSequence(serverID, "SequenceInCumulativeCount-" + sequence.getSequence())).append("\"");
                    result.append(" meta1=\"").append(bamDSClient.getLatestInCumulativeCountForSequence(serverID, "SequenceInCumulativeCount-" + sequence.getSequence())).append("\"");
                    result.append(" meta3=\"").append(bamDSClient.getLatestInFaultCountForSequence(serverID, "SequenceInFaultCount-" + sequence.getSequence())).append("\"");
                    result.append(" meta4=\"").append(bamDSClient.getLatestInAverageProcessingTimeForSequenceNoWrap(serverID, "SequenceInAvgProcessingTime-" + sequence.getSequence())).append("\"");
                    result.append(" meta5=\"").append(bamDSClient.getLatestInMinimumProcessingTimeForSequenceNoWrap(serverID, "SequenceInMinProcessingTime-" + sequence.getSequence())).append("\"");
                    result.append(" meta6=\"").append(bamDSClient.getLatestInMaximumProcessingTimeForSequenceNoWrap(serverID, "SequenceInMaxProcessingTime-" + sequence.getSequence())).append("\"");
                    result.append("/>\n");
                }
                result.append("</level1>\n");
            }
        }

        result.append("</level0>\n");
        return result.toString();

//        return "";
    }
}
