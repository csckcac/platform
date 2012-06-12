<!--
~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.identity.mgt.ui.i18n.Resources"
        request="<%=request%>"/>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">

    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>

    <%
        String userName = request.getParameter("userName");
        String userKey = request.getParameter("userKey");
        String question = request.getParameter("question");
        String answer = request.getParameter("answer");
        session.removeAttribute(IdentityManagementClient.USER_CHALLENGE_QUESTION);
        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityManagementClient client =
                    new IdentityManagementClient(backendServerURL, configContext) ;
            VerificationBean bean = client.verifyChallengeQuestion(userName, userKey, question, answer);
            if(bean != null && bean.getVerified() && bean.getKey() != null &&
                                                            bean.getKey().trim().length() > 0) {
                bean.setUserId(userName);
                session.setAttribute("confirmationBean", bean);
                response.sendRedirect("update_credential.jsp");
            }  else {
%>
    <div>
        Answer is wrong
    </div>
<%              return;
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

</fmt:bundle>

