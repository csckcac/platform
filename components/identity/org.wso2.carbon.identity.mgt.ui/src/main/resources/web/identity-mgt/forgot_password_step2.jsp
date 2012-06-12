<!--
~ Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>

    <%
        String userName = request.getParameter("userName");
        String userKey = null;
        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityManagementClient client =
                    new IdentityManagementClient(backendServerURL, configContext) ;
            CaptchaInfoBean bean = new CaptchaInfoBean();
            bean.setSecretKey(request.getParameter("captcha-secret-key"));
            bean.setUserAnswer(request.getParameter("captcha-user-answer"));
            VerificationBean verificationBean = client.verifyUser(userName, bean);
            if(verificationBean.getVerified() && verificationBean.getKey() != null){
                userKey = verificationBean.getKey();
            } else{
                response.sendRedirect("forgot_password_step1.jsp");
            }

        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR,
                request);
    %>
            <script type="text/javascript">
                location.href = "index.jsp";
            </script>
    <%
            return;
        }
    %>
    <div id="middle">
        <h2>
            How do you want to reset your password?
        </h2>
    </div>

    <script type="text/javascript">

        function doSubmit(){
            
            var sendEmail = document.getElementById("sendEmail");
            var secretQuestions = document.getElementById("secretQuestions");
            if(sendEmail.checked){
                passwordResetMethodForm.action  = "forgot_password_send_email.jsp";
            } else if(secretQuestions.checked){
                passwordResetMethodForm.action  = "forgot_password_secret_questions_step1.jsp";
            }

            passwordResetMethodForm.submit();
        }

        function cancel(){
            location.href = "../admin/login.jsp";
        }        
    </script>

    <form action="" id="passwordResetMethodForm"  method="post">
    <table>
        <tbody>
        <tr>
            <td><input type="radio" name="passwordResetMethod" id="sendEmail"/></td>
            <td>Send an email</td>
        </tr>
        <tr>
            <td><input type="radio" name="passwordResetMethod" id="secretQuestions"/></td>
            <td>Secret Questions</td>
        </tr>
        <tr>
            <td><input type="hidden"  name="userName" id="userName"
                       value="<%=userName%>" /></td>
            <td><input type="hidden"  name="userKey" id="userKey"
                       value="<%=userKey%>" /></td>
        </tr>
        <tr>
            <td>
                <input type="button" value="Cancel"  onclick="cancel()"/>
                <input type="button" value="Next" onclick="doSubmit()"/>
            </td>
        </tr>
        </tbody>
    </table>
    </form>
</fmt:bundle>

