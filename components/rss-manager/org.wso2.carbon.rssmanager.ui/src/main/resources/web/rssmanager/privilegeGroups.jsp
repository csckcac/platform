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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.PrivilegeGroup" %>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Database Instances"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript" src="js/uiValidator.js"></script>
    <%
        Log log = LogFactory.getLog(this.getClass());
        RSSManagerClient client = null;
        String privGroupName;

        try {
            String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(
                            CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());

        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage();
            log.error(errorMsg);
        }
    %>

    <%
        List<PrivilegeGroup> privGroups;
        if (client != null) {
            try {
                privGroups = client.getAllPrivilegeGroups();

                boolean hasRecords = false;
                if (privGroups.size() > 0) {
                    hasRecords = true;
                }
    %>
    <div id="middle">
        <h2><fmt:message key="rss.manager.user.privilege.groups"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="dataForm">
                <table class="styledLeft" id="privilegeGroupTable">
                    <% if (hasRecords) {%>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.privilege.group.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (int i = 0; i < privGroups.size(); i++) {
                            PrivilegeGroup privGroup = privGroups.get(i);
                            if (privGroup != null) {
                                privGroupName = privGroup.getPrivGroupName();
                    %>
                    <tr id="tr_<%=privGroupName%>">
                        <td id="td_<%=privGroup.getPrivGroupId()%>"><%=privGroupName%>
                        </td>
                        <td>
                                <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
                               href="javascript:submitEditForm('<%=privGroup.getPrivGroupId()%>')"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="removePrivilegeGroup(this, '<%=privGroup.getPrivGroupId()%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    </tr>
                    <%
                                        }
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                    %>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td colspan="4">
                            <a class="icon-link"
                               style="background-image:url(../admin/images/add.gif);"
                               href="addPrivilegeGroup.jsp"><fmt:message
                                    key="rss.manager.add.new.privilege.group"/></a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <script type="text/javascript">
                function submitEditForm(privGroupId){
                    document.getElementById('privGroupId').value = privGroupId;
                    document.getElementById('editForm').submit();
                }
            </script>
            <form method="post" action="editPrivilegeGroup.jsp" id="editForm">
                <input type="hidden" name="privGroupId" id="privGroupId" />
            </form>
        </div>
    </div>
</fmt:bundle>
