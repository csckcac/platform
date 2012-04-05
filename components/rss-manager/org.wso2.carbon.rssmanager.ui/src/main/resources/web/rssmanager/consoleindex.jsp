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
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabaseInstanceEntry" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            request="<%=request%>" i18nObjectName="taskjsi18n"/>
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="admin.console.header"/>
    <%
        RSSManagerClient client = null;
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());

    %>

    <script type="text/javascript" src="js/ui-validations.js"></script>
    <div id="middle">
        <h2><fmt:message key="rss.manager.databases"/></h2>

        <div id="workArea">
            <form method="post" action="consoleindex.jsp" name="dataForm">
                <table class="styledLeft" id="query-table">
                    <%
                        try {
                            List<DatabaseInstanceEntry> databases = client.getDatabaseInstanceList();
                            if (databases != null && databases.size() > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.database"/></th>
                        <th width="20%"><fmt:message key="rss.manager.user"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        String tenantDomain = null;
                        Iterator iterator = databases.iterator();
                        while (iterator.hasNext()) {
                            String db = (String) iterator.next();
                            if (db != null) {
                    %>
                    <tr>
                        <td><%=db%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               href="addDatabase.jsp?tenantDomain=<%=tenantDomain%>"><fmt:message
                                    key="rss.manager.edit.database"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               onclick="deleteQuery(document.getElementById('<%=tenantDomain%>').value);"
                               href="consoleindex.jsp#"><fmt:message
                                    key="rss.manager.delete.database"/></a>
                        </td>
                    </tr>

                    <%
                                    }
                                }
                            }
                        } catch (Exception e) {

                        }
                    %>
                    <tr>
                        <td class="addNewDatabase" colspan="3">
                            <a class="icon-link"
                               style="background-image:url(../admin/images/add.gif);"
                               href="addDatabase.jsp"><fmt:message
                                    key="rss.manager.add.new.database"/></a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
