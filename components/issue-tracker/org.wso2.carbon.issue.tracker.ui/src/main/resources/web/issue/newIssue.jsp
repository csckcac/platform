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
~
~ under the License.
-->
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../issue/js/validate.js"></script>
<script type="text/javascript" src="js/activity.js"></script>


<jsp:include page="../admin/error.jsp"/>
<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
<carbon:breadcrumb label="new.issue"
                   resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>


<div id="middle">


<h2><fmt:message key="new.issue"/></h2>

<div id="workArea">

<script type="text/javascript">
    var projectList;
</script>

<script type="text/javascript">
    function initDatePickers() {
        jQuery("#due").datepicker();
    }
    jQuery(document).ready(initDatePickers)
</script>
<script type="text/javascript">
    function validateFileExtension(id) {   //we do not need this checking now hence we allow all types of files
        var fileName = document.getElementById(id).value;

        var msg = '';

        var allowedExtensions = new Array(".txt", ".info", ".xml", ".doc", ".png", ".jpg", ".jpeg", ".gif", ".wsdl", ".java" ,".class",".jar");

        for (var i = 0; i < allowedExtensions.length; i++) {

            msg = msg + ' ' + allowedExtensions[i] + ',';
            if (fileName.lastIndexOf(allowedExtensions[i]) != -1) {
                return true;
            }
        }

        CARBON.showErrorDialog("This file extension is not allowed. Allowed extensions are " + msg);
        document.getElementById(id).value = '';
        return false;

    }


    function loadData() {
        var loadingImg = document.getElementById('loadingImageInService');
        loadingImg.style.display = '';

  jQuery.ajax({
            type: 'POST',
            url: 'loadProject-ajaxprocessor.jsp',
            data: 'account=0',
            success: function(loadResponse) {

                    loadResponse = loadResponse.replace(/^\s+|\s+$/g, '');

                    var info = eval('(' + loadResponse + ')');
                    var success = info.success;
                    if (success == 'fail') {
                        CARBON.showErrorDialog('Unable to connect to the selected account to obtain available issue types. Invalid username and password or unreachable url is given.Refresh the page and re try. If you have not already added an account,go to the Account page and add an account.');
                        var account = document.getElementById("accountNames");

                        if (null != account) {
                            document.getElementById("accountNames").value = '--Select--';
                        }
                        var newSelectHTML = '<option value="--Select--">--Select--</option>';

                        var project = document.getElementById("projectList");

                        if (null != project) {
                            document.getElementById("projectList").innerHTML = newSelectHTML;
                        }

                        var type = document.getElementById("type");

                        if (null != type) {
                            document.getElementById("type").innerHTML = newSelectHTML;
                        }

                        var priority = document.getElementById("priority");

                        if (null != priority) {
                            document.getElementById("priority").innerHTML = newSelectHTML;
                        }

                        loadingImg.style.display = 'none';

                    } else {

                        var issueTypes = info.issueType;
                        var issueIdList = info.issueId;
                        var priorityTypes = info.priorityName;
                        var priorityId = info.priorityId;

                        for (var j = 0; j < issueTypes.length; j++) {

                            // todo this is hard coded, need to remove
                            if (issueTypes[j] == "Query" || issueTypes[j] == "Incident" || issueTypes[j] == "Task" || issueTypes[j] == "Announcement") {
                                option = document.createElement("option");
                                option.value = issueIdList[j];
                                option.innerHTML = issueTypes[j];
                                document.getElementById("type").appendChild(option);
                            }

                        }

                        loadingImg.style.display = 'none';

                    }
            },
            error:function (xhr, ajaxOptions, thrownError) {
                  CARBON.showErrorDialog('Unable to connect to the selected account to obtain available issue types. Invalid username and password or unreachable url is given.Refresh the page and re try. If you have not already added an account,go to the Account page and add an account.');
            }
        });

    }

    function limitText(limitField, limitNum) {
	if (limitField.value.length > limitNum) {
		limitField.value = limitField.value.substring(0, limitNum);
	}
}
</script>

