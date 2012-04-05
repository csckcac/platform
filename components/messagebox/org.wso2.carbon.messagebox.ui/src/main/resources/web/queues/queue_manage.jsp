<%@ page import="org.wso2.carbon.messagebox.ui.Constants" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.admin.QueueUserPermissionBean" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<script type="text/javascript" src="js/treecontrol.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>

<!--Yahoo includes for animations-->
<script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>

<!--Yahoo includes for menus-->
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript">
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


</script>


<%-- YUI Calendar includes--%>
<link rel="stylesheet" type="text/css" href="../yui/build/fonts/fonts-min.css"/>
<link rel="stylesheet" type="text/css" href="../yui/build/calendar/assets/skins/sam/calendar.css"/>
<script type="text/javascript" src="../yui/build/calendar/calendar-min.js"></script>

<style type="text/css">

    #cal1Container {
        display: none;
        position: absolute;
        font-size: 12px;
        z-index: 1
    }

    .defaultText {
        color: #666666;
        font-style: italic;
    }
</style>


<%
    if (session.getAttribute("queueUserPermission") == null || session.getAttribute("queue") == null) {
%>
<script type="text/javascript">
    location.href = 'queues.jsp';</script>
<%
        return;
    }
    QueueUserPermissionBean[] queueUserPermissions = null;
    if (session.getAttribute("queueUserPermission") != null) {
        queueUserPermissions = (QueueUserPermissionBean[]) session.getAttribute("queueUserPermission");

    }
    String queue = (String) session.getAttribute("queue");
    String createdFrom = request.getParameter("createdFrom");
    String queueSize = request.getParameter("queueSize");
    String messageCount = request.getParameter("messageCount");
    String createdTime = request.getParameter("createdTime");
    String updatedTime = request.getParameter("updatedTime");
    String permissionModel = "User";
%>
<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">
    <carbon:breadcrumb
            label="queue.manage"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <div id="middle">
        <div style="clear:both">&nbsp;</div>
        <h2><fmt:message key="queue.Details"/></h2>

        <div id="workArea">
            <div id="test">
                <table class="styledLeft" style="width:100%">
                    <thead>
                    <tr>
                        <th colspan="2">
                            <fmt:message key="queue.Details"/>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="queue.name"/></td>
                        <td><%=queue%>
                        </td>
                    </tr>
                    <tr>
                        <td>Queue Size</td>
                        <td><%=queueSize%>
                        </td>
                    </tr>
                    <tr>
                        <td>Message Count</td>
                        <td><%=messageCount%>
                        </td>
                    </tr>
                    <tr>
                        <td>Created Time</td>
                        <td><%=createdTime%>
                        </td>
                    </tr>
                    <tr>
                        <td>Updated Time</td>
                        <td><%=updatedTime%>
                        </td>
                    </tr>
                    <tr>
                        <td>Type</td>
                        <td><%
                            if (Constants.MB_QUEUE_CREATED_FROM_SQS_CLIENT.equals(createdFrom)) {
                        %>
                            <img src="../queues/images/queue_type_sqs.gif" alt="">

                            <%
                            } else if (Constants.MB_QUEUE_CREATED_FROM_AMQP.equals(createdFrom)) {
                            %>
                            <img src="../queues/images/queue_type_amqp.gif" alt="">
                            <%
                                }
                            %>
                        </td>
                    </tr>
                    </tbody>
                </table>


                <div style="clear:both">&nbsp;</div>

                <table class="styledLeft" style="width:100%" id="permissionsTable">
                    <strong>
                        <fmt:message key="permission.Details"/>
                    </strong>
                    <thead>
                    <tr>
                        <th><%=permissionModel %>
                        </th>
                        <th><fmt:message key="consume"/></th>
                        <th><fmt:message key="publish"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        if (permissionModel.equals("User")) {
                            if (queueUserPermissions != null) {
                                for (QueueUserPermissionBean queueUserPermission : queueUserPermissions) {
                    %>
                    <tr>
                        <td><%=queueUserPermission.getUserName()%>
                        </td>
                        <td><input type="checkbox"
                                   id="<%=queueUserPermission.getUserName()%>^subscribe"
                                   value="subscribe" <% if (queueUserPermission.getAllowedToConsume()) { %>
                                   checked <% } %></td>
                        <td><input type="checkbox"
                                   id="<%=queueUserPermission.getUserName()%>^publish"
                                   value="publish"  <% if (queueUserPermission.getAllowedToPublish()) { %>
                                   checked <% } %></td>
                    </tr>
                    <%
                                }
                            }
                        } %>
                    <tr>
                        <td colspan="3">
                            <input type="button" class="button"
                                   onclick="updatePermissions('<%=permissionModel %>','<%=createdFrom%>','<%=queueSize%>','<%=messageCount%>','<%=createdTime%>','<%=updatedTime%>')"
                                   value="Update Permissions">
                        </td>
                    </tr>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</fmt:bundle>
