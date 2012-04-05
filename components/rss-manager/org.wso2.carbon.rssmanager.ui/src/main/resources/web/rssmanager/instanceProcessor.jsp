<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstance" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%!
    private String getDatabasePrefix(String instanceUrl) {
        if (instanceUrl != null && !"".equals(instanceUrl)) {
            return instanceUrl.split(":")[1];
        }
        return "";
    }
%>
<%
    String instanceName = request.getParameter("instanceName");
    String instanceUrl = request.getParameter("instanceUrl");
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String serverCategory = request.getParameter("serverCategory");
    String instanceType = request.getParameter("instanceType");
    String flag = request.getParameter("flag");
    String dbmsType = this.getDatabasePrefix(instanceUrl);

    RSSManagerClient client;

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);

    if ("save".equals(flag)) {
        RSSInstance rssIns = new RSSInstance();
        rssIns.setName(instanceName);
        rssIns.setServerURL(instanceUrl);
        rssIns.setAdminUsername(username);
        rssIns.setAdminPassword(password);
        rssIns.setDbmsType(dbmsType.toUpperCase());
        rssIns.setServerCategory(serverCategory.toUpperCase());
        if (tenantDomain == null) {
            if (RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE.equals(serverCategory.toUpperCase())) {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            }
        } else {
            rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
        }

        String msg = client.addRSSInstance(rssIns);
        if (msg.contentEquals("Database instance has been successfully added")) {

%>
<script type="text/javascript">
    CARBON.showConfirmationDialog("Database instance has been successfully added");
</script>
<%
} else if (msg.contentEquals("A database instance with the same name already exists")) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("A database instance with the same name already exists");
</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Failed to add the database instance");
</script>
<%
    }
} else if ("remove".equals(flag)) {
    String strRssInsId = request.getParameter("rssInsId");
    int rssInsId = (strRssInsId != null) ? Integer.parseInt(strRssInsId) : 0;
    client.removeDatabaseInstance(rssInsId);
} else if ("edit".equals(flag)) {
    String strRssInsId = request.getParameter("rssInsId");
    int rssInsId = (strRssInsId != null) ? Integer.parseInt(strRssInsId) : 0;
    RSSInstance rssIns = new RSSInstance();
    rssIns.setRssInstanceId(rssInsId);
    rssIns.setName(instanceName);
    rssIns.setServerURL(instanceUrl);
    rssIns.setAdminUsername(username);
    rssIns.setAdminPassword(password);
    rssIns.setDbmsType(dbmsType);
    rssIns.setServerCategory(serverCategory.toUpperCase());
    if (tenantDomain == null) {
        if (RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE.equals(serverCategory.toUpperCase())) {
            rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
        } else {
            rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
        }
    } else {
        rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
    }

    String msg = client.editRSSInstanceInfo(rssIns);
    if (msg.contentEquals("Database instance has been successfully edited")) {

%>
<script type="text/javascript">
    CARBON.showConfirmationDialog("Database instance has been successfully edited");
</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Failed to edit the database instance");
</script>
<%
        }
    }
%>
<script type="text/javascript">
    document.location.href = "instances.jsp?";
</script>
