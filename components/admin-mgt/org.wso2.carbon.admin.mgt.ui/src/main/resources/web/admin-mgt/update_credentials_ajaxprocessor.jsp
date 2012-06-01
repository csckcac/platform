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
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.captcha.mgt.constants.CaptchaMgtConstants" %>
<%@ page import="org.wso2.carbon.admin.mgt.stub.exception.xsd.AdminManagementException" %>
<%@ page import="org.wso2.carbon.admin.mgt.ui.clients.AdminManagementClient" %>
<%@ page import="org.wso2.carbon.admin.mgt.ui.utils.PasswordConfigUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.admin.mgt.ui.i18n.JSResources"
        request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.admin.mgt.ui.i18n.Resources">
<%

    try {
        boolean isCredentialsUpdated = PasswordConfigUtil.updatePasswordWithUserInput(
                request, config, session);

        if (isCredentialsUpdated) {
            session.setAttribute("update-credentials-success", "true");

            response.sendRedirect("../admin-mgt/success_update.jsp");
        } else {
            session.setAttribute("update-credentials-failed", "true");
            response.sendRedirect("../admin-mgt/update_verifier.jsp");
        }

    } catch (Exception e) {
        String msg = e.getMessage();
        if (msg.contains(CaptchaMgtConstants.CAPTCHA_ERROR_MSG)) {
            session.setAttribute("captcha-status", "failed");
        } else {
            session.setAttribute("update-credentials-failed", "true");
        }
        response.sendRedirect("../admin-mgt/update_verifier.jsp");
        return;
    }
%>
</fmt:bundle>