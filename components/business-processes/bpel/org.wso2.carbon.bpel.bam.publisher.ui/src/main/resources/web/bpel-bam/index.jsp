<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page
        import="org.wso2.carbon.bpel.bam.publisher.ui.clients.BAMPublisherConfigurationUpdateClient" %>
<%@ page import="org.wso2.carbon.bpel.bam.publisher.stub.BamServerInformation" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bpel.bam.publisher.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.bam.configuration.info"
            resourceBundle="org.wso2.carbon.bpel.bam.publisher.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<%
    response.setHeader("Cache-Control",
                       "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String bamURL = request.getParameter("bam_server_url");
    String bamUsername = request.getParameter("bam_server_username");
    String bamPassword = request.getParameter("bam_server_password");
    String thriftPort = request.getParameter("bam_server_thrift_port");
    String configurationUpdated = request.getParameter("bam_server_config_updated");
    boolean isAuthenticatedForProcessManagement =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");

    if (isAuthenticatedForProcessManagement) {
        BAMPublisherConfigurationUpdateClient client =
                new BAMPublisherConfigurationUpdateClient(cookie, backendServerURL,
                                                          configContext, request.getLocale());
        try {
            if (null != configurationUpdated && configurationUpdated.equals("true")) {
                client.updateBAMServerConfiguration(bamURL, bamUsername, bamPassword,
                                                    Integer.parseInt(thriftPort), true);
%>
        <script type="text/javascript">
            CARBON.showInfoDialog("<fmt:message key="bam.server.info.update.success"/>");
        </script>
<%
            } else {
                BamServerInformation bamServerConfiguration = client.getBAMServerConfiguration();
                if (null != bamServerConfiguration) {
                    bamURL = bamServerConfiguration.getServerURL();
                    bamUsername = bamServerConfiguration.getUsername();
                    bamPassword = bamServerConfiguration.getPassword();
                    thriftPort = Integer.toString(bamServerConfiguration.getThriftPort());
                }

            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }
%>

    <div id="middle">
    <div id="instance-list-main">
    <%
        if (isAuthenticatedForProcessManagement) {
    %>
    <%--<link rel="stylesheet" type="text/css" href="../bpel/css/bpel_icon_link.css" />--%>
    <%--<script type="text/javascript" src="../bpel/js/bpel-main.js"></script>--%>

    <h2><fmt:message key="bpel.bam.configuration.information"/></h2>

    <div id="workArea">
        <div id="bam-config">
            <table class="styledLeft" width="100%">
                <form method="POST" action="index.jsp">
                    <tbody>
                    <tr>
                        <td><fmt:message key="bam.server.url"/></td>
                        <td><input type="text" id="bam_server_url" size="20" name="bam_server_url"
                                   value="<%=bamURL%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="bam.server.username"/></td>
                        <td><input type="text" id="bam_server_username" size="20"
                                   name="bam_server_username" value="<%=bamUsername%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="bam.server.password"/></td>
                        <td><input type="password" id="bam_server_password" size="20"
                                   name="bam_server_password" value="<%=bamPassword%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="bam.server.thrift.port"/></td>
                        <td><input type="text" id="bam_server_thrift_port" size="20"
                                   name="bam_server_thrift_port" value="<%=thriftPort%>"/></td>
                    </tr>
                    <tr>
                        <td><input type="hidden" name="bam_server_config_updated" value="true"/>
                        </td>
                        <td><input type="submit" name="update" value="Update"></td>
                    </tr>
                    </tbody>
                </form>
            </table>
            <br/>
        </div>
    </div>
    <%} %>
</fmt:bundle>
