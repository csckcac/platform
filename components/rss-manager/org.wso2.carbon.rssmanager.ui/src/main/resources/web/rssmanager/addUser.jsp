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
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.PrivilegeGroup" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabaseInstanceEntry" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" language="JavaScript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="admin.console.header"/>

    <%
        String strDatabaseInsId = request.getParameter("dbInsId");
        int dbInsId = (strDatabaseInsId == null) ? 0 : Integer.parseInt(strDatabaseInsId);
//        String strRssInsId = request.getParameter("rssInsId");
//        int rssInsId = (strRssInsId == null) ? 0 : Integer.parseInt(strRssInsId);

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        username = (username == null) ? "" : username;
        password = (password == null) ? "" : password;

        RSSManagerClient client = null;
        DatabaseInstanceEntry db = null;

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
            db = client.getDatabaseInstance(dbInsId);
        } catch (Exception e) {
        }

    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.add.new.user"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="dataForm" onsubmit="return validatePrivileges();">
                <table class="styledLeft" id="dbGeneralInfo">
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.property.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <input type="hidden" name="dbInsId" id="dbInsId"
                           value="<%=(db != null) ? db.getDbInstanceId() : 0%>"/>
                    <tr>
                        <td><fmt:message key="rss.manager.permissions.username"/></td>
                        <td><input type="text" id="username" name="username" value="<%=username%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rss.manager.permissions.password"/></td>
                        <td><input type="password" id="password" name="password"
                                   value="<%=password%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rss.manager.default.user.repeat.password"/></td>
                        <td><input type="password" id="repeatPassword" name="repeatPassword"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rss.manager.privilege.group"/></td>
                        <td>
                            <select id="privGroupList" name="privGroupList">
                                <option value="">----SELECT----</option>
                                <%
                                    if (client != null) {
                                        try {
                                            List<PrivilegeGroup> privGroups =
                                                    client.getAllPrivilegeGroups();

                                            if (privGroups.size() > 0) {
                                                for (PrivilegeGroup privGroup : privGroups) {
                                %>
                                <option id="<%=privGroup.getPrivGroupId()%>"
                                        value="<%=privGroup.getPrivGroupName()%>">
                                    <%=privGroup.getPrivGroupName()%>
                                </option>
                                <%
                                                }
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <input id="flag" name="flag" type="hidden" value="add"/>
                    <input id="privGroupId" name="privGroupId" type="hidden"/>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   onclick="return validatePrivileges();"
                                   value="<fmt:message key="rss.manager.save"/>"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="submitCancelForm('<%=(db != null) ? db.getRssInstanceId() : 0%>','<%=(db != null) ? db.getDbInstanceId() : 0%>')"/>

                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <script type="text/javascript">
                function submitCancelForm(rssInsId, dbInsId) {
                    document.getElementById('dbInsId1').value = dbInsId;
                    document.getElementById('rssInsId1').value = rssInsId;
                    document.getElementById('cancelForm').submit();
                }
            </script>
            <form action="users.jsp" method="post" id="cancelForm">
                <input type="hidden" name="dbInsId" id="dbInsId1"/>
                <input type="hidden" name="rssInsId" id="rssInsId1"/>
            </form>
        </div>
    </div>
</fmt:bundle>

