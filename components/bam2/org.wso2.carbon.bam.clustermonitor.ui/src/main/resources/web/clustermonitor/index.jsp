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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.common.IUserAdmin" %>
<%@page import="org.wso2.carbon.user.mgt.common.UserStoreInfo"%>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>

<%@page import="org.wso2.carbon.utils.ServerConstants"%><script type="text/javascript" src="extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.userstore.ui.i18n.Resources">

<carbon:breadcrumb label="system.user.store"
		resourceBundle="org.wso2.carbon.userstore.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />


    <script type="text/javascript">
        function deleteUserStore() {
            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.userstore"/> ' + '?', doDelete, null);
        }
        
        function doDelete(){
            location.href = 'delete-finish.jsp';
        }

    </script>
    
    
     <%
        UserStoreInfo userStoreInfo = null;
        try{
            
            userStoreInfo = (UserStoreInfo)session.getAttribute(UserAdminClient.USER_STORE_INFO);
            if(userStoreInfo == null){
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                IUserAdmin proxy =
                    (IUserAdmin) CarbonUIUtil.
                    getServerProxy(new UserAdminClient(cookie, backendServerURL, configContext),
                                         IUserAdmin.class, session);
                userStoreInfo = proxy.getUserStoreInfo();
                session.setAttribute(UserAdminClient.USER_STORE_INFO, userStoreInfo);
            }
        }catch(Exception e){
            CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
   %>
            <jsp:include page="../admin/error.jsp"/>
   <%
            return;
            }
   %>
    


    <div id="middle">
        <h2><fmt:message key="user.management"/></h2>

        <div id="workArea">
            <table width="100%">
            
                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin")) {%>
                <tr>
                     <td>   
                        <table class="styledLeft" id="internal" width="100%">
			                <thead>
                                <tr>
                                    <th><fmt:message key="system.user.store"/></th>
                                </tr>
                            </thead>
                            <tr>
                                <td>
                                <a class="icon-link" style="background-image:url(images/users.gif);" href="../user/user-mgt.jsp"><fmt:message key="users"/></a>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                <a class="icon-link" style="background-image:url(images/user-roles.gif);" href="../role/role-mgt.jsp"><fmt:message key="roles"/></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </table>
                <% } %>
                <% if (!userStoreInfo.isReadOnly() && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/login") &&
                        !userStoreInfo.isPasswordsExternallyManaged()) {%>
                <br />
                <table class="styledLeft">
                <tbody>
                <tr>
                	<td class="middle-header">Change Password</td>
                </tr>
                <tr>
                <td ><a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../userstore/index.jsp" style="background-image: url(images/keys.gif);" class="icon-link"><fmt:message key="change.my.password"/></a></td>
                </tr>
                </tbody>
                </table>
                <% } %>
            
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('internal', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('external', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>