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
<%@ page import="org.wso2.carbon.bam.ui.report.beans.ServerDataBean" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMListAdminServiceClient" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Data" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Operation" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<div id="noDataError" style="display:none;"><fmt:message key="no.service.data"/></div>
<script type="text/javascript">
    var hasDataAnyWhere = false;
</script>
<ul class="root-list">
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    BAMListAdminServiceClient listAdminServiceClient = new BAMListAdminServiceClient(cookie, serverURL,
            configContext, request.getLocale());

    BAMStatQueryDSClient statQueryDataCacheClient = new BAMStatQueryDSClient(cookie, serverURL,
            configContext, request.getLocale());

    MonitoredServerDTO[] serverList = listAdminServiceClient.getServerList();
//before everything we need to calculate the highest bar width that the bar can go
    Float maxBarWidhA = new Float("0");
    Float maxBarWidhB = new Float("0");
    Float tmpValue = new Float("0");
    if (serverList != null) {
        for (MonitoredServerDTO server : serverList) {


            Data serverData = statQueryDataCacheClient.getLatestDataForServer(server.getServerId());
            if (serverData != null) {
                tmpValue = new Float(serverData.getAvgTime());
                if (maxBarWidhA < tmpValue) {
                    maxBarWidhA = tmpValue;
                }
                tmpValue = new Float(serverData.getMaxTime());
                if (maxBarWidhA < tmpValue) {
                    maxBarWidhA = tmpValue;
                }
                tmpValue = new Float(serverData.getMinTime());
                if (maxBarWidhA < tmpValue) {
                    maxBarWidhA = tmpValue;
                }

                //Graph B stuff
                tmpValue = new Float(serverData.getReqCount());
                if (maxBarWidhB < tmpValue) {
                    maxBarWidhB = tmpValue;
                }
                tmpValue = new Float(serverData.getResCount());
                if (maxBarWidhB < tmpValue) {
                    maxBarWidhB = tmpValue;
                }
                tmpValue = new Float(serverData.getFaultCount());
                if (maxBarWidhB < tmpValue) {
                    maxBarWidhB = tmpValue;
                }

            }
            ServiceDO[] servicesList = listAdminServiceClient.getServicesList(server.getServerId());
            if (servicesList != null) {
                for (ServiceDO service : servicesList) {
                    Data serviceData = statQueryDataCacheClient.getLatestDataForService(service.getId());
                    if (serviceData != null) {
                        tmpValue = new Float(serviceData.getAvgTime());
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }
                        tmpValue = new Float(serviceData.getMaxTime());
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }
                        tmpValue = new Float(serviceData.getMinTime());
                        if (maxBarWidhA < tmpValue) {
                            maxBarWidhA = tmpValue;
                        }

                        //Graph B stuff
                        tmpValue = new Float(serviceData.getReqCount());
                        if (maxBarWidhB < tmpValue) {
                            maxBarWidhB = tmpValue;
                        }
                        tmpValue = new Float(serviceData.getResCount());
                        if (maxBarWidhB < tmpValue) {
                            maxBarWidhB = tmpValue;
                        }
                        tmpValue = new Float(serviceData.getFaultCount());
                        if (maxBarWidhB < tmpValue) {
                            maxBarWidhB = tmpValue;
                        }
                    }
                    Operation[] operationsList = statQueryDataCacheClient.getOperations(service.getId());
                    if (operationsList != null) {
                        for (Operation operation : operationsList) {
                            Data operationData = statQueryDataCacheClient.getLatestDataForOperation(Integer.parseInt(operation.getId()));
                            if (operationData != null) {
                                tmpValue = new Float(operationData.getAvgTime());
                                if (maxBarWidhA < tmpValue) {
                                    maxBarWidhA = tmpValue;
                                }
                                tmpValue = new Float(operationData.getMaxTime());
                                if (maxBarWidhA < tmpValue) {
                                    maxBarWidhA = tmpValue;
                                }
                                tmpValue = new Float(operationData.getMinTime());
                                if (maxBarWidhA < tmpValue) {
                                    maxBarWidhA = tmpValue;
                                }

                                //Graph B stuff
                                tmpValue = new Float(operationData.getReqCount());
                                if (maxBarWidhB < tmpValue) {
                                    maxBarWidhB = tmpValue;
                                }
                                tmpValue = new Float(operationData.getResCount());
                                if (maxBarWidhB < tmpValue) {
                                    maxBarWidhB = tmpValue;
                                }
                                tmpValue = new Float(operationData.getFaultCount());
                                if (maxBarWidhB < tmpValue) {
                                    maxBarWidhB = tmpValue;
                                }
                            }
                        }
                    }

                }
            }
        }
    } // end if (serverList != null)
