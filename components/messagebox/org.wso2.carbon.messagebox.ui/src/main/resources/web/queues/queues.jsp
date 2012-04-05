<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messagebox.ui.Constants" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.QueueManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.admin.Queue" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--Yahoo includes for animations-->
<script src="../yui/build/animation/animation-min.js" type="text/javascript"></script>

<!--Yahoo includes for menus-->
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>


<!--Local js includes-->
<script type="text/javascript" src="js/treecontrol.js"></script>

<link href="css/tree-styles.css" media="all" rel="stylesheet"/>
<link href="css/dsxmleditor.css" media="all" rel="stylesheet"/>
<script type="text/javascript">
    function doDelete(queueName) {
        var theform = document.getElementById('deleteForm');
        theform.queueName.value = queueName;
        theform.submit();
    }
</script>

<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">
    <carbon:breadcrumb
            label="browse.queues"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>


    <jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

    <%
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                     session) + "QueueManagerAdminService.QueueManagerAdminServiceHttpsSoap12Endpoint";
        QueueManagerAdminServiceStub stub = new QueueManagerAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        int totalQueueCount = stub.getQueuesCount();
        int queueCountPerPage = 20;
        int pageNumber = 0;
        String pageNumberAsStr = request.getParameter("pageNumber");
        if (pageNumberAsStr != null) {
            pageNumber = Integer.parseInt(pageNumberAsStr);
        }
        int numberOfPages = (int) Math.ceil(((float) totalQueueCount) / queueCountPerPage);

        Queue[] queues = stub.getAllQueues(pageNumber*queueCountPerPage, queueCountPerPage);
    %>
    <%

        String queueName = request.getParameter("queueName");
        if (queueName != null) {
            try {
                stub.deleteQueue(queueName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Queue <%=queueName %> successfully deleted.', function
            () {
        location.href = 'queues.jsp';
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
    <div id="middle">
        <h2><fmt:message key="queue.list"/></h2>

        <div id="workArea">
            <%
                if (queues == null) {
            %>
            No queues are created.
            <%
            } else {

            %>
            <input type="hidden" name="pageNumber" value="<%=pageNumber%>"/>
            <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                              page="queues.jsp" pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
                              prevKey="prev" nextKey="next"
                              parameters="<%="test"%>"/>
            <table class="styledLeft" style="width:100%">
                <thead>
                <tr>
                    <th><fmt:message key="queue.name"/></th>
                    <th><fmt:message key="queue.depth"/></th>
                    <th><fmt:message key="queue.messageCount"/></th>
                    <th><fmt:message key="queue.created"/></th>
                    <th><fmt:message key="queue.updated"/></th>
                    <th><fmt:message key="queue.type"/></th>
                    <th style="width:30px;"><fmt:message key="queue.operations"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (queues != null) {
                        for (Queue queue : queues) {
                            long queueSize = queue.getQueueDepth();
                            String queueSizeWithPostFix;
                            if (queueSize > 1000000000) {
                                queueSizeWithPostFix = Long.toString(queueSize / 1000000000) + " GB";
                            } else if (queueSize > 1000000) {
                                queueSizeWithPostFix = Long.toString(queueSize / 1000000) + " MB";
                            } else if (queueSize > 1000) {
                                queueSizeWithPostFix = Long.toString(queueSize / 1000) + " KB";
                            } else {
                                queueSizeWithPostFix = Long.toString(queueSize) + " bytes";
                            }
                %>
                <tr>
                    <td><a href="#"
                           onclick="showManageQueueWindow('<%=queue.getQueueName()%>','<%=queue.getCreatedFrom()%>','<%=queueSizeWithPostFix%>','<%=queue.getMessageCount()%>','<%=queue.getCreatedTime().getTime().toString()%>','<%=queue.getUpdatedTime().getTime().toString()%>')"><%=queue.getQueueName()%>
                    </a></td>
                    <td><%=queueSizeWithPostFix%>
                    </td>
                    <td><%=queue.getMessageCount()%>
                    </td>
                    <td><%=queue.getCreatedTime().getTime().toString()%>
                    </td>
                    <td><%=queue.getUpdatedTime().getTime().toString()%>
                    </td>
                    <td>
                        <%
                            if (Constants.MB_QUEUE_CREATED_FROM_SQS_CLIENT.equals(queue.getCreatedFrom())) {
                        %>
                        <img src="../queues/images/queue_type_sqs.gif" alt="">

                        <%
                        } else if (Constants.MB_QUEUE_CREATED_FROM_AMQP.equals(queue.getCreatedFrom())) {
                        %>
                        <img src="../queues/images/queue_type_amqp.gif" alt="">
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <%
                            if (Constants.MB_QUEUE_CREATED_FROM_AMQP.equals(queue.getCreatedFrom())) {
                        %>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=queue.getQueueName()%>')">Delete</a>
                        <%
                            }
                        %>
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
            %>
            <div>
                <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                                 name="queueName"
                                                                                 value=""/></form>
            </div>
        </div>
    </div>

</fmt:bundle>