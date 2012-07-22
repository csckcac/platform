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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstanceMetaData" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Create database"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <%
        RSSManagerClient client = null;
        int systemRSSInstanceCount = 0;
        List<RSSInstanceMetaData> rssInstances = new ArrayList<RSSInstanceMetaData>();
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext,
                    request.getLocale());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                    CarbonUIMessage.ERROR, request, e);
        }
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.new.database"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="addDatabaseForm"
                  id="addDatabaseForm">
                <%
                    if (client != null) {
                        try {
                            systemRSSInstanceCount = client.getSystemRSSInstanceCount();
                            rssInstances = client.getRSSInstanceList();
                        } catch (Exception e) {
                            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                    CarbonUIMessage.ERROR, request, e);
                        }
                %>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>Create New Database</th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><select id="rssInstances"
                                                name="rssInstances">
                                        <%
                                            if (systemRSSInstanceCount > 0) {
                                                String rssInstanceType =
                                                        RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE;
                                        %>
                                        <option id="<%=rssInstanceType%>"
                                                value="<%=rssInstanceType%>">WSO2_RSS
                                        </option>
                                        <%
                                            }
                                            if (rssInstances.size() > 0 && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                                                for (RSSInstanceMetaData rssIns : rssInstances) {
                                                    if (rssIns != null) {
                                        %>
                                        <option id="<%=rssIns.getName()%>"
                                                value="<%=rssIns.getName()%>"><%=rssIns.getName()%>
                                        </option>
                                        <%
                                                        }
                                                    }
                                                }
                                            }
                                        %>
                                    </select></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message key="rss.manager.db.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="databaseName"
                                               name="databaseName"
                                               size="30" type="text"><font
                                            color='black'><%=(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ? "" : "_" + RSSManagerHelper.processDomainName(tenantDomain)%></font></td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.create"/>"
                                   onclick="return createDatabase()"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'databases.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>