<%
    IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);
    boolean isStratosService = client.isStratosService();
    boolean isSuperTenant = CarbonUIUtil.isSuperTenant(request);
    String supportInfoPageUrl = client.getSupportInfoUrl();
    boolean isTenantSubscriptionFree = true;

    try{
      isTenantSubscriptionFree = client.isTenantSubscriptionFree();


    if (!isTenantSubscriptionFree) {
%>

<form method="post" name="newIssueForm" enctype="multipart/form-data" target="_self">

<p>
    You will receive a confirmation email from WSO2 Support with details of this issue.
    <br/>
    <span style="font-weight: bold;">More information on WSO2 support can be found at <a href="<%=supportInfoPageUrl%>"
                                                                                         target="_blank">http://wso2.com/support </a>
    </span>
</p>
<table class="styledLeft noBorders" id="newIssueTable">
    <thead>
    <tr>
        <th colspan="3"><fmt:message key="new.issue"/></th>
    </tr>
    </thead>
    <tbody>

    <div id="dropdown">
        <%

            // accounts are allowed to add only for a super tenant in stratos environment
            if (!isStratosService) {

        %>

        <tr>
            <td class="leftCol-small" align="left" style="width:80px;">
                <fmt:message
                        key="new.issue.jira.account"/></td>

            <td align="left">

                <select id="accountNames" name="accountNames"
                        onchange="loadProjects(this.value)">
                    <option value="--Select--" selected="selected">--Select--</option>
                    <%


                        String[] accountNames = client.getAccountNames();


                        if (null != accountNames && accountNames.length != 0) {
                            for (String name : accountNames) {

                    %>
                    <option value=<%=name%>><%=name%>
                    </option>
                    <%
                            }
                        }

                    %>

                </select>

            </td>
        </tr>
    </div>


    <tr>
        <td style="width:100px;"><fmt:message key="new.issue.project"/><span
                class='required'>*</span></td>

        <td>
            <select id="projectList" name="projectList">
                <option value="--Select--" selected="selected">--Select--</option>
            </select>
            <img id="loadingImage" style="display:none;" src="images/loading-small.gif" alt="">
        </td>


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
        <td style="width:100px;"><fmt:message key="new.issue.type"/><span
                class='required'>*</span></td>
        <td align="left">
            <select id="type" name="type">
                <option value="--Select--" selected="selected">--Select--</option>

            </select>
            <img id="loadingImageInService" style="display:none;" src="images/loading-small.gif" alt="">
            <%
                if (isStratosService) {
            %>

            <script type="text/javascript">
                loadData();
            </script>

            <%
                }
            %>
        </td>
    </tr>


    <tr>
        <td style="width:100px;"><fmt:message key="new.issue.summary"/><span
                class='required'>*</span></td>
        <td align="left">
            <textarea id="summary" name="summary" class="longInput" rows="3" cols="40" onKeyDown="limitText(this.form.summary,255);"
onKeyUp="limitText(this.form.summary,250);"></textarea>
        </td>
    </tr>

    </tbody>
</table>

<table class="styledLeft noBorders" id="additionalData">

    <thead>
    <tr>
        <th colspan="3"><fmt:message key="new.issue.additional.data"/></th>
    </tr>
    </thead>
    <tbody>

    <tr>
        <td style="width:100px;"><fmt:message key="new.issue.description"/><span
                class='required'>*</span></td>
        <td align="left">
            <textarea id="description" name="description" class="longInput" rows="6"></textarea>
        </td>
    </tr>


    <% //priority and date are shown only for products
        if (!isStratosService) {
    %>
    <tr>
        <td class="leftCol-small" align="left" style="width:80px;"><fmt:message
                key="new.issue.due"/></td>
        <td>
            <input type="text" name="due" id="due"
                   style="widht:140px;" onkeypress="handleUserNameKeyPress(event);"/>
            <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);"
               onclick="jQuery('#due').datepicker( 'show' );" href="#"></a>

        </td>
    </tr>

    <tr>
        <td style="width:100px;"><fmt:message key="new.issue.priority"/></td>
        <td align="left">
            <select id="priority" name="priority">
                <option value="--Select--" selected="selected">--Select--</option>

            </select>
        </td>
    </tr>

    <%
        }
    %>
    </tbody>
</table>

<table class="styledLeft noBorders" id="attachmentTbl">
    <thead>

    <th colspan="3"><fmt:message key="new.issue.attachment"/></th>

    </thead>
     <tr>
        <td colspan="2">
            <a href="#" onclick="addRow()"
               style="background-image: url('images/add.gif');" class="icon-link">
                <fmt:message key="new.issue.add.new.attachments"/>
               </a><input type="hidden" name="attachmentName" id="attachmentName"/>
               <input type="hidden" name="errorRedirectionPage" value="../carbon/issue/newIssue.jsp">
        </td>
    </tr>
</table>

<%
    if (isSuperTenant) {
%>
<table class="styledLeft noBorders" id="attachments">
    <tr>
        <td style="width:100px;">
            <input type="checkbox" name="bundleInfo" id="bundleInfo"/>
        </td>
        <td><label for="bundleInfo"><fmt:message key="attach.bundleInfo"/></label></td>

    </tr>
    <tr>
        <td style="width:100px;">
            <input type="checkbox" name="threadDump" id="threadDump"/>
        </td>
        <td><label for="threadDump"><fmt:message key="attach.fullStackTrace"/></label></td>
    </tr>
    <tr>
        <td style="width:100px;">
            <input type="checkbox" name="log" id="log"/>

        </td>
        <td><label for="log"><fmt:message key="attach.log"/></label></td>
    </tr>
</table>

<%
    }
%>

<table class="styledLeft">
    <tr>
        <td class="buttonRow" colspan="3">
            <input type="submit"
                   value="<fmt:message key="new.issue.create"/>"
                   class="button" tabindex="3"
                   onclick="document.newIssueForm.action ='../../fileupload/attachFiles';return validateIssue();CARBON.showWarningDialog('Issue is successfully created');document.location.href='viewIssues.jsp'"/>

            <input class="button" type="reset"
                   value="<fmt:message key="reset"/>"
                   onclick="document.location.href='newIssue.jsp'"/>
        </td>
    </tr>
</table>


</form>

<%
} else {

%>

<table class="styledLeft">
    <tbody align="center">
    <tr>
        <td>
            <label>
                Since your current subscription plan is Demo, you are eligible to obtain our free support service from
            <span style="font-weight:bold;"><a href="<%=client.getForumLink()%>" target="_blank"> StratosLive forum</a>.
                </span>
            </label>
            <p>
                If you are eager to experience our comprehensive support service, please upgrade your subscription plan.
            </p>
        </td>
    </tr>
    </tbody>
</table>
<%
    }

     }catch(Exception e) {
        %>
  <script type="text/javascript">
         CARBON.showErrorDialog('Unable to connect to the cloud manager to obtain the subscription.');
  </script>

<table class="styledLeft">
    <tbody align="center">
    <tr>
        <td>
            <label>
               Unexpected error occurred. Please refer backend logs for more details. However you still can obtain our free support service from
            <span style="font-weight:bold;"><a href="<%=client.getForumLink()%>" target="_blank"> StratosLive forum</a>.
                </span>
            </label>

        </td>
    </tr>
    </tbody>
</table>


   <%
    }
