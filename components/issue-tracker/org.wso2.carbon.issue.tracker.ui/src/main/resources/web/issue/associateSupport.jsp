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
~ under the Lice nse.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericUser" %>
<script type="text/javascript" src="../issue/js/validate.js"></script>

<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
    <carbon:breadcrumb label="New Jira Account"
                       resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <%
        GenericUser user = (GenericUser) (session.getAttribute("user"));
        String username = user.getUsername();
        String email = user.getEmail();
        String onNewAccountCreation = request.getParameter("newAccount");

        if ("true".equals(onNewAccountCreation)) {
    %>
    <script type="text/javascript">
        CARBON.showInfoDialog('Congradulations!!. You have been created an Oxygen Tank account successfully. You will be receiving a verification email shortly. Please follow the instructions and provide your credentials below to associate your account with WSO2 support service.');
    </script>

    <%
        }
    %>
    <div id="middle">
        <h2><fmt:message key="associate.support.account"/></h2>

        <form action='associateSupportProcessor.jsp' method="POST" name="supportForm" id="supportForm">

            <table class="styledLeft noBorders" id="editAccountTable">
                <thead>
                <tr>
                    <th colspan="3"><fmt:message key="associate.support.account.heading"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="width:100px;"><fmt:message key="new.jira.username"/><span
                            class='required'>*</span></td>
                    <td align="left">
                        <input type="text" id="txtUserName" name="username" class="longInput"
                               rows="6" value="<%=username%>">
                    </td>
                </tr>
                <tr>
                    <td style="width:100px;"><fmt:message key="new.jira.email"/><span
                            class='required'>*</span></td>
                    <td align="left">
                        <input type="text" id="txtEmail" name="email" class="longInput"
                               rows="6" value="<%=email%>">
                    </td>
                </tr>

                <tr>
                    <td style="width:100px;"><fmt:message key="new.jira.password"/><span
                            class='required'>*</span></td>
                    <td align="left">
                        <input type="password" id="txtPassword" name="password"
                               class="password"
                               rows="6">
                    </td>
                </tr>

                <tr>
                    <td style="width:100px;">
                        <nobr><fmt:message key="new.jira.retype.password"/></nobr><span
                            class='required'>*</span></td>
                    <td align="left">
                        <input type="password" id="txtRetypePassword" name="retypePassword"
                               class="password"
                               rows="6">
                    </td>
                </tr>
                </tbody>
            </table>


            <table class="normal">
                <tr>
                    <td>
                        <input type="button"
                               value="<fmt:message key="new.jira.save"/>"
                               onclick="validateSupportForm()"
                               class="button"/>
                        <input class="button" type="reset"
                               value="<fmt:message key="cancel"/>"
                               onclick="document.location.href='viewAccounts.jsp'"/>

                    </td>
                </tr>
            </table>
        </form>


        </form>
    </div>


</fmt:bundle>