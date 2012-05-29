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

<%@ page import="org.wso2.carbon.admin.mgt.ui.utils.PasswordConfigUtil" %>
<%@ page import="org.wso2.carbon.captcha.mgt.constants.CaptchaMgtConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.admin.mgt.ui.i18n.JSResources"
        request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.admin.mgt.ui.i18n.Resources">
<link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>

<%

    try {
        boolean success = PasswordConfigUtil.initiatePasswordReset(request, config, session);

        if (success) { %>

<div id="middle">

    <h2>
        Password Reset Link Sent.
    </h2>

    <p>
        Please check your mail box for the password reset mail.
    </p>

    <p>
        You can change your log in credentials by clicking the link given in the mail.
    </p>

    <p></p>
</div>


<div id="workArea">
    You can login to your account from the <a href="/carbon/admin/login.jsp"> login page </a>
    using your new credentials after changing them.
</div>
<div id="workArea">
    You will be able to log in using the existing credentials, till you change your password.
</div>
<%
} else { %>
<div id="middle">

    <h2>
        Password Reset Failed
    </h2>

    <p>
        Please check whether you have entered your username correctly and <a
            href="forgot_password.jsp">retry</a>.
    </p>
</div>


<div id="workArea">
    You can login to your account or create a new account from the <a
        href="/carbon/admin/login.jsp"> login page</a>.
</div>
       <div id="middle">
            <p>
                The password reset feature requires you to have a valid email address associated
                with your account, that can receive an email with further information on resetting
                your password. If you have forgotten your username or email address, contact the
                administrator.
            </p>
        </div>


<% }

} catch (Exception e) {
%>
        <div id="middle">

            <h2>
                Password Reset Failed
            </h2>

            <p>
                Please make sure to enter the letters shown as in the image to change
                the password. <a href="forgot_password.jsp">Retry</a> resetting the password.
            </p>
            </div>
        <div id="middle">
            <p>
                The password reset feature requires you to have a valid email address associated
                with your account, that can receive an email with further information on resetting
                your password. If you have forgotten your username or email address, contact the
                administrator.
            </p>
        </div>
<% } %>
</fmt:bundle>