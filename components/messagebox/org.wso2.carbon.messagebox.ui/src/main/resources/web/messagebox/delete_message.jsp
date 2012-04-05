<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.DeleteMessage" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%
    try {
        String messageBoxId = request.getParameter("messageboxId");
        String receiptHandler = request.getParameter("receiptHandler");
        if (messageBoxId != null && receiptHandler != null) {
            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageBoxId);
            DeleteMessage deleteMessage = new DeleteMessage();
            String[] receiptHandlers = {receiptHandler};
            deleteMessage.setReceiptHandle(receiptHandlers);
            messageQueueStub.deleteMessage(deleteMessage);
        }

    } catch (AxisFault fault) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=fault.getMessage()%>');

</script>
<%
    }
%>