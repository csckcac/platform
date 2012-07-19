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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilegeTemplate" %>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Database Instances"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript" src="js/uiValidator.js"></script>
    <%
        RSSManagerClient client = null;
        String templateName;
        try {
            String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(
                            CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());

        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }
        
        List<DatabasePrivilegeTemplate> templates;
        if (client != null) {
            try {
                templates = client.getDatabasePrivilegesTemplates();
    %>
    <div id="middle">
        <h2><fmt:message key="rss.manager.database.privilege.templates"/></h2>
        <div id="workArea">
            <form method="post" action="#" name="dataForm">
                <table class="styledLeft" id="privilegeTemplateTable">
                    <%
                        if (templates.size() > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message
                                key="rss.manager.database.privilege.template.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabasePrivilegeTemplate template : templates) {
                            if (template != null) {
                                templateName = template.getName();
                    %>
                    <tr id="tr_<%=templateName%>">
                        <td id="td_<%=template.getName()%>"><%=templateName%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               href="javascript:dispatchEditDatabasePrivilegeTemplateRequest('<%=template.getName()%>')"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="dispatchDropDatabasePrivilegeTemplateRequest('<%=template.getName()%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    </tr>
                    <%
                                }
                            }
                        } else{ %>
                    <tr>
                        <td colspan="2">No templates created yet...</td>
                    </tr>
                    <% } %>
                    </tbody>


                </table>
                <%
                        } catch (Exception e) {
                            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                    CarbonUIMessage.ERROR, request, e);
                        }
                    }
                %>
                <div id="connectionStatusDiv" style="display: none;"></div>
                <a class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"
                   href="createDatabasePrivilegeTemplate.jsp"><fmt:message
                        key="rss.manager.add.new.database.privilege.template"/></a>

                <div style="clear:both"></div>
            </form>
            <script type="text/javascript">
                function dispatchEditDatabasePrivilegeTemplateRequest(privilegeTemplateName) {
                    document.getElementById('privilegeTemplateName').value = privilegeTemplateName;
                    document.getElementById('editForm').submit();
                }
            </script>
            <form method="post" action="editDatabasePrivilegeTemplate.jsp" id="editForm">
                <input type="hidden" name="privilegeTemplateName" id="privilegeTemplateName"/>
            </form>
        </div>
    </div>
</fmt:bundle>
