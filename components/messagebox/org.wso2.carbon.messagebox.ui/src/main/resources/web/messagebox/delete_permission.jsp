<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.RemovePermission" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%
    try {
        String messageBoxId = request.getParameter("messageboxId");
        String permissionLabel = request.getParameter("permissionLabel");
        if (messageBoxId != null && permissionLabel != null) {
            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageBoxId);
            RemovePermission removePermission = new RemovePermission();
            removePermission.setLabel(permissionLabel);
            messageQueueStub.removePermission(removePermission);
        }

    } catch (AxisFault fault) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=fault.getMessage()%>');

</script>
<%
    }
%>