<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>

<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.user.mgt.common.FlaggedName" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>


<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<script type="text/javascript" src="resources/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    boolean showFilterMessage = false;
    String[]  subjects = null;

    String filter = (String)request.getParameter("org.wso2.usermgt.role.add.filter");
    String selectedSubjectType = (String)request.getParameter("selectedSubjectType");
    String ruleId = (String)request.getParameter("ruleId");

    if(filter == null | (filter != null && filter.trim().equals(""))){
        filter = "*";
    }

    if(selectedSubjectType == null || selectedSubjectType.trim().equals("")) {
        selectedSubjectType = EntitlementPolicyConstants.SUBJECT_TYPE_ROLES;
    }

    String [] subjectTypes = new String[] {EntitlementPolicyConstants.SUBJECT_TYPE_ROLES,
            EntitlementPolicyConstants.SUBJECT_TYPE_USERS};

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                        CONFIGURATION_CONTEXT);
        UserAdminClient userAdminClient = new UserAdminClient(cookie, backEndServerURL,
                configContext);

        if(selectedSubjectType.equals(EntitlementPolicyConstants.SUBJECT_TYPE_USERS)) {
             subjects = userAdminClient.listUsers(filter);
        } else {
            int i = 0;
            FlaggedName[] roles = userAdminClient.getAllRolesNames();
            String[] roleNames = new String[roles.length];
            for(FlaggedName role : roles) {
                roleNames[i] = role.getItemName();
                i++;
            }
            subjects =roleNames;
        }

        if(subjects == null || subjects.length == 0){
            showFilterMessage = true;
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
    <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    %>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb label="select.subjects"
                       resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

<script type="text/javascript">

    function doAdd() {
        document.dataForm.action = "create-basic-policy.jsp?ruleId=" + '<%=ruleId%>' +
                                   '&selectedSubjectType=' + '<%=selectedSubjectType%>';
        document.dataForm.submit();
    }

    function doCancel() {
        location.href = 'create-basic-policy.jsp?ruleId=' + '<%=ruleId%>';
    }

    function getSelectedSubjectType() {
        var comboBox = document.getElementById("selectedSubjectType");
        var selectedSubjectType = comboBox[comboBox.selectedIndex].value;
        location.href = 'select-subjects.jsp?selectedSubjectType=' + selectedSubjectType + '&ruleId=' +'<%=ruleId%>';
    }

</script>


<div id="middle">
    <h2><fmt:message key="select.subjects"/></h2>

    <script type="text/javascript">
       <% if(showFilterMessage){ %>
            CARBON.showInfoDialog('<fmt:message key="no.subjects.filtered"/>', null, null);
       <% } %>
    </script>

    <div id="workArea">
            <table class="normal">
            <tr>
                <td>
                    <fmt:message key="select.subject.type"/>
                </td>
                <td>
                    <select onchange="getSelectedSubjectType();" id="selectedSubjectType" name="selectedSubjectType">
                    <%
                        for (String subjectType : subjectTypes) {
                            if(subjectType.equals(selectedSubjectType)) {
                    %>
                        <option value="<%=subjectType%>" selected="selected"><%=subjectType%></option>

                    <%
                            } else {
                    %>
                        <option value="<%=subjectType%>"><%=subjectType%></option>

                    <%
                            }  
                        }
                    %>

                    </select>
                </td>
            </tr>
            </table>

            <form name="filterForm" method="post" action="select-subjects.jsp?selectedSubjectType=<%=selectedSubjectType%>">
                <table class="normal">
                    <tr>
                        <td><fmt:message key="list.subjects"/></td>
                        <td>
                            <input type="text" name="org.wso2.usermgt.role.add.filter" value="<%=filter%>"/>
                        </td>
                        <td>
                            <input class="button" type="submit" value="<fmt:message key="subject.search"/>" />
                        </td>
                    </tr>
                </table>
            </form>

            <form method="post" name="dataForm" action="create-basic-policy.jsp">
            <table class="styledLeft">
            <thead>
                <tr>
                    <th><fmt:message key="select.subjects"/></th>
                </tr>
            </thead>
            <tr>
                <td class="formRaw">
                    <table class="normal">
                    <% if (subjects != null && subjects.length > 1) { %>
                        <tr>
                             <td>
                                 <a href="#" onclick="doSelectAll('subjects');"/><fmt:message key="select.all"/></a> |
                                 <a href="#" onclick="doUnSelectAll('subjects');"/><fmt:message key="unSelect.all"/></a>
                             </td>
                         </tr>

                    <%
                        }
                        if (subjects != null && subjects.length > 0) {
                            int i = 1;
                            for (String subject : subjects) {

                                if(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(subject)){
                                  continue;
                                }
                                
                                if(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(subject)){
                                  continue;
                                }

                    %>
                        <tr>
                           <td>
                               <input type="checkbox" name="subjects" value="<%=subject%>" /><%=subject%>
                           </td>
                        </tr>
                    <%
                            i++;
                            }
                        }
                    %>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <input type="submit" class="button" value="<fmt:message key="finish"/>" onclick="doAdd();"/>
                    <input type="button" class="button" value="<fmt:message key="cancel"/>" onclick="doCancel();"/>
                </td>
            </tr>
            </table>
            </form>
        </div>
    </div>
</fmt:bundle>