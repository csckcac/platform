<!--
~ Copyright 2009 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="org.wso2.carbon.bam.ui.client.BAMStatQueryDSClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.bam.ui.report.beans.MediationDataBean" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMListAdminServiceClient" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Endpoint" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.ProxyService" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Sequence" %>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<div id="noDataError" style="display:none;"><fmt:message key="no.mediation.data"/></div>
<script type="text/javascript">
    var hasDataAnyWhere = false;
</script>
<ul class="root-list">
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    BAMListAdminServiceClient listAdminServiceClient = new BAMListAdminServiceClient(cookie,
                                                                                     serverURL, configContext, request.getLocale());
    BAMStatQueryDSClient statQueryDSClient = new BAMStatQueryDSClient(cookie, serverURL,
                                                                      configContext, request.getLocale());

    MonitoredServerDTO[] serverList = listAdminServiceClient.getServerList();
    boolean hasData = false;
    //before everything we need to calculate the highest bar width that the bar can go
    Float maxBarWidhA = new Float("0");
    Float maxBarWidhB = new Float("0");
    if (serverList != null) {
        for (MonitoredServerDTO server : serverList) {
            Endpoint[] endpointsList = statQueryDSClient.getEndpoints(server.getServerId());
            if (endpointsList != null && endpointsList.length > 0) {
                for (Endpoint endpoint : endpointsList) {
                    if (endpoint != null) {
                        Float tmpValue = new Float(statQueryDSClient
                                                           .getLatestInAverageProcessingTimeForEndpoint(server.getServerId(), endpoint.getEndpoint()));
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }
                        tmpValue = new Float(statQueryDSClient
                                                     .getLatestInMaximumProcessingTimeForEndpoint(server.getServerId(),
                                                                                                  endpoint.getEndpoint()));
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }
                        tmpValue = new Float(statQueryDSClient
                                                     .getLatestInMinimumProcessingTimeForEndpoint(server.getServerId(),
                                                                                                  endpoint.getEndpoint()));
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }
                        //bar witdh B part

                        tmpValue = new Float(statQueryDSClient.getLatestInCumulativeCountForEndpoint(
                                server.getServerId(), endpoint.getEndpoint()));
                        if (maxBarWidhB < tmpValue) {
                            maxBarWidhB = tmpValue;
                        }
                        tmpValue = new Float(statQueryDSClient.getLatestInFaultCountForEndpoint(
                                server.getServerId(), endpoint.getEndpoint()));
                        if (maxBarWidhB < tmpValue) {
                            maxBarWidhB = tmpValue;
                        }
                    }
                }
            }
            ProxyService[] proxyServicesList = statQueryDSClient.getProxyServices(server
                                                                                          .getServerId());

            if (proxyServicesList != null && proxyServicesList.length > 0) {
                for (ProxyService proxyService : proxyServicesList) {
                    if (proxyService != null) {
                        Float tmpInt = new Float(statQueryDSClient
                                                         .getLatestInAverageProcessingTimeForProxy(server.getServerId(),
                                                                                                   proxyService.getProxyService()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient
                                                   .getLatestInMaximumProcessingTimeForProxy(server.getServerId(),
                                                                                             proxyService.getProxyService()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient
                                                   .getLatestInMinimumProcessingTimeForProxy(server.getServerId(),
                                                                                             proxyService.getProxyService()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }

                        //bar witdh B part

                        tmpInt = new Float(statQueryDSClient.getLatestInCumulativeCountForProxy(
                                server.getServerId(), proxyService.getProxyService()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient.getLatestInFaultCountForProxy(server
                                                                                                   .getServerId(), proxyService.getProxyService()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient.getLatestOutFaultCountForProxy(server
                                                                                                   .getServerId(), proxyService.getProxyService()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                    }
                }
            }

            Sequence[] sequenceList = statQueryDSClient.getSequences(server.getServerId());

            if (sequenceList != null && sequenceList.length > 0) {
                for (Sequence sequence : sequenceList) {
                    if (sequence != null) {
                        Float tmpInt = new Float(statQueryDSClient
                                                         .getLatestInAverageProcessingTimeForProxy(server.getServerId(),
                                                                                                   sequence.getSequence()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient
                                                   .getLatestInMaximumProcessingTimeForProxy(server.getServerId(),
                                                                                             sequence.getSequence()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient
                                                   .getLatestInMinimumProcessingTimeForProxy(server.getServerId(),
                                                                                             sequence.getSequence()));
                        if (maxBarWidhA < tmpInt) {
                            maxBarWidhA = tmpInt;
                        }

                        //bar witdh B part
                        tmpInt = new Float(statQueryDSClient.getLatestInCumulativeCountForSequence(
                                server.getServerId(), sequence.getSequence()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient.getLatestInFaultCountForSequence(server.getServerId(), sequence.getSequence()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                        tmpInt = new Float(statQueryDSClient.getLatestOutFaultCountForSequence(server.getServerId(), sequence.getSequence()));
                        if (maxBarWidhB < tmpInt) {
                            maxBarWidhB = tmpInt;
                        }
                    }
                }
            }
        }
    } // end if (serverList != null)
    NumberFormat formatterTime = new DecimalFormat("#.00");
    NumberFormat formatterCount = new DecimalFormat("#.##");
%>
    <%--Set the javascript global var to get the max width--%>
<script type="text/javascript">
    var maxBarWidhA = <%=maxBarWidhA%>;
    var maxBarWidhB = <%=maxBarWidhB%>;
</script>
<%
    int i = 0;
    List<MediationDataBean> mediationDataBeanList = new ArrayList<MediationDataBean>();
    if (serverList != null) {
        for (MonitoredServerDTO server : serverList) {

            i++;
            int j = 0; //initiate second level tree which is always 0,1,2
            Endpoint[] endpointsList = statQueryDSClient.getEndpoints(server.getServerId());
            ProxyService[] proxyServicesList = statQueryDSClient.getProxyServices(server
                                                                                          .getServerId());
            Sequence[] sequenceList = statQueryDSClient.getSequences(server.getServerId());

            if ((endpointsList != null && endpointsList.length > 0)
                || (proxyServicesList != null && proxyServicesList.length > 0)
                || (sequenceList != null && sequenceList.length > 0)) { // has data?
                hasData = true;

%>
<li <%if (serverList.length != i) {%> class="vertical-line" <%}%>>
<div class="minus-icon" onclick="treeColapse(this)"></div>
<div class="mediators level1"><%=server.getServerURL()%>
</div>
<div class="branch-node"></div>
<%
    } // has data
    int k = 0;
    if (endpointsList != null && endpointsList.length > 0) {
%>
<ul class="child-list">
    <li class="vertical-line">
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>"><fmt:message key="endpoints"/></div>
        <div class="branch-node"></div>
        <script type="text/javascript">
            var hasDataAnyWhere = true;
        </script>
        <%
            j++;
            for (Endpoint endpoint : endpointsList) {
                if (endpoint != null) {
                    k++;

                    String epAvgTime = statQueryDSClient
                            .getLatestInAverageProcessingTimeForEndpoint(server.getServerId(),
                                                                         endpoint.getEndpoint());
                    epAvgTime = formatterTime.format(Float.parseFloat(epAvgTime));
                    String epMaxTime = statQueryDSClient
                            .getLatestInMaximumProcessingTimeForEndpoint(server.getServerId(),
                                                                         endpoint.getEndpoint());
                    epMaxTime = formatterTime.format(Float.parseFloat(epMaxTime));
                    String epMinTime = statQueryDSClient
                            .getLatestInMinimumProcessingTimeForEndpoint(server.getServerId(),
                                                                         endpoint.getEndpoint());
                    epMinTime = formatterTime.format(Float.parseFloat(epMinTime));
                    String epReqCount = statQueryDSClient.getLatestInCumulativeCountForEndpoint(
                            server.getServerId(), endpoint.getEndpoint());
                    epReqCount = formatterCount.format(Float.parseFloat(epReqCount));
                    String epFaultCount = statQueryDSClient.getLatestInFaultCountForEndpoint(
                            server.getServerId(), endpoint.getEndpoint());
                    epFaultCount = formatterCount.format(Float.parseFloat(epFaultCount));

                    MediationDataBean mediationDataBean = new MediationDataBean();
                    mediationDataBean.setServerURL(server.getServerURL());
                    mediationDataBean.setServiceName(endpoint.getEndpoint());
                    mediationDataBean.setOperation("Endpoints");
                    mediationDataBean.setAvgTime(epAvgTime);
                    mediationDataBean.setMaxTime(epMaxTime);
                    mediationDataBean.setMinTime(epMinTime);
                    mediationDataBean.setReqCount(epReqCount);
                    mediationDataBean.setFaultCount(epFaultCount);

                    mediationDataBeanList.add(mediationDataBean);

        %>

        <ul class="child-list">
            <li <%if (endpointsList.length != k) {%> class="vertical-line"
                    <%}%>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>">
                    <a onclick="showHideData(this,'<%=endpoint.getEndpoint()%>','<%=i%><%=j%><%=k%>','L3')"><%=endpoint.getEndpoint()%>
                        <img src="images/up.png" alt="Hide Data" align="top" hspace="5"/></a>

                    <div class="data-chart" id="L3graph_A<%=i%><%=j%><%=k%>"></div>

                    <script type="text/javascript">

                        generateGraph([
                                          ["<fmt:message key="endpoint.average.time"/>",'<%=epAvgTime%>'],
                                          ["<fmt:message key="endpoint.maximum.time"/>",'<%=epMaxTime%>'],
                                          ["<fmt:message key="endpoint.minimum.time"/>",'<%=epMinTime%>']
                                      ], "L3graph_A<%=i%><%=j%><%=k%>", 'A');

                    </script>


                    <div class="data-chart" id="L3graph_B<%=i%><%=j%><%=k%>"></div>

                    <script type="text/javascript">

                        generateGraph([
                                          ["<fmt:message key="endpoint.request.count"/>",'<%=epReqCount%>'],
                                          ["<fmt:message key="endpoint.fault.count"/>",'<%=epFaultCount%>']
                                      ], "L3graph_B<%=i%><%=j%><%=k%>", 'B');

                    </script>
                    <span class="BarClear"></span></div>
                <div class="empty-brake"/>
            </li>
        </ul>
        <%
                }
            }
        %>
    </li>
</ul>
<%
    }
%> <%
    k = 0;
    if (proxyServicesList != null && proxyServicesList.length > 0) {
%>
<div class="branch-node"></div>
<ul class="child-list">
    <li class="vertical-line">
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>">Proxy Services</div>
        <script type="text/javascript">
            var hasDataAnyWhere = true;
        </script>
        <div class="branch-node"></div>
        <%
            j++;
            for (ProxyService proxyService : proxyServicesList) {
                if (proxyService != null) {
                    k++;

                    // In direction..
                    String proxyInAvgTime = statQueryDSClient
                            .getLatestInAverageProcessingTimeForProxy(server.getServerId(),
                                                                      proxyService.getProxyService());
                    proxyInAvgTime = formatterTime.format(Float.parseFloat(proxyInAvgTime));

                    String proxyInMaxTime = statQueryDSClient
                            .getLatestInMaximumProcessingTimeForProxy(server.getServerId(),
                                                                      proxyService.getProxyService());
                    proxyInMaxTime = formatterTime.format(Float.parseFloat(proxyInMaxTime));

                    String proxyInMinTime = statQueryDSClient
                            .getLatestInMinimumProcessingTimeForProxy(server.getServerId(),
                                                                      proxyService.getProxyService());
                    proxyInMinTime = formatterTime.format(Float.parseFloat(proxyInMinTime));

                    String proxyInReqCount = statQueryDSClient.getLatestInCumulativeCountForProxy(
                            server.getServerId(), proxyService.getProxyService());
                    proxyInReqCount = formatterCount.format(Float.parseFloat(proxyInReqCount));

                    String proxyInFaultCount = statQueryDSClient.getLatestInFaultCountForProxy(
                            server.getServerId(), proxyService.getProxyService());
                    proxyInFaultCount = formatterCount.format(Float.parseFloat(proxyInFaultCount));

                    MediationDataBean mediationDataBean = new MediationDataBean();
                    mediationDataBean.setServerURL(server.getServerURL());
                    mediationDataBean.setServiceName(proxyService.getProxyService() + "-In");
                    mediationDataBean.setOperation("Proxy Services");
                    mediationDataBean.setAvgTime(proxyInAvgTime);
                    mediationDataBean.setMaxTime(proxyInMaxTime);
                    mediationDataBean.setMinTime(proxyInMinTime);
                    mediationDataBean.setReqCount(proxyInReqCount);
                    mediationDataBean.setFaultCount(proxyInFaultCount);

                    mediationDataBeanList.add(mediationDataBean);

                    // Out direction..
                    String proxyOutAvgTime = statQueryDSClient
                            .getLatestOutAverageProcessingTimeForProxy(server.getServerId(),
                                                                       proxyService.getProxyService());
                    proxyOutAvgTime = formatterTime.format(Float.parseFloat(proxyOutAvgTime));

                    String proxyOutMaxTime = statQueryDSClient
                            .getLatestOutMaximumProcessingTimeForProxy(server.getServerId(),
                                                                       proxyService.getProxyService());
                    proxyOutMaxTime = formatterTime.format(Float.parseFloat(proxyOutMaxTime));

                    String proxyOutMinTime = statQueryDSClient
                            .getLatestOutMinimumProcessingTimeForProxy(server.getServerId(),
                                                                       proxyService.getProxyService());
                    proxyOutMinTime = formatterTime.format(Float.parseFloat(proxyOutMinTime));

                    String proxyOutReqCount = statQueryDSClient.getLatestOutCumulativeCountForProxy(
                            server.getServerId(), proxyService.getProxyService());
                    proxyOutReqCount = formatterCount.format(Float.parseFloat(proxyOutReqCount));

                    String proxyOutFaultCount = statQueryDSClient.getLatestOutFaultCountForProxy(
                            server.getServerId(), proxyService.getProxyService());
                    proxyOutFaultCount = formatterCount.format(Float.parseFloat(proxyOutFaultCount));

                    mediationDataBean = new MediationDataBean();
                    mediationDataBean.setServerURL(server.getServerURL());
                    mediationDataBean.setServiceName(proxyService.getProxyService() + "-Out");
                    mediationDataBean.setOperation("Proxy Services");
                    mediationDataBean.setAvgTime(proxyOutAvgTime);
                    mediationDataBean.setMaxTime(proxyOutMaxTime);
                    mediationDataBean.setMinTime(proxyOutMinTime);
                    mediationDataBean.setReqCount(proxyOutReqCount);
                    mediationDataBean.setFaultCount(proxyOutFaultCount);

                    mediationDataBeanList.add(mediationDataBean);

        %>
        <ul class="child-list">
            <li <%if (proxyServicesList.length != k) {%> class="vertical-line"
                    <%}%>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>">
                    <a onclick="showHideData(this,'<%=proxyService.getProxyService()%>','<%=i%><%=j%><%=k%>','L3')"><%=proxyService.getProxyService()%><img
                            src="images/up.png" alt="Hide Data" align="top" hspace="5"/></a>

                    <div>
                        <div class="data-chart" id="L3graph_In_A<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ['Proxy Service Average Time','<%=proxyInAvgTime%>'],
                                              ['Proxy Service Maximum Time','<%=proxyInMaxTime%>'],
                                              ['Proxy Service Minimum Time','<%=proxyInMinTime%>']
                                          ], "L3graph_In_A<%=i%><%=j%><%=k%>", 'A');

                        </script>

                        <div class="data-chart" id="L3graph_In_B<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ['Proxy Service Request Time','<%=proxyInReqCount%>'],
                                              ['Proxy Service Fault Count','<%=proxyInFaultCount%>']
                                          ], "L3graph_In_B<%=i%><%=j%><%=k%>", 'B');

                        </script>
                        <div style="margin-right:20px; float: right;width:80px">IN Direction</div>
                    </div>
                    <div style="clear:both"></div>
                    <div>
                        <div class="data-chart" id="L3graph_Out_A<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ['Proxy Service Average Time','<%=proxyOutAvgTime%>'],
                                              ['Proxy Service Maximum Time','<%=proxyOutMaxTime%>'],
                                              ['Proxy Service Minimum Time','<%=proxyOutMinTime%>']
                                          ], "L3graph_Out_A<%=i%><%=j%><%=k%>", 'A');

                        </script>

                        <div class="data-chart" id="L3graph_Out_C<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ['Proxy Service Request Time','<%=proxyOutReqCount%>'],
                                              ['Proxy Service Fault Count','<%=proxyOutFaultCount%>']
                                          ], "L3graph_Out_C<%=i%><%=j%><%=k%>", 'C');

                        </script>
                        <div style="margin-right:20px; float: right;width:80px">OUT Direction</div>
                    </div>
                    <span class="BarClear"></span>
                </div>
                <div class="empty-brake"/>
            </li>
        </ul>
        <%
                }
            }
        %>
    </li>
</ul>
<%
    }
%> <%
    k = 0;
    if (sequenceList != null && sequenceList.length > 0) {
%>
<div class="branch-node"></div>
<ul class="child-list">
    <li>
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>">Sequences</div>
        <div class="branch-node"></div>
        <script type="text/javascript">
            var hasDataAnyWhere = true;
        </script>
        <%
            j++;
            for (Sequence sequence : sequenceList) {
                if (sequence != null) {
                    k++;

                    // In direction..
                    String seqInAvgTime = statQueryDSClient
                            .getLatestInAverageProcessingTimeForSequence(server.getServerId(),
                                                                         sequence.getSequence());
                    seqInAvgTime = formatterTime.format(Float.parseFloat(seqInAvgTime));

                    String seqInMaxTime = statQueryDSClient
                            .getLatestInMaximumProcessingTimeForSequence(server.getServerId(),
                                                                         sequence.getSequence());
                    seqInMaxTime = formatterTime.format(Float.parseFloat(seqInMaxTime));

                    String seqInMinTime = statQueryDSClient
                            .getLatestInMinimumProcessingTimeForSequence(server.getServerId(),
                                                                         sequence.getSequence());
                    seqInMinTime = formatterTime.format(Float.parseFloat(seqInMinTime));

                    String seqInReqCount = statQueryDSClient.getLatestInCumulativeCountForSequence(
                            server.getServerId(), sequence.getSequence());
                    seqInReqCount = formatterCount.format(Float.parseFloat(seqInReqCount));

                    String seqInFaultCount = statQueryDSClient.getLatestInFaultCountForSequence(
                            server.getServerId(), sequence.getSequence());
                    seqInFaultCount = formatterCount.format(Float.parseFloat(seqInFaultCount));

                    MediationDataBean mediationDataBean = new MediationDataBean();
                    mediationDataBean.setServerURL(server.getServerURL());
                    mediationDataBean.setServiceName(sequence.getSequence() + "-In");
                    mediationDataBean.setOperation("Sequences");
                    mediationDataBean.setAvgTime(seqInAvgTime);
                    mediationDataBean.setMaxTime(seqInMaxTime);
                    mediationDataBean.setMinTime(seqInMinTime);
                    mediationDataBean.setReqCount(seqInReqCount);
                    mediationDataBean.setFaultCount(seqInFaultCount);

                    mediationDataBeanList.add(mediationDataBean);

                    // Out direction..
                    String seqOutAvgTime = statQueryDSClient
                            .getLatestOutAverageProcessingTimeForSequence(server.getServerId(),
                                                                          sequence.getSequence());
                    seqOutAvgTime = formatterTime.format(Float.parseFloat(seqOutAvgTime));

                    String seqOutMaxTime = statQueryDSClient
                            .getLatestOutMaximumProcessingTimeForSequence(server.getServerId(),
                                                                          sequence.getSequence());
                    seqOutMaxTime = formatterTime.format(Float.parseFloat(seqOutMaxTime));

                    String seqOutMinTime = statQueryDSClient
                            .getLatestOutMinimumProcessingTimeForSequence(server.getServerId(),
                                                                          sequence.getSequence());
                    seqOutMinTime = formatterTime.format(Float.parseFloat(seqOutMinTime));

                    String seqOutReqCount = statQueryDSClient.getLatestOutCumulativeCountForSequence(
                            server.getServerId(), sequence.getSequence());
                    seqOutReqCount = formatterCount.format(Float.parseFloat(seqOutReqCount));

                    String seqOutFaultCount = statQueryDSClient.getLatestOutFaultCountForSequence(
                            server.getServerId(), sequence.getSequence());
                    seqOutFaultCount = formatterCount.format(Float.parseFloat(seqOutFaultCount));

                    mediationDataBean = new MediationDataBean();
                    mediationDataBean.setServerURL(server.getServerURL());
                    mediationDataBean.setServiceName(sequence.getSequence() + "-Out");
                    mediationDataBean.setOperation("Sequences");
                    mediationDataBean.setAvgTime(seqOutAvgTime);
                    mediationDataBean.setMaxTime(seqOutMaxTime);
                    mediationDataBean.setMinTime(seqOutMinTime);
                    mediationDataBean.setReqCount(seqOutReqCount);
                    mediationDataBean.setFaultCount(seqOutFaultCount);

                    mediationDataBeanList.add(mediationDataBean);

        %>
        <ul class="child-list">
            <li <%if (sequenceList.length != k) {%> class="vertical-line" <%}%>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>"><a
                        onclick="showHideData(this,'<%=sequence.getSequence()%>','<%=i%><%=j%><%=k%>','L3')"><%=sequence.getSequence()%>
                    <img src="images/up.png" alt="Hide Data" align="top" hspace="5"/></a>

                    <div>
                        <div class="data-chart" id="L3graph_In_A<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ["<fmt:message key="sequence.average.time"/>",'<%=seqInAvgTime%>'],
                                              ["<fmt:message key="sequence.maximum.time"/>",'<%=seqInMaxTime%>'],
                                              ["<fmt:message key="sequence.minimum.time"/>",'<%=seqInMinTime%>']
                                          ], "L3graph_In_A<%=i%><%=j%><%=k%>", 'A');

                        </script>

                        <div class="data-chart" id="L3graph_In_B<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ["<fmt:message key="sequence.request.time"/>",'<%=seqInReqCount%>'],
                                              ["<fmt:message key="sequence.fault.count"/>",'<%=seqInFaultCount%>']
                                          ], "L3graph_In_B<%=i%><%=j%><%=k%>", 'B');

                        </script>
                        <div style="margin-right:20px; float: right;width:80px">IN Direction</div>
                    </div>
                    <div style="clear:both"></div>
                    <div>
                        <div class="data-chart" id="L3graph_Out_A<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ["<fmt:message key="sequence.average.time"/>",'<%=seqOutAvgTime%>'],
                                              ["<fmt:message key="sequence.maximum.time"/>",'<%=seqOutMaxTime%>'],
                                              ["<fmt:message key="sequence.minimum.time"/>",'<%=seqOutMinTime%>']
                                          ], "L3graph_Out_A<%=i%><%=j%><%=k%>", 'A');

                        </script>

                        <div class="data-chart" id="L3graph_Out_C<%=i%><%=j%><%=k%>"></div>
                        <script type="text/javascript">

                            generateGraph([
                                              ["<fmt:message key="sequence.request.time"/>",'<%=seqOutReqCount%>'],
                                              ["<fmt:message key="sequence.fault.count"/>",'<%=seqOutFaultCount%>']
                                          ], "L3graph_Out_C<%=i%><%=j%><%=k%>", 'C');

                        </script>
                        <div style="margin-right:20px; float: right;width:80px">OUT Direction</div>
                    </div>

                    <span class="BarClear"></span>
                </div>
                <div class="empty-brake"/>
            </li>
        </ul>
        <%
                }
            }
        %>

    </li>
</ul>
<%

    }
%>

</li>
<%

        }


    }
    request.getSession().setAttribute("reportMediationData", mediationDataBeanList);

    statQueryDSClient.cleanup();
    listAdminServiceClient.cleanup();

    if (!hasData) {
%>

<script type="text/javascript">
    if (hasDataAnyWhere) {
        document.getElementById("report_ui").style.display = "";
        document.getElementById("noDataError").style.display = "none";
    } else {
        document.getElementById("report_ui").style.display = "none";
        document.getElementById("noDataError").style.display = "";
    }

</script>
<%
    }

%>
</ul>

</fmt:bundle>
