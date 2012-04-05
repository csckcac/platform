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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<%
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String keyspace = request.getParameter("keyspace");
    String cf = request.getParameter("cf");

    String[] users = null;
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        users = cassandraKeyspaceAdminClient.getAllRoles();
    } catch (Exception e) {
%><%=e.getMessage()%><%
    }
%>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.share.cf.editor"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="cassandra.share.cf"/> <%=":" + cf%>
        </h2>

        <div id="workArea">
            <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                <tbody>
                <tr>
                    <td>
                        <div style="margin-top:10px;">
                            <table border="0" cellpadding="0" cellspacing="0" width="600" id="cfTable"
                                   class="styledInner">
                                <tr>
                                    <td><fmt:message key="cassandra.field.cf"/></td>
                                    <td align="left"><%=cf%>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="cassandra.field.roles"/></td>
                                    <td align="left">
                                        <select class="longInput" name="share_cf_users"
                                                id="share_cf_users">
                                            <% if (users == null) {
                                            %>
                                            <option value="nouser"><fmt:message
                                                    key="cassandra.field.noroles"/></option>
                                            <%
                                            } else {
                                            %>
                                            <option value="selectuser"><fmt:message
                                                    key="cassandra.field.slelectroles"/></option>
                                            <%
                                                for (String user : users) {
                                            %>
                                            <option value="<%=user%>"><%=user%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>

                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="3">

                        <input id="shareCFButton" class="button" name="shareCFButton" type="button"
                               onclick="shareOrClearcf('<%=keyspace%>','<%=cf%>','share'); return false;"
                               href="#"
                               value="<fmt:message key="cassandra.actions.share"/>"/>
                        <input id="clearCFButton" class="button" name="clearCFButton" type="button"
                               onclick="shareOrClearcf('<%=keyspace%>','<%=cf%>','clear'); return false;"
                               href="#"
                               value="<fmt:message key="cassandra.actions.clear"/>"/>
                        <input id="cancelCFButton" class="button" name="cancelCFButton" type="button"
                               onclick="location.href= 'keyspace_dashboard.jsp?name=' + '<%=keyspace%>'; return false;"
                               href="#"
                               value="<fmt:message key="cassandra.actions.cancel"/>"/>

                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
