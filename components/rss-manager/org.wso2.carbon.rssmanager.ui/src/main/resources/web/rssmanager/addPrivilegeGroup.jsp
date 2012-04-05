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
                       topPage="true" request="<%=request%>" label="admin.console.add.new.privilege.group"/>

    <div id="middle">
        <h2><fmt:message key="rss.manager.add.new.privilege.group"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="dataForm" id="dataForm">
                <table class="styledLeft" id="dbGeneralInfo">
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.property.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="rss.manager.privilege.group.name"/></td>
                        <td><input type="text" id="privGroupName" name="privGroupName"/></td>
                    </tr>
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
                            <td><input type="checkbox" id="select_priv" name="select_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.insert"/></td>
                            <td><input type="checkbox" id="insert_priv" name="insert_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.update"/></td>
                            <td><input type="checkbox" id="update_priv" name="update_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.delete"/></td>
                            <td><input type="checkbox" id="delete_priv" name="delete_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.create"/></td>
                            <td><input type="checkbox" id="create_priv" name="create_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.drop"/></td>
                            <td><input type="checkbox" id="drop_priv" name="drop_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.grant"/></td>
                            <td><input type="checkbox" id="grant_priv" name="grant_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.references"/></td>
                            <td><input type="checkbox" id="references_priv" name="references_priv"/>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.index"/></td>
                            <td><input type="checkbox" id="index_priv" name="index_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.alter"/></td>
                            <td><input type="checkbox" id="alter_priv" name="alter_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message
                                    key="rss.manager.permissions.create.temp.table"/></td>
                            <td><input type="checkbox" id="create_tmp_table_priv"
                                       name="create_tmp_table_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.lock.tables"/></td>
                            <td><input type="checkbox" id="lock_tables_priv"
                                       name="lock_tables_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.create.view"/></td>
                            <td><input type="checkbox" id="create_view_priv"
                                       name="create_view_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.show.view"/></td>
                            <td><input type="checkbox" id="show_view_priv" name="show_view_priv"/>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.create.routine"/></td>
                            <td><input type="checkbox" id="create_routine_priv"
                                       name="create_routine_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.alter.routine"/></td>
                            <td><input type="checkbox" id="alter_routine_priv"
                                       name="alter_routine_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.execute"/></td>
                            <td><input type="checkbox" id="execute_priv"
                                       name="execute_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.event"/></td>
                            <td><input type="checkbox" id="event_priv" name="event_priv"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="rss.manager.permissions.trigger"/></td>
                            <td><input type="checkbox" id="trigger_priv" name="trigger_priv"/></td>
                        </tr>
                        </tbody>
                    </table>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <input id="flag" name="flag" type="hidden" value="add"/>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.save"/>"
                                   onclick="return savePrivilegeGroup()"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href='privilegeGroups.jsp'"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>

