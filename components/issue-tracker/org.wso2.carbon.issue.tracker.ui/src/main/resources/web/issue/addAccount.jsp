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
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<script type="text/javascript" src="../issue/js/validate.js"></script>

<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
<carbon:breadcrumb label="New Jira Account"
                   resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>


<div id="middle">
<script type="text/javascript">
    function showNewAccountTable() {
        document.getElementById('accountForm').style.display = 'none';
        document.getElementById('newAccountForm').style.display = '';
    }

    function showExistingAccountTable() {
        document.getElementById('accountForm').style.display = '';
        document.getElementById('newAccountForm').style.display = 'none';
    }


    function loadDropdowns(){
        sessionAwareFunction(function() {
        jQuery.noConflict() ;
        jQuery("#countries").load('loadCountries-ajaxprocessor.jsp');
         jQuery("#industries").load('loadIndustries-ajaxprocessor.jsp');
    });
    }


</script>

        <%

          IssueTrackerClient  client = IssueTrackerClient.getInstance(config, session);
          boolean isStratosService = client.isStratosService();
          boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);

        %>

<h2><fmt:message key="add.new.jira.account"/></h2>


<div id="workArea">
<%

    // accounts are allowed to add only for a super tenant in stratos environment
    if (isStratosService) {

%>

<table class="styledLeft noBorders" id="optionsTable">
   <tr> <INPUT TYPE="radio" NAME="radios" VALUE="radio1" onclick="showNewAccountTable()">
    Create new OT account
    </tr>
    <tr>
    <INPUT TYPE="radio" NAME="radios" VALUE="radio2" CHECKED onclick="showExistingAccountTable()">
    Use my OT account
    </tr>
</table>

<%
    }
%>

<form action='addAccountProcessor.jsp' method="POST" name="accountForm" id="accountForm">

    <table class="styledLeft noBorders" id="editAccountTable">
        <thead>
        <tr>
            <th colspan="3"><fmt:message key="account.add"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="2" style=" ! important; color: rgb(119, 119, 119);"
                align="center">
                <label>Please use the email and password mentioned in the verification email sent to you on
                    tenant registration by WSO2 Oxygen Tank.</label>
            </td>
        </tr>
        <%

            // accounts are allowed to add only for a super tenant in stratos environment
            if (!isStratosService) {

        %>
        <tr>
            <td style="width:100px;"><fmt:message key="new.jira.key"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="txtKey" name="key" class="longInput"
                       rows="6">
            </td>
        </tr>

        <tr>
            <td style="width:100px;"><fmt:message key="new.jira.url"/><span
                    class='required'>*</span></td>

            <td align="left">
                <input type="text" id="txtURL" name="URL" class="longInput"
                       rows="6">
            </td>

        </tr>


        <tr>
            <td colspan="2" style="padding-left: 130px ! important; color: rgb(119, 119, 119);"
                align="center">
                <label>(e.g. https://support.wso2.com/jira)</label></td>
        </tr>

        <%
        } else {
        %>

        <input type="hidden" name="isStratosService" id="isStratosService"
               value="<%=isStratosService%>"/>

        <%

            }
        %>
        <tr>
            <td style="width:100px;"><fmt:message key="new.jira.username"/><span
                    class='required'>*</span></td>
            <td align="left">
                <input type="text" id="txtUserName" name="username" class="longInput"
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
                       onclick="validateLoginForm()"
                       class="button"/>
                <input class="button" type="reset"
                       value="<fmt:message key="cancel"/>"
                       onclick="document.location.href='viewAccounts.jsp'"/>

            </td>
        </tr>
    </table>
</form>

<form action='newOTAccountProcessor.jsp' method="POST" name="newAccountForm" id="newAccountForm" style="display:none;">
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