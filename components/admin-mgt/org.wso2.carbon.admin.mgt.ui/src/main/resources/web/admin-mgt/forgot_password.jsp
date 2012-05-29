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

<%@ page import="org.wso2.carbon.admin.mgt.stub.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.admin.mgt.ui.utils.PasswordConfigUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script type="text/javascript" src="js/update_credentials.js"></script>


<% String username = "";

    if (session.getAttribute("submit-username") != null) {
        username = (String) session.getAttribute("submit-username");
        session.setAttribute("submit-username", null);
    }
    if (session.getAttribute("submit-domain") != null) {
        session.setAttribute("submit-domain", null);
    }
    if (session.getAttribute("submit-admin") != null) {
        session.setAttribute("submit-admin", null);
    }
%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.admin.mgt.ui.i18n.JSResources"
        request="<%=request%>"/>

<fmt:bundle basename="org.wso2.carbon.admin.mgt.ui.i18n.Resources">


    <script type="text/javascript" src="js/forgot_password.js"></script>
    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>

    <%
        String tip = "Enter your username (foo@abc.com in a cloud set up).";
    %>
    <div id="middle">

        <h2>
            Forgot the password?
        </h2>

        <p>
            Please enter your user name below to reset your account credentials. You will get an email notification soon with the link to reset your password.         You can still <a href="/carbon/admin/login.jsp"> login </a>
        using your old credentials till you reset the password.
        </p>
    </div>

    <%
        CaptchaInfoBean captchaInfoBean;
        try {
            captchaInfoBean = PasswordConfigUtil.generateRandomCaptcha(config, session);
        } catch (Exception e) {
    %>
    <div>
        Error in generating the captcha image.
    </div>
    <%
            return;
        }

        String captchaImagePath = captchaInfoBean.getImagePath();

        String serverUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        // remove the services directory.
        String serverRoot = serverUrl.substring(0, serverUrl.length() - "/services/".length());

        String captchaImageUrl = "../../" + captchaImagePath;
        String captchaSecretKey = captchaInfoBean.getSecretKey();
    %>

    <form id="resetPasswordForm" action="reset_password_processor.jsp" method="post">

        <table>
            <tbody>
            <tr>
            <tr>
                <td><fmt:message key="username"/><span class="required">*</span></td>
                <td colspan="2"><input type="text" tabindex="1" name="username" id="username"
                                       style="width:400px" value="<%=username%>"/></td>
            </tr>
            <tr>
                <td></td>
                <td><%=tip%>
                </td>
            </tr>
            <tr>
                <td><input type="text" name="domain" id="domain" style="display:none;"/></td>
                <td><input type="text" name="admin" id="admin" style="display:none;"/></td>
            </tr>
            <tr></tr>
            <tr>
                <td><fmt:message key="word.verification"/><span
                        class="required">*</span></td>
                <td colspan="2"><fmt:message key="captcha.message"/></td>
            </tr>
            <tr>
                <td></td>
                <td colspan="2">
                    <div id="captchaImgDiv"></div>
                </td>
            </tr>
            <tr>
                <td></td>
                <td colspan="2" height="100"><input type="text" tabindex="2"
                                                    id="captcha-user-answer"
                                                    name="captcha-user-answer"
                                                    style="width:400px"
                                                    value=""/></td>
            </tr>
            <tr id="buttonRow">
                <td class="buttonRow">
                <input type="hidden" name="captcha-secret-key"
                           value="<%=captchaSecretKey%>"/>
                <input type="button" tabindex="2" value="Reset Password"
                           onclick="getDomainFromUserName(); getTenantAwareUserName(); initiatePasswordReset();">
                </td>
            </tr>
            <tr id="waitMessage" style="display:none">
                <td>
                    <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;">
                        <img
                                src="images/ajax-loader.gif" align="left" hspace="20"/>Please
                        wait until the Service is imported to the Registry.
                    </div>
                </td>
            </tr>
            </tbody>
        </table>

        <script type="text/javascript">
              showCaptcha('<%=captchaImageUrl%>');
        </script>
    </form>
</fmt:bundle>