%>

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
        oCell.style.width = "100px" ;

        oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<input type='file' name='attachmentName' id='attachment" + rows + "' size='50'  />";
        oCell.className = "styledLeft noBorders";

        oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<a href=\"#\" class='icon-link' style=\"background-image: url('../admin/images/delete.gif');\" value='  -  ' onclick=\"deleteRow('file" + rows + "');\" /><fmt:message key="new.issue.delete.attachments"/>";
        oCell.className = "styledLeft noBorders";
    }


    function deleteRow(rowId) {

        var tableRow = document.getElementById(rowId);
        tableRow.parentNode.deleteRow(tableRow.rowIndex - 1);
        alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
    }

    function loadProjects(name) {

        var newSelectHTML = '<option value="--Select--">--Select--</option>';
        document.getElementById("projectList").innerHTML = newSelectHTML;
        document.getElementById("type").innerHTML = newSelectHTML;
        document.getElementById("priority").innerHTML = newSelectHTML;

        if (name != '--Select--') {

            var loadingImg = document.getElementById('loadingImage');
            loadingImg.style.display = '';

            $.post("loadProject-ajaxprocessor.jsp?accountNames=" + document.getElementById("accountNames").value, {},
                    function(loadResponse) {

                        loadResponse = loadResponse.replace(/^\s+|\s+$/g, '');

                        projectList = eval('(' + loadResponse + ')');
                        var success = projectList.success;

                        if (success == 'fail') {
                            CARBON.showErrorDialog('Unable to connect to the selected account. Invalid username and password or unreachable url is given.');
                            document.getElementById("accountNames").value = '--Select--';
                            var newSelectHTML = '<option value="--Select--">--Select--</option>';
                            document.getElementById("projectList").innerHTML = newSelectHTML;
                            document.getElementById("type").innerHTML = newSelectHTML;
                            document.getElementById("priority").innerHTML = newSelectHTML;
                            loadingImg.style.display = 'none';

                        } else {

                            var projects = projectList.project;
                            var issueTypes = projectList.issueType;
                            var issueIdList = projectList.issueId;
                            var priorityTypes = projectList.priorityName;
                            var priorityId = projectList.priorityId;

                            var newSequenceSelectHTML = '<option value="--Select--">--Select--</option>';

                            for (var i = 0; i < projects.length; i++) {

                                newSequenceSelectHTML += '<option value="' + projects[i] + '">' + projects[i] + '</option>';

                            }

                            document.getElementById("projectList").innerHTML = newSequenceSelectHTML;


                            var newIssueTypesHTML = '<option value="--Select--">--Select--</option>';

                            for (var j = 0; j < issueTypes.length; j++) {

                                newIssueTypesHTML += '<option value="' + issueIdList[j] + '">' + issueTypes[j] + '</option>';

                            }

                            document.getElementById("type").innerHTML = newIssueTypesHTML;


                            var newPriorityTypesHTML = '<option value="--Select--">--Select--</option>';

                            for (var k = 0; k < priorityTypes.length; k++) {

                                newPriorityTypesHTML += '<option value="' + priorityId[k] + '">' + priorityTypes[k] + '</option>';

                            }

                            document.getElementById("priority").innerHTML = newPriorityTypesHTML;
                            loadingImg.style.display = 'none';

                        }


                    });

        }


    }


</script>
</div>
</div>
</fmt:bundle>
