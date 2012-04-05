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
<%@ page import="org.wso2.carbon.issue.tracker.stub.AccountInfo" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.AutoReportingSettings" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../issue/js/validate.js"></script>

<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
    <carbon:breadcrumb label="Edit Account"
                       resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                       topPage="false"
                       request="<%=request%>"/>


    <div id="middle">

                <%
           String accountName=request.getParameter("accountName");
           IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);

           boolean isStratosService = client.isStratosService();
           boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);

           AccountInfo accountInfo ;

           if(!isStratosService){
           accountInfo = client.getAccount(accountName);
           }else{
           accountInfo = client.getAccount();
           }
           GenericCredentials credentials = accountInfo.getCredentials();
           String url=credentials.getUrl();
           String password = credentials.getPassword();
           
           String email = accountInfo.getEmail();
           String uid= accountInfo.getUid();


           boolean isAutoReportingEnabled = accountInfo.getAutoReportingEnable();

           String autoReportingProject="--Select--";
           String autoReportingType="--Select--";
           String autoReportingPriority="--Select--";
           if(isAutoReportingEnabled){
             AutoReportingSettings settings=accountInfo.getAutoReportingSettings();
             autoReportingProject=settings.getProjectName();
             autoReportingType=settings.getIssueType();
             autoReportingPriority=settings.getPriority();

           }



    %>


        <h2><fmt:message key="edit.jira.account"/></h2>

        <div id="workArea">
            <form action='editAccountProcessor.jsp' method="POST" name="accountForm"
                  id="accountForm">


                <table class="styledLeft noBorders" id="editAccountTable">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="account.edit"/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <%

                        // accounts are allowed to add only for a super tenant in stratos environment
                        if (!isStratosService) {

                    %>
                    <tr>
                        <td style="width:100px;"><fmt:message key="new.jira.key"/><span
                                class='required'>*</span></td>
                        <td align="left">
                            <input type="text" id="txtKey" name="key" class="longInput"
                                   rows="6" value="<%=accountName%>">
                            <input type="hidden" id="oldKey" name="oldKey" class="longInput"
                                   rows="6" value="<%=accountName%>">

                        </td>
                    </tr>

                    <tr>
                        <td style="width:100px;"><fmt:message key="new.jira.url"/><span
                                class='required'>*</span></td>
                        <td align="left">
                            <input type="text" id="txtURL" name="URL" class="longInput"
                                   rows="6" value="<%=url%>">
                        </td>
                    </tr>

                        <%--<tr>--%>
                        <%--<td colspan="2" style="padding-left: 120px ! important; color: rgb(119, 119, 119);" align="center">--%>
                        <%--<label>(e.g. https://support.wso2.com/jira)</label></td>--%>
                        <%--</tr>--%>

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
                                   rows="6" value="<%=uid%>">
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
                                class='required'>*</span>
                        </td>
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


                <%

                %>


            </form>


        </div>

</fmt:bundle>