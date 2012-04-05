<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.ReceiveMessage" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%@ page import="java.math.BigInteger" %>
<%
    try {
        String messageBoxId = request.getParameter("messageboxId");
        String numberOfMessages = request.getParameter("numberOfMessages");
        String visibilityTimeout = request.getParameter("visibilityTimeout");
        if (messageBoxId != null && numberOfMessages != null && visibilityTimeout != null) {
            int messageCount = Integer.parseInt(numberOfMessages);
            int timeout = Integer.parseInt(visibilityTimeout);
            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageBoxId);
            ReceiveMessage receiveMessage = new ReceiveMessage();
            receiveMessage.setMaxNumberOfMessages(new BigInteger(Integer.toString(messageCount)));
            receiveMessage.setVisibilityTimeout(new BigInteger(Integer.toString(timeout)));
            messageQueueStub.receiveMessage(receiveMessage);
        }

    } catch (AxisFault fault) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=fault.getMessage()%>');

</script>
<%
    }
%>