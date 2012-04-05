<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.messagebox.ui.Constants" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceMessageBoxAdminExceptionException" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.admin.MessageDetails" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.admin.PermissionLabel" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.UserAdminStub" %>
<!--Yahoo includes for dom event handling-->

<!--Yahoo includes for animations-->
<script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<!--Yahoo includes for menus-->
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">
<script type="text/javascript">
<%
String messageboxID = request.getParameter("messageboxId");
String messageboxName = request.getParameter("messageboxName");
    String messageboxOwner = request.getParameter("messageboxOwner");
    if (messageboxID != null && messageboxOwner != null && messageboxName != null) {
    %>
YAHOO.util.Event.onAvailable("JScript", function() {
    editAreaLoader.init({
                            id : "JScript"        // textarea id
                            ,syntax: "js"            // syntax to be uses for highgliting
                            ,start_highlight: true        // to display with highlight mode on start-up
                            ,allow_resize: "both"
                            ,min_height:250
                        });
});
function handleFocus(obj, txt) {
    if (obj.value == txt) {
        obj.value = '';
        YAHOO.util.Dom.removeClass(obj, 'defaultText');

    }
}
function handleBlur(obj, txt) {
    if (obj.value == '') {
        obj.value = txt;
        YAHOO.util.Dom.addClass(obj, 'defaultText');
    }
}
YAHOO.util.Event.onDOMReady(
        function() {
            document.getElementById("hhid").value = "HH";
            document.getElementById("mmid").value = "mm";
            document.getElementById("ssid").value = "ss";
        }
        )
function addMessage(messageBoxId, messageboxName, messageboxOwner, messageBody) {
    if (messageBody.value.replace(/^\s+|\s+$/g, "") == "") {
        CARBON.showErrorDialog("<fmt:message key='error.empty.message'/>");
        return;
    }
    var callback =
    {
        success:function(o) {
            location.href = 'messagebox.jsp?' + "messageboxId=" + messageBoxId + "&messageboxOwner=" + messageboxOwner + "&messageboxName=" + messageboxName;
            if (o.responseText !== undefined) {

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    YAHOO.util.Connect.asyncRequest('POST', "add_message.jsp", callback, "messageboxId=" + messageBoxId + "&messageBody=" + messageBody.value);
}
function retrieveMessage(messageBoxId, messageboxName, messageboxOwner, numberOfMessages,
                         visibilityTimeout) {
    numberOfMessages = numberOfMessages.value.replace(/^\s+|\s+$/g, "");
    var messageCountToReceived = parseInt(numberOfMessages);
    if (isNaN(messageCountToReceived)) {
        CARBON.showErrorDialog("<fmt:message key='error.invalid.message.count'/>");
        return;
    }
    if (messageCountToReceived > 10 || messageCountToReceived < 1) {
        CARBON.showErrorDialog("<fmt:message key='error.invalid.message.count.value'/>");
        return;
    }
    visibilityTimeout = visibilityTimeout.value.replace(/^\s+|\s+$/g, "");
    var visibilityTimeoutValue = parseInt(visibilityTimeout);
    if (isNaN(visibilityTimeoutValue)) {
        CARBON.showErrorDialog("<fmt:message key='error.invalid.visibility.timeout.value'/>");
        return;
    }
    visibilityTimeoutValue = visibilityTimeoutValue * 1000;

    var callback =
    {
        success:function(o) {
            location.href = 'messagebox.jsp?' + "messageboxId=" + messageBoxId + "&messageboxOwner=" + messageboxOwner + "&messageboxName=" + messageboxName;
            if (o.responseText !== undefined) {

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    YAHOO.util.Connect.asyncRequest('POST', "receive_message.jsp", callback, "messageboxId=" + messageBoxId + "&numberOfMessages=" + messageCountToReceived + "&visibilityTimeout=" + visibilityTimeoutValue);
}
function deleteMessage(messageBoxId, messageboxName, messageboxOwner, receiptHandler) {
    var callback =
    {
        success:function(o) {
            location.href = 'messagebox.jsp?' + "messageboxId=" + messageBoxId + "&messageboxOwner=" + messageboxOwner + "&messageboxName=" + messageboxName;
            if (o.responseText !== undefined) {

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    YAHOO.util.Connect.asyncRequest('POST', "delete_message.jsp", callback, "messageboxId=" + messageBoxId + "&receiptHandler=" + receiptHandler);
}

function addPermission(messageBoxId, messageboxName, messageboxOwner) {
    var permissionLabel = "";
    // generating a name for permission label
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 10; i++) {
        permissionLabel += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    var sharedUserId = document.getElementById("carbonUsersId");
    var sharedUser = sharedUserId.options[sharedUserId.selectedIndex].value;
    var sendAllowed = document.getElementById("optionSend").checked;
    var receiveAllowed = document.getElementById("optionReceive").checked;
    var deleteAllowed = document.getElementById("optionDelete").checked;
    var changeVisibilityAllowed = document.getElementById("optionChangeVisibility").checked;
    var getMessageBoxAttributeAllowed = document.getElementById("optionGetQueueAttributes").checked;

    var parameters = "";


    if (sendAllowed) {
        parameters += "&sendAllowed=" + sendAllowed;
    }
    if (receiveAllowed) {
        parameters += "&receiveAllowed=" + receiveAllowed;
    }
    if (deleteAllowed) {
        parameters += "&deleteAllowed=" + deleteAllowed;
    }
    if (changeVisibilityAllowed) {
        parameters += "&changeVisibilityAllowed=" + changeVisibilityAllowed;
    }
    if (getMessageBoxAttributeAllowed) {
        parameters += "&getMessageBoxAttributeAllowed=" + getMessageBoxAttributeAllowed;
    }
    var callback =
    {
        success:function(o) {
            location.href = 'messagebox.jsp?' + "messageboxId=" + messageBoxId + "&messageboxOwner=" + messageboxOwner + "&messageboxName=" + messageboxName;
            if (o.responseText !== undefined) {

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    YAHOO.util.Connect.asyncRequest('POST', "add_permission.jsp", callback, "messageboxId=" + messageBoxId + "&permissionLabel=" + permissionLabel +
                                                                            "&sharedUser=" + sharedUser + parameters);
}

function deletePermission(messageBoxId, messageboxName, messageboxOwner, permissionLabel) {
    var callback =
    {
        success:function(o) {
            location.href = 'messagebox.jsp?' + "messageboxId=" + messageBoxId + "&messageboxOwner=" + messageboxOwner + "&messageboxName=" + messageboxName;
            if (o.responseText !== undefined) {

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    YAHOO.util.Connect.asyncRequest('POST', "delete_permission.jsp", callback, "messageboxId=" + messageBoxId + "&permissionLabel=" + permissionLabel);
}


function showHideMessageAddArea() {
    var divElement = document.getElementById('messageAddArea');
    if (divElement.style.display == "none") {
        divElement.style.display = "";
    } else {
        divElement.style.display = "none";
    }
}

function showHideMessageRetrieveArea() {
    var divElement = document.getElementById('messageRetrieveArea');
    if (divElement.style.display == "none") {
        divElement.style.display = "";
    } else {
        divElement.style.display = "none";
    }
}
function showHidePermissionAddArea() {
    var divElement = document.getElementById('permissionAddArea');
    if (divElement.style.display == "none") {
        divElement.style.display = "";
    } else {
        divElement.style.display = "none";
    }
}

</script>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<carbon:breadcrumb
        label="messagebox.view"
        resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<div id="middle">
<h2><fmt:message key='content.of'/> <%=messageboxName%>
</h2>

<div id="workArea">

<div style="clear:both">&nbsp;</div>
<strong>Retrievable Messages</strong>

<div style="padding-left:10px;">
    <a style="background-image: url(../admin/images/add.gif);"
       class="icon-link spacer"
       onclick="showHideMessageAddArea()"><fmt:message key='add.messages'/></a>

    <div style="clear:both"></div>

    <table class="styledLeft" id="messageAddArea" style="display:none; width:100%">
        <thead>
        <tr>
            <th colspan="2">Add Message</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="leftCol-med"><fmt:message key='message.text1'/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="addMessage" id="addMessageId"
                       class="initE"
                       value=""
                       title="Message content you need to send to this message box"/>
            </td>

        </tr>
        <tr>
            <td class="buttonRow" colspan="2">
                <input type="button" value="Put Message" class="button"
                       onclick="addMessage('<%=messageboxID%>','<%=messageboxName%>','<%=messageboxOwner%>', document.getElementById('addMessageId'))"/>
            </td>
        </tr>
        </tbody>
    </table>
    <div style="clear:both">&nbsp;</div>
    <table style="width:100%">
        <tr>
            <td>
                Added Messages
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='message.id'/></th>
                        <th><fmt:message key='message.text'/></th>
                        <th><fmt:message key='sent.timestamp'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        MessageBoxAdminServiceStub adminServiceStub = UIUtils.getMessageBoxAdminServiceStub(config, session, request);
                        MessageDetails[] messageDetailsArray = new MessageDetails[0];
                        try {
                            messageDetailsArray = adminServiceStub.getAllAvailableMessages(messageboxID);
                        } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {


                    %>
                    <script type="text/javascript">
                        CARBON.showErrorDialog('<%= e.getFaultMessage().getMessageBoxAdminException().getErrorMessage()%>');

                    </script>
                    <%
                            return;
                        }
                        if (messageDetailsArray != null) {
                            for (MessageDetails messageDetails : messageDetailsArray) {
                                if (messageDetails != null) {
                    %>
                    <tr>
                        <td>
                            <%=messageDetails.getMessageId()%>
                        </td>
                        <td>
                            <%=UIUtils.getHtmlString(messageDetails.getMessageBody())%>
                        </td>
                        <td>
                            <%=messageDetails.getSentTimestamp()%>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="3">No messages are added.</td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</div>
<div style="clear:both">&nbsp;</div>
<strong>Retrieved Messages</strong>

<div style="padding-left:10px;">
    <a style="background-image: url(../messagebox/images/retrieve_messages.png);"
       class="icon-link spacer"
       onclick="showHideMessageRetrieveArea()"><fmt:message key='retrieve.messages'/></a>

    <div style="clear:both"></div>
    <table class="styledLeft" id="messageRetrieveArea" style="display:none; width:100%">
        <thead>
        <tr>
            <th colspan="3">Retrieved Messages</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="leftCol-med"><fmt:message key='visibility.timeout.sec'/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="retrieveVisibilityValue"
                       id="retrieveVisibilityId"
                       class="initE"
                       value=""
                       title="Visibility time out for this messages"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med"><fmt:message key='number.of.messages.to.retrieve'/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="retrieveMessage" id="retrieveMessageId"
                       class="initE"
                       value=""
                       title="Number of messages you want to receive from this message box"/>
            </td>
        </tr>
        <tr>
            <td class="buttonRow" colspan="2">
                <input type="button" class="button"
                       value="<fmt:message key='button.retrieve.messages'/>"
                       onclick="retrieveMessage('<%=messageboxID%>','<%=messageboxName%>','<%=messageboxOwner%>',document.getElementById('retrieveMessageId'), document.getElementById('retrieveVisibilityId'))"/>
            </td>
        </tr>
        </tbody>
    </table>

    <div style="clear:both">&nbsp;</div>
    <table style="width:100%" id="receivedMessageArea">
        <tr>
            <td>
                    <%--<fmt:message key='retrieved.messages.from'/> <%=messageboxName%>--%>
                Retrieved Messages
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='message.receipt.handler'/></th>
                        <th><fmt:message key='message.text'/></th>
                        <th><fmt:message key='visibility.timeout'/></th>
                        <th><fmt:message key='delete.message'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        MessageDetails[] retrievedMessageDetailsArray = new MessageDetails[0];
                        try {
                            retrievedMessageDetailsArray = adminServiceStub.getAllRetrievedMessages(messageboxID);
                        } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {
                    %>
                    <script type="text/javascript">
                        CARBON.showErrorDialog('<%=e.getFaultMessage().getMessageBoxAdminException().getErrorMessage()%>');

                    </script>
                    <%
                            return;
                        }
                        if (retrievedMessageDetailsArray != null) {
                            for (MessageDetails messageDetails : retrievedMessageDetailsArray) {
                    %>
                    <tr>
                        <td>
                            <%=messageDetails.getReceiptHandler()%>
                        </td>
                        <td>
                            <%=UIUtils.getHtmlString(messageDetails.getMessageBody())%>
                        </td>
                        <td>
                            <%=messageDetails.getDefaultVisibilityTimeout()%>
                        </td>
                        <td class="buttonRow">
                            <a style="background-image: url(../admin/images/delete.gif);"
                               class="icon-link"
                               onclick="deleteMessage('<%=messageboxID%>','<%=messageboxName%>','<%=messageboxOwner%>','<%=messageDetails.getReceiptHandler()%>')">Delete</a>
                        </td>
                    </tr>
                    <%
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="4">No messages are retrieved.</td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</div>

<div style="clear:both">&nbsp;</div>
<strong>Permissions</strong>

<div style="padding-left:10px;">
    <a style="background-image: url(../admin/images/add.gif);"
       class="icon-link spacer"
       onclick="showHidePermissionAddArea()"><fmt:message key='add.permissions'/></a>

    <div style="clear:both"></div>
    <table id="permissionAddArea" style="display:none; width:100%" class="styledLeft">
        <%
            UserAdminStub userAdminStub = UIUtils.getUserAdminServiceClient(config, session, request);
            String currentUser = session.getAttribute("logged-user").toString();
            if (userAdminStub != null) {
                String[] users = null;
                try {
                    users = userAdminStub.listUsers("*");
                } catch (Exception e) {
                    //ignore exception as this will disallow logged in user to set permissions on message boxes.
                }
                if (users == null) {
        %>
        <thead>
        <tr>
            <th colspan="6">No users for sharing the message box. User has no permission to get users list.</th>
        </tr>
        </thead>
        <%
        } else if (users != null && users.length == 1) {
        %>
        <thead>
        <tr>
            <th colspan="6">No users for sharing the message box.</th>
        </tr>
        </thead>
        <%
        } else {
        %>
        <thead>
        <tr>
            <th colspan="6">Add Permission</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td style="width:175px;" class="leftCol-med">User :<span
                    class="required">*</span>
                <select name="carbonUsers" id="carbonUsersId">
                    <%
                        if (users != null) {
                            for (String user : users) {
                                if (!user.equalsIgnoreCase(currentUser)) {

                    %>
                    <option value="<%=user%>"><%=user%>
                    </option>
                    <%
                                }
                            }
                        }
                    %>
                </select>
            </td>

            <td>
                <input type="checkbox" id="optionSend" value="Send"> <fmt:message
                    key='send'/>
            </td>
            <td>
                <input type="checkbox" id="optionReceive" value="Receive">
                <fmt:message key='receive'/>
            </td>
            <td>
                <input type="checkbox" id="optionDelete" value="Delete"> <fmt:message
                    key='delete'/>
            </td>
            <td>
                <input type="checkbox" id="optionChangeVisibility"
                       value="ChangeVisibility"><fmt:message key='change.visibility'/>
            </td>
            <td>
                <input type="checkbox" id="optionGetQueueAttributes"
                       value="GetQueueAttributes"> <fmt:message
                    key='get.messagebox.attributes'/>
            </td>

        </tr>
        <tr>
            <td class="buttonRow" colspan="6">
                <input type="button" class="button" value="<fmt:message key='add.permission'/>"
                       onclick="addPermission('<%=messageboxID%>','<%=messageboxName%>', '<%=messageboxOwner%>')"/>
            </td>
        </tr>
        </tbody>
        <%
                }
            }
        %>

    </table>
    <div style="clear:both">&nbsp;</div>

    <table style="width:100%" class="styledLeft">
        <fmt:message key='permissions.on'/>
        <thead>
        <tr>
            <th><fmt:message key='shared.user'/></th>
            <th><fmt:message key='send'/></th>
            <th><fmt:message key='receive'/></th>
            <th><fmt:message key='delete'/></th>
            <th><fmt:message key='change.visibility'/></th>
            <th><fmt:message key='get.attributes'/></th>
            <th><fmt:message key='permission.label'/></th>
            <th><fmt:message key='delete.permission'/></th>
        </tr>
        </thead>
        <tbody>
        <%
            PermissionLabel[] permissionLabels = adminServiceStub.getAllPermissions(messageboxID);
            if (permissionLabels != null) {
                for (PermissionLabel permissionLabel : permissionLabels) {
                    if (permissionLabel.getSharedUsers() != null && !permissionLabel.getSharedUsers()[0].equals(currentUser)) {
                        boolean sendAllowed = false;
                        boolean receiveAllowed = false;
                        boolean deleteAllowed = false;
                        boolean changeVisibilityAllowed = false;
                        boolean getQueueAttributeAllowed = false;
                        for (String operation : permissionLabel.getOperations()) {
                            if (operation.equals(Constants.SQS_OPERATION_SEND_MESSAGE)) {
                                sendAllowed = true;
                            } else if (operation.equals(Constants.SQS_OPERATION_RECEIVE_MESSAGE)) {
                                receiveAllowed = true;
                            } else if (operation.equals(Constants.SQS_OPERATION_DELETE_MESSAGE)) {
                                deleteAllowed = true;
                            } else if (operation.equals(Constants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY)) {
                                changeVisibilityAllowed = true;
                            } else if (operation.equals(Constants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES)) {
                                getQueueAttributeAllowed = true;
                            }
                        }
        %>
        <tr>
            <td>
                <%=permissionLabel.getSharedUsers()[0]%>
            </td>
            <td>
                <input type="checkbox" name="option1"
                       value="Send" <%if(sendAllowed){%> checked<%}%>>
            </td>
            <td>
                <input type="checkbox" name="option2"
                       value="Receive" <%if(receiveAllowed){%> checked<%}%>>

            </td>
            <td>
                <input type="checkbox" name="option3"
                       value="Delete"<%if(deleteAllowed){%> checked<%}%>>
            </td>
            <td>
                <input type="checkbox" name="option4"
                       value="ChangeVisibility"<%if(changeVisibilityAllowed){%>
                       checked<%}%>>

            </td>
            <td>
                <input type="checkbox" name="option5"
                       value="GetQueueAttributes"<%if(getQueueAttributeAllowed){%>
                       checked<%}%>>

            </td>
            <td>
                <%=permissionLabel.getLabelName()%>
            </td>

            <td class="buttonRow">
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="deletePermission('<%=messageboxID%>','<%=messageboxName%>','<%=messageboxOwner%>', '<%=permissionLabel.getLabelName()%>')">Delete</a>
            </td>
        </tr>
        <%
                }
            }
        } else {
        %>
        <tr>
            <td colspan="8">No permissions are defined.</td>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>

</div>
</div>
</div>


<%
    }
%>
</fmt:bundle>
