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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract" %>
<%@ page
        import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAuthorisationParams" %>
<%@ page import="org.wso2.carbon.humantask.ui.util.HumanTaskUIUtil" %>
<%
    TTaskAbstract task = (TTaskAbstract) request.getAttribute("LoadedTask");
    boolean isNotification = task.getTaskType().equals("NOTIFICATION");
    String taskId =  (String) request.getAttribute("taskId");
    TTaskAuthorisationParams authParams = (TTaskAuthorisationParams) request.getAttribute("TaskAuthorisationParams");
    String client = (String) request.getAttribute("taskClient");
    String requestJSPContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-input.jsp";
    String outputJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-output.jsp";
    String responseJspContextPath = "/humantaskui/" + task.getTenantId() + "/" + task.getPackageName() + "/" + task.getName().getLocalPart() + "-response.jsp";
%>
<fmt:bundle basename="org.wso2.carbon.humantask.ui.i18n.Resources">

<div id="task-instance-list-main">

    <link href="css/humantask-gadget.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/humantask-util.js"></script>
    <script type="text/javascript" src="js/humantask.js"></script>
    <script type="text/javascript">

    jQuery(document).ready(function() {
        //forceScrolling();
        HUMANTASK.ready('<%=taskId%>', '<%=client%>', <%=isNotification%>);
    });

    forceScrolling = function() {
        jQuery('#contentPlacer').attr('style', 'height: 450px; overflow-y: auto;');
    };


</script>

<div>
    <a class="backToTaskList" href="" style="display:block;padding:0px 0px 10px 0px"><< Back to Task List</a>
    <div class="titleStrip" id="taskSubjectTxt"><div class="titleStripSide">&nbsp;</div></div>
    <div id="errorStrip" style="display:none;"></div>
    <div class="contentPlacer">
        <div class="tabLinks" id="tabs_task">
            <ul>
                <li id="claimLinkLi" style="display:none;"><a id="claimLink">Claim</a></li>
                <li id="startLinkLi" style="display:none;"><a id="startLink">Start</a></li>
                <li id="stopLinkLi" style="display:none;"><a id="stopLink">Stop</a></li>
                <li id="releaseLinkLi" style="display:none;"><a id="releaseLink">Release</a></li>
                <li id="suspendLinkLi" style="display:none;"><a id="suspendLink">Suspend</a></li>
                <li id="resumeLinkLi" style="display:none;"><a id="resumeLink">Resume</a></li>
                <li id="commentLinkLi" style="display:none;"><a onclick="toggleMe('commentSection')">Comment</a></li>
                <li id="delegateLinkLi" style="display:none;"><a onclick="HUMANTASK.handleDelegateSelection('delegateSection')">Assign</a></li>
                <li id="moreActionsLinkLi" style="display:none;"><a id="moreActionsLink">More Actions</a></li>
            </ul>
        </div>
        <div id="commentSection" style="display:none">
            <textarea id="commentTextAreaId" class="commentTextArea"></textarea>
            <input id="addCommentButton" type="button" class="button" value="Add Comment" />
            <div style="clear:both;"></div>
        </div>
        <div id="delegateSection" class="delegateDiv" style="display:none">
            <select id="assignableUserList"></select><input id="delegateButton" type="button" class="button" value="Assign" />
        </div>
        <div class="tabLessContent-noBorder" id="tabContent">
            <div id="actionTab" class="tabContentData">
                <fieldset>
                    <legend><a onclick="toggleMe('details')"><h3>Details:</h3></a></legend>
                    <div id="details">
                        <table class="normal">
                            <tr>
                                <td class="cellHSeperator">
                                    <table class="normal">
                                        <tbody>
                                            <tr><th>Type:</th><td id="taskTypeTxt"></td></tr>
                                            <tr><th>Priority:</th><td id="taskPriorityTxt"></td></tr>
                                            <tr><th>Created On:</th><td id="taskCreatedOnDateTxt"></td></tr>
                                            <tr><th>Updated On:</th><td id="taskUpdatedOnDateTxt"></td></tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td>
                                    <table class="normal">
                                        <tbody>
                                            <tr><th>Status:</th><td id="taskStatusTxt"></td></tr>
                                            <tr id="taskPreviousStatusTR" style="display:none;"><th>Previous Status:</th><td id="taskPreviousStatusTxt"></td></tr>
                                        </tbody>
                                     </table>
                                </td>
                            </tr>
                        </table>

                    </div>
                </fieldset>



                <fieldset>
                    <legend><a onclick="toggleMe('description')"><h3>Description:</h3></a></legend>
                    <div id="description">
                        <div id="descriptionTxtDiv">
                        </div>
                    </div>
                  </fieldset>

                <fieldset>
                    <legend><a onclick="toggleMe('people')"><h3>People:</h3></a></legend>
                    <div id="people">
                        <table class="normal">
                        <tbody>
                            <tr id="taskCreatedByTR" style="display:none;"><th>Created By:</th><td id="taskCreatedByTxt"></td></tr>
                            <tr><th>Owner:</th><td id="taskOwnerTxt"></td></tr>
                        </tbody>
                        </table>
                    </div>
                </fieldset>


                <fieldset id="requestFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('requestDiv')"><h3>Request:</h3></a></legend>
                    <div id="requestDiv" class="dynamicContent">
                          <jsp:include page="<%=requestJSPContextPath%>"/>
                    </div>
                </fieldset>

                <fieldset id="responseFormFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('responseFormDiv')"><h3>Response:</h3></a></legend>
                    <div id="responseFormDiv" class="dynamicContent">
                          <jsp:include page="<%=outputJspContextPath%>"/>
                    </div>
                    <div id="completeButtonDiv">
                          <button id="completeTaskButton" value="Complete">Complete</button>
                    </div>
                </fieldset>

                <fieldset id="responseFieldSet" style="display: none;">
                    <legend><a onclick="toggleMe('responseDiv')"><h3>Response:</h3></a></legend>
                    <div id="responseDiv" class="dynamicContent">
                        <jsp:include page="<%=responseJspContextPath%>"/>
                    </div>
                </fieldset>

            </div>
        </div>


        <div class="tabs_task" id="tabsDown">
            <ul>
                <%--<li><a onclick="selectTab({me:this,tabContainer:'tabsDown',tabContentContainer:'tabContentDown'})" class="selected" rel="commentsTab">Comments</a></li>--%>
                <li><a id="commentTabLink" onclick="HUMANTASK.handleTabSelection('commentsTab')" class="selected" rel="commentsTab">Comments</a></li>
                <li><a id="eventTabLink" onclick="HUMANTASK.handleTabSelection('eventsTab')" rel="eventsTab">History</a></li>
            </ul>
        </div>
        <div class="tabContent" id="tabContentDown">
            <div id="commentsTab" tabindex="100" class="tabContentData">
                <%-- The task comments are populated and appended at this div --%>
            </div>
            <div id="eventsTab" tabindex="101"  class="tabContentData" style="display:none;">
                <%-- The task events are populated and appended at this div --%>
            </div>
        </div>

    </div>
</div>
</div>

</fmt:bundle>