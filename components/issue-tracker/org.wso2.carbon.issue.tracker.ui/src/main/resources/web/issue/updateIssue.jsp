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
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericIssue" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../js/validate.js"></script>
<script type="text/javascript" src="js/activity.js"></script>

<jsp:include page="../admin/error.jsp"/>
<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
<carbon:breadcrumb label="update.issue"
                   resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>


<div id="middle">


<h2><fmt:message key="new.issue"/></h2>

<div id="workArea">


<form method="post" name="newIssueForm" enctype="multipart/form-data" target="_self">


    <table class="styledLeft noBorders" id="updateIssueTable">
        <thead>
        <tr>
            <th colspan="3"><fmt:message key="update.issue"/></th>
        </tr>
        </thead>
        <tbody>

        <div id="dropdown">

            <%
                IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);

                GenericIssue genericIssue = (GenericIssue) session.getAttribute("issue");
                String selectedAccount = (String) session.getAttribute("selectedAccount");

                String summary = "";
                String project = "";
                String type = "";
                String priority = "";
                String description = "";
                String issueKey = "";
                String account = "";
                String dueDate = "";

                System.out.println("selectedAccount = " + selectedAccount);
                if (null != genericIssue && null != selectedAccount) {


                    if(null != genericIssue.getSummary()){
                    summary = genericIssue.getSummary();
                    }

                    if(null !=  genericIssue.getProjectKey()){
                    project = genericIssue.getProjectKey();
                    }

                    if(null != genericIssue.getType()){
                    type = genericIssue.getType();
                    }

                    if(null != genericIssue.getPriority()){
                    priority = genericIssue.getPriority();
                    }

                    if(null != genericIssue.getDescription()){
                    description = genericIssue.getDescription();
                    }

                    if(null != genericIssue.getIssueKey()){
                    issueKey = genericIssue.getIssueKey();
                    }

//                    if(null != genericIssue.getDueDate() ){
//                    dueDate = genericIssue.getDueDate().getTime().toString();
//                    }

                                        
                    account = selectedAccount;



                }
                String[] accountNames = client.getAccountNames();

            %>

            <tr>
                <td class="leftCol-small" align="left" style="width:80px;">
                    <fmt:message
                            key="new.issue.jira.account"/></td>
                <td align="left">
                    <label><%=account%></label>
                </td>
                 <input type="hidden" name="accountName" id="accountName"
                   value="<%=account%>"/>
            </tr>
        </div>

        <tr>
                <td class="leftCol-small" align="left" style="width:80px;">
                    <fmt:message
                            key="update.issue.key"/></td>
                <td align="left">
                    <label><%=issueKey%></label>
                </td>
             <input type="hidden" name="issueKey" id="issueKey"
                   value="<%=issueKey%>"/>
            </tr>


        <tr>
            <td style="width:100px;"><fmt:message key="new.issue.summary"/></td>
            <td align="left">
               <label><%=summary%></label>
            </td>
            <input type="hidden" name="summary" id="summary"
                   value="<%=summary%>"/>
        </tr>

        </tbody>
    </table>

    <table class="styledLeft noBorders" id="attachmentTbl">
        <thead>

        <th colspan="3"><fmt:message key="new.issue.attachment"/></th>

        </thead>
        <tr>
            <td style="width:100px;"><fmt:message key="new.issue.attachment"/></td>
            <td><input type="file" name="attachmentName" size="50"/></td>
            <td align="left"><input type="button" width='20px' class="button" onclick="addRow();" value=" + "/>
            </td>

        </tr>
    </table>
     

    <table class="styledLeft">
        <tr>
            <td class="buttonRow" colspan="3">
                <input type="submit"
                       value="<fmt:message key="attach.files"/>"
                       class="button" tabindex="3"
                       onclick="document.newIssueForm.action ='../../fileupload/updateFiles';return validateIssue();CARBON.showWarningDialog('Issue is successfully created');document.location.href='viewIssues.jsp'"/>

                <input class="button" type="reset"
                       value="<fmt:message key="cancel"/>"
                       onclick="document.location.href='newIssue.jsp'"/>
            </td>
        </tr>
    </table>


</form>

<script type="text/javascript">

    var rows = 1;
    function addRow() {
        rows++;

        //add a row to the rows collection and get a reference to the newly added row
        var newRow = document.getElementById("attachmentTbl").insertRow(-1);
        newRow.id = 'file' + rows;

        var oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<input type='hidden'>";
        oCell.className = "styledLeft noBorders";


        oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<input type='file' name='attachmentName' size='50'/>";
        oCell.className = "styledLeft noBorders";

        oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file" + rows + "');\" />";
        oCell.className = "styledLeft noBorders";

    }


    function deleteRow(rowId) {
        var tableRow = document.getElementById(rowId);
        tableRow.parentNode.deleteRow(tableRow.rowIndex);
        alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
    }


</script>
</div>
</div>
</fmt:bundle>
