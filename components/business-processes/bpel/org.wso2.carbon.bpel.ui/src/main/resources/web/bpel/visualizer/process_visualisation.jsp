<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend.util.ProcessModelUtil" %>
<%@ page
        import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.ChangeSettingsUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend.ProcessModelBean" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter.AuthenticationManager" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!--
~ Copyright (c) 20011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%
    /**Storing the backend server url and session cookie to be reused during visualization tasks*/
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AuthenticationManager.init(backendServerURL, cookie, configContext);

    final Log log = LogFactory.getLog("process_visualization.jsp");
    String processID = request.getParameter("processID").trim();
    if (processID == null) {
        log.error("Pid is null", new NullPointerException("Pid is null"));
    }
    String mainBeanId = MainBeanUtil.generateMainBeanId(processID);

    /* Get the MainBean from the session */
    MainBean bean = (MainBean) session.getAttribute(mainBeanId);
    if (bean == null) {
        bean = new ProcessModelBean(processID);
        session.setAttribute(mainBeanId, bean);
    }

    int rowIndex = ProcessModelUtil.getRowIndexForProcessModelID(bean, processID);


    //we need to selectProcessModel only at the first load of the page.
    String perProcessModelSessionId = "firstTimeLoad:" + processID;
    if (session.getAttribute(perProcessModelSessionId) == null) {
        session.setAttribute(perProcessModelSessionId, true);
    }
    if (session.getAttribute(perProcessModelSessionId) != null && (Boolean)session.getAttribute(perProcessModelSessionId)) {
        ChangeSettingsUtil.selectProcessModel(bean, rowIndex);
        session.setAttribute(perProcessModelSessionId, false);
    }

%>

<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/bpimain.css" />
<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/slider.css" />
<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/svg.css" />

<script type="text/javascript" src="visualizer/resources/scripts/main.js"></script>
<script type="text/javascript" src="visualizer/resources/scripts/slider.js"></script>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<div id="bpi-body">
    <form id="bpiform" name="bpiform"
          enctype="application/x-www-form-urlencoded">
        <div id=svg-viewer>
            <table id="form:svg:table" class="bpi">
                <thead>
                </thead>
                <tbody>
                <tr>
                    <td class="center">
                        <jsp:include page="svg_generator-ajaxprocessor.jsp">
                            <jsp:param name="id" value="<%=processID%>" />
                        </jsp:include>
                    </td>
                </tr>
                </tbody>
            </table>

        </div>
    </form>
</div>
</fmt:bundle>