<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.SendMessage" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%
    try {
        String messageBoxId = request.getParameter("messageboxId");
        String messageBody = request.getParameter("messageBody");
        if (messageBoxId != null && messageBody != null && !messageBody.trim().equals("")) {
            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageBoxId);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setMessageBody(messageBody);
            messageQueueStub.sendMessage(sendMessage);
        }

    } catch (AxisFault fault) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=fault.getMessage()%>');

</script>
<%
    }
%>