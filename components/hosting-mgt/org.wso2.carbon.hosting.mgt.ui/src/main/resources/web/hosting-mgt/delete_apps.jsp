<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.hosting.mgt.ui.HostingAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%
    String[] phpAppFileNames = request.getParameterValues("appFileName");
    String pageNumber = request.getParameter("pageNumber");
    String deleteAllPhpapps = request.getParameter("deleteAllPhpapps");
    String cartridge = request.getParameter("cartridge");

    int pageNumberInt = 0;
    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }
%>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    ResourceBundle bundle = ResourceBundle
            .getBundle(HostingAdminClient.BUNDLE, request.getLocale());

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    HostingAdminClient client;
    try {
        client = new HostingAdminClient( request.getLocale(),cookie,configContext, backendServerURL);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    try {
        if (deleteAllPhpapps != null) {
            client.deleteAllApps(cartridge);
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.deleted.all.apps"),
                                                                CarbonUIMessage.INFO, request);
        }else {
            client.deleteApps(phpAppFileNames, cartridge);
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.deleted.apps"),
                                                                CarbonUIMessage.INFO, request);
        }
%>
<script>
    location.href = 'index.jsp?pageNumber=<%=pageNumberInt%>&cartridges=<%=cartridge%>'
</script>

<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "index.jsp?pageNumber=<%=pageNumberInt%>&cartridges=<%=cartridge%>";
</script>
<%
        return;
    }
%>