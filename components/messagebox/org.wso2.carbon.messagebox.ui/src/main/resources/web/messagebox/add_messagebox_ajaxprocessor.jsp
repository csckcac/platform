<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.CreateQueue" %>
<%@ page import="org.wso2.carbon.messagebox.stub.QueueServiceStub" %>
<%@ page
        import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceMessageBoxAdminExceptionException" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.xsd.MessageBoxDetail" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%@ page import="java.math.BigInteger" %>
<%
    String messageBoxName = request.getParameter("messageBoxName");
    String defaultVisibilityTimeout = request.getParameter("visibilityTimeout");

    MessageBoxAdminServiceStub adminServiceStub = UIUtils.getMessageBoxAdminServiceStub(config, session, request);
    MessageBoxDetail[] messageBoxDetailArray = new MessageBoxDetail[0];
    try {
        int maxMessageBoxCount = adminServiceStub.getMessageBoxesCount();
        messageBoxDetailArray = adminServiceStub.getAllMessageBoxes(0,maxMessageBoxCount);
    } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=e.getFaultMessage().getMessageBoxAdminException().getErrorMessage()%>');

</script>
<%
        return;
    }

    String msg = "true";
    boolean messageBoxExist = false;
    if (messageBoxDetailArray != null) {
        for (MessageBoxDetail messageBoxDetail : messageBoxDetailArray) {
            if (messageBoxDetail.getMessageBoxId().equals("admin/" + messageBoxName)) {
                messageBoxExist = true;
                break;
            }
        }
    }

    if (messageBoxExist) {
        msg = "Message box already exists with name: " + messageBoxName;
    } else {
        QueueServiceStub queueServiceStub = UIUtils.getQueueServiceClient(config, session, request);
        CreateQueue createQueue = new CreateQueue();
        createQueue.setQueueName(messageBoxName);
        createQueue.setDefaultVisibilityTimeout(new BigInteger(defaultVisibilityTimeout));
        try {
            queueServiceStub.createQueue(createQueue);
        } catch (AxisFault e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=e.getMessage()%>');

</script>
<%
        }
    }


%>
<%=msg%>