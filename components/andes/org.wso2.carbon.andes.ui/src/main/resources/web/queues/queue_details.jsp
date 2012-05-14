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
        int numberOfPages =1;


        try {
            if (queueList != null) {
                totalQueueCount = queueList.length;
                numberOfPages =  (int) Math.ceil(((float) totalQueueCount) / queueCountPerPage);
            }
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
                    <th><fmt:message key="queue.messageCount"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (queueList != null) {
                        for (Queue queue : queueList) {
                %>
                <tr>
                    <td>
                    <%=queue.getQueueName()%>
                    </td>
                    <td><%=queue.getMessageCount()%>
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
             </div>
    </div>
</fmt:bundle>