%>
    <%--Set the javascript global var to get the max width--%>
<script type="text/javascript">
    var maxBarWidhA = <%=maxBarWidhA%>;
    var maxBarWidhB = <%=maxBarWidhB%>;
</script>
<%
    int i = 0;
    List<ServerDataBean> serverDataBeans =new ArrayList<ServerDataBean>();

    String serverAvgTime = null;
    String serverMaxTime = null;
    String serverMinTime = null;
    String serverReqCount = null;
    String serverResCount = null;
    String serverFaultCount = null;
    String serviceAvgTime = null;
    String serviceMaxTime = null;
    String serviceMinTime = null;
    String serviceReqCount = null;
    String serviceResCount = null;
    String serviceFaultCount = null;
    NumberFormat formatterTime = new DecimalFormat("#.##");
    NumberFormat formatterCount = new DecimalFormat("#.##");
    if (serverList != null) {
        for (MonitoredServerDTO server : serverList) {
            i++;


%>
<script type="text/javascript">
    var hasData<%=i%> = false;
</script>
<%
    Data serverData = statQueryDataCacheClient.getLatestDataForServer(server.getServerId());
%>
<li <% if(serverList.length != i){%>class="vertical-line" <% } %> style="display:none;" id="server<%=i%>">
<div class="minus-icon" onclick="treeColapse(this)"></div>
<div class="mediators level1">

    <a onclick="showHideData(this,'<%=server.getServerURL()%>','<%=i%>','L1')"><%=server.getServerURL()%>
        <img src="images/up.png" alt="Hide Data" align="top" hspace="5"/></a>
    <%

        if (serverData != null) {
            serverAvgTime = formatterTime.format(Float.parseFloat(serverData.getAvgTime()));
            serverMaxTime = formatterTime.format(Float.parseFloat(serverData.getMaxTime()));
            serverMinTime = formatterTime.format(Float.parseFloat(serverData.getMinTime()));
            serverReqCount = formatterCount.format(Float.parseFloat(serverData.getReqCount()));
            serverResCount = formatterCount.format(Float.parseFloat(serverData.getResCount()));
            serverFaultCount = formatterCount.format(Float.parseFloat(serverData.getFaultCount()));
            if (Integer.parseInt(serverMinTime) == -1) {
                serverMinTime = "System not ready";
            }
    %>

    <div class="data-chart" id="L1graph_A<%=i%>"></div>
    <script type="text/javascript">
        hasData<%=i%> = true;
        hasDataAnyWhere = true;
    </script>
    <script type="text/javascript">

        generateGraph([
            ["<fmt:message key="server.average.time"/>",'<%=serverAvgTime%>'],
            ["<fmt:message key="server.maximum.time"/>",'<%=serverMaxTime%>'],
            ["<fmt:message key="server.minimum.time"/>",'<%=serverMinTime%>']
        ], "L1graph_A<%=i%>", 'A');


    </script>

    <div class="data-chart" id="L1graph_B<%=i%>"></div>
    <script type="text/javascript">

        generateGraph([
            ["<fmt:message key="server.request.count"/>",'<%=serverReqCount%>'],
            ["<fmt:message key="server.response.count"/>",'<%=serverResCount%>'],
            ["<fmt:message key="server.fault.count"/>",'<%=serverFaultCount%>']
        ], "L1graph_B<%=i%>", 'B');

    </script>
    <span class="BarClear"></span>
    <%
        }
    %>
</div>
<%
    if (serverData != null) { %>
<div class="branch-node"></div>
<% } else { %>
<div class="empty-brake"/>
<% }
    ServiceDO[] servicesList = listAdminServiceClient.getServicesList(server.getServerId());
    int j = 0;
    if (servicesList != null) {
        for (ServiceDO service : servicesList) {
            j++;

%>
<ul class="child-list">
    <li <% if(servicesList.length != j){%>class="vertical-line" <% } %>>
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators level2">
            <a onclick="showHideData(this,'<%=service.getName()%>','<%=i%><%=j%>','L2')"><%=service.getName()%><img
                    src="images/up.png" alt="Hide Data" align="top"
                    hspace="5"/></a>

            <%
                Data serviceData = statQueryDataCacheClient.getLatestDataForService(service.getId());
                if (serviceData != null) {
                    serviceAvgTime = formatterTime.format(Float.parseFloat(serviceData.getAvgTime()));
                    serviceMaxTime = formatterTime.format(Float.parseFloat(serviceData.getMaxTime()));
                    serviceMinTime = formatterTime.format(Float.parseFloat(serviceData.getMinTime()));
                    serviceReqCount = formatterCount.format(Float.parseFloat(serviceData.getReqCount()));
                    serviceResCount = formatterCount.format(Float.parseFloat(serviceData.getResCount()));
                    serviceFaultCount = formatterCount.format(Float.parseFloat(serviceData.getFaultCount()));

            %>
            <script type="text/javascript">
                hasData<%=i%> = true;
                hasDataAnyWhere = true;
            </script>

            <div class="data-chart" id="L2graph_A<%=i%><%=j%>"></div>
            <script type="text/javascript">

                generateGraph([
                    ["<fmt:message key="service.average.time"/>",'<%=serviceAvgTime%>'],
                    ["<fmt:message key="service.maximum.time"/>",'<%=serviceMaxTime%>'],
                    ["<fmt:message key="service.minimum.time"/>",'<%=serviceMinTime%>']
                ], "L2graph_A<%=i%><%=j%>", 'A');

            </script>
            <div class="data-chart" id="L2graph_B<%=i%><%=j%>"></div>
            <script type="text/javascript">
                generateGraph([
                    ["<fmt:message key="service.request.count"/>",'<%=serviceReqCount%>'],
                    ["<fmt:message key="service.response.count"/>",'<%=serviceResCount%>'],
                    ["<fmt:message key="service.fault.count"/>",'<%=serviceFaultCount%>']
                ], "L2graph_B<%=i%><%=j%>", 'B');

            </script>
            <span class="BarClear"></span>
            <%
                }
            %>
        </div>
        <div class="branch-node"></div>
        <%
            Operation[] operationsList = statQueryDataCacheClient.getOperations(service.getId());
            int k = 0;
            if (operationsList != null) {
                for (Operation operation : operationsList) {
                    k++;
        %>

        <ul class="child-list">
            <li <% if(operationsList.length != k){%>class="vertical-line" <% } %>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators level3">
                    <%
                        Data operationData = statQueryDataCacheClient.getLatestDataForOperation(Integer.parseInt(operation.getId()));
                        if (operationData != null) {
                            String opAvgTime = formatterTime.format(Float.parseFloat(operationData.getAvgTime()));
                            String opMaxTime = formatterTime.format(Float.parseFloat(operationData.getMaxTime()));
                            String opMinTime = formatterTime.format(Float.parseFloat(operationData.getMinTime()));
                            String opReqCount = formatterCount.format(Float.parseFloat(operationData.getReqCount()));
                            String opResCount = formatterCount.format(Float.parseFloat(operationData.getResCount()));
                            String opFaultCount = formatterCount.format(Float.parseFloat(operationData.getFaultCount()));

                                ServerDataBean serverDataBean = new ServerDataBean();
                                serverDataBean.setOperationName(operation.getName());
                                serverDataBean.setOpAvgResTime(opAvgTime);
                                serverDataBean.setOpMaxResTime(opMaxTime);
                                serverDataBean.setOpMinResTime(opMinTime);
                                serverDataBean.setOpMsgReqCount(opReqCount);
                                serverDataBean.setOpMsgResCount(opResCount);
                                serverDataBean.setOpMsgFaultCount(opFaultCount);

                                serverDataBean.setServerURL(server.getServerURL());
                                serverDataBean.setServerAvgResTime(serverAvgTime);
                                serverDataBean.setServerMaxResTime(serverMaxTime);
                                serverDataBean.setServerMinResTime(serverMinTime);
                                serverDataBean.setServerMsgReqCount(serverReqCount);
                                serverDataBean.setServerMsgResCount(serverResCount);
                                serverDataBean.setServerMsgFaultCount(serverFaultCount);

                                serverDataBean.setServiceName(service.getName());
                                serverDataBean.setServiceAvgResTime(serviceAvgTime);
                                serverDataBean.setServiceMaxResTime(serviceMaxTime);
                                serverDataBean.setServiceMinResTime(serviceMinTime);
                                serverDataBean.setServiceMsgReqCount(serviceReqCount);
                                serverDataBean.setServiceMsgResCount(serviceResCount);
                                serverDataBean.setServiceMsgFaultCount(serviceFaultCount);
                                serverDataBeans.add(serverDataBean);
                    %>
                    <script type="text/javascript">
                        hasData<%=i%> = true;
                        hasDataAnyWhere = true;
                    </script>
                    <a onclick="showHideData(this,'<%=operation.getName()%>','<%=i%><%=j%><%=k%>','L3')"><%=operation.getName()%><img
                            src="images/up.png" alt="Hide Data" align="top"
                            hspace="5"/></a>

                    <div class="data-chart" id="L3graph_A<%=i%><%=j%><%=k%>"></div>
                    <script type="text/javascript">
                        generateGraph([
                            ["<fmt:message key="operation.average.time"/>",'<%=opAvgTime%>'],
                            ["<fmt:message key="operation.maximum.time"/>",'<%=opMaxTime%>'],
                            ["<fmt:message key="operation.minimum.time"/>",'<%=opMinTime%>']
                        ], "L3graph_A<%=i%><%=j%><%=k%>", 'A');

                    </script>
                    <div class="data-chart" id="L3graph_B<%=i%><%=j%><%=k%>"></div>
                    <script type="text/javascript">

                        generateGraph([
                            ["<fmt:message key="operation.request.count"/>",'<%=opReqCount%>'],
                            ["<fmt:message key="operation.response.count"/>",'<%=opResCount%>'],
                            ["<fmt:message key="operation.fault.count"/>",'<%=opFaultCount%>']
                        ], "L3graph_B<%=i%><%=j%><%=k%>", 'B');

                    </script>
                    <span class="BarClear"></span>
                    <%
                        }
                    %>
                </div>
                <div class="empty-brake"/>
            </li>
        </ul>
        <%
                }
            }
        %>
        <div class="empty-brake"></div>
    </li>
</ul>
<%
        }
    }
%>
</li>
<script type="text/javascript">
    if (hasData<%=i%>) {
        document.getElementById('server<%=i%>').style.display = "";
    }
</script>
<%
        }
       // String[] reportData = serverDataBeans.toArray(new String[serverDataBeans.size()]);
        request.getSession().setAttribute("reportServerData", serverDataBeans);
    }
    listAdminServiceClient.cleanup();
    statQueryDataCacheClient.cleanup();
%>

</ul>
<script type="text/javascript">
    if (!hasDataAnyWhere) {
        document.getElementById("noDataError").style.display = "";
        document.getElementById("report_ui").style.display = "none";
    } else {
        document.getElementById("noDataError").style.display = "none";
        document.getElementById("report_ui").style.display = "";
    }
</script>
</fmt:bundle>
