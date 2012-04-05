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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<script type="text/javascript" src="../issue/js/validate.js"></script>
<script type="text/javascript" src="../issue/js/loadTables.js"></script>

<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
<carbon:breadcrumb label="Support user registration"
                   resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>


<div id="middle">

<div id="workArea">


<%

    IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);
    boolean isStratosService = client.isStratosService();
    boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);

    String createNew = request.getParameter("newAccount");
    boolean createNewAccount = false;

    if (null != createNew && "true".equals(createNew)) {
        createNewAccount = true;
    }

    String success = request.getParameter("success");

    if (null != success && "fail".equals(success)) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('Unable to create the OT account. The given email address is already registered.');
</script>
<%
    }
%>

<h2><fmt:message key="associate.support.account"/></h2>

<table class="styledLeft noBorders" id="optionsTable">
    <tr>
        <td style="width:100px;"><INPUT TYPE="radio" id="radio1" NAME="radios"
                                        VALUE="radio1" <%=createNewAccount?"checked=\"checked\"" : ""%>
                                        onclick="showNewAccountTable()">
        </td>
        <td><label for="radio1"><fmt:message key="create.new.ot.account.label"/></label></td>
    </tr>
    <tr>
        <td style="width:100px;">
            <INPUT TYPE="radio" id="radio2" NAME="radios"
                   VALUE="radio2" <%=!createNewAccount?"checked=\"checked\"" : ""%>
                   onclick="showExistingAccountTable()">

        </td>
        <td><label for="radio2"><fmt:message key="use.my.ot.account.label"/></label></td>
    </tr>
</table>


<form action='associateSupportProcessor.jsp' method="POST" name="supportForm" id="supportForm">

    <table class="styledLeft noBorders" id="editAccountTable">
        <thead>
        <tr>
            <th colspan="3"><fmt:message key="associate.support.account.heading"/></th>
        </tr>
        </thead>
        <tbody>

       <!-- <tr>
            <td style="width:100px;"><fmt:message key="new.jira.username"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="txtUserName" name="username" class="longInput"
                       rows="6">
            </td>
        </tr> -->

        <tr>
            <td style="width:100px;"><fmt:message key="new.jira.email"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="txtEmail" name="email" class="longInput"
                       rows="6">
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

            <%--<tr>--%>
            <%--<td style="width:100px;">--%>
            <%--<nobr><fmt:message key="new.jira.retype.password"/></nobr><span--%>
            <%--class='required'>*</span></td>--%>
            <%--<td align="left">--%>
            <%--<input type="password" id="txtRetypePassword" name="retypePassword"--%>
            <%--class="password"--%>
            <%--rows="6">--%>
            <%--</td>--%>
            <%--</tr>--%>
        </tbody>
    </table>


    <table class="normal">
        <tr>
            <td>
                <input type="button"
                       value="<fmt:message key="new.jira.save"/>"
                       onclick="validateExistingOTForm()"
                       class="button"/>
                <input class="button" type="reset"
                       value="<fmt:message key="cancel"/>"
                       onclick="document.location.href='viewAccounts.jsp'"/>

            </td>
        </tr>
    </table>
</form>


<form action='newOTAccountProcessor.jsp' method="POST" name="newAccountForm" id="newAccountForm" style="display:none">
    <table class="styledLeft noBorders" id="newAccount">
        <thead>
        <tr>
            <th colspan="3"><fmt:message key="create.new.account.heading"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.email"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="email" name="email" class="longInput"
                       rows="6">
            </td>
        </tr>

        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.first.name"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="firstName" name="firstName" class="longInput"
                       rows="6">
            </td>
        </tr>

        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.last.name"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="lastName" name="lastName" class="longInput"
                       rows="6">
            </td>
        </tr>

        <fieldset style="border: medium none ;">

            <script type="text/javascript">
                jQuery(document).ready(function() {
                    loadDropdowns();

                });
            </script>

            <tr>
                <td style="width:100px;"><fmt:message key="create.new.account.country"/><span
                        class='requiredcountries'>*</span></td>
                <td align="left">
                    <div id="countries">
                    </div>
                </td>
            </tr>
        </fieldset>
        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.industry"/><span
                    class='required'>*</span></td>
            <td align="left">
                <div id="industries">
                </div>
            </td>
        </tr>
        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.company"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="company" name="company" class="longInput"
                       rows="6">
            </td>
        </tr>

        <tr>
            <td style="width:100px;"><fmt:message key="create.new.account.job.title"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="jobTitle" name="jobTitle" class="longInput"
                       rows="6">
            </td>
        </tr>
        </tbody>
        <tbody>
    </table>
    <table class="normal">
        <tr>
            <td>
                <input type="button"
                       value="<fmt:message key="create.new.account.button"/>"
                       onclick="validateNewOTAccount()"
                       class="button"/>
            </td>
        </tr>
    </table>
</form>

</div>
</div>


</fmt:bundle>