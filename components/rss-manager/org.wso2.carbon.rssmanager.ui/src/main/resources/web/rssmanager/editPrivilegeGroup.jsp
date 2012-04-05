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
<%@ page import="org.wso2.carbon.rssmanager.ui.beans.DatabasePermissions" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilege" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.PrivilegeGroup" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" language="JavaScript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
<carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" label="admin.console.header"/>

<%!
    private Map<String, Object> getPrivMap(DatabasePrivilege[] privs) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (DatabasePrivilege priv : privs) {
            map.put(priv.getPrivName(), priv.getPrivValue());
        }
        return map;
    }
%>
<%
    String privGroupIdString = request.getParameter("privGroupId");
    int privGroupId = (privGroupIdString != null) ? Integer.parseInt(privGroupIdString) : 0;

    RSSManagerClient client;
    PrivilegeGroup pg = null;
    DatabasePermissions permissions = null;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {
        client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
        pg = client.getPrivilegeGroupById(privGroupId);
        permissions = new DatabasePermissions();
        permissions.setPrivilegeMap(this.getPrivMap(pg.getPrivs()));
    } catch (Exception e) {
        String errorMsg = e.getLocalizedMessage();
    }

%>

<div id="middle">
<h2><fmt:message key="rss.manager.add.new.privilege.group"/></h2>

<div id="workArea">
<%
    if (pg != null) {
        try {
%>
<form method="get" action="privGroupProcessor.jsp" name="dataForm" id="dataForm">
    <table class="styledLeft" id="privGroupInfo">
        <thead>
        <tr>
            <th width="20%"><fmt:message key="rss.manager.property.name"/></th>
            <th width="60%"><fmt:message key="rss.manager.value"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><fmt:message
                    key="rss.manager.privilege.group.name"/><% assert pg != null; %></td>
            <td><input type="text" id="privGroupName" name="privGroupName"
                       value="<%=pg.getPrivGroupName()%>" readonly="readonly"/>
            </td>
            <input id="privGroupId" name="privGroupId" value="<%=pg.getPrivGroupId()%>"
                   type="hidden"/>
        </tr>
        </tbody>
    </table>
    <table class="styledLeft" id="dbUserTable">
        <thead>
        <tr>
            <th width="20%"><fmt:message key="rss.manager.permission.name"/></th>
            <th width="60%"><input type="checkbox" id="selectAll" name="selectAll"
                                   onclick="selectAllOptions()"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><fmt:message key="rss.manager.permissions.select"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.SELECT_PRIV).toString())) {%>
            <td><input type="checkbox" name="select_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="select_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.insert"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.INSERT_PRIV).toString())) {%>
            <td><input type="checkbox" name="insert_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="insert_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.update"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.UPDATE_PRIV).toString())) {%>
            <td><input type="checkbox" name="update_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="update_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.delete"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.DELETE_PRIV).toString())) {%>
            <td><input type="checkbox" name="delete_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="delete_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.CREATE_PRIV).toString())) {%>
            <td><input type="checkbox" name="create_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.drop"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.DROP_PRIV).toString())) {%>
            <td><input type="checkbox" name="drop_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="drop_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.grant"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.GRANT_PRIV).toString())) {%>
            <td><input type="checkbox" name="grant_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="grant_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.references"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.REFERENCES_PRIV).toString())) {%>
            <td><input type="checkbox" name="references_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="references_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.index"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.INDEX_PRIV).toString())) {%>
            <td><input type="checkbox" name="index_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="index_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.alter"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.ALTER_PRIV).toString())) {%>
            <td><input type="checkbox" name="alter_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="alter_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.temp.table"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.CREATE_TMP_TABLE_PRIV).toString())) {%>
            <td><input type="checkbox" name="create_tmp_table_priv" checked="checked"/>
            </td>
            <%} else {%>
            <td><input type="checkbox" name="create_tmp_table_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.lock.tables"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.LOCK_TABLES_PRIV).toString())) {%>
            <td><input type="checkbox" name="lock_tables_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="lock_tables_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.view"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.CREATE_VIEW_PRIV).toString())) {%>
            <td><input type="checkbox" name="create_view_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_view_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.show.view"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.SHOW_VIEW_PRIV).toString())) {%>
            <td><input type="checkbox" name="show_view_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="show_view_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.routine"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.CREATE_ROUTINE_PRIV).toString())) {%>
            <td><input type="checkbox" name="create_routine_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_routine_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.alter.routine"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.ALTER_ROUTINE_PRIV).toString())) {%>
            <td><input type="checkbox" name="alter_routine_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="alter_routine_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.execute"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.EXECUTE_PRIV).toString())) {%>
            <td><input type="checkbox" name="execute_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="execute_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.event"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.EVENT_PRIV).toString())) {%>
            <td><input type="checkbox" name="event_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="event_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.trigger"/></td>
            <%if (permissions != null && "Y".equals(permissions.getPermission(RSSManagerConstants.TRIGGER_PRIV).toString())) {%>
            <td><input type="checkbox" name="trigger_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="trigger_priv"/></td>
            <%}%>
        </tr>
        </tbody>
    </table>
    <div id="connectionStatusDiv" style="display: none;"></div>
    <input id="flag" name="flag" type="hidden" value="edit"/>
    <tr>
        <td class="buttonRow" colspan="2">
            <input class="button" type="submit"
                   value="<fmt:message key="rss.manager.save"/>"/>
            <input class="button" type="button"
                   value="<fmt:message key="rss.manager.cancel"/>"
                   onclick="document.location.href='privilegeGroups.jsp'"/>

        </td>
    </tr>
</form>
</div>
<%
    } catch (Exception e) {

    }
} else {
%>
<div>
    <font style="color:red;">PRIVILEGE GROUP DOES NOT EXIST</font>
</div>
<%
    }
%>

</div>
</fmt:bundle>

