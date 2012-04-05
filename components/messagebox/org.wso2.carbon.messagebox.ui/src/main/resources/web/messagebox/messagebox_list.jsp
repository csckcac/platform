<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.messagebox.stub.DeleteQueue" %>
<%@ page import="org.wso2.carbon.messagebox.stub.MessageQueueStub" %>
<%@ page
        import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceMessageBoxAdminExceptionException" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.xsd.MessageBoxDetail" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>

<script type="text/javascript">
    function doDelete(messageId) {
        var theform = document.getElementById('deleteForm');
        theform.messageboxId.value = messageId;
        theform.submit();
    }
</script>

<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            request="<%=request%>"/>


    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <%

        String messageboxID = request.getParameter("messageboxId");
        if (messageboxID != null) {
            MessageQueueStub messageQueueStub = UIUtils.getMessageServiceClient(config, session, request, messageboxID);
            try {
                messageQueueStub.deleteQueue(new DeleteQueue());
    %>
    <script type="text/javascript">CARBON.showInfoDialog('MessageBox <%=messageboxID %> successfully deleted.', function
            () {
        location.href = 'messagebox_list.jsp';
    });</script>
    <%

    } catch (AxisFault fault) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('<%=fault.getMessage()%>');

    </script>
    <%
            }
        }
    %>


    <carbon:breadcrumb
            label="messagebox.list"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key='available.message.boxes'/></h2>

        <div id="workArea">

            <%
                MessageBoxAdminServiceStub adminServiceStub = UIUtils.getMessageBoxAdminServiceStub(config, session, request);
                MessageBoxDetail[] messageBoxDetailArray;
                int totalMessageBoxCount = adminServiceStub.getMessageBoxesCount();
                int messageBoxCountPerPage = 20;
                int pageNumber = 0;
                String pageNumberAsStr = request.getParameter("pageNumber");
                if (pageNumberAsStr != null) {
                    pageNumber = Integer.parseInt(pageNumberAsStr);
                }
                int numberOfPages = (int) Math.ceil(((float) totalMessageBoxCount) / messageBoxCountPerPage);
                try {
                    messageBoxDetailArray = adminServiceStub.getAllMessageBoxes(pageNumber * messageBoxCountPerPage, messageBoxCountPerPage);
                    if (messageBoxDetailArray == null || messageBoxDetailArray.length == 0) {
            %>
            <fmt:message key='no.message.boxes.available'/>
            <%
            } else {
            %>
            <input type="hidden" name="pageNumber" value="<%=pageNumber%>"/>
            <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                              page="messagebox_list.jsp" pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
                              prevKey="prev" nextKey="next"
                              parameters="<%="test"%>"/>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key='messagebox.name'/></th>
                    <th><fmt:message key='messagebox.owner'/></th>
                    <th><fmt:message key='message.count'/></th>
                    <th><fmt:message key='end.point.url'/></th>
                    <th style="width:150px;"><fmt:message key='operations'/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    String backendUrl = UIUtils.getBackendServerUrl(config, session, request);
                    for (MessageBoxDetail aMessageBoxDetailArray : messageBoxDetailArray) {
                        if (aMessageBoxDetailArray != null) {
                            String messageboxId = aMessageBoxDetailArray.getMessageBoxId();
                            String messageboxName = aMessageBoxDetailArray.getMessageBoxName();
                            String messageboxOwner = aMessageBoxDetailArray.getOwner();
                %>
                <tr>
                    <td>
                        <a href="messagebox.jsp?messageboxName=<%=messageboxName%>&messageboxId=<%=messageboxId%>&messageboxOwner=<%=messageboxOwner%>"><%=messageboxName%>
                        </a>
                    </td>
                    <td><%=messageboxOwner%>
                    </td>
                    <td>
                        <%=aMessageBoxDetailArray.getNumberOfMessages() %>
                    </td>
                    <td>
                        <%  String[] epr =  aMessageBoxDetailArray.getEpr();
                            String queueUrl = null;
                            if (epr != null) {
                                for (String endPoint : epr) {
                                    queueUrl = endPoint + messageboxId;
                                    if (aMessageBoxDetailArray.getTenantDomain() != null) {
                                        queueUrl = endPoint +  messageboxId;
                                    }
                                    if(queueUrl.startsWith("http://")){
                                        break;
                                    }
                                }
                            }
                        %>
                        <%=queueUrl%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link" onclick="doDelete('<%=messageboxId%>')">Delete</a>
                        <a style="background-image: url(../admin/images/view.gif);"
                           class="icon-link"
                           href="messagebox.jsp?messageboxName=<%=messageboxName%>&messageboxId=<%=messageboxId%>&messageboxOwner=<%=messageboxOwner%>">View</a>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            <%
                }
            } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {
            %>
            <script type="text/javascript">CARBON.showErrorDialog('Failed to get all message boxes.<%=e%>');</script>
            <%
                    return;
                } %>

            <div>
                <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                                 name="messageboxId"
                                                                                 value=""/></form>
            </div>
        </div>
    </div>
</fmt:bundle>