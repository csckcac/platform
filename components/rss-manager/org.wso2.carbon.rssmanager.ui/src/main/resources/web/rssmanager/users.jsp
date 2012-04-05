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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabaseInstanceEntry" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabaseUserEntry" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- The v=1 value is appended to the js/uiValidator.js to invalidate the existing cache -->
<script type="text/javascript" src="js/uiValidator.js?v=1" language="javascript"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="admin.console.header"/>

    <div id="middle">
        <h2><fmt:message key="rss.manager.users"/></h2>

        <%

            String tmpRssInsId = request.getParameter("rssInsId");
            if (tmpRssInsId == null || "".equals(tmpRssInsId)) {
                tmpRssInsId = (String) session.getAttribute(RSSManagerConstants.RSS_INSTANCE_ID);
            }
            int rssInsId = (tmpRssInsId != null && !"".equals(tmpRssInsId)) ? Integer.parseInt(tmpRssInsId) : 0;

            String tmpDbInsId = request.getParameter("dbInsId");
            if (tmpDbInsId == null || "".equals(tmpDbInsId)) {
                tmpDbInsId = (String) session.getAttribute(RSSManagerConstants.DATABASE_INSTANCE_ID);
            }
            int dbInsId = (tmpDbInsId != null && !"".equals(tmpDbInsId)) ? Integer.parseInt(tmpDbInsId) : 0;

            RSSManagerClient client = null;
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            DatabaseInstanceEntry db = null;
            try {
                client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
                db = client.getDatabaseInstance(dbInsId);
            } catch (Exception e) {
                String errorMsg = e.getLocalizedMessage();
            }
        %>
        <div id="workArea">
            <form method="post" action="userProcessor.jsp" name="dataForm">
                <table class="styledLeft" id="dbUserTable">
                    <%
                        if (client != null) {
                            try {
                                DatabaseUserEntry[] users = client.getUsersByDatabase(dbInsId);
                                if (users != null && users.length > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.user"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabaseUserEntry user : users) {
                            if (user != null) {
                    %>
                    <tr>
                        <td id="<%=user.getUserId()%>"><%=user.getUsername()%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image: url(../rssmanager/images/db-exp.png);"
                               onclick="submitExploreForm('<%=user.getUsername()%>', '<%=db.getDbUrl()%>','<%=RSSManagerCommonUtil.getJdbcDriverName(db.getDbUrl())%>')"
                               href="#"><fmt:message key="rss.manager.explore.database"/>
                            </a>
                            <%--<a class="icon-link"--%>
                               <%--style="background-image:url(../rssmanager/images/data-sources-icon.gif);"--%>
                               <%--onclick="createDataSource('<%=db.getRssInstanceId()%>','<%=user.getUsername()%>','<%=db.getDbName() + "_" + user.getUserId()%>','<%=db.getDbName()%>','<%=db.getDbInstanceId()%>')"--%>
                               <%--href="#"><fmt:message key="rss.manager.create.datasource"/></a>--%>
                            <%--<a class="icon-link"--%>
                            <a class="icon-link"
                               style="background-image:url(../rssmanager/images/data-sources-icon.gif);"
                               onclick="createDataSource('<%=db.getDbInstanceId()%>','<%=user.getUserId()%>')"
                               href="#"><fmt:message key="rss.manager.create.datasource"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               href="javascript:submitEditForm('<%=db.getRssInstanceId()%>','<%=db.getDbInstanceId()%>','<%=user.getUserId()%>')">
                                <fmt:message key="rss.manager.edit.user"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               onclick="deleteDatabaseUser(<%=user.getUserId()%>,<%=dbInsId%>,<%=user.getRssInstanceId()%>)"
                               href="#"><fmt:message
                                    key="rss.manager.delete.database"/></a>
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
                        <td colspan="3">
                            <a class="icon-link"
                               style="background-image:url(../admin/images/add.gif);"
                               href="javascript:submitAddForm('<%=rssInsId%>', '<%=dbInsId%>')"><fmt:message
                                    key="rss.manager.add.new.user"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.back"/>"
                                   onclick="submitCancelForm('<%=dbInsId%>', '<%=rssInsId%>')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <br/>
            </form>
            <script type="text/javascript">
                function submitEditForm(rssInsId, dbInsId, userId) {
                    document.getElementById('rssInsId2').value = rssInsId;
                    document.getElementById('dbInsId2').value = dbInsId;
                    document.getElementById('userId2').value = userId;
                    document.getElementById('flag').value = 'edit';
                    document.getElementById('editForm').submit();
                }
            </script>
            <form action="editUser.jsp" method="post" id="editForm">
                <input id="rssInsId2" name="rssInsId" type="hidden"/>
                <input id="dbInsId2" name="dbInsId" type="hidden"/>
                <input id="userId2" name="userId" type="hidden"/>
                <input id="flag" name="flag" type="hidden"/>
            </form>
            <script type="text/javascript">
                function submitAddForm(rssInsId, dbInsId){
                    document.getElementById('rssInsId1').value = rssInsId;
                    document.getElementById('dbInsId1').value = dbInsId;
                    document.getElementById('addForm').submit();
                }
            </script>
            <form action="addUser.jsp" method="post" id="addForm">
                <input type="hidden" id="rssInsId1" name="rssInsId"/>
                <input type="hidden" id="dbInsId1" name="dbInsId"/>
            </form>
            <script type="text/javascript">
                function submitCancelForm(rssInsId, dbInsId) {
                    document.getElementById('rssInsId').value = rssInsId;
                    document.getElementById('dbInsId').value = dbInsId;
                    document.getElementById('cancelForm').submit();
                }
            </script>
            <form action="databases.jsp" method="post" id="cancelForm">
                <input type="hidden" name="rssInsId" id="rssInsId"/>
                <input type="hidden" name="dbInsId" id="dbInsId"/>
            </form>
            <script type="text/javascript">
                function submitExploreForm(userName, url, driver) {
                    document.getElementById('userName').value = userName;
                    document.getElementById('url').value = encodeURIComponent(url);
                    document.getElementById('driver').value = encodeURIComponent(driver);
                    document.getElementById('exploreForm').submit();
                }
            </script>
            <form action="../dbconsole/login.jsp" method="post" id="exploreForm">
                <input type="hidden" id="userName" name="userName"/>
                <input type="hidden" id="url" name="url"/>
                <input type="hidden" id="driver" name="driver"/>
            </form>
        </div>
    </div>
</fmt:bundle>