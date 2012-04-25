/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var HUMANTASK = {};

HUMANTASK.authParams = null;
HUMANTASK.taskDetails = null;
HUMANTASK.taskId = null;
HUMANTASK.taskClient = null;


HUMANTASK.ready = function(taskId, taskClient, isNotification) {
    HUMANTASK.taskId = taskId;
    HUMANTASK.taskClient = taskClient;
    HUMANTASK.loadTask(taskId, taskClient);
    HUMANTASK.loadTaskAuthParams(taskId, taskClient);
    HUMANTASK.loadComments(taskId, taskClient);
    HUMANTASK.bindButtons();
};

HUMANTASK.loadTask = function(taskId, taskClient) {
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskDetails';
    $.getJSON(page,
              function(json) {
                  HUMANTASK.taskDetails = json;
                  //The post task details load actions should be called here.
                  HUMANTASK.fillTaskDetails();
                  HUMANTASK.showResponseFieldSet();
              });
};

HUMANTASK.loadTaskAuthParams = function(taskId, taskClient) {
    var authParams;
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=authParams';
    $.getJSON(page,
              function(json) {
                  HUMANTASK.authParams = json;
                  //The post task auth param load actions should be called here.
                  HUMANTASK.showHideActions();
              });
};

HUMANTASK.fillTaskDetails = function () {
    jQuery('#taskStatusTxt').text(HUMANTASK.taskDetails.taskStatus);
    jQuery('#taskPriorityTxt').text(HUMANTASK.taskDetails.taskPriority);
    jQuery('#taskCreatedOnDateTxt').text(HUMANTASK.taskDetails.taskCreatedOn);
    jQuery('#taskUpdatedOnDateTxt').text(HUMANTASK.taskDetails.taskUpdatedOn);
    jQuery('#taskTypeTxt').text(HUMANTASK.taskDetails.taskType);
    jQuery('#descriptionTxtDiv').text(HUMANTASK.taskDetails.taskPresentationDescription);
    jQuery('#taskOwnerTxt').text(HUMANTASK.taskDetails.taskOwner);

    if (HUMANTASK.taskDetails.taskCreatedBy != undefined && HUMANTASK.taskDetails.taskCreatedBy != null
            && HUMANTASK.taskDetails.taskCreatedBy.length > 0) {
        jQuery('#taskCreatedByTR').show();
        jQuery('#taskCreatedByTxt').text(HUMANTASK.taskDetails.taskCreatedBy);
    }
    jQuery('#taskSubjectTxt').text(HUMANTASK.taskDetails.taskPresentationSubject);

    if (HUMANTASK.taskDetails.taskPreviousStatus != undefined && HUMANTASK.taskDetails.taskPreviousStatus != null
            && HUMANTASK.taskDetails.taskPreviousStatus.length > 0) {
        jQuery('#taskPreviousStatusTR').show();
        jQuery('#taskPreviousStatusTxt').text(HUMANTASK.taskDetails.taskPreviousStatus);
    }

};

