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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstanceEntry" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="java.util.List" %>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Database Instances"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript" src="js/uiValidator.js"></script>
    <%
        RSSManagerClient client = null;
        String instanceName;
        String tenantDomain = null;

        try {
            String backendServerUrl =
                    CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(
                            CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new RSSManagerClient(cookie, backendServerUrl, configContext,
                    request.getLocale());
            tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);

        } catch (Exception e) {
        }
    %>

    <%
        List<RSSInstanceEntry> instances;
        if (client != null) {
            try {
                instances = client.getRSSInstanceList();
                boolean hasRecords = false;
                if (instances.size() > 0) {
                    hasRecords = true;
                }
    %>
    <div id="middle">
        <h2><fmt:message key="rss.manager.instances"/></h2>

        <div id="workArea">
            <form method="post" action="instances.jsp" name="instanceDataForm"
                  onsubmit="return validateInstanceDataForm();">
                <table class="styledLeft" id="instanceTable">
                    <% if (hasRecords) {%>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.instance.name"/></th>
                        <th width="20%"><fmt:message key="rss.manager.tenant.domain"/></th>
                        <th width="20%"><fmt:message key="rss.manager.server.category"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (RSSInstanceEntry instance : instances) {
                            if (instance != null) {
                                instanceName = instance.getName();
                    %>
                    <tr id="tr_<%=instanceName%>">
                        <td id="td_<%=instanceName%>"><%=instanceName%>
                        </td>
                        <td><%=instance.getTenantDomainName()%>
                        </td>
                        <td><%=instance.getServerCategory()%>
                        </td>
                        <%
                            if (tenantDomain == null) {
                                if (!RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE.equals(
                                        instance.getInstanceType()) ||
                                        RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE.equals(
                                                instance.getInstanceType())) {

                        %>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               onclick="submitEditForm(this, <%=instance.getRssInstanceId()%>)"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="removeInstance(this, '<%=instance.getRssInstanceId()%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    </tr>
                    <%
                        } else if (RSSManagerConstants.STRATOS_RSS.equals(instance.getTenantDomainName())) {
                            %>
                         <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               onclick="submitEditForm(this, <%=instance.getRssInstanceId()%>)"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="removeInstance(this, '<%=instance.getRssInstanceId()%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    </tr>
                    <%
                        }
                    } else {
                        if (!RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(instance.getInstanceType())) {
                    %>
                    <td>
                        <a class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"
                           onclick="submitEditForm(this, <%=instance.getRssInstanceId()%>)"><fmt:message
                                key="rss.manager.edit.instance"/></a>
                        <a class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"
                           href="#"
                           onclick="removeInstance(this, '<%=instance.getRssInstanceId()%>');"><fmt:message
                                key="rss.manager.drop.instance"/></a>
                    </td>
                    <%
                                                }
                                            }
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
                               href="addInstance.jsp"><fmt:message
                                    key="rss.manager.add.new.instance"/></a>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
            <script type="text/javascript">
                function submitEditForm(obj, rssInsId) {
                    var rowId = $(obj).parents('tr:eq(0)').attr('id');
                    var instanceName = rowId.substring("tr_".length, rowId.length);
                    document.getElementById('rssInsId').value = rssInsId;
                    document.getElementById('instanceName').value = instanceName;
                    document.getElementById('editForm').submit();
                }
            </script>
            <form action="editInstance.jsp" method="post" id="editForm">
                <input id="rssInsId" name="rssInsId" type="hidden"/>
                <input id="instanceName" name="instanceName" type="hidden"/>
            </form>
        </div>
    </div>
</fmt:bundle>
