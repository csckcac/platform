<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.ui.Constants" %>
<%@ page import="org.wso2.carbon.messagebox.stub.AddPermission" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%

    String messageBoxId = request.getParameter("messageboxId");
    String permissionLabel = request.getParameter("permissionLabel");
    String sharedUser = request.getParameter("sharedUser");
    String sendAllowed = request.getParameter("sendAllowed");
    String receiveAllowed = request.getParameter("receiveAllowed");
    String deleteAllowed = request.getParameter("deleteAllowed");
    String changeVisibilityAllowed = request.getParameter("changeVisibilityAllowed");
    String getMessageBoxAttributeAllowed = request.getParameter("getMessageBoxAttributeAllowed");

    if (messageBoxId != null && permissionLabel != null) {
        MessageBoxAdminServiceStub messageBoxAdminServiceStub = UIUtils.getMessageBoxAdminServiceStub(config, session, request);

        String[] sharedUsers = {messageBoxAdminServiceStub.getSQSKeys(sharedUser).getAccessKeyId()};
        List<String> allowedOperations = new ArrayList<String>();
        if (sendAllowed != null) {
            allowedOperations.add(Constants.SQS_OPERATION_SEND_MESSAGE);
        }
        if (receiveAllowed != null) {
            allowedOperations.add(Constants.SQS_OPERATION_RECEIVE_MESSAGE);
        }
        if (deleteAllowed != null) {
            allowedOperations.add(Constants.SQS_OPERATION_DELETE_MESSAGE);
        }
        if (changeVisibilityAllowed != null) {
            allowedOperations.add(Constants.SQS_OPERATION_CHANGE_MESSAGE_VISIBILITY);
        }
        if (getMessageBoxAttributeAllowed != null) {
            allowedOperations.add(Constants.SQS_OPERATION_GET_QUEUE_ATTRIBUTES);
        }

        try {

            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageBoxId);
            AddPermission addPermission = new AddPermission();
            addPermission.setLabel(permissionLabel);
            addPermission.setActionName(allowedOperations.toArray(new String[allowedOperations.size()]));
            addPermission.setAWSAccountId(sharedUsers);
            messageQueueStub.addPermission(addPermission);
        } catch (AxisFault fault) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=fault.getMessage()%>');

</script>
<%
        }
    }

%>