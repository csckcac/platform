<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">


    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <!--Yahoo includes for animations-->
    <script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>
    <script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
    <script type="text/javascript" src="../yui/build/event/event-min.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <!--Yahoo includes for menus-->
    <link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
    <script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
    <script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>


    <script type="text/javascript">
        function addMessageBox() {
            var messageBoxName = document.getElementById("messageBoxId").value.replace(/^\s+|\s+$/g, "");
            var defaultVisibilityTimeout = document.getElementById("visibilityTimeoutId").value.replace(/^\s+|\s+$/g, "");
            defaultVisibilityTimeout = parseInt(defaultVisibilityTimeout);

            if (messageBoxName == "") {
                CARBON.showErrorDialog("<fmt:message key='error.messagebox.name.empty'/>");
                return;
            }
            var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
            // Check for white space
            if (!reWhiteSpace.test(messageBoxName)) {
                CARBON.showErrorDialog("<fmt:message key='error.whitespaces.not.allowed'/>");
                return;
            }
            if (isNaN(defaultVisibilityTimeout)) {
                CARBON.showErrorDialog("<fmt:message key='error.visibility.timeout.invalid'/>");
                return;
            }


            var callback =
            {
                success:function(o) {
                    if (o.responseText !== undefined) {
                        if(o.responseText.search("Message box already exists with name")==-1){
                            location.href = 'messagebox_list.jsp?';
                            document.getElementById('addMessageBoxId').submit();
                        } else{
                            CARBON.showErrorDialog("Error:  " + o.responseText);
                        }
                    }
                },
                failure:function(o) {
                    if (o.responseText !== undefined) {
                        CARBON.showErrorDialog("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
                    }
                }
            };
            YAHOO.util.Connect.asyncRequest('POST', "add_messagebox_ajaxprocessor.jsp", callback, "messageBoxName=" + messageBoxName + "&visibilityTimeout=" + defaultVisibilityTimeout);

        }

    </script>
     <carbon:breadcrumb
            label="messagebox.add"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key='create.new.messagebox'/></h2>
        <div id="workArea">
            <form name="inputForm" action="messagebox_list.jsp" method="get" id="addMessageBoxId">
                <table style="width:100%" id="brokerAdd" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='enter.messagebox.details'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="messageboxCreateTable" class="normal-nopadding"
                                   style="width:100%">
                                <tbody>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key='messagebox.name'/><span
                                            class="required">*</span>
                                    </td>
                                    <td><input type="text" name="messageBoxName" id="messageBoxId"
                                               class="initE"
                                               value=""
                                               title="Message box name you need to create"/></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key='default.visibility.timeout'/><span
                                            class="required">*</span>
                                    </td>
                                    <td><input type="text" name="visibilityTimeout"
                                               id="visibilityTimeoutId"
                                               class="initE"
                                               value=""
                                               title="Set the visibility time out for the message box"/>
                                    </td>
                                </tr>

                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Create Message Box" class="button"
                                   onclick="addMessageBox()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>


            </form>
        </div>
    </div>
</fmt:bundle>