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
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.beans.DatabaseInstance" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    String dbName = request.getParameter("dbName");
    String flag = request.getParameter("flag");

    RSSManagerClient client;
    String msg;

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    if (!"drop".equals(flag)) {
        try {
            DatabaseInstance db = new DatabaseInstance();
            db.setName(dbName);
            db.setDatabaseInstanceId(0);

            msg = client.createDatabase(db);
            if (msg.contentEquals("Database has been successfully created")) {
%>
<script type="text/javascript">
    CARBON.showConfirmationDialog("Database has been successfully created");
</script>
<%
} else if (msg.contentEquals("A database with the same name already exists")) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("A database with the same name already exists");
</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Failed to create the database");
</script>
<%
            }
        }
        catch (Exception e) {
        }
    } else {
        String strDatabaseInsId = request.getParameter("dbInsId");
        int dbInsId = (strDatabaseInsId == null) ? 0 : Integer.parseInt(strDatabaseInsId);
        client.dropDatabase(dbInsId);
    }

%>
<script type="text/javascript">
    document.location.href = "databases.jsp";
</script>
