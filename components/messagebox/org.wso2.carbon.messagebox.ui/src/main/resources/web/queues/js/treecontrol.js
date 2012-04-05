var cal1;
YAHOO.util.Event.onDOMReady(function() {
    cal1 = new YAHOO.widget.Calendar("cal1", "cal1Container", { title:"Choose a date:", close:true });
    cal1.render();
    cal1.selectEvent.subscribe(calSelectHandler, cal1, true)
});
function showCalendar() {
    cal1.show();
}
function calSelectHandler(type, args, obj) {
    var selected = args[0];
    //var selDate = this.toDate(selected[0]);
    var selDate = args[0][0][0] + "/" + args[0][0][1] + "/" + args[0][0][2];
    var activeTime = document.getElementById("expirationTime");
    clearTextIn(activeTime);
    activeTime.value = selDate;
    cal1.hide();
}

var textValue = "";
function clearTextIn(obj) {
    if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
        YAHOO.util.Dom.removeClass(obj, 'initE');
        YAHOO.util.Dom.addClass(obj, 'normalE');
        textValue = obj.value;
        obj.value = "";
    }
}
function fillTextIn(obj) {
    if (obj.value == "") {
        obj.value = textValue;
        if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
            YAHOO.util.Dom.removeClass(obj, 'normalE');
            YAHOO.util.Dom.addClass(obj, 'initE');
        }
    }
}


function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var attributes = "";
    //Do minimizing for the rest of the nodes
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {

            if (allChildren[i].style.display == "none") {
                attributes = {
                    opacity: { to: 1 }
                };
                var anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.animate();
                allChildren[i].style.display = "";
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "plus");
                    YAHOO.util.Dom.addClass(icon, "minus");
                }
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                attributes = {
                    opacity: { to: 0 }
                };
                anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.duration = 0.3;
                anim.onComplete.subscribe(hideTreeItem, allChildren[i]);

                anim.animate();
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "minus");
                    YAHOO.util.Dom.addClass(icon, "plus");
                }
                todoOther = "hide";
                //parentNode.style.height = "50px";
            }
        }
    }
}

function showManageQueueWindow(queue, createdFrom, queueSize, messageCount, createdTime,
                               updatedTime) {

    var callback =
    {
        success:function(o) {
            if (o.responseText.search("SessionTimeOut") > 0) {
                location.href = "queues.jsp";
            } else if (o.responseText !== undefined) {
                location.href = 'queue_manage.jsp?createdFrom=' + createdFrom + '&queueSize=' + queueSize + '&messageCount=' + messageCount + "&createdTime=" + createdTime + "&updatedTime=" + updatedTime;
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "load_queue_details_from_bEnd_ajaxprocessor.jsp", callback, "queueName=" + queue + "&type=input" + "&createdFrom=" + createdFrom);
}


function hideTreeItem(state, opts, item) {
    item.style.display = "none";
}

function addQueueToBackEnd(queue, createdFrom) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.indexOf("Error") > -1) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                    });
                } else {
                    CARBON.showInfoDialog("" + o.responseText, function() {
                        location.href = "../queues/queues.jsp?createdFrom=" + createdFrom;
                    });
                }

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "add_queue_to_backend_ajaxprocessor.jsp", callback, "queue=" + queue + "&type=input");

}

function addQueue(createdFrom) {
    var topic = document.getElementById("queue");

    var error = "";

    if (topic.value == "") {
        error = "Queue can not be empty.\n";
    }
    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    addQueueToBackEnd(topic.value, createdFrom)
}

function addTopicFromManage() {
    var existingTopic = document.getElementById("existingTopic");
    var topic = document.getElementById("newTopic");
    var completeTopic = "";
    if (existingTopic.value == "/") {
        completeTopic = existingTopic.value + topic.value;
    } else {
        completeTopic = existingTopic.value + "/" + topic.value;
    }
    var error = "";

    if (topic.value == "") {
        error = "Topic can not be empty.\n";
    }
    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    addQueueToBackEnd(completeTopic)
}

function showCreateQueue() {

    var addSubTopicTable = document.getElementById("AddSubTopic");
    if (addSubTopicTable.style.display == "none") {
        addSubTopicTable.style.display = "";
    } else {
        addSubTopicTable.style.display = "none";
    }
}
function showAddSubscription() {
    var addPropertyTable = document.getElementById("userAdd");
    if (addPropertyTable.style.display == "none") {
        addPropertyTable.style.display = "";
    } else {
        addPropertyTable.style.display = "none";
    }
}

//'<%=permissionModel %>','<%=createdFrom%>','<%=queueSize%>','<%=messageCount%>','<%=createdTime%>','<%=updatedTime%>'
function updatePermissions(userOrRole, createdFrom, queueSize, messageCount, createdTime,
                           updatedTime) {
    var permissionTable = document.getElementById("permissionsTable");
    var rowCount = permissionTable.rows.length;
    var parameters = "";
    for (var i = 1; i < rowCount - 1; i++) {
        var roleName = permissionTable.rows[i].cells[0].innerHTML.replace(/^\s+|\s+$/g, "");
        var subscribeAllowed = permissionTable.rows[i].cells[1].getElementsByTagName("input")[0].checked;
        var publishAllowed = permissionTable.rows[i].cells[2].getElementsByTagName("input")[0].checked;
        if (i == 1) {
            parameters = roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        } else {
            parameters = parameters + roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        }
    }

    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if(o.responseText.search("Failed") == -1){
                    CARBON.showInfoDialog("" + o.responseText, function() {
                        location.href = "../queues/queue_manage.jsp?createdFrom=" + createdFrom + "&queueSize=" + queueSize + "&messageCount=" + messageCount + "&createdTime=" + createdTime + "&updatedTime=" + updatedTime;
                    });
                }else{
                    CARBON.showErrorDialog(o.responseText);
                }
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "update_role_permissions_ajaxprocessor.jsp", callback, "permissions=" + parameters + "&type=input" + "&userOrRole=" + userOrRole);
}


