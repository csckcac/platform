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
<%@ page import="org.wso2.carbon.issue.tracker.stub.AccountInfo" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<jsp:include page="../admin/error.jsp"/>


<%
    AccountInfo[] accountInfo;
    IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);
    accountInfo = client.getAccountInfo();
    int numberOfAccounts;
    if (null != accountInfo) {
        numberOfAccounts = accountInfo.length;
    } else {
        numberOfAccounts = 0;
    }
    boolean isStratosService = client.isStratosService();

    String onAccountCreation = request.getParameter("success");

    if ("true".equals(onAccountCreation)) {
%>

<script type="text/javascript">
    CARBON.showInfoDialog('Congratulations!!. You have been associated with WSO2 support service successfully.');
</script>
<%

    }
    String errorMessage = request.getParameter("errorMessage");
    if (errorMessage != null) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Account cannot be deleted!!! <%=errorMessage%>");

</script>
<%
    }
%>


<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
    <carbon:breadcrumb label="View Accounts"
                       resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function deleteUserGroup(account) {
            function doDelete() {
                var accountName = account;
                location.href = 'deleteAccount.jsp?accountName=' + accountName;
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.account"/> ' + account + '?', doDelete, null);
        }


        function editAccount(account) {
            var accountName = account;
            location.href = 'editAccount.jsp?accountName=' + accountName;
        }


    </script>
    <script type="text/javascript">

        function updateUserGroup(role) {

            var roleName = role;

            location.href = 'rename-role.jsp?roleName=' + roleName + '&userType=internal';
        }

    </script>
    <div id="middle">
        <h2><fmt:message key="accounts.list.header"/></h2>

        <div id="workArea">
            <table class="styledLeft" id="accountsTable">


                <%
                    if (accountInfo != null) {
                %>
                <thead>
                <tr>
                    <%
                        if (!isStratosService) {
                    %>
                    <th><fmt:message key="accounts.key"/></th>
                    <%
                        }
                    %>
                    <th><fmt:message key="accounts.url"/></th>
                    <th><fmt:message key="accounts.username"/></th>
                    <th><fmt:message key="accounts.action"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (numberOfAccounts != 0) {
                        for (AccountInfo account : accountInfo) {
                            if (!isStratosService) {
                                String key = account.getKey();
                %>
                <tr>
                    <td>
                        <%=key%>
                    </td>
                    <%
                        }
                        String url = account.getCredentials().getUrl();
                    %>

                    <td>
                        <%=url%>

                    </td>

                    <%
                        String username = account.getCredentials().getUsername();
                    %>

                    <td>
                        <%=username%>

                    </td>

                    <td>
                        <a href="#" onclick="editAccount('<%=account.getKey()%>')" class="icon-link"
                           style="background-image:url(images/edit.gif);"><fmt:message
                                key="account.edit"/></a>
                        <a href="#" onclick="deleteUserGroup('<%=account.getKey()%>')"
                           class="icon-link"
                           style="background-image:url(images/delete.gif);"><fmt:message
                                key="account.delete"/></a>
                    </td>
                </tr>
                <%
                        }
                    }
                } else {
                %>
                <p><b>No Accounts Found</b></p>
                <%


                    }

                %>


                </tbody>

            </table>
            <%
                if (isStratosService && numberOfAccounts == 0) {
            %>
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td>
                        <a href="supportUserRegistration.jsp" class="icon-link"
                           style="background-image:url(images/add.gif);"><fmt:message
                                key="add.new.account"/></a>
                    </td>
                </tr>
            </table>
            <%
                }
            %>

        </div>
    </div>


    <script type="text/javascript">
        alternateTableRows('accountsTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
