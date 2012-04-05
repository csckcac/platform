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
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>
<%
    String[] ksNames = null;
    String ksTableDisplay = "display:none;";
    try {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        ksNames = cassandraKeyspaceAdminClient.listKeyspacesOfCurrentUSer();
        if (ksNames != null && ksNames.length > 0) {
            ksTableDisplay = "";
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.keyspaces.msg"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2>
            <h2><fmt:message key="cassandra.keyspaces.msg"/></h2>
        </h2>
        <div id="workArea">
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="formRaw">
                        <table class="styledLeft" id="keyspaceTable" style="<%=ksTableDisplay%>">
                            <thead>
                            <tr>
                                <th width="20%"><fmt:message key="cassandra.keyspace.name"/></th>
                                <th width="60%"><fmt:message key="cassandra.actions"/></th>
                            </tr>
                            </thead>
                            <tbody id="keyspaceBody">
                            <%
                                int j = 0;
                                if (ksNames != null && ksNames.length != 0) {
                                    for (; j < ksNames.length; j++) {
                                        String name = ksNames[j];
                            %>
                            <%
                                if(name.equals("system") || name.equals("definitions")){
                            %>
                                 <tr id="keyspaceRaw<%=j%>">
                                <td id="keyspaceTD<%=j%>">
                                    <a id="keyspaceTDLink<%=j%>"
                                       onclick="location.href = 'keyspace_dashboard.jsp?name=' + '<%=name%>';"
                                       href="#"><%=name%>
                                    </a>
                                </td>
                                <td>
                                    <input type="hidden" name="keyspaceName<%=j%>" id="keyspaceName<%=j%>"
                                           value="<%=name%>"/>
                                    <a class="edit-icon-link" href="#"
                                       onclick="location.href = 'add_edit_keyspace.jsp?region=region1&item=cassandra_ks_mgt_create_menu&mode=edit&name=' + '<%=name%>';"><fmt:message
                                            key="cassandra.actions.edit"/></a>
                                  </td>
                            </tr>
                            <%
                                }else{
                            %>
                            <tr id="keyspaceRaw<%=j%>">
                                <td id="keyspaceTD<%=j%>">
                                    <a id="keyspaceTDLink<%=j%>"
                                       onclick="location.href = 'keyspace_dashboard.jsp?name=' + '<%=name%>';"
                                       href="#"><%=name%>
                                    </a>
                                </td>
                                <td>
                                    <input type="hidden" name="keyspaceName<%=j%>" id="keyspaceName<%=j%>"
                                           value="<%=name%>"/>
                                    <a class="edit-icon-link" href="#"
                                       onclick="location.href = 'share_keyspace_editor.jsp?region=region1&item=cassandra_ks_mgt_create_menu&name=' + '<%=name%>';"
                                       href="#"><fmt:message
                                            key="cassandra.actions.share"/></a>
                                    <a class="edit-icon-link" href="#"
                                       onclick="location.href = 'add_edit_keyspace.jsp?region=region1&item=cassandra_ks_mgt_create_menu&mode=edit&name=' + '<%=name%>';"><fmt:message
                                            key="cassandra.actions.edit"/></a>
                                    <a class="delete-icon-link"
                                       onclick="deleteKeyspace('<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.delete"/></a>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>
                            <input type="hidden" name="keyspaceCount" id="keyspaceCount" value="<%=j%>"/>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>
            <script type="text/javascript">
                alternateTableRows('keyspaceTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </diV>
</fmt:bundle>
