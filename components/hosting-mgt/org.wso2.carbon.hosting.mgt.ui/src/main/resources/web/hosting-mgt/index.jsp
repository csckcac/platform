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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.hosting.mgt.ui.HostingAdminClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    response.setHeader("Cache-Control", "no-cache");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    HostingAdminClient client;



    String[]  phpApps;

     try {
         client = new HostingAdminClient(cookie, serverURL, configContext , request.getLocale());
         phpApps = client.listPhpApps();
    } catch (Exception e) {
         response.setStatus(500);
         CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
         session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
        return;
    }

            ResourceBundle bundle = ResourceBundle.getBundle(HostingAdminClient.BUNDLE, request.getLocale());
    %>

<fmt:bundle basename="org.wso2.carbon.hosting.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="webapp"
                       resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>


<div id="middle">
<h2><fmt:message key="phpapps"/></h2>

<div id="workArea">

   <p>&nbsp;</p>
<form action="delete_phpapps.jsp" name="phpappsForm" method="post">
    <table class="styledLeft" id="webappsTable" width="100%">
        <thead>
        <tr>
            <th width="15%"><fmt:message key="name"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (String phpApp : phpApps) {
        %>
            <tr>
                <td>
                    <%=phpApp.substring(0, phpApp.indexOf(".zip"))%>
                </td>
            </tr>
        <%
            }
        %>

    </table>
    </form>
    <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                              selectAllFunction="selectAllInAllPages()"
                              selectNoneFunction="selectAllInThisPage(false)"
                              resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                              selectAllInPageKey="selectAllInPage"
                              selectAllKey="selectAll"
                              selectNoneKey="selectNone"
                              addRemoveFunction="deleteWebapps()"
                              addRemoveButtonId="delete2"
                              addRemoveKey="delete"/>

    </div>
</div>



</fmt:bundle>