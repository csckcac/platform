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
<%@ page import="org.wso2.carbon.admin.mgt.ui.clients.AdminManagementClient" %>
<%@ page import="org.wso2.carbon.admin.mgt.stub.beans.xsd.ConfirmationBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
        String data = null;
        String redirect = null;
        try {
            AdminManagementClient client = new AdminManagementClient(config,session);
            String confirm = request.getParameter("confirmation");
            
            ConfirmationBean confirmationBean = client.confirmUser(confirm);
            data = confirmationBean.getData();
            redirect = confirmationBean.getRedirectPath();
        } catch (Exception ignore) {
            String errorRedirect = (String)session.getAttribute("email-verification-error-redirect");
            if (errorRedirect != null) {
                session.removeAttribute("email-verification-error-redirect");
                response.sendRedirect(errorRedirect);
                return;
            }
            response.sendRedirect("expired_reset_link.jsp");
            return;
        }
        session.setAttribute("intermediate-data", data);
        response.sendRedirect(redirect);
%>



