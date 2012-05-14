<%@ page import="org.wso2.carbon.andes.stub.AndesAdminServiceStub" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.andes.stub.admin.types.Queue" %>
<%@ page import="org.wso2.carbon.andes.ui.Constants" %>
<%@ page import="org.wso2.carbon.andes.ui.UIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<fmt:bundle basename="org.wso2.carbon.andes.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.andes.ui.i18n.Resources"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <link rel="stylesheet" href="../qpid/css/dsxmleditor.css" />

    <%
        AndesAdminServiceStub stub = UIUtils.getAndesAdminServiceStub(config,session,request);

        Queue[] queueList =  stub.getAllQueues();
        long totalQueueCount;
        int queueCountPerPage = 20;
        int pageNumber = 0;
        String pageNumberAsStr = request.getParameter("pageNumber");
        if (pageNumberAsStr != null) {
            pageNumber = Integer.parseInt(pageNumberAsStr);
        }
        int numberOfPages;


        try {
            totalQueueCount = queueList.length;
            numberOfPages =  (int) Math.ceil(((float) totalQueueCount) / queueCountPerPage);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
            e.printStackTrace();
    %>

        %>
    <script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert("error");
    </script>
    <%
            return;
        }
    %>

    <script type="text/javascript">

        function updateWorkerLocationForQueue(queueName, index, successMessage)
        {
            var selectedNode = $('#combo'+index+' option:selected').val();
            $.ajax({
            url:'updateWorkers.jsp?queueName='+queueName+'&selectedNode='+selectedNode,
            async: false,
            dataType: "html",
            success:function(data) {
                 html = data;
                 var isSuccess = $(data).find('#workerReassignState').text();
                 if(isSuccess == successMessage)
                 {
                     var result = CARBON.showInfoDialog("Worker thread of"+queueName+"queue successfully moved to"+selectedNode,
                     function()
                     {
                         location.href = 'nodesList.jsp';
                     });
                 }
                 else
                {
                    CARBON.showErrorDialog("Failed moving "+queueName+" to "+selectedNode);
                }
            },
            failure:function(data) {
            if (data.responseText !== undefined) {
                CARBON.showErrorDialog("Error " + data.status + "\n Following is the message from the server.\n" + data.responseText);
            }
        }
        });
        }

    </script>

    <carbon:breadcrumb
    label="queues.list"
    resourceBundle="org.wso2.carbon.andes.ui.i18n.Resources"
    topPage="false"
    request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="queues.list"/></h2>
        <div id="workArea">

        <%
                if (queueList == null ) {
            %>
            No queues are created.
            <%
            } else {

            %>
            <input type="hidden" name="pageNumber" value="<%=pageNumber%>"/>
            <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                              page="queue_List.jsp" pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.andes.ui.i18n.Resources"
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
                    <th colspan="2"><fmt:message key="queue.worker"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (queueList != null) {
                        int index = 0;
                        for (Queue queue : queueList) {
                            String queueName = queue.getQueueName();
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
                    <td>
                    <%=queueName%>
                    </td>
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
                        <img src="images/queue_type_sqs.gif" alt="">

                        <%
                        } else if (Constants.MB_QUEUE_CREATED_FROM_AMQP.equals(queue.getCreatedFrom())) {
                        %>
                        <img src="images/queue_type_amqp.gif" alt="">
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a style="background-image: url(images/move.gif);"
                        class="icon-link" onclick="updateWorkerLocationForQueue('<%=queueName%>','<%=index%>','<%=Constants.SUCCESS%>')"></a>
                    </td>
                </tr>
                <%
                        }

                        index++;
                    }
                %>
                </tbody>
            </table>
            <%
                }
            %>
             </div>
    </div>
</fmt:bundle>