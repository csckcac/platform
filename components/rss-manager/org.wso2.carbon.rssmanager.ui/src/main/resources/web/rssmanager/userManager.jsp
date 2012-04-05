<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
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

<script type="text/javascript" language="JavaScript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="admin.console.header"/>

    <%
        String strDatabaseInsId = request.getParameter("dbInsId");
        int dbInsId = (strDatabaseInsId == null) ? 0 : Integer.parseInt(strDatabaseInsId);
        String strRssInsId = request.getParameter("rssInsId");
        int rssInsId = (strRssInsId == null) ? 0 : Integer.parseInt(strRssInsId);

        if (rssInsId != 0) {
            session.setAttribute(RSSManagerConstants.RSS_INSTANCE_ID, rssInsId);
        }
        if (dbInsId != 0) {
            session.setAttribute(RSSManagerConstants.DATABASE_INSTANCE_ID, dbInsId);
        }
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.user.management"/></h2>

        <div id="workArea">
            <form method="post" name="dataForm">
                <table class="styledLeft" id="dbGeneralInfo">
                    <tbody>

                    <tr>
                        <td width="20%"><fmt:message key="rss.manager.users"/></td>
                        <td width="80%"><a class="icon-link"
                               style="background-image:url(images/add.gif);"
                               onclick="redirectToUsersPage(<%=rssInsId%>, <%=dbInsId%>)"
                               href="#"><fmt:message
                                key="rss.manager.users"/></a></td>
                    </tr>
                    <tr>
                        <td width="20%"><fmt:message key="rss.manager.user.privilege.groups"/></td>
                        <td width="80%"><a class="icon-link"
                               style="background-image:url(images/add.gif);"
                               onclick="redirectToPrivilegeGroupsPage(<%=rssInsId%>, <%=dbInsId%>)"
                               href="#"><fmt:message
                                key="rss.manager.user.privilege.groups"/></a></td>
                    </tr>
                    </tbody>
                    <tr>
                        <br/>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.back"/>"
                                   onclick="document.location.href = 'databases.jsp'"/>
                        </td>
                    </tr>
                </table>
                <input id="flag" name="flag" type="hidden" value="add"/>
                <br/>
            </form>
        </div>
    </div>
</fmt:bundle>

