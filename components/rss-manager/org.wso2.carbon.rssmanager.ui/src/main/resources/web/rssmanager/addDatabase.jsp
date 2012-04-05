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
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstanceEntry" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="java.util.List" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Add Database"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <%
        RSSManagerClient client = null;
        RSSInstanceEntry roundRobinSelectedInstance = null;
        String tenantDomain = null;
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
            roundRobinSelectedInstance = client.getRoundRobinAssignedInstance();
            tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage();
        }
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.new.database"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="addDatabaseForm"
                  id="addDatabaseForm">
                <%
                    List<RSSInstanceEntry> instances = null;
                    if (client != null) {
                        try {
                            instances = client.getRSSInstanceList(tenantDomain);
                            } catch (Exception e) {
                                Log log = LogFactory.getLog(this.getClass());
                                log.error(e);
                            }
                %>
                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><select id="instances"
                                                name="instances">
                                        <option value="">-------SELECT-------</option>
                                        <%
                                            if (roundRobinSelectedInstance != null) {
                                        %>
                                        <option id="<%=roundRobinSelectedInstance.getRssInstanceId()%>"
                                                value="WSO2_RSS">WSO2_RSS
                                        </option>
                                        <%
                                            }
                                            if (instances != null && instances.size() > 0) {
                                                for (RSSInstanceEntry rssIns : instances) {
                                                    if (rssIns != null) {
                                        %>
                                        <option id="<%=rssIns.getRssInstanceId()%>"
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
                                    <td><input value="" id="dbName"
                                               name="dbName"
                                               size="30" type="text"></td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.create"/>"
                                   onclick="return validateDatabaseProperties()"/>

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