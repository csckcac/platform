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
<%@ page import="org.wso2.carbon.admin.mgt.ui.clients.AdminManagementClient" %>
<%@ page import="org.wso2.carbon.admin.mgt.ui.utils.PasswordConfigUtil" %>
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="org.wso2.carbon.admin.mgt.stub.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.admin.mgt.ui.i18n.JSResources"
        request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.admin.mgt.ui.i18n.Resources">

    <jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
    <script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
    <script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript" src="js/update_credentials.js"></script>

    <%
        boolean success = false;

        String data = (String) session.getAttribute("intermediate-data");

        PasswordConfigUtil.readIntermediateData(request, data);

        AdminManagementClient client;
        String domain;
        String adminName;
        String userName;
        String confirmationKey;
        try {
            domain = (String) request.getAttribute("tenantDomain");
            adminName = (String) request.getAttribute("admin");
            if (domain.trim().equals("")) {
               userName = adminName;
            } else {
               userName = adminName+"@"+domain;
            }
            confirmationKey = (String) request.getAttribute("confirmationKey");

            client = new AdminManagementClient(config, session);
            success = client.proceedUpdateCredentials(domain, confirmationKey);
        } catch (RegistryException e) {
    %>
    <div>Error in validating the contact.</div>
    <%
            return;
        }

    %>

    <% if (success) {
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

        if ("failed".equals(session.getAttribute("captcha-status"))) {
            session.setAttribute("captcha-status", null);
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('Please enter the letters shown as in the image to change your password.');
        });
    </script>
    <%
        }

    %>

    <%
        if ("true".equals(session.getAttribute("update-credentials-failed"))) {
            session.removeAttribute("update-credentials-failed");
    %>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('Credentials Update Failed. Pls retry updating the credentials.');
        });
    </script>
    <%
        }
    %>

    <div id="middle">
        <h2>
            <fmt:message key="password.reset"/>
        </h2>

        <p><fmt:message key="verified.request.successfully"/>
        </p>
    </div>
    <div id="workarea">
        <form id="updateCredentialsForm" action="update_credentials_ajaxprocessor.jsp"
              method="post">

            <table class="styledLeft">
                <tbody>
                <tr>
                    <td class="nopadding">
                        <table class="normal-nopadding" cellspacing="0">
                            <tbody>
                            <tr>
                                <td><fmt:message key="username"/></td>
                                <td colspan="2"><input readonly="true" type="text" name="username"
                                                       id="username"
                                                       style="width:400px" value="<%=userName%>"/>
                            </tr>
                            <tr>
                                <td><fmt:message key="new.admin.password"/>
                                    <span class="required">*</span></td>
                                <td colspan="2"><input type="password" tabindex="1" name="admin-password"
                                                       id="admin-password"
                                                       style="width:400px"/></td>
                            </tr>
                            <tr>
                                <td colspan="2">(Minimum of 6 Characters in length)</td>
                            </tr>
                            <tr>
                                <td><fmt:message key="new.admin.password.repeat"/>
                                    <span class="required">*</span></td>
                                <td colspan="2"><input type="password" tabindex="2" name="admin-password-repeat"
                                                       id="admin-password-repeat"
                                                       style="width:400px"/></td>
                            </tr>
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
                                <td colspan="2" height="100"><input type="text" tabindex="3"
                                                                    id="captcha-user-answer"
                                                                    name="captcha-user-answer"
                                                                    style="width:400px"
                                                                    value=""/></td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>

                <tr id="buttonRow">
                    <td class="buttonRow">
                        <input type="hidden" name="captcha-secret-key"
                               value="<%=captchaSecretKey%>"/>
                        <input type="hidden" name="admin" id="admin"
                               value="<%=adminName%>"/>
                        <input type="hidden" name="domain" id="domain"
                               value="<%=domain%>"/>
                        <input type="hidden" name="confirmationKey"
                               value="<%=confirmationKey%>"/>
                        <input class="button" type="button"
                               value="Update" onclick="updateCredentials()"/>
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
        </form>
    </div>
    <script type="text/javascript">
        showCaptcha('<%=captchaImageUrl%>');
    </script>

    <% } else { %>
    <div id="middle">
        <h2>
            <fmt:message key="password.reset.failed"/>
        </h2>

        <p><fmt:message key="request.verification.failed"/></p>

        <p>
            You can retry resetting the password <a href="forgot_password.jsp">here</a> .
        </p>
    </div>
    <%
        }
    %>

</fmt:bundle>