HUMANTASK.showHideActions = function() {

    if (HUMANTASK.authParams.authorisedToGetInput) {
        jQuery('#requestFieldSet').show();
    }

    if (HUMANTASK.authParams.authorisedToClaim) {
        jQuery('#claimLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToStart) {
        jQuery('#startLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToStop) {
        jQuery('#stopLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToRelease) {
        jQuery('#releaseLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToComment) {
        jQuery('#commentLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToDelegate) {
        jQuery('#delegateLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToSuspend) {
        jQuery('#suspendLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToResume) {
        jQuery('#resumeLinkLi').show();
    }

    if (HUMANTASK.authParams.authorisedToComplete) {
        jQuery('#responseFormFieldSet').show();
    }


};

HUMANTASK.showResponseFieldSet = function() {
    if (HUMANTASK.taskDetails.taskStatus == 'COMPLETED') {
        jQuery('#responseFieldSet').show();
    }
};

HUMANTASK.loadComments = function(taskId, taskClient) {
    jQuery('#commentsTab').empty();
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskComments';
    $.getJSON(page,
              function(commentJson) {
                  HUMANTASK.populateComments(commentJson);
              });
};

HUMANTASK.loadEvents = function(taskId, taskClient) {
    jQuery('#eventsTab').empty();
    var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + taskClient + '&taskId=' + taskId + '&loadParam=taskEvents';
    $.getJSON(page,
              function(eventJson) {
                  HUMANTASK.populateEvents(eventJson);
              });
};

HUMANTASK.populateComments = function (commentJSONMap) {

    $.each(commentJSONMap, function(commentId, commentJSON) {
        var commentDIV = HUMANTASK.createCommentDiv(commentJSON);
         jQuery('#commentsTab').append(commentDIV);
    });
};

HUMANTASK.populateEvents = function (eventJSONMap) {

    $.each(eventJSONMap, function(eventId, eventJSON) {
        var eventDiv = HUMANTASK.createEventDiv(eventJSON);
         jQuery('#eventsTab').append(eventDiv);
    });
};

HUMANTASK.createCommentDiv = function(commentJSON) {
    var commentDiv = '<div class="commentBox">';
    commentDiv +=   '<a>' + commentJSON.commentAddedBy + '</a> added a comment - ' + commentJSON.commentAddedTime + HUMANTASK.createDeleteCommentLink(commentJSON) ;
    commentDiv +=   '<div class="commentContent">' + commentJSON.commentText + '</div>' ;
    commentDiv += '</div>';

    return commentDiv;
};

HUMANTASK.createEventDiv = function(eventJSON) {
    //TODO construct this properly!!!
    var eventDiv = '<div class="commentBox">';
    eventDiv +=   '<b> User : </b> ' + eventJSON.eventInitiator + '<br>';
    eventDiv +=   '<b> Operation : </b> ' + eventJSON.eventType + '<br>';
    eventDiv +=   '<b> Time : </b> ' + eventJSON.eventTime + '<br>';


    if(eventJSON.oldState != eventJSON.newState) {
        eventDiv +=   '<b> Old State : </b> ' + eventJSON.oldState + '<br>';
        eventDiv +=   '<b> New State : </b> ' + eventJSON.newState + '<br>';
    }
    if(eventJSON.eventDetail) {
        eventDiv +=   '<b> Details : </b> ' + eventJSON.eventDetail + '<br>';
    }
    eventDiv += '</div>';

    return eventDiv;
};

HUMANTASK.createDeleteCommentLink = function(commentJSON) {
    return '<a onclick="HUMANTASK.deleteComment(' +  HUMANTASK.taskId +  ',' +  commentJSON.commentId + ')"> Delete</a>';
};

HUMANTASK.deleteComment = function (taskId, commentId) {

    var deleteCommentURL = 'task-operations-ajaxprocessor.jsp?operation=deleteComment&taskClient=' +
                           HUMANTASK.taskClient + '&taskId=' + taskId + '&commentId=' + commentId;
    $.getJSON(deleteCommentURL,
              function(json) {
                  if (json.CommentDeleted == 'true') {
                      //location.reload(true);
                      HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
                      jQuery("#commentsTab").focus();
                  } else {
                      alert('Error occurred while deleting comment : ' + json.CommentDeleted);
                      return true;
                  }
              });
};


HUMANTASK.bindButtons = function() {
    jQuery('#claimLink').click(HUMANTASK.claimTask);
    jQuery('#stopLink').click(HUMANTASK.stopTask);
    jQuery('#startLink').click(HUMANTASK.startTask);
    jQuery('#releaseLink').click(HUMANTASK.releaseTask);
    jQuery('#suspendLink').click(HUMANTASK.suspendTask);
    jQuery('#resumeLink').click(HUMANTASK.resumeTask);
    jQuery('#addCommentButton').click(HUMANTASK.addComment);
    jQuery('#completeTaskButton').click(HUMANTASK.completeTask);
    jQuery('#delegateButton').click(HUMANTASK.delegateTask);

};


HUMANTASK.claimTask = function() {
    var claimURL = 'task-operations-ajaxprocessor.jsp?operation=claim&taskClient=' +
                   HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(claimURL,
              function(json) {
                  if (json.TaskClaimed == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while claiming task : ' + json.TaskClaimed);
                      return true;
                  }
              });
};

HUMANTASK.startTask = function() {
    var startURL = 'task-operations-ajaxprocessor.jsp?operation=start&taskClient=' +
                   HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(startURL,
              function(json) {
                  if (json.TaskStarted == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while starting task : ' + json.TaskStarted);
                      return true;
                  }
              });
};

HUMANTASK.stopTask = function() {
    var stopURL = 'task-operations-ajaxprocessor.jsp?operation=stop&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(stopURL,
              function(json) {
                  if (json.TaskStopped == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while stopping task : ' + json.TaskStopped);
                      return true;
                  }
              });
};

HUMANTASK.releaseTask = function() {
    var releaseURL = 'task-operations-ajaxprocessor.jsp?operation=release&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(releaseURL,
              function(json) {
                  if (json.TaskReleased == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskReleased);
                      return true;
                  }
              });
};

HUMANTASK.suspendTask = function () {
    var suspendURL = 'task-operations-ajaxprocessor.jsp?operation=suspend&taskClient=' +
                     HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(suspendURL,
              function(json) {
                  if (json.TaskSuspended == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskSuspended);
                      return true;
                  }
              });
};

HUMANTASK.resumeTask = function() {
    var resumeURL = 'task-operations-ajaxprocessor.jsp?operation=resume&taskClient=' +
                    HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId;
    $.getJSON(resumeURL,
              function(json) {
                  if (json.TaskResumed == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskResumed);
                      return true;
                  }
              });
};

HUMANTASK.completeTask = function() {
    var OUTPUT_XML = createTaskOutput();
    var completeURL = 'task-operations-ajaxprocessor.jsp?operation=complete&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&payLoad=' + OUTPUT_XML;
    $.getJSON(completeURL,
              function(json) {
                  if (json.TaskCompleted == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while releasing task : ' + json.TaskCompleted);
                      return true;
                  }
              });
};

HUMANTASK.addComment = function() {
    var commentText =  jQuery('#commentTextAreaId').val();
    var addCommentURL = 'task-operations-ajaxprocessor.jsp?operation=addComment&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&commentText=' + commentText;
    $.getJSON(addCommentURL,
              function(json) {
                  if (json.CommentAdded == 'true') {
                      jQuery('#commentSection').hide();
                      HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
                      // clear the comment text area.
                      $('#commentTextAreaId').val('');
                      //focus on the current comments.
                      jQuery("#commentsTab").focus();
                  } else {
                      alert('Error occurred while adding comment to task : ' + json.CommentAdded);
                      return true;
                  }
              });
};

HUMANTASK.delegateTask = function() {
    var delegatee =  jQuery('#assignableUserList').val();
    var delegateURL = 'task-operations-ajaxprocessor.jsp?operation=delegate&taskClient=' +
                  HUMANTASK.taskClient + '&taskId=' + HUMANTASK.taskId + '&delegatee=' + delegatee;
    $.getJSON(delegateURL,
              function(json) {
                  if (json.TaskDelegated == 'true') {
                      location.reload(true);
                  } else {
                      alert('Error occurred while delageting task : ' + json.TaskDelegated);
                      return true;
                  }
              });
};

HUMANTASK.handleTabSelection = function (tabType) {

    if (tabType == 'commentsTab') {
        $('#eventsTab').hide();
        $('#eventTabLink').removeClass('selected');

        $('#commentsTab').show();
        $('#commentTabLink').addClass('selected');
        HUMANTASK.loadComments(HUMANTASK.taskId, HUMANTASK.taskClient);
    } else if (tabType == 'eventsTab') {
        $('#commentsTab').hide();
        $('#commentTabLink').removeClass('selected');

        $('#eventsTab').show();
        $('#eventTabLink').addClass('selected');
        HUMANTASK.loadEvents(HUMANTASK.taskId, HUMANTASK.taskClient);
    }
};

/**
 *
 * @param tabId
 */
HUMANTASK.handleDelegateSelection = function (tabId) {
    toggleMe(tabId);
    HUMANTASK.fillAssignableUsersList();
};

/**
 *
 */
HUMANTASK.fillAssignableUsersList = function () {

    // we need to do an ajax call only if the delegate section is visible.
    if ($('#delegateSection').is(":visible")) {
        var page = 'task-loading-ajaxprocessor.jsp?taskClient=' + HUMANTASK.taskClient +
                   '&taskId=' + HUMANTASK.taskId + '&loadParam=assignableUsers';
        $.getJSON(page,
                  function(eventJson) {
                      HUMANTASK.populateAssignableUserDropDown(eventJson);
                  });
    }
};

/**
 * Appends values to the people list drop down.
 * @param eventJson  user list json.
 */
HUMANTASK.populateAssignableUserDropDown = function (eventJson) {
    $('#assignableUserList').empty();
    $.each(eventJson, function(index, userNameJSON) {
        $('#assignableUserList').append(
                $('<option></option>').val(userNameJSON.userName).html(userNameJSON.userName)
        );
    });
};


