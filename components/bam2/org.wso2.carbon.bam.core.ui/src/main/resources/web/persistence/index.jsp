<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.core.stub.types.ConnectionDTO" %>
<%@ page import="org.wso2.carbon.bam.core.ui.ConnectionAdminClient" %>
<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.core.ui.i18n.Resources">

    <%

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ConnectionAdminClient client = new ConnectionAdminClient(cookie, serverURL, configContext);

    %>

    <div id="middle">
        <h2>Connection Parameters</h2>

        <div id="workArea">

            <%


                if (request.getParameter("configure") != null) {
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");

                    try {
                        client.configureConnectionParameters(username, password);
                    } catch (Exception e) {
                        String errorString = "Failed to configure connection parameters..";
            %>
            <script type="text/javascript">
                jQuery(document).init(function() {
                    CARBON.showErrorDialog('<%=errorString%>');
                });
            </script>

            <%
                    }
                
                String successMessage = "Connection parameters successfully updated..";
            %>
            <script type="text/javascript">
                jQuery(document).init(function() {
                    CARBON.showInfoDialog('<%=successMessage%>');
                });
            </script>

            <%
                }

                String username = "";
                String password = "";

                try {
                    ConnectionDTO params = client.getConnectionParameters();

                    if (params.getUsername() != null) {
                        username = params.getUsername();
                    }

                    if (params.getPassword() != null) {
                        password = params.getPassword();
                    }
                } catch (Exception e) {
                    String errorString = "Failed to fetch connection parameters..";
            %>

            <script type="text/javascript">
                jQuery(document).init(function() {
                    CARBON.showErrorDialog('<%=errorString%>');
                });
            </script>

            <%
                }
            %>

            <script type="text/javascript">

                function validate() {
                    value = document.getElementsByName("username")[0].value;
                    if (value === '') {
                        CARBON.showWarningDialog("<fmt:message key="username.is.required"/>");
                        return false;
                    }

                    value = document.getElementsByName("password")[0].value;
                    if (value === '') {
                        CARBON.showWarningDialog("<fmt:message key="password.is.required"/>");
                        return false;
                    }

                    document.tableForm.submit();

                    return true;
                }

                function cancel() {
                    location.href = "index.jsp";
                }

            </script>

            <form id="tableForm" name="tableForm" action="index.jsp" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><span style="float: left; position: relative; margin-top: 2px;">
                                <fmt:message key="configure.parameters"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td width="180px"><fmt:message key="username"/> <span
                                            class="required">*</span></td>
                                    <td>
                                        <input name="username" value="<%= username%>"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message
                                            key="password"/><span class="required"> *</span>
                                    </td>
                                    <td>
                                        <input type="password" name="password" value="<%= password%>" />
                                    </td>
                                </tr>

                                </tbody>
                            </table>

                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input type="button" value="<fmt:message key="save"/>"
                                               class="button" name="save"
                                               onclick="javascript:validate();"/>
                                        <input type="button" value="<fmt:message key="cancel"/>"
                                               name="cancel" class="button"
                                               onclick="javascript:cancel();"/>
                                    </td>
                                </tr>

                                </tbody>

                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <input type="hidden" name="configure"/>
            </form>
        </div>
    </div>

</fmt:bundle>